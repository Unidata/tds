package thredds.server.services;

import org.junit.Ignore;
import org.junit.Test;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;
import ucar.ma2.Array;
import ucar.ma2.MAMath;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class TestEnhancements {

  final static String ENHANCED_FILE = "localContent/testOffset.nc";
  final static String NCML_ENHANCED_FILE = "testOffsetWithNcml.nc";
  final static String ENHANCED_VAR_NAME = "variableWithOffset";
  final static String NOT_ENHANCED_VAR_NAME = "variableWithoutOffset";

  @Test
  public void testNCSSWithEnhancements() throws IOException {
    // scale-offset set as variable attribute
    final String endpoint = TestOnLocalServer
        .withHttpPath("/ncss/grid/" + ENHANCED_FILE + "?var=" + ENHANCED_VAR_NAME + "&var=" + NOT_ENHANCED_VAR_NAME);
    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.netcdf);

    try (NetcdfFile netcdfFile = NetcdfFiles.openInMemory("test_data.nc", content)) {
      final Variable enhancedVar = netcdfFile.findVariable(ENHANCED_VAR_NAME);
      final Variable orgVar = netcdfFile.findVariable(NOT_ENHANCED_VAR_NAME);
      assertThat(enhancedVar != null).isTrue();
      assertThat(orgVar != null).isTrue();
      assertThat(enhancedVar.findAttribute("add_offset")).isNull();
      final Array values1 = enhancedVar.read();
      final Array values2 = orgVar.read();
      MAMath.nearlyEquals(values1, values2);
    }
  }

  @Test
  public void testNCSSWithEnhancementsNcML() throws IOException {
    // scale-offset set as variable attribute
    final String endpoint = TestOnLocalServer.withHttpPath(
        "/ncss/grid/" + NCML_ENHANCED_FILE + "?var=" + ENHANCED_VAR_NAME + "&var=" + NOT_ENHANCED_VAR_NAME);
    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.netcdf);

    try (NetcdfFile netcdfFile = NetcdfFiles.openInMemory("test_data.nc", content)) {
      final Variable enhancedVar = netcdfFile.findVariable(ENHANCED_VAR_NAME);
      final Variable orgVar = netcdfFile.findVariable(NOT_ENHANCED_VAR_NAME);
      assertThat(enhancedVar != null).isTrue();
      assertThat(orgVar != null).isTrue();
      assertThat(orgVar.findAttribute("add_offset")).isNull();
      final Array values1 = enhancedVar.read();
      final Array values2 = orgVar.read();
      for (int i = 0; i < values1.getSize(); i++) {
        assertThat(values1.getDouble(i)).isEqualTo(values2.getDouble(i) + 100);
      }
    }
  }
}
