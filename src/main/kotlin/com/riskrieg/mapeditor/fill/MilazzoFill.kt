package com.riskrieg.mapeditor.fill

import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage

class MilazzoFill(private var image: BufferedImage, private val original: Color, private val fill: Color) : Fill {

    private val width: Int = image.width
    private val height: Int = image.height

    override fun image(): BufferedImage {
        return image
    }

    override fun fill(seed: Point) {
        if (original.rgb == fill.rgb) {
            return
        }
        if (canPaint(seed.x, seed.y)) {
            fill_(seed.x, seed.y)
        }
    }

    /**
     * This method moves the "cursor" to the top-left-most position that it can and then proceeds to fill from there.
     * @param x The x coordinate to start at.
     * @param y The y coordinate to start at.
     */
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

    /**
     * This method fills entire rectangular blocks at a time, making for relatively few pixel color tests compared to other methods.
     * @param x The x coordinate to start filling at.
     * @param y The y coordinate to start filling at.
     */
    private fun fillCore(x: Int, y: Int) {
        var thisX = x
        var thisY = y
        var lastRowLength: Int = 0
        do {
            var rowLength: Int = 0
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
        return getPixel(x, y) == original.rgb
    }

    private fun getPixel(x: Int, y: Int): Int {
        return image.getRGB(x, y)
    }

    private fun setPixel(x: Int, y: Int) {
        image.setRGB(x, y, fill.rgb)
    }

}