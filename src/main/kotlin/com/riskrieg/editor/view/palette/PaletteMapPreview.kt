package com.riskrieg.editor.view.palette

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.mouse.MouseScrollOrientation
import androidx.compose.ui.input.mouse.MouseScrollUnit
import androidx.compose.ui.input.mouse.mouseScrollFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riskrieg.editor.view.ViewConstants
import com.riskrieg.editor.viewmodel.PaletteViewModel
import java.awt.image.BufferedImage

// TODO: Allow users to use selected color to fill in territories (high priority)
// TODO: Allow users to use their own maps in the palette editor (low priority)

@Composable
fun PaletteMapPreview(model: PaletteViewModel, modifier: Modifier) {
    Box(modifier = modifier.background(color = ViewConstants.UI_BACKGROUND_DARK)) {
        val map = model.loadDefaultMap()
        MapViewport(modifier = Modifier.fillMaxSize().padding(20.dp).clip(RoundedCornerShape(4.dp)).clipToBounds(), baseLayer = map.baseLayer, textLayer = map.textLayer)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MapViewport(modifier: Modifier, baseLayer: BufferedImage, textLayer: BufferedImage) {
    val stateVertical = rememberScrollState(0)
    val stateHorizontal = rememberScrollState(0)

    val focusRequester = remember(::FocusRequester)
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(modifier = modifier.background(color = ViewConstants.UI_BACKGROUND_DARK)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(stateVertical)
                .horizontalScroll(stateHorizontal)
        ) {
            Canvas(modifier = Modifier
                .width((baseLayer.width).dp)
                .height((baseLayer.height).dp)
                .align(Alignment.Center)
                .focusable(true)
                .focusRequester(focusRequester)
                .focusTarget()
                .mouseScrollFilter(onMouseScroll = { event, _ -> // TODO: Update mouseScrollFilter to whatever replaces it
                    if (event.orientation == MouseScrollOrientation.Vertical) {
                        val deltaY = when (val delta = event.delta) {
                            is MouseScrollUnit.Line -> -delta.value
                            is MouseScrollUnit.Page -> -delta.value
                        }
                    }
                    false
                }).combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        focusRequester.requestFocus()
                    }
                )
            ) {
                drawIntoCanvas { canvas ->
                    canvas.drawImageRect(
                        image = baseLayer.toComposeImageBitmap(),
                        paint = Paint().apply { filterQuality = FilterQuality.High })
                    canvas.drawImageRect(
                        image = textLayer.toComposeImageBitmap(),
                        paint = Paint().apply { filterQuality = FilterQuality.High })
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd),
            adapter = rememberScrollbarAdapter(stateVertical)
        )
        HorizontalScrollbar(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(end = 12.dp),
            adapter = rememberScrollbarAdapter(stateHorizontal)
        )
    }
}