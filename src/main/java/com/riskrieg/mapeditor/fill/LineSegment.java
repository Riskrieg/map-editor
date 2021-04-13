package com.riskrieg.mapeditor.fill;

public class LineSegment {

  private int xL, xR, y, dy;

  public LineSegment(int xL, int xR, int y, int dy) {
    this.xL = xL;
    this.xR = xR;
    this.y = y;
    this.dy = dy;
  }

  public void incrementXL() {
    this.xL = xL + 1;
  }

  public int x1() {
    return xL;
  }

  public int x2() {
    return xR;
  }

  public int y() {
    return y;
  }

  public int dy() {
    return dy;
  }

}
