/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * Point3D.java
 *
 * Created on Oct 13, 2009 @ 10:17:56 AM
 */

package ucar.nc2.dt.ugrid.geom;


import java.io.Serializable;


/**
 * The <code>Point3D</code> class defines a point representing a location in {@code (x,y,z)} coordinate space.
 * <p>
 * This class is only the abstract superclass for all objects that store a 3D coordinate. The actual storage
 * representation of the
 * coordinates is left to the subclass.
 * <p>
 * This class is modeled after the <code>java.awt.geom.Point3D</code> class.
 * <p>
 * NOTE: This class should NOT be used for geographic information. Use LatLonPoint3D instead.
 * 
 * @author CBM <cmueller@asascience.com>
 * 
 */

public abstract class Point3D implements Cloneable {

  public static class Float extends Point3D implements Serializable {
    /**
     * JDK 1.6 serialVersionUID
     */
    private static final long serialVersionUID = 3109429321035889896L;

    /**
     * The X coordinate of this <code>Point3D</code>.
     */
    public float x;

    /**
     * The Y coordinate of this <code>Point3D</code>.
     */
    public float y;

    /**
     * The Z coordinate of this <code>Point3D</code>.
     */
    public float z;

    /**
     * Constructs and initializes a <code>Point3D</code> with coordinates (0,&nbsp;0,&nbsp;0).
     */
    public Float() {}

    /**
     * Constructs and initializes a <code>Point3D</code> with the specified coordinates.
     * 
     * @param x
     *        the X coordinate of the newly constructed <code>Point3D</code>
     * @param y
     *        the Y coordinate of the newly constructed <code>Point3D</code>
     * @param z
     *        the Z coordinate of the newly constructed <code>Point3D</code>
     */
    public Float(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    /**
     * {@inheritDoc}
     */
    public double getX() {
      return (double) x;
    }

    /**
     * {@inheritDoc}
     */
    public double getY() {
      return (double) y;
    }

    /**
     * {@inheritDoc}
     */
    public double getZ() {
      return (double) z;
    }

    /**
     * {@inheritDoc}
     */
    public void setLocation(double x, double y, double z) {
      this.x = (float) x;
      this.y = (float) y;
      this.z = (float) z;
    }

    /**
     * Sets the location of this <code>Point3D</code> to the specified <code>float</code> coordinates.
     * 
     * @param x
     *        the new X coordinate of this {@code Point3D}
     * @param y
     *        the new Y coordinate of this {@code Point3D}
     * @param z
     *        the new Z coordinate of this {@code Point3D}
     */
    public void setLocation(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    /**
     * {@inheritDoc}
     */
    public void moveBy(double dx, double dy, double dz) {
      this.x = this.x + (float) dx;
      this.y = this.y + (float) dy;
      this.z = this.z + (float) dz;
    }

    /**
     * Moves the <code>Point3D</code> by the amount indicated by the specified <code>float</code> magnitudes.
     * <p>
     * NOTE: Units are NOT considered. The units of the {@code origin} and {@code (u, v, w)} components are assumed to
     * be the same.<br>
     * Therefore conversions (i.e. from DD to Meters) should happen BEFORE calling this method.
     * 
     * @param dx
     *        the amount to move this {@code Point3D} in the X direction
     * @param dy
     *        the amount to move this {@code Point3D} in the Y direction
     * @param dz
     *        the amount to move this {@code Point3D} in the Z direction
     */
    public void moveBy(float dx, float dy, float dz) {
      this.x = this.x + dx;
      this.y = this.y + dy;
      this.z = this.z + dz;
    }

    /**
     * Returns a <code>String</code> that represents the value of this <code>Point3D</code>.
     * 
     * @return a string representation of this <code>Point3D</code>.
     */
    public String toString() {
      return "Point3D.Float[" + x + ", " + y + ", " + z + "]";
    }
  }

  /**
   * The <code>Double</code> class defines a point specified in <code>double</code> precision.
   */
  public static class Double extends Point3D implements Serializable {
    /**
     * JDK 1.6 serialVersionUID
     */
    private static final long serialVersionUID = -4388950813735371788L;

    /**
     * The X coordinate of this <code>Point3D</code>.
     */
    public double x;

    /**
     * The Y coordinate of this <code>Point3D</code>.
     */
    public double y;

    /**
     * The Z coordinate of this <code>Point3D</code>.
     */
    public double z;

    /**
     * Constructs and initializes a <code>Point3D</code> with coordinates (0,&nbsp;0,&nbsp;0).
     */
    public Double() {}

    /**
     * Constructs and initializes a <code>Point3D</code> with the specified coordinates.
     * 
     * @param x
     *        the X coordinate of the newly constructed <code>Point3D</code>
     * @param y
     *        the Y coordinate of the newly constructed <code>Point3D</code>
     * @param z
     *        the Z coordinate of the newly constructed <code>Point3D</code>
     */
    public Double(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    /**
     * {@inheritDoc}
     */
    public double getX() {
      return x;
    }

    /**
     * {@inheritDoc}
     */
    public double getY() {
      return y;
    }

    /**
     * {@inheritDoc}
     */
    public double getZ() {
      return z;
    }

    /**
     * {@inheritDoc}
     */
    public void setLocation(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    /**
     * {@inheritDoc}
     */
    public void moveBy(double dx, double dy, double dz) {
      this.x = this.x + dx;
      this.y = this.y + dy;
      this.z = this.z + dz;
    }

    /**
     * Returns a <code>String</code> that represents the value of this <code>Point3D</code>.
     * 
     * @return a string representation of this <code>Point3D</code>.
     */
    public String toString() {
      return "Point3D.Double[" + x + ", " + y + ", " + z + "]";
    }

  }

  /**
   * This is an abstract class that cannot be instantiated directly. Type-specific implementation subclasses are
   * available for
   * instantiation and provide a number of formats for storing the information necessary to satisfy the various accessor
   * methods below.
   * 
   * @see java.awt.geom.Point3D.Float
   * @see java.awt.geom.Point3D.Double
   * @see java.awt.Point
   */
  protected Point3D() {}


  /**
   * Returns the X coordinate of this <code>Point3D</code> in <code>double</code> precision.
   * 
   * @return the X coordinate of this <code>Point3D</code>.
   */
  public abstract double getX();

  /**
   * Returns the Y coordinate of this <code>Point3D</code> in <code>double</code> precision.
   * 
   * @return the Y coordinate of this <code>Point3D</code>.
   */
  public abstract double getY();

  /**
   * Returns the Z coordinate of this <code>Point3D</code> in <code>double</code> precision.
   * 
   * @return the Z coordinate of this <code>Point3D</code>.
   */
  public abstract double getZ();


  /**
   * Sets the location of this <code>Point3D</code> to the specified <code>double</code> coordinates.
   * 
   * @param x
   *        the new X coordinate of this {@code Point3D}
   * @param y
   *        the new Y coordinate of this {@code Point3D}
   * @param z
   *        the new Z coordinate of this {@code Point3D}
   */
  public abstract void setLocation(double x, double y, double z);

  /**
   * Sets the location of this <code>Point3D</code> to the same coordinates as the specified <code>Point3D</code>
   * object.
   * 
   * @param p
   *        the specified <code>Point3D</code> to which to set this <code>Point3D</code>
   */
  public void setLocation(Point3D p) {
    setLocation(p.getX(), p.getY(), p.getZ());
  }

  /**
   * Moves the <code>Point3D</code> by the amount indicated by the specified <code>double</code> magnitudes.
   * <p>
   * NOTE: Units are NOT considered. The units of the {@code origin} and {@code (u, v, w)} components are assumed to be
   * the same.<br>
   * Therefore conversions (i.e. from DD to Meters) should happen BEFORE calling this method.
   * 
   * @param dx
   *        the amount to move this {@code Point3D} in the X direction
   * @param dy
   *        the amount to move this {@code Point3D} in the Y direction
   * @param dz
   *        the amount to move this {@code Point3D} in the Z direction
   */
  public abstract void moveBy(double dx, double dy, double dz);

  /**
   * Returns the square of the distance between two points.
   * 
   * @param x1
   *        the X coordinate of the first specified point
   * @param y1
   *        the Y coordinate of the first specified point
   * @param z1
   *        the Z coordinate of the first specified point
   * @param x2
   *        the X coordinate of the second specified point
   * @param y2
   *        the Y coordinate of the second specified point
   * @param z2
   *        the Z coordinate of the second specified point
   * @return the square of the distance between the two sets of specified coordinates.
   * @since 1.2
   */
  public static double distanceSq(double x1, double y1, double z1, double x2, double y2, double z2) {
    x1 -= x2;
    y1 -= y2;
    z2 -= z2;
    return (x1 * x1 + y1 * y1 + z1 * z1);
  }

  /**
   * Returns the distance between two points.
   * 
   * @param x1
   *        the X coordinate of the first specified point
   * @param y1
   *        the Y coordinate of the first specified point
   * @param z1
   *        the Z coordinate of the first specified point
   * @param x2
   *        the X coordinate of the second specified point
   * @param y2
   *        the Y coordinate of the second specified point
   * @param z2
   *        the Z coordinate of the second specified point
   * @return the distance between the two sets of specified coordinates.
   */
  public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
    x1 -= x2;
    y1 -= y2;
    z1 -= z2;
    return Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
  }

  /**
   * Returns the square of the distance from this <code>Point3D</code> to a specified point.
   * 
   * @param px
   *        the X coordinate of the specified point to be measured against this <code>Point3D</code>
   * @param py
   *        the Y coordinate of the specified point to be measured against this <code>Point3D</code>
   * @param pz
   *        the Z coordinate of the specified point to be measured against this <code>Point3D</code>
   * @return the square of the distance between this <code>Point3D</code> and the specified point.
   */
  public double distanceSq(double px, double py, double pz) {
    px -= getX();
    py -= getY();
    pz -= getZ();
    return (px * px + py * py + pz * pz);
  }

  /**
   * Returns the distance from this <code>Point3D</code> to a specified point.
   * 
   * @param px
   *        the X coordinate of the specified point to be measured against this <code>Point3D</code>
   * @param py
   *        the Y coordinate of the specified point to be measured against this <code>Point3D</code>
   * @param pz
   *        the Z coordinate of the specified point to be measured against this <code>Point3D</code>
   * @return the distance between this <code>Point3D</code> and a specified point.
   */
  public double distance(double px, double py, double pz) {
    px -= getX();
    py -= getY();
    pz -= getZ();
    return Math.sqrt(px * px + py * py + pz * pz);
  }


  /**
   * Returns the square of the distance from this <code>Point3D</code> to a specified <code>Point3D</code>.
   * 
   * @param pt
   *        the specified point to be measured against this <code>Point3D</code>
   * @return the square of the distance between this <code>Point3D</code> to a specified <code>Point3D</code>.
   */
  public double distanceSq(Point3D pt) {
    double px = pt.getX() - this.getX();
    double py = pt.getY() - this.getY();
    double pz = pt.getZ() - this.getZ();
    return (px * px + py * py + pz * pz);
  }

  /**
   * Returns the distance from this <code>Point3D</code> to a specified <code>Point3D</code>.
   * 
   * @param pt
   *        the specified point to be measured against this <code>Point3D</code>
   * @return the distance between this <code>Point3D</code> and the specified <code>Point3D</code>.
   */
  public double distance(Point3D pt) {
    double px = pt.getX() - this.getX();
    double py = pt.getY() - this.getY();
    double pz = pt.getZ() - this.getZ();
    return Math.sqrt(px * px + py * py + pz * pz);
  }

  /**
   * Creates a new object of the same class and with the same contents as this object.
   * 
   * @return a clone of this instance.
   * @exception OutOfMemoryError
   *            if there is not enough memory.
   * @see java.lang.Cloneable
   */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
  }

  // /**
  // * Returns the hashcode for this <code>Point3D</code>.
  // *
  // * @return a hash code for this <code>Point3D</code>.
  // */
  // public int hashCode() {
  // long bits = java.lang.Double.doubleToLongBits(getX());
  // bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
  // return (((int) bits) ^ ((int) (bits >> 32)));
  // }

  /**
   * Determines whether or not two points are equal. Two instances of <code>Point3D</code> are equal if the values of
   * their <code>x</code>
   * , <code>y</code> and <code>z</code> member fields, representing their position in the coordinate space, are the
   * same.
   * 
   * @param obj
   *        an object to be compared with this <code>Point3D</code>
   * @return <code>true</code> if the object to be compared is an instance of <code>Point3D</code> and has the same
   *         values;
   *         <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if (obj instanceof Point3D) {
      Point3D p3d = (Point3D) obj;
      return (getX() == p3d.getX()) && (getY() == p3d.getY() && (getZ() == p3d.getZ()));
    }
    return super.equals(obj);
  }
}
