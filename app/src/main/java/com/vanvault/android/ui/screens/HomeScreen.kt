package com.vanvault.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vanvault.android.ui.components.ScrollableColumn
import com.vanvault.android.ui.components.ConfirmActionDialog

/**
 * Pantalla de bienvenida simple.
 *
 * @deprecated La pantalla principal es ahora [DashboardScreen]. Se conserva
 * únicamente como referencia; no esta registrada en el NavGraph de MainActivity.
 */
@Deprecated("Usar DashboardScreen como pantalla principal")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit
) {
    val user = com.vanvault.android.di.FirebaseModule.provideFirebaseAuth().currentUser
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VanVault Inicio") },
                actions = {
                    TextButton(onClick = { showLogoutDialog = true }) {
                        Text("Cerrar Sesión", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        ScrollableColumn(
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
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sesión iniciada como:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user?.email ?: "Usuario desconocido",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    // T08: confirmar antes de cerrar sesión
    ConfirmActionDialog(
        showDialog = showLogoutDialog,
        title = "¿Cerrar sesión?",
        message = "Tendrás que volver a iniciar sesión para acceder a tu inventario.",
        confirmLabel = "Cerrar sesión",
        isDestructive = false,
        onConfirm = {
            com.vanvault.android.di.FirebaseModule.provideFirebaseAuth().signOut()
            onLogoutClick()
        },
        onDismiss = { showLogoutDialog = false }
    )
}
