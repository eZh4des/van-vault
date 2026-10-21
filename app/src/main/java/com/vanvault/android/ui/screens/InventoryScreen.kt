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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vanvault.android.domain.model.Product
import com.vanvault.android.domain.model.StockStatus
import com.vanvault.android.ui.components.NavItem
import com.vanvault.android.ui.components.VanVaultBottomNavBar
import com.vanvault.android.ui.viewmodel.InventoryViewModel
import com.vanvault.android.ui.viewmodel.StockFilter

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                            fontSize = 24.sp
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
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (state.isSearchActive && state.searchQuery.isNotBlank())
                            "No se encontraron resultados"
                        else
                            "No hay productos en el inventario",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                                price = formattedPrice
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
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
            fontSize = 14.sp,
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
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
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
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = quantity,
                        color = quantityColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = priceLabel,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = price,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
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
                fontSize = 14.sp
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
