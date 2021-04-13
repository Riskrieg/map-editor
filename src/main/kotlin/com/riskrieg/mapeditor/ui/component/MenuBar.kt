package com.riskrieg.mapeditor.ui.component

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.window.KeyStroke
import androidx.compose.ui.window.Menu
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuItem
import com.riskrieg.mapeditor.model.EditMode
import com.riskrieg.mapeditor.model.EditorModel

fun MyMenuBar(model: EditorModel): MenuBar {
    return MenuBar(
        Menu(
            name = "File",
            MenuItem(
                name = "New",
                shortcut = KeyStroke(Key.N),
                onClick = {
                    model.reset()
                }
            ),
            MenuItem(
                name = "Open...",
                shortcut = KeyStroke(Key.O),
                onClick = {
                    model.openRkmFile()
                }
            ),
            MenuItem(
                name = "Export...",
                shortcut = KeyStroke(Key.E),
                onClick = {
                    model.exportAsRkm()
                }
            )
        ),
        Menu(
            name = "Edit",
            MenuItem(
                name = "Edit Territories",
                shortcut = KeyStroke(Key.T),
                onClick = {
                    model.deselect()
                    model.editMode = EditMode.EDIT_TERRITORY
                    model.update()
                }
            ),
            MenuItem(
                name = "Edit Neighbors",
                shortcut = KeyStroke(Key.B),
                onClick = {
                    model.clearSelectedRegions()
                    model.editMode = EditMode.EDIT_NEIGHBORS
                    model.update()
                }
            )
        ),
        Menu(
            name = "Import",
            MenuItem(
                name = "Image Layers...",
                shortcut = KeyStroke(Key.L),
                onClick = {
                    model.importMapAsLayers()
                }
            )
        ),
        Menu(
            name = "Debug",
            MenuItem(
                name = "Import Graph...",
                shortcut = KeyStroke(Key.G),
                onClick = {
                    model.importGraphFile()
                }
            ),
            MenuItem(
                name = "Export Graph...",
                shortcut = KeyStroke(Key.R),
                onClick = {
                    model.exportGraphFile()
                }
            )
        ),
        Menu(
            name = "Help",
            MenuItem(
                name = "About",
                onClick = {
                    // TODO: Bring up popup menu
                }
            )
        )
    )
}