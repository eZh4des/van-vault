package com.example.vanvault.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class NavItem {
    HOME, INVENTORY, SALES, PROFILE
}

@Composable
fun VanVaultBottomNavBar(
    currentRoute: NavItem,
    onNavigate: (NavItem) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Color(0xFFE4E4E7),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            },
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        val selectedColor = Color(0xFFDC2626)
        val unselectedColor = Color(0xFF71717A)
        val indicatorColor = Color(0xFFFEE2E2)

        val colors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            indicatorColor = indicatorColor,
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor
        )

        NavigationBarItem(
            selected = currentRoute == NavItem.HOME,
            onClick = { onNavigate(NavItem.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == NavItem.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Inicio"
                )
            },
            label = { Text("Inicio", fontWeight = if (currentRoute == NavItem.HOME) FontWeight.SemiBold else FontWeight.Normal) },
            colors = colors
        )

        NavigationBarItem(
            selected = currentRoute == NavItem.INVENTORY,
            onClick = { onNavigate(NavItem.INVENTORY) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == NavItem.INVENTORY) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                    contentDescription = "Inventario"
                )
            },
            label = { Text("Inventario", fontWeight = if (currentRoute == NavItem.INVENTORY) FontWeight.SemiBold else FontWeight.Normal) },
            colors = colors
        )

        NavigationBarItem(
            selected = currentRoute == NavItem.SALES,
            onClick = { onNavigate(NavItem.SALES) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == NavItem.SALES) Icons.Filled.Receipt else Icons.Outlined.Receipt,
                    contentDescription = "Ventas"
                )
            },
            label = { Text("Ventas", fontWeight = if (currentRoute == NavItem.SALES) FontWeight.SemiBold else FontWeight.Normal) },
            colors = colors
        )

        NavigationBarItem(
            selected = currentRoute == NavItem.PROFILE,
            onClick = { onNavigate(NavItem.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == NavItem.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Perfil"
                )
            },
            label = { Text("Perfil", fontWeight = if (currentRoute == NavItem.PROFILE) FontWeight.SemiBold else FontWeight.Normal) },
            colors = colors
        )
    }
}
