package com.riskrieg.mapeditor.util;

import java.awt.Point;

public class MathUtil {

  public static double distance(Point one, Point two) {
    int dx = two.x - one.x;
    int dy = two.y - one.y;
    return Math.sqrt((dx * dx) + (dy * dy));
  }

}
