package com.riskrieg.mapeditor.model.map.graph;

import java.awt.Point;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Territory implements Comparable<Territory> { // TODO: Convert to Record when Java 16 comes out

  private final String name;
  private final Set<Point> seedPoints;

  public Territory(String name, Set<Point> seedPoints) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(seedPoints);
    if (seedPoints.isEmpty()) {
      throw new IllegalStateException("seedPoints must not be empty");
    }
    this.name = name;
    this.seedPoints = seedPoints;
  }

  public Territory(String name, Point seedPoint) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(seedPoint);
    this.name = name;
    this.seedPoints = new HashSet<>();
    this.seedPoints.add(seedPoint);
  }

  public String name() {
    return name;
  }

  public Set<Point> seedPoints() {
    return seedPoints;
  }

  @Override
  public int compareTo(Territory o) {
    return this.name().compareTo(o.name());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Territory territory = (Territory) o;
    return Objects.equals(name, territory.name) &&
        Objects.equals(seedPoints, territory.seedPoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, seedPoints);
  }

  @Override
  public String toString() {
    return name;
  }

}
