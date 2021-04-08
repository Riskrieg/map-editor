package com.riskrieg.mapeditor.util

import org.jetbrains.skija.Bitmap
import org.jetbrains.skija.ColorAlphaType
import org.jetbrains.skija.ImageInfo
import java.awt.image.BufferedImage


object ImageUtil {
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

}