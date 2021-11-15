package com.riskrieg.editor.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aaronjyoder.util.json.gson.GsonUtil
import com.riskrieg.editor.Constants
import com.riskrieg.editor.algorithm.fill.MilazzoFill
import com.riskrieg.editor.algorithm.label.LabelPosition
import com.riskrieg.editor.util.ImageUtil
import com.riskrieg.editor.util.TerritoryUtil
import com.riskrieg.map.RkmMap
import com.riskrieg.map.data.MapAuthor
import com.riskrieg.map.data.MapGraph
import com.riskrieg.map.data.MapImage
import com.riskrieg.map.data.MapName
import com.riskrieg.map.edge.Border
import com.riskrieg.map.territory.SeedPoint
import com.riskrieg.map.territory.TerritoryId
import com.riskrieg.map.vertex.Territory
import com.riskrieg.rkm.RkmReader
import com.riskrieg.rkm.RkmWriter
import org.jgrapht.Graphs
import org.jgrapht.graph.SimpleGraph
import java.awt.Color
import java.awt.Font
import java.awt.Point
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.Normalizer
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.collections.ArrayDeque


class EditorModel {

    /* Exported Data */

    var mapDisplayName: String by mutableStateOf("")
    var mapAuthorName: String by mutableStateOf("")

    private var graph = SimpleGraph<Territory, Border>(Border::class.java)

    private var base: BufferedImage by mutableStateOf(BufferedImage(1, 1, 2))
    private var text: BufferedImage by mutableStateOf(BufferedImage(1, 1, 2))

    private var currentBase: BufferedImage by mutableStateOf(base)

    /* Internal Model Data */

    var editView by mutableStateOf(false)

    // Doing it this way because using the selectedRegions/selectedTerritories lists requires too many changes
    var isSelectingTerritory by mutableStateOf(false)
    var isSelectingRegion by mutableStateOf(false)

    var mousePos by mutableStateOf(Point(0, 0))

    var newTerritoryName by mutableStateOf("")

    // Unfortunately necessary for now
    private val submittedTerritories = mutableStateListOf<Territory>()
    private val finishedTerritories = mutableStateListOf<Territory>()

    /* Functional */
    private val mapSimpleNameRegex = Regex("[a-z0-9-]+")

    /* Selection */
    private val selectedRegions: ArrayDeque<Point> = ArrayDeque()
    private var selectedTerritories: MutableSet<Territory> = HashSet()
    private val selectedNeighbors: MutableSet<Territory> = HashSet()

    /* Getters */

    fun mapImage(): BufferedImage {
        return currentBase
    }

    fun textImage(): BufferedImage {
        return text
    }

    /** Methods **/

    /* Menu Bar Functions */

    fun newFile() {
        editView = false
        mapDisplayName = ""
        mapAuthorName = ""
        deselectAll()
        submittedTerritories.clear()
        finishedTerritories.clear()
        graph = SimpleGraph<Territory, Border>(Border::class.java)
        base = BufferedImage(1, 1, 2)
        text = BufferedImage(1, 1, 2)
        currentBase = base
    }

    fun openRkm() {
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("${Constants.NAME} Map (*.rkm)", "rkm")
        if (chooser.showDialog(null, "Open") == JFileChooser.APPROVE_OPTION) {
            try {
                val reader = RkmReader(chooser.selectedFile.toPath())
                val map = reader.read()
                newFile()
                base = map.mapImage().baseImage()
                text = map.mapImage().textImage()

                mapDisplayName = map.mapName().displayName()
                mapAuthorName = map.author().name()

                graph = SimpleGraph<Territory, Border>(Border::class.java)

                for (territory in map.graph().vertices()) {
                    graph.addVertex(territory)
                }
                for (border in map.graph().edges()) {
                    val source = map.graph().vertices().find { it.id().equals(border.source()) }
                    val target = map.graph().vertices().find { it.id().equals(border.target()) }
                    graph.addEdge(source, target, border)
                }

                submittedTerritories.addAll(graph.vertexSet())
                for (territory in graph.vertexSet()) {
                    if (Graphs.neighborSetOf(graph, territory).isNotEmpty()) {
                        finishedTerritories.add(territory)
                    }
                }
                editView = true
                update()
            } catch (e: Exception) {
                if (e.message != null && e.message!!.contains("invalid checksum", true)) {
                    JOptionPane.showMessageDialog(null, "Could not open .rkm map file: invalid checksum.")
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid .rkm map file.")
                }
                return
            }
        }
    }

    fun saveRkm() {
        if (mapDisplayName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Map display name cannot be empty.")
            return
        }
        if (mapAuthorName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Map author name cannot be empty.")
            return
        }
        if (graph.vertexSet().size == 0) {
            JOptionPane.showMessageDialog(null, "Nothing to export.")
            return
        } else if (graph.edgeSet().size == 0) {
            JOptionPane.showMessageDialog(null, "Please finish adding territory neighbors before exporting.")
            return
        }
        val chooser = JFileChooser()
        chooser.currentDirectory = File(".")
        chooser.dialogTitle = "Save ${Constants.NAME} Map File"
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("${Constants.NAME} Map File (*.rkm)", "rkm")

        val normalizedName = Normalizer.normalize(mapDisplayName, Normalizer.Form.NFD).replace("[^\\p{ASCII}]".toRegex(), "")
        val mapSimpleName = normalizedName.lowercase().replace("\\s+".toRegex(), "-").replace("[^a-z0-9-]".toRegex(), "")

        chooser.selectedFile = File("$mapSimpleName.rkm")
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            if (chooser.selectedFile.name.isNullOrBlank() || !chooser.selectedFile.nameWithoutExtension.matches(mapSimpleNameRegex)) {
                JOptionPane.showMessageDialog(null, "Invalid file name. Use only lowercase letters, numbers, and hyphens/dashes.")
            } else {
                val directory = chooser.currentDirectory.path.replace('\\', '/') + "/"
                try {
                    val rkmMap = RkmMap(MapName(mapSimpleName, mapDisplayName), MapAuthor(mapAuthorName), MapGraph(graph), MapImage(base, text))
                    val writer = RkmWriter(rkmMap)
                    val fos = FileOutputStream(File(directory + "${mapSimpleName}.rkm"))
                    writer.write(fos)
                    fos.close()
                    JOptionPane.showMessageDialog(null, "Map file successfully exported to the selected directory.")
                } catch (e: Exception) {
                    e.printStackTrace()
                    JOptionPane.showMessageDialog(null, "Unable to save map file due to an error.")
                }
            }
        }
    }

    fun openBaseImageOnly() {
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("Images (*.png)", "png")
        if (chooser.showDialog(null, "Import Base Layer") == JFileChooser.APPROVE_OPTION) {
            try {
                val newBase = ImageIO.read(chooser.selectedFile)
                newFile()
                base = newBase
                text = BufferedImage(base.width, base.height, BufferedImage.TYPE_INT_ARGB)

                editView = true
                update()
            } catch (e: IOException) {
                JOptionPane.showMessageDialog(null, "Error loading image.")
            }
        }
    }

    fun openImageLayers() {
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("Images (*.png)", "png")
        if (chooser.showDialog(null, "Import Base Layer") == JFileChooser.APPROVE_OPTION) {
            try {
                val newBase = ImageIO.read(chooser.selectedFile)
                newFile()
                base = newBase

                val successText = chooser.showDialog(null, "Import Text Layer")
                if (successText == JFileChooser.APPROVE_OPTION) {
                    val newText = ImageIO.read(chooser.selectedFile)
                    if (newText.height == newBase.height && newText.width == newBase.width) {
                        text = newText
                        editView = true
                        update()
                    } else {
                        JOptionPane.showMessageDialog(null, "Your text layer must match the width and height of your base layer. Import your base layer first.")
                    }
                }

            } catch (e: IOException) {
                JOptionPane.showMessageDialog(null, "Error loading image.")
            }
        }
    }

    fun reimportBaseImage() {
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("Image (*.png)", "png")
        if (chooser.showDialog(null, "New Base Layer Image") == JFileChooser.APPROVE_OPTION) {
            try {
                val newBase = ImageIO.read(chooser.selectedFile)
                deselectAll()
                base = newBase
                update()
            } catch (e: IOException) {
                JOptionPane.showMessageDialog(null, "Error loading image.")
            }
        }
    }

    fun reimportTextImage() {
        if (!editView) {
            JOptionPane.showMessageDialog(null, "Please import a map or map images before doing this.")
            return
        }
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("Image (*.png)", "png")
        if (chooser.showDialog(null, "New Text Layer Image") == JFileChooser.APPROVE_OPTION) {
            try {
                val newText = ImageIO.read(chooser.selectedFile)
                if (newText.height == mapImage().height && newText.width == mapImage().width) {
                    deselectAll()
                    text = newText
                    update()
                } else {
                    JOptionPane.showMessageDialog(null, "Your text layer must match the width and height of your base layer. Import your base layer first.")
                }
            } catch (e: IOException) {
                JOptionPane.showMessageDialog(null, "Error loading image.")
            }
        }
    }

    fun importGraph() {
        if (!editView) {
            JOptionPane.showMessageDialog(null, "Please import a map or map images before doing this.")
            return
        }
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("${Constants.NAME} Graph (*.json)", "json")
        if (chooser.showDialog(null, "Import Graph File") == JFileChooser.APPROVE_OPTION) {
            try {
                val mapGraph: MapGraph? = GsonUtil.read(chooser.selectedFile.toPath(), MapGraph::class.java)
                if (mapGraph != null) {
                    deselectAll()
                    graph = SimpleGraph<Territory, Border>(Border::class.java)
                    submittedTerritories.clear()
                    finishedTerritories.clear()

                    for (territory in mapGraph.vertices()) {
                        graph.addVertex(territory)
                    }
                    for (border in mapGraph.edges()) {
                        val source = graph.vertexSet().find { it.id().equals(border.source()) }
                        val target = graph.vertexSet().find { it.id().equals(border.target()) }
                        graph.addEdge(source, target, border)
                    }
                    submittedTerritories.addAll(graph.vertexSet())
                    finishedTerritories.addAll(graph.vertexSet())
                    update()
                } else {
                    JOptionPane.showMessageDialog(null, "Error loading graph file.")
                }
            } catch (e: Exception) {
                graph = SimpleGraph<Territory, Border>(Border::class.java)
                submittedTerritories.clear()
                finishedTerritories.clear()
                JOptionPane.showMessageDialog(null, "File invalid: JSON format does not match that of a correct map graph file.")
            }
        }
    }

    fun exportGraph() {
        if (graph.vertexSet().size == 0) {
            JOptionPane.showMessageDialog(null, "Nothing to export.")
            return
        } else if (graph.edgeSet().size == 0) {
            JOptionPane.showMessageDialog(null, "Please add some territory neighbors before exporting.")
            return
        }
        val chooser = JFileChooser()
        chooser.currentDirectory = File(".")
        chooser.dialogTitle = "Save ${Constants.NAME} Graph File"
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("${Constants.NAME} Graph (*.json)", "json")

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            if (chooser.selectedFile.name.isNullOrBlank() || !chooser.selectedFile.nameWithoutExtension.matches(mapSimpleNameRegex)) {
                JOptionPane.showMessageDialog(null, "Invalid file name.")
            } else {
                val fileName = chooser.selectedFile.nameWithoutExtension
                try {
                    GsonUtil.write(chooser.currentDirectory.toPath().resolve("$fileName.json"), MapGraph::class.java, MapGraph(graph))
                    JOptionPane.showMessageDialog(null, "Graph file successfully exported to the selected directory.")
                } catch (e: Exception) {
                    JOptionPane.showMessageDialog(null, "Unable to save graph file due to an error.")
                }
            }
        }
    }

    /* Selection */

    fun interact() {
        val selectedTerritory = getTerritory(mousePos, graph.vertexSet())
        if (selectedTerritory.isPresent) { // Territory or Neighbor
            if (selectedRegions.isNotEmpty()) { // Deselect region
                deselectRegions()
                isSelectingRegion = false
            }
            if (selectedTerritories.isNotEmpty()) { // Territory already selected
                if (selectedTerritories.contains(selectedTerritory.get())) { // Deselect territory
                    deselectTerritory()
                    isSelectingTerritory = false
                } else { // Neighbor
                    if (selectedNeighbors.contains(selectedTerritory.get())) { // Deselect neighbor
                        selectedNeighbors.remove(selectedTerritory.get())
                    } else { // Select neighbor
                        selectedNeighbors.add(selectedTerritory.get())
                    }
                }
            } else { // Select territory
                selectedTerritories.add(selectedTerritory.get())
                newTerritoryName = selectedTerritory.get().name()
                for (selected in selectedTerritories) {
                    selectedNeighbors.addAll(Graphs.neighborListOf(graph, selected))
                }
                isSelectingTerritory = true
            }
        } else { // Region
            val root = ImageUtil.getRootPixel(base, mousePos)
            if (base.getRGB(root.x, root.y) == Constants.TERRITORY_COLOR.rgb) {
                if (selectedTerritories.isNotEmpty() || selectedNeighbors.isNotEmpty()) { // Deselect territory
                    deselectTerritory()
                    isSelectingTerritory = false
                }
                if (selectedRegions.contains(root)) { // Deselect region
                    selectedRegions.remove(root)
                    if (selectedRegions.isEmpty()) {
                        isSelectingRegion = false
                    }
                } else { // Select region
                    selectedRegions.add(root)
                    isSelectingRegion = true
                }
            }
        }
        update()
    }

    private fun deselectRegions() {
        selectedRegions.clear()
        isSelectingRegion = false
    }

    private fun deselectTerritory() {
        selectedTerritories.clear()
        selectedNeighbors.clear()
        newTerritoryName = ""
        isSelectingTerritory = false
    }

    fun deselectAll() {
        deselectRegions()
        deselectTerritory()
        update()
    }

    fun submitSelectedRegions(drawLabel: Boolean) {
        if (selectedRegions.isEmpty()) {
            JOptionPane.showMessageDialog(null, "You need to select one or more regions first.")
            return
        }
        if (submittedTerritories.stream().anyMatch { a -> a.name().equals(newTerritoryName.trim(), true) }) {
            JOptionPane.showMessageDialog(null, "A territory with that name already exists.")
            return
        }
        if (newTerritoryName.isBlank()) {
            JOptionPane.showMessageDialog(null, "Please enter a valid territory name.")
            return
        }

        if (drawLabel) {

//            val labelPosition = LabelPosition(base, selectedRegions.toMutableSet()).calculatePosition(0.001)

            val lp = LabelPosition(base, selectedRegions.toMutableSet(), 0.001)

            if (lp.canLabelFit(newTerritoryName.trim(), 20)) { // Only draw label if it can fit
                val labelPosition = lp.calculatePosition()

                val convertedText = BufferedImage(text.width, text.height, BufferedImage.TYPE_INT_ARGB)
                convertedText.graphics.drawImage(text, 0, 0, null)
                convertedText.graphics.dispose()

                val textGraphics = convertedText.createGraphics()

                textGraphics.paint = Constants.BORDER_COLOR

                // TODO: Set size based on whether it can fit inside territory bounds, to a minimum, with 20 as the maximum and default
                ImageUtil.drawCenteredString(textGraphics, newTerritoryName.trim(), Rectangle(labelPosition.x, labelPosition.y, 1, 1), Font("Spectral", Font.PLAIN, 20))

                textGraphics.dispose()
                text = convertedText
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    "A label will not fit in that territory. You will need to label it manually in an image editor and then import it as the new text layer."
                )
                return
            }
        }

        if (selectedRegions.isNotEmpty()) {
            val seedPoints = HashSet<SeedPoint>()
            for (point in selectedRegions) {
                seedPoints.add(SeedPoint(point.x, point.y))
            }
            val result = Territory(TerritoryId(newTerritoryName.trim()), seedPoints)
            graph.addVertex(result)
            submittedTerritories.add(result)
            deselectRegions()
            newTerritoryName = ""
            update()
        }
    }

    fun submitSelectedNeighbors() {
        for (selected in selectedTerritories) {
            if (graph.containsVertex(selected)) { // Handles updating deselected neighbors
                val edgesToRemove = HashSet<Border>()
                val currentNeighbors = Graphs.neighborListOf(graph, selected)
                currentNeighbors.removeAll(selectedNeighbors)

                for (deselectedNeighbor in currentNeighbors) {
                    for (border in graph.edgeSet()) {
                        if (border.equals(graph.getEdge(selected, deselectedNeighbor))) {
                            edgesToRemove.add(border)
                        }
                    }
                }
                graph.removeAllEdges(edgesToRemove)
            }

            for (neighbor in selectedNeighbors) {
                val border = Border(selected.id(), neighbor.id())
                graph.addEdge(selected, neighbor, border)
            }
            if (Graphs.neighborListOf(graph, selected).size == 0) {
                finishedTerritories.remove(selected)
            } else if (!finishedTerritories.contains(selected)) {
                finishedTerritories.add(selected)
            }
            deselectTerritory()
        }
        update()
    }

    fun deleteSelectedTerritory() {
        if (selectedTerritories.isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have not selected a territory to delete.")
            return
        }
        for (territory in selectedTerritories) {

            // Delete territory label
            val seedPoints = HashSet<Point>()
            for (sp in territory.seedPoints()) {
                seedPoints.add(Point(sp.x(), sp.y()))
            }
            val innerPointsMap = TerritoryUtil.createInnerPointMap(seedPoints, base)

            for ((seed, innerPoints) in innerPointsMap) {
                for (point in innerPoints) {
                    text.setRGB(point.x, point.y, Color(0, 0, 0, 0).rgb)
                }
            }

            // Delete territory
            finishedTerritories.remove(territory)
            submittedTerritories.remove(territory)
            graph.removeVertex(territory)
        }
        deselectTerritory()
        update()
    }

    fun deleteAll() {
        deselectAll()
        submittedTerritories.clear()
        finishedTerritories.clear()
        currentBase = base
        graph = SimpleGraph<Territory, Border>(Border::class.java)
        update()
    }

    /* Private */

    private fun update() {
        val copy = BufferedImage(base.width, base.height, BufferedImage.TYPE_INT_ARGB)
        val g2d = copy.createGraphics()
        g2d.drawImage(base, 0, 0, null)
        g2d.dispose()

        for (territory in submittedTerritories) {
            for (seedPoint in territory.seedPoints()) {
                val point = seedPoint.asPoint()
                MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.SUBMITTED_COLOR).fill(point)
            }
        }

        for (territory in finishedTerritories) {
            for (seedPoint in territory.seedPoints()) {
                val point = seedPoint.asPoint()
                MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.FINISHED_COLOR).fill(point)
            }
        }

        for (territory in selectedNeighbors) {
            for (seedPoint in territory.seedPoints()) {
                val point = seedPoint.asPoint()
                MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.NEIGHBOR_SELECT_COLOR).fill(point)
            }
        }

        if (selectedRegions.isNotEmpty()) {
            for (point in selectedRegions) {
                MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.SELECT_COLOR).fill(point)
            }
        } else if (selectedTerritories.isNotEmpty()) {
            for (territory in selectedTerritories) {
                for (seedPoint in territory.seedPoints()) {
                    val point = seedPoint.asPoint()
                    MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.SELECT_COLOR).fill(point)
                }
            }
        }
        currentBase = copy
    }

    private fun getTerritory(point: Point, territorySet: Set<Territory>): Optional<Territory> {
        val root = ImageUtil.getRootPixel(base, point)
        for (territory in territorySet) {
            if (territory.seedPoints().contains(SeedPoint(root.x, root.y))) {
                return Optional.of(territory)
            }
        }
        return Optional.empty()
    }

}