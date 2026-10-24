package com.vanvault.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vanvault.android.R

/**
 * Logo de VanVault — replica el icono adaptive del launcher (`mipmap/ic_launcher`).
 *
 * El launcher esta definido en `mipmap-anydpi-v26/ic_launcher.xml` como:
 *  - background: `@color/ic_launcher_background` (rojo de marca #DC2626)
 *  - foreground: `@mipmap/ic_launcher_foreground` (silueta de la van)
 *
 * `painterResource` no soporta `AdaptiveIconDrawable`, por eso el icono se
 * compone aqui con las mismas dos capas, de modo que el login muestre
 * exactamente el mismo logo que el usuario ve al instalar la app.
 *
 * La escala 108/72 emula el recorte central que aplica el launcher (el
 * foreground es 108x108 y solo se muestra la zona segura central de 72x72).
 */
@Composable
fun VanVaultLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    cornerRadius: Dp = 16.dp,
    contentDescription: String = "VanVault Logo"
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(colorResource(R.color.ic_launcher_background))
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .scale(108f / 72f)
        )
    }
}
