package com.riskrieg.mapeditor.map.graph;

import java.util.Objects;
import java.util.Set;
import org.jgrapht.Graph;

public class MapData { // TODO: Turn into Record in Java 16

  private final Set<Territory> vertices;
  private final Set<Edge> edges;

  public MapData(Set<Territory> vertices, Set<Edge> edges) {
    Objects.requireNonNull(vertices);
    Objects.requireNonNull(edges);
    this.vertices = vertices;
    this.edges = edges;
  }

  public MapData(Graph<Territory, Edge> graph) {
    Objects.requireNonNull(graph);
    this.vertices = graph.vertexSet();
    this.edges = graph.edgeSet();
  }

  public Set<Territory> vertices() {
    return vertices;
  }

  public Set<Edge> edges() {
    return edges;
  }

}
