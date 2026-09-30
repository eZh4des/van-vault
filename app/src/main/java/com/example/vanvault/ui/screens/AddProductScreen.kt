package com.example.vanvault.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vanvault.data.models.Product
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    onBackClick: () -> Unit,
    onScannerClick: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var modelOrBarcode by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Observar el resultado del escáner en tiempo real
    val scannedBarcode = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("scanned_barcode")
        ?.observeAsState()

    LaunchedEffect(scannedBarcode?.value) {
        scannedBarcode?.value?.let { barcode ->
            modelOrBarcode = barcode
            // Limpiamos el valor para no re-procesarlo
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scanned_barcode")
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text("Agregar Producto", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre del producto
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFDC2626),
                    focusedLabelColor = Color(0xFFDC2626)
                )
            )

            // Modelo / Código de barras
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = modelOrBarcode,
                    onValueChange = { modelOrBarcode = it },
                    label = { Text("Modelo / EAN-13") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFDC2626),
                        focusedLabelColor = Color(0xFFDC2626)
                    )
                )

                FilledIconButton(
                    onClick = onScannerClick,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(top = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear", modifier = Modifier.size(28.dp))
                }
            }

            // Cantidad
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Cantidad en inventario") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFDC2626),
                    focusedLabelColor = Color(0xFFDC2626)
                )
            )

            // Precio Unitario
            OutlinedTextField(
                value = unitPrice,
                onValueChange = { unitPrice = it },
                label = { Text("Precio unitario (COP, sin puntos ni comas)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFDC2626),
                    focusedLabelColor = Color(0xFFDC2626)
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón Guardar
            Button(
                onClick = {
                    if (name.isBlank() || modelOrBarcode.isBlank() || quantity.isBlank() || unitPrice.isBlank()) {
                        Toast.makeText(context, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    
                    // Referencia a la tabla de inventario
                    val dbRef = FirebaseDatabase.getInstance().getReference("inventory")
                    
                    // Generamos un ID de Firebase (Push ID) que es corto y cronológico
                    val productId = dbRef.push().key ?: return@Button
                    
                    val product = Product(
                        id = productId,
                        name = name.trim(),
                        modelOrBarcode = modelOrBarcode.trim(),
                        quantity = quantity.toIntOrNull() ?: 0,
                        unitPrice = unitPrice.toDoubleOrNull() ?: 0.0
                    )

                    dbRef.child(productId).setValue(product).addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Producto guardado", Toast.LENGTH_SHORT).show()
                            onBackClick() // Devolver a la pantalla anterior
                        } else {
                            Toast.makeText(context, "Error al guardar el producto", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Producto", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
