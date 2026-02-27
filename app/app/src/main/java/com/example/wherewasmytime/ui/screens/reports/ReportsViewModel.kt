import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wherewasmytime.WhereWasMyTimeApp
import com.example.wherewasmytime.data.local.entity.CategoryEntity
import com.example.wherewasmytime.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayStats(
    val dayLabel: String,   // "Pzt", "Sal" vb.
    val minutes: Int
)

data class CategoryStats(
    val category: CategoryEntity,
    val totalMinutes: Int,
    val percentage: Float
)

data class ReportsUiState(
    val selectedFilter: ReportFilter = ReportFilter.WEEK,
    val dayStats: List<DayStats> = emptyList(),
    val categoryStats: List<CategoryStats> = emptyList(),
    val totalMinutes: Int = 0,
    val avgMinutesPerDay: Int = 0,
    val longestSession: Int = 0,
    val sessionCount: Int = 0,
    val rawSessions: List<SessionEntity> = emptyList(),
    val currentCategories: Map<Long, String> = emptyMap()
)

enum class ReportFilter { WEEK, MONTH }

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as WhereWasMyTimeApp).repository

    private val _filter = MutableStateFlow(ReportFilter.WEEK)
    val filter: StateFlow<ReportFilter> = _filter.asStateFlow()

    val uiState: StateFlow<ReportsUiState> = combine(
        _filter,
        repository.getAllCategories()
    ) { filter, allCategories ->
        Pair(filter, allCategories)
    }.flatMapLatest { (filter, allCategories) ->
        val (startMs, endMs) = getRange(filter)
        repository.getSessionsInRange(startMs, endMs).map { sessions ->
            buildUiState(filter, sessions, allCategories, startMs, endMs)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState()
    )

    fun setFilter(filter: ReportFilter) {
        _filter.value = filter
    }

    private fun getRange(filter: ReportFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val endMs = cal.timeInMillis

        when (filter) {
            ReportFilter.WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            ReportFilter.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
        }
        return Pair(cal.timeInMillis, endMs)
    }

    private fun buildUiState(
        filter: ReportFilter,
        sessions: List<SessionEntity>,
        allCategories: List<CategoryEntity>,
        startMs: Long,
        endMs: Long
    ): ReportsUiState {
        val catMap = allCategories.associateBy { it.id }
        val totalMinutes = sessions.sumOf { it.durationMinutes }

        // Günlük dağılım
        val dayStats = buildDayStats(filter, sessions, startMs)

        // Kategori dağılımı
        val categoryMinutes = sessions
            .filter { it.categoryId != null }
            .groupBy { it.categoryId!! }
            .mapValues { (_, s) -> s.sumOf { it.durationMinutes } }

        val categoryStats = categoryMinutes.entries
            .sortedByDescending { it.value }
            .mapNotNull { (catId, mins) ->
                val cat = catMap[catId] ?: return@mapNotNull null
                CategoryStats(
                    category = cat,
                    totalMinutes = mins,
                    percentage = if (totalMinutes > 0) mins.toFloat() / totalMinutes else 0f
                )
            }

        val dayCount = dayStats.count { it.minutes > 0 }.coerceAtLeast(1)

        return ReportsUiState(
            selectedFilter = filter,
            dayStats = dayStats,
            categoryStats = categoryStats,
            totalMinutes = totalMinutes,
            avgMinutesPerDay = totalMinutes / dayCount,
            longestSession = sessions.maxOfOrNull { it.durationMinutes } ?: 0,
            sessionCount = sessions.size,
            rawSessions = sessions,
            currentCategories = catMap.mapValues { it.value.name }
        )
    }

    private fun buildDayStats(
        filter: ReportFilter,
        sessions: List<SessionEntity>,
        startMs: Long
    ): List<DayStats> {
        val turkishDays = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cts", "Paz")

        return when (filter) {
            ReportFilter.WEEK -> {
                (0..6).map { offset ->
                    val dayCal = Calendar.getInstance().apply {
                        timeInMillis = startMs
                        add(Calendar.DAY_OF_YEAR, offset)
                    }
                    val dayStart = dayCal.apply {
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val dayEnd = dayStart + 86_400_000L

                    val mins = sessions
                        .filter { it.startTime in dayStart until dayEnd }
                        .sumOf { it.durationMinutes }

                    // Türkçe gün adı: Calendar.DAY_OF_WEEK → 1=Paz,2=Pzt...7=Cts
                    val dowIndex = (dayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    DayStats(dayLabel = turkishDays[dowIndex], minutes = mins)
                }
            }
            ReportFilter.MONTH -> {
                // Her haftayı topla (4 bar)
                (0..3).map { week ->
                    val weekStart = startMs + week * 7 * 86_400_000L
                    val weekEnd = weekStart + 7 * 86_400_000L
                    val mins = sessions
                        .filter { it.startTime in weekStart until weekEnd }
                        .sumOf { it.durationMinutes }
                    DayStats(dayLabel = "${week + 1}. Hafta", minutes = mins)
                }
            }
        }
    }

    private fun generateCsvContent(): String {
        val state = uiState.value
        val csvContent = StringBuilder()
        csvContent.append("Oturum ID,Kategori,Tarih,Saat,Sure (dk)\n")

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        state.rawSessions.forEach { session ->
            val catName = state.currentCategories[session.categoryId] ?: "Bilinmeyen"
            val dateStr = dateFormat.format(Date(session.startTime))
            val timeStr = timeFormat.format(Date(session.startTime))
            csvContent.append("${session.id},\"$catName\",$dateStr,$timeStr,${session.durationMinutes}\n")
        }
        return csvContent.toString()
    }

    fun exportToCsv(context: Context) {
        if (uiState.value.rawSessions.isEmpty()) return

        viewModelScope.launch {
            try {
                val csvContent = generateCsvContent()

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val filename = "Zaman_Raporu_$timestamp.csv"
                val file = File(context.cacheDir, filename)

                FileOutputStream(file).use { fos ->
                    fos.write(0xEF)
                    fos.write(0xBB)
                    fos.write(0xBF)
                    fos.write(csvContent.toByteArray())
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, "Raporu Paylaş")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveCsvToUri(context: Context, uri: Uri) {
        if (uiState.value.rawSessions.isEmpty()) return

        viewModelScope.launch {
            try {
                val csvContent = generateCsvContent()

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(0xEF)
                    outputStream.write(0xBB)
                    outputStream.write(0xBF)
                    outputStream.write(csvContent.toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
