package com.vanvault.android.domain.model

/**
 * Item vendido: aparece en la pantalla SoldProducts.
 *
 * Pragmatic tip: model inmutable. La UI nunca debe mutarlo,
 * solo emitir nuevas instancias vía StateFlow.
 */
data class SoldItem(
    val id: String,
    val productId: String,
    val productName: String,
    val modelOrBarcode: String,
    val quantitySold: Int,
    val unitPrice: Money,
    val total: Money,
    val buyerLabel: String,
    val soldAtLabel: String    // ya formateado como "Hoy, 14:32"
)

/**
 * Métricas ROI (Return On Investment) para la pantalla RoiScreen.
 *
 * @param monthlyData 12 puntos (un año) con revenue/cost/profit.
 *                     Cada punto está ordenado del más reciente al más antiguo.
 */
data class RoiMetrics(
    val totalRevenue: Money,
    val totalCost: Money,
    val netProfit: Money,
    val roiPercent: Float,
    val bestSellingProduct: Product?,
    val monthlyData: List<MonthlyRoiPoint>,
    val inventoryTurnover: Float    // veces que rota el inventario al año
)

data class MonthlyRoiPoint(
    val monthLabel: String,    // "Ene", "Feb"...
    val revenue: Money,
    val cost: Money,
    val profit: Money
)