package com.riskrieg.mapeditor.fill

import java.awt.Point
import java.awt.image.BufferedImage

interface Fill {

    fun image(): BufferedImage

    fun fill(seed: Point)

}