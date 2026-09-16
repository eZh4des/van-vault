package com.example.vanvault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    onBackClick: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val userEmail = user?.email ?: ""
    
    // Estados para la base de datos
    var name by remember { mutableStateOf(user?.displayName ?: "") }
    var phone by remember { mutableStateOf("Cargando...") }

    // Recuperar datos de la base de datos
    LaunchedEffect(userEmail) {
        user?.uid?.let { uid ->
            val ref = FirebaseDatabase.getInstance().getReference("users").child(uid)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dbName = snapshot.child("name").getValue(String::class.java)
                    val dbPhone = snapshot.child("phone").getValue(String::class.java)
                    
                    if (!dbName.isNullOrBlank()) {
                        name = dbName
                    }
                    if (!dbPhone.isNullOrBlank()) {
                        phone = dbPhone
                    } else {
                        phone = "No registrado"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    phone = "Error al cargar"
                }
            })
        } ?: run {
            phone = "No registrado"
        }
    }

    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .takeIf { it.isNotBlank() } ?: "U" // 'U' de Usuario si está vacío

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Mi cuenta",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = Color(0xFF1C1C1E)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = Color(0xFF1C1C1E)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
                HorizontalDivider(color = Color(0xFFE4E4E7), thickness = 1.dp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFFEE2E2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color(0xFFDC2626),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF1C1C1E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = Color(0xFF71717A)
                )
            }

            // Información Personal Section
            SectionHeader(title = "INFORMACIÓN PERSONAL")
            AccountListItem(label = "Nombre completo", value = name, showDivider = true)
            AccountListItem(label = "Correo electrónico", value = userEmail, showDivider = true)
            AccountListItem(label = "Teléfono", value = phone, showDivider = false)

            // Seguridad Section
            SectionHeader(title = "SEGURIDAD")
            AccountListItem(label = "Cambiar contraseña", value = null, showDivider = true)
            AccountListItem(label = "Autenticación de dos factores", value = "Desactivado", showDivider = false)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F4F5))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color(0xFF71717A),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AccountListItem(label: String, value: String?, showDivider: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO */ }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = Color(0xFF71717A)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF71717A)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color = Color(0xFFE4E4E7),
                thickness = 1.dp
            )
        }
    }
}
