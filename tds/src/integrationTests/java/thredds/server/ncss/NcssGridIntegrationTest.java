/*
 * Copyright (c) 1998-2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.ncss;

import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.test.util.TestOnLocalServer;
import thredds.util.Constants;
import thredds.util.ContentType;
import thredds.util.TestUtils;
import ucar.httpservices.HTTPException;
import ucar.httpservices.HTTPFactory;
import ucar.httpservices.HTTPMethod;
import ucar.httpservices.HTTPSession;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ffi.netcdf.NetcdfClibrary;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;
import static ucar.ma2.MAMath.nearlyEquals;

@Category(NeedsCdmUnitTest.class)
public class NcssGridIntegrationTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private void openBinaryNew(byte[] content, String gridName) throws IOException {
    try (NetcdfFile nf = NetcdfFiles.openInMemory("test_data.nc", content)) {
      GridDataset gdsDataset = new GridDataset(NetcdfDatasets.enhance(nf, NetcdfDataset.getDefaultEnhanceMode(), null));
      assertThat(gdsDataset.findGridByName(gridName)).isNotNull();
      logger.debug("{}", nf);
    }
  }

  @Test
  public void checkGridForDifferentFormats() throws Exception {
    checkGrid("");
    checkGrid("&accept=netcdf");
    checkGrid("&accept=netcdf3");
    checkGrid("&accept=netcdf4");
    checkGrid("&accept=netcdf4-classic");
    checkGrid("&accept=netcdf4ext");
  }

  private void checkGrid(String acceptParameter) throws Exception {
    final String path =
        "/ncss/grid/gribCollection/GFS_CONUS_80km/GFS_CONUS_80km_20120227_0000.grib1?var=Temperature_isobaric";
    final String endpoint = TestOnLocalServer.withHttpPath(path + acceptParameter);

    byte[] content = TestOnLocalServer.getContent(endpoint, 200, ContentType.netcdf);
    openBinaryNew(content, "Temperature_isobaric");
  }

  @Test
  public void shouldWriteLongAttributeToNetcdf4ExtendedModel() throws Exception {
    final String path = "/ncss/grid/scanLocal/testGridWithLongAttribute.nc4?var=var&accept=netcdf4ext";
    final String endpoint = TestOnLocalServer.withHttpPath(path);

    byte[] content = TestOnLocalServer.getContent(endpoint, 200, ContentType.netcdf);

    try (NetcdfFile nf = NetcdfFiles.openInMemory("test_data.nc", content)) {
      assertThat(nf.findGlobalAttribute("globalLongAttribute")).isNotNull();

      GridDataset gdsDataset = new GridDataset(NetcdfDatasets.enhance(nf, NetcdfDataset.getDefaultEnhanceMode(), null));
      GeoGrid grid = gdsDataset.findGridByName("var");
      assertThat(grid).isNotNull();

      Array data = grid.readDataSlice(-1, -1, -1, -1);
      assertThat(nearlyEquals(data, Array.makeFromJavaArray(new int[] {0, 1, 2, 3}))).isTrue();
    }
  }

  @Ignore("TODO Fix S3 FeatureCollection index path")
  @Test
  public void checkS3GridWithNonTopLevelS3Key() throws IOException {
    String endpoint = TestOnLocalServer.withHttpPath(
        "/ncss/grid/S3GribCollectionWithFolders/GFS_CONUS_80km_20120227_0000.grib1?var=Temperature_isobaric");

    byte[] content = TestOnLocalServer.getContent(endpoint, 200, ContentType.netcdf);
    openBinaryNew(content, "Temperature_isobaric");
  }

  @Ignore("TODO Fix S3 FeatureCollection index path")
  @Test
  public void checkS3GridWithTopLevelS3Key() throws IOException {
    String endpoint = TestOnLocalServer.withHttpPath(
        "/ncss/grid/S3GribCollectionWithoutFolders/GFS_CONUS_80km_20120227_0000.grib1?var=Temperature_isobaric");

    byte[] content = TestOnLocalServer.getContent(endpoint, 200, ContentType.netcdf);
    openBinaryNew(content, "Temperature_isobaric");
  }

  @Test
  public void checkGridNoVars() throws Exception {
    String endpoint = TestOnLocalServer.withHttpPath("/ncss/grid/testGFSfmrc/GFS_CONUS_80km_nc_best.ncd");
    TestOnLocalServer.getContent(endpoint, 400);
  }

  @Test
  public void checkFmrcBest() throws Exception {
    String endpoint = TestOnLocalServer.withHttpPath(
        "/ncss/grid/testGFSfmrc/GFS_CONUS_80km_nc_best.ncd?var=Relative_humidity_height_above_ground,Temperature_height_above_ground");

    byte[] content = TestOnLocalServer.getContent(endpoint, 200, ContentType.netcdf);

    // Open the binary response in memory
    openBinaryNew(content, "Relative_humidity_height_above_ground");
  }

  // this fails when _ChunkSizes are left on
  @Test
  public void testNcssFailure() throws Exception {
    TestUtils.skipTestIfNetCDF4NotPresent();

    String filename =
        "scanCdmUnitTests/formats/netcdf4/COMPRESS_LEV2_20140201000000-GLOBCURRENT-L4-CURekm_15m-ERAWS_EEM-v02.0-fv01.0.nc";
    String endpoint = TestOnLocalServer.withHttpPath("/ncss/grid/" + filename
        + "?var=eastward_ekman_current_velocity&north=79.8750&west=-140&east=170&south=-79.8750&horizStride=1&"
        + "time_start=2014-02-01T00%3A00%3A00Z&time_end=2014-02-01T00%3A00%3A00Z&timeStride=1&accept=netcdf4-classic");

    byte[] content = TestOnLocalServer.getContent(endpoint, 200, ContentType.netcdf);

    // Open the binary response in memory
    openBinaryNew(content, "eastward_ekman_current_velocity");
  }

  @Test
  public void shouldReturnCorrectFileTypeForAcceptParameter() throws HTTPException {
    TestUtils.skipTestIfNetCDF4NotPresent();

    checkFileType("netcdf3", HttpServletResponse.SC_OK, ".nc");
    checkFileType("netcdf", HttpServletResponse.SC_OK, ".nc");
    checkFileType("netcdf4-classic", HttpServletResponse.SC_OK, ".nc4");
    checkFileType("netcdf4", HttpServletResponse.SC_OK, ".nc4");
    checkFileType("netcdf4ext", HttpServletResponse.SC_OK, ".nc4");
  }

  private void checkFileType(String acceptParameter, int expectedResponseCode, String expectedSuffix)
      throws HTTPException {
    final String path =
        "/ncss/grid/gribCollection/GFS_CONUS_80km/GFS_CONUS_80km_20120227_0000.grib1?var=Temperature_isobaric";
    final String endpoint = TestOnLocalServer.withHttpPath(path + "&accept=" + acceptParameter);

    try (HTTPSession session = HTTPFactory.newSession(endpoint)) {
      final HTTPMethod method = HTTPFactory.Head(session);

      final int status = method.execute();
      assertThat(status).isEqualTo(expectedResponseCode);

      if (status == HttpServletResponse.SC_OK) {
        Optional<String> filename = method.getResponseHeaderValue(Constants.Content_Disposition);
        assertThat(filename.isPresent()).isTrue();
        assertThat(filename.get())
            .isEqualTo("attachment; filename=GFS_CONUS_80km_20120227_0000.grib1" + expectedSuffix);
      }
    }
  }

  @Test
  public void shouldNotReturnNaNsForBestGribCollectionVariable() throws IOException, InvalidRangeException {
    // precip variable not present in 2024-03-01T19:00:00Z partition
    final String varName = "Total_precipitation_Forecast_altitude_above_msl_1_Hour_Accumulation";
    final String timeBeforeMissing = "2024-03-01T18:00:00Z";
    final String timeAfterMissing = "2024-03-01T21:00:00Z";

    final String endpoint = TestOnLocalServer.withHttpPath("/ncss/grid/grib/NCEP/RTMA/CONUS_2p5km/Best" + "?var="
        + varName + "&north=40&west=-70&east=-69&south=39&horizStride=1&accept=netcdf3" + "&time_start="
        + timeBeforeMissing + "&time_end=" + timeAfterMissing);

    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.netcdf);

    try (NetcdfFile ncf = NetcdfFiles.openInMemory("test_data.nc", content)) {
      final Variable var = ncf.findVariable(varName);
      assertThat((Object) var).isNotNull();
      // first element for all times
      final Array array = var.read(":,0:0,0:0,0:0");
      assertThat(array.getSize()).isEqualTo(3);
      assertThat(array.getFloat(0)).isNotNaN();
      assertThat(array.getFloat(1)).isNotNaN();
      assertThat(array.getFloat(2)).isNotNaN();
    }
  }
}
