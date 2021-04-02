package com.riskrieg.mapeditor.fill;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

public class BasicFill implements Fill {

  private BufferedImage image;
  private final Color original;
  private final Color fill;
  private final int imgWidth;
  private final int imgHeight;

  public BasicFill(BufferedImage image, Color original, Color fill) {
    this.image = image;
    this.original = original;
    this.fill = fill;
    this.imgWidth = image.getWidth();
    this.imgHeight = image.getHeight();
  }

  @Override
  public BufferedImage getImage() {
    return image;
  }

  public void fill(Point seed) {
    boolean[][] visited = new boolean[imgHeight][imgWidth];
    Queue<Point> queue = new LinkedList<>();
    queue.add(new Point(seed.x, seed.y));

    while (!queue.isEmpty()) {
      Point p = queue.remove();
      if (scan(visited, p.x, p.y)) {
        setPixel(p.x, p.y);
        queue.add(new Point(p.x - 1, p.y));
        queue.add(new Point(p.x + 1, p.y));
        queue.add(new Point(p.x, p.y - 1));
        queue.add(new Point(p.x, p.y + 1));
      }
    }
  }

  /* Private Methods */

  private boolean scan(boolean[][] visited, int x, int y) {
    if (x >= 0 && y >= 0 && x < imgWidth && y < imgHeight && image.getRGB(x, y) == original.getRGB() && !visited[y][x]) {
      visited[y][x] = true;
      return true;
    } else {
      return false;
    }
  }

  private Color getColor(int x, int y) {
    return new Color(image.getRGB(x, y));
  }

  private int getPixel(int x, int y) {
    return image.getRGB(x, y);
  }

  private void setPixel(int x, int y) {
    image.setRGB(x, y, fill.getRGB());
  }

}
