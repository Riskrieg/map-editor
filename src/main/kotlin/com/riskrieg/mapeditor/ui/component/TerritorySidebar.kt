package com.riskrieg.mapeditor.ui.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riskrieg.map.territory.Territory
import com.riskrieg.mapeditor.Constants
import com.riskrieg.mapeditor.model.EditMode
import com.riskrieg.mapeditor.model.EditorModel
import javax.swing.JOptionPane
import javax.swing.JTextArea

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TerritorySidebar(model: EditorModel, modifier: Modifier) {
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
private fun TerritoryList(list: SnapshotStateList<Territory>, selectedIndex: Int, modifier: Modifier = Modifier, selectAction: (Int) -> Unit) {
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