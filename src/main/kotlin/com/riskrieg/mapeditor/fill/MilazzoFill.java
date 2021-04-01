package com.riskrieg.mapeditor.fill;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * This fill algorithm was adapted from Adam Milazzo's algorithm here: http://www.adammil.net/blog/v126_A_More_Efficient_Flood_Fill.html
 */
public class MilazzoFill implements Fill {

  private BufferedImage image;
  private final int original;
  private final int fill;
  private final int imgWidth;
  private final int imgHeight;

  public MilazzoFill(BufferedImage image, Color original, Color fill) {
    this.image = image;
    this.original = original.getRGB();
    this.fill = fill.getRGB();
    this.imgWidth = image.getWidth();
    this.imgHeight = image.getHeight();

  }

  public BufferedImage getImage() {
    return image;
  }

  public void fill(Point seed) {
    if (original == fill) {
      return;
    }
    if (canPaint(seed.x, seed.y)) {
      _fill(seed.x, seed.y, imgWidth, imgHeight);
    }
  }

  /**
   * This method moves the "cursor" to the top-left-most position that it can and then proceeds to fill from there.
   * @param x The x coordinate to start at.
   * @param y The y coordinate to start at.
   * @param width The width of the image.
   * @param height The height of the image.
   */
  private void _fill(int x, int y, int width, int height) { // Move to the upper left-most spot before filling.
    while(true)
    {
      int ox = x, oy = y;
      while(y != 0 && canPaint(x, y-1)) y--;
      while(x != 0 && canPaint(x-1, y)) x--;
      if(x == ox && y == oy) break;
    }
    fillCore(x, y, width, height);
  }

  /**
   * This method fills entire rectangular blocks at a time, making for relatively few pixel color tests compared to other methods.
   * @param x The x coordinate to start filling at.
   * @param y The y coordinate to start filling at.
   * @param width The width of the image.
   * @param height The height of the image.
   */
  private void fillCore(int x, int y, int width, int height) {
    int lastRowLength = 0;
    do {
      int rowLength = 0, sx = x;
      if (lastRowLength != 0 && !canPaint(x, y)) {
        do {
          if (--lastRowLength == 0) {
            return;
          }
        } while (!canPaint(++x, y));
        sx = x;
      } else {
        for (; x != 0 && canPaint(x - 1, y); rowLength++, lastRowLength++) {
          setPixel(--x, y);
          if (y != 0 && canPaint(x, y - 1)) {
            _fill(x, y - 1, width, height);
          }
        }
      }

      for (; sx < width && canPaint(sx, y); rowLength++, sx++) {
        setPixel(sx, y);
      }

      if (rowLength < lastRowLength) {
        for (int end = x + lastRowLength; ++sx < end; ) {
          if (canPaint(sx, y)) {
            fillCore(sx, y, width, height);
          }
        }
      } else if (rowLength > lastRowLength && y != 0) {
        for (int ux = x + lastRowLength; ++ux < sx; ) {
          if (canPaint(ux, y - 1)) {
            _fill(ux, y - 1, width, height);
          }
        }
      }
      lastRowLength = rowLength;
    } while (lastRowLength != 0 && ++y < height);
  }

  /* Private Methods */

  private boolean canPaint(int x, int y) {
    return getPixel(x, y) == original;
  }

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
