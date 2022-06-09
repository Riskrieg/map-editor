package com.riskrieg.editor.view.map

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.scale
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.riskrieg.editor.view.ViewConstants
import com.riskrieg.editor.viewmodel.MapViewModel
import java.awt.Point
import kotlin.math.pow

@OptIn(ExperimentalFoundationApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun MapView(model: MapViewModel, modifier: Modifier) {
    Column(modifier = Modifier.background(color = ViewConstants.UI_BACKGROUND_DARK)) {
        Row(modifier = Modifier.weight(1f)) {
            MapSidebarView(model, modifier = Modifier.fillMaxHeight().width(180.dp))
            Column(Modifier.weight(1f)) {
                MapViewport(model, modifier)
            }
        }
        MapFooterView(
            model,
            Modifier.fillMaxWidth().height(25.dp)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
private fun MapViewport(model: MapViewModel, modifier: Modifier) {
    val stateVertical = rememberScrollState(0)
    val stateHorizontal = rememberScrollState(0)

    var pointerPos: Offset by remember { mutableStateOf(Offset(0f, 0f)) }

    var scale: Float by remember { mutableStateOf(1.0f) }

    var canZoom: Boolean by remember { mutableStateOf(false) }

    val maxScale = 1.75f
    val minScale = 0.65f

    val focusRequester = remember(::FocusRequester)
    LaunchedEffect(Unit) {
        focusRequester.requestFocus() // TODO: Fix focus stuff after entering territory name
    }

    Box(modifier = modifier.background(color = ViewConstants.UI_BACKGROUND_DARK)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(stateVertical)
                .horizontalScroll(stateHorizontal)
        ) {
            Canvas(modifier = Modifier
                .width((model.mapImage().width * scale).dp)
                .height((model.mapImage().height * scale).dp)
                .align(Alignment.Center)
                .focusable(true)
                .focusRequester(focusRequester)
                .focusTarget()
                .pointerMoveFilter(
                    onMove = { pointerPos = it; false },
                    onExit = { pointerPos = Offset(0f, 0f); false }
                ).onKeyEvent { keyEvent ->
                    canZoom = keyEvent.isCtrlPressed
                    if (keyEvent.isCtrlPressed && keyEvent.key == Key.Zero) {
                        scale = 1.0f
                    }
                    false
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Scroll) {
                                val scrollDelta = event.changes.first().scrollDelta
                                if (canZoom) {
                                    scale *= 1.02f.pow(-scrollDelta.y)
                                    scale = minScale.coerceAtLeast(maxScale.coerceAtMost(scale))
                                    event.changes.first().consume()
                                }
                            }
                        }
                    }
                }
                .combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        focusRequester.requestFocus()
                        model.interact()
                    }
                )
            ) {
                drawIntoCanvas { canvas ->
                    model.mousePosition = Point((pointerPos.x / scale).toInt(), (pointerPos.y / scale).toInt()) // TODO: Works fine but doesn't live-update in the footer

                    canvas.scale(scale, scale, 0f, 0f)

                    canvas.drawImageRect(
                        image = model.mapImage().toComposeImageBitmap(),
                        paint = Paint().apply { filterQuality = FilterQuality.High })
                    canvas.drawImageRect(
                        image = model.textImage().toComposeImageBitmap(),
                        paint = Paint().apply { filterQuality = FilterQuality.High })
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd),
            adapter = rememberScrollbarAdapter(stateVertical)
        )
        HorizontalScrollbar(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(end = 12.dp),
            adapter = rememberScrollbarAdapter(stateHorizontal)
        )
    }
}