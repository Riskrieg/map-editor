package com.riskrieg.editor.viewmodel

import androidx.compose.ui.awt.ComposeWindow
import com.riskrieg.core.api.color.ColorPalette
import com.riskrieg.core.api.game.map.GameMap
import com.riskrieg.core.decode.RkmDecoder
import com.riskrieg.core.decode.RkpDecoder
import java.awt.Point

class PaletteViewModel(private val window: ComposeWindow, var mousePosition: Point) {

    /** Methods **/

    fun init(palette: ColorPalette) {

    }

    fun reset() {

    }

    fun save() {

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

}