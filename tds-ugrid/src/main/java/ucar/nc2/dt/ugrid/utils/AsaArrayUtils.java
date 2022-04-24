/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * AsaArrayUtils.java
 *
 * Created on Jun 12, 2009 @ 8:14:41 AM
 */

package ucar.nc2.dt.ugrid.utils;



/**
 * Utilities for manipulating arrays.
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class AsaArrayUtils {

  public static int[] lonQuadrantCounts(double[] array) {
    int one = 0, two = 0, three = 0, four = 0;
    for (int i = 0; i < array.length; i++) {
      switch (lonQuadrant(array[i])) {
        case 1:
          one++;
          break;
        case 2:
          two++;
          break;
        case 3:
          three++;
          break;
        case 4:
          four++;
          break;

      }
    }
    return new int[] {one, two, three, four};
  }

  public static int lonQuadrant(double lon) {
    int ret = 1;
    if (lon >= 0 & lon < 90) {
      ret = 1;
    } else if (lon >= 90 & lon <= 180) {
      ret = 2;
    } else if (lon > -180 & lon <= -90) {
      ret = 3;
    } else if (lon > -90 & lon < 0) {
      ret = 4;
    }
    return ret;
  }

  /**
   * Get the minimum and maximum values of an array restricting the values to either positive or negative. If
   * <tt>positive</tt> is true,
   * only positive numbers will be addressed. If <tt>positive</tt> is false, only negative numbers will be addressed.
   * 
   * @param array
   *        the array to process
   * @param positive
   *        If true, negative numbers are ignored. If false, positive numbers are ignored.
   * @return a <tt>double[]</tt> where [0]==minimum and [1]==maximum
   */
  public static double[] minMaxSigned(double[] array, boolean positive) {
    double min, max;
    double val;
    if (positive) {
      min = Double.POSITIVE_INFINITY;
      max = 0;
      for (int i = 0; i < array.length; i++) {
        val = array[i];
        if (val >= 0) {
          min = (val < min) ? val : min;
          max = (val > max) ? val : max;
        }
      }
    } else {
      min = 0;
      max = Double.NEGATIVE_INFINITY;
      for (int i = 0; i < array.length; i++) {
        val = array[i];
        if (val <= 0) {
          min = (val < min) ? val : min;
          max = (val > max) ? val : max;
        }
      }
    }
    return new double[] {min, max};
  }

  public static double findNextLower(double[] array, double val) {
    double ret = array[0], nv, d, delta = Double.POSITIVE_INFINITY;
    val = Math.abs(val);
    for (int i = 0; i < array.length; i++) {
      nv = array[i];
      if (nv < val) {
        d = Math.abs(nv) - val;
        if (d > 0d && d < delta) {
          delta = d;
          ret = nv;
        }
      }
    }
    return ret;
  }

  public static double findNextHigher(double[] array, double val) {
    double ret = array[0], nv, d, delta = Double.POSITIVE_INFINITY;
    val = Math.abs(val);
    for (int i = 0; i < array.length; i++) {
      nv = array[i];
      if (nv > val) {
        d = Math.abs(nv) - val;
        if (d > 0d && d < delta) {
          delta = d;
          ret = nv;
        }
      }
    }
    return ret;
  }

  public static double[] leftRightLongitude(double[] array) {
    double[] ret = null;

    /* Normal min max */
    double[] mm = minMax(array);
    double left = mm[0];
    double right = mm[1];

    /* Shortcut if everything is either positive OR negative (no crossings) */
    if ((left >= 0 & right >= 0) || (left < 0 & right < 0)) {
      ret = mm;
    } else {
      /* Find out where the data is located */
      int[] counts = lonQuadrantCounts(array);
      double[] pmm = minMaxSigned(array, true);
      double[] nmm = minMaxSigned(array, false);
      int one = counts[0];
      int two = counts[1];
      int three = counts[2];
      int four = counts[3];
      if ((one > 0 & two > 0 & three > 0 & four > 0)) {
        /* Data in all quadrants - difficult to determine starting point */
        if ((two > one) & (three > four)) {
          /* Data starts in 1 and goes to 4 - CROSSES DATELINE */
          left = pmm[0];
          right = nmm[1];
        } else if ((two < one) & (three < four)) {
          /* Data starts in 3 and goes to 2 - NORMAL - do nothing */
        } else if ((four < one) & (four < two) & (four < three)) {
          /* Data starts in 4 and goes to 3 or 4 - CROSSES DATELINE */
          left = nmm[1];
          right = findNextLower(array, left);
        } else {
          /* When in doubt - whole globe... */
          left = -180;
          right = 180;
        }
      } else if (two > 0 & three > 0) {
        /* Data in at least 2 & 3 - CROSSES DATELINE */
        left = pmm[0];
        right = nmm[1];
      }
      ret = new double[] {left, right};
    }
    return ret;
  }

  /**
   * Determines the minimum and maximum values in the <tt>array</tt>. Calls {@link #minMax(double[], double)} with
   * <tt>Double.NaN</tt> as
   * the <tt>noDataValue</tt>.
   * 
   * @param array
   * @return a <tt>double[]</tt> where [0]==minimum and [1]==maximum
   * @see #minMax(double[], double)
   */
  public static double[] minMax(double[] array) {
    return minMax(array, Double.NaN);
  }

  /**
   * Determines the minimum and maximum values in the <tt>array</tt>, ignoring any instances of <tt>noDataValue</tt>.
   * 
   * @param array
   * @param noDataValue
   * @return a <tt>double[]</tt> where [0]==minimum and [1]==maximum
   */
  public static double[] minMax(double[] array, double noDataValue) {
    double[] ret = null;
    double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
    double val;
    for (int i = 0; i < array.length; i++) {
      val = array[i];
      if (val != noDataValue) {
        min = (val < min) ? val : min;
        max = (val > max) ? val : max;
      }
    }
    if (!Double.isInfinite(min) & !Double.isInfinite(max)) {
      ret = new double[] {min, max};
    }

    return ret;
  }

  /**
   * Determines the minimum and maximum values in the two dimensional array <tt>multi</tt>. Calls
   * {@link #minMax(double[], double)} with
   * <tt>Double.NaN</tt> as the <tt>noDataValue</tt>.
   * 
   * @param multi
   * @return a <tt>double[]</tt> where [0]==minimum and [1]==maximum
   * @see #minMax(double[][], double)
   */
  public static double[] minMax(double[][] multi) {
    return minMax(multi, Double.NaN);
  }

  /**
   * Determines the minimum and maximum values in the two dimensional array <tt>multi</tt>, ignoring any instances of
   * <tt>noDataValue</tt>
   * .
   * 
   * @param multi
   * @param noDataValue
   * @return a <tt>double[]</tt> where [0]==minimum and [1]==maximum
   */
  public static double[] minMax(double[][] multi, double noDataValue) {
    double[] ret = null;
    double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
    double val;
    for (int i = 0; i < multi.length; i++) {
      for (int j = 0; j < multi[i].length; j++) {
        val = multi[i][j];
        if (val != noDataValue) {
          min = (val < min) ? val : min;
          max = (val > max) ? val : max;
        }
      }
    }
    if (!Double.isInfinite(min) & !Double.isInfinite(max)) {
      ret = new double[] {min, max};
    }

    return ret;
  }

  /**
   * Determines the minimum and maximum values in the <tt>array</tt>. Calls {@link #minMax(float[], float)} with
   * <tt>Float.NaN</tt> as the
   * <tt>noDataValue</tt>.
   * 
   * @param array
   * @return a <tt>float[]</tt> where [0]==minimum and [1]==maximum
   * @see #minMax(float[], float)
   */
  public static float[] minMax(float[] array) {
    return minMax(array, Float.NaN);
  }

  /**
   * Determines the minimum and maximum values in the <tt>array</tt>, ignoring any instances of <tt>noDataValue</tt>.
   * 
   * @param array
   * @param noDataValue
   * @return a <tt>float[]</tt> where [0]==minimum and [1]==maximum
   */
  public static float[] minMax(float[] array, float noDataValue) {
    float[] ret = null;
    float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
    float val;
    for (int i = 0; i < array.length; i++) {
      val = array[i];
      if (val != noDataValue) {
        min = (val < min) ? val : min;
        max = (val > max) ? val : max;
      }
    }
    if (!Float.isInfinite(min) & !Float.isInfinite(max)) {
      ret = new float[] {min, max};
    }

    return ret;
  }

  /**
   * Determines the minimum and maximum values in the two dimensional array <tt>multi</tt>. Calls
   * {@link #minMax(float[], float)} with
   * <tt>Float.NaN</tt> as the <tt>noDataValue</tt>.
   * 
   * @param multi
   * @return a <tt>float[]</tt> where [0]==minimum and [1]==maximum
   * @see #minMax(float[][], float)
   */
  public static float[] minMax(float[][] multi) {
    return minMax(multi, Float.NaN);
  }

  /**
   * Determines the minimum and maximum values in the two dimensional array <tt>multi</tt>, ignoring any instances of
   * <tt>noDataValue</tt>
   * .
   * 
   * @param multi
   * @param noDataValue
   * @return a <tt>float[]</tt> where [0]==minimum and [1]==maximum
   */
  public static float[] minMax(float[][] multi, float noDataValue) {
    float[] ret = null;
    float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
    float val;
    for (int i = 0; i < multi.length; i++) {
      for (int j = 0; j < multi[i].length; j++) {
        val = multi[i][j];
        if (val != noDataValue) {
          min = (val < min) ? val : min;
          max = (val > max) ? val : max;
        }
      }
    }
    if (!Float.isInfinite(min) & !Float.isInfinite(max)) {
      ret = new float[] {min, max};
    }

    return ret;
  }

  /**
   * Determines if the members of <tt>array</tt> are evenly spaced.
   * 
   * @param array
   * @return <tt>true</tt> if the members have uniform intervals, <tt>false</tt> otherwise
   */
  public static boolean isUniformInterval(int[] array) {
    boolean ret = true;
    if (array.length > 1) {
      int d0 = array[1] - array[0];
      int d1;
      for (int i = 2; i < array.length; i++) {
        d1 = array[i] - array[i - 1];
        if (d0 != d1) {
          ret = false;
          break;
        }
      }
    }

    return ret;
  }

  /**
   * Determines if the members of <tt>array</tt> are evenly spaced.
   * 
   * @param array
   * @return <tt>true</tt> if the members have uniform intervals, <tt>false</tt> otherwise
   */
  public static boolean isUniformInterval(long[] array) {
    boolean ret = true;
    if (array.length > 1) {
      long d0 = array[1] - array[0];
      long d1;
      for (int i = 2; i < array.length; i++) {
        d1 = array[i] - array[i - 1];
        if (d0 != d1) {
          ret = false;
          break;
        }
      }
    }

    return ret;
  }

  /**
   * Determines if the members of <tt>array</tt> are evenly spaced.
   * 
   * @param array
   * @return <tt>true</tt> if the members have uniform intervals, <tt>false</tt> otherwise
   */
  public static boolean isUniformInterval(float[] array) {
    boolean ret = true;
    if (array.length > 1) {
      float d0 = array[1] - array[0];
      float d1;
      for (int i = 2; i < array.length; i++) {
        d1 = array[i] - array[i - 1];
        if (d0 != d1) {
          ret = false;
          break;
        }
      }
    }

    return ret;
  }

  /**
   * Determines if the members of <tt>array</tt> are evenly spaced.
   * 
   * @param array
   * @return <tt>true</tt> if the members have uniform intervals, <tt>false</tt> otherwise
   */
  public static boolean isUniformInterval(double[] array) {
    boolean ret = true;
    if (array.length > 1) {
      double d0 = array[1] - array[0];
      double d1;
      for (int i = 2; i < array.length; i++) {
        d1 = array[i] - array[i - 1];
        if (d0 != d1) {
          ret = false;
          break;
        }
      }
    }

    return ret;
  }



  /**
   * Recreates the array specified by <code>original</code> as a two dimensional set with dimension sizes defined by
   * {@code dim1} and
   * {@code dim2}. The values of this array are then swapped along the newly defined grid and returned as a single
   * dimension array.
   * 
   * <p>
   * <b>For example:</b> <br />
   * The <code>float</code> array defined by the set {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11} could be swapped by -
   * <ul>
   * <li>dimensions 6, 2 yielding {0, 6, 1, 7, 2, 8, 3, 9, 4, 10, 5, 11}</li>
   * <li>dimensions 4, 3 yielding {0, 4, 8, 1, 5, 9, 2, 6, 10, 3, 7, 11}</li>
   * <li>dimensions 3, 4 yielding {0, 3, 6, 9, 1, 4, 7, 10, 2, 5, 8, 11}</li>
   * </ul>
   * </p>
   * 
   * 
   * @param original
   *        the <code>float[]</code> array to be swapped
   * @param dim1
   *        the horizontal (x) dimension of the grid on which <code>original</code> will be swapped
   * @param dim2
   *        the vertical (y) dimension of the grid on which <code>original</code> will be swapped
   * 
   *        <br />
   *        TODO: there is no prevention for an ArrayOutOfBoundsException when accessing orig[] within the first nested
   *        For Loop
   */
  public static float[] swapDimensions(float[] original, int dim1, int dim2) {
    float[] orig = original.clone();
    float[] swap = new float[orig.length];

    float[][] temp = new float[dim2][dim1];
    int oi = 0;
    for (int d2 = 0; d2 < dim2; d2++) {
      for (int d1 = 0; d1 < dim1; d1++) {
        temp[d2][d1] = orig[oi++];
      }
    }

    int si = 0;
    for (int d1 = 0; d1 < dim1; d1++) {
      for (int d2 = 0; d2 < dim2; d2++) {
        swap[si++] = temp[d2][d1];
      }
    }

    // logger.debug("orig,swap");
    // for(int it = 0; it < swap.length; it++) {
    // logger.debug(orig[it] + "," + swap[it]);
    // }
    return swap;
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
  public static float[] calculateModes(float[] numbers, float fillVal) {

    cern.colt.map.OpenDoubleIntHashMap table = new cern.colt.map.OpenDoubleIntHashMap();
    cern.colt.list.FloatArrayList modes = new cern.colt.list.FloatArrayList();

    int max = 0;
    for (float n : numbers) {
      if (Float.isNaN(fillVal)) {
        if (Float.isNaN(n)) {
          continue;
        }
      } else {
        if (n == fillVal) {
          continue;
        }
      }
      int frequency = 0;
      frequency = table.get(n);
      table.put(n, ++frequency);
      if (frequency > max) {
        max = frequency;
        modes.clear();
      }
      if (frequency >= max) {
        modes.add(n);
      }
    }
    modes.trimToSize();
    return modes.elements();
  }



  /**
   * Searches the specified array for any value less than <code>checkVal</code> within the tolerance range defined by
   * <code>tolerance</code>. Because this returns the first value within range, when closestPrevious() is invoked for
   * the array defined by
   * {5l, 6l, 7l, 8l, 9l} where <code>checkVal = 8</code> and <code>tolerance = 2</code>, the index 1 (value 6) will be
   * returned.
   * 
   * @param myArray
   *        the <code>Long</code> array within which to search
   * @param checkVal
   *        the <code>long</code> value to search for
   * @param tolerance
   *        a <code>long</code> range within which to search for the check value
   * 
   * @return the index of the value from <code>myArray</code> which is less then <code>checkval</code> but no less than
   *         <code>checkVal - tolerance</code>. If no value is within this tolerance range and less then
   *         <code>checkVal</code> or all
   *         values of <code>myArray</code> are greater then <code>checkVal</code> -1 is returned.
   */
  public static int closestPrevious(Long[] myArray, long checkVal, long tolerance) {
    int index = 0;
    while (index < myArray.length) {
      if (checkVal >= myArray[index] & checkVal < (myArray[index] + tolerance)) {
        return index;
      }
      index++;
    }
    return -1;
  }



  /**
   * Converts the given primative <code>double</code> array to a primitive <code>float</code> array
   * 
   * @param in
   *        a primative <code>double</code> array
   * 
   * @return a <code>float</code> array equivalent to the specified <code>double</code> array
   */
  public static float[] doubleArrayToFloatArray(double[] in) {
    float[] out = new float[in.length];
    for (int i = 0; i < in.length; i++) {
      if (Double.isNaN(in[i])) {
        out[i] = Float.NaN;
      } else {
        out[i] = (float) in[i];
      }
    }
    return out;
  }

  public static float[][] doubleArrayToFloatArray(double[][] in) {
    float[][] out = new float[in.length][];
    for (int i = 0; i < in.length; i++) {
      out[i] = doubleArrayToFloatArray(in[i]);
    }
    return out;
  }

  /**
   * Converts the given primative <code>float</code> array to a primitive <code>double</code> array
   * 
   * @param in
   *        a primative <code>float</code> array
   * 
   * @return a <code>double</code> array equivalent to the specified <code>float</code> array
   */
  public static double[] floatArrayToDoubleArray(float[] in) {
    double[] out = new double[in.length];
    for (int i = 0; i < in.length; i++) {
      if (Float.isNaN(in[i])) {
        out[i] = Double.NaN;
      } else {
        out[i] = (double) in[i];
      }
    }
    return out;
  }

  public static double[][] floatArrayToDoubleArray(float[][] in) {
    double[][] out = new double[in.length][];
    for (int i = 0; i < in.length; i++) {
      out[i] = floatArrayToDoubleArray(in[i]);
    }
    return out;
  }

  /**
   * Retrieves a subset of the given array ignoring values with an index lesser than <code>start</code> and pulling
   * values at an interval
   * denoted by <code>skip</code>
   * 
   * @param in
   *        a primative float array
   * @param start
   *        the starting index to begin parsing the given array
   * @param skip
   *        the &quot;skip&quot; interval for parsing values from the array
   * 
   * @return an array subset from indices <code>start</code> to <code>in.length</code> at an interval denoted by
   *         <code>skip</code>
   */
  public static float[] getSubsetArrayFloat(float[] in, int start, int skip) {
    cern.colt.list.FloatArrayList out = new cern.colt.list.FloatArrayList();
    int i = start;
    while (i < in.length) {
      out.add(in[i]);
      i += skip;
    }
    return out.elements();
  }


}
