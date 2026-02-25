package com.example.wherewasmytime.ui.screens.timer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wherewasmytime.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTimerScreen(
    categoryId: Long,
    categoryName: String,
    onBack: () -> Unit,
    viewModel: ActiveTimerViewModel = viewModel()
) {
    val state by viewModel.timerState.collectAsState()

    // Kategori başladıysa ve aynı oturum değilse başlat
    LaunchedEffect(categoryId) {
        if (!state.isRunning && !state.isPaused) {
            viewModel.startTimer(categoryId, categoryName)
        }
    }

    val hours = state.elapsedSeconds / 3600
    val minutes = (state.elapsedSeconds % 3600) / 60
    val seconds = state.elapsedSeconds % 60

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.categoryName.ifEmpty { categoryName },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Seçenekler")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- Arka plan glow efekti ---
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .blur(80.dp)
                        .background(Primary.copy(alpha = 0.2f), CircleShape)
                )

                // --- Büyük sayaç ---
                Text(
                    text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-2).sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- Saat / Dakika / Saniye kutuları ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeUnit(label = "Saat", value = String.format("%02d", hours), modifier = Modifier.weight(1f))
                TimeUnit(label = "Dakika", value = String.format("%02d", minutes), modifier = Modifier.weight(1f))
                TimeUnit(
                    label = "Saniye",
                    value = String.format("%02d", seconds),
                    modifier = Modifier.weight(1f),
                    isActive = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (state.isPaused) "DURAKLATILDI" else "AKTİF OTURUM",
                style = MaterialTheme.typography.labelSmall,
                color = if (state.isPaused) MaterialTheme.colorScheme.onSurfaceVariant else Primary,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Fotoğraf Ekle Butonu ---
            OutlinedButton(
                onClick = { },
                shape = CircleShape,
                modifier = Modifier.height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.AddAPhoto,
                    contentDescription = "Fotoğraf Ekle",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fotoğraf Ekle", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- Kontroller: Durdur / Duraklat-Devam ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Durdur Butonu
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledIconButton(
                        onClick = {
                            viewModel.stopTimer()
                            onBack()
                        },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Red.copy(alpha = 0.15f),
                            contentColor = Color.Red
                        ),
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.Red, RoundedCornerShape(4.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Durdur", style = MaterialTheme.typography.labelSmall, color = Color.Red, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.width(40.dp))

                // Duraklat / Devam Et Butonu (büyük)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledIconButton(
                        onClick = {
                            if (state.isRunning) viewModel.pauseTimer()
                            else viewModel.resumeTimer()
                        },
                        modifier = Modifier.size(96.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Primary,
                            contentColor = Color.Black
                        ),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (state.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (state.isRunning) "Duraklat" else "Devam Et",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        if (state.isRunning) "Duraklat" else "Devam Et",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeUnit(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val bgColor = if (isActive) Primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isActive) Primary else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
    }
}
