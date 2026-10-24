package com.vanvault.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vanvault.android.domain.model.UserProfile
import com.vanvault.android.ui.components.AccountInfoItem
import com.vanvault.android.ui.components.AccountSectionHeader
import com.vanvault.android.ui.components.AvatarCircle
import com.vanvault.android.ui.components.ConfirmActionDialog
import com.vanvault.android.ui.components.M3TopBarWithBack
import com.vanvault.android.ui.viewmodel.MyAccountViewModel
import kotlinx.coroutines.launch

/**
 * MyAccountScreen — pantalla "Mi Cuenta" basada en `screen-settings.svg`.
 *
 * Estructura visual (de arriba a abajo):
 *  1. Top bar con back y título "Mi cuenta"
 *  2. Avatar circular 80dp + nombre + email + teléfono
 *  3. Sección INFORMACIÓN PERSONAL (lista de datos del usuario)
 *  4. Sección SEGURIDAD (Cambiar contraseña, Autenticación 2FA)
 *  5. Botón "Cerrar sesión" al final
 *
 * Cumple auditoría:
 *  - Item 03: typography M3 (sp)
 *  - Item 04: Color Roles M3
 *  - Item 09: UDF con StateFlow
 *  - Item 15: touch targets >= 48dp
 *  - Nielsen #3, #5: ConfirmDialog + Snackbar on logout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAccountScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    viewModel: MyAccountViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Muestra mensajes one-shot del VM (Snackbar)
    LaunchedEffect(state.message) {
        val msg = state.message
        if (msg != null) {
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.onMessageShown()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            M3TopBarWithBack(
                title = "Mi cuenta",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.profile == null) {
            // Sesión expirada o sin usuario
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay sesión activa",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            MyAccountContent(
                profile = state.profile!!,
                onChangePassword = onChangePassword,
                onLogoutClick = { showLogoutDialog = true }
            )
        }
    }

    if (showLogoutDialog) {
        ConfirmActionDialog(
            showDialog = true,
            title = "¿Cerrar sesión?",
            message = "Tendrás que volver a iniciar sesión para acceder a tu inventario.",
            itemName = state.profile?.email,
            confirmLabel = "Cerrar sesión",
            isDestructive = false,
            onConfirm = {
                viewModel.onSignOut()
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun MyAccountContent(
    profile: UserProfile,
    onChangePassword: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // === Header con avatar grande ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarCircle(
                initials = profile.initials,
                contentDescription = "Avatar de ${profile.displayName}",
                size = 80.dp,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                fontSizeSp = 32
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = profile.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = profile.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!profile.isEmailVerified) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "Correo no verificado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // === INFORMACIÓN PERSONAL ===
        AccountSectionHeader(title = "Información personal")
        AccountInfoItem(
            label = "Nombre completo",
            value = profile.displayName,
            showChevron = false
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        AccountInfoItem(
            label = "Correo electrónico",
            value = profile.email,
            showChevron = false
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        AccountInfoItem(
            label = "Teléfono",
            value = profile.phone,
            showChevron = false
        )

        // === SEGURIDAD ===
        AccountSectionHeader(title = "Seguridad")
        AccountInfoItem(
            label = "Cambiar contraseña",
            value = null,
            showChevron = true,
            onClick = onChangePassword
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        AccountInfoItem(
            label = "Autenticación de dos factores",
            value = "Desactivado",
            showChevron = true
        )

        Spacer(Modifier.height(32.dp))

        // === Botón Cerrar sesión ===
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .heightIn(min = 48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
            )
        ) {
            Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(48.dp))
    }
}