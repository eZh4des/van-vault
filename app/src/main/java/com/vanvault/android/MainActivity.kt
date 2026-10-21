package com.vanvault.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vanvault.android.domain.repository.SettingsRepository
import com.vanvault.android.ui.components.NavItem
import com.vanvault.android.ui.screens.*
import com.vanvault.android.ui.theme.VanVaultTheme
import com.vanvault.android.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity con `@AndroidEntryPoint` — Hilt provee dependencias aquí.
 *
 * T02 (Hilt): Ya NO usamos [com.vanvault.android.di.ServiceLocator] ni
 * instanciamos Firebase manualmente. Todo se inyecta via `@Inject` /
 * `@HiltViewModel`.
 *
 * Google Architecture (UI layer): Esta Activity es el "host" de la
 * navegación. No conoce Firebase ni repositorios — solo compone pantallas.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Inyectado por Hilt (gracias a SettingsModule.bindSettingsRepository). */
    @javax.inject.Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VanVaultTheme {
                val navController = rememberNavController()
                val auth = com.vanvault.android.di.FirebaseModule.provideFirebaseAuth()
                val currentUser = auth.currentUser

                // SettingsViewModel inyectado via Hilt (no necesita Factory).
                // Usamos viewModel() directamente para activity-scoped VM.
                val settingsViewModel: SettingsViewModel = hiltViewModel()

                val startDestination = if (currentUser != null && currentUser.isEmailVerified) {
                    "home"
                } else {
                    "login"
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    enterTransition = { androidx.compose.animation.fadeIn() },
                    exitTransition = { androidx.compose.animation.fadeOut() },
                    popEnterTransition = { androidx.compose.animation.fadeIn() },
                    popExitTransition = { androidx.compose.animation.fadeOut() }
                ) {
                    composable("login") {
                        LoginScreen(
                            onRegisterClick = { navController.navigate("register") },
                            onForgotPasswordClick = { navController.navigate("forgot_password") },
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onBackClick = { navController.navigateUp() },
                            onLoginClick = {
                                navController.navigate("login") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("forgot_password") {
                        ForgotPasswordScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    composable("home") {
                        DashboardScreen(
                            onOpenInventory = { navController.navigate("inventory") },
                            onOpenSold = { navController.navigate("sold") },
                            onOpenRoi = { navController.navigate("roi") },
                            onOpenAdmin = { navController.navigate("admin") },
                            onSeeAllSales = { navController.navigate("sold") },
                            onNavigate = { navItem ->
                                when (navItem) {
                                    NavItem.HOME -> { /* ya estamos aquí */ }
                                    NavItem.INVENTORY -> navController.navigate("inventory") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    NavItem.SALES -> navController.navigate("sold") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    NavItem.PROFILE -> navController.navigate("profile") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                    composable("inventory") {
                        InventoryScreen(
                            onNavigate = { navItem ->
                                when (navItem) {
                                    NavItem.HOME -> navController.navigate("home") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    NavItem.PROFILE -> navController.navigate("profile") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    else -> {}
                                }
                            },
                            onAddProductClick = { navController.navigate("add_product") },
                            onEditProductClick = { navController.navigate("edit_product") }
                        )
                    }
                    composable("available") {
                        AvailableScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    composable("edit_product") {
                        EditProductScreen(
                            navController = navController,
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    composable("profile") {
                        ProfileScreen(
                            onNavigate = { navItem ->
                                when (navItem) {
                                    NavItem.HOME, NavItem.INVENTORY -> navController.navigate("home") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    else -> {}
                                }
                            },
                            onAccountClick = { navController.navigate("my_account") },
                            onLogoutClick = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onAppearanceClick = { navController.navigate("appearance") },
                            onNotificationsClick = { navController.navigate("notifications") },
                            onLanguageClick = { navController.navigate("language") },
                            onHelpClick = { navController.navigate("help") }
                        )
                    }
                    composable("account_details") {
                        AccountDetailsScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    composable("my_account") {
                        MyAccountScreen(
                            onBackClick = { navController.navigateUp() },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onChangePassword = {
                                navController.navigate("account_details")
                            }
                        )
                    }
                    composable("appearance") {
                        val settings by settingsViewModel.settings.collectAsState()
                        AppearanceScreen(
                            isDarkMode = settings.isDarkMode,
                            onDarkModeChange = { settingsViewModel.setDarkMode(it) },
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    composable("notifications") {
                        val settings by settingsViewModel.settings.collectAsState()
                        NotificationsScreen(
                            isNotificationsEnabled = settings.isNotificationsEnabled,
                            onNotificationsChange = { settingsViewModel.setNotificationsEnabled(it) },
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    composable("language") {
                        val settings by settingsViewModel.settings.collectAsState()
                        LanguageScreen(
                            currentLanguage = settings.language,
                            onLanguageChange = { settingsViewModel.setLanguage(it) },
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    composable("help") {
                        HelpScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    composable("add_product") {
                        AddProductScreen(
                            navController = navController,
                            onBackClick = { navController.navigateUp() },
                            onScannerClick = { navController.navigate("scanner") }
                        )
                    }
                    composable("scanner") {
                        ScannerScreen(
                            onBackClick = { navController.navigateUp() },
                            onBarcodeScanned = { barcode ->
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("scanned_barcode", barcode)
                                navController.navigateUp()
                            }
                        )
                    }
                    composable("sold") {
                        SoldScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    composable("roi") {
                        RoiScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    composable("admin") {
                        AdminScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                }
            }
        }
    }
}