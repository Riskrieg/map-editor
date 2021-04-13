package com.riskrieg.mapeditor.fill

import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import java.util.*

class BasicFill(private var image: BufferedImage, private val original: Color, private val fill: Color) : Fill {

    private val width: Int = image.width
    private val height: Int = image.height

    override fun image(): BufferedImage {
        return image
    }

    override fun fill(seed: Point) {
        val visited = Array(image.height) { BooleanArray(image.width) }
        val queue: Queue<Point> = LinkedList()
        queue.add(Point(seed.x, seed.y))

        while (!queue.isEmpty()) {
            val p = queue.remove()
            if (scan(visited, p.x, p.y)) {
                setPixel(p.x, p.y)
                queue.add(Point(p.x - 1, p.y))
                queue.add(Point(p.x + 1, p.y))
                queue.add(Point(p.x, p.y - 1))
                queue.add(Point(p.x, p.y + 1))
            }
        }
    }

    /* Private Methods */

    private fun scan(visited: Array<BooleanArray>, x: Int, y: Int): Boolean {
        if (x > 0 && y > 0 && x < width && y < height && image.getRGB(x, y) == original.rgb && !visited[y][x]) {
            visited[y][x] = true
            return true
        }
        return false
    }

    private fun setPixel(x: Int, y: Int) {
        image.setRGB(x, y, fill.rgb)
    }

}