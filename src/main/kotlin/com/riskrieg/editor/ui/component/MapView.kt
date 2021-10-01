package com.riskrieg.editor.ui.component

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.riskrieg.editor.model.EditorModel
import java.awt.Point

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MapView(model: EditorModel, mapViewModifier: Modifier) {
    val stateVertical = rememberScrollState(0)
    val stateHorizontal = rememberScrollState(0)

    var pointerPos: Offset by remember { mutableStateOf(Offset(0f, 0f)) }

    val scale: Float by remember { mutableStateOf(1.0f) }

    val focusRequester = remember(::FocusRequester)
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }


    Box(modifier = mapViewModifier.background(color = Color(255, 255, 255))) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(stateVertical)
                .horizontalScroll(stateHorizontal)
        ) {
            Canvas(modifier = Modifier.width((model.mapImage().width * scale).dp)
                .height((model.mapImage().height * scale).dp).align(
                    Alignment.Center
                )
                .focusable(true)
                .focusRequester(focusRequester)
                .focusTarget()
                .pointerMoveFilter(
                    onMove = { pointerPos = it; false },
                    onExit = { pointerPos = Offset(0f, 0f); false }
                )
                .combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        model.interact()
                    }
                )
            ) {
                drawIntoCanvas { canvas ->
                    model.mousePos = Point((pointerPos.x / scale).toInt(), (pointerPos.y / scale).toInt())

                    canvas.scale(scale, scale, 0f, 0f)
                    canvas.drawImageRect(
                        image = model.mapImage().toComposeBitmap(),
                        paint = Paint().apply { filterQuality = FilterQuality.High })
                    canvas.drawImageRect(
                        image = model.textImage().toComposeBitmap(),
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