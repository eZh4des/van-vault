package com.vanvault.android.ui.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Observa el estado de conectividad de red en tiempo real.
 *
 * Usa [ConnectivityManager.registerDefaultNetworkCallback] para reaccionar
 * a cambios de red (no solo al abrir la pantalla).
 *
 * Cumple T07 de la auditoria: 'Gestión de Estados Vacios (Empty States)
 * y Modo Sin Conexion' — la app debe comunicar claramente cuando no hay red.
 *
 * @return `true` si hay red disponible (o si no se puede determinar).
 */
@Composable
fun rememberIsOnline(): State<Boolean> {
    val context = LocalContext.current
    val isOnline = remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm == null) {
            isOnline.value = true
            return@DisposableEffect onDispose { }
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isOnline.value = true }
            override fun onLost(network: Network) { isOnline.value = false }
            override fun onUnavailable() { isOnline.value = false }
        }

        // Estado inicial (por si ya estamos offline al abrir la pantalla)
        val capabilities = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        isOnline.value = capabilities?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) ?: true

        try {
            cm.registerDefaultNetworkCallback(callback)
        } catch (e: SecurityException) {
            isOnline.value = true
        }

        onDispose {
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (e: IllegalArgumentException) {
                // El callback ya fue desregistrado
            }
        }
    }

    return isOnline
}
