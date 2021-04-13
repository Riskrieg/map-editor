package com.riskrieg.mapeditor.util

import org.jetbrains.skija.Bitmap
import org.jetbrains.skija.ColorAlphaType
import org.jetbrains.skija.ImageInfo
import java.awt.image.BufferedImage

object Extensions {

    fun BufferedImage.convert(imageType: Int): BufferedImage {
        val result = BufferedImage(this.width, this.height, imageType)
        val g = result.graphics
        g.drawImage(this, 0, 0, null)
        g.dispose()
        return result
    }

    fun BufferedImage.toBitmap(): Bitmap {
        val pixels = getBytes(this)

        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeS32(this.width, this.height, ColorAlphaType.PREMUL))
        bitmap.installPixels(bitmap.imageInfo, pixels, (this.width * 4).toLong())

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