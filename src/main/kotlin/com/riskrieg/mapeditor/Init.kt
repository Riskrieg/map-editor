package com.riskrieg.mapeditor

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.DesktopTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Menu
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuItem
import com.riskrieg.mapeditor.ui.Editor

class Init {

    fun start() {
        Window(
            title = "Riskrieg Map Editor v2.0",
            size = IntSize(1280, 720),
            menuBar = MyMenuBar()
        ) {
            DesktopTheme {
                Editor().init()
            }
        }
    }

//    @Composable
//    fun MapView() {
//        val base: ImageBitmap = imageResource(Constants.MAP_PATH + "north-america/north-america-base.png")
//        val text: ImageBitmap = imageResource(Constants.MAP_PATH + "north-america/north-america-text.png")
//        Image(bitmap = base, contentDescription = "", modifier = Modifier.size(base.width.dp, base.height.dp))
//        Image(bitmap = text, contentDescription = "", modifier = Modifier.size(text.width.dp, text.height.dp))
//    }

    fun MyMenuBar(): MenuBar {
        return MenuBar(
            Menu(
                name = "File",
                MenuItem(
                    name = "Test"
                ),
                MenuItem(
                    name = "Test2"
                )
            )
        )
    }

}