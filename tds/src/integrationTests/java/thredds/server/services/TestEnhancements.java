package thredds.server.services;

import org.junit.Test;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import ucar.nc2.dataset.NetcdfDatasets;

import static com.google.common.truth.Truth.assertThat;

public class TestEnhancements {
  private static final double TOLERANCE = 1.0e-5;

  final static String ENHANCED_FILE = "localContent/testOffset.nc";
  final static String NCML_ENHANCED_FILE = "testOffsetWithNcml.nc";
  final static String NCML_ENHANCE_NONE_FILE = "testOffsetWithNcmlEnhanceNone.nc";
  final static String ENHANCED_VAR_NAME = "variableWithOffset";
  final static String NOT_ENHANCED_VAR_NAME = "variableWithoutOffset";

  @Test
  public void testNCSSWithEnhancements() throws IOException {
    // scale-offset set as variable attribute
    final String endpoint = TestOnLocalServer
        .withHttpPath("/ncss/grid/" + ENHANCED_FILE + "?var=" + ENHANCED_VAR_NAME + "&var=" + NOT_ENHANCED_VAR_NAME);
    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.netcdf);

    try (NetcdfFile netcdfFile = NetcdfFiles.openInMemory("test_data.nc", content)) {
      checkResultWithEnhancements(netcdfFile, 0, true);
    }
  }

  @Test
  public void testNCSSWithEnhancementsNcML() throws IOException {
    // scale-offset set as variable attribute
    final String endpoint = TestOnLocalServer.withHttpPath(
        "/ncss/grid/" + NCML_ENHANCED_FILE + "?var=" + ENHANCED_VAR_NAME + "&var=" + NOT_ENHANCED_VAR_NAME);
    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.netcdf);

    try (NetcdfFile netcdfFile = NetcdfFiles.openInMemory("test_data.nc", content)) {
      checkResultWithEnhancements(netcdfFile, 100, true);
    }
  }

  @Test
  public void testOpendapWithEnhancements() throws IOException {
    final String endpoint = TestOnLocalServer.withHttpPath("/dodsC/" + ENHANCED_FILE);

    try (NetcdfFile netcdfFile = NetcdfDatasets.openFile(endpoint, null)) {
      // Does not apply dataset attribute enhancements to data
      checkResultWithEnhancements(netcdfFile, -100, true);
    }
  }

  @Test
  public void testOpendapWithEnhancementsNcML() throws IOException {
    final String endpoint = TestOnLocalServer.withHttpPath("/dodsC/" + NCML_ENHANCED_FILE);

    try (NetcdfFile netcdfFile = NetcdfDatasets.openFile(endpoint, null)) {
      // Does apply NcML enhancements to data with enhance="all"
      checkResultWithEnhancements(netcdfFile, 100, true);
    }
  }

  @Test
  public void testOpendapWithEnhanceNoneNcML() throws IOException {
    final String endpoint = TestOnLocalServer.withHttpPath("/dodsC/" + NCML_ENHANCE_NONE_FILE);

    try (NetcdfFile netcdfFile = NetcdfDatasets.openFile(endpoint, null)) {
      // Does not apply NcML enhancements to data with enhance="none"
      checkResultWithEnhancements(netcdfFile, -100, false);
    }
  }

  private void checkResultWithEnhancements(NetcdfFile netcdfFile, int expectedDiff, boolean shouldRemoveOffsetAtt)
      throws IOException {
    final Variable enhancedVar = netcdfFile.findVariable(ENHANCED_VAR_NAME);
    final Variable orgVar = netcdfFile.findVariable(NOT_ENHANCED_VAR_NAME);
    assertThat(enhancedVar != null).isTrue();
    assertThat(orgVar != null).isTrue();
    assertThat(orgVar.findAttribute("add_offset") == null).isEqualTo(shouldRemoveOffsetAtt);
    final Array values1 = enhancedVar.read();
    final Array values2 = orgVar.read();
    for (int i = 0; i < values1.getSize(); i++) {
      assertThat(values1.getDouble(i)).isWithin(TOLERANCE).of(values2.getDouble(i) + expectedDiff);
    }
  }
}
