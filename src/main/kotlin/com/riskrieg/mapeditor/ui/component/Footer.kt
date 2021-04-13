package com.riskrieg.mapeditor.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riskrieg.mapeditor.model.EditorModel

@Composable
fun Footer(model: EditorModel) {
    Row(Modifier.fillMaxWidth().height(25.dp).background(color = Color(240, 240, 240)).padding(3.dp)) {
        Text(
            text = "Left click to select/deselect regions or territories.",
            fontSize = 12.sp,
            textAlign = TextAlign.Start
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Edit Mode: ${model.editMode}  |   Mouse: (${model.mousePos.x}, ${model.mousePos.y})  |   Size: ${model.width()}x${model.height()}",
            fontSize = 12.sp,
            textAlign = TextAlign.End
        )
    }
}