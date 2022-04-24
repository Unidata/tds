/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * Math.java
 *
 * Created on May 21, 2009 @ 2:49:21 PM
 */

package ucar.nc2.dt.ugrid.utils;


import java.util.HashMap;
import java.util.Map;

import cern.colt.list.FloatArrayList;


/**
 * Utilities for common mathematical operations.
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class AsaMath {

  /**
   * Constant for converting from radians to degrees
   */
  public final static double RAD_2_DEG = (180 / Math.PI);
  /**
   * Constant for converting from degrees to radians;
   */
  public final static double DEG_2_RAD = (Math.PI / 180);
  /**
   * Radians representing 0 degrees
   */
  private final double ZERO_RAD = 0;
  /**
   * Radians representing 90 degrees
   */
  private final double QUARTER_RAD_CIRCLE = Math.PI * 0.5;
  /**
   * Radians representing 180 degrees
   */
  private final double HALF_RAD_CIRCLE = Math.PI;
  /**
   * Radians representing 270 degrees
   */
  private final double THREE_QUARTER_RAD_CIRCLE = (3 * Math.PI) * 0.5;

  /**
   * Calculates speed from U & V
   * 
   * @param u
   *        the u component
   * @param v
   *        the v component
   * @return the speed
   */
  public static double calcSpeed(double u, double v) {
    return Math.sqrt((u * u) + (v * v));
  }

  /**
   * Calculates direction from U & V
   * 
   * @param u
   *        the u component
   * @param v
   *        the v component
   * @return the direction
   */
  public static double calcDirection(double u, double v) {
    double dir = Math.atan2(u, v) * RAD_2_DEG;
    return (u < 0) ? dir + 360 : dir;
  }

  /**
   * Calculates both speed and direction from U & V
   * 
   * @param u
   *        the u component
   * @param v
   *        the v component
   * @return a <tt>double[]</tt> where where [0]==speed and [1]==direction
   */
  public static double[] calcSpeedDirection(double u, double v) {
    return new double[] {calcSpeed(u, v), calcDirection(u, v)};
  }

  /**
   * Calculates speeds and directions from an array of U & V pairs. The arrays MUST be of the same length.
   * 
   * @param us
   *        the array of u components
   * @param vs
   *        the array of v components
   * @return a <tt>double[][]</tt> where [0]==speed[] and [1]==direction[]
   */
  public static double[][] calcSpeedDirection(double[] us, double[] vs) {
    double[][] ret = null;
    if (us.length != vs.length) {
      return ret;
    }
    ret = new double[2][];

    double[] speeds = new double[us.length];
    double[] dirs = new double[us.length];
    double u, v, dir;
    for (int i = 0; i < us.length; i++) {
      u = us[i];
      v = vs[i];
      dir = Math.atan2(u, v) * RAD_2_DEG;
      dirs[i] = (u < 0) ? dir + 360 : dir;
      speeds[i] = Math.sqrt((u * u) + (v * v));
    }
    ret[0] = speeds;
    ret[1] = dirs;

    return ret;
  }

  /**
   * Calulates U and V from speed and direction
   * 
   * @param speed
   * @param direction
   * @return a <tt>double[]</tt> where [0]==U and [1]==V
   */
  public static double[] calcUVFromSpeedDir(double speed, double direction) {
    direction *= DEG_2_RAD;
    double u = speed * Math.sin(direction);
    double v = speed * Math.cos(direction);
    return new double[] {u, v};
  }

  /**
   * Calculates Us & Vs from arrays of speeds and directions. Arrays MUST be the same length.
   * 
   * @param speeds
   * @param directions
   * @return a <tt>double[][]</tt> where [0]==U[] and [1]==V[]
   */
  public static double[][] calcUVFromSpeedDir(double[] speeds, double[] directions) {
    double[][] ret = null;
    if (speeds.length != directions.length) {
      return ret;
    }
    ret = new double[2][];

    double[] us = new double[speeds.length];
    double[] vs = new double[speeds.length];
    double dir;
    for (int i = 0; i < speeds.length; i++) {
      dir = directions[i] * DEG_2_RAD;
      us[i] = speeds[i] * Math.sin(dir);
      vs[i] = speeds[i] * Math.cos(dir);
    }
    ret[0] = us;
    ret[1] = vs;

    return ret;
  }

  /**
   * Calculates a time from <code>time</code> in the specified units. Valid values for <code>
   * units</code> are: "hour", "hours", "hrs", "hr",
   * "minute", "minutes", "mins", or "min"
   * 
   * @param time
   *        a <code>double</code> time in milliseconds
   * @param units
   *        the <code>String</code> units to convert the given time to
   * 
   * @return a <code>long</code> value representing the number of minutes or hours in <code>time</code> dependant on the
   *         units specified
   */
  public static long getTimeInMilliseconds(double time, String units) {
    long timeIncrement = 0;
    if (units.contains("hour") || units.contains("hours") || units.contains("hrs") || units.contains("hr")) {
      timeIncrement = (long) (time * 60 * 60 * 1000);
    } else if (units.contains("minute") || units.contains("minutes") || units.contains("mins")
        || units.contains("min")) {
      timeIncrement = (long) (time * 60 * 1000);
    }
    return timeIncrement;
  }



  /**
   * Determines if the specified value can be parsed as a <code>double</code>
   * 
   * @param value
   *        an <code>Object</code> to be parsed
   * 
   * @return <code>true</code> if the specified object can be parsed as a <code>double</code>; <code>false</code>
   *         otherwise
   */
  public static boolean isNumeric(Object value) {
    boolean result = false;

    try {
      /* Call parseDouble for effects */
      Double.parseDouble((String) value);
      result = true;
    } catch (NumberFormatException ex) {
      result = false;
    }


    return result;
  }

  /**
   * Rounds <tt>value</tt> to the number of decimal points indicated by <tt>precision</tt>.
   * 
   * @param value
   * @param precision
   * @return
   */
  public static double roundDouble(double value, int precision) {
    double sign = (value >= 0) ? 1 : -1;
    double factor = Math.pow(10, precision);
    double n = value * factor;

    n = sign * Math.abs(Math.floor(n + 0.5));

    return n / factor;
  }

  /**
   * Rounds <tt>value</tt> to the number of decimal points indicated by <tt>precision</tt>.
   * 
   * @param value
   * @param precision
   * @return
   */
  public static float roundFloat(float value, int precision) {
    float sign = (value >= 0) ? 1 : -1;
    float factor = (float) Math.pow(10, precision);
    float n = value * factor;

    n = (float) (sign * Math.abs(Math.floor(n + 0.5)));

    return n / factor;
  }

  /**
   * Averages the values in <tt>vals</tt> ignoring any <tt>Double.NaN</tt> values.
   * 
   * @param vals
   *        the values to be averaged
   * @return <tt>Double.NaN</tt> if vals is null or all values are <tt>Double.NaN</tt>, otherwise the average of
   *         <tt>vals</tt>
   */
  public static double averageDouble(double[] vals) {
    double ret = Double.NaN;
    if (vals != null) {
      if (vals.length > 1) {
        double sum = 0;
        int count = 0;
        for (double d : vals) {
          if (!Double.isNaN(d)) {
            sum += d;
            count++;
          }
        }
        if (count > 0) {
          ret = sum / count;
        }
      } else {
        ret = vals[0];
      }
    }
    return ret;
  }

  /**
   * Averages the values in <tt>vals</tt> ignoring any <tt>ignoreVal</tt> values.
   * 
   * @param vals
   *        the values to be averaged
   * @param ignoreVal
   *        the value to ignore
   * @return <tt>Double.NaN</tt> if vals is null or all values are <tt>ignoreVal</tt>, otherwise the average of
   *         <tt>vals</tt>
   */
  public static double averageDouble(double[] vals, double ignoreVal) {
    double ret = Double.NaN;
    if (vals != null) {
      if (vals.length > 1) {
        double sum = 0;
        int count = 0;
        for (double d : vals) {
          if (d != ignoreVal) {
            sum += d;
            count++;
          }
        }
        if (count > 0) {
          ret = sum / count;
        }
      } else {
        ret = vals[0];
      }
    }
    return ret;
  }

  /**
   * Averages the values in <tt>vals</tt> ignoring any <tt>Float.NaN</tt> values.
   * 
   * @param vals
   *        the values to be averaged
   * @return <tt>Float.NaN</tt> if vals is null or all values are <tt>Float.NaN</tt>, otherwise the average of
   *         <tt>vals</tt>
   */
  public static float averageFloat(float[] vals) {
    float ret = Float.NaN;
    if (vals != null) {
      if (vals.length > 1) {
        float sum = 0;
        int count = 0;
        for (float d : vals) {
          if (!Float.isNaN(d)) {
            sum += d;
            count++;
          }
        }
        if (count > 0) {
          ret = sum / count;
        }
      } else {
        ret = vals[0];
      }
    }
    return ret;
  }

  /**
   * Averages the values in <tt>vals</tt> ignoring any <tt>ignoreVal</tt> values.
   * 
   * @param vals
   *        the values to be averaged
   * @param ignoreVal
   *        the value to ignore
   * @return <tt>Float.NaN</tt> if vals is null or all values are <tt>ignoreVal</tt>, otherwise the average of
   *         <tt>vals</tt>
   */
  public static float averageFloat(float[] vals, float ignoreVal) {
    float ret = Float.NaN;
    if (vals != null) {
      if (vals.length > 1) {
        float sum = 0;
        int count = 0;
        for (float d : vals) {
          if (d != ignoreVal) {
            sum += d;
            count++;
          }
        }
        if (count > 0) {
          ret = sum / count;
        }
      } else {
        ret = vals[0];
      }
    }
    return ret;
  }


  /**
   * Returns the statistical mode of the given dataset denoted by <code>numbers</code> while ignoring entries whose
   * values match
   * <code>fillVal</code>. If multiple values yeild the same frequency of occurance, each of them will be returned.
   * 
   * @param numbers
   *        a <code>float</code> array of numbers
   * @param fillVal
   *        the "filling" value within <code>numbers</code> to be ignored in determining a statistical mode.
   * 
   * @return an array of values which share the same frequency as the highest occuring value from the array
   *         <code>numbers</code>
   * 
   */
  public static float[] calculateModes(float[] numbers, Float fillVal) {
    Map<Float, Integer> table = new HashMap<Float, Integer>();
    FloatArrayList modes = new FloatArrayList();
    Integer max = 0;
    for (Float n : numbers) {
      if (Float.isNaN(fillVal)) {
        if (Float.isNaN(n)) {
          continue;
        }
      } else {
        if (n == fillVal) {
          continue;
        }
      }
      Integer frequency = table.remove(n);
      if (frequency == null) {
        frequency = 0;
      }
      table.put(n, ++frequency);
      if (frequency > max) {
        max = frequency;
        modes = new FloatArrayList();
      }
      if (frequency >= max) {
        modes.add(n);
      }
    }
    modes.trimToSize();
    return modes.elements();
    // return ArrayUtils.toPrimitive(modes.toArray(new Float[0]));
    // return Utils.floatObjArrayToFloatPrimArray(modes.toArray(new Float[]{}));
  }


  /**
   * Rounds <code>value</code> to the closest multiple of {@code nearest}. The {@code roundDirection} parameter
   * indicates which direction
   * to force rounding. A '0' rounds naturally (up for values > 0.5, down for values < 0.5). A number > 0 forces a round
   * up, while < 0
   * forces a round down.
   * <p>
   * Examples:
   * </p>
   * <p>
   * The code <br />
   * <i> double val = roundToNearest(102.536, 0.05, 1); </i><br />
   * results in <i>val</i> having a value of: <b>102.55</b>
   * <p>
   * The code <br />
   * <i> double val = roundToNearest(102.536, 50, -1); </i><br />
   * results in <i>val</i> having a value of: <b>100</b>
   * </p>
   * 
   * @param value
   *        the input value.
   * @param nearest
   *        the value of the nearest number to round to
   * @param roundDirection
   *        the direction to force rounding. <i>0</i> uses natural rounding, <i><0</i> always rounds up, <i>>0</i>
   *        always rounds down.
   * 
   * @return the rounded number
   */
  public static double roundToNearest(double value, double nearest, int roundDirection) {
    if (nearest == 0) {
      return value;
    }
    if (roundDirection < 0) {
      value = value - (nearest * 0.5);// round down
    } else if (roundDirection > 0) {
      value = value + (nearest * 0.5);// round up
    }
    double ret = Math.round(value / nearest) * nearest;
    return ret;
  }

  public static boolean almostEqual(double val1, double val2, double tolerance) {
    return (Math.abs(val1 - val2) <= tolerance);
  }

  public static boolean almostEqual(float val1, float val2, float tolerance) {
    return (Math.abs(val1 - val2) <= tolerance);
  }

  public static boolean sameSign(double val1, double val2) {
    if ((val1 >= 0 && val2 >= 0) || (val1 <= 0 && val2 <= 0)) {
      return true;
    }
    return false;
  }
}
