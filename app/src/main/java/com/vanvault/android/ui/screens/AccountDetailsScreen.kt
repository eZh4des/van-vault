package com.vanvault.android.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.vanvault.android.ui.util.minTouchTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    onBackClick: () -> Unit,
    onChangePasswordClick: () -> Unit = {},
    onTwoFactorClick: () -> Unit = {}
) {
    val user = com.vanvault.android.di.FirebaseModule.provideFirebaseAuth().currentUser
    val userEmail = user?.email ?: ""
    
    // Estados para la base de datos
    var name by remember { mutableStateOf(user?.displayName ?: "") }
    var phone by remember { mutableStateOf("Cargando...") }

    // Recuperar datos de la base de datos
    LaunchedEffect(userEmail) {
        user?.uid?.let { uid ->
            val ref = com.vanvault.android.di.FirebaseModule.provideFirebaseDatabase().getReference("users").child(uid)
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Mi cuenta",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
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
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Información Personal Section
            SectionHeader(title = "INFORMACIÓN PERSONAL")
            AccountListItem(label = "Nombre completo", value = name, showDivider = true)
            AccountListItem(label = "Correo electrónico", value = userEmail, showDivider = true)
            AccountListItem(label = "Teléfono", value = phone, showDivider = false)

            // Seguridad Section
            SectionHeader(title = "SEGURIDAD")
            AccountListItem(
                label = "Cambiar contraseña",
                value = null,
                showDivider = true,
                onClick = onChangePasswordClick
            )
            AccountListItem(
                label = "Autenticación de dos factores",
                value = "Desactivado",
                showDivider = false,
                onClick = onTwoFactorClick
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Fila de informacion de la cuenta.
 *
 * @param onClick accion opcional. Si es null la fila es puramente
 *   informativa (no clickeable) — evita el "boton muerto" que teniamos
 *   antes con `.clickable { /* TODO */ }`.
 */
@Composable
fun AccountListItem(
    label: String,
    value: String?,
    showDivider: Boolean,
    onClick: (() -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                // T10: area tactil minima 48dp cuando es interactiva
                .then(if (onClick != null) Modifier.minTouchTarget() else Modifier)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(0.4f) // Le damos el 40% del espacio a la columna 1 (Label)
            )
            
            Spacer(modifier = Modifier.width(16.dp)) // Margen de separación obligatorio
            
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.6f) // Le damos el 60% del espacio a la columna 2 (Valor) para que pueda crecer a la izquierda
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.weight(0.6f))
            }
            
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp
            )
        }
    }
}
