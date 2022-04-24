/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * LatLonRectangle2D.java
 *
 * Created on Oct 16, 2009 @ 9:06:08 AM
 */

package ucar.nc2.dt.ugrid.geom;


import java.awt.geom.Point2D;
import java.io.Serializable;

import ucar.nc2.dt.ugrid.utils.AsaArrayUtils;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;


/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class LatLonRectangle2D implements Serializable {



  /**
   * Serial version ID
   */
  private static final long serialVersionUID = -3463384339436190519L;

  private LatLonPoint2D lowerLeft;
  private LatLonPoint2D upperRight;
  private boolean crossesDateline = false;
  private boolean coversAllLons = false;
  private double width;
  private double centLon;

  @Deprecated
  public LatLonRectangle2D(LatLonPoint2D leftPoint, double dLat, double dLon) {
    this(leftPoint.getLatitude(), leftPoint.getLongitude(), dLat + leftPoint.getLatitude(),
        LatLonPoint2D.normLon(dLon + leftPoint.getLongitude()));
  }

  public LatLonRectangle2D(double leftY, double leftX, double rightY, double rightX) {
    init(leftY, leftX, rightY, rightX);
  }

  private void init(double leftY, double leftX, double rightY, double rightX) {
    double dLon = LatLonPoint2D.normLon360(rightX - leftX);

    double[] latMM = AsaArrayUtils.minMax(new double[] {leftY, rightY});
    double[] lonMM = new double[] {0, 0};

    if (dLon > 0.0) {
      lonMM[0] = leftX;
      lonMM[1] = leftX + dLon;
      crossesDateline = (lonMM[1] > 180.0);
    } else {
      lonMM[0] = leftX + dLon;
      lonMM[1] = leftX;
      crossesDateline = (lonMM[1] < -180.0);
    }

    this.lowerLeft = new LatLonPoint2D.Double(latMM[0], lonMM[0]);
    this.upperRight = new LatLonPoint2D.Double(latMM[1], lonMM[1]);

    this.width = Math.abs(dLon);
    this.centLon = LatLonPoint2D.normLon(leftX + (dLon * 0.5));
    this.coversAllLons = (this.width >= 360.0);
  }

  public LatLonRectangle2D(LatLonPoint2D leftPoint, LatLonPoint2D rightPoint) {
    this(leftPoint.getLatitude(), leftPoint.getLongitude(), rightPoint.getLatitude(), rightPoint.getLongitude());
  }

  public LatLonRectangle2D(Point2D leftPoint, Point2D rightPoint) {
    this(leftPoint.getY(), leftPoint.getX(), rightPoint.getY(), rightPoint.getX());
  }

  public LatLonRectangle2D(LatLonRectangle2D rect) {
    this(rect.getLowerLeftPoint(), rect.getUpperRightPoint());
  }

  // public LatLonRectangle2D(double leftY, double leftX, double rightY,
  // double rightX) {
  // this(new LatLonPoint2D.Double(leftY, leftX), rightY - leftY,
  // LatLonPoint2D.normLon360(rightX - leftX));
  // }

  /**
   * Returns the coordinates of this <code>LatLonRectangle2D</code> as a <code>double</code> array with a length of 4.
   * The coordinates are
   * arranged in the order of southern lat, western lon, northern lat, and eastern lon<br />
   * <br />
   * In terms of points where LL = lower left and UR = upper right, this is equivalent to the arrangement of
   * <code>double[] {lly, llx, ury, urx}</code>.
   * 
   * @return a <code>double</code> array with a length of 4 containing the lat/lon points of this rectangle. If this
   *         object contains no
   *         vertices <code>null</code> is returned.
   */
  public double[] getBoundingLatLonValues() {
    double[] srcVals = new double[4];
    srcVals[0] = getLatMin();
    srcVals[1] = getLonMin();
    srcVals[2] = getLatMax();
    srcVals[3] = getLonMax();

    return srcVals;
  }

  public LatLonPoint2D getLowerLeftPoint() {
    return lowerLeft;
  }

  public LatLonPoint2D getUpperRightPoint() {
    return upperRight;
  }

  public LatLonPoint2D getLowerRightPoint() {
    return new LatLonPoint2D.Double(lowerLeft.getLatitude(), upperRight.getLongitude());
  }

  public LatLonPoint2D getUpperLeftPoint() {
    return new LatLonPoint2D.Double(upperRight.getLatitude(), lowerLeft.getLongitude());
  }

  public boolean crossesDateline() {
    return crossesDateline;
  }

  public boolean equals(LatLonRectangle2D rect) {
    return lowerLeft.equals(rect.getLowerLeftPoint()) && upperRight.equals(rect.getUpperRightPoint());
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return getLatMax() - getLatMin();

  }

  public double getCenterLatitude() {
    return (lowerLeft.getLatitude() + (getHeight() * 0.5));
  }

  public double getCenterLongitude() {
    return centLon;
  }

  public LatLonPoint2D getCentroid() {
    /*
     * NOTE: since the only way centLon is read is through this method and
     * since centLon is normalized when constructed into a centroid
     * all methods which set centLon do not have to normalize.
     * This is an optimization and is low priority.
     */
    return new LatLonPoint2D.Double(getCenterLatitude(), centLon);
  }

  public double getLonMin() {
    return lowerLeft.getLongitude();
  }

  public double getLonMax() {
    return upperRight.getLongitude();
  }

  public double getLatMin() {
    return lowerLeft.getLatitude();
  }

  public double getLatMax() {
    return upperRight.getLatitude();
  }

  public double[] getLonCoords() {
    double l = lowerLeft.getLongitude();
    double r = upperRight.getLongitude();
    return new double[] {l, l, r, r};
  }

  public double[] getLatCoords() {
    double t = upperRight.getLatitude();
    double b = lowerLeft.getLatitude();
    return new double[] {t, b, b, t};
  }

  public boolean contains(Point2D point) {
    return contains(point.getY(), point.getX());
  }

  public boolean contains(LatLonPoint2D point) {
    return contains(point.getLatitude(), point.getLongitude());
  }

  public boolean contains(double lat, double lon) {
    if ((lat + LatLonPoint2D.EQUIVALENT_TOLERANCE) < lowerLeft.getLatitude()
        || lat - LatLonPoint2D.EQUIVALENT_TOLERANCE > upperRight.getLatitude()) {
      return false;
    }

    if (width >= 360.0) {
      return true;
    }

    if (crossesDateline) {
      return ((lon >= lowerLeft.getLongitude()) || (lon <= upperRight.getLongitude()));
    } else {
      lon = LatLonPoint2D.normLon(lon);
      return ((lon >= lowerLeft.getLongitude()) && (lon <= upperRight.getLongitude()));
    }
  }

  public boolean contains(LatLonRectangle2D rect) {
    return (width >= rect.getWidth()) && this.contains(rect.getLowerLeftPoint())
        && this.contains(rect.getUpperRightPoint());
  }

  public boolean contains(LatLonPolygon2D poly) {
    for (LatLonPoint2D p : poly.getVertices()) {
      if (!contains(p.getLatitude(), p.getLongitude())) {
        return false;
      }
    }
    return true;
  }

  public boolean containedBy(LatLonRectangle2D rect) {
    return (rect.getWidth() >= width) && rect.contains(lowerLeft) && rect.contains(upperRight);
  }

  public boolean intersects(LatLonRectangle2D rect) {
    return intersects(new LatLonPolygon2D.Double(rect));
  }

  public boolean intersects(LatLonPolygon2D poly) {
    LatLonPolygon2D thisRect = new LatLonPolygon2D.Double(this);
    return thisRect.intersects(poly);
  }

  public boolean intersects(LatLonPoint2D point) {
    LatLonPolygon2D thisRect = new LatLonPolygon2D.Double(this);
    return thisRect.intersects(point);
  }

  /* TODO: finish this */
  // public void extend(double leftY, double leftX, double rightY, double rightX) {

  // leftY = Math.min(this.lowerLeft.getLatitude(), Math.min(leftY, rightY));
  // rightY = Math.max(this.upperRight.getLatitude(), Math.max(leftY, rightY));
  //
  //
  // double dLonLeft = LatLonPoint2D.normLon360(rightX - leftX);
  // double[] lonMM = new double[] { 0, 0 };
  //
  // if (dLonLeft > 0.0) {
  // lonMM[0] = leftX;
  // lonMM[1] = leftX + dLonLeft;
  // crossesDateline = (lonMM[1] > 180.0);
  // } else {
  // lonMM[0] = leftX + dLonLeft;
  // lonMM[1] = leftX;
  // crossesDateline = (lonMM[1] < -180.0);
  // }
  //
  //
  //
  //
  // this.lowerLeft = new LatLonPoint2D.Double(leftY, lonMM[0]);
  // this.upperRight = new LatLonPoint2D.Double(rightY, lonMM[1]);
  //
  // this.width = Math.abs(dLon);
  // this.centLon = LatLonPoint2D.normLon(leftX + (dLon * 0.5));
  // this.coversAllLons = (this.width >= 360.0);
  // }

  public void extend(double pLat, double pLon) {
    if (contains(pLat, pLon)) {
      return;
    }
    if (pLat > upperRight.getLatitude()) {
      upperRight.setLatitude(pLat);
    }
    if (pLat < lowerLeft.getLatitude()) {
      lowerLeft.setLatitude(pLat);
    }

    if (coversAllLons) {
      /* do nothing */
    } else if (crossesDateline) {
      double d1 = pLon - upperRight.getLongitude();
      double d2 = lowerLeft.getLongitude() - pLon;
      if ((d1 > 0.0) && (d2 > 0.0)) {
        if (d1 > d2) {
          lowerLeft.setLongitude(pLon);
        } else {
          upperRight.setLongitude(pLon);
        }
      }
    } else {
      if (pLon > upperRight.getLongitude()) {
        if (pLon - upperRight.getLongitude() > lowerLeft.getLongitude() - pLon + 360.0) {
          crossesDateline = true;
          lowerLeft.setLongitude(pLon);
        } else {
          upperRight.setLongitude(pLon);
        }
      } else if (pLon < lowerLeft.getLongitude()) {
        if (lowerLeft.getLongitude() - pLon > pLon + 360.0 - upperRight.getLongitude()) {
          crossesDateline = true;
          upperRight.setLongitude(pLon);
        } else {
          lowerLeft.setLongitude(pLon);
        }
      }
    }

    /* Recalculate width and center */
    width = upperRight.getLongitude() - lowerLeft.getLongitude();
    centLon = (upperRight.getLongitude() + lowerLeft.getLongitude()) * 0.5;
    if (crossesDateline) {
      width += 360.0;
      centLon -= 180.0;
    }
  }

  public void extend(LatLonPoint2D point) {
    extend(point.getLatitude(), point.getLongitude());
  }



  /**
   * Extends the bounds of this <code>LatLonRectangle2D</code> by the coordinates in the bounding rectangle given by
   * <code>bounds</code>.
   * It is assumed that bounds is a non-null double array with a length of 4 which contains lat/lon coords in the
   * arrangement of LLy, LLx,
   * URy, URx.
   * 
   * @param rect
   * @param bounds
   */
  public void extend(double[] bounds) {

    /** Expand the lats (simple) */
    double latMax = bounds[2];
    double latMin = bounds[0];
    if (latMax > upperRight.getLatitude()) {
      upperRight.setLatitude(latMax);
    }
    if (latMin < lowerLeft.getLatitude()) {
      lowerLeft.setLatitude(latMin);
    }

    if (coversAllLons) {
      return;
    }


    /** Expand the lons (check for dateline crossing) */
    double lonMin = getLonMin(); /* technically west-most NOT min */
    double lonMax = getLonMax(); /* technically east-most NOT max */
    double nLonMin = LatLonPoint2D.normLon(bounds[1], lonMin);
    double nLonMax = LatLonPoint2D.normLon(bounds[3], lonMax);
    lonMin = Math.min(lonMin,
        nLonMin); /* Dont normalize lons here, they are auto normalized on LatLonPoint2D.Double.setLongitude() */
    lonMax = Math.max(lonMax,
        nLonMax); /* Dont normalize lons here, they are auto normalized on LatLonPoint2D.Double.setLongitude() */
    width = LatLonPoint2D.normLon360(lonMax - lonMin);
    coversAllLons = width >= 360.0;
    if (coversAllLons) {
      width = 360.0;
      lonMin = -180.0;
      /* FIXME: why are we reorienting the rect when the globe is covered? Does it matter? */
    }

    // double lonMin = getLonMin();
    // double lonMax = getLonMax();
    // double nLonMin = LatLonPoint2D.normLon(bounds[1], lonMin);
    // double nLonMax = nLonMin + LatLonPoint2D.normLon360(bounds[3] - bounds[1]);
    // lonMin = Math.min(lonMin, nLonMin);
    // lonMax = Math.max(lonMax, nLonMax);
    // width = lonMax - lonMin;
    // coversAllLons = width >= 360.0;
    // if (coversAllLons) {
    // width = 360.0;
    // lonMin = -180.0;
    // } else {
    // lonMin = LatLonPoint2D.normLon(lonMin);
    // }

    lowerLeft.setLongitude(lonMin);
    upperRight.setLongitude(lonMin + width);
    centLon = lonMin + width * 0.5;
    crossesDateline = lowerLeft.getLongitude() > upperRight.getLongitude();
  }



  public void extend(LatLonRectangle2D rect) {

    /** Expand the lats (simple) */
    double latMax = rect.getLatMax();
    double latMin = rect.getLatMin();
    if (latMax > upperRight.getLatitude()) {
      upperRight.setLatitude(latMax);
    }
    if (latMin < lowerLeft.getLatitude()) {
      lowerLeft.setLatitude(latMin);
    }

    if (coversAllLons) {
      return;
    }


    /** Expand the lons (check for dateline crossing) */
    double lonMin = getLonMin(); /* technically west-most NOT min */
    double lonMax = getLonMax(); /* technically east-most NOT max */
    double nLonMin = LatLonPoint2D.normLon(rect.getLonMin(), lonMin);
    double nLonMax = LatLonPoint2D.normLon(rect.getLonMax(), lonMax);
    lonMin = Math.min(lonMin,
        nLonMin); /* Dont normalize lons here, they are auto normalized on LatLonPoint2D.Double.setLongitude() */
    lonMax = Math.max(lonMax,
        nLonMax); /* Dont normalize lons here, they are auto normalized on LatLonPoint2D.Double.setLongitude() */
    width = LatLonPoint2D.normLon360(lonMax - lonMin);
    coversAllLons = width >= 360.0;
    if (coversAllLons) {
      width = 360.0;
      lonMin = -180.0;
      /* FIXME: why are we reorienting the rect when the globe is covered? Does it matter? */
    }

    // double lonMin = getLonMin();
    // double lonMax = getLonMax();
    // double nLonMin = LatLonPoint2D.normLon(rect.getLonMin(), lonMin);
    // double nLonMax = nLonMin + rect.getWidth();
    // lonMin = Math.min(lonMin, nLonMin);
    // lonMax = Math.max(lonMax, nLonMax);
    // width = lonMax - lonMin;
    // coversAllLons = width >= 360.0;
    // if (coversAllLons) {
    // width = 360.0;
    // lonMin = -180.0;
    // } else {
    // lonMin = LatLonPoint2D.normLon(lonMin);
    // }

    lowerLeft.setLongitude(lonMin);
    upperRight.setLongitude(lonMin + width);
    centLon = lonMin + width * 0.5;
    crossesDateline = lowerLeft.getLongitude() > upperRight.getLongitude();
  }

  public void extend(LatLonPolygon2D poly) {
    double[] pys = poly.getLatCoords();
    double[] mmy = AsaArrayUtils.minMax(pys);
    if (mmy[1] > upperRight.getLatitude()) {
      upperRight.setLatitude(mmy[1]);
    }
    if (mmy[0] < lowerLeft.getLatitude()) {
      lowerLeft.setLatitude(mmy[0]);
    }

    if (coversAllLons) {
      return;
    }

    double lonMin = getLonMin();
    // double lonMax = (crossesDateline) ?
    // LatLonPoint2D.normLon360(getLonMax()) : getLonMax();
    double lonMax = getLonMax();
    double[] pxs = poly.getLonCoords();
    if (shouldCrossDateline(poly)) {
      for (int i = 0; i < pxs.length; i++) {
        pxs[i] = LatLonPoint2D.normLon360(pxs[i]);
      }
      lonMin = LatLonPoint2D.normLon360(lonMin);
      lonMax = LatLonPoint2D.normLon360(lonMax);
    }
    double[] mmx = AsaArrayUtils.minMax(pxs);
    lonMin = Math.min(lonMin, mmx[0]);
    lonMax = Math.max(lonMax, mmx[1]);
    width = lonMax - lonMin;
    coversAllLons = width >= 360.0;
    if (coversAllLons) {
      width = 360.0;
      lonMin = -180.0;
    } else {
      lonMin = LatLonPoint2D.normLon(lonMin);
    }

    lowerLeft.setLongitude(lonMin);
    upperRight.setLongitude(lonMin + width);
    centLon = lonMin + width * 0.5;
    crossesDateline = lowerLeft.getLongitude() > upperRight.getLongitude();
  }

  public boolean shouldCrossDateline(LatLonPolygon2D poly) {
    // /* If the polygon or rectangle already crosses the dateline, obviously it "should" cross the dateline */
    // if (poly.crossesDateline() || this.crossesDateline()) {
    // return true;
    // }
    //
    // /* Next, get the centroids of the shapes */
    // LatLonPoint2D polyC = poly.getCentroid();
    // LatLonPoint2D rectC = this.getCentroid();
    // /* If they're both + or both -, the dateline isn't an issue */
    // if ((polyC.getLongitude() > 0 & rectC.getLongitude() > 0) || (polyC.getLongitude() < 0 & rectC.getLongitude() <
    // 0)) {
    // return false;
    // }
    //
    // double dist = polyC.distance(rectC);
    // LatLonPoint2D polyC180 = new LatLonPoint2D.Double(polyC.getLatitude(), 180);
    // LatLonPoint2D polyC180N = new LatLonPoint2D.Double(polyC.getLatitude(), -180);
    // double pDistTo180 = poly.distance(polyC180);
    // double pDistTo180N = poly.distance(polyC180N);
    // LatLonPoint2D rectC180 = new LatLonPoint2D.Double(rectC.getLatitude(), 180);
    // LatLonPoint2D rectC180N = new LatLonPoint2D.Double(rectC.getLatitude(), -180);
    // double rDistTo180 = rectC.distance(rectC180);
    // double rDistTo180N = rectC.distance(rectC180N);
    //
    // double minP = Math.min(pDistTo180, pDistTo180N);
    // double minR = Math.min(rDistTo180, rDistTo180N);
    //
    // if ((minP + minR) < Math.abs(dist)) {
    // return true;
    // }
    //
    //
    // return false;

    double[] polyBounds = poly.getBoundingLatLonValues();
    double[] rectBounds = new double[4];
    rectBounds[0] = this.getLatMin();
    rectBounds[1] = this.getLonMin();
    rectBounds[2] = this.getLatMax();
    rectBounds[3] = this.getLonMax();

    /** If either shape crosses the union will cross */
    if (polyBounds[3] < polyBounds[1] || rectBounds[3] < rectBounds[1]) {
      return true;
    }


    double minLon;
    double maxLon;


    /** Normalize the poly lon bounds over the rectangle lon bounds so that they can be compared objectively */
    double polyNormMinLon = LatLonPoint2D.normLon(polyBounds[1], rectBounds[1]);
    double polyNormMaxLon = LatLonPoint2D.normLon(polyBounds[3], rectBounds[3]);


    /** Determine the ACTUAL min and max lon bounds for the union of the two shapes */
    minLon = Math.min(rectBounds[1], polyNormMinLon);
    maxLon = Math.max(rectBounds[3], polyNormMaxLon);


    /** Normalize the values over map space (-180 to 180) */
    minLon = LatLonPoint2D.normLon(minLon);
    maxLon = LatLonPoint2D.normLon(maxLon);


    /** Does the normalized union of the two shapes cross the dateline? */
    if (minLon <= maxLon) {
      return false;
    } else {
      return true;
    }
  }



  public double getArea() {
    return (getWidth() * getHeight());
  }

  /**
   * Creates and returns a copy of this <tt>LatLonRectangle2D</tt>
   * 
   * @return a copy of this <tt>LatLonRectangle2D</tt>
   */
  public LatLonRectangle2D copy() {
    return new LatLonRectangle2D(this);
  }

  /**
   * Shifts this rectangle by the distances <b>deltaLat</b> and <b>deltaLon</b>.
   * 
   * @param deltaLat
   *        the distance (in Decimal Degrees) by which to move this rectangle vertically. + to the North, - to the South
   * @return a reference to <b>this</b> <tt>LatLonRectangle2D</tt> - for convenience
   */
  public LatLonRectangle2D translate(double deltaLat, double deltaLon) {
    init(this.getLowerLeftPoint().getLatitude() + deltaLat, this.getLowerLeftPoint().getLongitude() + deltaLon,
        this.getUpperRightPoint().getLatitude() + deltaLat, this.getUpperRightPoint().getLongitude() + deltaLon);
    return this;
  }

  /**
   * Uses {@link #translate(double, double)} to shift this <tt>LatLonRectangle2D</tt> so that the centroid matches the
   * centroid of the
   * <b>referenceRectangle</b>. Delegates to {@link #recenter(Point2D)}
   * 
   * @param referenceRectangle
   *        the <tt>LatLonRectangle2D</tt> to move this <tt>LatLonRectangle2D</tt>'s centroid to
   * @return this <tt>LatLonRectangle2D</tt>, for convenience
   */
  public LatLonRectangle2D recenter(LatLonRectangle2D referenceRectangle) {
    return recenter(referenceRectangle.getCentroid());
  }

  /**
   * Uses {@link #translate(double, double)} to shift this <tt>LatLonRectangle2D</tt> so that the centroid matches
   * <b>newCenterPoint</b>.
   * Delegates to {@link #recenter(double, double)}
   * 
   * @param newCenterPoint
   *        the new center point
   * @return this <tt>LatLonRectangle2D</tt>, for convenience
   */
  public LatLonRectangle2D recenter(Point2D newCenterPoint) {
    return recenter(newCenterPoint.getY(), newCenterPoint.getX());
  }

  /**
   * Uses {@link #translate(double, double)} to shift this <tt>LatLonRectangle2D</tt> so that the centroid matches the
   * location specified
   * by <b>centerLat</b> and <b>centerLon</b>.
   * 
   * @param centerLat
   *        the new center latitude
   * @param centerLon
   *        the new center longitude
   * @return this <tt>LatLonRectangle2D</tt>, for convenience
   */
  public LatLonRectangle2D recenter(double centerLat, double centerLon) {
    LatLonPoint2D tcent = this.getCentroid();
    double dlat = centerLat - tcent.getLatitude();
    double dlon = centerLon - tcent.getLongitude();

    return translate(dlat, dlon);
  }

  public String toString() {
    return "ll: " + lowerLeft + " | ur: " + upperRight;
  }

  public LatLonRect toLatLonRect() {
    return new LatLonRect(
        new LatLonPointImpl(this.getUpperLeftPoint().getLatitude(), this.getUpperLeftPoint().getLongitude()),
        new LatLonPointImpl(this.getCenterLatitude(), this.getCenterLongitude()));
  }

}
