package com.riskrieg.editor.model.internal

import com.riskrieg.core.api.game.map.Territory
import com.riskrieg.core.api.game.map.territory.Border
import org.jgrapht.Graph

data class MapGraph(val vertexSet: Set<Territory>, val edgeSet: Set<Border>) {
    constructor(graph: Graph<Territory, Border>) : this(graph.vertexSet(), graph.edgeSet())
}
