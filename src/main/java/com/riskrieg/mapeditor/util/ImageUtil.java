package com.riskrieg.mapeditor.util;

import com.riskrieg.mapeditor.fill.Fill;
import com.riskrieg.mapeditor.fill.MilazzoFill;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import javax.imageio.ImageIO;

public class ImageUtil {

  public static Point getRootPixel(BufferedImage image, Point point) {
    Point origin = new Point(0, 0);
    Point result = new Point(point.x, point.y);
    boolean[][] hits = new boolean[image.getHeight()][image.getWidth()];
    Queue<Point> queue = new LinkedList<>();
    queue.add(point);
    double shortestDistance = MathUtil.distance(point, origin);

    while (!queue.isEmpty()) {
      Point p = queue.remove();
      if (floodDo(image, hits, p.x, p.y, image.getRGB(point.x, point.y), image.getRGB(point.x, point.y))) {
        queue.add(new Point(p.x - 1, p.y));
        queue.add(new Point(p.x + 1, p.y));
        queue.add(new Point(p.x, p.y - 1));
        queue.add(new Point(p.x, p.y + 1));
      }
    }

    for (int x = 0; x < hits.length; x++) {
      for (int y = 0; y < hits[x].length; y++) {
        if (hits[x][y]) {
          double distanceToOrigin = MathUtil.distance(new Point(x, y), origin);
          if (distanceToOrigin < shortestDistance) {
            shortestDistance = distanceToOrigin;
            result = new Point(y, x);
          }
        }
      }
    }

    return result;
  }

  public static Color getPixelColor(BufferedImage image, Point point) {
    int clr = image.getRGB(point.x, point.y);
    int red = (clr & 0x00ff0000) >> 16;
    int green = (clr & 0x0000ff00) >> 8;
    int blue = clr & 0x000000ff;
    return new Color(red, green, blue);
  }

  public static BufferedImage bucketFill(BufferedImage image, Point point, Color newColor) {
    Fill fill = new MilazzoFill(image, new Color(image.getRGB(point.x, point.y)), newColor);
    fill.fill(point);
    return fill.getImage();
  }

  private static boolean floodDo(BufferedImage image, boolean[][] hits, int x, int y, int oldColor, int newColor) {
    if (x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight() && image.getRGB(x, y) == oldColor && !hits[y][x]) {
      image.setRGB(x, y, newColor);
      hits[y][x] = true;
      return true;
    } else {
      return false;
    }
  }

  public static BufferedImage convert(BufferedImage image, int imageType) {
    BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
    Graphics g = result.getGraphics();
    g.drawImage(image, 0, 0, null);
    return result;
  }

  public static BufferedImage convert(File file, int imageType) throws IOException {
    BufferedImage image = ImageIO.read(file);
    BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
    Graphics g = result.getGraphics();
    g.drawImage(image, 0, 0, null);
    return result;
  }

  public static BufferedImage convert(String imagePath, int imageType) throws IOException {
    BufferedImage image = ImageIO.read(new File(imagePath));
    BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
    Graphics g = result.getGraphics();
    g.drawImage(image, 0, 0, null);
    return result;
  }

}
