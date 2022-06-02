package com.riskrieg.editor.view.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riskrieg.core.api.game.map.GameMap
import com.riskrieg.editor.view.ViewConstants
import com.riskrieg.editor.viewmodel.MapViewModel

@Composable
fun MapSidebarView(model: MapViewModel, modifier: Modifier) {
    Column(
        modifier = modifier.background(color = ViewConstants.UI_BACKGROUND_DARK)
    ) {
        val colors = TextFieldDefaults.textFieldColors(
            cursorColor = Color(GameMap.BORDER_COLOR.rgb),
            focusedIndicatorColor = Color(GameMap.BORDER_COLOR.rgb),
            backgroundColor = Color(GameMap.TERRITORY_COLOR.rgb)
        )
        Spacer(modifier = Modifier.height(2.dp))

        Text("Map Display Name", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), color = ViewConstants.UI_TEXT_ON_DARK)
        TextField(
            model.mapDisplayName,
            colors = colors,
            singleLine = true,
            onValueChange = {
                model.mapDisplayName = it
            }, modifier = Modifier.padding(horizontal = 10.dp)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text("Author Name", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), color = ViewConstants.UI_TEXT_ON_DARK)
        TextField(
            model.mapAuthorName,
            colors = colors,
            singleLine = true,
            onValueChange = {
                model.mapAuthorName = it
            }, modifier = Modifier.padding(horizontal = 10.dp)
        )

        Spacer(modifier = Modifier.height(5.dp))

        AnimatedVisibility(visible = model.isSelectingTerritory || model.isSelectingRegion) {
            Column {
                Spacer(modifier = Modifier.height(5.dp))
                Divider(modifier = Modifier.padding(horizontal = 10.dp), color = Color.LightGray, thickness = 2.dp)
                Spacer(modifier = Modifier.height(5.dp))

                Text("Territory Name", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), color = ViewConstants.UI_TEXT_ON_DARK)
                TextField(
                    model.newTerritoryName,
                    colors = colors,
                    singleLine = true,
                    enabled = !model.isSelectingTerritory,
                    onValueChange = {
                        model.newTerritoryName = it.replace("[^\\p{IsAlphabetic}\\p{IsDigit}]".toRegex(), "").toUpperCase(Locale.current)
                    }, modifier = Modifier.padding(horizontal = 10.dp)
                )

                Spacer(modifier = Modifier.height(5.dp))

                Button(modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 10.dp), shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(54, 112, 180), contentColor = Color.White),
                    onClick = {
                        if (model.isSelectingTerritory) {
                            model.submitSelectedNeighbors()
                        } else if (model.isSelectingRegion) {
                            model.submitSelectedRegions(false)
                        }
                    }) {
                    if (model.isSelectingTerritory) {
                        Text("Submit", fontSize = 14.sp)
                    } else {
                        Text("Add", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                if (model.isSelectingRegion && !model.selectedRegionsHaveLabel) {
                    Button(modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 10.dp), shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(180, 112, 54), contentColor = Color.White),
                        enabled = !model.selectedRegionsHaveLabel,
                        onClick = {
                            model.submitSelectedRegions(true)
                        }) {
                        Text("Add & Label", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                }

                if (model.isSelectingTerritory) {
                    Button(modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 10.dp), shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(190, 98, 54), contentColor = Color.White),
                        enabled = model.isSelectingTerritory,
                        onClick = {
                            if (model.selectedTerritoryHasLabel) model.clearTerritoryLabel() else model.addTerritoryLabel()
                        }) {
                        Text(if (model.selectedTerritoryHasLabel) "Clear Label" else "Add Label", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Button(modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 10.dp), shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(190, 54, 54), contentColor = Color.White),
                        enabled = model.isSelectingTerritory,
                        onClick = {
                            model.deleteSelectedTerritory()
                        }) {
                        Text("Delete", fontSize = 14.sp)
                    }
                }

            }
        }

    }
}