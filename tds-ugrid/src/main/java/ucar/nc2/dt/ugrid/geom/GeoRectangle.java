/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * GeoRectangle.java
 *
 * Created on Jun 4, 2009 @ 10:03:30 AM
 */

package ucar.nc2.dt.ugrid.geom;


import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;


// import com.asascience.utils.MethodNotImplementedException;



/**
 * The <code>GeoRectangle</code> class provides a mechanism for manipulating rectangular regions on a geographically
 * referenced plane.
 * 
 * @author TPL <tlarocque@asascience.com>
 * @deprecated This class does not handle the dateline properly. Use the LatLonRectangle2D class instead.
 */
public abstract class GeoRectangle implements Shape, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -3499332575290254914L;
  /**
   * The polygon backing for this rectangle object
   */
  protected Polygon2D polygon;



  /**
   * Extends this rectangle by the bounding box of the given polygon. The resulting rectangle is the union of this
   * <code>GeoRectangle</code> with the rectangle representing the bounds of the given <code>Polygon2D</code>
   * 
   * 
   * @param polygon
   *        A <code>polygon</code> object
   */
  public abstract void add(Polygon2D polygon);



  /**
   * Adds a <code>GeoRectangle</code> object to this <code>GeoRectangle</code>. The resulting <code>GeoRectangle</code>
   * is the union of
   * the two <code>GeoRectangle</code> objects.
   * 
   * @param r
   *        the <code>Rectangle2D</code> to add to this <code>Rectangle2D</code>.
   */
  public abstract void add(GeoRectangle r);



  @Override
  public abstract boolean equals(Object obj);


  /**
   * Retrieves the lower-left point of this rectangle
   * 
   * @return a <code>Point2D</code> object
   */
  public abstract Point2D getLowerLeft();



  /**
   * Retrieves the upper-right point of this rectangle
   * 
   * @return a <code>Point2D</code> object
   */
  public abstract Point2D getUpperRight();



  /**
   * Determines the lowest x value within the points of this rectangle
   * 
   * @return the lowest x value
   */
  public abstract double getMinX();



  /**
   * Determines the lowest x value within the points of this rectangle
   * 
   * @return the lowest x value
   */
  public abstract double getMinY();



  /**
   * Determines the lowest x value within the points of this rectangle
   * 
   * @return the lowest x value
   */
  public abstract double getMaxX();



  /**
   * Determines the lowest x value within the points of this rectangle
   * 
   * @return the lowest x value
   */
  public abstract double getMaxY();

  public double[] getXCoords() {
    return polygon.getXCoords();
  }

  public double[] getYCoords() {
    return polygon.getYCoords();
  }

  /**
   * Calculates the height of this rectangle
   * 
   * @return the height of this rectangle
   */
  public double getHeight() {
    return getMaxY() - getMinY();

  }



  /**
   * Calculates the width of this rectangle
   * 
   * @return the width of this rectangle
   */
  public double getWidth() {
    return getMaxX() - getMinX();
  }



  /**
   * Calculates the area of this <code>GeoRectangle</code> object
   * 
   * @return the area of this <code>GeoRectangle</code>
   */
  public double getArea() {
    return getWidth() * getHeight();
  }

  public Point2D getCentroid() {
    return polygon.getCentroid();
  }

  public boolean contains(Point2D p) {
    if (isEmpty()) {
      return false;
    }
    return polygon.contains(p);
  }

  public boolean contains(Rectangle2D r) {
    if (isEmpty()) {
      return false;
    }
    return polygon.contains(r);
  }



  // TODO javadoc
  public boolean contains(GeoRectangle r) {

    if (isEmpty()) {
      return false;
    }


    return (r.getMinX() >= getMinX() && r.getMinY() >= getMinY() && r.getMaxX() <= getMaxX()
        && r.getMaxY() <= getMaxY());
  }


  public boolean contains(Polygon2D p) {
    return contains(p.getBoundsGeo());
  }



  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Shape#contains(double, double)
   */
  public boolean contains(double x, double y) {
    return polygon.contains(x, y);
  }



  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Shape#contains(double, double, double, double)
   */
  /**
   * not implemented; returns false
   */
  public boolean contains(double x, double y, double w, double h) {
    // TODO Auto-generated method stub
    // throw new MethodNotImplementedException("Method not implemented!");
    return false;
  }


  /**
   * Retrieves a copy of this <code>GeoRectangle</code> object with the exact same bounding coordinates. The backing
   * polygon associated
   * with this copy may have its nodes in a different order than the original.
   * 
   * @return a <code>GeoRectangle</code>
   */
  public abstract GeoRectangle copy();

  /**
   * Retrieves a copy of the 4-point polygon representation of this <code>GeoRectangle</code>
   * 
   * @return a <code>Polygon2D</code> object
   */
  public abstract Polygon2D getBackingPolygon();



  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Shape#getBounds()
   */
  /**
   * not implemented; returns false
   */
  public Rectangle getBounds() {
    // TODO Auto-generated method stub
    // throw new MethodNotImplementedException("Method not implemented!");
    return null;
  }



  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Shape#getBounds2D()
   */
  /**
   * not implemented; returns false
   */
  public Rectangle2D getBounds2D() {
    // TODO Auto-generated method stub
    return null;
    // throw new MethodNotImplementedException("Method not implemented!");
  }



  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Shape#getPathIterator(java.awt.geom.AffineTransform)
   */
  /**
   * not implemented; returns false
   */
  public PathIterator getPathIterator(AffineTransform at) {
    // TODO Auto-generated method stub
    return null;
    // throw new MethodNotImplementedException("Method not implemented!");
  }



  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Shape#getPathIterator(java.awt.geom.AffineTransform, double)
   */
  /**
   * not implemented; returns false
   */
  public PathIterator getPathIterator(AffineTransform at, double flatness) {
    // TODO Auto-generated method stub
    return null;
    // throw new MethodNotImplementedException("Method not implemented!");
  }



  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Shape#intersects(java.awt.geom.Rectangle2D)
   */
  /**
   * not implemented; returns false
   */
  public boolean intersects(Rectangle2D r) {
    // TODO Auto-generated method stub
    return false;
    // throw new MethodNotImplementedException("Method not implemented!");
  }



  // TODO javadoc
  public boolean intersects(GeoRectangle r) {

    if (isEmpty()) {
      return false;
    }

    return (r.getMaxX() > getMinX() && r.getMaxY() > getMinY() && r.getMinX() < getMaxX() && r.getMinY() < getMaxY());

  }



  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Shape#intersects(double, double, double, double)
   */
  /**
   * not implemented; returns false
   */
  public boolean intersects(double x, double y, double w, double h) {
    // TODO Auto-generated method stub
    return false;
    // throw new MethodNotImplementedException("Method not implemented!");
  }



  /**
   * Determines whether or not this <code>GeoRectangle</code> is empty.
   * 
   * @return <code>true</code> if this <code>GeoRectangle</code> is empty; <code>false</code> otherwise.
   */
  public boolean isEmpty() {
    return (polygon.getBounds2D().isEmpty());
  }



  /**
   * Set the coordinates of this rectangle by its lower-left and upper-right points
   * 
   * @param xCorner1
   *        the x coordinate of the first corner of this rectangle
   * @param yCorner1
   *        the y coordinate of the first corner of this rectangle
   * @param xCorner2
   *        the x coordinate of the opposite corner of this rectangle
   * @param yCorner2
   *        the y coordinate of the opposite corner of this rectangle
   */
  public abstract void setRect(double xCorner1, double yCorner1, double xCorner2, double yCorner2);

  /**
   * Set the coordinates of this rectangle to the coordinates of the two points. These points should be "opposite"
   * corners of the
   * rectangle.
   * 
   * @param corner1
   *        the first corner (i.e. lower_left)
   * @param corner2
   *        the second corner (i.e. upper_right)
   */
  public void setRect(Point2D corner1, Point2D corner2) {
    setRect(corner1.getX(), corner1.getY(), corner2.getX(), corner2.getY());
  }

  /**
   * Set the coordinates of this rectangle to the same coordinates of the given rectangle
   * 
   * @param rect
   *        a <code>GeoRectangle</code> object
   */
  public void setRect(GeoRectangle rect) {
    setRect(rect.getMinX(), rect.getMinY(), rect.getMaxX(), rect.getMaxY());
  }


  public String toString() {
    // FIXME: write this method
    // return getClass().getName() + ": {"
    return super.toString();
  }

  /**
   * Unions the pair of source <code>Rectangle2D</code> objects and puts the result into the specified destination
   * <code>Rectangle2D</code> object. One of the source rectangles can also be the destination to avoid creating a third
   * Rectangle2D
   * object, but in this case the original points of this source rectangle will be overwritten by this method.
   * 
   * @param src1
   *        the first of a pair of <code>Rectangle2D</code> objects to be combined with each other
   * @param src2
   *        the second of a pair of <code>Rectangle2D</code> objects to be combined with each other
   * @param dest
   *        the <code>Rectangle2D</code> that holds the results of the union of <code>src1</code> and <code>src2</code>
   * @since 1.2
   */
  public static void union(GeoRectangle src1, GeoRectangle src2, GeoRectangle dest) {
    // TODO: rewrite this method by getting the min of src1.min and src1.min etc.. [TPL]
    dest.setRect(src1);
    dest.add(src2);
  }



  /**
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * @author tlarocque
   * 
   */
  public static class Double extends GeoRectangle {

    /**
     * Constructs a GeoRectangle.Double with all four points located at {0,0}
     */
    public Double() {
      this(0, 0, 0, 0);
    }

    /**
     * Constructs a GeoRectangle.Double from the given location. All 4 vertices of the rectangle are set to the same
     * value.
     * 
     * @param x
     *        the x location
     * @param y
     *        the y location
     */
    public Double(double x, double y) {
      this(x, y, x, y);
    }

    /**
     * Constructs a GeoRectangle.Double from the given location. All 4 vertices of the rectangle are set to the same
     * value.
     * 
     * @param loc
     *        the starting location
     */
    public Double(Point2D loc) {
      this(loc.getX(), loc.getY(), loc.getX(), loc.getY());
    }

    /**
     * Constructs a GeoRectangle.Double from the given corner points.
     * 
     * @param corner1
     *        a point representing any corner of the rectangle
     * @param corner2
     *        a point representing the point opposite <code>corner1</code>
     */
    public Double(Point2D corner1, Point2D corner2) {
      this(corner1.getX(), corner1.getY(), corner2.getX(), corner2.getY());
    }



    /**
     * Constructor accepting two double arrays, one for x values and one for y values. Only the FIRST TWO members of
     * each array are
     * used.
     * 
     * @param xVals
     *        the x coordinates of two opposite corners of this rectangle
     * @param yVals
     *        the y coordinates of two opposite corners of this rectangle
     */
    public Double(double[] xVals, double[] yVals) {
      this(xVals[0], yVals[0], xVals[1], yVals[1]);
    }



    /**
     * Initializes a new GeoRectangle.Double with the given coordinates for the lower left and upper right points of the
     * rectangle
     * 
     * @param xCorner1
     *        the x coordinate of the first corner of this rectangle
     * @param yCorner1
     *        the y coordinate of the first corner of this rectangle
     * @param xCorner2
     *        the x coordinate of the opposite corner of this rectangle
     * @param yCorner2
     *        the y coordinate of the opposite corner of this rectangle
     */
    public Double(double xCorner1, double yCorner1, double xCorner2, double yCorner2) {
      super.polygon = new Polygon2D.Double(4);
      setRect(xCorner1, yCorner1, xCorner2, yCorner2);
    }



    /**
     * Extends this rectangle by the bounding box of the given polygon. The resulting rectangle is the union of this
     * <code>GeoRectangle</code> with the rectangle representing the bounds of the given <code>Polygon2D</code>
     * 
     * @param polygon
     *        A <code>polygon</code> object
     */
    public void add(Polygon2D polygon) {

      if (polygon.getVertexCount() <= 0) {
        return;
      }

      /* Calculate the new min and max values */
      double xMin = Math.min(getMinX(), polygon.getMinX());
      double xMax = Math.max(getMaxX(), polygon.getMaxX());
      double yMin = Math.min(getMinY(), polygon.getMinY());
      double yMax = Math.max(getMaxY(), polygon.getMaxY());
      /* Apply the new values to this GeoRectangle */
      setRect(xMin, yMin, xMax, yMax);

      /* Build a bounding GeoRectangle from the given polygon */
      /* **************************************************** */
      // double xLowerLeft = java.lang.Double.MAX_VALUE;
      // double yLowerLeft = java.lang.Double.MAX_VALUE;
      // double xUpperRight = java.lang.Double.MIN_VALUE;
      // double yUpperRight = java.lang.Double.MIN_VALUE;


      // for (int ctr = 0; ctr < polygon.getVertexCount(); ctr++) {
      // double x = polygon.getX(ctr);
      // double y = polygon.getY(ctr);
      // xLowerLeft = Math.min(getMinX(), x);
      // yLowerLeft = Math.min(getMinY(), y);
      // xUpperRight = Math.max(getMaxX(), x);
      // yUpperRight = Math.max(getMaxY(), y);
      // }


      /* Resize this GeoRectangle accordingly */
      /* ************************************ */
      // setRect(xLowerLeft, yLowerLeft, xUpperRight, yUpperRight);
    }



    /**
     * Adds a <code>Rectangle2D</code> object to this <code>Rectangle2D</code>. The resulting <code>Rectangle2D</code>
     * is the union of
     * the two <code>GeoRectangle</code> objects.
     * 
     * @param r
     *        the <code>Rectangle2D</code> to add to this <code>Rectangle2D</code>.
     */
    public void add(GeoRectangle r) {
      add(r.getBackingPolygon());
      // double x1 = Math.min(getMinX(), r.getMinX());
      // double y1 = Math.min(getMinY(), r.getMinY());
      // double x2 = Math.max(getMaxX(), r.getMaxX());
      // double y2 = Math.max(getMaxY(), r.getMaxY());
      // setRect(x1, y1, x2, y2);
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.utils.geom.GeoRectangle#copy()
     */
    @Override
    public GeoRectangle.Double copy() {
      // double x1, y1, x2, y2;
      // x1 = getLowerLeft().getX();
      // y1 = getLowerLeft().getY();
      // x2 = getUpperRight().getX();
      // y2 = getUpperRight().getY();

      // return new GeoRectangle.Double(x1, y1, x2, y2);
      return new GeoRectangle.Double(getLowerLeft(), getUpperRight());
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.data.GeoRectangle#setRect(double, double, double, double)
     */
    public void setRect(double xCorner1, double yCorner1, double xCorner2, double yCorner2) {

      if (null == super.polygon) {
        throw new IllegalStateException("The backing polygon of this rectangle has not been initialized");
      }

      polygon.reset();

      polygon.moveTo(xCorner1, yCorner1);
      polygon.lineTo(xCorner1, yCorner2);
      polygon.lineTo(xCorner2, yCorner2);
      polygon.lineTo(xCorner2, yCorner1);

      polygon.closePath();
    }



    /**
     * Determines if the given object possesses the same coordinate bounds as this <code>GeoRectangle</code>. The two
     * objects do not
     * necessarily have to be equivalent as given by <code>this == obj</code>, however, if they are this will return
     * true
     * 
     * @param obj
     *        another <code>Object</code>
     * 
     * @return <code>true</code> if the given object is an instance of <code>GeoRectangle</code> and has the same
     *         rectangular bounds as
     *         this <code>GeoRectangle</code>, otherwise <code>false</code>.
     */
    @Override
    public boolean equals(Object obj) {

      if (this == obj) {
        return true;
      }

      /*
       * Its ok if the object is not GeoRectangle.Double
       * .equals() is more concerned with the bounds of the rectangular
       * object.
       */
      if (obj instanceof GeoRectangle) {
        GeoRectangle rect = (GeoRectangle) obj;
        return getMinX() == rect.getMinX() && getMinY() == rect.getMinY() && getMaxX() == rect.getMaxX()
            && getMaxY() == rect.getMaxY();
      } else {
        return false;
      }
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.utils.geom.GeoRectangle#getBackingPolygon()
     */
    @Override
    public Polygon2D.Double getBackingPolygon() {
      return ((Polygon2D.Double) polygon).copy();
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.data.GeoRectangle#getLowerLeft()
     */
    @Override
    public Point2D getLowerLeft() {
      return new Point2D.Double(getMinX(), getMinY());
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.data.GeoRectangle#getUpperRight()
     */
    @Override
    public Point2D getUpperRight() {
      return new Point2D.Double(getMaxX(), getMaxY());
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.data.GeoRectangle#getMinX()
     */
    public double getMinX() {
      double min = java.lang.Double.POSITIVE_INFINITY;
      for (int ctr = 0; ctr < polygon.getVertexCount(); ctr++) {
        min = Math.min(min, polygon.getX(ctr));
      }

      return min;
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.data.GeoRectangle#getMinY()
     */
    public double getMinY() {
      double min = java.lang.Double.POSITIVE_INFINITY;
      for (int ctr = 0; ctr < polygon.getVertexCount(); ctr++) {
        min = Math.min(min, polygon.getY(ctr));
      }

      return min;
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.data.GeoRectangle#getMaxX()
     */
    public double getMaxX() {
      double max = java.lang.Double.NEGATIVE_INFINITY;
      for (int ctr = 0; ctr < polygon.getVertexCount(); ctr++) {
        max = Math.max(max, polygon.getX(ctr));
      }

      return max;
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.asascience.data.GeoRectangle#getMaxY()
     */
    public double getMaxY() {
      double max = java.lang.Double.NEGATIVE_INFINITY;
      for (int ctr = 0; ctr < polygon.getVertexCount(); ctr++) {
        max = Math.max(max, polygon.getY(ctr));
      }

      return max;
    }


  }



}
