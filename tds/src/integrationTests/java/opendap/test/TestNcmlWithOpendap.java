package opendap.test;

import static com.google.common.truth.Truth.assertThat;
import static ucar.ma2.MAMath.nearlyEquals;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import thredds.test.util.TestOnLocalServer;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.unidata.util.test.TestDir;

public class TestNcmlWithOpendap {
  private static final double[] expectedValues =
      new double[] {-1.161895003862225, -0.3872983346207417, 0.3872983346207417, 1.161895003862225};

  @Test
  public void shouldUseNcmlToModifyDatasetScanValues() throws IOException {
    final String path = "/thredds/dodsC/StandardizeDatasetScan/testgrid1.nc";
    final String url = "dods://" + TestDir.remoteTestServer + path;
    try (NetcdfFile ncFile = NetcdfDatasets.openDataset(url)) {
      final Variable var = ncFile.findVariable("standardizedVar");
      assertThat((Object) var).isNotNull();
      final Array values = var.read();
      assertThat(nearlyEquals(values, Array.makeFromJavaArray(expectedValues))).isTrue();
    }
  }

  @Test
  public void shouldUseNcmlToModifyDatasetScanAsciiValues() {
    final String path = "/dodsC/StandardizeDatasetScan/testgrid1.nc.ascii?standardizedVar%5B0:1:1%5D%5B0:1:1%5D";
    final String endpoint = TestOnLocalServer.withHttpPath(path);

    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK);
    final String stringContent = new String(content, StandardCharsets.UTF_8);
    final DecimalFormat df = new DecimalFormat("#.###");
    df.setRoundingMode(RoundingMode.DOWN);
    Arrays.stream(expectedValues).forEach(d -> assertThat(stringContent).contains(df.format(d)));
  }
}
