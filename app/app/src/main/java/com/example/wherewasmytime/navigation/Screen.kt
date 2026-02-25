package com.example.wherewasmytime.navigation

/**
 * Uygulamadaki tüm navigasyon rotaları burada tanımlanır.
 * Sealed class kullanımı tip güvenliği sağlar.
 */
sealed class Screen(val route: String) {
    // Ana Tab Ekranları
    object Home : Screen("home")
    object Goals : Screen("goals")
    object Reports : Screen("reports")
    object Settings : Screen("settings")

    // Alt Ekranlar (Tab dışı, üstte açılır)
    object ActiveTimer : Screen("active_timer/{categoryId}/{categoryName}") {
        fun createRoute(categoryId: String, categoryName: String): String {
            val encodedName = java.net.URLEncoder.encode(categoryName, "UTF-8")
            return "active_timer/$categoryId/$encodedName"
        }
    }
    object ManualEntry : Screen("manual_entry")
    object CategoryList : Screen("category_list")
}
