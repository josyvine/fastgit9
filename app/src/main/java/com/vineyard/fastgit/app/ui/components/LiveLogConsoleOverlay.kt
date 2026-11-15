package com.vineyard.fastgit.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vineyard.fastgit.app.ui.theme.GhAccentBlue
import com.vineyard.fastgit.app.ui.theme.GhBgDark
import com.vineyard.fastgit.app.ui.theme.GhSurfaceDark
import com.vineyard.fastgit.app.ui.theme.GhTextSecondaryDark
import com.vineyard.fastgit.app.utils.AppLogger
import com.vineyard.fastgit.app.utils.LogEntry
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun LiveLogConsoleOverlay() {
    val logs by AppLogger.logs.collectAsState()
    val isOverlayVisible by AppLogger.isOverlayVisible.collectAsState()
    val isMinimized by AppLogger.isMinimized.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Count error logs
    val errorCount = remember(logs) { logs.count { it.isError } }
    val successCount = remember(logs) { logs.count { it.isSuccess } }

    var filterType by remember { mutableStateOf("ALL") } // "ALL", "ERROR", "SUCCESS"

    val filteredLogs = remember(logs, filterType) {
        when (filterType) {
            "ERROR" -> logs.filter { it.isError }
            "SUCCESS" -> logs.filter { it.isSuccess }
            else -> logs
        }
    }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportedPathsList by remember { mutableStateOf<List<String>>(emptyList()) }

    // Saved draggable position across minimize/expand toggles (-1f = default initial position)
    var offsetX by remember { mutableFloatStateOf(-1f) }
    var offsetY by remember { mutableFloatStateOf(-1f) }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log File Saved!", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Log files have been written to the 'fastgit log' folder in your device storage:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    exportedPathsList.forEach { path ->
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(6.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF28A745), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(path, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                    Text(
                        text = "💡 Tip: Open your File Manager app (e.g., Files by Google or Samsung My Files) and go to Internal Storage > Download > fastgit log or Documents > fastgit log",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.surface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Minimized Floating Circle / Pill (Freely draggable anywhere on screen)
    if (isMinimized || !isOverlayVisible) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val density = LocalDensity.current
            val buttonSize = 56.dp
            val buttonSizePx = with(density) { buttonSize.toPx() }
            val edgePaddingPx = with(density) { 12.dp.toPx() }
            val defaultRightMarginPx = with(density) { 16.dp.toPx() }
            val defaultBottomClearancePx = with(density) { 156.dp.toPx() }
            val statusTopPx = WindowInsets.statusBars.getTop(density).toFloat()
            val navBottomPx = WindowInsets.navigationBars.getBottom(density).toFloat()

            val maxWidthPx = constraints.maxWidth.toFloat()
            val maxHeightPx = constraints.maxHeight.toFloat()

            val minX = edgePaddingPx
            val maxX = (maxWidthPx - buttonSizePx - edgePaddingPx).coerceAtLeast(minX)
            val minY = statusTopPx + edgePaddingPx
            val maxY = (maxHeightPx - buttonSizePx - navBottomPx - edgePaddingPx).coerceAtLeast(minY)

            LaunchedEffect(maxWidthPx, maxHeightPx, minX, maxX, minY, maxY) {
                if (maxWidthPx > 0f && maxHeightPx > 0f) {
                    if (offsetX < 0f || offsetY < 0f) {
                        offsetX = (maxWidthPx - buttonSizePx - defaultRightMarginPx).coerceIn(minX, maxX)
                        offsetY = (maxHeightPx - buttonSizePx - navBottomPx - defaultBottomClearancePx).coerceIn(minY, maxY)
                    } else {
                        offsetX = offsetX.coerceIn(minX, maxX)
                        offsetY = offsetY.coerceIn(minY, maxY)
                    }
                }
            }

            val currentX = if (offsetX < 0f) {
                (maxWidthPx - buttonSizePx - defaultRightMarginPx).coerceIn(minX, maxX)
            } else {
                offsetX.coerceIn(minX, maxX)
            }
            val currentY = if (offsetY < 0f) {
                (maxHeightPx - buttonSizePx - navBottomPx - defaultBottomClearancePx).coerceIn(minY, maxY)
            } else {
                offsetY.coerceIn(minY, maxY)
            }

            Surface(
                onClick = { AppLogger.expandOverlay() },
                shape = CircleShape,
                color = if (errorCount > 0) Color(0xFFD73A49) else MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
                    .size(buttonSize)
                    .pointerInput(minX, maxX, minY, maxY) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val baseX = if (offsetX < 0f) currentX else offsetX
                            val baseY = if (offsetY < 0f) currentY else offsetY
                            offsetX = (baseX + dragAmount.x).coerceIn(minX, maxX)
                            offsetY = (baseY + dragAmount.y).coerceIn(minY, maxY)
                        }
                    }
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Live Logs",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )

                    // Error counter badge if any
                    if (errorCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(20.dp)
                                .background(Color.Red, CircleShape)
                                .border(1.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (errorCount > 99) "99+" else errorCount.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Full Screen Console Dialog
    if (isOverlayVisible && !isMinimized) {
        Dialog(
            onDismissRequest = { AppLogger.minimizeOverlay() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            val listState = rememberLazyListState()

            // Auto-scroll on new log entry
            LaunchedEffect(filteredLogs.size) {
                if (filteredLogs.isNotEmpty()) {
                    listState.animateScrollToItem(filteredLogs.size - 1)
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF090D16) // Terminal deep pitch black background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Live Process & Error Console",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${logs.size} total logs • $errorCount errors • $successCount successes",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                // Minimize Button
                                IconButton(
                                    onClick = { AppLogger.minimizeOverlay() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Minimize",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Close Button
                                IconButton(
                                    onClick = { AppLogger.closeOverlay() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }

                    // Action Toolbar: Filter Chips, Copy, Clear
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = filterType == "ALL",
                                onClick = { filterType = "ALL" },
                                label = { Text("All (${logs.size})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.surface,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            FilterChip(
                                selected = filterType == "ERROR",
                                onClick = { filterType = "ERROR" },
                                label = { Text("Errors ($errorCount)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD73A49),
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            FilterChip(
                                selected = filterType == "SUCCESS",
                                onClick = { filterType = "SUCCESS" },
                                label = { Text("Success ($successCount)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF28A745),
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Save Log File to SDCARD Button
                            TextButton(
                                onClick = {
                                    val paths = AppLogger.exportLogsToFiles()
                                    exportedPathsList = paths
                                    showExportDialog = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SaveAlt,
                                    contentDescription = "Save to SD",
                                    tint = Color(0xFF28A745),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save File", fontSize = 12.sp, color = Color(0xFF28A745))
                            }

                            // Copy All Logs Button
                            TextButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("FastGit App Logs", AppLogger.getFullLogText())
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Full log report copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            // Clear Logs Button
                            IconButton(
                                onClick = { AppLogger.clearLogs() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)

                    // Logs List
                    if (filteredLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = GhTextSecondaryDark,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No log entries found for filter '$filterType'",
                                    color = GhTextSecondaryDark,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF04060A))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredLogs, key = { "log_${it.id}" }) { item ->
                                LogItemRow(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItemRow(item: LogEntry) {
    val bgColor = when {
        item.isError -> Color(0xFF2C0A0A)
        item.isSuccess -> Color(0xFF0A2C12)
        else -> Color(0xFF0D1117)
    }

    val textColor = when {
        item.isError -> Color(0xFFFF7B72)
        item.isSuccess -> Color(0xFF7EE787)
        else -> Color(0xFFC9D1D9)
    }

    val tagColor = when {
        item.isError -> Color(0xFFFF4D4D)
        item.isSuccess -> Color(0xFF56D364)
        else -> GhAccentBlue
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(0.5.dp, tagColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "[${item.tag}]",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = tagColor,
                        modifier = Modifier
                            .background(tagColor.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    Text(
                        text = item.timestamp,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = GhTextSecondaryDark
                    )
                }

                if (item.isError) {
                    Text(
                        text = "ERROR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        modifier = Modifier
                            .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                } else if (item.isSuccess) {
                    Text(
                        text = "SUCCESS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF28A745),
                        modifier = Modifier
                            .background(Color(0xFF28A745).copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.message,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = textColor,
                lineHeight = 16.sp
            )
        }
    }
}