package com.riskrieg.mapeditor.fill;

import java.awt.Point;
import java.awt.image.BufferedImage;

public interface Fill {

  BufferedImage getImage();

  void fill(Point seed);

}
