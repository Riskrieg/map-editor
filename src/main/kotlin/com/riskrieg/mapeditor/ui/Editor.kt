package com.riskrieg.mapeditor.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riskrieg.mapeditor.Constants
import com.riskrieg.mapeditor.model.EditMode
import com.riskrieg.mapeditor.model.EditorModel
import com.riskrieg.mapeditor.model.Territory
import org.jetbrains.skija.IRect
import java.awt.Point
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.JTextArea
import javax.swing.filechooser.FileNameExtensionFilter


class Editor(private val model: EditorModel) {

    private var mousePos by mutableStateOf(Point(0, 0))

    private val submittedTerritories = mutableStateListOf<Territory>() // TODO: Ideally temporary but may not have another workaround

    @Composable
    fun init() {
        MyLayout()
    }

    @Composable
    fun MyLayout() {
        Column {
            Row(modifier = Modifier.weight(1f)) {
                SideBar(Modifier.fillMaxHeight().width(120.dp))
                MapView(Modifier.fillMaxSize())
            }
            Footer()
        }
    }

    @Composable
    private fun Footer() {
        Row(Modifier.fillMaxWidth().height(25.dp).background(color = Color.LightGray).padding(3.dp)) {
            Text(
                text = "Left click to select/deselect regions or territories.",
                fontSize = 12.sp,
                textAlign = TextAlign.Start
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Edit Mode: ${model.editMode.value}  |   Mouse: (${mousePos.x}, ${mousePos.y})  |   Size: ${model.width()}x${model.height()}",
                fontSize = 12.sp,
                textAlign = TextAlign.End
            )
        }
    }

    @Composable
    private fun SideBar(modifier: Modifier) {
        Box(modifier = modifier) {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
                when (model.editMode.value) {
                    EditMode.NO_EDIT -> {
                    }
                    EditMode.EDIT_TERRITORY -> {
                        Button(modifier = Modifier.fillMaxWidth().height(40.dp).absolutePadding(top = 4.dp, left = 2.dp, right = 2.dp, bottom = 0.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(Constants.BORDER_COLOR.rgb), contentColor = Color.White),
                            onClick = {
                                val name = JOptionPane.showInputDialog(JTextArea(), "Enter territory name:")
                                if (name != null && name.isNotBlank()) {
                                    val opt = model.submitRegionsAsTerritory(name)
                                    if (opt.isPresent) {
                                        submittedTerritories.add(opt.get())
                                    }
                                    model.update()
                                }
                            }) {
                            Text("Add", fontSize = 14.sp)
                        }
                        Button(modifier = Modifier.fillMaxWidth().height(40.dp).absolutePadding(top = 0.dp, left = 2.dp, right = 2.dp, bottom = 0.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(Constants.BORDER_COLOR.rgb), contentColor = Color.White),
                            onClick = {

                            }) {
                            Text("Remove", fontSize = 14.sp)
                        }
                    }
                    EditMode.EDIT_NEIGHBORS -> {
                        Button(modifier = Modifier.fillMaxWidth().height(40.dp).absolutePadding(top = 4.dp, left = 2.dp, right = 2.dp, bottom = 0.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(Constants.BORDER_COLOR.rgb), contentColor = Color.White),
                            onClick = {
                                model.submitNeighbors()
                                model.update()
                            }) {
                            Text("Submit", fontSize = 14.sp)
                        }
                    }
                }
                Box(Modifier.fillMaxSize().background(color = Color.White)) {
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
    private fun MapView(modifier: Modifier) { // TODO: At some point, they will make canvas pointerMoveFilter relative to the canvas instead of the window, so change code here whenever that happens.
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
                            model.update()
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
                        canvas.nativeCanvas.drawBitmapRect(model.base(), IRect(0, 0, model.width(), model.height()).toRect())
                        canvas.nativeCanvas.drawBitmapRect(model.text(), IRect(0, 0, model.width(), model.height()).toRect())
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