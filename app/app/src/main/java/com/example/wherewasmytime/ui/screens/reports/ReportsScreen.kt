package com.example.wherewasmytime.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wherewasmytime.ui.theme.Primary

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // --- Başlık ---
        item {
            Text(
                text = "Raporlar",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        // --- Filtre Sekmeleri ---
        item {
            FilterTabs(
                selected = uiState.selectedFilter,
                onSelect = viewModel::setFilter,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- Özet İstatistik Kartları ---
        item {
            SummaryRow(uiState = uiState, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- Günlük Bar Grafik ---
        item {
            SectionTitle("Günlük Dağılım", modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            BarChart(
                data = uiState.dayStats,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- Kategori Dağılımı ---
        item {
            SectionTitle("Kategori Dağılımı", modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (uiState.categoryStats.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Bu dönemde kayıt yok",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(uiState.categoryStats) { stat ->
                CategoryStatRow(
                    stat = stat,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ===================== Alt Bileşenler =====================

@Composable
private fun FilterTabs(
    selected: ReportFilter,
    onSelect: (ReportFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        ReportFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Primary else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { onSelect(filter) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (filter == ReportFilter.WEEK) "Bu Hafta" else "Bu Ay",
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(uiState: ReportsUiState, modifier: Modifier = Modifier) {
    fun formatTime(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}s ${m}dk" else "${m}dk"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("Toplam", formatTime(uiState.totalMinutes), Primary, Modifier.weight(1f))
        StatCard("Günlük Ort.", formatTime(uiState.avgMinutesPerDay), Color(0xFF3B82F6), Modifier.weight(1f))
        StatCard("En Uzun", formatTime(uiState.longestSession), Color(0xFFF59E0B), Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = accentColor, textAlign = TextAlign.Center)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun BarChart(data: List<DayStats>, modifier: Modifier = Modifier) {
    val maxMinutes = data.maxOfOrNull { it.minutes } ?: 1
    val primaryColor = Primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { day ->
                BarItem(
                    label = day.dayLabel,
                    minutes = day.minutes,
                    maxMinutes = maxMinutes,
                    barColor = if (day.minutes > 0) primaryColor else surfaceColor,
                    labelColor = textColor
                )
            }
        }
    }
}

@Composable
private fun BarItem(
    label: String,
    minutes: Int,
    maxMinutes: Int,
    barColor: Color,
    labelColor: Color
) {
    val fraction = if (maxMinutes > 0) minutes.toFloat() / maxMinutes else 0f
    val barMaxHeight = 100.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.width(32.dp)
    ) {
        // Süre etiketi (yalnızca > 0 ise)
        if (minutes > 0) {
            Text(
                text = if (minutes < 60) "${minutes}d" else "${minutes / 60}s",
                fontSize = 8.sp,
                color = barColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        // Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barMaxHeight * fraction.coerceAtLeast(0.02f))
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(barColor)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Gün etiketi
        Text(
            text = label,
            fontSize = 9.sp,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun CategoryStatRow(stat: CategoryStats, modifier: Modifier = Modifier) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(stat.category.color))
    } catch (e: Exception) { Primary }

    val hours = stat.totalMinutes / 60
    val mins = stat.totalMinutes % 60
    val timeStr = if (hours > 0) "${hours}s ${mins}dk" else "${mins}dk"
    val pct = (stat.percentage * 100).toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        text = stat.category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = timeStr, style = MaterialTheme.typography.bodyMedium, color = accentColor, fontWeight = FontWeight.Bold)
                    Text(text = "%$pct", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // İlerleme çubuğu
            LinearProgressIndicator(
                progress = { stat.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}
