/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * GeoPoint2D.java
 *
 * Created on Oct 16, 2009 @ 8:32:27 AM
 */

package ucar.nc2.dt.ugrid.geom;


import java.awt.geom.Point2D;
import java.io.Serializable;

import ucar.nc2.dt.ugrid.utils.AsaMath;


/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public abstract class LatLonPoint2D extends Point2D implements Serializable {

  /**
   * Serial version ID
   */
  private static final long serialVersionUID = 4807208704519349333L;

  public final static double EQUIVALENT_TOLERANCE = 1.0e-9;

  protected LatLonPoint2D() {}

  /**
   * Factory method that will create a LatLonPoint2D.Float from a Point2D object. If pt2D is already a
   * LatLonPoint2D.Float object, it is
   * simply returned.
   * 
   * @param pt2D
   * @return a LatLonPoint2D.Float object.
   */
  public static LatLonPoint2D getFloat(Point2D pt2D) {
    if (pt2D instanceof Float) {
      return (Float) pt2D;
    } else {
      return new Float(pt2D);
    }
  }

  /**
   * Factory method that will create a LatLonPoint2D.Double from a Point2D object. If pt2D is already a
   * LatLonPoint2D.Double object, it is
   * simply returned.
   * 
   * @param pt2D
   * @return a LatLonPoint2D.Double object.
   */
  public static LatLonPoint2D getDouble(Point2D pt2D) {
    if (pt2D instanceof Double) {
      return (Double) pt2D;
    } else {
      return new Double(pt2D);
    }
  }

  /**
   * Set the latitude, longitude for this point.
   * 
   * @param lat
   *        decimal degree latitude
   * @param lon
   *        decimal degree longitude.
   */
  public abstract void setLatLon(double lat, double lon);

  /**
   * Set the latitude, longitude for this point, with the option of noting whether the values are in degrees or radians.
   * 
   * @param lat
   *        latitude
   * @param lon
   *        longitude.
   * @param isRadians
   *        true of values are radians.
   */
  public abstract void setLatLon(double lat, double lon, boolean isRadians);

  /**
   * @return decimal degree longitude as a float.
   */
  public abstract double getLongitude();

  /**
   * @return decimal degree latitude as a float.
   */
  public abstract double getLatitude();

  /**
   * @return radian longitude value.
   */
  public abstract double getRadLon();

  /**
   * @return radian latitude value.
   */
  public abstract double getRadLat();

  /**
   * Set decimal degree latitude.
   */
  public abstract void setLatitude(double lat);

  /**
   * Set decimal degree longitude.
   */
  public abstract void setLongitude(double lon);

  /**
   * The Float version of a LatLonPoint2D, where coordinates are held to float precision.
   */
  public static class Float extends LatLonPoint2D {

    /**
     * 
     */
    private static final long serialVersionUID = -2447464428275551182L;
    protected float lat;
    protected float lon;
    protected transient float radLat;
    protected transient float radLon;

    /**
     * Default constructor, values set to 0, 0.
     */
    public Float() {}

    /**
     * @param lat
     *        decimal degree latitude.
     * @param lon
     *        decimal degree longitude.
     */
    public Float(float lat, float lon) {
      setLatLon(lat, lon, false);
    }

    /**
     * @param lat
     *        latitude
     * @param lon
     *        longitude
     * @param isRadian
     *        true if values are radians, false if decimal degrees.
     */
    public Float(float lat, float lon, boolean isRadian) {
      setLatLon(lat, lon, isRadian);
    }

    /**
     * Create Float version from another LatLonPoint2D.
     * 
     * @param llp
     */
    public Float(LatLonPoint2D llp) {
      setLatLon(llp.getLatitude(), llp.getLongitude(), false);
    }

    /**
     * Create Float version from Point2D object, where the x, y values are expected to be decimal degrees.
     * 
     * @param pt2D
     */
    public Float(Point2D pt2D) {
      setLatLon(pt2D.getY(), pt2D.getX(), false);
    }

    /**
     * Point2D method, inheriting signature!!
     * 
     * @param x
     *        longitude value in decimal degrees.
     * @param y
     *        latitude value in decimal degrees.
     */
    public void setLocation(float x, float y) {
      setLatLon(y, x, false);
    }

    /**
     * Point2D method, inheriting signature!!
     * 
     * @param x
     *        longitude value in decimal degrees.
     * @param y
     *        latitude value in decimal degrees.
     */
    public void setLocation(double x, double y) {
      setLatLon((float) y, (float) x, false);
    }

    /**
     * Set lat/lon values.
     * 
     * @param lat
     *        decimal degree latitude.
     * @param lon
     *        decimal degree longitude.
     */
    public void setLatLon(float lat, float lon) {
      setLatLon(lat, lon, false);
    }

    /**
     * Set lat/lon values.
     * 
     * @param lat
     *        decimal degree latitude.
     * @param lon
     *        decimal degree longitude.
     */
    public void setLatLon(double lat, double lon) {
      setLatLon((float) lat, (float) lon, false);
    }

    /**
     * Set lat/lon values.
     * 
     * @param lat
     *        latitude.
     * @param lon
     *        longitude.
     * @param isRadians
     *        true if values are radians.
     */
    public void setLatLon(double lat, double lon, boolean isRadians) {
      if (isRadians) {
        radLat = (float) lat;
        radLon = (float) lon;
        this.lat = (float) (AsaMath.DEG_2_RAD * lat);
        this.lon = (float) (AsaMath.DEG_2_RAD * lon);
      } else {
        this.lat = (float) LatLonPoint2D.Double.normLat(lat);
        this.lon = (float) LatLonPoint2D.Double.normLon(lon);
        radLat = (float) (AsaMath.DEG_2_RAD * lat);
        radLon = (float) (AsaMath.DEG_2_RAD * lon);
      }
    }

    /**
     * Set lat/lon values.
     * 
     * @param lat
     *        latitude.
     * @param lon
     *        longitude.
     * @param isRadians
     *        true if values are radians.
     */
    public void setLatLon(float lat, float lon, boolean isRadians) {
      if (isRadians) {
        radLat = lat;
        radLon = lon;
        this.lat = (float) (AsaMath.DEG_2_RAD * lat);
        this.lon = (float) (AsaMath.DEG_2_RAD * lon);
      } else {
        this.lat = (float) normLat(lat);
        this.lon = (float) normLon(lon);
        radLat = (float) (AsaMath.DEG_2_RAD * lat);
        radLon = (float) (AsaMath.DEG_2_RAD * lon);
      }
    }

    /**
     * Point2D method.
     * 
     * @return decimal degree longitude.
     * 
     * @see java.awt.geom.Point2D#getX()
     */
    public double getX() {
      return (double) lon;
    }

    /**
     * Point2D method
     * 
     * @return decimal degree latitude.
     */
    public double getY() {
      return (double) lat;
    }

    /**
     * @return decimal degree longitude.
     */
    public double getLongitude() {
      return lon;
    }

    /**
     * @return decimal degree latitude.
     */
    public double getLatitude() {
      return lat;
    }

    /**
     * @return radian longitude.
     */
    public double getRadLon() {
      return (double) radLon;
    }

    /**
     * @return radian latitude.
     */
    public double getRadLat() {
      return (double) radLat;
    }

    /**
     * Set latitude.
     * 
     * @param lat
     *        latitude in decimal degrees
     */
    public void setLatitude(float lat) {
      this.lat = (float) normLat(lat);
      radLat = (float) (AsaMath.DEG_2_RAD * lat);
    }

    /**
     * Set latitude.
     * 
     * @param lat
     *        latitude in decimal degrees
     */
    public void setLatitude(double lat) {
      setLatitude((float) lat);
    }

    /**
     * Set longitude.
     * 
     * @param lon
     *        longitude in decimal degrees
     */
    public void setLongitude(float lon) {
      this.lon = (float) normLon(lon);
      radLon = (float) (AsaMath.DEG_2_RAD * lon);
    }

    /**
     * Set longitude.
     * 
     * @param lon
     *        longitude in decimal degrees
     */
    public void setLongitude(double lon) {
      setLongitude((float) lon);
    }

    // /**
    // * Find a LatLonPoint2D a distance and direction away from this point,
    // * based on the spherical earth model.
    // *
    // * @param dist distance, in radians.
    // * @param az radians of azimuth (direction) east of north (-PI &lt;= Az
    // * &lt; PI)
    // * @return LatLonPoint2D result
    // */
    // public LatLonPoint2D getPoint(float dist, float az) {
    // return GreatCircle.sphericalBetween(radLat, radLon, dist, az);
    // }

    public String toString() {
      return "LatLonPoint2D.Float[lat=" + lat + ",lon=" + lon + "]";
    }
  }

  /**
   * Double precision version of LatLonPoint2D.
   * 
   * @author dietrick
   */
  public static class Double extends LatLonPoint2D {
    /**
     * 
     */
    private static final long serialVersionUID = -7463055211717523471L;

    protected double lat;
    protected double lon;
    protected transient double radLat;
    protected transient double radLon;

    /**
     * Default constructor, values set to 0, 0.
     */
    public Double() {}

    /**
     * Set the latitude, longitude for this point in decimal degrees.
     * 
     * @param lat
     *        latitude
     * @param lon
     *        longitude.
     */
    public Double(double lat, double lon) {
      setLatLon(lat, lon, false);
    }

    /**
     * Set the latitude, longitude for this point, with the option of noting whether the values are in degrees or
     * radians.
     * 
     * @param lat
     *        latitude
     * @param lon
     *        longitude.
     * @param isRadians
     *        true of values are radians.
     */
    public Double(double lat, double lon, boolean isRadian) {
      setLatLon(lat, lon, isRadian);
    }

    /**
     * Create Double version from another LatLonPoint2D.
     * 
     * @param llp
     */
    public Double(LatLonPoint2D llp) {
      setLatLon(llp.getLatitude(), llp.getLongitude(), false);
    }

    /**
     * Create Double version from Point2D object, where the x, y values are expected to be decimal degrees.
     * 
     * @param pt2D
     */
    public Double(Point2D pt2D) {
      setLatLon(pt2D.getY(), pt2D.getX(), false);
    }

    /**
     * Point2D method, inheriting signature!!
     * 
     * @param x
     *        longitude value in decimal degrees.
     * @param y
     *        latitude value in decimal degrees.
     */
    public void setLocation(double x, double y) {
      setLatLon(y, x, false);
    }

    /**
     * Set latitude and longitude.
     * 
     * @param lat
     *        latitude in decimal degrees.
     * @param lon
     *        longitude in decimal degrees.
     */
    public void setLatLon(double lat, double lon) {
      setLatLon(lat, lon, false);
    }

    /**
     * Set latitude and longitude.
     * 
     * @param lat
     *        latitude.
     * @param lon
     *        longitude.
     * @param isRadians
     *        true if lat/lon values are radians.
     */
    public void setLatLon(double lat, double lon, boolean isRadians) {
      if (isRadians) {
        radLat = lat;
        radLon = lon;
        this.lat = AsaMath.DEG_2_RAD * lat;
        this.lon = AsaMath.DEG_2_RAD * lon;
      } else {
        this.lat = normLat(lat);
        this.lon = normLon(lon);
        radLat = AsaMath.DEG_2_RAD * lat;
        radLon = AsaMath.DEG_2_RAD * lon;
      }
    }

    /**
     * @return longitude in decimal degrees.
     */
    public double getX() {
      return lon;
    }

    /**
     * @return latitude in decimal degrees.
     */
    public double getY() {
      return lat;
    }

    /**
     * @return float latitude in decimal degrees.
     */
    public double getLatitude() {
      return lat;
    }

    /**
     * @return float longitude in decimal degrees.
     */
    public double getLongitude() {
      return lon;
    }

    /**
     * @return radian longitude.
     */
    public double getRadLon() {
      return radLon;
    }

    /**
     * @return radian latitude.
     */
    public double getRadLat() {
      return radLat;
    }

    /**
     * Set latitude.
     * 
     * @param lat
     *        latitude in decimal degrees
     */
    public void setLatitude(double lat) {
      this.lat = normLat(lat);
      radLat = AsaMath.DEG_2_RAD * lat;
    }

    /**
     * Set longitude.
     * 
     * @param lon
     *        longitude in decimal degrees
     */
    public void setLongitude(double lon) {
      this.lon = normLon(lon);
      radLon = AsaMath.DEG_2_RAD * lon;
    }

    // /**
    // * Find a LatLonPoint2D a distance and direction away from this point,
    // * based on the spherical earth model.
    // *
    // * @param dist distance, in radians.
    // * @param az radians of azimuth (direction) east of north (-PI &lt;= Az
    // * &lt; PI)
    // * @return LatLonPoint2D result
    // */
    // public LatLonPoint2D getPoint(double dist, double az) {
    // return GreatCircle.sphericalBetween(radLat, radLon, dist, az);
    // }

    public String toString() {
      return "LatLonPoint2D.Double[lat=" + lat + ",lon=" + lon + "]";
    }

  }

  /**
   * Set location values from another lat/lon point.
   * 
   * @param llp
   */
  public void setLatLon(LatLonPoint2D llp) {
    setLatLon(llp.getY(), llp.getX(), false);
  }

  public double distance(double px, double py) {
    return Math.sqrt(distanceSq(px, py));
  }

  public double distance(Point2D pt) {
    return distance(pt.getX(), pt.getY());
  }

  public double distanceSq(double px, double py) {
    px -= getX();
    px = (Math.abs(px) > 180) ? 360 - Math.abs(px) : px;
    py -= getY();
    return (px * px + py * py);
  }

  public double distanceSq(Point2D pt) {
    return distanceSq(pt.getX(), pt.getY());
  }


  /**
   * put longitude into the range [-180, 180] deg
   * 
   * @param lon
   *        lon to normalize
   * @return longitude in range [-180, 180] deg
   */
  static public double range180(double lon) {
    return normLon(lon);
  }

  /**
   * put longitude into the range [0, 360] deg
   * 
   * @param lon
   *        lon to normalize
   * @return longitude into the range [0, 360] deg
   */
  static public double normLon360(double lon) {
    return normLon(lon, 180.0);
  }

  /**
   * put longitude into the range [center +/- 180] deg
   * 
   * @param lon
   *        lon to normalize
   * @param center
   *        center point
   * @return longitude into the range [center +/- 180] deg
   */
  static public double normLon(double lon, double center) {
    return center + Math.IEEEremainder(lon - center, 360.0);
  }

  /**
   * Normalize the longitude to lie between +/-180
   * 
   * @param lon
   *        east latitude in degrees
   * @return normalized lon
   */
  static public double normLon(double lon) {
    if ((lon < -180.0) || (lon > 180.0)) {
      return Math.IEEEremainder(lon, 360.0);
    } else {
      return lon;
    }
  }

  /**
   * Normalize the latitude to lie between +/-90
   * 
   * @param lat
   *        north latitude in degrees
   * @return normalized lat
   */
  static public double normLat(double lat) {
    if (lat < -90.0) {
      return -90.0;
    } else if (lat > 90.0) {
      return 90.0;
    } else {
      return lat;
    }
  }

  /**
   * Check if latitude is bogus. Latitude is invalid if lat &gt; 90&deg; or if lat &lt; &minus;90&deg;.
   * 
   * @param lat
   *        latitude in decimal degrees
   * @return boolean true if latitude is invalid
   */
  public static boolean isInvalidLatitude(double lat) {
    return ((lat > 90.0) || (lat < -90.0));
  }

  /**
   * Check if longitude is bogus. Longitude is invalid if lon &gt; 180&deg; or if lon &lt; &minus;180&deg;.
   * 
   * @param lon
   *        longitude in decimal degrees
   * @return boolean true if longitude is invalid
   */
  public static boolean isInvalidLongitude(double lon) {
    return ((lon < -180.0) || (lon > 180.0));
  }

  public static LatLonPoint2D calculateGeographicCenter(LatLonPoint2D[] points) {
    LatLonPoint2D ret = null;
    if (points.length > 0) {
      double x = Math.cos(points[0].getRadLat()) * Math.cos(points[0].getRadLon());
      double y = Math.cos(points[0].getRadLat()) * Math.sin(points[0].getRadLon());
      double z = Math.sin(points[0].getRadLat());
      for (int i = 1; i < points.length; i++) {
        x += Math.cos(points[i].getRadLat()) * Math.cos(points[i].getRadLon());
        y += Math.cos(points[i].getRadLat()) * Math.sin(points[i].getRadLon());
        z += Math.sin(points[i].getRadLat());
      }
      double lon = Math.atan2(y, x);
      double hyp = Math.sqrt(x * x + y * y);
      double lat = Math.atan2(z, hyp);
      ret = new LatLonPoint2D.Double(Math.toDegrees(lat), Math.toDegrees(lon));
    }

    return ret;
  }

  /**
   * Determines whether two LatLonPoints are equal.
   * 
   * @param obj
   *        Object
   * @return Whether the two points are equal up to a tolerance of 10 <sup>-9 </sup> degrees in latitude and longitude.
   */
  public boolean equals(Object obj) {
    if (obj instanceof LatLonPoint2D) {
      LatLonPoint2D pt = (LatLonPoint2D) obj;
      return (AsaMath.almostEqual(getY(), pt.getY(), EQUIVALENT_TOLERANCE)
          && AsaMath.almostEqual(getX(), pt.getX(), EQUIVALENT_TOLERANCE));
    }
    return false;
  }

  // /**
  // * Find the distance to another LatLonPoint2D, based on a earth spherical
  // * model.
  // *
  // * @param toPoint LatLonPoint2D
  // * @return distance, in radians. You can use an com.bbn.openmap.proj.Length
  // * to convert the radians to other units.
  // */
  // public double distance(LatLonPoint2D toPoint) {
  // return GreatCircle.sphericalDistance(getRadLat(),
  // getRadLon(),
  // toPoint.getRadLat(),
  // toPoint.getRadLon());
  // }
  //
  // /**
  // * Find the azimuth to another point, based on the spherical earth model.
  // *
  // * @param toPoint LatLonPoint2D
  // * @return the azimuth `Az' east of north from this point bearing toward the
  // * one provided as an argument.(-PI &lt;= Az &lt;= PI).
  // *
  // */
  // public double azimuth(LatLonPoint2D toPoint) {
  // return GreatCircle.sphericalAzimuth(getRadLat(),
  // getRadLon(),
  // toPoint.getRadLat(),
  // toPoint.getRadLon());
  // }
}
