package com.riskrieg.mapeditor.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.riskrieg.mapeditor.Constants
import com.riskrieg.mapeditor.model.EditorModel

@Composable
fun MetadataEditor(model: EditorModel, modifier: Modifier) {
    Column(modifier = modifier.padding(5.dp)) {
        val colors = TextFieldDefaults.textFieldColors(cursorColor = Color(Constants.BORDER_COLOR.rgb), focusedIndicatorColor = Color(Constants.BORDER_COLOR.rgb))
        Spacer(modifier = Modifier.height(2.dp))
        Text("Map Simple Name")
        TextField(model.mapSimpleName, colors = colors, onValueChange = {
            model.mapSimpleName = it
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