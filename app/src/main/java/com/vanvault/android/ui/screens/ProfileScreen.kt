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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanvault.android.ui.components.NavItem
import com.vanvault.android.ui.components.VanVaultBottomNavBar
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigate: (NavItem) -> Unit,
    onAccountClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ajustes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
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
            SettingsOption(icon = Icons.Outlined.Palette, title = "Apariencia")
            SettingsOption(icon = Icons.Outlined.Notifications, title = "Notificaciones")
            SettingsOption(icon = Icons.Outlined.Language, title = "Idioma")
            SettingsOption(icon = Icons.AutoMirrored.Outlined.HelpOutline, title = "Ayuda")
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Logout button
            SettingsOption(
                icon = Icons.AutoMirrored.Outlined.Logout,
                title = "Cerrar sesión",
                isDestructive = true,
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onLogoutClick()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
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
                fontSize = 16.sp,
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
