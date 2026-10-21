package com.vanvault.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.vanvault.android.ui.viewmodel.AddProductViewModel

/**
 * Pantalla "Agregar Producto" refactorizada con UDF + ViewModel.
 *
 * **Cambios (T01)**:
 *  - NO importa `FirebaseDatabase`, `Product`, `Repository` directamente.
 *  - Estado de formulario vive en [AddProductViewModel].
 *  - La validación se ejecuta en el ViewModel, no en el Composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    onBackClick: () -> Unit,
    onScannerClick: () -> Unit,
    viewModel: AddProductViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Escuchar efectos one-shot (Saved → navegar atrás)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            if (effect is AddProductViewModel.AddProductEffect.Saved) {
                onBackClick()
            }
        }
    }

    // Observar el barcode que viene del ScannerScreen (sin acoplar a NavController desde VM)
    val scannedBarcode = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("scanned_barcode")
        ?.observeAsState()

    LaunchedEffect(scannedBarcode?.value) {
        scannedBarcode?.value?.let { barcode ->
            viewModel.onBarcodeScanned(barcode)
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scanned_barcode")
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Agregar Producto", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(48.dp) // WCAG: ≥48dp touch target
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
            // Nombre del producto
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error
                )
            )

            // Modelo / Código de barras + scanner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.modelOrBarcode,
                    onValueChange = viewModel::onModelChange,
                    label = { Text("Modelo / EAN-13") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                FilledIconButton(
                    onClick = onScannerClick,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "Escanear código de barras",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Cantidad
            OutlinedTextField(
                value = state.quantity,
                onValueChange = viewModel::onQuantityChange,
                label = { Text("Cantidad en inventario") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                isError = state.quantityError != null,
                supportingText = state.quantityError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )

            // Precio Unitario
            OutlinedTextField(
                value = state.unitPrice,
                onValueChange = viewModel::onPriceChange,
                label = { Text("Precio unitario (COP)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                isError = state.priceError != null,
                supportingText = state.priceError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón Guardar
            Button(
                onClick = { viewModel.onSave() },
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
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
                    Text(
                        "Guardar Producto",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}