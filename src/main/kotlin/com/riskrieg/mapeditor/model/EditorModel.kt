package com.riskrieg.mapeditor.model

import androidx.compose.ui.graphics.asDesktopBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.riskrieg.mapeditor.Constants
import com.riskrieg.mapeditor.fill.MilazzoFill
import com.riskrieg.mapeditor.util.ImageUtil
import org.jetbrains.skija.Bitmap
import org.jgrapht.Graph
import org.jgrapht.Graphs
import org.jgrapht.graph.SimpleGraph
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO


class EditorModel(mapName: String = "") {

    private val graph: Graph<Territory, Border>

    private var base: BufferedImage
    private var text: BufferedImage

    init {
        graph = SimpleGraph(Border::class.java)
        if (mapName.isBlank()) {
            base = BufferedImage(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT, 2)
            text = BufferedImage(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT, 2)
        } else { // Primarily for easy debugging
            base = ImageUtil.convert(ImageIO.read(File("src/main/resources/" + Constants.MAP_PATH + "$mapName/$mapName-base.png")), 2)
            text = ImageUtil.convert(ImageIO.read(File("src/main/resources/" + Constants.MAP_PATH + "$mapName/$mapName-text.png")), 2)
        }
    }

    var editMode: EditMode = EditMode.NO_EDIT

    /* Basic Functions */
    fun base(): Bitmap {
        return ImageUtil.toBitmap(base).asImageBitmap().asDesktopBitmap()
    }

    fun text(): Bitmap {
        return ImageUtil.toBitmap(text).asImageBitmap().asDesktopBitmap()
    }

    fun width(): Int {
        return base.width
    }

    fun height(): Int {
        return base.height
    }

    fun update(): Bitmap {
        val copy = BufferedImage(base.width, base.height, BufferedImage.TYPE_INT_ARGB)
        val g2d = copy.createGraphics()
        g2d.drawImage(base, 0, 0, null)
        g2d.dispose()

        for (territory in graph.vertexSet()) {
            if (Graphs.neighborSetOf(graph, territory).isEmpty()) {
                for (point in territory.seedPoints) {
                    MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.SUBMITTED_COLOR).fill(point)
                }
            } else {
                for (point in territory.seedPoints) {
                    MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.FINISHED_COLOR).fill(point)
                }
            }
        }
        if (selectedRegions.isNotEmpty()) {
            for (point in selectedRegions) {
                MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.SELECT_COLOR).fill(point)
            }
        } else if (selected != noTerritorySelected) {
            for (point in selected.seedPoints) {
                MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.SELECT_COLOR).fill(point)
            }
        }
        if (neighbors.isNotEmpty()) {
            for (territory in neighbors) {
                for (point in territory.seedPoints) {
                    MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.NEIGHBOR_SELECT_COLOR).fill(point)
                }
            }
        }
        return ImageUtil.toBitmap(copy).asImageBitmap().asDesktopBitmap()
    }

    /* EditMode.EDIT_TERRITORY */
    private val selectedRegions: Deque<Point> = ArrayDeque()

    fun isRegionSelected(point: Point): Boolean {
        val root = ImageUtil.getRootPixel(base, point)
        return selectedRegions.contains(root)
    }

    fun getSelectedRegions(): Deque<Point> {
        return selectedRegions
    }

    fun clearSelectedRegions() {
        selectedRegions.clear()
    }

    fun selectRegion(point: Point) {
        val root = ImageUtil.getRootPixel(base, point)
        if (base.getRGB(root.x, root.y) == Constants.TERRITORY_COLOR.rgb) {
            selectedRegions.add(root)
        }
    }

    fun deselectRegion(point: Point) {
        val root = ImageUtil.getRootPixel(base, point)
        selectedRegions.remove(root)
    }

    fun submitTerritoryFromRegions(name: String) {
        if (selectedRegions.isNotEmpty()) {
            graph.addVertex(Territory(name, selectedRegions.toSet()))
        }
    }

    /* EditMode.EDIT_NEIGHBORS */
    private val noTerritorySelected: Territory = Territory("UNSELECTED", Point(-1, -1))
    private var selected: Territory = noTerritorySelected
    private val neighbors: MutableSet<Territory> = HashSet()

//    fun isSelected(point: Point): Boolean { // Might not need this but it's here for now just in case
//        return selected.seedPoints.contains(point)
//    }

    fun select(selected: Territory) {
        this.selected = selected
        neighbors.addAll(Graphs.neighborListOf(graph, selected)) // Add all existing neighbors
    }

    fun clearSelection() {
        this.selected = noTerritorySelected
        neighbors.clear()
    }

    fun selectNeighbor(territory: Territory) {
        if (territory == selected) {
            return
        }
        neighbors.add(territory)
    }

    fun deselectNeighbor(territory: Territory) {
        neighbors.remove(territory)
    }

    fun submitNeighbors() {
        if (selected != noTerritorySelected) {
            // TODO: Implement this properly
        }
    }

}