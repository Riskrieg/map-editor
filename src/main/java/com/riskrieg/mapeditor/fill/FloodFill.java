package com.riskrieg.mapeditor.fill;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

public class FloodFill implements Fill {

  private BufferedImage image;
  private final int original;
  private final int fill;
  private final int imgWidth;
  private final int imgHeight;

  public FloodFill(BufferedImage image, Color original, Color fill) {
    this.image = image;
    this.original = original.getRGB();
    this.fill = fill.getRGB();
    this.imgWidth = image.getWidth();
    this.imgHeight = image.getHeight();
  }

  @Override
  public BufferedImage getImage() {
    return image;
  }

  public void fill(Point seed) {
    if (original == fill) {
      return;
    }

    Queue<Point> queue = new LinkedList<>();

    if (getPixel(seed.x, seed.y) == original) {
      queue.add(seed);

      while (!queue.isEmpty()) {
        Point n = queue.poll();
        if (getPixel(n.x, n.y) == original) {
          int wx = n.x;
          int ex = n.x + 1;

          while (wx >= 0 && getPixel(wx, n.y) == original) {
            wx--;
          }

          while (ex <= imgWidth - 1 && getPixel(ex, n.y) == original) {
            ex++;
          }

          for (int ix = wx + 1; ix < ex; ix++) {
            setPixel(ix, n.y);

            if (n.y - 1 >= 0 && getPixel(ix, n.y - 1) == original) {
              queue.add(new Point(ix, n.y - 1));
            }

            if (n.y + 1 < imgHeight && getPixel(ix, n.y + 1) == original) {
              queue.add(new Point(ix, n.y + 1));
            }
          }

        }
      }

    }
  }

  /* Private Methods */

  private Color getColor(int x, int y) {
    return new Color(image.getRGB(x, y));
  }

  private int getPixel(int x, int y) {
    return image.getRGB(x, y);
  }

  private void setPixel(int x, int y) {
    image.setRGB(x, y, fill);
  }


}
