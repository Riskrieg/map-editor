package com.riskrieg.mapeditor.map.graph;

import java.util.Objects;

public class Edge { // TODO: Turn into Record in Java 16

  private final Territory source;
  private final Territory target;

  public Edge(Territory source, Territory target) {
    Objects.requireNonNull(source);
    Objects.requireNonNull(target);
    this.source = source;
    this.target = target;
  }

  public Territory target() {
    return target;
  }

  public Territory source() {
    return source;
  }

}
