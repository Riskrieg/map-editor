package com.riskrieg.mapeditor.model.map.alignment;

public class InterfaceAlignment { // TODO: Turn into Record in Java 16

  private final HorizontalAlignment horizontal;
  private final VerticalAlignment vertical;

  public InterfaceAlignment(HorizontalAlignment horizontal, VerticalAlignment vertical) {
    this.horizontal = horizontal;
    this.vertical = vertical;
  }

  public HorizontalAlignment horizontal() {
    return horizontal;
  }

  public VerticalAlignment vertical() {
    return vertical;
  }

}
