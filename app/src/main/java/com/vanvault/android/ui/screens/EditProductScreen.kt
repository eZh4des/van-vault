package com.vanvault.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.vanvault.android.ui.components.M3TextField
import com.vanvault.android.ui.components.VanVaultDialog
import com.vanvault.android.ui.viewmodel.EditProductViewModel
import com.vanvault.android.ui.util.A11y.merged

/**
 * Pantalla "Editar Producto" refactorizada con UDF + ViewModel (T01).
 *
 * **Antes**: instanciaba `FirebaseDatabase.getInstance()`, `ProductRepository` y
 * mantenía 10 `remember { mutableStateOf(...) }`.
 * **Ahora**: delega todo al ViewModel; la UI solo emite eventos y renderiza estado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    navController: NavController,
    onBackClick: () -> Unit,
    viewModel: EditProductViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showNotFoundDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                EditProductViewModel.EditProductEffect.Saved -> onBackClick()
                EditProductViewModel.EditProductEffect.NotFound -> showNotFoundDialog = true
                is EditProductViewModel.EditProductEffect.Error -> {
                    errorMessage = effect.message
                    showErrorDialog = true
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Editar Producto", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Buscador
            Text(
                "Busca por nombre de producto o coincidencia:",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Ej. panel solar 100w") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.onSearch() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar producto",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            if (state.isSearching) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }

            // Resultados de búsqueda (cuando hay varios)
            if (state.searchResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(state.searchResults, key = { it.id }) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onSelectFromList(product) }
                                // T09: anunciar el producto como una unidad
                                .merged(
                                    description = "${product.name}, SKU ${product.modelOrBarcode}, ${product.quantity} unidades, ${product.unitPrice.toDisplayString()}"
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    product.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "SKU: ${product.modelOrBarcode}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            VanVaultDialog(
                showDialog = showNotFoundDialog,
                title = "Producto no encontrado",
                message = "El producto no se encuentra agregado. Por favor dirígete a la pantalla Agregar Producto.",
                onDismiss = { showNotFoundDialog = false }
            )

            VanVaultDialog(
                showDialog = showErrorDialog,
                title = "Error",
                message = errorMessage,
                onDismiss = { showErrorDialog = false }
            )

            // Form de edición
            if (state.isEditing) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                M3TextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Nombre del producto",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )

                M3TextField(
                    value = state.modelOrBarcode,
                    onValueChange = viewModel::onModelChange,
                    label = "Modelo / EAN-13",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )

                M3TextField(
                    value = state.quantity,
                    onValueChange = viewModel::onQuantityChange,
                    label = "Cantidad en inventario",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )

                M3TextField(
                    value = state.unitPrice,
                    onValueChange = viewModel::onPriceChange,
                    label = "Precio unitario (COP)",
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.onSave() },
                    enabled = state.canSubmit,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar Cambios", style = MaterialTheme.typography.labelLarge)
                    }
                }
            } else if (!state.isSearching) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Busca un producto para empezar a editar",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}