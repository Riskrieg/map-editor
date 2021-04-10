package com.riskrieg.mapeditor

import androidx.compose.desktop.DesktopMaterialTheme
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
import javax.imageio.ImageIO
import javax.swing.JOptionPane

class Init {

    private val model: EditorModel = EditorModel("north-america")

    fun start() {
        Window(
            title = "${Constants.NAME} Map Editor v2.0.0-ALPHA-1",
            icon = ImageIO.read(Init::class.java.classLoader.getResourceAsStream("icon/icon.png")),
            size = IntSize(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT),
            menuBar = MyMenuBar()
        ) {
            DesktopMaterialTheme {
                Editor(model).init()
            }
        }
    }

    fun MyMenuBar(): MenuBar {
        return MenuBar(
            Menu(
                name = "File",
                MenuItem(
                    name = "Import...",
                    shortcut = KeyStroke(Key.I),
                    onClick = {
                        JOptionPane.showMessageDialog(null, "This isn't implemented quite yet!")
//                        model.importMapFile()
                    }
                ),
                MenuItem(
                    name = "Import as Layers...",
                    shortcut = KeyStroke(Key.O),
                    onClick = {
                        model.importMapLayers()
                    }
                ),
                MenuItem(
                    name = "Export...",
                    shortcut = KeyStroke(Key.E),
                    onClick = {
                        JOptionPane.showMessageDialog(null, "This isn't implemented quite yet!")
                    }
                )
            ),
            Menu(
                name = "Mode",
                MenuItem(
                    name = "Edit Territories",
                    shortcut = KeyStroke(Key.T),
                    onClick = {
                        model.deselect()
                        model.editMode.value = EditMode.EDIT_TERRITORY
                        model.update()
                    }
                ),
                MenuItem(
                    name = "Edit Neighbors",
                    shortcut = KeyStroke(Key.N),
                    onClick = {
                        model.clearSelectedRegions() // TODO: Have to update baseBitmap somehow
                        model.editMode.value = EditMode.EDIT_NEIGHBORS
                        model.update()
                    }
                )
            )
        )
    }

}