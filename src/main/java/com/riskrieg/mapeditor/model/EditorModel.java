package com.riskrieg.mapeditor.model;

import com.riskrieg.mapeditor.map.graph.Territory;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.Set;
import javax.swing.DefaultListModel;

public class EditorModel {

  private String mapName;

  private BufferedImage baseLayer;
  private BufferedImage textLayer;

  private EditMode editMode;

  private final Deque<Point> selectedRegions = new ArrayDeque<>();
  private final MapDataModel dataModel;

  private final DefaultListModel<Territory> territoryListModel;

  public EditorModel(EditMode editMode) {
    this.dataModel = new MapDataModel();
    this.territoryListModel = new DefaultListModel<>();

    this.editMode = editMode;
  }

  public DefaultListModel<Territory> getTerritoryListModel() {
    return territoryListModel; // TODO: Temp
  }

  // MapDataModel

  public Set<Territory> getSubmitted() {
    return dataModel.getSubmitted();
  }

  public Set<Territory> getFinished() {
    return dataModel.getFinished();
  }

  public Set<Territory> getSelectedNeighbors() {
    return dataModel.getSelectedNeighbors();
  }

  public Optional<Territory> getSelected() {
    return dataModel.getSelected();
  }

  public void select(Territory selected) {
    dataModel.select(selected);
  }

  public void clearSelection() {
    dataModel.clearSelection();
  }

  public boolean selectNeighbor(Territory territory) {
    return dataModel.selectNeighbor(territory);
  }

  public boolean deselectNeighbor(Territory territory) {
    return dataModel.deselectNeighbor(territory);
  }

  public void submitTerritory(Territory territory) {
    territoryListModel.addElement(territory);
    dataModel.submitTerritory(territory);
  }

  public boolean removeSubmittedTerritory(Territory territory) {
    territoryListModel.removeElement(territory);
    return dataModel.removeSubmittedTerritory(territory);
  }

  public void submitNeighbors() {
    dataModel.submitNeighbors();
  }

  public boolean save(String fileName) {
    return dataModel.save(fileName);
  }

  // This class

  public Optional<String> getMapName() {
    return Optional.ofNullable(mapName);
  }

  public Optional<BufferedImage> getBaseLayer() {
    return Optional.ofNullable(baseLayer);
  }

  public Optional<BufferedImage> getTextLayer() {
    return Optional.ofNullable(textLayer);
  }

  public boolean imageLayersPresent() {
    return getBaseLayer().isPresent() && getTextLayer().isPresent();
  }

  public EditMode getEditMode() {
    return editMode;
  }

  public Deque<Point> getSelectedRegions() {
    return selectedRegions;
  }

  public void setBaseLayer(BufferedImage baseLayer) {
    this.baseLayer = baseLayer;
  }

  public void setTextLayer(BufferedImage textLayer) {
    this.textLayer = textLayer;
  }

  public void setEditMode(EditMode editMode) {
    this.editMode = editMode;
  }

  public void setMapName(String mapName) {
    this.mapName = mapName;
  }

  public boolean selectRegion(Point point) {
    return selectedRegions.add(point);
  }

  public boolean deselectRegion(Point point) {
    return selectedRegions.remove(point);
  }

  public void clearActivePoints() {
    selectedRegions.clear();
  }

}
