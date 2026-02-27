package com.example.wherewasmytime.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wherewasmytime.data.local.entity.CategoryEntity
import com.example.wherewasmytime.data.local.entity.SessionEntity
import com.example.wherewasmytime.ui.theme.Primary
import com.example.wherewasmytime.ui.theme.SurfaceCard
import com.example.wherewasmytime.ui.theme.SurfaceVariantDark
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartSession: (CategoryEntity) -> Unit = {},
    onManualEntry: () -> Unit = {},
    onManageCategories: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var sessionToEdit by remember { mutableStateOf<SessionEntity?>(null) }

    if (sessionToEdit != null) {
        SessionEditSheet(
            session = sessionToEdit!!,
            onDismiss = { sessionToEdit = null },
            onSave = { newStartTimeMs, newDurationMins ->
                viewModel.updateSession(
                    sessionToEdit!!.copy(
                        startTime = newStartTimeMs,
                        durationMinutes = newDurationMins
                    )
                )
                sessionToEdit = null
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
        // --- Header ---
        item {
            HomeHeader()
        }

        // --- Toplam Süre Kartı ---
        item {
            TotalTimeCard(
                totalMinutes = uiState.totalTodayMinutes,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Büyük Başlat Butonu ---
        item {
            StartSessionButton(
                onManualEntry = onManualEntry,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        // --- Hızlı Başlangıç: Kategoriler ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hızlı Başlat",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Yönet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onManageCategories() }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- Kategori Grid (2 sütun) ---
        item {
            CategoryGrid(
                categories = uiState.activeCategories,
                onCategoryClick = onStartSession,
                onManageClick = onManageCategories,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        // --- Son Aktiviteler Başlığı ---
        item {
            Text(
                text = "Son Aktiviteler",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- Son Aktiviteler Listesi ---
        if (uiState.recentSessions.isEmpty()) {
            item {
                EmptySessionsHint(modifier = Modifier.padding(horizontal = 16.dp))
            }
        } else {
            items(uiState.recentSessions, key = { it.id }) { session ->
                RecentSessionItem(
                    session = session,
                    categoryName = uiState.categoriesMap[session.categoryId]?.name ?: "Bilinmeyen",
                    onClick = { sessionToEdit = session },
                    onDelete = {
                        viewModel.deleteSession(session)
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Oturum silindi",
                                actionLabel = "GERİ AL",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.insertSession(session)
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}
}

// ===================== Alt Bileşenler =====================

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hoş geldin,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        IconButton(
            onClick = { },
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Bildirimler",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TotalTimeCard(totalMinutes: Int, modifier: Modifier = Modifier) {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val timeText = if (hours > 0) "${hours}s ${minutes}dk" else "${minutes}dk"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (MaterialTheme.colorScheme.background == Color(0xFF102216))
                    Color(0xFF1A3325)
                else
                    Color(0xFF1C2B23)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Arka planda glow efekti
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-20).dp)
                .background(Primary.copy(alpha = 0.15f), CircleShape)
                .blur(40.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Bugün Toplam",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeText,
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (totalMinutes > 0) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Devam et!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StartSessionButton(onManualEntry: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Büyük "Oturum Başlat" butonu
        Button(
            onClick = { },
            modifier = Modifier
                .weight(1f)
                .height(70.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Oturum Başlat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "Kategori seç ve başla",
                        fontSize = 11.sp,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Manuel giriş küçük butonu
        OutlinedButton(
            onClick = onManualEntry,
            modifier = Modifier
                .size(70.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.linearGradient(listOf(Primary.copy(0.4f), Primary.copy(0.4f)))
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Manuel Ekle",
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Manuel",
                    fontSize = 9.sp,
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<CategoryEntity>,
    onCategoryClick: (CategoryEntity) -> Unit,
    onManageClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val items = categories + null
    val rows = items.chunked(2)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { category ->
                    if (category == null) {
                        AddCategoryCard(
                            onClick = onManageClick,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        CategoryCard(
                            category = category,
                            onClick = { onCategoryClick(category) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Tek eleman varsa boşluğu doldur
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: CategoryEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(category.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                category.dailyGoalMinutes?.let { goal ->
                    Text(
                        text = "Hedef: ${goal / 60}s ${goal % 60}dk",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCategoryCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Yeni Ekle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Yeni Ekle",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Kategori",
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.Transparent
            )
        }
    }
}

@Composable
private fun RecentSessionItem(
    session: SessionEntity,
    categoryName: String,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val startTimeStr = timeFormat.format(Date(session.startTime))
    val durationText = if (session.durationMinutes < 60)
        "${session.durationMinutes}dk"
    else
        "${session.durationMinutes / 60}s ${session.durationMinutes % 60}dk"

    var offsetX by remember { mutableStateOf(0f) }
    val maxSwipe = with(LocalDensity.current) { 80.dp.toPx() }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.error)
    ) {
        // Arka plandaki sil butonu
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(80.dp)
                .fillMaxHeight()
                .clickable {
                    offsetX = 0f
                    onDelete()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Sil",
                tint = MaterialTheme.colorScheme.onError
            )
        }

        // Ön plandaki kart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffsetX = offsetX + dragAmount
                            // Sadece sola çekmeye izin ver, ve maxSwipe kadar
                            offsetX = newOffsetX.coerceIn(-maxSwipe, 0f)
                        },
                        onDragEnd = {
                            // Yarıdan fazla çekildiyse açık bırak, yoksa kapat
                            if (offsetX < -maxSwipe / 2) {
                                offsetX = -maxSwipe
                            } else {
                                offsetX = 0f
                            }
                        },
                        onDragCancel = { offsetX = 0f }
                    )
                }
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Bugün, $startTimeStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = durationText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    }
}

@Composable
private fun EmptySessionsHint(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Bugün henüz oturum yok.\nBir kategori seçerek başla! 🚀",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionEditSheet(
    session: SessionEntity,
    onDismiss: () -> Unit,
    onSave: (Long, Int) -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var timeText by remember { mutableStateOf(timeFormat.format(Date(session.startTime))) }
    var durationText by remember { mutableStateOf(session.durationMinutes.toString()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Oturumu Düzenle",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = timeText,
                onValueChange = { timeText = it },
                label = { Text("Başlangıç Saati (SS:dd)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = durationText,
                onValueChange = { durationText = it },
                label = { Text("Süre (Dakika)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (!session.photoPath.isNullOrEmpty()) {
                val photoFile = File(session.photoPath)
                if (photoFile.exists()) {
                    AsyncImage(
                        model = photoFile,
                        contentDescription = "Oturum Fotoğrafı",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }

            Button(
                onClick = {
                    try {
                        val parsedTime = timeFormat.parse(timeText)
                        val duration = durationText.toIntOrNull() ?: session.durationMinutes
                        if (parsedTime != null) {
                            val cal = Calendar.getInstance()
                            val originalCal = Calendar.getInstance().apply { timeInMillis = session.startTime }
                            cal.time = parsedTime
                            // Sadece saat ve dakikayı güncelle, tarih aynı kalsın
                            originalCal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
                            originalCal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
                            
                            onSave(originalCal.timeInMillis, duration)
                        } else {
                            onSave(session.startTime, duration)
                        }
                    } catch (e: Exception) {
                        onSave(session.startTime, durationText.toIntOrNull() ?: session.durationMinutes)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Kaydet", color = Color.Black)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
