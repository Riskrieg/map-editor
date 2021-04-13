package com.riskrieg.mapeditor.util

import java.awt.Point
import java.awt.image.BufferedImage
import kotlin.math.sqrt


object ImageUtil {
    /**
     * This function uses a fill algorithm to get the relevant region, and then loops through the region to find the point closest to (0, 0).
     * This is done in order to get a consistent pixel result for each region so that if you click any pixel in the region, the algorithm can tell which region it is.
     */
    fun getRootPixel(image: BufferedImage, point: Point): Point {
        val origin = Point(0, 0)
        var result = Point(point.x, point.y)
        var shortestDistance = distance(point, origin)

        val check = MilazzoCheck(image, point)

        val array = check.array()
        for (x in array.indices) {
            for (y in array[x].indices) {
                if (array[x][y]) {
                    val distanceToOrigin = distance(Point(x, y), origin)
                    if (distanceToOrigin < shortestDistance) {
                        shortestDistance = distanceToOrigin
                        result = Point(x, y)
                    }
                }
            }
        }

        return result
    }

    private fun distance(one: Point, two: Point): Double { // Used in getRootPixel
        val dx = two.x - one.x
        val dy = two.y - one.y
        return sqrt((dx * dx + dy * dy).toDouble())
    }

}