package com.anant.sonqiva.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.anant.sonqiva.ui.theme.BackgroundDark

@Composable
fun AtmosphericBackground(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF6366F1),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Subtle atmospheric background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.18f),
                            Color(0xFF3B1E63).copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 1200f
                    )
                )
        )
        content()
    }
}
