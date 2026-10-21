package com.vanvault.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vanvault.android.domain.model.KpiMetric

/**
 * Tarjeta KPI reutilizable para Dashboard, ROI y Admin.
 *
 * Pragmatic tip: "Decorator pattern" — envuelve cualquier métrica y le da
 * el estilo M3 (Card elevated + typography scale + accent color).
 *
 * Cumple auditoría:
 *  - Item 02: bordes redondeados consistentes (16dp)
 *  - Item 09: contenido semántico anunciado por TalkBack
 *  - Item 15: touch target mínimo (Card completa si es clickable)
 */
@Composable
fun DashboardKpiCard(
    metric: KpiMetric,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val accent = accentColorFor(metric.accentColor)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = iconFor(metric.icon),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = metric.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = trendIcon(metric.trend),
                    contentDescription = trendDescription(metric.trend),
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = trendLabel(metric.trend),
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
            }
        }
    }
}

private fun accentColorFor(accent: KpiMetric.AccentColor): Color = when (accent) {
    KpiMetric.AccentColor.PRIMARY -> Color(0xFFDC2626)    // brand rojo
    KpiMetric.AccentColor.SUCCESS -> Color(0xFF059669)    // verde positivo
    KpiMetric.AccentColor.WARNING -> Color(0xFFF97316)    // naranja alerta
    KpiMetric.AccentColor.ERROR -> Color(0xFFDC2626)      // rojo error
    KpiMetric.AccentColor.NEUTRAL -> Color(0xFF71717A)    // gris
}

@Composable
private fun iconFor(name: String): ImageVector = when (name) {
    "trending_up" -> Icons.Filled.TrendingUp
    "inventory" -> Icons.Filled.Inventory2
    "warning" -> Icons.Filled.Warning
    "shopping_cart" -> Icons.Filled.ShoppingCart
    "receipt_long" -> Icons.Filled.ReceiptLong
    "percent" -> Icons.Filled.Percent
    "groups" -> Icons.Filled.Groups
    "settings" -> Icons.Filled.Settings
    "attach_money" -> Icons.Filled.AttachMoney
    else -> Icons.Outlined.Info
}

private fun trendIcon(trend: KpiMetric.Trend): ImageVector = when (trend) {
    KpiMetric.Trend.UP -> Icons.Filled.TrendingUp
    KpiMetric.Trend.DOWN -> Icons.Filled.TrendingDown
    KpiMetric.Trend.FLAT -> Icons.Filled.TrendingFlat
}

private fun trendDescription(trend: KpiMetric.Trend): String = when (trend) {
    KpiMetric.Trend.UP -> "Tendencia al alza"
    KpiMetric.Trend.DOWN -> "Tendencia a la baja"
    KpiMetric.Trend.FLAT -> "Tendencia estable"
}

private fun trendLabel(trend: KpiMetric.Trend): String = when (trend) {
    KpiMetric.Trend.UP -> "vs mes anterior"
    KpiMetric.Trend.DOWN -> "vs mes anterior"
    KpiMetric.Trend.FLAT -> "Sin cambios"
}