package com.riskrieg.editor.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.riskrieg.editor.view.map.MapView
import com.riskrieg.editor.view.palette.PaletteView
import com.riskrieg.editor.viewmodel.EditorViewModel
import com.riskrieg.editor.viewmodel.internal.EditorType

class EditorView(val model: EditorViewModel) {

    @Composable
    fun build() {
//        val themeColors = if (isSystemInDarkTheme()) {
//            darkColors()
//        } else {
//            lightColors()
//        }
        MaterialTheme(colors = darkColors()) { // TODO: Support both dark and light themes
            mainView()
        }
    }

    @Composable
    fun mainView() {
        when (model.editorType) {
            EditorType.RKM_MAP -> MapView(model.mapViewModel, Modifier.fillMaxSize())
            EditorType.RKP_PALETTE -> PaletteView(model.paletteViewModel, Modifier.fillMaxSize())
            else -> DefaultEditorView()
        }
    }

    @Composable
    fun DefaultEditorView() {
        Box(
            modifier = Modifier.fillMaxSize().background(color = if (model.isDragAndDropping) Color(0, 0, 0, 0xB4) else ViewConstants.UI_BACKGROUND_DARK)
        ) {
            Text(
                "Drag a map file (.rkm) or palette file (.rkp) into this window in order to get started.\n\nYou can also go to File -> Import to get started.",
                fontStyle = FontStyle.Italic, textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
                color = ViewConstants.UI_TEXT_ON_DARK
            )
        }
    }

}