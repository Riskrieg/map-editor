package com.riskrieg.mapeditor.ui.component

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.riskrieg.mapeditor.model.EditMode
import com.riskrieg.mapeditor.model.EditorModel
import java.awt.Point

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MapView(model: EditorModel, modifier: Modifier) { // TODO: At some point, they will make canvas pointerMoveFilter relative to the canvas instead of the window, so change code here whenever that happens.
    val stateVertical = rememberScrollState(0)
    val stateHorizontal = rememberScrollState(0)
    var pointerPos: Offset? by remember { mutableStateOf(null) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(stateVertical)
                .padding(end = 12.dp, bottom = 12.dp)
                .horizontalScroll(stateHorizontal)
        ) {
            Canvas(modifier = androidx.compose.ui.Modifier.width(model.width().dp).height(model.height().dp)
                .pointerMoveFilter(
                    onMove = { pointerPos = it; false },
                    onExit = { pointerPos = null; false }
                ).combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        when (model.editMode) {
                            EditMode.NO_EDIT -> {
                            }
                            EditMode.EDIT_TERRITORY -> {
                                if (!model.isRegionSelected(model.mousePos)) {
                                    model.selectRegion(model.mousePos)
                                } else if (model.isRegionSelected(model.mousePos)) {
                                    model.deselectRegion(model.mousePos)
                                }
                            }
                            EditMode.EDIT_NEIGHBORS -> {
                                if (model.hasSelection()) {
                                    if (model.isSelected(model.mousePos)) {
                                        model.deselect()
                                    } else if (model.isNeighbor(model.mousePos)) {
                                        model.deselectNeighbor(model.mousePos)
                                    } else {
                                        model.selectNeighbor(model.mousePos)
                                    }
                                } else {
                                    model.select(model.mousePos)
                                }
                            }
                        }
                        model.update()
                    }
                )
            ) {
                drawIntoCanvas { canvas ->
                    if (pointerPos != null) {
                        val scrollOffset = Offset(stateHorizontal.value.toFloat(), stateVertical.value.toFloat())
                        model.mousePos = Point(
                            scrollOffset.x.toInt() + pointerPos!!.x.toInt(),
                            scrollOffset.y.toInt() + pointerPos!!.y.toInt()
                        )
                    }
                    canvas.drawImageRect(model.base(), paint = Paint().apply { filterQuality = FilterQuality.None })
                    canvas.drawImageRect(model.text(), paint = Paint().apply { filterQuality = FilterQuality.None })
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