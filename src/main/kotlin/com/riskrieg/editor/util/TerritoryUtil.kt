package com.riskrieg.editor.util

import com.riskrieg.editor.Constants
import com.riskrieg.editor.algorithm.label.GetInnerPointFill
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage

object TerritoryUtil {

    fun createInnerPointMap(seedPoints: MutableSet<Point>, base: BufferedImage): HashMap<Point, HashSet<Point>> { // Get the inner point map
        val innerPointMap = HashMap<Point, HashSet<Point>>()

        val temp = BufferedImage(base.width, base.height, BufferedImage.TYPE_INT_ARGB) // TODO: Change this so image isn't needed
        val g2d = temp.createGraphics()
        g2d.drawImage(base, 0, 0, null)
        g2d.dispose()

        for (seedPoint in seedPoints) {
            val innerPoints = HashSet<Point>()
            val fill = GetInnerPointFill(temp, Color(temp.getRGB(seedPoint.x, seedPoint.y)), Constants.SELECT_COLOR) // TODO: Change this so it doesn't actually need to fill?
            fill.fill(seedPoint)
            innerPoints.addAll(fill.allPoints)
            innerPointMap[seedPoint] = innerPoints
        }

        return innerPointMap
    }

}