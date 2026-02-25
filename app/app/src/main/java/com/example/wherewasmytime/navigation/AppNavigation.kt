package com.example.wherewasmytime.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wherewasmytime.ui.screens.categories.ManageCategoriesScreen
import com.example.wherewasmytime.ui.screens.goals.GoalsScreen
import com.example.wherewasmytime.ui.screens.home.HomeScreen
import com.example.wherewasmytime.ui.screens.manual.ManualEntryScreen
import com.example.wherewasmytime.ui.screens.reports.ReportsScreen
import com.example.wherewasmytime.ui.screens.settings.SettingsScreen
import com.example.wherewasmytime.ui.screens.timer.ActiveTimerScreen

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Ana Sayfa", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Goals.route, "Hedefler", Icons.Filled.TrackChanges, Icons.Outlined.TrackChanges),
    BottomNavItem(Screen.Reports.route, "Raporlar", Icons.Filled.PieChart, Icons.Outlined.PieChart),
    BottomNavItem(Screen.Settings.route, "Ayarlar", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(text = item.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartSession = { category ->
                        navController.navigate(
                            Screen.ActiveTimer.createRoute(category.id.toString(), category.name)
                        )
                    },
                    onManualEntry = {
                        navController.navigate(Screen.ManualEntry.route)
                    },
                    onManageCategories = {
                        navController.navigate(Screen.CategoryList.route)
                    }
                )
            }
            composable(Screen.Goals.route) { GoalsScreen() }
            composable(Screen.Reports.route) { ReportsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }

            // Aktif Zamanlayıcı Ekranı
            composable(
                route = Screen.ActiveTimer.route,
                arguments = listOf(
                    androidx.navigation.navArgument("categoryId") {
                        type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("categoryName") {
                        type = androidx.navigation.NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val categoryIdStr = backStackEntry.arguments?.getString("categoryId") ?: "0"
                val categoryId = categoryIdStr.toLongOrNull() ?: 0L
                val encodedName = backStackEntry.arguments?.getString("categoryName") ?: ""
                val categoryName = java.net.URLDecoder.decode(encodedName, "UTF-8")
                ActiveTimerScreen(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    onBack = { navController.popBackStack() }
                )
            }

            // Manuel Zaman Girişi Ekranı
            composable(Screen.ManualEntry.route) {
                ManualEntryScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Kategori Yönetimi Ekranı
            composable(Screen.CategoryList.route) {
                ManageCategoriesScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}


