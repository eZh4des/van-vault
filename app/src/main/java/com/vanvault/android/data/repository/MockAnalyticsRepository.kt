package com.vanvault.android.data.repository

import com.vanvault.android.domain.model.AdminSnapshot
import com.vanvault.android.domain.model.AdminUser
import com.vanvault.android.domain.model.DashboardSnapshot
import com.vanvault.android.domain.model.KpiMetric
import com.vanvault.android.domain.model.Money
import com.vanvault.android.domain.model.MonthlyRoiPoint
import com.vanvault.android.domain.model.PendingTask
import com.vanvault.android.domain.model.RecentMovement
import com.vanvault.android.domain.model.RoiMetrics
import com.vanvault.android.domain.model.SoldItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MockAnalyticsRepository — fuente de datos sintéticos para Dashboard, ROI,
 * Admin y SoldProducts.
 *
 * **Por qué existe**: hasta que conectemos Firebase Realtime Database con
 * los nodos `sales/`, `metrics/`, `users/`, esta clase permite entregar la
 * auditoría con datos realistas. El contrato es el mismo que tendrá el repo
 * real, así que la UI NO cambiará cuando lo reemplacemos.
 *
 * Pragmatic tip: "Tracer Bullets" — código production-ready pero delgado.
 */
@Singleton
class MockAnalyticsRepository @Inject constructor() {

    // -------- DASHBOARD --------

    suspend fun getDashboardSnapshot(): DashboardSnapshot {
        delay(250) // simula latencia Firebase
        return DashboardSnapshot(
            greeting = "Hola, bienvenido",
            userName = "Administrador",
            primaryMetrics = listOf(
                KpiMetric(
                    id = "products_total",
                    label = "Productos",
                    value = "1.284 und",
                    trend = KpiMetric.Trend.FLAT,
                    icon = "inventory",
                    accentColor = KpiMetric.AccentColor.PRIMARY
                ),
                KpiMetric(
                    id = "sales_month",
                    label = "Ventas del mes",
                    value = "$ 18.450",
                    trend = KpiMetric.Trend.UP,
                    icon = "receipt_long",
                    accentColor = KpiMetric.AccentColor.PRIMARY
                ),
                KpiMetric(
                    id = "total_investment",
                    label = "Inversión total",
                    value = "$ 42.100",
                    trend = KpiMetric.Trend.UP,
                    icon = "credit_card",
                    accentColor = KpiMetric.AccentColor.PRIMARY
                ),
                KpiMetric(
                    id = "net_profit",
                    label = "Ganancia neta",
                    value = "$ 12.450",
                    trend = KpiMetric.Trend.UP,
                    icon = "trending_up",
                    accentColor = KpiMetric.AccentColor.PRIMARY
                )
            ),
            secondaryMetrics = emptyList(),
            recentMovements = listOf(
                RecentMovement(
                    id = "m1",
                    productName = "Perno de acero 1/2",
                    type = RecentMovement.MovementType.SALE,
                    quantity = 1,
                    timestampLabel = "Ferreteria Central",
                    unitPrice = Money.ofCop(120_000)
                ),
                RecentMovement(
                    id = "m2",
                    productName = "Martillo Galponero",
                    type = RecentMovement.MovementType.SALE,
                    quantity = 1,
                    timestampLabel = "Eduardo Pérez",
                    unitPrice = Money.ofCop(450_000)
                )
            )
        )
    }

    // -------- SOLD PRODUCTS --------

    fun observeSoldItems(): Flow<List<SoldItem>> = flow {
        emit(getSoldItems())
    }

    suspend fun getSoldItems(): List<SoldItem> {
        delay(200)
        return listOf(
            SoldItem(
                id = "s1",
                productId = "p1",
                productName = "DVR 4CH TURBO 1080p",
                modelOrBarcode = "DS-7104HGHI-M1",
                quantitySold = 2,
                unitPrice = Money.ofCop(165_000),
                total = Money.ofCop(330_000),
                buyerLabel = "Cliente #1042",
                soldAtLabel = "Hoy, 14:32"
            ),
            SoldItem(
                id = "s2",
                productId = "p2",
                productName = "Cámara IP 4MP Bullet",
                modelOrBarcode = "DS-2CD2043G2-I",
                quantitySold = 4,
                unitPrice = Money.ofCop(245_000),
                total = Money.ofCop(980_000),
                buyerLabel = "Cliente #1039",
                soldAtLabel = "Hoy, 11:08"
            ),
            SoldItem(
                id = "s3",
                productId = "p3",
                productName = "Disco Duro 1TB Surveillance",
                modelOrBarcode = "WD-Purple-1TB",
                quantitySold = 1,
                unitPrice = Money.ofCop(389_000),
                total = Money.ofCop(389_000),
                buyerLabel = "Cliente #1035",
                soldAtLabel = "Ayer, 17:45"
            ),
            SoldItem(
                id = "s4",
                productId = "p4",
                productName = "Fuente de poder 12V 10A",
                modelOrBarcode = "PSU-12V-10A",
                quantitySold = 6,
                unitPrice = Money.ofCop(45_000),
                total = Money.ofCop(270_000),
                buyerLabel = "Cliente #1031",
                soldAtLabel = "Ayer, 09:12"
            ),
            SoldItem(
                id = "s5",
                productId = "p5",
                productName = "Cable UTP Cat6 305m",
                modelOrBarcode = "CAT6-305M",
                quantitySold = 3,
                unitPrice = Money.ofCop(245_000),
                total = Money.ofCop(735_000),
                buyerLabel = "Cliente #1028",
                soldAtLabel = "22/10/2025"
            )
        )
    }

    // -------- ROI --------

    suspend fun getRoiMetrics(): RoiMetrics {
        delay(300)
        val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun",
                           "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        val baseRevenue = listOf(2.8, 3.1, 3.4, 3.0, 3.6, 3.9,
                                  4.2, 4.0, 4.4, 4.5, 4.7, 4.825)
        val baseCost = listOf(2.0, 2.2, 2.4, 2.1, 2.5, 2.7,
                              2.9, 2.8, 3.0, 3.1, 3.2, 3.3)

        val points = months.zip(baseRevenue).zip(baseCost).map { (rev_cost, label) ->
            val (label_, rev) = rev_cost
            val cost = baseRevenue.indexOf(rev).let { baseCost[it] }
            MonthlyRoiPoint(
                monthLabel = label_,
                revenue = Money.fromDouble(rev * 1_000_000),
                cost = Money.fromDouble(cost * 1_000_000),
                profit = Money.fromDouble((rev - cost) * 1_000_000)
            )
        }

        return RoiMetrics(
            totalRevenue = Money.fromDouble(48.345 * 1_000_000),
            totalCost = Money.fromDouble(33.200 * 1_000_000),
            netProfit = Money.fromDouble(15.145 * 1_000_000),
            roiPercent = 31.3f,
            bestSellingProduct = null,
            monthlyData = points,
            inventoryTurnover = 4.8f
        )
    }

    // -------- ADMIN --------

    suspend fun getAdminSnapshot(): AdminSnapshot {
        delay(200)
        return AdminSnapshot(
            totalUsers = 8,
            activeUsers = 5,
            users = listOf(
                AdminUser(
                    id = "u1",
                    displayName = "Carlos Ramírez",
                    email = "carlos@vanvault.co",
                    role = com.vanvault.android.domain.model.UserRole.ADMIN,
                    initials = "CR",
                    isActive = true,
                    lastSeenLabel = "En línea"
                ),
                AdminUser(
                    id = "u2",
                    displayName = "Diana Pérez",
                    email = "diana@vanvault.co",
                    role = com.vanvault.android.domain.model.UserRole.ADMIN,
                    initials = "DP",
                    isActive = true,
                    lastSeenLabel = "Hace 5 min"
                ),
                AdminUser(
                    id = "u3",
                    displayName = "Andrés López",
                    email = "andres@vanvault.co",
                    role = com.vanvault.android.domain.model.UserRole.CLIENT,
                    initials = "AL",
                    isActive = true,
                    lastSeenLabel = "Hace 1 hora"
                ),
                AdminUser(
                    id = "u4",
                    displayName = "María González",
                    email = "maria@vanvault.co",
                    role = com.vanvault.android.domain.model.UserRole.CLIENT,
                    initials = "MG",
                    isActive = false,
                    lastSeenLabel = "Ayer"
                ),
                AdminUser(
                    id = "u5",
                    displayName = "Pedro Sánchez",
                    email = "pedro@vanvault.co",
                    role = com.vanvault.android.domain.model.UserRole.CLIENT,
                    initials = "PS",
                    isActive = true,
                    lastSeenLabel = "Hace 30 min"
                )
            ),
            pendingTasks = listOf(
                PendingTask(
                    id = "t1",
                    title = "Revisar inventario semanal",
                    description = "8 productos con stock bajo requieren reposición",
                    priority = PendingTask.Priority.HIGH
                ),
                PendingTask(
                    id = "t2",
                    title = "Actualizar precios de la categoría DVR",
                    description = "5 productos sin actualizar desde hace 30 días",
                    priority = PendingTask.Priority.MEDIUM
                ),
                PendingTask(
                    id = "t3",
                    title = "Respaldar base de datos",
                    description = "Programar backup automático cada lunes",
                    priority = PendingTask.Priority.LOW
                )
            )
        )
    }
}