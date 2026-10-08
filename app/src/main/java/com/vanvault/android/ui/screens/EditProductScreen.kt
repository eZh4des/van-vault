
package com.vanvault.android.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vanvault.android.data.models.Product
import com.google.firebase.database.FirebaseDatabase
import com.vanvault.android.data.repository.ProductRepository
import com.vanvault.android.data.repository.ProductSearch
import com.vanvault.android.ui.components.VanVaultDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    navController: NavController,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    
    // Search states
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Product>>(emptyList()) }
    var showNotFoundDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val repository = remember { ProductRepository() }
    
    // Edited product states
    var currentProductId by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var modelOrBarcode by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    
    var isSaving by remember { mutableStateOf(false) }



    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Editar Producto", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search bar
            Text("Busca por nombre de producto o coincidencia:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Ej. panel solar 100w") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        if (searchQuery.isNotBlank()) {
                            scope.launch {
                                isSearching = true
                                val result = repository.search(searchQuery.trim())
                                isSearching = false
                                when (result) {
                                    is ProductSearch.Results -> {
                                        searchResults = result.products
                                        if (result.products.size == 1) {
                                            val product = result.products.first()
                                            currentProductId = product.id
                                            name = product.name
                                            modelOrBarcode = product.modelOrBarcode
                                            quantity = product.quantity.toString()
                                            unitPrice = product.unitPrice.toLong().toString()
                                            searchResults = emptyList()
                                        } else {
                                            currentProductId = null
                                        }
                                    }
                                    is ProductSearch.Empty -> {
                                        searchResults = emptyList()
                                        showNotFoundDialog = true
                                    }
                                    is ProductSearch.Error -> {
                                        searchResults = emptyList()
                                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            if (isSearching) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
            }

            if (searchResults.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                    items(searchResults) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    currentProductId = product.id
                                    name = product.name
                                    modelOrBarcode = product.modelOrBarcode
                                    quantity = product.quantity.toString()
                                    unitPrice = product.unitPrice.toLong().toString()
                                    searchResults = emptyList()
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(product.name, fontWeight = FontWeight.SemiBold)
                                Text("SKU: ${product.modelOrBarcode}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            VanVaultDialog(
                showDialog = showNotFoundDialog,
                title = "Producto no encontrado",
                message = "El producto no se encuentra agregado, por favor diríjase a la vista agregar producto y lo agrega.",
                onDismiss = { showNotFoundDialog = false }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))

            // Edit form (only visible if a product is selected)
            if (currentProductId != null) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del producto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )

                OutlinedTextField(
                    value = modelOrBarcode,
                    onValueChange = { modelOrBarcode = it },
                    label = { Text("Modelo / EAN-13") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad en inventario") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )

                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    label = { Text("Precio unitario (COP, sin puntos ni comas)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (name.isBlank() || modelOrBarcode.isBlank() || quantity.isBlank() || unitPrice.isBlank()) {
                            Toast.makeText(context, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSaving = true
                        val updatedProduct = Product(
                            id = currentProductId!!,
                            name = name.trim(),
                            modelOrBarcode = modelOrBarcode.trim(),
                            quantity = quantity.toIntOrNull() ?: 0,
                            unitPrice = unitPrice.toDoubleOrNull() ?: 0.0
                        )

                        FirebaseDatabase.getInstance().getReference("inventory").child(currentProductId!!)
                            .setValue(updatedProduct).addOnCompleteListener { task ->
                                isSaving = false
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                } else {
                                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_LONG).show()
                                }
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Busca un producto para empezar a editar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

