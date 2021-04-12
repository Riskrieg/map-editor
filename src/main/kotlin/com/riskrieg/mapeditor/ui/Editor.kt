package com.riskrieg.mapeditor.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riskrieg.map.territory.Territory
import com.riskrieg.mapeditor.Constants
import com.riskrieg.mapeditor.model.EditMode
import com.riskrieg.mapeditor.model.EditorModel
import java.awt.Point
import javax.swing.JOptionPane
import javax.swing.JTextArea


class Editor(private val model: EditorModel) {

    private var mousePos by mutableStateOf(Point(0, 0))

    @Composable
    fun init() {
        MyLayout()
    }

    @Composable
    fun MyLayout() {
        Column {
            Row(modifier = Modifier.weight(1f)) {
                SideBar(Modifier.fillMaxHeight().width(120.dp))
                Column(Modifier.weight(1f)) {
                    MapView(Modifier.fillMaxSize())
                }
                if (model.editMode != EditMode.NO_EDIT) {
                    MetadataEditor(Modifier.fillMaxHeight().width(180.dp))
                }
            }
            Footer()
        }
    }

    @Composable
    fun MetadataEditor(modifier: Modifier) {
        Column(modifier = modifier.padding(5.dp)) {
            val colors = TextFieldDefaults.textFieldColors(cursorColor = Color(Constants.BORDER_COLOR.rgb), focusedIndicatorColor = Color(Constants.BORDER_COLOR.rgb))
            Spacer(modifier = Modifier.height(2.dp))
            Text("Map Code Name")
            TextField(model.mapCodeName, colors = colors, onValueChange = {
                model.mapCodeName = it
            })
            Spacer(modifier = Modifier.height(2.dp))
            Text("Map Display Name")
            Spacer(modifier = Modifier.height(5.dp))
            TextField(model.mapDisplayName, colors = colors, onValueChange = {
                model.mapDisplayName = it
            })
            Spacer(modifier = Modifier.height(2.dp))
            Text("Map Author Name")
            Spacer(modifier = Modifier.height(5.dp))
            TextField(model.mapAuthorName, colors = colors, onValueChange = {
                model.mapAuthorName = it
            })
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun SideBar(modifier: Modifier) {
        var selectedTerritory: Territory? by remember { mutableStateOf(null) }
        var selectedItemIndex by remember { mutableStateOf(-1) }
        Box(modifier = modifier) {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
                when (model.editMode) {
                    EditMode.NO_EDIT -> {
                    }
                    EditMode.EDIT_TERRITORY -> {
                        Button(modifier = Modifier.fillMaxWidth().height(40.dp).absolutePadding(top = 4.dp, left = 2.dp, right = 2.dp, bottom = 0.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(Constants.BORDER_COLOR.rgb), contentColor = Color.White),
                            onClick = {
                                val name = JOptionPane.showInputDialog(JTextArea(), "Enter territory name: ")
                                if (name != null && name.isNotBlank()) {
                                    val opt = model.submitRegionsAsTerritory(name)
                                    if (opt.isPresent) {
                                        selectedTerritory = opt.get()
                                    }
                                    model.update()
                                }
                            }) {
                            Text("Add", fontSize = 14.sp)
                        }
                        Button(modifier = Modifier.fillMaxWidth().height(40.dp).absolutePadding(top = 0.dp, left = 2.dp, right = 2.dp, bottom = 0.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(Constants.BORDER_COLOR.rgb), contentColor = Color.White),
                            onClick = {
                                if (selectedTerritory != null) {
                                    model.removeSubmitted(selectedTerritory!!)
                                    selectedItemIndex = -1
                                }
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
                TerritoryList(model.getSubmittedTerritories(), selectedItemIndex, selectAction = { index ->
                    if (model.editMode == EditMode.EDIT_TERRITORY) {
                        if (index == selectedItemIndex) {
                            selectedItemIndex = -1
                            selectedTerritory = null
                        } else {
                            selectedItemIndex = index
                            selectedTerritory = model.getSubmittedTerritories()[index]
                        }
                    }
                })
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun TerritoryList(list: SnapshotStateList<Territory>, selectedIndex: Int, modifier: Modifier = Modifier, selectAction: (Int) -> Unit) {
        Box(Modifier.fillMaxSize().background(color = Color.White)) {
            val state = rememberLazyListState()
            LazyColumn(modifier = Modifier.fillMaxSize().padding(end = 12.dp), state) {
                items(list.size) { i ->
                    if (list.isNotEmpty()) {
                        Box(
                            modifier = Modifier.height(32.dp).fillMaxWidth()
                                .background(color = if (selectedIndex == i) Color(127, 187, 235, 220) else Color(240, 240, 240))
                                .clickable { selectAction(i) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (i != list.size) {
                                Text(list[i].name())
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = state,
                    itemCount = list.size,
                    averageItemSize = 37.dp
                )
            )

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
                            when (model.editMode) {
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

    @Composable
    private fun Footer() {
        Row(Modifier.fillMaxWidth().height(25.dp).background(color = Color(240, 240, 240)).padding(3.dp)) {
            Text(
                text = "Left click to select/deselect regions or territories.",
                fontSize = 12.sp,
                textAlign = TextAlign.Start
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Edit Mode: ${model.editMode}  |   Mouse: (${mousePos.x}, ${mousePos.y})  |   Size: ${model.width()}x${model.height()}",
                fontSize = 12.sp,
                textAlign = TextAlign.End
            )
        }
    }

}