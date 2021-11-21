package com.riskrieg.editor.core.algorithm.fill

import java.awt.Point
import java.awt.image.BufferedImage

interface Fill {

    fun image(): BufferedImage

    fun fill(seed: Point)

}