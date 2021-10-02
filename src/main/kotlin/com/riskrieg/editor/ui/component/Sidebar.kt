package com.riskrieg.editor.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riskrieg.editor.Constants
import com.riskrieg.editor.model.EditorModel

@Composable
fun Sidebar(model: EditorModel, modifier: Modifier) {
    if (model.editView) { // TODO: Fix focus stuff here
        Column(modifier = modifier.background(color = Color(255, 255, 255))) {
            val colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color(Constants.BORDER_COLOR.rgb),
                focusedIndicatorColor = Color(Constants.BORDER_COLOR.rgb),
                backgroundColor = Color(Constants.TERRITORY_COLOR.rgb)
            )
            Spacer(modifier = Modifier.height(2.dp))

            Text("Map Display Name", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
            TextField(
                model.mapDisplayName, colors = colors, onValueChange = {
                    model.mapDisplayName = it
                }, modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text("Author Name", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
            TextField(
                model.mapAuthorName, colors = colors, onValueChange = {
                    model.mapAuthorName = it
                }, modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text("Territory Name", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
            TextField(
                model.newTerritoryName, colors = colors,
                enabled = !model.isSelectingTerritory(),
                onValueChange = {
                    model.newTerritoryName = it
                }, modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Button(modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 10.dp), shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(54, 112, 180), contentColor = Color.White),
                onClick = {
                    if (model.isSelectingTerritory()) {
                        model.submitSelectedNeighbors()
                    } else {
                        model.submitSelectedRegions()
                    }
                }) {
                if (model.isSelectingTerritory()) {
                    Text("Submit", fontSize = 14.sp)
                } else {
                    Text("Add", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            Button(modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 10.dp), shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(190, 54, 54), contentColor = Color.White),
                enabled = model.isSelectingTerritory(),
                onClick = {
                    model.deleteSelectedTerritory()
                }) {
                Text("Delete", fontSize = 14.sp)
            }

        }
    }
}