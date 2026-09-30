package com.example.vanvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vanvault.ui.components.NavItem
import com.example.vanvault.ui.screens.AddProductScreen
import com.example.vanvault.ui.screens.ForgotPasswordScreen
import com.example.vanvault.ui.screens.HomeScreen
import com.example.vanvault.ui.screens.InventoryScreen
import com.example.vanvault.ui.screens.LoginScreen
import com.example.vanvault.ui.screens.RegisterScreen
import com.example.vanvault.ui.screens.ScannerScreen
import com.example.vanvault.ui.theme.VanVaultTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VanVaultTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                // region: validate user credentials
                val startDestination = if (currentUser != null && currentUser.isEmailVerified) {
                    "home"
                } else {
                    "login"
                }
                // endregion

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
                        InventoryScreen(
                            onNavigate = { navItem -> 
                                when(navItem) {
                                    NavItem.PROFILE -> navController.navigate("profile") { launchSingleTop = true }
                                    // navItem.SALES y HOME se añadirán luego
                                    else -> {}
                                }
                            },
                            onAddProductClick = { navController.navigate("add_product") }
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
                }
            }
        }
    }
}
