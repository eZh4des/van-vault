package com.vanvault.android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vanvault.android.domain.model.Language
import com.vanvault.android.ui.components.ScrollableColumn
import com.vanvault.android.ui.util.minTouchTarget
import com.vanvault.android.ui.components.ScrollableColumn
import com.vanvault.android.ui.util.A11y.merged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    currentLanguage: Language,
    onLanguageChange: (Language) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Idioma",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        ScrollableColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LanguageOption(
                language = Language.SPANISH_MX,
                isSelected = currentLanguage == Language.SPANISH_MX,
                onSelect = { onLanguageChange(Language.SPANISH_MX) }
            )
            LanguageOption(
                language = Language.ENGLISH_US,
                isSelected = currentLanguage == Language.ENGLISH_US,
                onSelect = { onLanguageChange(Language.ENGLISH_US) }
            )
        }
    }
}

@Composable
private fun LanguageOption(
    language: Language,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            // T10: area tactil minima 48dp (WCAG 2.5.8)
            .minTouchTarget()
            // T09: indicar el idioma seleccionado
            .merged(
                description = language.displayName,
                state = if (isSelected) "Seleccionado" else null
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = language.code,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
