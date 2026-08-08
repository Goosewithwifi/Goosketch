package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.hypot

enum class FilterType {
    ORIGINAL,
    CANNY_EDGES,
    HIGH_CONTRAST_SKETCH,
    INVERTED_EDGES
}

object OpenCvFilter {

    /**
     * Converts a source Bitmap into clean line-art / edge detection sketch offline.
     */
    suspend fun processEdgeDetection(
        src: Bitmap,
        threshold: Float = 0.5f,
        filterType: FilterType = FilterType.CANNY_EDGES
    ): Bitmap = withContext(Dispatchers.Default) {
        if (filterType == FilterType.ORIGINAL) return@withContext src

        val width = src.width
        val height = src.height

        // Downscale large bitmaps for fast processing
        val maxDim = 800
        val scale = if (width > maxDim || height > maxDim) {
            maxDim.toFloat() / maxOf(width, height)
        } else 1.0f

        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)

        val scaledSrc = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)
        } else src

        val grayBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayBitmap)
        val paint = Paint()

        // Grayscale matrix
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(scaledSrc, 0f, 0f, paint)

        val pixels = IntArray(targetWidth * targetHeight)
        grayBitmap.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)

        val grayLuminance = IntArray(targetWidth * targetHeight) { i ->
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }

        val outputPixels = IntArray(targetWidth * targetHeight)
        val edgeCutoff = (10 + threshold * 120).toInt()

        // Sobel Gradient Edge Extraction
        for (y in 1 until targetHeight - 1) {
            for (x in 1 until targetWidth - 1) {
                val idx = y * targetWidth + x

                // Sobel kernels
                val gx = -grayLuminance[idx - targetWidth - 1] + grayLuminance[idx - targetWidth + 1] -
                        2 * grayLuminance[idx - 1] + 2 * grayLuminance[idx + 1] -
                        grayLuminance[idx + targetWidth - 1] + grayLuminance[idx + targetWidth + 1]

                val gy = -grayLuminance[idx - targetWidth - 1] - 2 * grayLuminance[idx - targetWidth] - grayLuminance[idx - targetWidth + 1] +
                        grayLuminance[idx + targetWidth - 1] + 2 * grayLuminance[idx + targetWidth] + grayLuminance[idx + targetWidth + 1]

                val mag = hypot(gx.toDouble(), gy.toDouble()).toInt().coerceIn(0, 255)

                val isEdge = mag > edgeCutoff

                when (filterType) {
                    FilterType.CANNY_EDGES -> {
                        // Dark lines on white background
                        outputPixels[idx] = if (isEdge) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                    }
                    FilterType.INVERTED_EDGES -> {
                        // White lines on dark background
                        outputPixels[idx] = if (isEdge) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
                    }
                    FilterType.HIGH_CONTRAST_SKETCH -> {
                        val lum = grayLuminance[idx]
                        val contrastCutoff = (80 + threshold * 100).toInt()
                        outputPixels[idx] = if (lum < contrastCutoff) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                    }
                    else -> {
                        outputPixels[idx] = pixels[idx]
                    }
                }
            }
        }

        val resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(outputPixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)
        resultBitmap
    }

    /**
     * Generates a clean preset vector graphic stencil Bitmap for instant AR tracing.
     */
    fun createPresetStencil(type: String): Bitmap {
        val width = 800
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 10f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        when (type.lowercase()) {
            "goose" -> {
                val path = Path()
                // Goose outline drawing
                path.moveTo(350f, 650f)
                // Body
                path.cubicTo(200f, 650f, 200f, 450f, 350f, 450f)
                path.cubicTo(320f, 350f, 320f, 250f, 380f, 200f)
                // Head
                path.cubicTo(410f, 170f, 480f, 170f, 510f, 210f)
                // Beak
                path.moveTo(500f, 200f)
                path.lineTo(600f, 220f)
                path.lineTo(500f, 240f)
                // Eye
                canvas.drawCircle(460f, 210f, 8f, paint)
                // Back neck to body
                path.moveTo(480f, 240f)
                path.cubicTo(420f, 320f, 450f, 420f, 550f, 480f)
                path.cubicTo(650f, 520f, 650f, 650f, 350f, 650f)
                // Tail feathers
                path.moveTo(220f, 550f)
                path.lineTo(150f, 530f)
                path.lineTo(200f, 580f)
                path.lineTo(140f, 580f)
                path.lineTo(210f, 620f)
                // Wings detail
                path.moveTo(320f, 500f)
                path.cubicTo(280f, 540f, 300f, 600f, 450f, 610f)

                canvas.drawPath(path, paint)
            }
            "floral" -> {
                // Rose / Flower stencil
                val center = 400f
                for (r in 40..250 step 35) {
                    val count = (r / 20) + 3
                    for (i in 0 until count) {
                        val angle = (2 * Math.PI / count) * i
                        val cx = center + (r * Math.cos(angle)).toFloat()
                        val cy = center + (r * Math.sin(angle)).toFloat()
                        canvas.drawCircle(cx, cy, (r / 2.2f), paint)
                    }
                }
                // Stem and leaf
                val stemPath = Path().apply {
                    moveTo(400f, 600f)
                    cubicTo(380f, 680f, 420f, 720f, 400f, 780f)
                }
                canvas.drawPath(stemPath, paint)
            }
            "mandala" -> {
                val cx = 400f
                val cy = 400f
                canvas.drawCircle(cx, cy, 350f, paint)
                canvas.drawCircle(cx, cy, 250f, paint)
                canvas.drawCircle(cx, cy, 150f, paint)
                canvas.drawCircle(cx, cy, 50f, paint)

                for (i in 0 until 12) {
                    val angle = Math.toRadians(i * 30.0)
                    val x1 = cx + (50 * Math.cos(angle)).toFloat()
                    val y1 = cy + (50 * Math.sin(angle)).toFloat()
                    val x2 = cx + (350 * Math.cos(angle)).toFloat()
                    val y2 = cy + (350 * Math.sin(angle)).toFloat()
                    canvas.drawLine(x1, y1, x2, y2, paint)
                }
            }
            else -> {
                // Star & Geometric Art
                val path = Path()
                val cx = 400f
                val cy = 400f
                val outerRadius = 320f
                val innerRadius = 140f
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) outerRadius else innerRadius
                    val angle = i * Math.PI / 5 - Math.PI / 2
                    val x = cx + (r * Math.cos(angle)).toFloat()
                    val y = cy + (r * Math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                canvas.drawPath(path, paint)
                canvas.drawCircle(cx, cy, 80f, paint)
            }
        }

        return bitmap
    }
}
