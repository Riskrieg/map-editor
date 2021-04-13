package com.riskrieg.mapeditor.model;

import com.aaronjyoder.util.json.gson.GsonUtil;
import com.riskrieg.mapeditor.map.graph.Edge;
import com.riskrieg.mapeditor.map.graph.MapData;
import com.riskrieg.mapeditor.map.graph.Territory;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;

public class MapDataModel {

  private Graph<Territory, Edge> graph;

  private Territory selected;
  private Set<Territory> selectedNeighbors;
  private Set<Territory> finishedTerritories;

  public MapDataModel() {
    graph = new SimpleGraph<>(Edge.class);
    selectedNeighbors = new HashSet<>();
    finishedTerritories = new HashSet<>();
  }

  public boolean save(String fileName) {
    try {
      GsonUtil.write(fileName + ".json", MapData.class, new MapData(graph));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public Set<Territory> getSubmitted() {
    return graph.vertexSet();
  }

  public Set<Territory> getFinished() {
    return finishedTerritories;
  }

  public Set<Territory> getSelectedNeighbors() {
    return selectedNeighbors;
  }

  public Optional<Territory> getSelected() {
    return Optional.ofNullable(selected);
  }

  public void select(Territory selected) {
    this.selected = selected;
    if (selected != null) {
      selectedNeighbors.addAll(Graphs.neighborListOf(graph, selected));
    }
  }

  public void clearSelection() {
    this.selected = null;
    this.selectedNeighbors.clear();
  }

  public boolean selectNeighbor(Territory territory) {
    if (territory == null || territory.equals(selected)) {
      return false;
    }
    return this.selectedNeighbors.add(territory);
  }

  public boolean deselectNeighbor(Territory territory) {
    return this.selectedNeighbors.remove(territory);
  }

  public void submitTerritory(Territory territory) {
    graph.addVertex(territory);
  }

  public boolean removeSubmittedTerritory(Territory territory) {
    finishedTerritories.remove(territory);
    return graph.removeVertex(territory);
  }

  public void submitNeighbors() {
    // Remove deselected neighbors.
    if (graph.containsVertex(selected)) {
      Set<Edge> edgesToRemove = new HashSet<>();
      Set<Territory> currentNeighbors = Graphs.neighborSetOf(graph, selected);// graph.getNeighbors(selected);
      currentNeighbors.removeAll(selectedNeighbors); // This is the set of all deselected neighboring territories.
      for (Territory deselectedNeighbor : currentNeighbors) {
        for (Edge edge : graph.edgeSet()) {
          if (edge.equals(graph.getEdge(selected, deselectedNeighbor))) {
            edgesToRemove.add(edge);
          }
        }
      }
      graph.removeAllEdges(edgesToRemove);
    }

    // Add all selected neighbors in case any new ones were added.
    for (Territory selectedNeighbor : selectedNeighbors) {
      Edge edge = new Edge(selected, selectedNeighbor);
      graph.addEdge(selected, selectedNeighbor, edge);
    }
    finishedTerritories.add(selected);
    clearSelection();
  }

}
