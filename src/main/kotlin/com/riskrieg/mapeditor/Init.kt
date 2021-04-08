package com.riskrieg.mapeditor

import androidx.compose.desktop.DesktopTheme
import androidx.compose.desktop.Window
import androidx.compose.ui.unit.IntSize
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
                Editor("north-america").init()
            }
        }
    }

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