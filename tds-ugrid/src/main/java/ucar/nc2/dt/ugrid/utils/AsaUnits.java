/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * AsaUnits.java
 *
 * Created on Jul 2, 2009 @ 11:24:06 AM
 */

package ucar.nc2.dt.ugrid.utils;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class AsaUnits {

  public AsaUnits() {}


  /**
   * Enumeration used in converting between different speed units. Meters are used as a conversion base.
   * 
   * @author tlarocque
   */
  public enum SpeedType {
    /*
     * TODO: check these values for accuracy
     * These values where obtained from google.com converter
     */
    UNKNOWN(1, 1, "unknown"),
    KNOTS(0.514444444, 1.94384449, "kt"),
    METERS_SEC(1, 1, "m/s"),
    MILLIMETERS_SEC(0.001, 1000, "mm/s"),
    CENTIMETERS_SEC(0.01, 100, "cm/s"),
    MILES_HOUR(0.44704, 2.23693629, "mph"),
    KILOMETERS_HOUR(0.277777778, 3.6, "km/h");

    private double toMeters;
    private double fromMeters;
    private String dispName;

    SpeedType(double toMetersMultiplier, double fromMetersMultiplier, String displayName) {
      toMeters = toMetersMultiplier;
      fromMeters = fromMetersMultiplier;
      dispName = displayName;
    }

    /**
     * @return the number of meters equal to <code>n</code> when <code>n</code> is in the units specified by this
     *         enumeration
     */
    public double convertToMeters(double n) {
      return n * toMeters;
    }

    /**
     * @return a number in the units specified by this enumeration equal to <code>n</code> meters
     */
    public double convertFromMeters(double n) {
      return n * fromMeters;
    }

    public String getDisplayName() {
      return dispName;
    }

    public String toString() {
      return dispName;
    }

    public static SpeedType getSpeedType(String string) {
      for (SpeedType st : SpeedType.values()) {
        if (st.getDisplayName().equalsIgnoreCase(string)) {
          return st;
        }
      }
      return SpeedType.UNKNOWN;
    }
  }

  /**
   * Enumeration used in converting between different distance units. Meters are used as a conversion base.
   * 
   * @author tlarocque
   */
  public enum DistanceType {
    UNKNOWN(1, 1, "unknown"),
    METERS(1, 1, "m"),
    KILOMETERS(1000, 0.001, "km"),
    MILES(1609.344, 0.000621371192, "mi"),
    FEET(0.3048, 3.2808399, "ft"),
    NAUTICAL_MILES(1852, 0.000539, "nm");

    private double toMeters;
    private double fromMeters;
    private String dispName;

    DistanceType(double toMetersMultiplier, double fromMetersMultiplier, String displayName) {
      toMeters = toMetersMultiplier;
      fromMeters = fromMetersMultiplier;
      dispName = displayName;
    }

    /**
     * @return the number of meters equal to <code>n</code> when <code>n</code> is in the units specified by this
     *         enumeration
     */
    public double convertToMeters(double n) {
      return n * toMeters;
    }

    /**
     * @return a number in the units specified by this enumeration equal to <code>n</code> meters
     */
    public double convertFromMeters(double n) {
      return n * fromMeters;
    }

    public String getDisplayName() {
      return dispName;
    }

    public String toString() {
      return dispName;
    }

    public static DistanceType getDistanceType(String string) {
      for (DistanceType dt : DistanceType.values()) {
        if (dt.getDisplayName().equalsIgnoreCase(string)) {
          return dt;
        }
      }
      return DistanceType.UNKNOWN;
    }
  }

  /**
   * Enumeration used in converting between different mass units. grams are used as a conversion base.
   * 
   * @author kgrunenberg
   */
  public enum MassType {
    GRAM(1, 1, "g"),
    KILOGRAM(1000, 0.001, "kg"),
    USTON(907184.74, 0.000001102311310924, "ton (US Short)"),
    LBS(453.59237, 0.002204622621849, "lbs");

    private double toMeters;
    private double fromMeters;
    private String dispName;

    MassType(double toGramsMultiplier, double fromGramsMultiplier, String displayName) {
      toMeters = toGramsMultiplier;
      fromMeters = fromGramsMultiplier;
      dispName = displayName;
    }

    /**
     * @return the number of grams equal to <code>n</code> when <code>n</code> is in the units specified by this
     *         enumeration
     */
    public double convertToGrams(double n) {
      return n * toMeters;
    }

    /**
     * @return a number in the units specified by this enumeration equal to <code>n</code> grams
     */
    public double convertFromGrams(double n) {
      return n * fromMeters;
    }

    public String getDisplayName() {
      return dispName;
    }

    public String toString() {
      return dispName;
    }

    public static MassType getMassType(String string) {
      for (MassType mt : MassType.values()) {
        if (mt.getDisplayName().equalsIgnoreCase(string)) {
          return mt;
        }
      }
      return MassType.GRAM;
    }
  }

  /**
   * Converts the given <code>speed</code> from the units <code>fromUnits</code> to the units specified by
   * <code>toUnit</code>
   * 
   * @param fromUnits
   *        an enumeration specifying the unit type of <code>speed</code>
   * @param toUnits
   *        an enumeration specifying the units to convert <code>speed</code> to
   * @param speed
   *        a <code>double</code> value to convert to the given units
   * 
   * @return the number given by <code>speed</code> converted from one unit to another
   * 
   */
  public static double convertSpeed(SpeedType fromUnits, SpeedType toUnits, double speed) {
    double meters = fromUnits.convertToMeters(speed);
    return toUnits.convertFromMeters(meters);
  }

  /**
   * Converts the given <code>speed</code> array from the units <code>fromUnits</code> to the units specified by
   * <code>toUnit</code>
   * 
   * @param fromUnits
   *        an enumeration specifying the unit type of <code>speed</code>
   * @param toUnits
   *        an enumeration specifying the units to convert <code>speed</code> to
   * @param speed
   *        a <code>double[]</code> of values to convert to the given units
   * 
   * @return the numbers given by <code>speed</code> converted from one unit to another
   * 
   */
  public static double[] convertSpeed(SpeedType fromUnits, SpeedType toUnits, double[] speed) {
    double[] ret = new double[speed.length];
    for (int i = 0; i < ret.length; i++) {
      // First Convert to our base unit (meters/sec)
      ret[i] = fromUnits.convertToMeters(speed[i]);
      // Now we can convert to our desired units (ToUnits)
      ret[i] = toUnits.convertFromMeters(ret[i]);
    }
    return ret;
  }

  /**
   * Converts the given <code>distance</code> from the units <code>fromUnits</code> to the units specified by
   * <code>toUnit</code>
   * 
   * @param fromUnits
   *        an enumeration specifying the unit type of <code>distance</code>
   * @param toUnits
   *        an enumeration specifying the units to convert <code>distance</code> to
   * @param distance
   *        a <code>double</code> value to convert to the given units
   * 
   * @return the number given by <code>distance</code> converted from one unit to another
   * 
   */
  public static double convertDistance(DistanceType fromUnits, DistanceType toUnits, double distance) {
    double meters = fromUnits.convertToMeters(distance);
    return toUnits.convertFromMeters(meters);
  }

  /**
   * Converts the given <code>distance</code> array from the units <code>fromUnits</code> to the units specified by
   * <code>toUnit</code>
   * 
   * @param fromUnits
   *        an enumeration specifying the unit type of <code>distance</code>
   * @param toUnits
   *        an enumeration specifying the units to convert <code>distance</code> to
   * @param distance
   *        a <code>double[]</code> value to convert to the given units
   * 
   * @return the numbers given by <code>distance</code> converted from one unit to another
   * 
   */
  public static double[] convertDistance(DistanceType fromUnits, DistanceType toUnits, double[] distance) {
    double[] ret = new double[distance.length];
    for (int i = 0; i < ret.length; i++) {
      // First Convert to our base unit (meters)
      ret[i] = fromUnits.convertToMeters(distance[i]);
      // Now we can convert to our desired units (ToUnits)
      ret[i] = toUnits.convertFromMeters(ret[i]);
    }
    return ret;
  }
}
