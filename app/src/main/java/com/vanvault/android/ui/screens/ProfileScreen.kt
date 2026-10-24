package com.vanvault.android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vanvault.android.ui.components.ConfirmActionDialog
import com.vanvault.android.ui.components.NavItem
import com.vanvault.android.ui.components.VanVaultBottomNavBar
import com.vanvault.android.ui.util.A11y.merged
import com.vanvault.android.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigate: (NavItem) -> Unit,
    onAccountClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onHelpClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Cuando el VM reporta logout completado, navegamos a login.
    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) {
            onLogoutClick()
            viewModel.onLogoutHandled()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ajustes",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            VanVaultBottomNavBar(
                currentRoute = NavItem.PROFILE,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsOption(icon = Icons.Outlined.Person, title = "Tu cuenta", onClick = onAccountClick)
            SettingsOption(icon = Icons.Outlined.Palette, title = "Apariencia", onClick = onAppearanceClick)
            SettingsOption(icon = Icons.Outlined.Notifications, title = "Notificaciones", onClick = onNotificationsClick)
            SettingsOption(icon = Icons.Outlined.Language, title = "Idioma", onClick = onLanguageClick)
            SettingsOption(icon = Icons.AutoMirrored.Outlined.HelpOutline, title = "Ayuda", onClick = onHelpClick)
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Logout button
            SettingsOption(
                icon = Icons.AutoMirrored.Outlined.Logout,
                title = "Cerrar sesión",
                isDestructive = true,
                onClick = { showLogoutDialog = true }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        ConfirmActionDialog(
            showDialog = true,
            title = "¿Cerrar sesión?",
            message = "Tendrás que volver a iniciar sesión para acceder a tu inventario.",
            itemName = null,
            confirmLabel = "Cerrar sesión",
            isDestructive = false,
            onConfirm = {
                viewModel.onLogout()
                showLogoutDialog = false
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun SettingsOption(
    icon: ImageVector,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
    val iconColor = if (isDestructive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            // T09: agrupar la fila en un solo nodo accesible
            .merged(
                description = title,
                state = if (isDestructive) "Accion destructiva" else null
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            if (!isDestructive) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Ir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
