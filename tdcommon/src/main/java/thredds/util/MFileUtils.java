/*
 * Copyright (c) 2025-2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.util;

import static ucar.nc2.util.IO.default_file_buffersize;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import thredds.inventory.MFile;
import ucar.nc2.iosp.zarr.ZarrKeys;
import ucar.nc2.util.IO;

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

  /**
   * copy MFile to output stream
   *
   * @param fileIn copy this {@link MFile}
   * @param out copy here
   * @throws java.io.IOException on io error
   */
  public static void copyMFile(MFile fileIn, OutputStream out) throws IOException {
    copyMFileB(fileIn, out, default_file_buffersize);
  }

  /**
   * copy MFile to output stream, specify internal buffer size
   *
   * @param fileIn copy this {@link MFile}
   * @param out copy to this stream
   * @param bufferSize internal buffer size.
   * @throws java.io.IOException on io error
   */
  public static void copyMFileB(MFile fileIn, OutputStream out, int bufferSize) throws IOException {
    try (InputStream fin = fileIn.getInputStream()) {
      InputStream in = new BufferedInputStream(fin);
      IO.copyB(in, out, bufferSize);
    }
  }
}
