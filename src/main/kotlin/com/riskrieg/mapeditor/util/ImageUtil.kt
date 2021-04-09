package com.riskrieg.mapeditor.util

import org.jetbrains.skija.Bitmap
import org.jetbrains.skija.ColorAlphaType
import org.jetbrains.skija.ImageInfo
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

    fun convert(image: BufferedImage, imageType: Int): BufferedImage {
        val result = BufferedImage(image.width, image.height, imageType)
        val g = result.graphics
        g.drawImage(image, 0, 0, null)
        return result
    }

    fun toBitmap(image: BufferedImage): Bitmap {
        val pixels = getBytes(image);

        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeS32(image.width, image.height, ColorAlphaType.PREMUL))
        bitmap.installPixels(bitmap.imageInfo, pixels, (image.width * 4).toLong())
        return bitmap
    }

    private fun getBytes(image: BufferedImage): ByteArray {
        val width = image.width
        val height = image.height

        val buffer = IntArray(width * height)
        image.getRGB(0, 0, width, height, buffer, 0, width)

        val pixels = ByteArray(width * height * 4)

        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = buffer[y * width + x]
                pixels[index++] = ((pixel and 0xFF)).toByte() // Blue component
                pixels[index++] = (((pixel shr 8) and 0xFF)).toByte() // Green component
                pixels[index++] = (((pixel shr 16) and 0xFF)).toByte() // Red component
                pixels[index++] = (((pixel shr 24) and 0xFF)).toByte() // Alpha component
            }
        }

        return pixels
    }

    private fun distance(one: Point, two: Point): Double { // Used in getRootPixel
        val dx = two.x - one.x
        val dy = two.y - one.y
        return sqrt((dx * dx + dy * dy).toDouble())
    }

}