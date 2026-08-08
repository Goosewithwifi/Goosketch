package com.example

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun OverlayCanvas(
    overlayBitmap: Bitmap?,
    opacity: Float,
    isLocked: Boolean,
    isFlippedHorizontal: Boolean = false,
    isFlippedVertical: Boolean = false,
    showGrid: Boolean = false,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    offset: Offset,
    onOffsetChange: (Offset) -> Unit,
    rotation: Float,
    onRotationChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { testTag = "overlay_canvas_container" }
    ) {
        if (overlayBitmap != null) {
            val imageBitmap = remember(overlayBitmap) { overlayBitmap.asImageBitmap() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isLocked) {
                        if (!isLocked) {
                            detectTransformGestures { _, pan, zoom, rotate ->
                                val newScale = (scale * zoom).coerceIn(0.2f, 8.0f)
                                onScaleChange(newScale)
                                onRotationChange(rotation + rotate)
                                onOffsetChange(
                                    Offset(
                                        x = offset.x + pan.x,
                                        y = offset.y + pan.y
                                    )
                                )
                            }
                        }
                    }
                    .pointerInput(isLocked) {
                        if (!isLocked) {
                            detectTapGestures(
                                onDoubleTap = {
                                    // Reset alignment on double tap when unlocked
                                    onScaleChange(1.0f)
                                    onRotationChange(0f)
                                    onOffsetChange(Offset.Zero)
                                }
                            )
                        }
                    }
            ) {
                // The Overlay Image with Gestures Applied
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = if (isFlippedHorizontal) -scale else scale
                            scaleY = if (isFlippedVertical) -scale else scale
                            rotationZ = rotation
                            translationX = offset.x
                            translationY = offset.y
                            alpha = opacity
                        },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = imageBitmap,
                        contentDescription = "Tracing Reference Overlay",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            // Empty State Rationale
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                    tonalElevation = 6.dp
                ) {
                    Text(
                        text = "Tap Gallery 🖼️ to pick an image or select a Preset Stencil 🪿 below to start tracing!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(20.dp),
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Grid Overlay for Proportional Tracing Guidelines
        if (showGrid) {
            val gridColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val columns = 4
                val rows = 6

                val colWidth = width / columns
                val rowHeight = height / rows

                // Vertical Grid Lines
                for (i in 1 until columns) {
                    drawLine(
                        color = gridColor,
                        start = Offset(colWidth * i, 0f),
                        end = Offset(colWidth * i, height),
                        strokeWidth = 1.5f.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                // Horizontal Grid Lines
                for (i in 1 until rows) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, rowHeight * i),
                        end = Offset(width, rowHeight * i),
                        strokeWidth = 1.5f.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }
        }

        // Lock Banner Indicator
        AnimatedVisibility(
            visible = isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                tonalElevation = 8.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Canvas Locked",
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 6.dp)
                    )
                    Text(
                        text = "Canvas Locked (Tracing Mode)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
