package com.riskrieg.editor.view.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.riskrieg.editor.viewmodel.MapViewModel

@Composable
fun MapFooterView(model: MapViewModel, modifier: Modifier) {
    Row(
        modifier = modifier.background(color = Color(240, 240, 240))
    ) {
        Text(
            text = "Left click to select/deselect regions or territories.",
            fontSize = 12.sp,
            textAlign = TextAlign.Start
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Mouse: (${model.mousePosition.x}, ${model.mousePosition.y})  |   Size: ${model.mapImage().width}x${model.mapImage().height}",
            fontSize = 12.sp,
            textAlign = TextAlign.End
        )
    }
}