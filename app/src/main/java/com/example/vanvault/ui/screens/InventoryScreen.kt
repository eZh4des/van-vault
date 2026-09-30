package com.example.vanvault.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vanvault.data.models.Product
import com.example.vanvault.ui.components.NavItem
import com.example.vanvault.ui.components.VanVaultBottomNavBar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onNavigate: (NavItem) -> Unit = {},
    onAddProductClick: () -> Unit = {}
) {
    var fabExpanded by remember { mutableStateOf(false) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("inventory")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<Product>()
                for (child in snapshot.children) {
                    val prod = child.getValue(Product::class.java)
                    if (prod != null) {
                        productList.add(prod)
                    }
                }
                products = productList
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Inventario",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
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
                        FabOption(text = "Editar producto", onClick = { fabExpanded = false })
                        FabOption(text = "Eliminar producto", onClick = { fabExpanded = false })
                    }
                }

                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Opciones",
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
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipCustom(text = "Disponible", isSelected = true)
                FilterChipCustom(text = "Insuficiente", isSelected = false)
                FilterChipCustom(text = "Agotado", isSelected = false)
                FilterChipCustom(text = "Todos", isSelected = false)
            }

            // Inventory List
            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFDC2626))
                }
            } else if (products.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No hay productos en el inventario", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products) { product ->
                        val statusLabel = when {
                            product.quantity <= 0 -> "AGOTADO"
                            product.quantity < 5 -> "INSUFICIENTE"
                            else -> "DISPONIBLE"
                        }
                        
                        val quantityColor = when {
                            product.quantity <= 0 -> Color(0xFFDC2626) // Rojo
                            product.quantity < 5 -> Color(0xFFF97316) // Naranja
                            else -> Color(0xFF059669) // Verde
                        }

                        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                        format.maximumFractionDigits = 0
                        val formattedPrice = format.format(product.unitPrice)

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
                        Spacer(modifier = Modifier.height(100.dp)) // Espacio extra para el FAB expandido
                    }
                }
            }
        }
    }
}

@Composable
fun FabOption(text: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFDC2626),
        shadowElevation = 4.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun FilterChipCustom(text: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) Color(0xFFFEE2E2) else Color.White,
                shape = RoundedCornerShape(17.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFFDC2626) else Color(0xFFE4E4E7),
                shape = RoundedCornerShape(17.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFFDC2626) else Color(0xFF71717A),
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun InventoryItemCard(
    title: String,
    subtitle: String,
    statusLabel: String,
    quantity: String,
    quantityColor: Color = Color(0xFFDC2626),
    priceLabel: String,
    price: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE4E4E7))
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
                color = Color(0xFF1C1C1E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFF71717A)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        color = Color(0xFF71717A),
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
                        color = Color(0xFF71717A),
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = price,
                        color = Color(0xFF1C1C1E),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
