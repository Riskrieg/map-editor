package com.riskrieg.mapeditor.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riskrieg.mapeditor.Constants
import com.riskrieg.mapeditor.model.EditMode
import com.riskrieg.mapeditor.model.EditorModel
import com.riskrieg.mapeditor.model.Territory
import org.jetbrains.skija.Bitmap
import org.jetbrains.skija.IRect
import java.awt.Point
import javax.swing.JOptionPane
import javax.swing.JTextArea


class Editor(private val model: EditorModel, private var baseBitmap: MutableState<Bitmap>, private var textBitmap: Bitmap) {

    private var mousePos = Point(0, 0)

    private val submittedTerritories = mutableStateListOf<Territory>() // TODO: Ideally temporary but may not have another workaround

    @Composable
    fun init() {
        lightColors(primary = Color.Blue, primaryVariant = Color.Blue, secondary = Color.Yellow, secondaryVariant = Color.Yellow, surface = Color.Cyan)
        darkColors(primary = Color.Blue, primaryVariant = Color.Blue, secondary = Color.Yellow, secondaryVariant = Color.Yellow, surface = Color.Cyan)
        Row {
            SideBar()
            MapView()
        }
    }

    @Composable
    private fun SideBar() {
        Box(Modifier.fillMaxHeight().width(120.dp).padding(4.dp)) {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
                when (model.editMode.value) {
                    EditMode.NO_EDIT -> {
                    }
                    EditMode.EDIT_TERRITORY -> {
                        Button(modifier = Modifier.fillMaxWidth().height(35.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(Constants.BORDER_COLOR.rgb), contentColor = Color.White),
                            onClick = {
                                val name = JOptionPane.showInputDialog(JTextArea(), "Enter territory name:")
                                val opt = model.submitRegionsAsTerritory(name)
                                if (opt.isPresent) {
                                    submittedTerritories.add(opt.get())
                                }
                                baseBitmap.value = model.update()
                            }) {
                            Text("Add", fontSize = 14.sp)
                        }
                        Button(modifier = Modifier.fillMaxWidth().height(35.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(Constants.BORDER_COLOR.rgb), contentColor = Color.White),
                            onClick = {

                            }) {
                            Text("Remove", fontSize = 14.sp)
                        }
                    }
                    EditMode.EDIT_NEIGHBORS -> {
                        Button(modifier = Modifier.fillMaxWidth().height(35.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(Constants.BORDER_COLOR.rgb), contentColor = Color.White),
                            onClick = {
                                model.submitNeighbors()
                                baseBitmap.value = model.update()
                            }) {
                            Text("Submit", fontSize = 14.sp)
                        }
                    }
                }
                Box(Modifier.fillMaxSize().background(color = Color(Constants.TERRITORY_COLOR.rgb))) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(submittedTerritories) { territory ->
                            Text(modifier = Modifier.fillMaxWidth(), text = territory.name) // TODO: Heavily unfinished and not working properly
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun MapView() { // TODO: At some point, they will make canvas pointerMoveFilter relative to the canvas instead of the window, so change code here whenever that happens.
        val stateVertical = rememberScrollState(0)
        val stateHorizontal = rememberScrollState(0)
        var pointerPos: Offset? by remember { mutableStateOf(null) }

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
                            when (model.editMode.value) {
                                EditMode.NO_EDIT -> {
                                }
                                EditMode.EDIT_TERRITORY -> {
                                    if (!model.isRegionSelected(mousePos)) {
                                        model.selectRegion(mousePos)
                                    } else if (model.isRegionSelected(mousePos)) {
                                        model.deselectRegion(mousePos)
                                    }
                                }
                                EditMode.EDIT_NEIGHBORS -> {
                                    if (model.hasSelection()) {
                                        if (model.isSelected(mousePos)) {
                                            model.deselect()
                                        } else if (model.isNeighbor(mousePos)) {
                                            model.deselectNeighbor(mousePos)
                                        } else {
                                            model.selectNeighbor(mousePos)
                                        }
                                    } else {
                                        model.select(mousePos)
                                    }
                                }
                            }
                            baseBitmap.value = model.update()
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
                        canvas.nativeCanvas.drawBitmapRect(baseBitmap.value, IRect(0, 0, model.width(), model.height()).toRect())
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