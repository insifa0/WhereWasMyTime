package com.example.wherewasmytime.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wherewasmytime.data.preferences.AppTheme
import com.example.wherewasmytime.ui.theme.Primary

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val context = LocalContext.current
    var showWipeDialog by remember { mutableStateOf(false) }
    var wipeDone by remember { mutableStateOf(false) }

    // Tüm verileri silme onay dialogu
    if (showWipeDialog) {
        WipeDataDialog(
            onConfirm = {
                viewModel.wipeAllData { wipeDone = true }
                showWipeDialog = false
            },
            onDismiss = { showWipeDialog = false }
        )
    }

    // Silme başarı mesajı
    if (wipeDone) {
        LaunchedEffect(Unit) {
            wipeDone = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                text = "Ayarlar",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        // --- Tema Seçimi ---
        item {
            SettingsSection(title = "Görünüm") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppTheme.entries.forEach { theme ->
                        ThemeOptionRow(
                            label = when (theme) {
                                AppTheme.DARK -> "Karanlık Tema"
                                AppTheme.LIGHT -> "Aydınlık Tema"
                                AppTheme.SYSTEM -> "Sistem Varsayılanı"
                            },
                            icon = when (theme) {
                                AppTheme.DARK -> Icons.Outlined.DarkMode
                                AppTheme.LIGHT -> Icons.Outlined.LightMode
                                AppTheme.SYSTEM -> Icons.Outlined.BrightnessMedium
                            },
                            isSelected = currentTheme == theme,
                            onClick = { viewModel.setTheme(theme) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Bildirim Ayarları ---
        item {
            SettingsSection(title = "Bildirimler") {
                SettingsRow(
                    icon = Icons.Outlined.Notifications,
                    label = "Bildirim İzinlerini Yönet",
                    subtitle = "Android Ayarları'na gider",
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Veri Yönetimi ---
        item {
            SettingsSection(title = "Veri Yönetimi") {
                SettingsRow(
                    icon = Icons.Outlined.DeleteForever,
                    label = "Tüm Geçmişi Sil",
                    subtitle = "Tüm oturumlar kalıcı olarak silinir",
                    iconTint = MaterialTheme.colorScheme.error,
                    labelColor = MaterialTheme.colorScheme.error,
                    onClick = { showWipeDialog = true }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Hakkında ---
        item {
            SettingsSection(title = "Hakkında") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    InfoRow(label = "Uygulama Adı", value = "WhereWasMyTime")
                    InfoRow(label = "Sürüm", value = "1.0.0-alpha")
                    InfoRow(label = "Yapım Yılı", value = "2026")
                }
            }
        }
    }
}

// ===================== Alt Bileşenler =====================

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(4.dp), content = content)
        }
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Primary.copy(alpha = 0.12f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                icon, contentDescription = null,
                tint = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = labelColor, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WipeDataDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
        },
        title = {
            Text("Tüm Geçmişi Sil", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Tüm oturum kayıtları kalıcı olarak silinecek. Kategoriler ve hedefler korunur. Bu işlem geri alınamaz.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Evet, Sil", color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("İptal") }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}
