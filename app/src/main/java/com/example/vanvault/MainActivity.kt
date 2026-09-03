package com.example.vanvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.vanvault.ui.screens.ForgotPasswordScreen
import com.example.vanvault.ui.screens.LoginScreen
import com.example.vanvault.ui.screens.RegisterScreen
import com.example.vanvault.ui.theme.VanVaultTheme
import com.google.firebase.database.database
import com.google.firebase.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VanVaultTheme {
                var currentScreen by remember { mutableStateOf("login") }

                when (currentScreen) {
                    "login" -> LoginScreen(
                        onRegisterClick = { currentScreen = "register" },
                        onForgotPasswordClick = { currentScreen = "forgot_password" }
                    )
                    "register" -> RegisterScreen(
                        onBackClick = { currentScreen = "login" },
                        onLoginClick = { currentScreen = "login" }
                    )
                    "forgot_password" -> ForgotPasswordScreen(
                        onBackClick = { currentScreen = "login" }
                    )
                }
            }
        }
    }
    fun testdatabase() {
        val database = Firebase.database
        val myRef = database.getReference("message")

        myRef.setValue("Hello, World!")
    }
}
