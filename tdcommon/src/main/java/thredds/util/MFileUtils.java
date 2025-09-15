/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.util;

import thredds.inventory.MFile;
import ucar.nc2.iosp.zarr.ZarrKeys;

public class MFileUtils {

  /**
   * Determine if a Zarr MFile can be opened by netCDF-Java
   *
   * @param mfile possible Zarr dataset
   * @return true if can be opened by netCDF-Java
   */
  public static boolean isMfileZarr(MFile mfile) {
    boolean isZarr = false;
    boolean isDir = mfile.isDirectory();
    if (isDir) {
      // if directory contains a .zgroup, it can be served as a netCDF-Java
      // zarr dataset (in addition to being explored as a directory)
      // I think having a .array should be enough as well, but the
      // netCDF-Java IOSP really wants a .zgroup to create the dataset.
      // TODO: revisit if ISOP extended to work with just .zarray
      MFile zgroup = mfile.getChild(ZarrKeys.ZGROUP);
      isZarr = zgroup != null && zgroup.exists();
    }
    return isZarr;
  }
}
