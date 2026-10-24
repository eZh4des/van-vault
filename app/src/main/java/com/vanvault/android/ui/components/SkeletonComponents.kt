package com.vanvault.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Bloque de esqueleto con efecto shimmer (Material You style).
 *
 * **Por que skeleton en vez de spinner?** (T12 — Item 11 de la auditoría)
 *  - Un spinner comunica "estoy cargando algo"; un skeleton comunica
 *    "esto es lo que se va a ver, con estos datos" → mejor percepcion
 *    de velocidad (Google RAIL: 100ms retroalimentacion, 1s percepcion).
 *  - Evita el salto de layout cuando los datos llegan (el contenido ya
 *    ocupa el espacio final).
 *
 * **Accesibilidad**:
 *  - El shimmer es puramente decorativo: `contentDescription = null`
 *    para que TalkBack no lo anuncie.
 *  - El contenedor announces "Cargando" via `stateDescription` para
 *    que el usuario sepa que debe esperar.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp),
    isLoading: Boolean = true
) {
    if (!isLoading) return

    val transition = rememberInfiniteTransition(label = "skeleton")
    val translateX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val highlightColor = MaterialTheme.colorScheme.surfaceContainerHighest

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateX - 600f, 0f),
        end = Offset(translateX, 0f)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
            .semantics { }
    )
}

/**
 * Esqueleto de una card de producto (fila de inventario).
 *
 * Reproduce la estructura visual de [InventoryItemCard] para que el
 * layout no salte cuando llegan los datos.
 */
@Composable
fun SkeletonProductCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nombre del producto
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(18.dp)
            )
            // SKU
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(14.dp)
            )
            Spacer(Modifier.height(8.dp))
            // Fila inferior: status + precio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(70.dp)
                            .height(12.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier
                            .width(90.dp)
                            .height(18.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier
                            .width(100.dp)
                            .height(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Lista de esqueletos de producto.
 *
 * @param itemCount cuantos items mostrar (default 5)
 */
@Composable
fun SkeletonProductList(
    modifier: Modifier = Modifier,
    itemCount: Int = 5
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            SkeletonProductCard()
        }
    }
}

// ============================================================================
// Skeletons para otros tipos de contenido
// ============================================================================

/** Esqueleto de un item de venta (avatar + datos + precio). */
@Composable
fun SkeletonSoldItem(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SkeletonBox(Modifier.fillMaxWidth(0.6f).height(16.dp))
                SkeletonBox(Modifier.fillMaxWidth(0.35f).height(12.dp))
            }
            Spacer(Modifier.width(8.dp))
            SkeletonBox(Modifier.width(90.dp).height(18.dp))
        }
    }
}

/** Esqueleto de un usuario en el panel de administracion. */
@Composable
fun SkeletonUserCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SkeletonBox(Modifier.fillMaxWidth(0.5f).height(16.dp))
                SkeletonBox(Modifier.fillMaxWidth(0.7f).height(12.dp))
            }
            Spacer(Modifier.width(8.dp))
            SkeletonBox(
                modifier = Modifier.width(70.dp).height(26.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

/** Esqueleto de una tarjeta de metrica (Dashboard / ROI). */
@Composable
fun SkeletonMetricCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBox(Modifier.fillMaxWidth(0.5f).height(14.dp))
                SkeletonBox(
                    modifier = Modifier.size(24.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            SkeletonBox(Modifier.fillMaxWidth(0.6f).height(24.dp))
        }
    }
}

/** Esqueleto del encabezado de perfil (avatar + nombre). */
@Composable
fun SkeletonProfileHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkeletonBox(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(40.dp)
        )
        Spacer(Modifier.height(12.dp))
        SkeletonBox(
            modifier = Modifier
                .width(180.dp)
                .height(24.dp)
        )
        Spacer(Modifier.height(8.dp))
        SkeletonBox(
            modifier = Modifier
                .width(220.dp)
                .height(16.dp)
        )
    }
}
