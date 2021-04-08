package com.riskrieg.mapeditor.fill

import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import java.util.*

class FloodFill(private var image: BufferedImage, private val original: Color, private val fill: Color) : Fill {

    private val width: Int = image.width
    private val height: Int = image.height

    override fun image(): BufferedImage {
        return image
    }

    override fun fill(seed: Point) {
        if (original.rgb == fill.rgb) {
            return
        }

        val queue: Queue<Point> = LinkedList()

        if (getPixel(seed.x, seed.y) == original.rgb) {
            queue.add(seed)

            while (!queue.isEmpty()) {
                val n: Point = queue.poll()
                if (getPixel(n.x, n.y) == original.rgb) {
                    var wx = n.x
                    var ex = n.x + 1

                    while (wx >= 0 && getPixel(wx, n.y) == original.rgb) {
                        wx--
                    }

                    while (ex <= width - 1 && getPixel(ex, n.y) == original.rgb) {
                        ex++
                    }

                    for (ix in wx + 1 until ex) {
                        setPixel(ix, n.y)

                        if (n.y - 1 >= 0 && getPixel(ix, n.y - 1) == original.rgb) {
                            queue.add(Point(ix, n.y - 1))
                        }

                        if (n.y + 1 < height && getPixel(ix, n.y + 1) == original.rgb) {
                            queue.add(Point(ix, n.y + 1))
                        }
                    }

                }

            }

        }
    }

    /* Private Methods */

    private fun getPixel(x: Int, y: Int): Int {
        return image.getRGB(x, y)
    }

    private fun setPixel(x: Int, y: Int) {
        image.setRGB(x, y, fill.rgb)
    }


}