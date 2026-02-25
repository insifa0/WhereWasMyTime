package com.example.wherewasmytime.ui.screens.manual

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wherewasmytime.data.local.entity.CategoryEntity
import com.example.wherewasmytime.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    onBack: () -> Unit,
    viewModel: ManualEntryViewModel = viewModel()
) {
    val form by viewModel.form.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current

    val dateFormatter = remember { SimpleDateFormat("d MMMM yyyy", Locale("tr")) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Manuel Giriş", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Hata mesajı ---
            form.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // --- 1. Kategori Seçimi ---
            FormSection(title = "Kategori") {
                CategoryPicker(
                    categories = categories,
                    selectedCategory = form.selectedCategory,
                    onCategorySelected = { viewModel.setCategory(it) }
                )
            }

            // --- 2. Tarih Seçimi ---
            FormSection(title = "Tarih") {
                val cal = Calendar.getInstance().apply { timeInMillis = form.selectedDateMs }
                ClickableField(
                    value = dateFormatter.format(Date(form.selectedDateMs)),
                    icon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp)) }
                ) {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val newCal = Calendar.getInstance()
                            newCal.set(year, month, day, 0, 0, 0)
                            viewModel.setDate(newCal.timeInMillis)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            }

            // --- 3. Başlangıç Saati ---
            FormSection(title = "Başlangıç Saati") {
                val hour = form.startHour
                val minute = form.startMinute
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                ClickableField(
                    value = timeFormatter.format(cal.time),
                    icon = { Icon(Icons.Filled.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp)) }
                ) {
                    TimePickerDialog(
                        context,
                        { _, h, m -> viewModel.setStartTime(h, m) },
                        hour, minute, true
                    ).show()
                }
            }

            // --- 4. Süre (dakika) ---
            FormSection(title = "Süre") {
                DurationPicker(
                    minutes = form.durationMinutes,
                    onMinus = { viewModel.setDuration(form.durationMinutes - 5) },
                    onPlus = { viewModel.setDuration(form.durationMinutes + 5) },
                    onValueChange = { viewModel.setDuration(it) }
                )
            }

            // --- 5. Not (Opsiyonel) ---
            FormSection(title = "Not (İsteğe Bağlı)") {
                OutlinedTextField(
                    value = form.note,
                    onValueChange = { viewModel.setNote(it) },
                    placeholder = { Text("Neler yaptın?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Kaydet Butonu ---
            Button(
                onClick = { viewModel.saveEntry(onSuccess = onBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !form.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Kaydет",  // intentional - Kotlin won't have an issue
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ===================== Yardımcı Composable'lar =====================

@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPicker(
    categories: List<CategoryEntity>,
    selectedCategory: CategoryEntity?,
    onCategorySelected: (CategoryEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "Kategori seç...",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Primary)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedTextColor = if (selectedCategory != null)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            categories.forEach { category ->
                val accentColor = try {
                    Color(android.graphics.Color.parseColor(category.color))
                } catch (e: Exception) { Primary }

                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Text(category.name)
                        }
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ClickableField(
    value: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        trailingIcon = icon,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        enabled = false,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = Color.Transparent,
            disabledTrailingIconColor = Primary
        )
    )
}

@Composable
private fun DurationPicker(
    minutes: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onValueChange: (Int) -> Unit
) {
    val hours = minutes / 60
    val mins = minutes % 60

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Eksi butonu
        FilledIconButton(
            onClick = onMinus,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Azalt")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%02d:%02d", hours, mins),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Text(
                text = "saat : dakika",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Artı butonu
        FilledIconButton(
            onClick = onPlus,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Primary,
                contentColor = Color.Black
            )
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Artır")
        }
    }
}
