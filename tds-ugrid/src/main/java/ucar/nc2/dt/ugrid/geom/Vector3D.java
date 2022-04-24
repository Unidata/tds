/*
 * Vector3D.java
 *
 * Created on September 25, 2007, 1:34 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

package ucar.nc2.dt.ugrid.geom;


/**
 * Holds the u, v and w components of a vector as well as it's origin (<code>Point3D</code>).
 * <p>
 * The class also provides methods to perform mathematical operations on this and other vectors (addition, subtraction,
 * multiplication,
 * division, cross multiplication, and dot multiplication)
 * 
 * @author CBM
 */
public class Vector3D {

  private Point3D origin;
  private double u;
  private double v;
  private double w;

  /**
   * Creates a new instance of Vector3D with an origin of (0, 0, 0) and no magnitude (u=0, v=0, w=0)
   */
  public Vector3D() {
    origin = new Point3D.Double();
  }

  /**
   * Creates a new instance of Vector3D with an origin of (0, 0, 0) and the specified u, v & w magnitudes.
   * 
   * @param u
   *        The <CODE>double</CODE> u-component of the vector.
   * @param v
   *        The <CODE>double</CODE> v-component of the vector.
   * @param w
   *        The <CODE>double</CODE> w-component of the vector.
   */
  public Vector3D(double u, double v, double w) {
    this.u = u;
    this.v = v;
    this.w = w;
  }

  /**
   * Creates a new instance of Vector3D with the specified origin and u, v & w magnitudes.
   * 
   * @param origin
   *        the <code>Point3D</code> origin of the vector.
   * @param u
   *        The <CODE>double</CODE> u-component of the vector.
   * @param v
   *        The <CODE>double</CODE> v-component of the vector.
   * @param w
   *        The <CODE>double</CODE> w-component of the vector.
   */
  public Vector3D(Point3D origin, double u, double v, double w) {
    this.origin = origin;
    this.u = u;
    this.v = v;
    this.w = w;
  }

  /**
   * Gets the origin of this <code>Vector3D</code>
   * 
   * @return the origin
   */
  public Point3D getOrigin() {
    return origin;
  }

  /**
   * Sets the origin of this <code>Vector3D</code>
   * 
   * @param origin
   *        the new origin
   */
  public void setOrigin(Point3D origin) {
    this.origin = origin;
  }

  /**
   * Returns the u component of the vector.
   * 
   * @return <CODE>double</CODE> The u component.
   */
  public double getU() {
    return u;
  }

  /**
   * Sets the u component of the vector.
   * 
   * @param u
   *        <CODE>double</CODE> The u component.
   */
  public void setU(double u) {
    this.u = u;
  }

  /**
   * Returns the v component of the vector.
   * 
   * @return <CODE>double</CODE> The v component.
   */
  public double getV() {
    return v;
  }

  /**
   * Sets the v component of the vector.
   * 
   * @param v
   *        <CODE>double</CODE> The v component.
   */
  public void setV(double v) {
    this.v = v;
  }

  /**
   * Returns the w component of the vector.
   * 
   * @return <CODE>double</CODE> The w component.
   */
  public double getW() {
    return w;
  }

  /**
   * Sets the w component of the vector.
   * 
   * @param w
   *        <CODE>double</CODE> The w component.
   */
  public void setW(double w) {
    this.w = w;
  }

  /**
   * Builds a descriptive string presenting the u, v, and w components of this vector
   * 
   * @return a descriptive <code>String</code>
   */
  public String toString2() {
    return "Vector3D[u,v,w]: [" + getU() + "," + getV() + "," + getW() + "]";
  }

  /**
   * Gets the magnitude of this <code>Vector3D</code>.
   * <p>
   * NOTE: Units are NOT considered, the units of magnitude are the same as those of the vector components.
   * 
   * @return the vector magnitude
   */
  public double getMagnitude() {
    return Math.sqrt(u * u + v * v + w * w);
  }

  /**
   * Gets the <code>Point3D</code> indicated by applying the vector {@code (u, v, w)} components to the
   * <code>origin</code>
   * <p>
   * NOTE: Units are NOT considered. The units of the {@code origin} and {@code (u, v, w)} components are assumed to be
   * the same.<br>
   * Therefore conversions (i.e. from DD to Meters) should happen BEFORE calling this method.
   * 
   * @return the end point.
   */
  public Point3D getEndPoint() {
    Point3D ret = (Point3D) origin.clone();
    ret.moveBy(u, v, w);
    return ret;
  }

  /**
   * Adds the numerical value of each component of the first vector with the values in the second vector.
   * 
   * @param v1
   *        the first <code>Vector3D</code>
   * @param v2
   *        the second <code>Vector3D</code>
   * 
   * @return a <code>Vector3D</code> representing the addition of the two vectors
   */
  public static Vector3D add(Vector3D v1, Vector3D v2) {
    return (new Vector3D(v1.getU() + v2.getU(), v1.getV() + v2.getV(), v1.getW() + v2.getW()));
  }

  /**
   * Subtracts the numerical value of each component of the second vector from the values in the first vector.
   * 
   * @param v1
   *        the first <code>Vector3D</code>
   * @param v2
   *        the second <code>Vector3D</code>
   * 
   * @return a <code>Vector3D</code> representing the subtraction of the two vectors
   */
  public static Vector3D sub(Vector3D v1, Vector3D v2) {
    return (new Vector3D(v1.getU() - v2.getU(), v1.getV() - v2.getV(), v1.getW() - v2.getW()));
  }

  /**
   * Multiplies the numerical value of each component of the first vector with the values in the second vector.
   * 
   * @param v1
   *        the first <code>Vector3D</code>
   * @param v2
   *        the second <code>Vector3D</code>
   * 
   * @return a <code>Vector3D</code> representing the product of the two vectors
   */
  public static Vector3D mult(Vector3D v, double d) {
    return (new Vector3D(v.getU() * d, v.getV() * d, v.getW() * d));
  }

  /**
   * Divides the numerical value of each component of the first vector by the values in the second vector.
   * 
   * @param v1
   *        the first <code>Vector3D</code>
   * @param v2
   *        the second <code>Vector3D</code>
   * 
   * @return a <code>Vector3D</code> representing the quotient of the two vectors
   */
  public static Vector3D div(Vector3D v, double d) {
    return (new Vector3D(v.getU() / d, v.getV() / d, v.getW() / d));
  }

  /**
   * Determines the cross product of the two given vectors
   * 
   * @param v1
   *        the first <code>Vector3D</code>
   * @param v2
   *        the second <code>Vector3D</code>
   * 
   * @return a <code>Vector3D</code> which is perpendicular to the plane containing the given vector's. i.e. Cross
   *         product.
   */
  public static Vector3D cross(Vector3D v1, Vector3D v2) {
    // TODO: what orientation does this represent, and does it matter??
    // should this clarification be in the above javadoc??
    return (new Vector3D(v1.getV() * v2.getW() - v1.getW() * v2.getV(), v1.getW() * v2.getU() - v1.getU() * v2.getW(),
        v1.getU() * v2.getV() - v1.getV() * v2.getU()));
  }

  /**
   * Determines the cross product of this <code>Vector3D</code> with the one given
   * 
   * @param other
   *        another <code>Vector3D</code>
   * 
   * @return a <code>Vector3D</code> representing the cross product of this vector and the one given
   */
  public Vector3D cross(Vector3D other) {
    return cross(this, other);
  }

  /**
   * Determines the dot product of the two given vectors
   * 
   * @param v1
   *        the first <code>Vector3D</code>
   * @param v2
   *        the second <code>Vector3D</code>
   * 
   * @return a <code>Vector3D</code> which represents the dot product of the given vectors.
   */
  public static double dot(Vector3D v1, Vector3D v2) {
    return (v1.getU() * v2.getU() + v1.getV() * v2.getV() + v1.getW() * v2.getW());
  }

  /**
   * Determines the dot product of this <code>Vector3D</code> with the one given
   * 
   * @param other
   *        another <code>Vector3D</code>
   * 
   * @return a <code>Vector3D</code> representing the dot product of this vector and the one given
   */
  public double dot(Vector3D other) {
    return dot(this, other);
  }

  // </editor-fold>
}
