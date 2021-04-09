package com.riskrieg.mapeditor.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.riskrieg.mapeditor.model.EditMode
import com.riskrieg.mapeditor.model.EditorModel
import org.jetbrains.skija.Bitmap
import org.jetbrains.skija.IRect
import java.awt.Point

class Editor(private val model: EditorModel) {

    private var mousePos = Point(0, 0)

    @Composable
    fun init() {
        Row {
            SideBar()
            MapView()
        }
    }

    @Composable
    private fun SideBar() {
        Box(Modifier.fillMaxHeight().width(80.dp).padding(4.dp)) {

        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun MapView() { // TODO: At some point, they will make canvas pointerMoveFilter relative to the canvas instead of the window, so change code here whenever that happens.
        val stateVertical = rememberScrollState(0)
        val stateHorizontal = rememberScrollState(0)
        var pointerPos: Offset? by remember { mutableStateOf(null) }

        var baseBitmap by remember { mutableStateOf(Bitmap()) }
        baseBitmap = model.base()
        val textBitmap = model.text()

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(stateVertical)
                    .padding(end = 12.dp, bottom = 12.dp)
                    .horizontalScroll(stateHorizontal)
            ) {
                Canvas(modifier = Modifier.width(model.width().dp).height(model.height().dp)
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
                                    if (!model.isRegionSelected(mousePos)) {
                                        model.selectRegion(mousePos)
                                        baseBitmap = model.update()
                                    } else if (model.isRegionSelected(mousePos)) {
                                        model.deselectRegion(mousePos)
                                        baseBitmap = model.update()
                                    }
                                }
                                EditMode.EDIT_NEIGHBORS -> {
                                }
                            }
                        }
                    )
                ) {
                    drawIntoCanvas { canvas ->
                        if (pointerPos != null) {
                            val scrollOffset = Offset(stateHorizontal.value.toFloat(), stateVertical.value.toFloat())
                            mousePos = Point(
                                scrollOffset.x.toInt() + pointerPos!!.x.toInt(),
                                scrollOffset.y.toInt() + pointerPos!!.y.toInt()
                            )
                        }
                        canvas.nativeCanvas.drawBitmapRect(baseBitmap, IRect(0, 0, model.width(), model.height()).toRect())
                        canvas.nativeCanvas.drawBitmapRect(textBitmap, IRect(0, 0, model.width(), model.height()).toRect())
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

}