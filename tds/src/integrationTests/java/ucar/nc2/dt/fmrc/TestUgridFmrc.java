/*
 * Copyright (c) 2022 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package ucar.nc2.dt.fmrc;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import thredds.test.util.TdsTestDir;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.unidata.util.test.TestDir;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

public class TestUgridFmrc {

  private static final String FILE_1 = TestDir.cdmUnitTestDir + "conventions/ugrid/USF_WSF/usf_fvcom_2019_01_27.nc";
  private static final String FILE_2 = TestDir.cdmUnitTestDir + "conventions/ugrid/USF_WSF/usf_fvcom_2019_01_28.nc";

  private static NetcdfFile ncf1;
  private static NetcdfFile ncf2;

  @BeforeClass
  public static void openIndividualFiles() {
    try {
      ncf1 = NetcdfDatasets.openFile(FILE_1, null);
      ncf2 = NetcdfDatasets.openFile(FILE_2, null);
    } catch (IOException e) {
      System.out.println("Could not setup the test. Failed to open one of the test files. " + e.getMessage());
      assertThat(ncf1).isNotNull();
      assertThat(ncf2).isNotNull();
    }
  }

  /**
   * Test that FMRC 2D collection is pulling data from the correct granules
   */
  @Test
  @Category(NeedsCdmUnitTest.class)
  public void test2DFmrc() throws IOException, InvalidRangeException {
    final String urlPath2d = "/thredds/cdmremote/testUsfWsfUgridFmrc/USF_WSF_nc_fmrc.ncd";
    final String tdsLocation = "cdmremote:http://" + TdsTestDir.remoteTestServer + urlPath2d;
    String testVariableName = "salinity";
    String singleRuntimeSection = ":,2,10:10000:100";
    try (NetcdfFile fmrc2d = NetcdfDatasets.openFile(tdsLocation, null)) {
      ImmutableList<Variable> vars = fmrc2d.getVariables();
      assertThat(vars).isNotEmpty();
      // read data from variable
      Variable fmrcVar = fmrc2d.findVariable(testVariableName);
      Array fmrcData = fmrcVar.read("0," + singleRuntimeSection);
      // test against data from individual files
      Variable ncf1Var = ncf1.findVariable(testVariableName);
      Array ncf1Data = ncf1Var.read(singleRuntimeSection);
      assertThat(ncf1Data.get1DJavaArray(DataType.FLOAT)).isEqualTo(fmrcData.get1DJavaArray(DataType.FLOAT));

      Variable ncf2Var = ncf2.findVariable(testVariableName);
      Array ncf2Data = ncf2Var.read(singleRuntimeSection);
      assertThat(ncf2Data.get1DJavaArray(DataType.FLOAT)).isNotEqualTo(fmrcData.get1DJavaArray(DataType.FLOAT));

      fmrcData = fmrcVar.read("1," + singleRuntimeSection);
      assertThat(ncf2Data.get1DJavaArray(DataType.FLOAT)).isEqualTo(fmrcData.get1DJavaArray(DataType.FLOAT));
    }
  }

  /**
   * Test that FMRC Constant Forecast Offset collection is pulling data from the correct granules
   */
  @Test
  @Category(NeedsCdmUnitTest.class)
  public void testCFOFmrc() throws IOException, InvalidRangeException {
    // 8 hour offset into a collection of runs that output hourly
    final String urlPathCfo = "/thredds/cdmremote/testUsfWsfUgridFmrc/offset/USF_WSF_nc_Offset_8.0hr";
    final String tdsLocation = "cdmremote:http://" + TdsTestDir.remoteTestServer + urlPathCfo;
    String testVariableName = "salinity";
    String singleSpatialSection = "2,10:10000:100";
    // index 8 => forecast hour 8 since index 0 is forecast hour 0
    String singleRunTimeIndex = "8";
    try (NetcdfFile fmrcCfo = NetcdfDatasets.openFile(tdsLocation, null)) {
      ImmutableList<Variable> vars = fmrcCfo.getVariables();
      assertThat(vars).isNotEmpty();
      // read data from variable
      Variable fmrcVar = fmrcCfo.findVariable(testVariableName);
      Array fmrcData = fmrcVar.read("0," + singleSpatialSection);
      // test against data from individual files
      Variable ncf1Var = ncf1.findVariable(testVariableName);
      Array ncf1Data = ncf1Var.read(singleRunTimeIndex + "," + singleSpatialSection);
      assertThat(ncf1Data.get1DJavaArray(DataType.FLOAT)).isEqualTo(fmrcData.get1DJavaArray(DataType.FLOAT));

      Variable ncf2Var = ncf2.findVariable(testVariableName);
      Array ncf2Data = ncf2Var.read(singleRunTimeIndex + "," + singleSpatialSection);
      assertThat(ncf2Data.get1DJavaArray(DataType.FLOAT)).isNotEqualTo(fmrcData.get1DJavaArray(DataType.FLOAT));

      fmrcData = fmrcVar.read("1," + singleSpatialSection);
      assertThat(ncf2Data.get1DJavaArray(DataType.FLOAT)).isEqualTo(fmrcData.get1DJavaArray(DataType.FLOAT));
    }
  }

  /**
   * Test that FMRC Constant Forecast Time collection is pulling data from the correct granule
   */
  @Test
  @Category(NeedsCdmUnitTest.class)
  public void testCFTFmrc() throws IOException, InvalidRangeException {
    // 8 hour offset into a collection of runs that output hourly
    final String urlPathCft =
        "/thredds/cdmremote/testUsfWsfUgridFmrc/forecast/USF_WSF_nc_ConstantForecast_2019-01-28T03:00:00Z";
    final String tdsLocation = "cdmremote:http://" + TdsTestDir.remoteTestServer + urlPathCft;
    String testVariableName = "salinity";
    String singleSpatialSection = "2,10:10000:100";
    // Forecast time 2019-01-28T03:00:00Z should be the 4th time index into the second granule of the collection
    // 4th time value is index value 3 since zero based
    String singleRunTimeIndex = "3";
    try (NetcdfFile fmrcCfo = NetcdfDatasets.openFile(tdsLocation, null)) {
      ImmutableList<Variable> vars = fmrcCfo.getVariables();
      assertThat(vars).isNotEmpty();
      // read data from variable
      Variable fmrcVar = fmrcCfo.findVariable(testVariableName);
      Array fmrcData = fmrcVar.read("0," + singleSpatialSection);
      // test against data from individual file
      Variable ncf2Var = ncf2.findVariable(testVariableName);
      Array ncf2Data = ncf2Var.read(singleRunTimeIndex + "," + singleSpatialSection);
      assertThat(ncf2Data.get1DJavaArray(DataType.FLOAT)).isEqualTo(fmrcData.get1DJavaArray(DataType.FLOAT));
    }
  }

  /**
   * Test that FMRC Forecast Model Run collection is pulling data from the correct granule
   */
  @Test
  @Category(NeedsCdmUnitTest.class)
  public void testFMRFmrc() throws IOException, InvalidRangeException {
    // the 2019-01-28T00:00:00Z Forecast Model Run should be the same as the second granule that makes up collection.
    final String urlPathCft = "/thredds/cdmremote/testUsfWsfUgridFmrc/runs/USF_WSF_nc_RUN_2019-01-28T00:00:00Z";
    final String tdsLocation = "cdmremote:http://" + TdsTestDir.remoteTestServer + urlPathCft;
    String testVariableName = "salinity";
    String testSection = ":,2,10:10000:100";
    try (NetcdfFile fmrcCfo = NetcdfDatasets.openFile(tdsLocation, null)) {
      ImmutableList<Variable> vars = fmrcCfo.getVariables();
      assertThat(vars).isNotEmpty();
      // read data from variable
      Variable fmrcVar = fmrcCfo.findVariable(testVariableName);
      Array fmrcData = fmrcVar.read(testSection);
      // test against data from individual file
      Variable ncf2Var = ncf2.findVariable(testVariableName);
      Array ncf2Data = ncf2Var.read(testSection);
      assertThat(ncf2Data.get1DJavaArray(DataType.FLOAT)).isEqualTo(fmrcData.get1DJavaArray(DataType.FLOAT));
    }
  }

  @AfterClass
  public static void cleanup() throws IOException {
    if (ncf1 != null) {
      ncf1.close();
    }
    if (ncf2 != null) {
      ncf2.close();
    }
  }
}
