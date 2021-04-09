package com.riskrieg.mapeditor

import androidx.compose.desktop.DesktopTheme
import androidx.compose.desktop.Window
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.KeyStroke
import androidx.compose.ui.window.Menu
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuItem
import com.riskrieg.mapeditor.model.EditMode
import com.riskrieg.mapeditor.model.EditorModel
import com.riskrieg.mapeditor.ui.Editor
import java.io.File
import javax.imageio.ImageIO

class Init {

    private val model: EditorModel = EditorModel("north-america")

    fun start() {
        Window(
            title = "${Constants.NAME} Map Editor v2.0.0",
            icon = ImageIO.read(File("src/main/resources/icon/riskrieg-icon.png")),
            size = IntSize(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT),
            menuBar = MyMenuBar()
        ) {
            DesktopTheme {
                Editor(model).init()
            }
        }
    }

    fun MyMenuBar(): MenuBar {
        return MenuBar(
            Menu(
                name = "File",
                MenuItem( // TODO: Figure out a more streamlined way to do these two
                    name = "Open base layer...",
                    shortcut = KeyStroke(Key.O)
                ),
                MenuItem(
                    name = "Open text layer...",
                    shortcut = KeyStroke(Key.L)
                ),
                MenuItem(
                    name = "Save",
                    shortcut = KeyStroke(Key.S)
                ),
                MenuItem(
                    name = "Import...",
                    shortcut = KeyStroke(Key.I)
                ),
                MenuItem(
                    name = "Export...",
                    shortcut = KeyStroke(Key.E)
                )
            ),
            Menu(
                name = "Mode",
                MenuItem(
                    name = "Edit Territories",
                    shortcut = KeyStroke(Key.T),
                    onClick = {
                        model.editMode = EditMode.EDIT_TERRITORY
                    }
                ),
                MenuItem(
                    name = "Edit Neighbors",
                    shortcut = KeyStroke(Key.N),
                    onClick = {
                        model.clearSelectedRegions() // TODO: Have to update baseBitmap somehow
                        model.editMode = EditMode.EDIT_NEIGHBORS
                    }
                )
            )
        )
    }

}