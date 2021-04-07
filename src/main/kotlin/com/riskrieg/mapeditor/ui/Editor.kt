package com.riskrieg.mapeditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asDesktopBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.riskrieg.mapeditor.Constants
import org.jetbrains.skija.IRect
import java.awt.Point

class Editor(val mapName: String = "") {

    private lateinit var base: ImageBitmap
    private lateinit var text: ImageBitmap

    private var mousePos = Point(0, 0)

    @Composable
    private fun loadImages() {
        base = imageResource(Constants.MAP_PATH + "$mapName/$mapName-base.png")
        text = imageResource(Constants.MAP_PATH + "$mapName/$mapName-text.png")
    }

    @Composable
    fun init() {
        loadImages()
        Row {
            SideBar()
            MapView()
        }
    }

    @Composable
    private fun SideBar() {
        Box(Modifier.fillMaxHeight().width(80.dp).padding(4.dp)) {
            Surface(color = Color.DarkGray, modifier = Modifier.fillMaxSize()) {

            }
        }
    }

    @Composable
    private fun MapView() {
        Box {
            Canvas(modifier = mapModifier()) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawBitmapRect(base.asDesktopBitmap(), IRect(0, 0, base.width, base.height).toRect())
                    canvas.nativeCanvas.drawBitmapRect(text.asDesktopBitmap(), IRect(0, 0, base.width, base.height).toRect())
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun mapModifier(): Modifier {
        return Modifier.size(base.width.dp, base.height.dp).combinedClickable(
            interactionSource = MutableInteractionSource(),
            indication = null,
            onClick = {
                println("${mousePos.x}, ${mousePos.y}")
            },
            onDoubleClick = {

            },
            onLongClick = {

            }
        ).pointerMoveFilter(onMove = {
            mousePos = Point(it.x.toInt(), it.y.toInt())
            false
        })
    }

}