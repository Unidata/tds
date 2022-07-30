/*
 * Copyright (c) 2011-2014 Applied Science Associates
 * See LICENSE for license information.
 */

/*
 * A collection of String utilities.
 */
package ucar.nc2.dt.ugrid.utils;

/**
 *
 * @author kgrunenberg
 */
public class AsaStringUtils {

  public static String repeat(String s, int cnt) {
    StringBuilder sb = new StringBuilder(s.length() * cnt);
    for (int i = 0; i < cnt; i++) {
      sb.append(s);
    }
    return sb.toString();
  }
}
