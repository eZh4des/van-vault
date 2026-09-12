package com.example.vanvault.ui.screens

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isRegistered by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crear cuenta",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            if (isRegistered) {
                Text(
                    text = "¡Registro Exitoso!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Hemos enviado un enlace de validación a $email. Por favor, revisa tu correo (y bandeja de spam) y haz clic en el enlace para verificar tu cuenta.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 32.dp),
                    color = Color.Black
                )
                Button(
                    onClick = onLoginClick, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Volver al Inicio de Sesión", color = Color.White)
                }
            } else {

                // Nombre Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Nombre completo",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej. Juan Pérez", color = Color.LightGray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            unfocusedBorderColor = Color(0xFFE4E4E7),
                            focusedBorderColor = Color(0xFFDC2626),
                            cursorColor = Color(0xFFDC2626)
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Email Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Correo electrónico",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("correo@ejemplo.com", color = Color.LightGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            unfocusedBorderColor = Color(0xFFE4E4E7),
                            focusedBorderColor = Color(0xFFDC2626),
                            cursorColor = Color(0xFFDC2626)
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Password Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••", color = Color.LightGray) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            unfocusedBorderColor = Color(0xFFE4E4E7),
                            focusedBorderColor = Color(0xFFDC2626),
                            cursorColor = Color(0xFFDC2626)
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Confirm Password Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Confirmar contraseña",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••", color = Color.LightGray) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            unfocusedBorderColor = Color(0xFFE4E4E7),
                            focusedBorderColor = Color(0xFFDC2626),
                            cursorColor = Color(0xFFDC2626)
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Register Button
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && name.isNotEmpty()) {
                            if (password == confirmPassword) {
                                if (password.length >= 6) {
                                    isLoading = true
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                auth.currentUser?.sendEmailVerification()
                                                    ?.addOnCompleteListener { verificationTask ->
                                                        isLoading = false
                                                        if (verificationTask.isSuccessful) {
                                                            isRegistered = true
                                                        } else {
                                                            Toast.makeText(context, "Error al enviar correo de validación: ${verificationTask.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                            } else {
                                                isLoading = false
                                                Toast.makeText(context, "Error de registro: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626)
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(
                            text = "Registrarse",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Login Link
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes una cuenta?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
                TextButton(onClick = onLoginClick) {
                    Text(
                        text = "Inicia sesión",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen()
    }
}
