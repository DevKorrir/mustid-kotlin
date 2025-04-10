package dev.korryr.digitalid.ui.features.qrReader.presentation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun QRScannerOverlay() {
    // Create a pulsing animation for the frame
    val infiniteTransition = rememberInfiniteTransition(label = "scannerPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scannerScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Semi-transparent overlay
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .zIndex(0f)
        )

        // QR code scanning frame with cute style
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .zIndex(1f)
        ) {
            // Cut out transparent square in the middle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(24.dp)
                    )
            )

            // Corner decorations
            val cornerSize = 32.dp
            val cornerThickness = 5.dp
            val cornerColor = MaterialTheme.colorScheme.tertiary

            // Top-left corner
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .border(
                        width = cornerThickness,
                        color = cornerColor,
                        shape = RoundedCornerShape(topStart = 12.dp)
                    )
            )

            // Top-right corner
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .border(
                        width = cornerThickness,
                        color = cornerColor,
                        shape = RoundedCornerShape(topEnd = 12.dp)
                    )
            )

            // Bottom-left corner
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .border(
                        width = cornerThickness,
                        color = cornerColor,
                        shape = RoundedCornerShape(bottomStart = 12.dp)
                    )
            )

            // Bottom-right corner
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .border(
                        width = cornerThickness,
                        color = cornerColor,
                        shape = RoundedCornerShape(bottomEnd = 12.dp)
                    )
            )
        }

        // Helper text
        Text(
            text = "Position QR code in frame",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelLarge
        )
    }
}