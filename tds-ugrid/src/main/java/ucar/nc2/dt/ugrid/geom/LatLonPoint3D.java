/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * LatLonPoint3D.java
 *
 * Created on Oct 22, 2009 @ 10:15:49 AM
 */

package ucar.nc2.dt.ugrid.geom;


import java.awt.geom.Point2D;


/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class LatLonPoint3D extends LatLonPoint2D.Double {

  private double depth;

  public LatLonPoint3D() {
    super(0.0, 0.0, false);
    setDepth(0.0);
  }

  public LatLonPoint3D(double lat, double lon) {
    super(lat, lon, false);
    setDepth(0.0);
  }

  public LatLonPoint3D(double lat, double lon, double depth) {
    super(lat, lon, false);
    setDepth(depth);
  }

  public LatLonPoint3D(LatLonPoint2D point, double depth) {
    super(point.getLatitude(), point.getLongitude(), false);
    setDepth(depth);
  }

  public LatLonPoint3D(Point2D point, double depth) {
    super(point.getY(), point.getX(), false);
    setDepth(depth);
  }

  public LatLonPoint3D(LatLonPoint3D point) {
    super(point.getLatitude(), point.getLongitude(), false);
    setDepth(point.getDepth());
  }

  public LatLonPoint2D getHorizontalPosition() {
    return new LatLonPoint2D.Double(getLatitude(), getLongitude());
  }

  public void setDepth(double depth) {
    this.depth = depth;
  }

  public double getDepth() {
    return depth;
  }

  @Override
  public LatLonPoint3D clone() {
    return (LatLonPoint3D) super.clone();
  }
}
