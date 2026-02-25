package com.example.wherewasmytime.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wherewasmytime.data.local.entity.CategoryEntity
import com.example.wherewasmytime.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // Hedef düzenleme bottom sheet
    if (uiState.editingCategory != null) {
        GoalEditSheet(
            category = uiState.editingCategory!!,
            onDismiss = viewModel::closeEditing,
            onSave = { cat, mins -> viewModel.saveGoal(cat, mins) }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // --- Başlık ---
        item {
            Text(
                text = "Hedefler",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        // --- Özet banner ---
        item {
            val completed = uiState.goals.count { it.goalMinutes > 0 && it.percentage >= 1f }
            val withGoals = uiState.goals.count { it.goalMinutes > 0 }
            SummaryBanner(completedCount = completed, totalGoals = withGoals)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- Kategori hedef kartları ---
        items(uiState.goals) { goal ->
            GoalCard(
                goal = goal,
                onEditClick = { viewModel.startEditing(goal.category) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // --- Bilgi notu ---
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Kalem ikonuna tıklayarak kategori başına günlük hedef belirleyebilirsin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ===================== Alt Bileşenler =====================

@Composable
private fun SummaryBanner(completedCount: Int, totalGoals: Int) {
    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
            }

            Column {
                Text(
                    text = if (totalGoals == 0) "Henüz hedef yok"
                           else "$completedCount / $totalGoals hedef tamamlandı",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (totalGoals == 0) "Kategori hedefleri belirleyerek günlük ilerleni takip et."
                           else if (completedCount == totalGoals) "Mükemmel! Tüm hedefleri ulaştın 🎉"
                           else "Devam et, başarıya çok yakınsın!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: GoalProgress,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(goal.category.color))
    } catch (e: Exception) { Primary }

    val hasGoal = goal.goalMinutes > 0
    val pct = (goal.percentage * 100).toInt().coerceAtMost(100)
    val isCompleted = goal.percentage >= 1f

    val todayStr = formatMinutes(goal.todayMinutes)
    val goalStr = if (hasGoal) formatMinutes(goal.goalMinutes) else "–"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Üst satır
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
                    Column {
                        Text(
                            text = goal.category.name,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (hasGoal) {
                            Text(
                                text = "$todayStr / $goalStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Bugün: $todayStr  •  Hedef yok",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasGoal) {
                        Text(
                            text = if (isCompleted) "✓" else "%$pct",
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Primary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                    }
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Hedef Düzenle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // İlerleme çubuğu
            if (hasGoal) {
                LinearProgressIndicator(
                    progress = { goal.percentage.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(100.dp)),
                    color = if (isCompleted) Primary else accentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalEditSheet(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity, Int) -> Unit
) {
    var inputText by remember {
        mutableStateOf(category.dailyGoalMinutes?.toString() ?: "")
    }
    val minutes = inputText.toIntOrNull() ?: 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${category.name} — Günlük Hedef",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Dakika cinsinden günlük hedef gir. Sıfır girersen hedef kaldırılır.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Dakika (örn: 60)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                suffix = {
                    if (minutes > 0) {
                        val h = minutes / 60
                        val m = minutes % 60
                        Text(
                            if (h > 0) "= ${h}s ${m}dk" else "= ${m}dk",
                            color = Primary, fontSize = 12.sp
                        )
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("İptal") }

                Button(
                    onClick = { onSave(category, minutes) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Kaydet", color = Color.Black, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}s ${m}dk" else "${m}dk"
}
