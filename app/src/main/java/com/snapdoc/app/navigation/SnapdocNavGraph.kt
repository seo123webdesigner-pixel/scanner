package com.snapdoc.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.snapdoc.app.core.ui.components.BottomNavItem
import com.snapdoc.app.core.ui.components.SnapdocBottomNav
import com.snapdoc.app.feature.about.AboutScreen
import com.snapdoc.app.feature.categories.CategoriesScreen
import com.snapdoc.app.feature.document.DocumentDetailScreen
import com.snapdoc.app.feature.home.HomeScreen
import com.snapdoc.app.feature.onboarding.OnboardingScreen
import com.snapdoc.app.feature.onboarding.SplashScreen
import com.snapdoc.app.feature.paywall.PaywallScreen
import com.snapdoc.app.feature.scanner.ScannerFlowScreen
import com.snapdoc.app.feature.search.SearchScreen
import com.snapdoc.app.feature.settings.ManageCategoriesScreen
import com.snapdoc.app.feature.settings.SettingsScreen

private val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Outlined.Home, SnapdocRoute.Home.path),
    BottomNavItem("Categories", Icons.Outlined.GridView, SnapdocRoute.Categories.path),
    BottomNavItem("Settings", Icons.Outlined.Settings, SnapdocRoute.Settings.path),
)

@Composable
fun SnapdocNavGraph() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route.orEmpty()
    val showNav = currentRoute in bottomNavItems.map { it.route }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = SnapdocRoute.Splash.path,
        ) {
            composable(SnapdocRoute.Splash.path) {
                SplashScreen(onNavigate = { target ->
                    navController.navigate(target) {
                        popUpTo(SnapdocRoute.Splash.path) { inclusive = true }
                    }
                })
            }

            composable(SnapdocRoute.Onboarding.path) {
                OnboardingScreen(onComplete = { target ->
                    navController.navigate(target) {
                        popUpTo(SnapdocRoute.Onboarding.path) { inclusive = true }
                    }
                })
            }

            composable(SnapdocRoute.Home.path) {
                HomeScreen(
                    onScan = { navController.navigate(SnapdocRoute.Scanner.path) },
                    onSearch = { navController.navigate(SnapdocRoute.Search.path) },
                    onOpen = { id -> navController.navigate(SnapdocRoute.Document.build(id)) },
                )
            }

            composable(SnapdocRoute.Categories.path) {
                CategoriesScreen(onOpenCategory = {
                    // Phase 8 enhancement: deep-link into a per-category filter screen.
                })
            }

            composable(SnapdocRoute.Search.path) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpen = { id -> navController.navigate(SnapdocRoute.Document.build(id)) },
                )
            }

            composable(SnapdocRoute.Scanner.path) {
                ScannerFlowScreen(
                    onSaved = { id ->
                        navController.navigate(SnapdocRoute.Document.build(id)) {
                            popUpTo(SnapdocRoute.Home.path)
                        }
                    },
                    onCancel = { navController.popBackStack() },
                )
            }

            composable(
                route = SnapdocRoute.Document.path,
                arguments = listOf(navArgument(SnapdocRoute.Document.ARG_ID) { type = NavType.StringType }),
            ) {
                DocumentDetailScreen(
                    onBack = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack() },
                )
            }

            composable(SnapdocRoute.Settings.path) {
                SettingsScreen(
                    onManageCategories = { navController.navigate(SnapdocRoute.ManageCategories.path) },
                    onPaywall = { navController.navigate(SnapdocRoute.Paywall.path) },
                    onAbout = { navController.navigate(SnapdocRoute.About.path) },
                )
            }

            composable(SnapdocRoute.ManageCategories.path) {
                ManageCategoriesScreen(onBack = { navController.popBackStack() })
            }

            composable(SnapdocRoute.Paywall.path) {
                PaywallScreen(onBack = { navController.popBackStack() })
            }

            composable(SnapdocRoute.About.path) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }

        if (showNav) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PaddingValues()),
                contentAlignment = androidx.compose.ui.Alignment.BottomCenter,
            ) {
                SnapdocBottomNav(
                    items = bottomNavItems,
                    selectedRoute = currentRoute,
                    onSelect = { item ->
                        if (item.route != currentRoute) {
                            navController.navigate(item.route) {
                                popUpTo(SnapdocRoute.Home.path) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )
            }
        }
    }
}
