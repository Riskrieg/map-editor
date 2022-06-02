package com.riskrieg.editor.view.palette

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riskrieg.core.api.color.GameColor
import com.riskrieg.core.api.game.map.GameMap
import com.riskrieg.editor.util.ColorUtil
import com.riskrieg.editor.view.ViewConstants
import com.riskrieg.editor.viewmodel.PaletteViewModel
import java.util.*

@Composable
fun PaletteSidebarView(model: PaletteViewModel, modifier: Modifier) {
    var selected by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(-1) }

    val onClickListItem = { index: Int ->
        if (selected && selectedIndex == index) {
            // Deselect
            selected = false
            selectedIndex = -1
            model.deselectActiveColor()
        } else {
            // Select
            selectedIndex = index
            selected = true
            model.selectActiveColor(model.colorSet.toList()[index])
        }
    }

    val onMoveSelection = { movedUp: Boolean ->
        if (movedUp) {
            model.moveSelectedColorUp()
            selectedIndex -= 1
        } else {
            model.moveSelectedColorDown()
            selectedIndex += 1
        }
    }

    val onDelete = {
        // TODO: Clear map of the deleted color
        model.removeSelectedColor()

        // Deselect
        selected = false
        selectedIndex = -1
        model.deselectActiveColor()
    }

    Box(modifier = modifier.background(color = ViewConstants.UI_BACKGROUND_DARK)) {
        Column {
//            ColorEntryView(modifier = Modifier.weight(1.0F, false).padding(vertical = 4.dp))
            ReorderView(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 0.dp),
                selected = selected,
                index = selectedIndex,
                maxIndex = model.colorSet.size - 1,
                onMoveSelection = onMoveSelection,
                onDelete = onDelete
            )
            SelectableColorListView(
                modifier = Modifier.fillMaxHeight().padding(8.dp),
                set = model.colorSet,
                selected = { i -> selectedIndex == i && selected },
                onClickItem = onClickListItem
            )
        }
    }
}

@Composable
private fun ColorEditorView(modifier: Modifier) {
    Box(modifier = modifier) {
        Row {
            // TextField to enter palette name
            // TextField to enter color name
            // TextField to enter color
        }
    }
}

@Composable
private fun ReorderView(
    modifier: Modifier,
    selected: Boolean,
    index: Int,
    maxIndex: Int,
    onMoveSelection: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Box(modifier = modifier) {
        Column {
            Button(modifier = Modifier.height(40.dp).fillMaxWidth().padding(start = 0.dp, end = 0.dp, top = 2.dp, bottom = 2.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(190, 54, 54),
                    contentColor = Color.White
                ),
                enabled = selected,
                onClick = {
                    onDelete.invoke()
                    // TODO: Remove selected item from the list
                }) {
                Text("Delete", fontSize = 14.sp)
            }
            Row {
                // Button to remove color
                Button(modifier = Modifier.weight(1.0F).padding(start = 0.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(180, 112, 54),
                        contentColor = Color.White
                    ),
                    enabled = selected && index > 0,
                    onClick = {
                        onMoveSelection.invoke(true)
                    }) {
                    Text("Up", fontSize = 14.sp)
                }

                Button(modifier = Modifier.weight(1.0F).padding(start = 2.dp, end = 0.dp, top = 2.dp, bottom = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(180, 112, 54),
                        contentColor = Color.White
                    ),
                    enabled = selected && index < maxIndex,
                    onClick = {
                        onMoveSelection.invoke(false)
                    }) {
                    Text("Down", fontSize = 14.sp)
                }

            }
        }
    }
}

@Composable
private fun SelectableColorListView(
    modifier: Modifier,
    set: SortedSet<GameColor>,
    selected: (Int) -> Boolean,
    onClickItem: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    Box(modifier = modifier.background(color = ViewConstants.UI_BACKGROUND_DARK_ON_DARK, shape = RoundedCornerShape(4.dp))) {
        LazyColumn(Modifier.fillMaxSize().padding(top = 8.dp, bottom = 8.dp, start = 12.dp, end = 16.dp), listState) {
            items(set.size) { i ->
                val gameColor = set.toList()[i]
                SelectableColorListItem(
                    text = gameColor.name,
                    backgroundColor = Color(gameColor.r, gameColor.g, gameColor.b),
                    index = i,
                    selected = selected(i),
                    onClick = onClickItem
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(vertical = 4.dp),
            adapter = rememberScrollbarAdapter(scrollState = listState)
        )
    }

}

@Composable
private fun SelectableColorListItem(text: String = "[ITEM]", backgroundColor: Color, index: Int, selected: Boolean, onClick: (Int) -> Unit) {
    val borderStroke = if (selected) {
        BorderStroke(4.dp, ColorUtil.getTextLightOrDark(backgroundColor))
    } else {
        BorderStroke(0.dp, Color.Transparent)
    }
    Box( // TODO: Border color leaks out the corners
        modifier = Modifier.height(48.dp)
            .fillMaxWidth()
            .background(color = backgroundColor, shape = if (selected) RoundedCornerShape(6.dp) else RoundedCornerShape(4.dp)) // Hacky workaround for color leaking from corners
            .border(border = borderStroke, shape = RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .selectable(selected = selected, onClick = { onClick.invoke(index) }),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp),
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = if (selected) ColorUtil.getTextLightOrDark(backgroundColor) else Color(GameMap.TEXT_COLOR.red, GameMap.TEXT_COLOR.green, GameMap.TEXT_COLOR.blue)
        )
    }
}