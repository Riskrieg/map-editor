package com.riskrieg.editor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import com.riskrieg.core.api.Riskrieg
import com.riskrieg.core.api.color.ColorPalette
import com.riskrieg.core.api.color.GameColor
import com.riskrieg.core.api.game.map.GameMap
import com.riskrieg.core.api.game.territory.Claim
import com.riskrieg.core.decode.RkmDecoder
import com.riskrieg.core.decode.RkpDecoder
import com.riskrieg.core.encode.RkpEncoder
import java.awt.Color
import java.awt.Point
import java.io.File
import java.io.FileOutputStream
import java.text.Normalizer
import java.util.*
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

class PaletteViewModel(private val window: ComposeWindow, var mousePosition: Point) {

    private var activeColor by mutableStateOf(GameColor(-1, "None", 0, 0, 0))
    var newColorName by mutableStateOf("")
    var newColorHexString by mutableStateOf("")

    var paletteName by mutableStateOf("")
    var colorSet: TreeSet<GameColor> by mutableStateOf(sortedSetOf())

    private var paletteMap: GameMap = loadDefaultMap()
    private var claims: Set<Claim> by mutableStateOf(mutableSetOf())

    private val paletteNameRegex = Regex("[a-z0-9-]+")

    /** Methods **/

    fun init(palette: ColorPalette) {
        reset()

        paletteName = palette.name
        colorSet = TreeSet(palette.set)
    }

    fun reset() {
        activeColor = GameColor(-1, "None", 0, 0, 0)
        newColorName = ""
        newColorHexString = ""

        paletteName = ""
        colorSet.clear()

        paletteMap = loadDefaultMap()
        claims = mutableSetOf()
    }

    fun save() {
        if (colorSet.isEmpty()) {
            throw IllegalStateException("No colors to export.")
        }
        if (paletteName.isBlank()) {
            throw IllegalStateException("Please name your palette before saving.")
        }
        if (colorSet.size < ColorPalette.MINIMUM_SIZE) {
            throw IllegalStateException("Your palette must have at least ${ColorPalette.MINIMUM_SIZE} colors.")
        }
        if (colorSet.size > ColorPalette.MAXIMUM_SIZE) {
            throw IllegalStateException("Your palette can only have ${ColorPalette.MAXIMUM_SIZE} colors at maximum.")
        }

        val chooser = JFileChooser()
        chooser.dialogTitle = "Save ${Riskrieg.NAME} Palette File"
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("${Riskrieg.NAME} Palette File (*.rkp)", "rkp")
        chooser.currentDirectory = File(System.getProperty("user.home"))

        val normalizedName = Normalizer.normalize(paletteName, Normalizer.Form.NFD).replace("[^\\p{ASCII}]".toRegex(), "")
        val normalizedPaletteName = normalizedName.lowercase().replace("\\s+".toRegex(), "-").replace("[^a-z0-9-]".toRegex(), "")

        chooser.selectedFile = File("$normalizedPaletteName.rkp")
        if (chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
            if (chooser.selectedFile.name.isNullOrBlank() || !chooser.selectedFile.nameWithoutExtension.matches(paletteNameRegex)) {
                throw IllegalStateException("Invalid file name. Use only lowercase letters, numbers, and hyphens/dashes.")
            } else {
                val directory = chooser.currentDirectory.path.replace('\\', '/') + "/"
                try {
                    val palette = ColorPalette(paletteName, colorSet)
                    val encoder = RkpEncoder()
                    val fos = FileOutputStream(File(directory + "${normalizedPaletteName}.rkp"))
                    encoder.encode(palette, fos)
                    fos.close()
                    JOptionPane.showMessageDialog(window, "Palette file successfully exported to the selected directory.", "Success", JOptionPane.PLAIN_MESSAGE)
                } catch (e: Exception) {
                    throw IllegalStateException("Unable to save palette file due to an unexpected error.")
                }
            }
        }
    }

    fun activeColorName(): String {
        return if (isActiveColorSelected()) activeColor.name else ""
    }

    fun activeColorHexString(): String {
        return if (isActiveColorSelected()) String.format("#%02X%02X%02X", activeColor.r, activeColor.g, activeColor.b) else ""
    }

    fun isNewColorNameValid(): Boolean {
        return newColorName.isNotBlank()
    }

    fun isNewColorHexStringValid(): Boolean {
        return try {
            newColorHexString.isNotBlank() && Color.decode(newColorHexString) != null
        } catch (e: Exception) {
            false
        }
    }

    fun selectActiveColor(gameColor: GameColor) {
        this.activeColor = gameColor
    }

    fun deselectActiveColor() {
        this.activeColor = GameColor(-1, "None", 0, 0, 0)
    }

    fun updateSelectedColor() {
        if (isActiveColorSelected() && isNewColorNameValid() && isNewColorHexStringValid()) {
            val updatedColor = GameColor(activeColor.id, newColorName, newColorHexString)
            colorSet.remove(activeColor)
            colorSet.add(updatedColor)

            // hack to update lazylist
            deselectActiveColor()
            selectActiveColor(updatedColor)
        }
    }

    fun colorSetContainsNewColor(): Boolean {
        for (gameColor in colorSet) {
            if (newColorName.trim() == gameColor.name || newColorHexString.trim() == String.format("#%02X%02X%02X", gameColor.r, gameColor.g, gameColor.b)) {
                return true
            }
        }
        return false
    }

    fun addNewColor() {
        if (isNewColorNameValid() && isNewColorHexStringValid()) {
            val newColor = GameColor(colorSet.size, newColorName, newColorHexString)
            colorSet.add(newColor)

        }
    }

    fun removeSelectedColor() {
        if (isActiveColorSelected()) {
            val removedIndex = activeColor.id
            val oldIndexSet: TreeSet<GameColor> = sortedSetOf()
            val reindexedSet: TreeSet<GameColor> = sortedSetOf()
            for (i in removedIndex + 1 until colorSet.size) {
                val currentColor = getGameColorAt(i)
                if (currentColor != null) {
                    oldIndexSet.add(currentColor)
                    val reindexedColor = GameColor(currentColor.id - 1, currentColor.name, currentColor.r, currentColor.g, currentColor.b)
                    reindexedSet.add(reindexedColor)
                }
            }

            colorSet.remove(activeColor)
            colorSet.removeAll(oldIndexSet)
            colorSet.addAll(reindexedSet)

            deselectActiveColor()
        }
    }

    fun moveSelectedColorUp() {
        if (isActiveColorSelected()) {
            val movedUp = GameColor(activeColor.id - 1, activeColor.name, activeColor.r, activeColor.g, activeColor.b)
            val toMove = getGameColorAt(movedUp.id)
            if (toMove != null) {
                val movedDown = GameColor(toMove.id + 1, toMove.name, toMove.r, toMove.g, toMove.b)
                colorSet.remove(toMove)
                colorSet.remove(activeColor)
                colorSet.add(movedUp)
                colorSet.add(movedDown)
                this.activeColor = movedUp
            }
        }
    }

    fun moveSelectedColorDown() {
        if (isActiveColorSelected()) {
            val movedDown = GameColor(activeColor.id + 1, activeColor.name, activeColor.r, activeColor.g, activeColor.b)
            val toMove = getGameColorAt(movedDown.id)
            if (toMove != null) {
                val movedUp = GameColor(toMove.id - 1, toMove.name, toMove.r, toMove.g, toMove.b)
                colorSet.remove(toMove)
                colorSet.remove(activeColor)
                colorSet.add(movedDown)
                colorSet.add(movedUp)
                this.activeColor = movedDown
            }
        }
    }

    fun paletteMap(): GameMap {
        return paletteMap
    }

    fun loadDefaultPalette(): ColorPalette {
        val resource = loadResource("palette/default.rkp")
        return RkpDecoder().decode(resource)
    }

    fun loadDefaultMap(): GameMap {
        val resource = loadResource("map/north-america.rkm")
        return RkmDecoder().decode(resource)
    }

    private fun loadResource(path: String): ByteArray {
        val resource = Thread.currentThread().contextClassLoader.getResource(path)
        requireNotNull(resource) { "Resource $path not found" }
        return resource.readBytes()
    }

    /* Private */

    private fun isActiveColorSelected(): Boolean {
        return this.activeColor != GameColor(-1, "None", 0, 0, 0)
    }

    private fun getGameColorWithId(id: Int): GameColor? {
        for (gameColor in colorSet) {
            if (id == gameColor.id) {
                return gameColor
            }
        }
        return null
    }

    private fun getGameColorAt(index: Int): GameColor? {
        if (index < 0) {
            return null
        } else if (index >= colorSet.size) {
            return null
        }
        return colorSet.toList()[index]
    }

}