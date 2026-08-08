package com.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoosketchTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoosketchTheme {
                GoosketchMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoosketchMainScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State Variables
    var rawBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var opacity by remember { mutableFloatStateOf(0.5f) }
    var isLocked by remember { mutableStateOf(false) }
    var isTorchEnabled by remember { mutableStateOf(false) }

    var filterType by remember { mutableStateOf(FilterType.CANNY_EDGES) }
    var edgeThreshold by remember { mutableFloatStateOf(0.5f) }

    var isFlippedHorizontal by remember { mutableStateOf(false) }
    var isFlippedVertical by remember { mutableStateOf(false) }
    var showGrid by remember { mutableStateOf(false) }

    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableFloatStateOf(0f) }

    var isProcessing by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Load initial preset stencil (Goose Stencil) on start
    LaunchedEffect(Unit) {
        val initialPreset = OpenCvFilter.createPresetStencil("goose")
        rawBitmap = initialPreset
        isProcessing = true
        processedBitmap = OpenCvFilter.processEdgeDetection(
            src = initialPreset,
            threshold = edgeThreshold,
            filterType = filterType
        )
        isProcessing = false
    }

    // Function to re-process bitmap when filter type or threshold changes
    fun applyFilter(type: FilterType, thresh: Float = edgeThreshold) {
        val src = rawBitmap ?: return
        coroutineScope.launch {
            isProcessing = true
            processedBitmap = OpenCvFilter.processEdgeDetection(
                src = src,
                threshold = thresh,
                filterType = type
            )
            isProcessing = false
        }
    }

    // Photo Gallery Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (bitmap != null) {
                        withContext(Dispatchers.Main) {
                            rawBitmap = bitmap
                            scale = 1.0f
                            offset = Offset.Zero
                            rotation = 0f
                            applyFilter(filterType)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Goosketch 🪿",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.semantics { testTag = "gallery_pick_button" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Pick Image from Gallery",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showInfoDialog = !showInfoDialog },
                        modifier = Modifier.semantics { testTag = "info_button" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Goosketch",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // LAYER 1 (Bottom): Live CameraX Preview Feed
            CameraPreview(
                isTorchEnabled = isTorchEnabled,
                modifier = Modifier.fillMaxSize()
            )

            // LAYER 2 (Middle): Gesture-based Tracing Overlay Canvas
            OverlayCanvas(
                overlayBitmap = processedBitmap ?: rawBitmap,
                opacity = opacity,
                isLocked = isLocked,
                isFlippedHorizontal = isFlippedHorizontal,
                isFlippedVertical = isFlippedVertical,
                showGrid = showGrid,
                scale = scale,
                onScaleChange = { scale = it },
                offset = offset,
                onOffsetChange = { offset = it },
                rotation = rotation,
                onRotationChange = { rotation = it },
                modifier = Modifier.fillMaxSize()
            )

            // Floating Action Buttons (Positioned ABOVE the bottom control toolbar panel to avoid overlap)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 145.dp)
            ) {
                // Quick Lock Floating Button
                FilledTonalIconButton(
                    onClick = { isLocked = !isLocked },
                    modifier = Modifier.semantics { testTag = "quick_lock_button" }
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Toggle Lock Screen Alignment",
                        tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Main "AI Vectorize" / "Show Original" FAB
                ExtendedFloatingActionButton(
                    onClick = {
                        val newFilter = if (filterType == FilterType.ORIGINAL) FilterType.CANNY_EDGES else FilterType.ORIGINAL
                        filterType = newFilter
                        applyFilter(newFilter)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Vectorize Edge Extraction"
                        )
                    },
                    text = {
                        Text(if (filterType == FilterType.ORIGINAL) "AI Vectorize" else "Show Original")
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.semantics { testTag = "ai_vectorize_fab" }
                )
            }

            // Processing Indicator
            if (isProcessing) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Extracting Line-Art...",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // LAYER 3 (Top Overlays): Bottom Control Toolbar Panel
            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .semantics { testTag = "bottom_controls_bar" }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    // Opacity Slider Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Opacity,
                            contentDescription = "Opacity",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Opacity: ${(opacity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(90.dp)
                        )
                        Slider(
                            value = opacity,
                            onValueChange = { opacity = it },
                            valueRange = 0.05f..1.0f,
                            modifier = Modifier
                                .weight(1f)
                                .semantics { testTag = "opacity_slider" }
                        )
                    }

                    // Quick Action Tools Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Torch / Flashlight Toggle
                        IconButton(
                            onClick = { isTorchEnabled = !isTorchEnabled },
                            modifier = Modifier.semantics { testTag = "flashlight_button" }
                        ) {
                            Icon(
                                imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle Torch",
                                tint = if (isTorchEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Canvas Alignment Lock Button
                        IconButton(
                            onClick = { isLocked = !isLocked },
                            modifier = Modifier.semantics { testTag = "lock_button" }
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Toggle Canvas Lock",
                                tint = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Grid Overlay Toggle
                        IconButton(
                            onClick = { showGrid = !showGrid },
                            modifier = Modifier.semantics { testTag = "grid_button" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = "Toggle Grid Overlay",
                                tint = if (showGrid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Flip Horizontal
                        IconButton(
                            onClick = { isFlippedHorizontal = !isFlippedHorizontal },
                            modifier = Modifier.semantics { testTag = "flip_button" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flip,
                                contentDescription = "Mirror Horizontally",
                                tint = if (isFlippedHorizontal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Reset Alignment
                        IconButton(
                            onClick = {
                                scale = 1.0f
                                offset = Offset.Zero
                                rotation = 0f
                            },
                            modifier = Modifier.semantics { testTag = "reset_alignment_button" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Alignment",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Fine Tuning Sheet Opener
                        IconButton(
                            onClick = { showBottomSheet = true },
                            modifier = Modifier.semantics { testTag = "fine_tune_button" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Fine Tuning Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // MODAL BOTTOM SHEET for Advanced AI Line-Art & Preset Stencils Controls
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.semantics { testTag = "control_bottom_sheet" }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Line-Art & Stencil Controls",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Filter Selection Chips
                        Text(
                            text = "Edge Extraction Mode",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = filterType == FilterType.CANNY_EDGES,
                                onClick = {
                                    filterType = FilterType.CANNY_EDGES
                                    applyFilter(FilterType.CANNY_EDGES)
                                },
                                label = { Text("Line Art") }
                            )
                            FilterChip(
                                selected = filterType == FilterType.INVERTED_EDGES,
                                onClick = {
                                    filterType = FilterType.INVERTED_EDGES
                                    applyFilter(FilterType.INVERTED_EDGES)
                                },
                                label = { Text("Inverted Stencil") }
                            )
                            FilterChip(
                                selected = filterType == FilterType.HIGH_CONTRAST_SKETCH,
                                onClick = {
                                    filterType = FilterType.HIGH_CONTRAST_SKETCH
                                    applyFilter(FilterType.HIGH_CONTRAST_SKETCH)
                                },
                                label = { Text("High Contrast") }
                            )
                            FilterChip(
                                selected = filterType == FilterType.ORIGINAL,
                                onClick = {
                                    filterType = FilterType.ORIGINAL
                                    applyFilter(FilterType.ORIGINAL)
                                },
                                label = { Text("Original") }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Sensitivity / Threshold Slider
                        if (filterType != FilterType.ORIGINAL) {
                            Text(
                                text = "Edge Threshold Sensitivity",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Slider(
                                value = edgeThreshold,
                                onValueChange = {
                                    edgeThreshold = it
                                    applyFilter(filterType, it)
                                },
                                valueRange = 0.1f..0.9f
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Preset Stencils Section
                        Text(
                            text = "Preset Stencils (Quick Trace)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val stencils = listOf("Goose" to "🪿 Goose", "Floral" to "🌸 Floral", "Mandala" to "☸️ Mandala", "Star" to "⭐ Star")
                            items(stencils) { (key, label) ->
                                Card(
                                    onClick = {
                                        val stencilBmp = OpenCvFilter.createPresetStencil(key)
                                        rawBitmap = stencilBmp
                                        scale = 1.0f
                                        offset = Offset.Zero
                                        rotation = 0f
                                        applyFilter(filterType)
                                    },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Info Dialog
            if (showInfoDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showInfoDialog = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "About Goosketch 🪿",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Goosketch is a 100% free, open-source, privacy-first AR tracing app for Android.\n\n" +
                                    "• Offline Processing: Edge detection runs locally on device without cloud APIs or telemetry.\n" +
                                    "• Alignment Lock: Freeze overlay movement while tracing on physical paper.\n" +
                                    "• AI Line-Art Vectorization: Convert photos into clear outlines for effortless tracing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
