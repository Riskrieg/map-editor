package com.riskrieg.editor.ui

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.riskrieg.editor.model.EditorModel
import com.riskrieg.editor.ui.component.FooterView
import com.riskrieg.editor.ui.component.MapView
import com.riskrieg.editor.ui.component.Sidebar

class Editor(private val model: EditorModel) {

    @Composable
    fun build() {
        val themeColors = if (isSystemInDarkTheme()) {
            darkColors()
        } else {
            lightColors()
        }
        DesktopMaterialTheme(colors = themeColors) {
            mainView()
        }
    }

    @Composable
    fun mainView() {
        Column {
            Row(modifier = Modifier.weight(1f)) {
                Sidebar(model, modifier = Modifier.fillMaxHeight().width(180.dp))
                Column(Modifier.weight(1f)) {
                    if (model.editView) {
                        MapView(model, Modifier.fillMaxSize())
                    } else {
                        NewProjectView()
                    }
                }
            }
            FooterView(model)
        }
    }

    @Composable
    fun NewProjectView() {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                "Open a map file (.rkm) or import your map image in order to get started.",
                fontStyle = FontStyle.Italic, textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }


}