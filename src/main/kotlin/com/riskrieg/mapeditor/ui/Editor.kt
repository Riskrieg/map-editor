package com.riskrieg.mapeditor.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.riskrieg.mapeditor.Constants

class Editor(val mapName: String = "") {

    private lateinit var base: ImageBitmap
    private lateinit var text: ImageBitmap

    @Composable
    private fun loadImages() {
        base = imageResource(Constants.MAP_PATH + "north-america/north-america-base.png")
        text = imageResource(Constants.MAP_PATH + "north-america/north-america-text.png")
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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun MapView() {
        Box(modifier = Modifier.combinedClickable(
            onClick = {

            },
            onDoubleClick = {

            },
            onLongClick = {

            }
        ).indication(interactionSource = MutableInteractionSource() ,indication = null)) {
            Image(bitmap = base, contentDescription = "", modifier = Modifier.size(base.width.dp, base.height.dp))
            Image(bitmap = text, contentDescription = "", modifier = Modifier.size(text.width.dp, text.height.dp))
        }
    }

}