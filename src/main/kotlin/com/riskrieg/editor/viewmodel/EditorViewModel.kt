package com.riskrieg.editor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import com.riskrieg.core.api.Riskrieg
import com.riskrieg.core.decode.RkmDecoder
import com.riskrieg.core.decode.RkpDecoder
import com.riskrieg.editor.viewmodel.internal.EditorType
import java.awt.Point
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

class EditorViewModel(private val window: ComposeWindow) {

    var mousePosition by mutableStateOf(Point(0, 0))

    val mapViewModel by mutableStateOf(MapViewModel(window, mousePosition))
    val paletteViewModel by mutableStateOf(PaletteViewModel(window, mousePosition))

    var editorType: EditorType by mutableStateOf(EditorType.NONE)

    var isDragAndDropping by mutableStateOf(false)

    fun reset() { // Reset all data
        editorType = EditorType.NONE
        isDragAndDropping = false
        mapViewModel.reset()
        paletteViewModel.reset()
    }

    /* Menu Functions */

    fun promptOpenFile() {
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("${Riskrieg.NAME} Map (*.rkm) or Palette (*.rkp)", "rkm", "rkp")
        chooser.currentDirectory = File(System.getProperty("user.home"))
        if (chooser.showDialog(window, "Open") == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.selectedFile)
        }
    }

    fun openFile(file: File) {
        isDragAndDropping = false
        try {
            when (file.extension) {
                "rkm" -> {
                    val map = RkmDecoder().decode(file.toPath())
                    reset()
                    mapViewModel.init(map)
                    editorType = EditorType.RKM_MAP
                }
                "rkp" -> {
                    val palette = RkpDecoder().decode(file.toPath())
                    reset()
                    paletteViewModel.init(palette)
                    editorType = EditorType.RKP_PALETTE
                }
                else -> {
                    reset()
                    JOptionPane.showMessageDialog(window, "Unsupported file type", "Error", JOptionPane.ERROR_MESSAGE)
                    return
                }
            }
        } catch (e: Exception) {
            reset()
            when (file.extension) {
                "rkm" -> {
                    if (e.message != null && e.message!!.contains("invalid checksum", true)) {
                        JOptionPane.showMessageDialog(window, "Could not open .rkm map file: invalid checksum.", "Error", JOptionPane.ERROR_MESSAGE)
                    } else {
                        JOptionPane.showMessageDialog(window, "Invalid .rkm map file.", "Error", JOptionPane.ERROR_MESSAGE)
                    }
                }
                "rkp" -> {
                    JOptionPane.showMessageDialog(window, "Invalid .rkp palette file.", "Error", JOptionPane.ERROR_MESSAGE)
                }
                else -> {
                    JOptionPane.showMessageDialog(window, "Invalid file.", "Error", JOptionPane.ERROR_MESSAGE)
                }
            }
            return
        }
    }

    fun save() {
        try {
            when (editorType) {
                EditorType.RKM_MAP -> {
                    mapViewModel.save()
                }
                EditorType.RKP_PALETTE -> {
                    paletteViewModel.save()
                }
                else -> {
                    JOptionPane.showMessageDialog(window, "Nothing to save", "Error", JOptionPane.ERROR_MESSAGE)
                    return
                }
            }
        } catch (e: Exception) {
            when (editorType) {
                EditorType.RKM_MAP -> {
                    JOptionPane.showMessageDialog(window, "Error saving (.rkm): ${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
                }
                EditorType.RKP_PALETTE -> {
                    JOptionPane.showMessageDialog(window, "Error saving (.rkp): ${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
                }
                else -> {
                    JOptionPane.showMessageDialog(window, "Error saving file: ${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
                }
            }
        }
    }

    /* Private methods */

}