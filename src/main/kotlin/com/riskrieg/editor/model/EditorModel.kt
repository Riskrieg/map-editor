package com.riskrieg.editor.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.riskrieg.core.api.game.map.GameMap
import com.riskrieg.core.api.game.map.Territory
import com.riskrieg.core.api.game.map.territory.Border
import com.riskrieg.core.api.game.map.territory.Nucleus
import com.riskrieg.core.api.identifier.TerritoryIdentifier
import com.riskrieg.core.decode.RkmDecoder
import com.riskrieg.core.encode.RkmEncoder
import com.riskrieg.core.util.io.RkJsonUtil
import com.riskrieg.editor.core.Constants
import com.riskrieg.editor.core.algorithm.fill.MilazzoFill
import com.riskrieg.editor.core.algorithm.label.LabelPosition
import com.riskrieg.editor.util.ImageUtil
import com.riskrieg.editor.util.TerritoryUtil
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
import java.nio.file.Files
import java.text.Normalizer
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.collections.ArrayDeque


class EditorModel(private val window: ComposeWindow) {

    /* Exported Data */

    var mapDisplayName: String by mutableStateOf("")
    var mapAuthorName: String by mutableStateOf("")

    private var graph = SimpleGraph<Territory, Border>(Border::class.java)

    private var baseLayer: BufferedImage by mutableStateOf(BufferedImage(1, 1, 2))
    private var textLayer: BufferedImage by mutableStateOf(BufferedImage(1, 1, 2))

    private var currentBaseLayer: BufferedImage by mutableStateOf(baseLayer)

    /* Internal Model Data */

    var editView by mutableStateOf(false)
    var isDragAndDropping by mutableStateOf(false)

    // Doing it this way because using the selectedRegions/selectedTerritories lists requires too many changes
    var isSelectingTerritory by mutableStateOf(false)
    var isSelectingRegion by mutableStateOf(false)
    var selectedRegionsHaveLabel by mutableStateOf(false)
    var selectedTerritoryHasLabel by mutableStateOf(false)

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
        return currentBaseLayer
    }

    fun textImage(): BufferedImage {
        return textLayer
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
        baseLayer = BufferedImage(1, 1, 2)
        textLayer = BufferedImage(1, 1, 2)
        currentBaseLayer = baseLayer
    }

    fun openFile(rkmFile: File) {
        isDragAndDropping = false
        try {
            val decoder = RkmDecoder()
            val map = decoder.decode(rkmFile.toPath())
            newFile()
            baseLayer = map.baseLayer()
            textLayer = map.textLayer()

            mapDisplayName = map.displayName()
            mapAuthorName = map.author()

            graph = SimpleGraph<Territory, Border>(Border::class.java)

            for (territory in map.graph().vertexSet()) {
                graph.addVertex(territory)
            }
            for (border in map.graph().edgeSet()) {
                val source = map.graph().vertexSet().find { t -> t.identifier.id == border.sourceId }
                val target = map.graph().vertexSet().find { t -> t.identifier.id == border.targetId }
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
                JOptionPane.showMessageDialog(window, "Could not open .rkm map file: invalid checksum.", "Error", JOptionPane.ERROR_MESSAGE)
            } else {
                JOptionPane.showMessageDialog(window, "Invalid .rkm map file.", "Error", JOptionPane.ERROR_MESSAGE)
            }
            return
        }
    }

    fun openRkm() {
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("${Constants.NAME} Map (*.rkm)", "rkm")
        chooser.currentDirectory = File(System.getProperty("user.home"))
        if (chooser.showDialog(window, "Open") == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.selectedFile)
        }
    }

    fun saveRkm() {
        if (mapDisplayName.isEmpty()) {
            JOptionPane.showMessageDialog(window, "Map display name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        if (mapAuthorName.isEmpty()) {
            JOptionPane.showMessageDialog(window, "Map author name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        if (graph.vertexSet().size == 0) {
            JOptionPane.showMessageDialog(window, "Nothing to export.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        } else if (graph.edgeSet().size == 0) {
            JOptionPane.showMessageDialog(window, "Please finish adding territory neighbors before exporting.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        val chooser = JFileChooser()
        chooser.dialogTitle = "Save ${Constants.NAME} Map File"
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("${Constants.NAME} Map File (*.rkm)", "rkm")
        chooser.currentDirectory = File(System.getProperty("user.home"))

        val normalizedName = Normalizer.normalize(mapDisplayName, Normalizer.Form.NFD).replace("[^\\p{ASCII}]".toRegex(), "")
        val mapCodename = normalizedName.lowercase().replace("\\s+".toRegex(), "-").replace("[^a-z0-9-]".toRegex(), "")

        chooser.selectedFile = File("$mapCodename.rkm")
        if (chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
            if (chooser.selectedFile.name.isNullOrBlank() || !chooser.selectedFile.nameWithoutExtension.matches(mapSimpleNameRegex)) {
                JOptionPane.showMessageDialog(window, "Invalid file name. Use only lowercase letters, numbers, and hyphens/dashes.", "Error", JOptionPane.ERROR_MESSAGE)
            } else {
                val directory = chooser.currentDirectory.path.replace('\\', '/') + "/"
                try {
                    val rkmMap = GameMap(mapCodename, mapDisplayName, mapAuthorName, graph.vertexSet(), graph.edgeSet(), baseLayer, textLayer)
                    val encoder = RkmEncoder()
                    val fos = FileOutputStream(File(directory + "${mapCodename}.rkm"))
                    encoder.encode(rkmMap, fos)
                    fos.close()
                    JOptionPane.showMessageDialog(window, "Map file successfully exported to the selected directory.", "Success", JOptionPane.PLAIN_MESSAGE)
                } catch (e: Exception) {
                    e.printStackTrace()
                    JOptionPane.showMessageDialog(window, "Unable to save map file due to an error.", "Error", JOptionPane.ERROR_MESSAGE)
                }
            }
        }
    }

    fun openBaseImageOnly() {
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("Images (*.png)", "png")
        chooser.currentDirectory = File(System.getProperty("user.home"))
        if (chooser.showDialog(window, "Import Base Layer") == JFileChooser.APPROVE_OPTION) {
            try {
                val newBaseLayer = ImageIO.read(chooser.selectedFile)
                newFile()
                baseLayer = newBaseLayer
                textLayer = BufferedImage(baseLayer.width, baseLayer.height, BufferedImage.TYPE_INT_ARGB)

                isDragAndDropping = false
                editView = true
                update()
            } catch (e: IOException) {
                JOptionPane.showMessageDialog(window, "Error loading image.", "Error", JOptionPane.ERROR_MESSAGE)
            }
        }
    }

    fun openImageLayers() {
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("Images (*.png)", "png")
        chooser.currentDirectory = File(System.getProperty("user.home"))
        if (chooser.showDialog(window, "Import Base Layer") == JFileChooser.APPROVE_OPTION) {
            try {
                val newBaseLayer = ImageIO.read(chooser.selectedFile)
                newFile()
                baseLayer = newBaseLayer

                val successText = chooser.showDialog(window, "Import Text Layer")
                if (successText == JFileChooser.APPROVE_OPTION) {
                    val newTextLayer = ImageIO.read(chooser.selectedFile)
                    if (newTextLayer.height == newBaseLayer.height && newTextLayer.width == newBaseLayer.width) {
                        textLayer = newTextLayer
                        isDragAndDropping = false
                        editView = true
                        update()
                    } else {
                        JOptionPane.showMessageDialog(
                            window,
                            "Your text layer must match the width and height of your base layer. Import your base layer first.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                }

            } catch (e: IOException) {
                JOptionPane.showMessageDialog(window, "Error loading image.", "Error", JOptionPane.ERROR_MESSAGE)
            }
        }
    }

    fun reimportBaseImage() {
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("Image (*.png)", "png")
        chooser.currentDirectory = File(System.getProperty("user.home"))
        if (chooser.showDialog(window, "New Base Layer Image") == JFileChooser.APPROVE_OPTION) {
            try {
                val newBaseLayer = ImageIO.read(chooser.selectedFile)
                deselectAll()
                baseLayer = newBaseLayer
                update()
            } catch (e: IOException) {
                JOptionPane.showMessageDialog(window, "Error loading image.", "Error", JOptionPane.ERROR_MESSAGE)
            }
        }
    }

    fun reimportTextImage() {
        if (!editView) {
            JOptionPane.showMessageDialog(window, "Please import a map or base image before doing this.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("Image (*.png)", "png")
        chooser.currentDirectory = File(System.getProperty("user.home"))
        if (chooser.showDialog(window, "New Text Layer Image") == JFileChooser.APPROVE_OPTION) {
            try {
                val newTextLayer = ImageIO.read(chooser.selectedFile)
                if (newTextLayer.height == mapImage().height && newTextLayer.width == mapImage().width) {
                    deselectAll()
                    textLayer = newTextLayer
                    update()
                } else {
                    JOptionPane.showMessageDialog(
                        window,
                        "Your text layer must match the width and height of your base layer. Import your base layer first.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            } catch (e: IOException) {
                JOptionPane.showMessageDialog(window, "Error loading image.", "Error", JOptionPane.ERROR_MESSAGE)
            }
        }
    }

    fun exportTextImage() {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Save ${Constants.NAME} Text Image"
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("Image (*.png)", "png")
        chooser.currentDirectory = File(System.getProperty("user.home"))
        if (chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
            if (chooser.selectedFile.name.isNullOrBlank()) {
                JOptionPane.showMessageDialog(window, "Invalid file name.", "Error", JOptionPane.ERROR_MESSAGE)
            } else {
                val fileName = chooser.selectedFile.nameWithoutExtension
                try {
                    ImageIO.write(textLayer, "png", chooser.currentDirectory.toPath().resolve("$fileName.png").toFile())
                    JOptionPane.showMessageDialog(window, "Text image successfully exported to the selected directory.", "Success", JOptionPane.PLAIN_MESSAGE)
                } catch (e: Exception) {
                    JOptionPane.showMessageDialog(window, "Unable to text image due to an error.", "Error", JOptionPane.ERROR_MESSAGE)
                }
            }
        }
    }

    fun importGraph() {
        if (!editView) {
            JOptionPane.showMessageDialog(window, "Please import a map or map images before doing this.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        val chooser = JFileChooser()
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("${Constants.NAME} Graph (*.json)", "json")
        chooser.currentDirectory = File(System.getProperty("user.home"))
        if (chooser.showDialog(window, "Import Graph File") == JFileChooser.APPROVE_OPTION) {
            try {
                // Workaround for RkJsonUtil not supporting Kotlin super well atm
                val jsonMapper = jacksonMapperBuilder().addModule(KotlinModule.Builder().build()).build()
                val path = chooser.selectedFile.toPath()
                var mapGraph: MapGraph? = null
                if (Files.isRegularFile(path) && Files.isReadable(path)) {
                    mapGraph = jsonMapper.readValue(Files.newBufferedReader(path), jsonMapper.constructType(MapGraph::class.java))
                }
                // End workaround
                // val mapGraph: MapGraph? = RkJsonUtil.read(chooser.selectedFile.toPath(), MapGraph::class.java) <- This would be preferable
                if (mapGraph != null) {
                    deselectAll()
                    graph = SimpleGraph<Territory, Border>(Border::class.java)
                    submittedTerritories.clear()
                    finishedTerritories.clear()

                    for (territory in mapGraph.vertexSet) {
                        graph.addVertex(territory)
                    }
                    for (border in mapGraph.edgeSet) {
                        val source = graph.vertexSet().find { t -> t.identifier.id == border.sourceId }
                        val target = graph.vertexSet().find { t -> t.identifier.id == border.targetId }
                        graph.addEdge(source, target, border)
                    }
                    submittedTerritories.addAll(graph.vertexSet())
                    finishedTerritories.addAll(graph.vertexSet())
                    update()
                } else {
                    JOptionPane.showMessageDialog(window, "Error loading graph file.", "Error", JOptionPane.ERROR_MESSAGE)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                graph = SimpleGraph<Territory, Border>(Border::class.java)
                submittedTerritories.clear()
                finishedTerritories.clear()
                JOptionPane.showMessageDialog(window, "File invalid: JSON format does not match that of a correct map graph file.", "Error", JOptionPane.ERROR_MESSAGE)
            }
        }
    }

    fun exportGraph() { // TODO: This doesn't work outside of the IDE for some reason.
        if (graph.vertexSet().size == 0) {
            JOptionPane.showMessageDialog(window, "Nothing to export.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        } else if (graph.edgeSet().size == 0) {
            JOptionPane.showMessageDialog(window, "Please add some territory neighbors before exporting.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        val chooser = JFileChooser()
        chooser.dialogTitle = "Save ${Constants.NAME} Graph File"
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = FileNameExtensionFilter("${Constants.NAME} Graph (*.json)", "json")
        chooser.currentDirectory = File(System.getProperty("user.home"))

        if (chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
            if (chooser.selectedFile.name.isNullOrBlank() || !chooser.selectedFile.nameWithoutExtension.matches(mapSimpleNameRegex)) {
                JOptionPane.showMessageDialog(window, "Invalid file name. Use only lowercase letters, numbers, and hyphens/dashes.", "Error", JOptionPane.ERROR_MESSAGE)
            } else {
                try {
                    RkJsonUtil.write(chooser.currentDirectory.toPath().resolve("${chooser.selectedFile.nameWithoutExtension}.json"), MapGraph::class.java, MapGraph(graph))
                    JOptionPane.showMessageDialog(window, "Graph file successfully exported to the selected directory.", "Success", JOptionPane.PLAIN_MESSAGE)
                } catch (e: Exception) {
                    e.printStackTrace()
                    JOptionPane.showMessageDialog(window, "Unable to save graph file due to an error.", "Error", JOptionPane.ERROR_MESSAGE)
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
                this.selectedTerritoryHasLabel = checkSelectedTerritoryHasLabel()
            }
        } else { // Region
            val root = ImageUtil.getRootPixel(baseLayer, mousePos)
            if (baseLayer.getRGB(root.x, root.y) == Constants.TERRITORY_COLOR.rgb) {
                if (selectedTerritories.isNotEmpty() || selectedNeighbors.isNotEmpty()) { // Deselect territory
                    deselectTerritory()
                    isSelectingTerritory = false
                }
                if (selectedRegions.contains(root)) { // Deselect region
                    selectedRegions.remove(root)
                    if (selectedRegions.isEmpty()) {
                        isSelectingRegion = false
                    }
                    this.selectedRegionsHaveLabel = checkSelectedRegionsHaveLabel()
                } else { // Select region
                    selectedRegions.add(root)
                    isSelectingRegion = true
                    this.selectedRegionsHaveLabel = checkSelectedRegionsHaveLabel()
                }
            }
        }
        update()
    }

    private fun deselectRegions() {
        selectedRegions.clear()
        isSelectingRegion = false
        this.selectedRegionsHaveLabel = false
    }

    private fun deselectTerritory() {
        selectedTerritories.clear()
        selectedNeighbors.clear()
        newTerritoryName = ""
        isSelectingTerritory = false
        this.selectedTerritoryHasLabel = false
    }

    fun deselectAll() {
        deselectRegions()
        deselectTerritory()
        update()
    }

    fun submitSelectedRegions(drawLabel: Boolean) {
        if (selectedRegions.isEmpty()) {
            JOptionPane.showMessageDialog(window, "You need to select one or more regions first.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }
        if (submittedTerritories.stream().anyMatch { a -> a.name().equals(newTerritoryName.trim(), true) }) {
            JOptionPane.showMessageDialog(window, "A territory with that name already exists.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }
        if (newTerritoryName.isBlank()) {
            JOptionPane.showMessageDialog(window, "Please enter a valid territory name.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }

        if (drawLabel) {

            val lp = LabelPosition(baseLayer, selectedRegions.toMutableSet(), 0.001)

            val territoryFont = Font("Spectral Medium", Font.PLAIN, 20)

            if (lp.canLabelFit(newTerritoryName.trim(), territoryFont)) { // Only draw label if it can fit
                val labelPosition = lp.calculatePosition()

                val convertedText = ImageUtil.createCopy(ImageUtil.convert(textLayer, BufferedImage.TYPE_INT_ARGB))

                val textGraphics = convertedText.createGraphics()

                textGraphics.paint = Constants.TEXT_COLOR

                // TODO: Set size based on whether it can fit inside territory bounds, to a minimum, with 20 as the maximum and default
                ImageUtil.drawCenteredString(textGraphics, newTerritoryName.trim(), Rectangle(labelPosition.x, labelPosition.y, 1, 1), territoryFont)

                textGraphics.dispose()
                textLayer = convertedText
            } else {
                JOptionPane.showMessageDialog(
                    window,
                    "A label will not fit in that territory. You will need to export the current text image using the debug menu, manually apply your label in an image editor, and then re-import the text image using the debug menu. You should save your work first.",
                    "Warning", JOptionPane.WARNING_MESSAGE
                )
                return
            }
        }

        if (selectedRegions.isNotEmpty()) {
            val nuclei = HashSet<Nucleus>()
            for (point in selectedRegions) {
                nuclei.add(Nucleus(point.x, point.y))
            }
            val result = Territory(TerritoryIdentifier.of(newTerritoryName.trim()), nuclei)
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
                val border = Border(selected.identifier.id, neighbor.identifier.id)
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

    private fun checkSelectedTerritoryHasLabel(): Boolean {
        for (territory in selectedTerritories) {
            val nuclei = HashSet<Point>()
            for (sp in territory.nuclei()) {
                nuclei.add(Point(sp.x(), sp.y()))
            }
            val innerPointsMap = TerritoryUtil.createInnerPointMap(nuclei, baseLayer)

            for ((_, innerPoints) in innerPointsMap) {
                for (point in innerPoints) {
                    if (textLayer.getRGB(point.x, point.y) != Color(0, 0, 0, 0).rgb) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun checkSelectedRegionsHaveLabel(): Boolean {
        for (region in selectedRegions) {
            val innerPointsMap = TerritoryUtil.createInnerPointMap(mutableSetOf(region), baseLayer)
            for ((_, innerPoints) in innerPointsMap) {
                for (point in innerPoints) {
                    if (textLayer.getRGB(point.x, point.y) != Color(0, 0, 0, 0).rgb) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun addTerritoryLabel() {
        if (selectedTerritories.isEmpty()) {
            JOptionPane.showMessageDialog(window, "You have not selected a territory to add a label to.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }
        if (selectedTerritories.size > 1) {
            JOptionPane.showMessageDialog(window, "You can only add a label to one territory at a time.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }
        if (this.selectedTerritoryHasLabel) {
            JOptionPane.showMessageDialog(window, "This territory is already labelled.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }
        for (territory in selectedTerritories) {
            val lp = LabelPosition(baseLayer, territory.nuclei.map { nucleus -> nucleus.toPoint() }.toMutableSet(), 0.001)

            val territoryFont = Font("Spectral Medium", Font.PLAIN, 20)
            if (lp.canLabelFit(territory.identifier.id.trim(), territoryFont)) { // Only draw label if it can fit
                val labelPosition = lp.calculatePosition()

                val convertedText = ImageUtil.createCopy(ImageUtil.convert(textLayer, BufferedImage.TYPE_INT_ARGB))

                val textGraphics = convertedText.createGraphics()

                textGraphics.paint = Constants.TEXT_COLOR

                // TODO: Set size based on whether it can fit inside territory bounds, to a minimum, with 20 as the maximum and default
                ImageUtil.drawCenteredString(textGraphics, territory.identifier.id.trim(), Rectangle(labelPosition.x, labelPosition.y, 1, 1), territoryFont)

                textGraphics.dispose()
                textLayer = convertedText
                update()
                this.selectedTerritoryHasLabel = true
            } else {
                JOptionPane.showMessageDialog(
                    window,
                    "A label will not fit in that territory. You will need to export the current text image using the debug menu, manually apply your label in an image editor, and then re-import the text image using the debug menu. You should save your work first.",
                    "Warning", JOptionPane.WARNING_MESSAGE
                )
                return
            }
        }
    }

    fun clearTerritoryLabel() {
        if (selectedTerritories.isEmpty()) {
            JOptionPane.showMessageDialog(window, "You have not selected a territory to delete.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }
        for (territory in selectedTerritories) {
            // Clear territory label
            val nuclei = HashSet<Point>()
            for (sp in territory.nuclei()) {
                nuclei.add(Point(sp.x(), sp.y()))
            }
            val innerPointsMap = TerritoryUtil.createInnerPointMap(nuclei, baseLayer)

            for ((_, innerPoints) in innerPointsMap) {
                for (point in innerPoints) {
                    textLayer.setRGB(point.x, point.y, Color(0, 0, 0, 0).rgb)
                }
            }
        }
        update()
        this.selectedTerritoryHasLabel = false
    }

    fun deleteSelectedTerritory() {
        if (selectedTerritories.isEmpty()) {
            JOptionPane.showMessageDialog(window, "You have not selected a territory to delete.", "Warning", JOptionPane.WARNING_MESSAGE)
            return
        }
        for (territory in selectedTerritories) {
            // Delete territory data
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
        currentBaseLayer = baseLayer
        graph = SimpleGraph<Territory, Border>(Border::class.java)
        update()
    }

    /* Private */

    private fun update() {
        val copy = BufferedImage(baseLayer.width, baseLayer.height, BufferedImage.TYPE_INT_ARGB)
        val g2d = copy.createGraphics()
        g2d.drawImage(baseLayer, 0, 0, null)
        g2d.dispose()

        for (territory in submittedTerritories) {
            for (seedPoint in territory.nuclei()) {
                val point = seedPoint.toPoint()
                MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.SUBMITTED_COLOR).fill(point)
            }
        }

        for (territory in finishedTerritories) {
            for (seedPoint in territory.nuclei()) {
                val point = seedPoint.toPoint()
                MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.FINISHED_COLOR).fill(point)
            }
        }

        for (territory in selectedNeighbors) {
            for (seedPoint in territory.nuclei()) {
                val point = seedPoint.toPoint()
                MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.NEIGHBOR_SELECT_COLOR).fill(point)
            }
        }

        if (selectedRegions.isNotEmpty()) {
            for (point in selectedRegions) {
                MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.SELECT_COLOR).fill(point)
            }
        } else if (selectedTerritories.isNotEmpty()) {
            for (territory in selectedTerritories) {
                for (seedPoint in territory.nuclei()) {
                    val point = seedPoint.toPoint()
                    MilazzoFill(copy, Color(copy.getRGB(point.x, point.y)), Constants.SELECT_COLOR).fill(point)
                }
            }
        }
        currentBaseLayer = copy
    }

    private fun getTerritory(point: Point, territorySet: Set<Territory>): Optional<Territory> {
        val root = ImageUtil.getRootPixel(baseLayer, point)
        for (territory in territorySet) {
            if (territory.nuclei().contains(Nucleus(root.x, root.y))) {
                return Optional.of(territory)
            }
        }
        return Optional.empty()
    }

}