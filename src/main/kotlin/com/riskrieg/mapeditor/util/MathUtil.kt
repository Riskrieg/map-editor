package com.riskrieg.mapeditor.util

import java.awt.Point
import kotlin.math.sqrt

object MathUtil {

    fun distance(one: Point, two: Point): Double {
        val dx = two.x - one.x
        val dy = two.y - one.y
        return sqrt((dx * dx + dy * dy).toDouble())
    }

}