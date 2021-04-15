package com.riskrieg.mapeditor.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.riskrieg.mapeditor.model.EditMode
import com.riskrieg.mapeditor.model.EditorModel
import com.riskrieg.mapeditor.ui.component.Footer
import com.riskrieg.mapeditor.ui.component.MapView
import com.riskrieg.mapeditor.ui.component.MetadataEditor
import com.riskrieg.mapeditor.ui.component.TerritorySidebar


class Editor(private val model: EditorModel) {

    @Composable
    fun init() {
        MyLayout()
    }

    @Composable
    fun MyLayout() {
        Column {
            Row(modifier = Modifier.weight(1f)) {
                TerritorySidebar(model = model, modifier = Modifier.fillMaxHeight().width(120.dp))
                Column(Modifier.weight(1f)) {
                    if (model.editMode == EditMode.NO_EDIT) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                "Open a map file (.rkm) or import your map images in order to get started.",
                                fontStyle = FontStyle.Italic, textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        MapView(model = model, modifier = Modifier.fillMaxSize())
                    }
                }
                if (model.editMode != EditMode.NO_EDIT) {
                    MetadataEditor(model = model, modifier = Modifier.fillMaxHeight().width(180.dp))
                }
            }
            Footer(model = model)
        }
    }

}