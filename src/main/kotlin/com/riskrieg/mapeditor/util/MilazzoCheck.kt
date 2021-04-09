package com.riskrieg.mapeditor.util

import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage

/**
 * The purpose of this class is to use the Milazzo bucket fill algorithm to return a 2D boolean array of all pixels that are part of the region.
 * The (x, y) coordinate in the array is set to "true" if it is a pixel that is part of the fill region.
 */
class MilazzoCheck(private var image: BufferedImage, private val start: Point) {

    private val width: Int = image.width
    private val height: Int = image.height
    private val visited = Array(image.width) { BooleanArray(image.height) }
    private val original: Color = Color(image.getRGB(start.x, start.y))
    private val fill: Color = Color.PINK // Doesn't matter as long as it's not the territory color

    fun array(): Array<BooleanArray> {
        fill(start)
        return visited
    }

    private fun fill(seed: Point) {
        if (original.rgb == fill.rgb) {
            return
        }
        if (canPaint(seed.x, seed.y)) {
            fill_(seed.x, seed.y)
        }
    }

    private fun fill_(x: Int, y: Int) {
        var thisX = x
        var thisY = y
        while (true) {
            val ox: Int = thisX
            val oy: Int = thisY
            while (thisY != 0 && canPaint(thisX, thisY - 1)) thisY--
            while (thisX != 0 && canPaint(thisX - 1, thisY)) thisX--
            if (thisX == ox && thisY == oy) break
        }
        fillCore(thisX, thisY)
    }

    private fun fillCore(x: Int, y: Int) {
        var thisX = x
        var thisY = y
        var lastRowLength = 0
        do {
            var rowLength = 0
            var sx: Int = thisX
            if (lastRowLength != 0 && !canPaint(thisX, thisY)) {
                do {
                    if (--lastRowLength == 0) {
                        return
                    }
                } while (!canPaint(++thisX, thisY))
                sx = thisX
            } else {
                while (thisX != 0 && canPaint(thisX - 1, thisY)) {
                    setPixel(--thisX, thisY)
                    if (thisY != 0 && canPaint(thisX, thisY - 1)) {
                        fill_(thisX, thisY - 1)
                    }
                    rowLength++
                    lastRowLength++
                }
            }

            while (sx < width && canPaint(sx, thisY)) {
                setPixel(sx, thisY)
                rowLength++
                sx++
            }

            if (rowLength < lastRowLength) {
                val end = thisX + lastRowLength
                while (++sx < end) {
                    if (canPaint(sx, thisY)) {
                        fillCore(sx, thisY)
                    }
                }
            } else if (rowLength > lastRowLength && thisY != 0) {
                var ux = thisX + lastRowLength
                while (++ux < sx) {
                    if (canPaint(ux, thisY - 1)) {
                        fill_(ux, thisY - 1)
                    }
                }
            }
            lastRowLength = rowLength
        } while (lastRowLength != 0 && ++thisY < height)
    }

    /* Private Methods */

    private fun canPaint(x: Int, y: Int): Boolean {
        return !visited[x][y] && getPixel(x, y) == original.rgb
    }

    private fun getPixel(x: Int, y: Int): Int {
        return image.getRGB(x, y)
    }

    private fun setPixel(x: Int, y: Int) {
        visited[x][y] = true
    }

}