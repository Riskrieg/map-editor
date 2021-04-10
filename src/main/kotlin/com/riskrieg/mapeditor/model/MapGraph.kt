package com.riskrieg.mapeditor.model

import org.jgrapht.Graph

class MapGraph(val vertices: Set<Territory>, val edges: Set<Border>) {

    constructor(graph: Graph<Territory, Border>) : this(graph.vertexSet(), graph.edgeSet())

    fun vertices(): Set<Territory> {
        return vertices
    }

    fun edges(): Set<Border> {
        return edges
    }

    init {
        check(vertices.isNotEmpty()) { "Field 'vertices' of type Set<Territory> must not be empty" }
        check(edges.isNotEmpty()) { "Field 'edges' of type Set<Border> must not be empty" }
    }

}