package com.vanvault.android.ui.screens

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
import com.vanvault.android.data.models.Product
import com.vanvault.android.ui.components.NavItem
import com.vanvault.android.ui.components.VanVaultBottomNavBar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vanvault.android.data.repository.ProductRepository
import com.vanvault.android.data.repository.ProductSearch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onNavigate: (NavItem) -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onEditProductClick: () -> Unit = {}
) {
    var fabExpanded by remember { mutableStateOf(false) }
    var allProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Search states
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val repository = remember { ProductRepository() }

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
                allProducts = productList
                if (!isSearchActive) isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isSearchActive) isLoading = false
            }
        })
    }

    val displayProducts = if (isSearchActive && searchQuery.isNotBlank()) searchResults else allProducts

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { newQuery ->
                                searchQuery = newQuery
                                searchJob?.cancel()
                                searchJob = scope.launch {
                                    delay(300) // debounce
                                    if (newQuery.isNotBlank()) {
                                        isSearching = true
                                        val result = repository.search(newQuery)
                                        searchResults = when (result) {
                                            is ProductSearch.Results -> result.products
                                            else -> emptyList()
                                        }
                                        isSearching = false
                                    } else {
                                        searchResults = emptyList()
                                    }
                                }
                            },
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
                        IconButton(onClick = { 
                            isSearchActive = false
                            searchQuery = ""
                            searchResults = emptyList()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar búsqueda")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
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
            if (isLoading || isSearching) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (displayProducts.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        if (isSearchActive && searchQuery.isNotBlank()) "No se encontraron resultados" else "No hay productos en el inventario",
                        color = Color.Gray
                    )
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        val dbRef = FirebaseDatabase.getInstance().getReference("inventory")
                        dbRef.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val snapshot = task.result
                                val productList = mutableListOf<Product>()
                                for (child in snapshot.children) {
                                    val prod = child.getValue(Product::class.java)
                                    if (prod != null) {
                                        productList.add(prod)
                                    }
                                }
                                allProducts = productList
                            }
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayProducts) { product ->
                            val statusLabel = when {
                                product.quantity <= 0 -> "AGOTADO"
                                product.quantity < 5 -> "INSUFICIENTE"
                                else -> "DISPONIBLE"
                            }
                            
                            val quantityColor = when {
                                product.quantity <= 0 -> Color(0xFFDC2626) // Red
                                product.quantity < 5 -> Color(0xFFF97316) // Orange
                                else -> Color(0xFF059669) // Green
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
                            Spacer(modifier = Modifier.height(100.dp)) // Extra space for expanded FAB
                        }
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
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.surface,
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
                color = if (isSelected) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.onPrimary,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                modifier = Modifier.fillMaxWidth(),
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
