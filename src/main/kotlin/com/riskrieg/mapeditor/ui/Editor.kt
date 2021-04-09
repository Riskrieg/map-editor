package com.riskrieg.mapeditor.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asDesktopBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp
import com.riskrieg.mapeditor.Constants
import com.riskrieg.mapeditor.fill.Fill
import com.riskrieg.mapeditor.fill.MilazzoFill
import com.riskrieg.mapeditor.util.ImageUtil
import org.jetbrains.skija.IRect
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class Editor(private val mapName: String = "") {

    private var mousePos = Point(0, 0)

    private lateinit var base: BufferedImage
    private lateinit var text: BufferedImage

    @Composable
    fun init() {
        Row {
            SideBar()
            MapView()
        }
    }

    @Composable
    private fun SideBar() {
        Box(Modifier.fillMaxHeight().width(80.dp).padding(4.dp)) {

        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun MapView() { // TODO: At some point, they will make canvas pointerMoveFilter relative to the canvas instead of the window, so change code here whenever that happens.
        if (mapName.isBlank()) {
            base = BufferedImage(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT, 2)
            text = BufferedImage(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT, 2)
        } else { // Primarily for easy debugging
            base = ImageUtil.convert(ImageIO.read(File("src/main/resources/" + Constants.MAP_PATH + "$mapName/$mapName-base.png")), 2)
            text = ImageUtil.convert(ImageIO.read(File("src/main/resources/" + Constants.MAP_PATH + "$mapName/$mapName-text.png")), 2)
        }

        val stateVertical = rememberScrollState(0)
        val stateHorizontal = rememberScrollState(0)
        var pointerPos: Offset? by remember { mutableStateOf(null) }
        var baseBitmap by remember { mutableStateOf(ImageUtil.toBitmap(base).asImageBitmap().asDesktopBitmap()) }
        var textBitmap = ImageUtil.toBitmap(text).asImageBitmap().asDesktopBitmap()

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(stateVertical)
                    .padding(end = 12.dp, bottom = 12.dp)
                    .horizontalScroll(stateHorizontal)
            ) {
                Canvas(modifier = Modifier.width(base.width.dp).height(base.height.dp)
                    .pointerMoveFilter(
                        onMove = { pointerPos = it; false },
                        onExit = { pointerPos = null; false }
                    ).combinedClickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { // TODO: Code is only here for now to test
                            val root = ImageUtil.getRootPixel(base, mousePos) // Test
                            println("Mouse: (${mousePos.x}, ${mousePos.y}) | Root: (${root.x}, ${root.y})") // Test
                            if (base.getRGB(mousePos.x, mousePos.y) == Constants.TERRITORY_COLOR.rgb) {
                                val fill: Fill = MilazzoFill(base, Constants.TERRITORY_COLOR, Constants.SELECT_COLOR)
                                fill.fill(Point(mousePos.x, mousePos.y))
                                baseBitmap = ImageUtil.toBitmap(base).asImageBitmap().asDesktopBitmap()
                            } else if (base.getRGB(mousePos.x, mousePos.y) == Constants.SELECT_COLOR.rgb) {
                                val fill: Fill = MilazzoFill(base, Constants.SELECT_COLOR, Constants.TERRITORY_COLOR)
                                fill.fill(Point(mousePos.x, mousePos.y))
                                baseBitmap = ImageUtil.toBitmap(base).asImageBitmap().asDesktopBitmap()
                            }
                        }
                    )
                ) {
                    drawIntoCanvas { canvas ->
                        if (pointerPos != null) {
                            val scrollOffset = Offset(stateHorizontal.value.toFloat(), stateVertical.value.toFloat())
                            mousePos = Point(
                                scrollOffset.x.toInt() + pointerPos!!.x.toInt(),
                                scrollOffset.y.toInt() + pointerPos!!.y.toInt()
                            )
                        }
                        canvas.nativeCanvas.drawBitmapRect(baseBitmap, IRect(0, 0, base.width, base.height).toRect())
                        canvas.nativeCanvas.drawBitmapRect(textBitmap, IRect(0, 0, base.width, base.height).toRect())
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

}