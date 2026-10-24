package com.vanvault.android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vanvault.android.domain.model.KpiMetric
import com.vanvault.android.domain.model.RecentMovement
import com.vanvault.android.ui.components.SkeletonMetricCard
import com.vanvault.android.ui.components.NavItem
import com.vanvault.android.ui.components.SkeletonMetricCard
import com.vanvault.android.ui.components.OfflineBanner
import com.vanvault.android.ui.components.SkeletonMetricCard
import com.vanvault.android.ui.components.VanVaultBottomNavBar
import com.vanvault.android.ui.util.rememberIsOnline
import com.vanvault.android.ui.viewmodel.DashboardViewModel
import com.vanvault.android.ui.theme.successColor
import com.vanvault.android.ui.theme.warningColor

/**
 * DashboardScreen — pantalla principal (route "home").
 *
 * **Diseño actualizado (post-revisión de diseño)**:
 *  - Sin TopAppBar / título "VanVault": la parte superior solo tiene
 *    status bar + saludo + campana de notificaciones.
 *  - **Sin banner de alerta** de stock bajo (lo eliminamos por petición).
 *  - **4 KPI cards en grid 2×2 (filas)**: Productos, Ventas del mes,
 *    Inversión total, Ganancia neta — ocupan todo el ancho del padding.
 *  - **Sin** sección "Operación" ni "Accesos directos".
 *  - **Bottom Navigation** con 4 destinos: Inicio, Inventario, Ventas, Perfil.
 *  - **Últimas ventas** (movimientos recientes) con link "Ver todas".
 *
 * Cumple auditoría:
 *  - Item 01: Bottom nav M3 (NavigationBar) con 4 destinos
 *  - Item 03: typography M3 (sp)
 *  - Item 04: Color Roles M3 + dark theme
 *  - Item 15: touch targets ≥ 48dp
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenInventory: () -> Unit,
    onOpenSold: () -> Unit,
    onOpenRoi: () -> Unit,
    onOpenAdmin: () -> Unit,
    onNavigate: (NavItem) -> Unit = {},
    onSeeAllSales: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snapshot = state.snapshot

    // T07: estado de conexion
    val isOnline by rememberIsOnline()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            VanVaultBottomNavBar(
                currentRoute = NavItem.HOME,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                OfflineBanner(
                    isOnline = isOnline,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    onRetry = viewModel::onRefresh
                )
                if (state.isLoading && snapshot == null) {
                    // T12: skeletons de KPIs (Item 11)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SkeletonMetricCard(modifier = Modifier.weight(1f))
                            SkeletonMetricCard(modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SkeletonMetricCard(modifier = Modifier.weight(1f))
                            SkeletonMetricCard(modifier = Modifier.weight(1f))
                        }
                    }
                } else if (snapshot != null) {
                    DashboardContent(
                        snapshot = snapshot,
                        onOpenInventory = onOpenInventory,
                        onOpenSold = onOpenSold,
                        onOpenRoi = onOpenRoi,
                        onSeeAllSales = onSeeAllSales
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    snapshot: com.vanvault.android.domain.model.DashboardSnapshot,
    onOpenInventory: () -> Unit,
    onOpenSold: () -> Unit,
    onOpenRoi: () -> Unit,
    onSeeAllSales: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // === Header: saludo + campana ===
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = snapshot.greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = snapshot.userName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onSeeAllSales,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notificaciones",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // === Resumen del mes — 4 KPI cards en grid 2x2 (filas) ===
        Text(
            text = "Resumen del mes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))

        // Convertimos la lista de 4 KPIs en dos filas de 2 cards cada una
        val rows = snapshot.primaryMetrics.chunked(2)
        rows.forEachIndexed { index, rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowMetrics.forEach { metric ->
                    SimpleKpiCard(
                        metric = metric,
                        modifier = Modifier.weight(1f),
                        onClick = when (metric.id) {
                            "products_total" -> onOpenInventory
                            "sales_month", "net_profit" -> onOpenSold
                            "total_investment" -> onOpenRoi
                            else -> null
                        }
                    )
                }
            }
            if (index < rows.lastIndex) {
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        // === Últimas ventas ===
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Últimas ventas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(
                onClick = onSeeAllSales,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = "Ver todas",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        snapshot.recentMovements.forEach { movement ->
            SaleRow(movement)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}

/**
 * Tarjeta KPI minimalista (estilo "Resumen del mes" del diseño).
 *
 * Diferencia con [DashboardKpiCard]: aquí solo se muestra label + value
 * + ícono circular a la derecha — sin tendencia (más simple, alineado al mockup).
 */
@Composable
private fun SimpleKpiCard(
    metric: KpiMetric,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val accent = accentColorFor(metric.accentColor)
    Surface(
        modifier = modifier
            .heightIn(min = 92.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onClick ?: {},
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconFor(metric.icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Text(
                text = metric.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Fila de "última venta": ícono de recibo + nombre + cliente + precio.
 */
@Composable
private fun SaleRow(movement: RecentMovement) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono "recibo de dinero" (representado por request_page o receipt)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movement.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = movement.timestampLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = movement.unitPrice?.toDisplayString() ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun accentColorFor(accent: KpiMetric.AccentColor): Color = when (accent) {
    KpiMetric.AccentColor.PRIMARY -> MaterialTheme.colorScheme.primary
    KpiMetric.AccentColor.SUCCESS -> MaterialTheme.successColor
    KpiMetric.AccentColor.WARNING -> MaterialTheme.warningColor
    KpiMetric.AccentColor.ERROR -> MaterialTheme.colorScheme.error
    KpiMetric.AccentColor.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun iconFor(name: String) = when (name) {
    "trending_up" -> Icons.Filled.TrendingUp
    "inventory" -> Icons.Filled.Inventory2
    "warning" -> Icons.Filled.Warning
    "shopping_cart" -> Icons.Filled.ShoppingCart
    "receipt_long" -> Icons.Filled.ReceiptLong
    "percent" -> Icons.Filled.Percent
    "groups" -> Icons.Filled.Groups
    "settings" -> Icons.Filled.Settings
    "attach_money" -> Icons.Filled.AttachMoney
    "credit_card" -> Icons.Filled.CreditCard
    else -> Icons.Filled.Info
}