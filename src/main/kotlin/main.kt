import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.formdev.flatlaf.FlatDarkLaf
import com.riskrieg.editor.core.Constants
import com.riskrieg.editor.view.EditorView
import com.riskrieg.editor.viewmodel.EditorViewModel
import com.riskrieg.editor.viewmodel.internal.EditorType
import java.awt.Desktop
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File
import java.net.URL
import javax.swing.JOptionPane
import kotlin.system.exitProcess

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {

    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    try {
        ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, javaClass.getResourceAsStream("/font/spectral/Spectral-Regular.ttf")))
        ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, javaClass.getResourceAsStream("/font/spectral/Spectral-Medium.ttf")))
        ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, javaClass.getResourceAsStream("/font/spectral/Spectral-MediumItalic.ttf")))
    } catch (e: Exception) {
        e.printStackTrace()
    }

    var themeStr by remember { mutableStateOf("dark") }
    FlatDarkLaf.setup()

    Window(
        onCloseRequest = ::exitApplication,
        title = "${Constants.NAME} Map Editor v${Constants.VERSION}",
        state = rememberWindowState(width = Constants.DEFAULT_WINDOW_WIDTH.dp, height = Constants.DEFAULT_WINDOW_HEIGHT.dp),
        icon = painterResource("icon/icon.png")
    ) {
        window.setLocationRelativeTo(null)
        val editorViewModel by remember { mutableStateOf(EditorViewModel(window)) }
        MenuBar {
            Menu("File", mnemonic = 'F') {
                Item(
                    "New",
                    icon = painterResource("icons/$themeStr/new.svg"),
                    onClick = { editorViewModel.reset() },
                    shortcut = KeyShortcut(Key.N, ctrl = true),
                    enabled = editorViewModel.editorType != EditorType.NONE
                )
                Item(
                    "Open...",
                    icon = painterResource("icons/$themeStr/open.svg"),
                    onClick = { editorViewModel.promptOpenFile() },
                    shortcut = KeyShortcut(Key.O, ctrl = true)
                )
                Menu("Import...", mnemonic = 'I') {
                    Item(
                        "Base Image",
                        icon = painterResource("icons/$themeStr/import_image.svg"),
                        onClick = { editorViewModel.mapViewModel.openBaseImageOnly() },
                        shortcut = KeyShortcut(Key.I, ctrl = true),
                        enabled = editorViewModel.editorType == EditorType.NONE || editorViewModel.editorType == EditorType.RKM_MAP
                    )
                    Item(
                        "Image Layers",
                        icon = painterResource("icons/$themeStr/import_image_layers.svg"),
                        onClick = { editorViewModel.mapViewModel.openImageLayers() },
                        shortcut = KeyShortcut(Key.I, alt = true),
                        enabled = editorViewModel.editorType == EditorType.NONE || editorViewModel.editorType == EditorType.RKM_MAP
                    )
//                    Item( // TODO: Add import palette menu item
//                        "Palette",
//                        icon = painterResource("icons/$themeStr/import_image_layers.svg"),
//                        onClick = { editorViewModel.mapViewModel.openImageLayers() },
//                        shortcut = KeyShortcut(Key.P, alt = true),
//                        enabled = editorViewModel.editorType == EditorType.NONE || editorViewModel.editorType == EditorType.RKM_MAP
//                    )
                }
                Separator()
                Item(
                    "Create Palette",
                    icon = painterResource("icons/$themeStr/palette.svg"),
                    onClick = { editorViewModel.newPalette() },
                    shortcut = KeyShortcut(Key.P, ctrl = true),
                    enabled = editorViewModel.editorType == EditorType.NONE
                )
                Separator()
                Item(
                    "Save...",
                    icon = painterResource("icons/$themeStr/save.svg"),
                    onClick = { editorViewModel.save() },
                    shortcut = KeyShortcut(Key.S, ctrl = true),
                    enabled = editorViewModel.editorType == EditorType.RKM_MAP || editorViewModel.editorType == EditorType.RKP_PALETTE
                )
                Separator()
                Item(
                    "Exit",
                    icon = painterResource("icons/$themeStr/exit.svg"),
                    onClick = { exitProcess(0) },
                    shortcut = KeyShortcut(Key.E, ctrl = true)
                )
            }
            Menu("Edit", mnemonic = 'E') {
                Item(
                    "Add as territory",
                    icon = painterResource("icons/$themeStr/add_as_territory.svg"),
                    onClick = { editorViewModel.mapViewModel.submitSelectedRegions(false) },
                    shortcut = KeyShortcut(Key.F1, ctrl = false),
                    enabled = editorViewModel.editorType == EditorType.RKM_MAP && editorViewModel.mapViewModel.isSelectingRegion
                )
                Item(
                    "Submit selected neighbors",
                    icon = painterResource("icons/$themeStr/submit_selected_neighbors.svg"),
                    onClick = { editorViewModel.mapViewModel.submitSelectedNeighbors() },
                    shortcut = KeyShortcut(Key.F2, ctrl = false),
                    enabled = editorViewModel.editorType == EditorType.RKM_MAP && editorViewModel.mapViewModel.isSelectingTerritory
                )
                Separator()
                Item(
                    "Delete selected territory",
                    icon = painterResource("icons/$themeStr/delete_selected_territory.svg"),
                    onClick = { editorViewModel.mapViewModel.deleteSelectedTerritory() },
                    shortcut = KeyShortcut(Key.Delete, ctrl = false),
                    enabled = editorViewModel.editorType == EditorType.RKM_MAP && editorViewModel.mapViewModel.isSelectingTerritory
                )
                Item(
                    "Delete all",
                    icon = painterResource("icons/$themeStr/delete_all.svg"),
                    onClick = { editorViewModel.mapViewModel.deleteAll() },
                    shortcut = KeyShortcut(Key.Delete, alt = true),
                    enabled = editorViewModel.editorType == EditorType.RKM_MAP
                )
                Separator()
                Item(
                    "Deselect",
                    icon = painterResource("icons/$themeStr/deselect.svg"),
                    onClick = { editorViewModel.mapViewModel.deselectAll() },
                    shortcut = KeyShortcut(Key.D, ctrl = true),
                    enabled = editorViewModel.editorType == EditorType.RKM_MAP && (editorViewModel.mapViewModel.isSelectingRegion || editorViewModel.mapViewModel.isSelectingTerritory)
                )
            }
            Menu("Debug", mnemonic = 'D') {
                Item(
                    "Re-import base image...",
                    icon = painterResource("icons/$themeStr/reimport_base_image.svg"),
                    onClick = { editorViewModel.mapViewModel.reimportBaseImage() },
                    shortcut = KeyShortcut(Key.B, alt = true),
                    enabled = editorViewModel.editorType == EditorType.RKM_MAP
                )
                Item(
                    "Re-import text image...",
                    icon = painterResource("icons/$themeStr/reimport_text_image.svg"),
                    onClick = { editorViewModel.mapViewModel.reimportTextImage() },
                    shortcut = KeyShortcut(Key.T, alt = true),
                    enabled = editorViewModel.editorType == EditorType.RKM_MAP
                )
                Separator()
                Item(
                    "Export text image...",
                    icon = painterResource("icons/$themeStr/export_graph.svg"),
                    onClick = { editorViewModel.mapViewModel.exportTextImage() },
                    shortcut = KeyShortcut(Key.Y, alt = true),
                    enabled = editorViewModel.editorType == EditorType.RKM_MAP // && textImage != null
                )
                Separator()
                Item(
                    "Import graph...",
                    icon = painterResource("icons/$themeStr/import_graph.svg"),
                    onClick = { editorViewModel.mapViewModel.importGraph() },
                    shortcut = KeyShortcut(Key.G, alt = true),
                    enabled = editorViewModel.editorType == EditorType.RKM_MAP
                )
                Item(
                    "Export graph...",
                    icon = painterResource("icons/$themeStr/export_graph.svg"),
                    onClick = { editorViewModel.mapViewModel.exportGraph() },
                    shortcut = KeyShortcut(Key.R, alt = true),
                    enabled = editorViewModel.editorType == EditorType.RKM_MAP // && graph != null
                )
            }
            Menu("Help", mnemonic = 'H') {
                Item(
                    "Discord",
                    icon = painterResource("icons/$themeStr/discord.svg"),
                    onClick = {
                        openLink("https://www.discord.com/invite/weU8jYDbW4")
                    }
                )
                Separator()
                Item(
                    "About",
                    icon = painterResource("icons/$themeStr/about.svg"),
                    onClick = {
                        openLink("https://www.riskrieg.com")
                    }
                )
            }
        }
        EditorView(editorViewModel).build()

        window.contentPane.dropTarget = makeDropTarget(editorViewModel, window)
    }
}

private fun openLink(linkStr: String) {
    try {
        Desktop.getDesktop().browse(URL(linkStr).toURI())
    } catch (e: Exception) {
        // TODO: Open dialog popup?
    }
}

private fun makeDropTarget(model: EditorViewModel, window: ComposeWindow): DropTarget {
    return object : DropTarget() {
        override fun dragEnter(event: DropTargetDragEvent?) {
            if (model.editorType == EditorType.NONE) {
                model.isDragAndDropping = true
            }
        }

        override fun dragExit(dte: DropTargetEvent?) {
            if (model.editorType == EditorType.NONE) {
                model.isDragAndDropping = false
            }
        }

        override fun drop(event: DropTargetDropEvent) {
            try {
                if (model.editorType == EditorType.NONE) {
                    event.acceptDrop(DnDConstants.ACTION_REFERENCE)
                    val droppedFiles = event.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>

                    if (droppedFiles.size == 1) {
                        model.openFile(droppedFiles[0] as File)
                    } else {
                        JOptionPane.showMessageDialog(window, "You can only drag in one file at a time.", "Error", JOptionPane.ERROR_MESSAGE)
                    }
                }
            } catch (e: Exception) {
                model.isDragAndDropping = false
                JOptionPane.showMessageDialog(window, "Error opening file.", "Error", JOptionPane.ERROR_MESSAGE)
                e.printStackTrace()
            }
        }
    }
}