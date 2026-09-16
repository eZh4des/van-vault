package com.example.vanvault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VanVault Inicio") },
                actions = {
                    TextButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogoutClick()
                    }) {
                        Text("Cerrar Sesión", color = Color(0xFFDC2626))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Bienvenido a VanVault!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sesión iniciada como:",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user?.email ?: "Usuario desconocido",
                style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFDC2626))
            )
        }
    }
}
