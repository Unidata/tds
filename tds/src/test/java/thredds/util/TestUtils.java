package thredds.util;

import thredds.server.ncss.format.SupportedFormat;
import ucar.nc2.ffi.netcdf.NetcdfClibrary;

import static org.junit.Assume.assumeTrue;

public class TestUtils {

  public static void skipTestIfNetCDF4NotPresent(SupportedFormat format) {
    if (format == SupportedFormat.NETCDF4) {
      skipTestIfNetCDF4NotPresent();
    }
  }

  public static void skipTestIfNetCDF4NotPresent() {
    assumeTrue(NetcdfClibrary.isLibraryPresent());
  }
}
