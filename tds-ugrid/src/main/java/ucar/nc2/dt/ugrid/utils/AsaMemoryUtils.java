/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * AsaMemoryUtils.java
 *
 * Created on Oct 7, 2009 @ 4:44:42 PM
 */

package ucar.nc2.dt.ugrid.utils;


/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class AsaMemoryUtils {



  public static int BYTE = 0;
  public static int MEGABYTE = 1;
  public static int GIGABYTE = 2;


  /**
   * Returns the amount of free memory in the Java Virtual Machine in units denoted by <code>sizeType</code>
   * 
   * @param sizeType
   *        the type of units to report the JVM's free memory. Valid values are:
   *        <ul>
   *        <li><code><b>com.asascience.utilities.Utils.Memory.BYTE</b></code></li>
   *        <li><code><b>com.asascience.utilities.Utils.Memory.MEGABYTE</b></code></li>
   *        <li><code><b>com.asascience.utilities.Utils.Memory.GIGABYTE</b></code></li>
   * 
   * @return the total amount of free memory in the Java Virtual Machine
   * 
   *         <br />
   *         TODO: if sizeType = 0, Double.NaN is returned
   */
  public static double freeMemoryAs(int sizeType) {
    long freeMem = java.lang.Runtime.getRuntime().freeMemory();
    switch (sizeType) {
      case 0:// bytes
        break;
      case 1:// mb
        return Convert.bytesToMegabytes(freeMem);
      case 2:// gb
        return Convert.bytesToGigabytes(freeMem);
    }

    return Double.NaN;
  }


  /**
   * Returns the total amount of memory in the Java Virtual Machine in units denoted by <code>sizeType</code>
   * 
   * @param sizeType
   *        the type of units to report the JVM's free memory. Valid values are:
   *        <ul>
   *        <li><code><b>com.asascience.utilities.Utils.Memory.BYTE</b></code></li>
   *        <li><code><b>com.asascience.utilities.Utils.Memory.MEGABYTE</b></code></li>
   *        <li><code><b>com.asascience.utilities.Utils.Memory.GIGABYTE</b></code></li>
   * 
   * @return the total amount of memory in the Java Virtual Machine
   * 
   *         <br />
   *         TODO: if sizeType = 0, Double.NaN is returned
   */
  public static double totalMemoryAs(int sizeType) {
    long freeMem = java.lang.Runtime.getRuntime().totalMemory();
    switch (sizeType) {
      case 0:// bytes
        break;
      case 1:// mb
        return Convert.bytesToMegabytes(freeMem);
      case 2:// gb
        return Convert.bytesToGigabytes(freeMem);
    }

    return Double.NaN;
  }


  /**
   * a static class used in converting between bytes, megabytes, and gigabytes
   */
  public static class Convert {

    /**
     * Converts the specified <code>long</code> value from bytes to megabytes
     * 
     * @param bytes
     *        a <code>long</code> value
     * 
     * @return the number of megabytes in <code>bytes</code>
     */
    public static double bytesToMegabytes(long bytes) {
      return (bytes * 9.53674316e-7);
    }

    /**
     * Converts the specified <code>double</code> value from megabytes to bytes
     * 
     * @param megabytes
     *        a <code>double</code> value
     * 
     * @return the number of bytes in <code>megabytes</code>
     */
    public static double megabytesToBytes(double megabytes) {
      return (megabytes * 1048576);
    }

    /**
     * Converts the specified <code>long</code> value from bytes to gigabytes
     * 
     * @param bytes
     *        a <code>long</code> value
     * 
     * @return the number of gigabytes in <code>bytes</code>
     */
    public static double bytesToGigabytes(long bytes) {
      return (bytes * 9.31322575e-10);
    }

    /**
     * Converts the specified <code>double</code> value from gigabytes to bytes
     * 
     * @param gigabytes
     *        a <code>double</code> value
     * 
     * @return the number of bytes in <code>gigabytes</code>
     */
    public static double gigabytesToBytes(double gigabytes) {
      return (gigabytes * 1073741824);
    }

    /**
     * Converts the specified <code>double</code> value from megabytes to gigabytes
     * 
     * @param megabytes
     *        a <code>double</code> value
     * 
     * @return the number of gigabytes in <code>megabytes</code>
     */
    public static double megabytesToGigabytes(double megabytes) {
      return (megabytes * 0.0009765625);
    }

    /**
     * Converts the specified <code>double</code> value from gigabytes to megabytes
     * 
     * @param gigabytes
     *        a <code>double</code> value
     * 
     * @return the number of megabytes in <code>gigabytes</code>
     */
    public static double gigabytesToMegabytes(double gigabytes) {
      return (gigabytes * 1024);
    }
  }
}
