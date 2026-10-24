package com.vanvault.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vanvault.android.domain.model.Product
import com.vanvault.android.domain.model.StockStatus
import com.vanvault.android.ui.components.ConfirmDeleteDialog
import com.vanvault.android.ui.components.NavItem
import com.vanvault.android.ui.components.VanVaultBottomNavBar
import com.vanvault.android.ui.viewmodel.InventoryEffect
import com.vanvault.android.ui.viewmodel.InventoryViewModel
import com.vanvault.android.ui.viewmodel.StockFilter
import kotlinx.coroutines.launch

/**
 * Pantalla de Inventario refactorizada.
 *
 * **Cambios T02**: NO se importa el ServiceLocator ni se construye el
 * ViewModel manualmente. `hiltViewModel()` resuelve todas las dependencias.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onNavigate: (NavItem) -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onEditProductClick: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var fabExpanded by remember { mutableStateOf(false) }

    // === Estado local para dialog y snackbar (no debe persistir en rotación) ===
    val snackbarHostState = remember { SnackbarHostState() }
    var productPendingDelete by remember { mutableStateOf<Product?>(null) }
    val scope = rememberCoroutineScope()

    // Reaccion a eventos one-shot del VM (Snackbar Undo tras eliminar)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is InventoryEffect.Deleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Producto eliminado",
                        actionLabel = "Deshacer",
                        withDismissAction = true,
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        viewModel.onRestoreProduct(effect.product)
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (state.isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Buscar producto...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.onSearchActiveChange(false) },
                            modifier = Modifier.size(48.dp) // WCAG: ≥48dp touch target
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar búsqueda")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            "Inventario",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.onSearchActiveChange(true) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar productos")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            VanVaultBottomNavBar(
                currentRoute = NavItem.INVENTORY,
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { 50 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        FabOption(text = "Agregar producto", onClick = {
                            fabExpanded = false
                            onAddProductClick()
                        })
                        FabOption(text = "Editar producto", onClick = {
                            fabExpanded = false
                            onEditProductClick()
                        })
                        FabOption(text = "Eliminar producto", onClick = { fabExpanded = false })
                    }
                }

                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Mostrar más opciones",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StockFilterChip(
                    filter = StockFilter.AVAILABLE,
                    currentFilter = state.filter,
                    count = state.countFor(StockStatus.AVAILABLE),
                    onClick = { viewModel.onFilterChange(StockFilter.AVAILABLE) }
                )
                StockFilterChip(
                    filter = StockFilter.LOW_STOCK,
                    currentFilter = state.filter,
                    count = state.countFor(StockStatus.LOW_STOCK),
                    onClick = { viewModel.onFilterChange(StockFilter.LOW_STOCK) }
                )
                StockFilterChip(
                    filter = StockFilter.OUT_OF_STOCK,
                    currentFilter = state.filter,
                    count = state.countFor(StockStatus.OUT_OF_STOCK),
                    onClick = { viewModel.onFilterChange(StockFilter.OUT_OF_STOCK) }
                )
                StockFilterChip(
                    filter = StockFilter.ALL,
                    currentFilter = state.filter,
                    count = state.allProducts.size,
                    onClick = { viewModel.onFilterChange(StockFilter.ALL) }
                )
            }

            if (state.isLoading || state.isSearching) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.visibleProducts.isEmpty()) {
                // Diferentes empty states segun el contexto:
                val isSearchEmpty = state.isSearchActive && state.searchQuery.isNotBlank()
                when {
                    isSearchEmpty -> InlineEmptyState(
                        iconVector = androidx.compose.material.icons.Icons.Filled.Search,
                        title = "Sin resultados",
                        description = "No encontramos productos que coincidan con \"${state.searchQuery}\".",
                        actionLabel = "Limpiar busqueda",
                        onAction = { viewModel.onSearchQueryChange("") }
                    )
                    state.loadTimedOut -> InlineEmptyState(
                        iconVector = androidx.compose.material.icons.Icons.Filled.Close,
                        title = "No se pudo cargar el inventario",
                        description = "Verifica tu conexion a Internet e intenta de nuevo.",
                        actionLabel = "Reintentar",
                        onAction = viewModel::onRefresh
                    )
                    state.isEmpty -> InlineEmptyState(
                        iconVector = androidx.compose.material.icons.Icons.Filled.Add,
                        title = "Inventario vacio",
                        description = "Por favor agregue productos al inventario para comenzar.",
                        actionLabel = "Agregar producto",
                        onAction = onAddProductClick
                    )
                    else -> Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay productos para mostrar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.onRefresh() },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.visibleProducts, key = { it.id }) { product ->
                            val (statusLabel, quantityColor) = statusFor(product)
                            val formattedPrice = product.unitPrice.toDisplayString()

                            InventoryItemCard(
                                title = product.name,
                                subtitle = "SKU: ${product.modelOrBarcode}",
                                statusLabel = statusLabel,
                                quantity = "${product.quantity} unidades",
                                quantityColor = quantityColor,
                                priceLabel = "PRECIO UNITARIO",
                                price = formattedPrice,
                                onClick = { /* TODO: detail */ },
                                onLongClick = { productPendingDelete = product }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }

        // Dialog de confirmación antes de eliminar (Nielsen #3)
        val toDelete = productPendingDelete
        if (toDelete != null) {
            ConfirmDeleteDialog(
                showDialog = true,
                title = "¿Eliminar producto?",
                message = "Esta acción no se puede deshacer fácilmente. " +
                          "El producto volverá a aparecer si usas 'Deshacer'.",
                itemName = toDelete.name,
                confirmLabel = "Eliminar",
                onConfirm = {
                    viewModel.onDeleteWithUndo(toDelete)
                    productPendingDelete = null
                },
                onDismiss = { productPendingDelete = null }
            )
        }
    }
}

@Composable
private fun statusFor(product: Product): Pair<String, Color> {
    val status = product.stockStatus
    val (label, color) = when (status) {
        StockStatus.AVAILABLE -> "DISPONIBLE" to MaterialTheme.colorScheme.primary
        StockStatus.LOW_STOCK -> "INSUFICIENTE" to MaterialTheme.colorScheme.tertiary
        StockStatus.OUT_OF_STOCK -> "AGOTADO" to MaterialTheme.colorScheme.error
    }
    return label to color
}

@Composable
fun FabOption(text: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp,
        modifier = Modifier
            .heightIn(min = 48.dp) // WCAG: ≥48dp touch target
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

/**
 * Card de producto — soporta tap y long-press.
 *
 *  - tap normal: edita el producto
 *  - long-press: muestra menú contextual con accion Eliminar
 *
 * Cumple:
 *  - WCAG 2.5.8: touch target >= 48dp (la card es grande)
 *  - Android Compose: combinedClickable para distinguir tap vs long-press
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun InventoryItemCard(
    title: String,
    subtitle: String,
    statusLabel: String,
    quantity: String,
    quantityColor: Color,
    priceLabel: String,
    price: String,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = quantity,
                        color = quantityColor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = priceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = price,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Chip de filtro de stock — interactivo, con contador y resaltado del seleccionado.
 *
 * Cumple WCAG 2.5.8 (touch target ≥ 48dp) y Nielsen #1 (visibilidad del estado).
 */
@Composable
private fun StockFilterChip(
    filter: StockFilter,
    currentFilter: StockFilter,
    count: Int,
    onClick: () -> Unit
) {
    val isSelected = filter == currentFilter
    val backgroundColor = when (filter) {
        StockFilter.AVAILABLE -> if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                  else MaterialTheme.colorScheme.surfaceContainerLow
        StockFilter.LOW_STOCK -> if (isSelected) MaterialTheme.colorScheme.tertiaryContainer
                                  else MaterialTheme.colorScheme.surfaceContainerLow
        StockFilter.OUT_OF_STOCK -> if (isSelected) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow
        StockFilter.ALL -> if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                          else MaterialTheme.colorScheme.surfaceContainerLow
    }
    val borderColor = when (filter) {
        StockFilter.AVAILABLE -> MaterialTheme.colorScheme.primary
        StockFilter.LOW_STOCK -> MaterialTheme.colorScheme.tertiary
        StockFilter.OUT_OF_STOCK -> MaterialTheme.colorScheme.error
        StockFilter.ALL -> MaterialTheme.colorScheme.secondary
    }
    val textColor = when (filter) {
        StockFilter.AVAILABLE -> MaterialTheme.colorScheme.onPrimaryContainer
        StockFilter.LOW_STOCK -> MaterialTheme.colorScheme.onTertiaryContainer
        StockFilter.OUT_OF_STOCK -> MaterialTheme.colorScheme.onErrorContainer
        StockFilter.ALL -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(17.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(17.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = filter.label,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.width(6.dp))
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = borderColor.copy(alpha = 0.85f)
            ) {
                Text(
                    text = "$count",
                    color = MaterialTheme.colorScheme.surface,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * Empty state inline (sin redibujar toda la pantalla).
 *
 * Se usa en InventoryScreen para 3 escenarios:
 *  - Sin resultados de busqueda
 *  - Timeout / fallo de carga
 *  - Inventario vacio (aun sin agregar productos)
 *
 * Cumple WCAG 2.5.8: action button >= 48dp.
 * Cumple Nielsen #1: feedback visual del estado del sistema.
 */
@Composable
private fun InlineEmptyState(
    iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(actionLabel)
            }
        }
    }
}
