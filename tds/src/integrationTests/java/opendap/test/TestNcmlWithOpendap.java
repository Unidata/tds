package opendap.test;

import static com.google.common.truth.Truth.assertThat;
import static ucar.ma2.MAMath.nearlyEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;
import thredds.test.util.TestOnLocalServer;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;

public class TestNcmlWithOpendap {
  private static final double[] expectedValues = new double[] {Double.NaN, 1, 2, 3};

  @Test
  public void shouldUseNcmlToModifyDatasetScanValues() throws IOException {
    final String path = "dodsC/EnhancedDatasetScan/testgrid1.nc";
    final String url = "dods://" + TestOnLocalServer.server + path;
    try (NetcdfFile ncFile = NetcdfDatasets.openDataset(url)) {
      final Variable var = ncFile.findVariable("enhancedVar");
      assertThat((Object) var).isNotNull();
      final Array values = var.read();
      assertThat(nearlyEquals(values, Array.makeFromJavaArray(expectedValues))).isTrue();
    }
  }

  @Test
  public void shouldUseNcmlToModifyDatasetScanAsciiValues() {
    final String path = "/dodsC/EnhancedDatasetScan/testgrid1.nc.ascii?enhancedVar%5B0:1:1%5D%5B0:1:1%5D";
    final String endpoint = TestOnLocalServer.withHttpPath(path);

    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK);
    final String stringContent = new String(content, StandardCharsets.UTF_8);
    final DecimalFormat df = new DecimalFormat("#.#");
    Arrays.stream(expectedValues).forEach(d -> assertThat(stringContent).contains(df.format(d)));
  }
}
