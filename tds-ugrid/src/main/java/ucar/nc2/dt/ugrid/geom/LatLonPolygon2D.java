/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * GeoPolygon2D.java
 *
 * Created on Oct 16, 2009 @ 8:32:41 AM
 */

package ucar.nc2.dt.ugrid.geom;


import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author CBM <cmueller@asascience.com>
 */

public abstract class LatLonPolygon2D implements Serializable {

  /**
   * Serial version ID
   */
  private static final long serialVersionUID = -832292217494381300L;

  protected boolean crossesDateline = false;

  /**
   * Adds the <tt>LatLonPoint2D</tt> to the end of the polygon
   *
   * @param point
   */
  public void lineTo(LatLonPoint2D point) {
    lineTo(point.getLatitude(), point.getLongitude());
  }

  public void lineTo(Point2D point) {
    lineTo(point.getY(), point.getX());
  }

  public abstract double getArea();

  /**
   * Adds the point specified by the provided <tt>lat</tt> and <tt>lon</tt> to the end of the polygon
   *
   * @param lat
   *        the latitude of the point to add
   * @param lon
   *        the longitude of the point to add
   */
  public abstract void lineTo(double lat, double lon);

  /**
   * Calculates the geometric center of this <tt>LatLonPolygon2D</tt>
   *
   * @return the center point
   */
  public abstract LatLonPoint2D getCentroid();

  /**
   * Determines if the <tt>LatLonPoint2D</tt> is within this polygon
   *
   * @param point
   *        the point to test
   * @return true if the point is contained within this polygon; false otherwise
   */
  public boolean contains(LatLonPoint2D point) {
    return contains(point.getLatitude(), point.getLongitude());
  }

  /**
   * Determines if the point represented by <tt>lat</tt>, <tt>lon</tt> is within this polygon
   *
   * @param lat
   *        the latitude (y) of the point to test
   * @param lon
   *        the longitude (x) of the point to test
   * @return true if the point is contained within this polygon; false otherwise
   */
  public abstract boolean contains(double lat, double lon);

  /**
   * Determines if the specified <tt>LatLonRectangle2D</tt> is COMPLETELY contained within this <tt>LatLonPolygon2D</tt>
   *
   * @param rect
   *        the <tt>LatLonRectangle2D</tt> to check
   * @return true if the <tt>LatLonRectangle2D</tt> is COMPLETELY contained in this polygon; false otherwise
   */
  public boolean contains(LatLonRectangle2D rect) {
    return contains(rect.getUpperLeftPoint()) && contains(rect.getUpperRightPoint())
        && contains(rect.getLowerRightPoint()) && rect.contains(rect.getLowerLeftPoint());
  }

  /**
   * Determines if the specified <tt>LatLonPolygon2D</tt> is COMPLETELY contained within this <tt>LatLonPolygon2D</tt>
   *
   * @param poly
   *        the <tt>LatLonPolygon2D</tt> to check
   * @return true if the <tt>LatLonPolygon2D</tt> is COMPLETELY contained in this polygon; false otherwise
   */
  public boolean contains(LatLonPolygon2D poly) {
    for (LatLonPoint2D p : poly.getVertices()) {
      if (!contains(p.getLatitude(), p.getLongitude())) {
        return false;
      }
    }
    return true;
  }

  public abstract boolean intersects(LatLonPolygon2D poly);

  public boolean intersects(LatLonPoint2D point) {
    return intersects(point, this.crossesDateline());
  }

  public abstract boolean intersects(LatLonPoint2D point, boolean crossesDateline);

  public abstract boolean intersects(LatLonPoint2D p1, LatLonPoint2D p2, boolean crossesDateline);

  public abstract double distanceSq(LatLonPoint2D p);

  public double distance(LatLonPoint2D p) {
    return Math.sqrt(distanceSq(p));
  }

  /**
   * Gets a list of <tt>LatLonPoint2D</tt> vertices in this polygon
   *
   * @return the vertices
   */
  public abstract List<LatLonPoint2D> getVertices();

  public abstract int getVertexCount();

  public abstract double[] getLonCoords();

  public abstract double[] getLatCoords();

  public abstract LatLonRectangle2D getBouningLatLonRectangle2D();

  /**
   * Calculates the bounding box of this <code>LatLonPolygon2D</code> and returns the coordinates as a
   * <code>double</code> array with a
   * length of 4. The coordinates are arranged in the order of western lon, southern lat, eastern lon, and northern
   * lat.<br />
   * <br />
   * In terms of points where LL = lower left and UR = upper right, this is equivalent to the arrangement of
   * <code>double[] {lly, llx, ury, urx}</code>.
   *
   * @return a <code>double</code> array with a length of 4 containing the lat/lon points of the bounding box of this
   *         <code>LatLonPolygon2D</code>. If this object contains no vertices <code>null</code> is returned.
   */
  public abstract double[] getBoundingLatLonValues();

  public boolean crossesDateline() {
    return crossesDateline;
  }

  /**
   * Copies this <tt>LatLonPolygon2D</tt> to a new <tt>LatLonPolygon2D</tt>
   *
   * @return a copy of this polygon
   */
  public LatLonPolygon2D copy() {
    return new LatLonPolygon2D.Double(this.getVertices());
  }

  /**
   * Return a string representation of the polygon.
   */
  public String toString() {
    StringBuilder out = new StringBuilder(getClass().getName() + "[\n");

    List<LatLonPoint2D> pts = getVertices();
    for (LatLonPoint2D p : pts) {
      out.append("\t");
      out.append(p);
      out.append("\n");
    }
    out.append("]");

    return out.toString();
  }

  public static class Double extends LatLonPolygon2D {
    List<LatLonPoint2D> vertices;
    private LatLonPoint2D last = null;
    public double westLon = 0.0;
    public double eastLon = 0.0;
    public double northLat = 0.0;
    public double southLat = 0.0;

    /**
     * Create an empty polygon with no vertices
     */
    public Double() {
      vertices = new ArrayList<LatLonPoint2D>();
    }

    /**
     * Create a new polygon with a single starting point.
     *
     * @param point
     *        the starting point for the polygon
     */
    public Double(LatLonPoint2D point) {
      this();
      // vertices.add(point);
      northLat = point.getY();
      southLat = point.getY();
      westLon = point.getX();
      eastLon = point.getX();

      last = point;
      vertices.add(point);
    }

    /**
     * Create a new polygon with a single starting point.
     *
     * @param lat
     *        the latitude of the starting point
     * @param lon
     *        the longitude of the starting point
     */
    public Double(double lat, double lon) {
      this();
      // vertices.add(new LatLonPoint2D.Double(lat, lon));
      northLat = lat;
      southLat = lat;
      westLon = lon;
      eastLon = lon;

      last = new LatLonPoint2D.Double(lat, lon);
      vertices.add(new LatLonPoint2D.Double(lat, lon));
    }

    /**
     * Create a new polygon from the provided <tt>vertices</tt>. The resulting polygon is NOT closed
     *
     * @param vertices
     *        the vertices of the polygon
     */
    public Double(List<LatLonPoint2D> vertices) {
      this();
      for (LatLonPoint2D p : vertices) {
        lineTo(p);
      }
    }

    /**
     * Create a new polygon from the provided <tt>vertices</tt>. The resulting polygon is NOT closed
     *
     * @param vertices
     *        the vertices of the polygon
     */
    public Double(LatLonPoint2D[] vertices) {
      this(Arrays.asList(vertices));
    }

    /**
     * Create a new polygon from the provided arrays. If the arrays are not the same length, the resulting polygon is
     * empty. The
     * resulting polygon is NOT closed
     *
     * @param lats
     * @param lons
     */
    public Double(double[] lats, double[] lons) {
      this();
      if (lats.length != lons.length) {
        return;
      }
      for (int i = 0; i < lats.length; i++) {
        lineTo(lats[i], lons[i]);
      }
    }

    public Double(LatLonRectangle2D rect) {
      this();
      /* Determine the min and max, lat and lon */
      LatLonPoint2D lowerLeft = rect.getLowerLeftPoint();
      LatLonPoint2D upperRight = rect.getUpperRightPoint();
      westLon = lowerLeft.getLongitude();
      eastLon = upperRight.getLongitude();
      southLat = lowerLeft.getLatitude();
      northLat = upperRight.getLatitude();

      /* Add each point as a vertex */
      vertices.add(lowerLeft);
      vertices.add(rect.getLowerRightPoint());
      vertices.add(upperRight);
      vertices.add(rect.getUpperLeftPoint());

      last = rect.getUpperLeftPoint();

      crossesDateline = rect.crossesDateline();
    }

    public Double(Polygon2D poly) {
      this();
      for (Point2D p : poly.getVertices()) {
        lineTo(p.getY(), p.getX());
      }
    }

    @Override
    public double getArea() {
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


      if (this.getVertexCount() < 3) {
        return 0;
      }



      double result = 0.0;
      double xy, yx;
      int lastNodeIndex = this.getVertexCount() - 1;

      if (crossesDateline) {
        for (int i = 0; i < lastNodeIndex; i++) {
          xy = LatLonPoint2D.normLon360(vertices.get(i).getLongitude()) * vertices.get(i + 1).getLatitude();
          yx = vertices.get(i).getLatitude() * LatLonPoint2D.normLon360(vertices.get(i + 1).getLongitude());

          result += (xy - yx);
        }

        /* The last point must also be the first... add this in as well */
        xy = LatLonPoint2D.normLon360(vertices.get(lastNodeIndex).getLongitude()) * vertices.get(0).getLatitude();
        yx = vertices.get(lastNodeIndex).getLatitude() * LatLonPoint2D.normLon360(vertices.get(0).getLongitude());
        result += (xy - yx);
      } else {
        for (int i = 0; i < lastNodeIndex; i++) {
          xy = vertices.get(i).getLongitude() * vertices.get(i + 1).getLatitude();
          yx = vertices.get(i).getLatitude() * vertices.get(i + 1).getLongitude();

          result += (xy - yx);
        }

        /* The last point must also be the first... add this in as well */
        xy = vertices.get(lastNodeIndex).getLongitude() * vertices.get(0).getLatitude();
        yx = vertices.get(lastNodeIndex).getLatitude() * vertices.get(0).getLongitude();
        result += (xy - yx);
      }


      return Math.abs(result * 0.5);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void lineTo(double lat, double lon) {
      if (java.lang.Double.isNaN(lat) || java.lang.Double.isNaN(lon)) {
        return;
      }
      LatLonPoint2D n = new LatLonPoint2D.Double(lat, lon);
      lineTo(n);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void lineTo(LatLonPoint2D point) {
      if (vertices.isEmpty()) {
        northLat = point.getY();
        southLat = point.getY();
        westLon = point.getX();
        eastLon = point.getX();

        last = point;
        vertices.add(point);
        return;
      }

      if (!last.equals(point)) {
        /* Check to see if the dateline has been crossed */
        double lon = point.getLongitude();
        double llon = last.getLongitude();
        if ((llon > 90 & lon < 0) || (llon < -90 & lon > 0) || (lon > 90 & llon < 0) || (lon < -90 & llon > 0)) {
          crossesDateline = true;

        }
        /* Determine the min and max, lat and lon */
        if (crossesDateline) {
          double normPointX = LatLonPoint2D.normLon360(point.getX());
          westLon = LatLonPoint2D.normLon(Math.min(LatLonPoint2D.normLon360(westLon), normPointX));
          eastLon = LatLonPoint2D.normLon(Math.max(LatLonPoint2D.normLon360(eastLon), normPointX));
        } else {
          westLon = Math.min(westLon, point.getX());
          eastLon = Math.max(eastLon, point.getX());
        }
        northLat = Math.max(northLat, point.getY());
        southLat = Math.min(southLat, point.getY());

        last = point;
        vertices.add(point);
      }
    }

    /**
     * {@inheritDoc}
     */
    public List<LatLonPoint2D> getVertices() {
      return vertices;
    }

    public int getVertexCount() {
      return vertices.size();
    }

    public double[] getLonCoords() {
      double[] ret = new double[vertices.size()];
      int i = 0;
      for (LatLonPoint2D p : vertices) {
        ret[i++] = p.getLongitude();
      }
      return ret;
    }

    public double[] getLatCoords() {
      double[] ret = new double[vertices.size()];
      int i = 0;
      for (LatLonPoint2D p : vertices) {
        ret[i++] = p.getLatitude();
      }
      return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LatLonPoint2D getCentroid() {
      double px = 0, py = 0;
      if (crossesDateline) {
        for (LatLonPoint2D v : vertices) {
          px += LatLonPoint2D.normLon360(v.getLongitude());
          py += v.getLatitude();
        }
        px /= vertices.size();
        py /= vertices.size();

        px = LatLonPoint2D.normLon(px);
      } else {
        for (LatLonPoint2D v : vertices) {
          px += v.getLongitude();
          py += v.getLatitude();
        }
        px /= vertices.size();
        py /= vertices.size();
      }

      return new LatLonPoint2D.Double(py, px);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(double lat, double lon) {
      int crossings = 0;
      int count = vertices.size();
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
      double[] xs = new double[count];
      double[] ys = new double[count];
      int c = 0;
      if (crossesDateline) {
        /* Need to shift everything into 0-360 space */
        lon = LatLonPoint2D.normLon360(lon);
        for (LatLonPoint2D v : vertices) {
          xs[c] = LatLonPoint2D.normLon360(v.getLongitude());
          ys[c] = v.getLatitude();
          c++;
        }
      } else {
        for (LatLonPoint2D v : vertices) {
          xs[c] = v.getLongitude();
          ys[c] = v.getLatitude();
          c++;
        }
      }

      double x1, x2, y1, y2;
      LatLonPoint2D v1, v2;
      for (int i = 1; i < count; i++) {
        x1 = xs[i - 1];
        x2 = xs[i];
        y1 = ys[i - 1];
        y2 = ys[i];

        if ((lon < x1) || (lon < x2)) {
          if (lat == y2) {
            crossings++;
          } else if (lat == y1) {

          } else if (Line2D.linesIntersect(lon, lat, Math.max(x1, x2), lat, x1, y1, x2, y2)) {
            crossings++;
          }
        }
      }
      x1 = xs[count - 1];
      x2 = xs[0];
      y1 = ys[count - 1];
      y2 = ys[0];
      if ((lon < x1) || (lon < x2)) {
        if (Line2D.linesIntersect(lon, lat, Math.max(x1, x2), lat, x1, y1, x2, y2) && (lat != y1)) {
          crossings++;
        }
      }

      // if(crossesDateline) {
      // /* Shift the polygon back*/
      // for(LatLonPoint2D v : vertices) {
      // v.setLongitude(LatLonPoint2D.normLon(v.getLongitude()));
      // }
      // }

      return (crossings % 2) == 1;
    }



    /**
     * Calculates the bounding box of this <code>LatLonPolygon2D</code> and returns the coordinates as a
     * <code>double</code> array with
     * a length of 4. The coordinates are arranged in the order of southern lat, western lon, northern lat, and eastern
     * lon<br />
     * <br />
     * In terms of points where LL = lower left and UR = upper right, this is equivalent to the arrangement of
     * <code>double[] {lly, llx, ury, urx}</code>.
     *
     * @return a <code>double</code> array with a length of 4 containing the lat/lon points of the bounding box of this
     *         <code>LatLonPolygon2D</code>. If this object contains no vertices <code>null</code> is returned.
     */
    @Override
    public double[] getBoundingLatLonValues() {
      // int count = vertices.size();
      // if (count == 0) {
      // return null;
      // }
      // double[] xs = new double[count];
      // double[] ys = new double[count];
      // int c = 0;
      // if (crossesDateline) {
      // /* Need to shift everything into 0-360 space */
      // for (LatLonPoint2D v : vertices) {
      // xs[c] = LatLonPoint2D.normLon360(v.getLongitude());
      // ys[c] = v.getLatitude();
      // c++;
      // }
      // } else {
      // for (LatLonPoint2D v : vertices) {
      // xs[c] = v.getLongitude();
      // ys[c] = v.getLatitude();
      // c++;
      // }
      // }
      // double[] lonMM = AsaArrayUtils.minMax(xs);
      // if (crossesDateline) {
      // lonMM[0] = LatLonPoint2D.normLon(lonMM[0]);
      // lonMM[1] = LatLonPoint2D.normLon(lonMM[1]);
      // }
      // double[] latMM = AsaArrayUtils.minMax(ys);
      //
      //
      // return new double[] { latMM[0], lonMM[0], latMM[1], lonMM[1] };
      // lly, llx, ury, urx
      return new double[] {southLat, westLon, northLat, eastLon};
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public LatLonRectangle2D getBouningLatLonRectangle2D() {
      double[] v = getBoundingLatLonValues();
      return new LatLonRectangle2D(v[0], v[1], v[2], v[3]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean intersects(LatLonPolygon2D poly) {
      if (this.equals(poly)) {
        return true;
      }

      boolean dateline = this.crossesDateline() || poly.crossesDateline();

      int countPoly1 = this.getVertexCount();
      int countPoly2 = poly.getVertexCount();
      /* If either polygon does not have vertices... */
      if (countPoly1 == 0 || countPoly1 == 0) {
        return false;
      }

      List<LatLonPoint2D> v1 = this.getVertices();
      List<LatLonPoint2D> v2 = poly.getVertices();
      /*
       * If either polygon only has 1 vertex; treat it as a point
       */
      if (countPoly1 == 1) {
        return poly.intersects(v1.get(0), dateline);
      }
      if (countPoly2 == 1) {
        return this.intersects(v2.get(0), dateline);
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
      LatLonRectangle2D b1 = this.getBouningLatLonRectangle2D();
      LatLonRectangle2D b2 = poly.getBouningLatLonRectangle2D();
      LatLonPoint2D c1 = b1.getCentroid();
      LatLonPoint2D c2 = b2.getCentroid();

      double r1 = c1.distance(b1.getLonMin(), b1.getLatMin());
      double r2 = c2.distance(b2.getLonMin(), b2.getLatMin());

      // if (c1.distance(c2) > r1 + r2) { /* Too far away to intersect */
      // return false;
      // }

      /*
       * If either polygon contains the centroid of the other, there is an
       * intersection. (This is the most commonly tripped inclusion check)
       */
      if (this.contains(c2)) {
        return true;
      }
      if (poly.contains(c1)) {
        return true;
      }

      /** Full intersection checking */
      /*******************************/

      /*
       * Check for the intersection of any line segment of the given polygon
       * with this polygon
       */
      for (int i = 1; i < countPoly2; i++) {
        if (intersects(v2.get(i - 1), v2.get(i), dateline)) {
          return true;
        }
      }
      /*
       * Don't forget the line segment from index[size()] to index[0]
       * Only check this if the polygon has more than 2 vertices (is not a line segment)
       */
      if (countPoly2 > 2) {
        if (intersects(v2.get(countPoly2 - 1), v2.get(0), dateline)) {
          return true;
        }
      }


      return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean intersects(LatLonPoint2D point, boolean crossesDateline) {
      /* If the point is contained it also intersects... */
      if (contains(point)) {
        return true;
      }
      /*
       * Recursively check if the point intersects any of this polygon's lines
       * This code segment is what makes this method inclusive
       */
      int count = getVertexCount();
      double px = (crossesDateline) ? LatLonPoint2D.normLon360(point.getLongitude()) : point.getLongitude();
      double py = point.getLatitude();
      double vlon0, vlon1;
      for (int i = 1; i < count; i++) {
        vlon0 = (crossesDateline) ? LatLonPoint2D.normLon360(vertices.get(i - 1).getLongitude())
            : vertices.get(i - 1).getLongitude();
        vlon1 = (crossesDateline) ? LatLonPoint2D.normLon360(vertices.get(i).getLongitude())
            : vertices.get(i).getLongitude();
        if (lineIntersectsPoint(vlon0, vertices.get(i - 1).getLatitude(), vlon1, vertices.get(i).getLatitude(), px,
            py)) {
          return true;
        }
      }
      /*
       * Don't forget the line segment from index[0] to index[size()]
       * Only check this if the polygon has more than 2 vertices (is not a line segment)
       */
      if (count > 2) {
        vlon0 = (crossesDateline) ? LatLonPoint2D.normLon360(vertices.get(count - 1).getLongitude())
            : vertices.get(count - 1).getLongitude();
        vlon1 = (crossesDateline) ? LatLonPoint2D.normLon360(vertices.get(0).getLongitude())
            : vertices.get(0).getLongitude();
        if (lineIntersectsPoint(vlon0, vertices.get(count - 1).getLatitude(), vlon1, vertices.get(0).getLatitude(), px,
            py)) {
          return true;
        }
      }

      return false;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean intersects(LatLonPoint2D e0, LatLonPoint2D e1, boolean crossesDateline) {
      int count = getVertexCount();

      if (count == 0) {
        return false;
      }

      if (count == 1) {
        /* Treat this polygon as a point */
        if (crossesDateline) {
          return lineIntersectsPoint(LatLonPoint2D.normLon360(e0.getX()), e0.getY(),
              LatLonPoint2D.normLon360(e1.getX()), e1.getY(), LatLonPoint2D.normLon360(vertices.get(0).getLongitude()),
              vertices.get(0).getLatitude());
        }
        return lineIntersectsPoint(e0.getX(), e0.getY(), e1.getX(), e1.getY(), vertices.get(0).getLongitude(),
            vertices.get(0).getLatitude());
      }


      /* Iterate through each line segment made by the points of this polygon */
      double p0x, p0y, p1x, p1y;
      double p2x, p2y, p3x, p3y;
      p0x = (crossesDateline) ? LatLonPoint2D.normLon360(e0.getLongitude()) : e0.getLongitude();
      p0y = e0.getY();
      p1x = (crossesDateline) ? LatLonPoint2D.normLon360(e1.getLongitude()) : e1.getLongitude();
      p1y = e1.getY();

      for (int i = 1; i < count; i++) {
        p2x = (crossesDateline) ? LatLonPoint2D.normLon360(vertices.get(i - 1).getLongitude())
            : vertices.get(i - 1).getLongitude();
        p2y = vertices.get(i - 1).getLatitude();

        p3x = (crossesDateline) ? LatLonPoint2D.normLon360(vertices.get(i).getLongitude())
            : vertices.get(i).getLongitude();
        p3y = vertices.get(i).getLatitude();

        if (Line2D.linesIntersect(p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y)) {
          return true;
        }
      }


      /*
       * If this polygon has more than two vertices finalize the connection and
       * check the last line segment...
       */
      if (count > 2) {
        p2x = (crossesDateline) ? LatLonPoint2D.normLon360(vertices.get(0).getLongitude())
            : vertices.get(0).getLongitude();
        p2y = vertices.get(0).getLatitude();

        p3x = (crossesDateline) ? LatLonPoint2D.normLon360(vertices.get(count - 1).getLongitude())
            : vertices.get(count - 1).getLongitude();
        p3y = vertices.get(count - 1).getLatitude();

        if (Line2D.linesIntersect(p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y)) {
          return true;
        }
      }


      return false;
    }

    @Override
    public double distanceSq(LatLonPoint2D p) {
      double resultSq = java.lang.Double.POSITIVE_INFINITY;
      double tempDistSq = 0.0;

      if (getVertexCount() <= 0) {
        throw new IllegalStateException("No vertices set");
      } else if (getVertexCount() == 1) {
        resultSq = p.distanceSq(vertices.get(0).getLongitude(), vertices.get(0).getLatitude());
      } else {
        if (contains(p)) {
          return 0.0;
        }
        int count = getVertexCount();
        double v0x, v0y, v1x, v1y, px, py;
        px = (crossesDateline) ? LatLonPoint2D.normLon360(p.getLongitude()) : p.getLongitude();
        py = p.getLatitude();
        for (int i = 0; i < count - 1; i++) {
          v0x = (crossesDateline) ? LatLonPoint2D.normLon360(vertices.get(i).getLongitude())
              : vertices.get(i).getLongitude();
          v0y = vertices.get(i).getLatitude();
          v1x = (crossesDateline) ? LatLonPoint2D.normLon360(vertices.get(i + 1).getLongitude())
              : vertices.get(i + 1).getLongitude();
          v1y = vertices.get(i + 1).getLatitude();
          tempDistSq = Line2D.ptSegDistSq(v0x, v0y, v1x, v1y, px, py);
          tempDistSq = Math.min(tempDistSq, resultSq);
        }
        v0x = (crossesDateline) ? LatLonPoint2D.normLon360(vertices.get(0).getLongitude())
            : vertices.get(0).getLongitude();
        v0y = vertices.get(0).getLatitude();
        v1x = (crossesDateline) ? LatLonPoint2D.normLon360(vertices.get(count - 1).getLongitude())
            : vertices.get(count - 1).getLongitude();
        v1y = vertices.get(count - 1).getLatitude();
        tempDistSq = Line2D.ptSegDistSq(v0x, v0y, v1x, v1y, px, py);
        resultSq = Math.min(tempDistSq, resultSq);
      }
      // } else {
      // if(contains(p)) {
      // return 0.0;
      // }
      // for(LatLonPoint2D pt : getVertices()) {
      // resultSq = Math.min(resultSq, p.distance(pt));
      // }
      // }

      return resultSq;
    }

  }


  /**
   * Calculates the area of a polygon bounding box for an array of doubles in the orientation of: LLy, LLx, URy, URx
   *
   * @param latLonValues
   *        an array of doubles in the orientation of: LLy, LLx, URy, URx
   *
   * @return the area of the given bounding box
   */
  public static double calculateLLArrayArea(double[] latLonValues) {

    double height = latLonValues[2] - latLonValues[0];
    double width = latLonValues[3] - latLonValues[1];
    if (latLonValues[3] < latLonValues[1]) {
      width = LatLonPoint2D.normLon360(width);
    }

    return width * height;

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


}
