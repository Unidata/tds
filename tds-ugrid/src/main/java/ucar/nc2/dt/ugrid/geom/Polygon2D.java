/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * Polygon2D.java
 *
 * Created on May 12, 2009 @ 12:39:26 PM
 */

package ucar.nc2.dt.ugrid.geom;


import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.FloatArrayList;

import ucar.nc2.dt.ugrid.utils.AsaArrayUtils;



/**
 * NOTE: This class should NOT be used in situations involving geographic (lat, lon) data. In such cases, use
 * LatLonPolygon2D instead.
 * 
 * @author CBM <cmueller@asascience.com>
 * @modified TPL <tlarocque@asascience.com>
 */
public abstract class Polygon2D implements Shape, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 6512233111784242139L;

  /*
   * TODO: check the contains methods and make sure
   * they are 100% accurate; previous versions of them were not.
   */


  /**
   * The current number of coordinates
   */
  protected int _coordCount = 0;

  /**
   * The flag that says that the polygon has been closed
   */
  protected boolean _closed = false;



  /**
   * Calculates the <code>double</code> area of the given polygon.
   * 
   * @param poly
   *        a <code>Polygon2D</code> object
   * 
   * @return the <code>double</code> area
   */
  public static double area(Polygon2D poly) {
    /*
     * This formula calculates area by halving the determinant of a
     * 2 by n+1 matrix of the polygons vertices, where n is the number
     * of points (nodes) of that polygon. The calculation appears as follows:
     * 
     * | X1 Y1 |
     * Area = 1/2 * | X2 Y2 | = 1/2 * [(X1Y2 + X2Yn + ... + XnY1)-(Y1X2 + Y2Xn + ... + YnX1)]
     * | Xn Yn |
     * | X1 Y1 |
     * 
     * @see http://www.mathwords.com/a/area_convex_polygon.htm
     * 
     * @see http://2000clicks.com/mathhelp/GeometryPolygonAreaDeterminant.htm
     */


    if (poly.getVertexCount() < 3) {
      return 0;
    }


    double result = 0.0;
    double xy, yx;
    int lastNodeIndex = poly.getVertexCount() - 1;

    for (int i = 0; i < lastNodeIndex; i++) {
      xy = poly.getX(i) * poly.getY(i + 1);
      yx = poly.getY(i) * poly.getX(i + 1);

      result += (xy - yx);
    }

    /* The last point must also be the first... add this in as well */
    xy = poly.getX(lastNodeIndex) * poly.getY(0);
    yx = poly.getY(lastNodeIndex) * poly.getX(0);
    result += (xy - yx);



    return Math.abs(result * 0.5);
  }


  /**
   * Checks if the line segment drawn from p0 to p1 intersects the line drawn
   * from p2 to p3. This is an optimized variation of
   * {@link Line2D#linesIntersect(double, double, double, double, double, double, double, double)
   * 
   * @param p0x
   *        the first points x value
   * @param p0y
   *        the first points y value
   * @param p1x
   *        the second points x value
   * @param p1y
   *        the second point's y value
   * @param p2x
   *        the third point's x value
   * @param p2y
   *        the third point's y value
   * @param p3x
   *        the fourth point's x value
   * @param p3y
   *        the fourth point's y value
   * 
   * @return <code>true</code> if the lines created from the given points
   *         intersect, otherwise <code>false</code>
   */
  public static boolean linesIntersect(double p0x, double p0y, double p1x, double p1y, double p2x, double p2y,
      double p3x, double p3y) {
    boolean result = false;


    /*
     * Calculate the barycentric coordinates (a1, a2, a3) of the
     * triangle given by p1, p2, p3 with respect to the point p0. This
     * formula is a breakdown of matrix multiplications. see
     * http://www.crackthecode.us/barycentric/barycentric_coordinates.html
     * for barycentric coordinate calculations
     */
    double a1 = ((p0x * p2y) - (p0x * p3y) - (p2x * p0y) + (p2x * p3y) + (p3x * p0y) - (p3x * p2y))
        / ((p1x * p2y) - (p1x * p3y) - (p2x * p1y) + (p2x * p3y) + (p3x * p1y) - (p3x * p2y));
    double a2 = ((p1x * p0y) - (p1x * p3y) - (p0x * p1y) + (p0x * p3y) + (p3x * p1y) - (p3x * p0y))
        / ((p1x * p2y) - (p1x * p3y) - (p2x * p1y) + (p2x * p3y) + (p3x * p1y) - (p3x * p2y));
    // double a3 = ((p1x * p2y) - (p1x * p0y) - (p2x * p1y) + (p2x * p0y) +
    // (p0x * p1y) - (p0x * p2y))
    // / ((p1x * p2y) - (p1x * p3y) - (p2x * p1y) + (p2x * p3y) + (p3x *
    // p1y) - (p3x * p2y));



    /*
     * For purposes of debugging; a1 + a2 + a3 should ALWAYS == 1.
     * However, due to floating point precision issues this calculation
     * may be close, but not exact. A floating precision of 3 may be
     * sufficient to check these values during debugging
     */
    // if (Math.round((a1 + a2 + a3) * 1000) / 1000d != 1d) {
    // System.out.println("ERROR in coordinate calculations : " + a1 + " "
    // + a2 + " " + a3);
    // }
    /**
     * According to the 2D Segments Intersection test given by figure 5 page 4 at
     * http://wscg.zcu.cz/WSCG2004/Papers_2004_Full/B83.pdf,
     * if the sign of the barycentric coordinates of p1, p2, and p3 with respect to p4 are a1<=0, a2>=0, and a3>=0 then
     * the line
     * segments given by (p1, p4) and (p2, p3) intersect.
     * 
     * NOTE: To save processing time, a3 is not calculated. Since a1 + a2 + a3 = 1 it can be said that a3 = 1 - a1 - a2.
     * Additionally,
     * for this check to be true a3 must be greater than or equal to 1. Instead of writing a3 >= 1 we can check that a1
     * + a2 <= 1
     */
    // if (a1 <= 0 && a2 >= 0 && a3 >= 0) {
    if (a1 <= 0 && a2 >= 0 && a1 + a2 <= 1) {
      result = true;
    }


    return result;
  }


  /**
   * Checks if the point given by p1x, p1y is intersected by the line segment given by e0, e1
   * 
   * @param e0x
   *        the line segment's first endpoint's x value
   * @param e0y
   *        the line segment's first endpoint's y value
   * @param e1x
   *        the line segment's second endpoint's x value
   * @param e1y
   *        the line segment's second endpoint's y value
   * @param p1x
   *        the point's x value
   * @param p1y
   *        the point's y value
   * 
   * @return <code>true</code> if the point is intersected by the given line segment, otherwise <code>false</code>
   */
  public static boolean lineIntersectsPoint(double e0x, double e0y, double e1x, double e1y, double p1x, double p1y) {
    /**
     * The following calculation normalizes all points (p1, e0, e1) over e0 and checks if vector e1 is divisible by p1
     * via:
     * 
     * p1x / p1y == e1x / e1y
     * 
     * Which can be rewritten as:
     * 
     * e1x / p1x * p1y == e1y
     * 
     * At this point, it is understood that p1 is intersected by the infinitely extending line given by e0, e1. Simply
     * checking if p1 is
     * between e0 and e1 will determine if the given point is intersected by the line segment given by e0, e1
     */
    if ((e1x - e0x) / (p1x - e0x) * (p1y - e0y) + e0y == e1y) {
      /*
       * Check that p's x value is between e0x and e1x
       * 
       * The only way p1x could be outside the range from e0x to e1x is if
       * (p1x < e0x <= e1x) or if (p1x > e0x >= e1x) since p1x is inside the range
       * if (e0x >= p1x >= e1x) or if (e0x <= p1x <= e1x)
       */
      if (p1x > e0x && p1x > e1x) {
        return false;
      }
      if (p1x < e0x && p1x < e1x) {
        return false;
      }


      /*
       * Check that p's y value is between e0y and e1y
       * 
       * The only way p1y could be outside the range from e0y to e1y is if
       * (p1y < e0y <= e1y) or if (p1y > e0y >= e1y) since p1y is inside the range
       * if (e0y >= p1y >= e1y) or if (e0y <= p1y <= e1y)
       */
      if (p1y > e0y && p1y > e1y) {
        return false;
      }
      if (p1y < e0y && p1y < e1y) {
        return false;
      }


      /* If all the above checks passed.. */
      return true;


    } /* ELSE return false */


    return false;


  }



  /**
   * Close the polygon. No further segments can be added. If this method not called, then the path iterators will treat
   * the polygon as
   * thought it were closed, and implicitly join the most recently added vertex to the first one. However, this method
   * should generally be
   * called, as if the last vertex is the same as the first vertex, then it merges them.
   */
  public void closePath() {
    if ((getX(getVertexCount() - 1) == getX(0)) && (getY(getVertexCount() - 1) == getY(0))) {
      _coordCount -= 2;
    }

    _closed = true;
  }



  /**
   * Return true if the given point is inside the polygon. This method uses a straight-forward algorithm, where a point
   * is taken to be
   * inside the polygon if a horizontal line extended from the point to infinity intersects an odd number of segments.
   */
  public boolean contains(double x, double y) {

    int crossings = 0;
    int count = getVertexCount();


    /*
     * If count == 0 -- A point cannot be contained in nothingness...
     * 
     * If count == 1 -- Since this is not an inclusive check, if there is
     * only 1 vertex in this polygon, it cannot contain another point, even
     * if the coordinates of those points are the same
     * 
     * If count == 2 -- Since this is not an inclusive check, a line segment
     * cannot contain the given point either
     */
    if (count < 3) {
      return false;
    }


    // Iterate over all vertices
    int i = 1;

    for (; i < count;) {
      double x1 = getX(i - 1);
      double x2 = getX(i);
      double y1 = getY(i - 1);
      double y2 = getY(i);

      // Crossing if lines intersect
      if ((x < x1) || (x < x2)) {
        if (y == y2) {
          crossings++;
        } else if (y == y1) {
          // do nothing, so that two adjacent segments
          // don't both get counted
        } else if (Line2D.linesIntersect(x, y, Math.max(x1, x2), y, x1, y1, x2, y2)) {
          crossings++;
        }
      }

      i++;
    }

    /* Final segment (always check this, even if this poly is a line segment) */

    double x1 = getX(count - 1);
    double x2 = getX(0);
    double y1 = getY(count - 1);
    double y2 = getY(0);

    // Crossing if lines intersect
    if ((x < x1) || (x < x2)) {
      if (Line2D.linesIntersect(x, y, Math.max(x1, x2), y, x1, y1, x2, y2) && (y != y1)) {
        crossings++;
      }
    }



    // True if odd number of crossings
    return (crossings % 2) == 1;
  }



  /**
   * Return true if the given point is inside the polygon.
   */
  public boolean contains(Point2D p) {
    return contains(p.getX(), p.getY());
  }



  /**
   * Return true if the given rectangle is entirely inside the polygon. (Currently, this algorithm can be fooled by
   * supplying a rectangle
   * that has all four corners inside the polygon, but which intersects some edges.)
   */
  public boolean contains(Rectangle2D r) {
    /* TODO: fix this javadoc once contains(double, double, double, double) is corrected */
    return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }



  /**
   * Return true if the given rectangle is entirely inside the polygon. (Currently, this algorithm can be fooled by
   * supplying a rectangle
   * that has all four corners inside the polygon, but which intersects some edges.)
   */
  public boolean contains(GeoRectangle r) {
    /* TODO: fix this javadoc once contains(Polygon2D) is corrected */
    double x1 = r.getMinX();
    double x2 = r.getMaxX();
    double y1 = r.getMinY();
    double y2 = r.getMaxY();

    return contains(x1, y1) && contains(x1, y2) && contains(x2, y1) && contains(x2, y2);
    // return contains(r.getBackingPolygon());


    /* TODO: use the above coords x1, x2, y1, y2; to check that there are NO line intersections */
  }


  /**
   * Determines if the given polygon is fully contained within this polygon.<br />
   * <br />
   * This method can be fooled by suppling a polygon whose points are completly contained in this polygon but has some
   * intersecting lines.
   * In this instance, although the polygons intersect the given polygon is not "wholely" contained within this polygon.
   * This method would
   * still return <code>true</code>.
   * 
   * 
   * @param p
   *        a <code>Polygon2D</code> object
   * 
   * @return <code>true</code> if all the points of the given polygon are contained within this Polygon2D, otherwise
   *         <code>false</code>
   */
  public boolean contains(Polygon2D p) {
    /* FIXME: this method can be fooled, read the javadoc above */
    boolean result = true;

    for (int ctr = 0; ctr < p.getVertexCount(); ctr++) {
      if (!contains(p.getX(ctr), p.getY(ctr))) {
        result = false;
        break;
      }
    }

    /* TODO: insure there are no intersecting lines */

    return result;
  }


  /**
   * Creates a copy of this polygon with the same coordinates. The original polygon remains unchanged.
   * 
   * @return a copy of the this polygon
   */
  public abstract Polygon2D copy();


  /**
   * Return true if the given rectangle is entirely inside the polygon. (Currently, this algorithm can be fooled by
   * supplying a rectangle
   * that has all four corners inside the polygon, but which intersects some edges.)
   */
  public boolean contains(double x1, double y1, double w, double h) {
    /* FIXME: this method can be fooled, read the javadoc above */
    double x2 = x1 + w;
    double y2 = y1 + h;
    return contains(x1, y1) && contains(x1, y2) && contains(x2, y1) && contains(x2, y2);
  }



  /**
   * Calculates the area of this <code>Polygon2D</code>
   * 
   * @return the <code>double</code> area of this polygon
   */
  public double getArea() {
    return area(this);
  }


  /**
   * Get the integer {@link Rectangle} bounds of the polygon.
   */
  public Rectangle getBounds() {
    return getBounds2D().getBounds();
  }



  /**
   * Get the floating-point bounds of the polygon.
   */
  public abstract Rectangle2D getBounds2D();



  /**
   * Get the bound box of this poly as a <code>GeoRectangle2D</code>
   */
  public abstract GeoRectangle getBoundsGeo();


  /**
   * Calculates the center point of this <code>Polygon2D</code>
   * 
   * @return the center point of this <code>Polygon2D</code>
   */
  public abstract Point2D getCentroid();


  /**
   * Calculates the square of the distance from the point <code>p</code> to the nearest line segment specified by
   * connecting the vertices
   * of this <code>Polygon2D</code>
   * 
   * @param p
   *        a <code>Point2D</code>
   * 
   * @return the square of the distance from the given point to this <code>Polygon2D</code>
   */
  public double distanceSq(Point2D p) {
    double resultSq = java.lang.Double.POSITIVE_INFINITY;
    double tempDistSq = 0.0;

    if (getVertexCount() <= 0) {

      throw new IllegalStateException("No vertices set");

    } else if (getVertexCount() == 1) {

      resultSq = p.distanceSq(getX(0), getY(0));

    } else {

      if (contains(p)) {
        /* If the point is inside this poly, the dist is 0 */
        return 0;
      }

      /* Check the line segments from each vertex */
      for (int ctr = 0; ctr < getVertexCount() - 1; ctr++) {
        tempDistSq = Line2D.ptSegDistSq(getX(ctr), getY(ctr), getX(ctr + 1), getY(ctr + 1), p.getX(), p.getY());
        if (tempDistSq < resultSq) {
          resultSq = tempDistSq;
        }
      }

      /* Finaly check the line segment from index 0 to the last index */
      tempDistSq = Line2D.ptSegDistSq(getX(0), getY(0), getX(getVertexCount() - 1), getY(getVertexCount() - 1),
          p.getX(), p.getY());
      if (tempDistSq < resultSq) {
        resultSq = tempDistSq;
      }

    }


    return resultSq;
  }



  /**
   * Calculates the square of the distance from the point <code>p</code> to the nearest line segment specified by
   * connecting the vertices
   * of this <code>Polygon2D</code>
   * 
   * @param p
   *        a <code>Point2D</code>
   * 
   * @return the square of the distance from the given point to this <code>Polygon2D</code>
   */
  public double distance(Point2D p) {
    return Math.sqrt(distanceSq(p));
  }


  /**
   * Gets a double array containing the X coordinates of the polygon.
   * 
   * @return the X coordinates
   */
  public abstract double[] getXCoords();


  /**
   * Gets a double array containing the Y coordinates of the polygon.
   * 
   * @return the Y coordinates
   */
  public abstract double[] getYCoords();

  /**
   * Gets a list of <tt>Point2D</tt> vertices of this polygon.
   * 
   * @return a <tt>List{Point2D}</tt> of vertices
   */
  public abstract List<Point2D> getVertices();

  /**
   * Determines the lowest x value within the points of this polygon
   * 
   * @return the lowest x value
   */
  public double getMinX() {
    double min = java.lang.Double.POSITIVE_INFINITY;
    int count = getVertexCount();

    for (int ctr = 0; ctr < count; ctr++) {
      min = Math.min(min, getX(ctr));
    }

    return min;
  }



  /**
   * Determines the lowest y value within the points of this polygon
   * 
   * @return the lowest y value
   */
  public double getMinY() {
    double min = java.lang.Double.POSITIVE_INFINITY;
    int count = getVertexCount();

    for (int ctr = 0; ctr < count; ctr++) {
      min = Math.min(min, getY(ctr));
    }

    return min;
  }



  /**
   * Determines the highest x value within the points of this polygon
   * 
   * @return the highest x value
   */
  public double getMaxX() {
    double max = java.lang.Double.NEGATIVE_INFINITY;
    int count = getVertexCount();

    for (int ctr = 0; ctr < count; ctr++) {
      max = Math.max(max, getX(ctr));
    }

    return max;
  }



  /**
   * Determines the highest x value within the points of this polygon
   * 
   * @return the highest x value
   */
  public double getMaxY() {
    double max = java.lang.Double.NEGATIVE_INFINITY;
    int count = getVertexCount();

    for (int ctr = 0; ctr < count; ctr++) {
      max = Math.max(max, getY(ctr));
    }

    return max;
  }



  /**
   * Get a path iterator over the object.
   */
  public PathIterator getPathIterator(AffineTransform at, double flatness) {
    return getPathIterator(at);
  }

  /**
   * Get a path iterator over the object.
   */
  public PathIterator getPathIterator(AffineTransform at) {
    return new PolygonIterator(this, at);
  }

  /**
   * Get the number of vertices
   */
  public int getVertexCount() {

    return (int) (_coordCount * 0.5);
  }

  /**
   * Get the given X-coordinate
   * 
   * @exception IndexOutOfBoundsException
   *            The index is out of bounds.
   */
  public abstract double getX(int index);


  /**
   * Get the given Y-coordinate
   * 
   * @exception IndexOutOfBoundsException
   *            The index is out of bounds.
   */
  public abstract double getY(int index);


  /**
   * Checks if the line segment given by the extremes <code>e1</code> and <code>e2</code> intersect this polygon, by
   * checking segment to
   * segment intersection for each vector of this polygon. [TPL]
   */
  public boolean intersects(Point2D e0, Point2D e1) {

    int count = getVertexCount();

    if (count == 0) {
      return false;
    }

    if (count == 1) {
      /* Treat this polygon as a point */
      return lineIntersectsPoint(e0.getX(), e0.getY(), e1.getX(), e1.getY(), this.getX(0), this.getY(0));
    }


    /* Iterate through each line segment made by the points of this polygon */
    double p0x, p0y, p1x, p1y;
    double p2x, p2y, p3x, p3y;
    p0x = e0.getX();
    p0y = e0.getY();
    p1x = e1.getX();
    p1y = e1.getY();


    for (int i = 1; i < count; i++) {
      p2x = getX(i - 1);
      p2y = getY(i - 1);

      p3x = getX(i);
      p3y = getY(i);

      if (Line2D.linesIntersect(p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y)) {
        return true;
      }
    }


    /*
     * If this polygon has more than two vertices finalize the connection and
     * check the last line segment...
     */
    if (count > 2) {
      p2x = getX(0);
      p2y = getY(0);

      p3x = getX(count - 1);
      p3y = getY(count - 1);

      if (Line2D.linesIntersect(p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y)) {
        return true;
      }
    }


    return false;
  }


  /**
   * Checks if this polygon intersects the given polygon. This algorithm uses the 2D Polygon-Polygon Intersection test
   * as proposed in the
   * webaddress below
   * 
   * @return <code>true</code> if there is an intersection, otherwise <code>false</code>
   * 
   * @see <a href="http://wscg.zcu.cz/WSCG2004/Papers_2004_Full/B83.pdf"> Efficient Collision Detection between 2D
   *      Polygons</a>
   */
  public boolean intersects(Polygon2D poly) {


    /** Preliminary (shortcut) checks */
    /**********************************/
    if (this == poly) {
      return true;
    }


    int countPoly1 = this.getVertexCount();
    int countPoly2 = poly.getVertexCount();


    /* If either polygon does not have vertices... */
    if (countPoly1 == 0 || countPoly1 == 0) {
      return false;
    }

    /*
     * If either polygon only has 1 vertex; treat it as a point
     */
    if (countPoly1 == 1) {
      return poly.intersects(new Point2D.Double(this.getX(0), this.getY(0)));
    }
    if (countPoly2 == 1) {
      return this.intersects(new Point2D.Double(poly.getX(0), poly.getY(0)));
    }

    /*
     * Test if the bounding circumferences of the polygons intersect. (This
     * is the most commonly tripped exclusion check)
     * 
     * NOTE: the circle given by c1, r1 and c1, r2 are the circles surrounding
     * the bounding rectangles of the polygons. This is not exactly the same as
     * the surrounding circumferences of the polygons, but is accurate
     * enough for exclusion testing
     * 
     * NOTE: for grabbing the centroid use the center point of the
     * bounding rectangle instead of the actual polygon
     */
    Point2D c1 = this.getBoundsGeo().getBackingPolygon().getCentroid();
    Point2D c2 = poly.getBoundsGeo().getBackingPolygon().getCentroid();

    double r1 = c1.distance(this.getMinX(), this.getMinY());
    double r2 = c2.distance(poly.getMinX(), poly.getMinY());

    if (c1.distance(c2) > r1 + r2) { /* Too far away to intersect */
      return false;
    }

    /*
     * If either polygon contains the centroid of the other, there is an
     * intersection. (This is the most commonly tripped inclusion check)
     */
    if (this.intersects(c2)) {
      return true;
    }

    if (poly.intersects(c1)) {
      return true;
    }



    /** Full intersection checking */
    /*******************************/
    Point2D p1, p2;

    /*
     * Check for the intersection of any line segment of the given polygon
     * with this polygon
     */
    for (int i = 1; i < countPoly2; i++) {
      p1 = new Point2D.Double(poly.getX(i - 1), poly.getY(i - 1));
      p2 = new Point2D.Double(poly.getX(i), poly.getY(i));

      if (intersects(p1, p2)) {
        return true;
      }
    }

    /*
     * Don't forget the line segment from index[size()] to index[0]
     * Only check this if the polygon has more than 2 vertices (is not a line segment)
     */
    if (countPoly2 > 2) {
      p1 = new Point2D.Double(poly.getX(countPoly2 - 1), poly.getY(countPoly2 - 1));
      p2 = new Point2D.Double(poly.getX(0), poly.getY(0));

      if (intersects(p1, p2)) {
        return true;
      }
    }

    return false;
  }


  /**
   * Test if the polygon is intersected by the given rectangle. (Currently, this algorithm can be fooled by supplying a
   * rectangle that has
   * no corners inside the polygon, and does not contain any vertex of the polygon, but which intersects some edges.)
   */
  public boolean intersects(Rectangle2D r) {
    return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }


  /**
   * Test if the polygon is intersected by the given GeoRectangle. (Currently, this algorithm can be fooled by supplying
   * a rectangle that
   * has no corners inside the polygon, and does not contain any vertex of the polygon, but which intersects some
   * edges.)
   */
  public boolean intersects(GeoRectangle r) {
    /* use minx maxy as the starting point (upper left corner) */
    // return intersects(r.getMinX(), r.getMaxY(), r.getWidth(),
    // r.getHeight());
    return intersects(r.getBackingPolygon());
  }


  /**
   * Test if the polygon is intersected by the given rectangle.
   */
  public boolean intersects(double x1, double y1, double w, double h) {
    double x2 = x1 + w;
    double y2 = y1 + h;
    int count = getVertexCount();

    if (count == 0) {
      return false;
    }

    // If the bounds don't intersect, then return false.
    Rectangle2D rect = new Rectangle2D.Double(x1, y1, w, h);

    if (!getBounds().intersects(rect)) {
      return false;
    }

    // return true if the polygon contains any vertex of the rectangle.
    if (contains(x1, y1) || contains(x1, y2) || contains(x2, y1) || contains(x2, y2)) {
      return true;
    }

    // return true if the rectangle contains any vertex of the polygon
    for (int i = 0; i < getVertexCount(); i++) {
      if (rect.contains(getX(i), getY(i))) {
        return true;
      }
    }

    // return true if any line segment of the polygon crosses a line
    // segment of the rectangle.
    // This is rather long, I wonder if it could be optimized.
    // Iterate over all vertices
    for (int i = 1; i < count; i++) {
      double vx1 = getX(i - 1);
      double vx2 = getX(i);
      double vy1 = getY(i - 1);
      double vy2 = getY(i);

      if (Line2D.linesIntersect(x1, y1, x1, y2, vx1, vy1, vx2, vy2)) {
        return true;
      }

      if (Line2D.linesIntersect(x1, y2, x2, y2, vx1, vy1, vx2, vy2)) {
        return true;
      }

      if (Line2D.linesIntersect(x2, y2, x2, y1, vx1, vy1, vx2, vy2)) {
        return true;
      }

      if (Line2D.linesIntersect(x2, y1, x1, y1, vx1, vy1, vx2, vy2)) {
        return true;
      }
    }

    /*
     * Checks the final line segment from index 0 to index "size()" if there
     * are more than 2 vertices (the polygon is not a line segment)
     */
    if (count > 2) {
      double vx1 = getX(count - 1);
      double vx2 = getX(0);
      double vy1 = getY(count - 1);
      double vy2 = getY(0);

      if (Line2D.linesIntersect(x1, y1, x1, y2, vx1, vy1, vx2, vy2)) {
        return true;
      }

      if (Line2D.linesIntersect(x1, y2, x2, y2, vx1, vy1, vx2, vy2)) {
        return true;
      }

      if (Line2D.linesIntersect(x2, y2, x2, y1, vx1, vy1, vx2, vy2)) {
        return true;
      }

      if (Line2D.linesIntersect(x2, y1, x1, y1, vx1, vy1, vx2, vy2)) {
        return true;
      }
    }


    return false;
  }


  /**
   * Inclusively checks if the given point is contained within this polygon or intersected by any of its line-segments
   * 
   * @param p
   *        a <code>Point2D</code>
   * 
   * @return <code>true</code> if the given point is intersected by this <code>Polygon2D</code>, otherwise
   *         <code>false</code>
   */
  public boolean intersects(Point2D p) {

    /* If the point is contained it also intersects... */
    if (contains(p)) {
      return true;
    }


    /*
     * Recursively check if the point intersects any of this polygon's lines
     * This code segment is what makes this method inclusive
     */
    int count = getVertexCount();
    double p1x = p.getX();
    double p1y = p.getY();

    for (int i = 1; i < count; i++) {
      if (lineIntersectsPoint(getX(i - 1), getY(i - 1), getX(i), getY(i), p1x, p1y)) {
        return true;
      }
    }


    /*
     * Don't forget the line segment from index[0] to index[size()]
     * Only check this if the polygon has more than 2 vertices (is not a line segment)
     */
    if (count > 2) {
      if (lineIntersectsPoint(getX(count - 1), getY(count - 1), getX(0), getY(0), p1x, p1y)) {
        return true;
      }
    }


    return false;
  }



  public void lineTo(Point2D p) {
    lineTo(p.getX(), p.getY());
  }


  /**
   * Add a new vertex to the end of the polygon. Throw an exception of the polygon has already been closed.
   */
  public abstract void lineTo(double x, double y);

  public void moveTo(Point2D p) {
    moveTo(p.getX(), p.getY());
  }

  /**
   * Move the start point of the vertex to the given position. Throw an exception if the line already contains any
   * vertices.
   */
  public abstract void moveTo(double x, double y);


  /**
   * Reset the polygon back to empty.
   */
  public void reset() {
    _coordCount = 0;
    _closed = false;
  }


  /**
   * Set the given X-coordinate.
   * 
   * @exception IndexOutOfBoundsException
   *            The index is out of bounds.
   */
  public abstract void setX(int index, double x);

  /**
   * Set the given Y-coordinate
   * 
   * @exception IndexOutOfBoundsException
   *            The index is out of bounds.
   */
  public abstract void setY(int index, double y);

  /**
   * Transform the polygon with the given transform.
   */
  public abstract void transform(AffineTransform at);

  /**
   * Translate the polygon the given distance.
   */
  public abstract void translate(double x, double y);

  /**
   * Return a string representation of the polygon.
   */
  public String toString() {
    String out = getClass().getName() + "[\n";

    for (int i = 0; i < getVertexCount(); i++) {
      out = out + "\t" + getX(i) + ", " + getY(i) + "\n";
    }

    out = out + "]";
    return out;
  }

  /**
   * The concrete Polygon class that stores coordinates internally as floats.
   */
  public static class Float extends Polygon2D {
    /**
     * The coordinates
     */
    float[] _coords;

    /**
     * Create a new polygon with no vertices.
     */
    public Float() {
      this(1);
    }

    /**
     * Create a new polygon with space for the given number of vertices.
     */
    public Float(int size) {
      _coords = new float[2 * size];
    }

    /**
     * Create a new polygon with space for the given number of vertices.
     */
    public Float(float[] coords) {
      _coords = coords;
      _coordCount = coords.length;
    }

    public Float(Point2D p) {
      this((float) p.getX(), (float) p.getY());
    }

    /**
     * Create a new polygon with a single start point
     */
    public Float(float x, float y) {
      this(1);
      _coords[0] = x;
      _coords[1] = y;
      _coordCount = 2;
    }

    /**
     * Create a new polygon with the provided x & y locations. If the two arrays are of unequal length, an empty polygon
     * is created. NaN
     * values are ignored
     * 
     * @param xlocs
     *        the array of x locations.
     * @param ylocs
     *        the array of y locations.
     */
    public Float(float[] xlocs, float[] ylocs) {
      this();
      if (xlocs.length != ylocs.length) {
        return;
      }
      FloatArrayList coords = new FloatArrayList();
      for (int i = 0; i < xlocs.length; i++) {
        if (!java.lang.Float.isNaN(xlocs[i]) & !java.lang.Float.isNaN(ylocs[i])) {
          coords.add(xlocs[i]);
          coords.add(ylocs[i]);
        }
      }
      coords.trimToSize();
      _coords = coords.elements();
      // _coords = new float[xlocs.length];
      // int j = 0;
      // for (int i = 0; i < xlocs.length; i++) {
      // _coords[j++] = xlocs[i];
      // _coords[j++] = ylocs[i];
      // }
      _coordCount = _coords.length;
      // this.closePath();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.utils.geom.Polygon2D#copy()
     */
    @Override
    public Polygon2D.Float copy() {
      // int vertCount = getVertexCount();
      //
      // if (vertCount <= 0) {
      // return new Polygon2D.Float();
      // }
      //
      // Polygon2D.Float result = new Polygon2D.Float((float) getX(0), (float) getY(0));
      //
      // if (vertCount > 1) {
      // /*
      // * Start adding lines at index 1 since
      // * index 0 is the starting point
      // */
      // for (int ctr = 1; ctr < vertCount; ctr++) {
      // result.lineTo(getX(ctr), getY(ctr));
      // }
      // }
      //
      //
      // result.closePath();
      // return result;
      return new Polygon2D.Float(AsaArrayUtils.doubleArrayToFloatArray(this.getXCoords()),
          AsaArrayUtils.doubleArrayToFloatArray(this.getYCoords()));
    }



    /**
     * Get the floating-point bounds of the polygon.
     */
    public Rectangle2D getBounds2D() {
      if (_coordCount <= 1) {
        return new Rectangle2D.Float();
      }

      float x1 = _coords[0];
      float y1 = _coords[1];
      float x2 = x1;
      float y2 = y1;

      for (int i = 2; i < _coordCount;) {
        if (_coords[i] < x1) {
          x1 = _coords[i];
        } else if (_coords[i] > x2) {
          x2 = _coords[i];
        }

        i++;

        if (_coords[i] < y1) {
          y1 = _coords[i];
        } else if (_coords[i] > y2) {
          y2 = _coords[i];
        }

        i++;
      }

      return new Rectangle2D.Float(x1, y1, x2 - x1, y2 - y1);
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.utils.geom.Polygon2D#getBoundsGeo()
     */
    /**
     * This method is not implemented and will always return null
     */
    @Override
    public GeoRectangle.Double getBoundsGeo() {

      return null;
      // throw new
      // MethodNotImplementedException("getBoundsGeo() not implemented");

      // int vertCount = getVertexCount();
      //
      // if (vertCount <= 0) {
      //
      // return new GeoRectangle.Double();
      //
      // } else {
      // double minX = java.lang.Double.POSITIVE_INFINITY;
      // double minY = java.lang.Double.POSITIVE_INFINITY;
      // double maxX = java.lang.Double.NEGATIVE_INFINITY;
      // double maxY = java.lang.Double.NEGATIVE_INFINITY;
      //
      // for (int ctr = 0; ctr < vertCount; ctr++) {
      // minX = Math.min(minX, getX(ctr));
      // minY = Math.min(minY, getY(ctr));
      // maxX = Math.max(maxX, getX(ctr));
      // maxY = Math.max(maxY, getY(ctr));
      // }
      //
      // return new GeoRectangle.Double(minX, minY, maxX, maxY);
      // }
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.utils.geom.Polygon2D#getCentroid()
     */
    @Override
    public Point2D getCentroid() {
      float px = 0, py = 0;
      int vertexCount = getVertexCount();

      if (vertexCount >= 1) {

        /* Get the x and y averages (centroid) */
        for (int ctr = 0; ctr < vertexCount; ctr++) {
          px += getX(ctr);
          py += getY(ctr);
        }

        px /= vertexCount;
        py /= vertexCount;

      }
      /* Else let px and py == 0 */


      return new Point2D.Float(px, py);
    }

    public List<Point2D> getVertices() {
      List<Point2D> ret = new ArrayList<Point2D>();
      for (int i = 0; i < _coords.length; i++) {
        ret.add(new Point2D.Float(_coords[i++], _coords[i]));
      }
      return ret;
    }

    public double[] getXCoords() {
      DoubleArrayList ret = new DoubleArrayList();
      for (int i = 0; i < _coords.length; i += 2) {
        ret.add(_coords[i]);
      }
      ret.trimToSize();
      return ret.elements();
      // double[] ret = new double[(int) (_coordCount * 0.5)];
      // // double[] ret = new double[_coordCount];
      // int cnt = 0;
      // for (int i = 0; i < _coords.length; i++) {
      // ret[i] = _coords[cnt];
      // i++;
      // cnt++;
      // }
      // return ret;
    }

    public double[] getYCoords() {
      DoubleArrayList ret = new DoubleArrayList();
      for (int i = 1; i < _coords.length; i += 2) {
        ret.add(_coords[i]);
      }
      ret.trimToSize();
      return ret.elements();
      // double[] ret = new double[(int) (_coordCount * 0.5)];
      // // double[] ret = new double[_coordCount];
      // int cnt = 0;
      // for (int i = 1; i < _coords.length; i++) {
      // ret[i] = _coords[cnt];
      // i++;
      // cnt++;
      // }
      // return ret;
    }

    /**
     * Get the given X-coordinate
     * 
     * @exception IndexOutOfBoundsException
     *            The index is out of bounds.
     */
    public double getX(int index) {
      return _coords[index * 2];
    }

    /**
     * Get the given Y-coordinate
     * 
     * @exception IndexOutOfBoundsException
     *            The index is out of bounds.
     */
    public double getY(int index) {
      return _coords[(index * 2) + 1];
    }

    /**
     * Add a new vertex to the end of the line.
     */
    public void lineTo(double x, double y) {
      if (_closed) {
        throw new UnsupportedOperationException("This polygon has already been closed");
      }

      if (_coordCount == _coords.length) {
        float[] temp = new float[_coordCount + 2];
        System.arraycopy(_coords, 0, temp, 0, _coordCount);
        _coords = temp;
      }

      _coords[_coordCount++] = (float) x;
      _coords[_coordCount++] = (float) y;
    }

    /**
     * Move the start point of the vertex to the given position.
     * 
     * @exception UnsupportedOperationException
     *            The polygon already has vertices
     */
    public void moveTo(double x, double y) {
      if (_coordCount > 0) {
        throw new UnsupportedOperationException("This polygon already has vertices");
      }

      _coords[0] = (float) x;
      _coords[1] = (float) y;
      _coordCount = 2;
    }

    /**
     * Set the given X-coordinate.
     * 
     * @exception IndexOutOfBoundsException
     *            The index is out of bounds.
     */
    public void setX(int index, double x) {
      _coords[index * 2] = (float) x;
    }

    /**
     * Set the given Y-coordinate
     * 
     * @exception IndexOutOfBoundsException
     *            The index is out of bounds.
     */
    public void setY(int index, double y) {
      _coords[(index * 2) + 1] = (float) y;
    }

    /**
     * Transform the polygon with the given transform.
     */
    public void transform(AffineTransform at) {
      at.transform(_coords, 0, _coords, 0, _coordCount / 2);
    }

    /**
     * Translate the polygon the given distance.
     */
    public void translate(double x, double y) {
      float fx = (float) x;
      float fy = (float) y;

      for (int i = 0; i < _coordCount;) {
        _coords[i++] += fx;
        _coords[i++] += fy;
      }
    }

  }

  // /////////////////////////////////////////////////////////////////
  // // Double

  /**
   * The concrete Polygon class that stores coordinates internally as doubles.
   */
  public static class Double extends Polygon2D {
    /**
     * The coordinates
     */
    double[] _coords;

    /**
     * Create a new polygon with no coordinates
     */
    public Double() {
      this(1);
    }

    /**
     * Create a new polygon with space for the given number of vertices.
     */
    public Double(int size) {
      _coords = new double[2 * size];
    }

    /**
     * Create a new polygon with the given vertices, in the format [x0, y0, x1, y1, ... ].
     */
    public Double(double[] coords) {
      _coords = coords;
      _coordCount = coords.length;
    }

    public Double(Point2D p) {
      this(p.getX(), p.getY());
    }

    /**
     * Create a new polygon with a single start point
     */
    public Double(double x, double y) {
      this(1);
      _coords[0] = x;
      _coords[1] = y;
      _coordCount = 2;
    }

    /**
     * Create a new polygon with the provided x & y locations. If the two arrays are of unequal length, an empty polygon
     * is created. NaN
     * values are ignored
     * 
     * @param xlocs
     *        the array of x locations.
     * @param ylocs
     *        the array of y locations.
     */
    public Double(double[] xlocs, double[] ylocs) {
      this();
      if (xlocs.length != ylocs.length) {
        return;
      }
      DoubleArrayList coords = new DoubleArrayList();
      for (int i = 0; i < xlocs.length; i++) {
        if (!java.lang.Double.isNaN(xlocs[i]) & !java.lang.Double.isNaN(ylocs[i])) {
          coords.add(xlocs[i]);
          coords.add(ylocs[i]);
        }
      }
      coords.trimToSize();
      _coords = coords.elements();
      //
      // _coords = new double[2 * xlocs.length];
      // int j = 0;
      // for (int i = 0; i < xlocs.length; i++) {
      // _coords[j++] = xlocs[i];
      // _coords[j++] = ylocs[i];
      // }
      _coordCount = _coords.length;
      // this.closePath();
    }

    /**
     * Creates a copy of this polygon with the same coordinates. The original polygon remains unchanged.
     * 
     * @return a copy of the this polygon
     */
    @Override
    public Polygon2D.Double copy() {
      // int vertCount = getVertexCount();
      //
      // if (vertCount <= 0) {
      // return new Polygon2D.Double();
      // }
      //
      // Polygon2D.Double result = new Polygon2D.Double(getX(0), getY(0));
      //
      // if (vertCount > 1) {
      // /*
      // * Start adding lines at index 1 since
      // * index 0 is the starting point
      // */
      // for (int ctr = 1; ctr < vertCount; ctr++) {
      // result.lineTo(getX(ctr), getY(ctr));
      // }
      // }
      //
      //
      // result.closePath();
      // return result;
      return new Polygon2D.Double(this.getXCoords(), this.getYCoords());
    }



    /**
     * Get the floating-point bounds of the polygon.
     */
    public Rectangle2D getBounds2D() {
      if (_coordCount <= 0) {
        return new Rectangle2D.Double();
      }

      double x1 = _coords[0];
      double y1 = _coords[1];
      double x2 = x1;
      double y2 = y1;

      for (int i = 2; i < _coordCount;) {
        if (_coords[i] < x1) {
          x1 = _coords[i];
        } else if (_coords[i] > x2) {
          x2 = _coords[i];
        }

        i++;

        if (_coords[i] < y1) {
          y1 = _coords[i];
        } else if (_coords[i] > y2) {
          y2 = _coords[i];
        }

        i++;
      }

      return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.utils.geom.Polygon2D#getBoundsGeo()
     */
    @Override
    public GeoRectangle.Double getBoundsGeo() {
      // int vertCount = getVertexCount();
      //
      // if (vertCount <= 0) {
      //
      // return new GeoRectangle.Double();
      //
      // } else {
      // double minX = java.lang.Double.POSITIVE_INFINITY;
      // double minY = java.lang.Double.POSITIVE_INFINITY;
      // double maxX = java.lang.Double.NEGATIVE_INFINITY;
      // double maxY = java.lang.Double.NEGATIVE_INFINITY;
      //
      // for (int ctr = 0; ctr < vertCount; ctr++) {
      // minX = Math.min(minX, getX(ctr));
      // minY = Math.min(minY, getY(ctr));
      // maxX = Math.max(maxX, getX(ctr));
      // maxY = Math.max(maxY, getY(ctr));
      // }
      //
      // return new GeoRectangle.Double(minX, minY, maxX, maxY);
      // }
      return new GeoRectangle.Double(this.getMinX(), this.getMinY(), this.getMaxX(), this.getMaxY());
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.utils.geom.Polygon2D#getCentroid()
     */
    @Override
    public Point2D getCentroid() {
      double px = 0, py = 0;
      int vertexCount = getVertexCount();

      if (vertexCount >= 1) {

        /* Get the x and y averages (centroid) */
        for (int ctr = 0; ctr < vertexCount; ctr++) {
          px += getX(ctr);
          py += getY(ctr);
        }

        px /= vertexCount;
        py /= vertexCount;

      }
      /* Else let px and py == 0 */


      return new Point2D.Double(px, py);
    }

    public List<Point2D> getVertices() {
      List<Point2D> ret = new ArrayList<Point2D>();
      for (int i = 0; i < _coords.length; i++) {
        ret.add(new Point2D.Double(_coords[i++], _coords[i]));
      }
      return ret;
    }

    public double[] getXCoords() {
      DoubleArrayList ret = new DoubleArrayList();
      for (int i = 0; i < _coords.length; i += 2) {
        ret.add(_coords[i]);
      }
      ret.trimToSize();
      return ret.elements();
      // double[] ret = new double[(int) (_coordCount * 0.5)];
      // // double[] ret = new double[_coordCount];
      // int cnt = 0;
      // for (int i = 0; i < _coords.length; i++) {
      // ret[cnt] = _coords[i];
      // i++;
      // cnt++;
      // }
      // return ret;
    }

    public double[] getYCoords() {
      DoubleArrayList ret = new DoubleArrayList();
      for (int i = 1; i < _coords.length; i += 2) {
        ret.add(_coords[i]);
      }
      ret.trimToSize();
      return ret.elements();
      // double[] ret = new double[(int) (_coordCount * 0.5)];
      // // double[] ret = new double[_coordCount];
      // int cnt = 0;
      // for (int i = 1; i < _coords.length; i++) {
      // ret[cnt] = _coords[i];
      // i++;
      // cnt++;
      // }
      // return ret;
    }

    /**
     * Get the given X-coordinate
     * 
     * @exception IndexOutOfBoundsException
     *            The index is out of bounds.
     */
    public double getX(int index) {
      return _coords[index * 2];
    }

    /**
     * Get the given Y-coordinate
     * 
     * @exception IndexOutOfBoundsException
     *            The index is out of bounds.
     */
    public double getY(int index) {
      return _coords[(index * 2) + 1];
    }

    /**
     * Add a new vertex to the end of the line.
     */
    public void lineTo(double x, double y) {
      if (_closed) {
        throw new UnsupportedOperationException("This polygon has already been closed");
      }

      if (_coordCount == _coords.length) {
        double[] temp = new double[_coordCount + 2];
        System.arraycopy(_coords, 0, temp, 0, _coordCount);
        _coords = temp;
      }

      _coords[_coordCount++] = x;
      _coords[_coordCount++] = y;
    }

    /**
     * Move the start point of the vertex to the given position.
     * 
     * @exception UnsupportedOperationException
     *            The polygon already has vertices
     */
    public void moveTo(double x, double y) {
      if (_coordCount > 0) {
        throw new UnsupportedOperationException("This polygon already has vertices");
      }

      _coords[0] = x;
      _coords[1] = y;
      _coordCount = 2;
    }

    /**
     * Set the given X-coordinate.
     * 
     * @exception IndexOutOfBoundsException
     *            The index is out of bounds.
     */
    public void setX(int index, double x) {
      _coords[index * 2] = x;
    }

    /**
     * Set the given Y-coordinate
     * 
     * @exception IndexOutOfBoundsException
     *            The index is out of bounds.
     */
    public void setY(int index, double y) {
      _coords[(index * 2) + 1] = y;
    }

    /**
     * Transform the polygon with the given transform.
     */
    public void transform(AffineTransform at) {
      at.transform(_coords, 0, _coords, 0, _coordCount / 2);
    }

    /**
     * Translate the polygon the given distance.
     */
    public void translate(double x, double y) {
      for (int i = 0; i < _coordCount;) {
        _coords[i++] += x;
        _coords[i++] += y;
      }
    }
  }
}
