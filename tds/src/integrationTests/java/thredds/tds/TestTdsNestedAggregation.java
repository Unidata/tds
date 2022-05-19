package thredds.tds;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import org.junit.Test;
import thredds.test.util.TestOnLocalServer;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDatasets;

public class TestTdsNestedAggregation {
  private static final String PATH = "dodsC/NestedAggregation";

  @Test
  public void shouldCreateNestedAggregation() throws IOException {
    final String endpoint = TestOnLocalServer.withHttpPath(PATH);

    try (final NetcdfFile netcdfFile = NetcdfDatasets.openFile(endpoint, null)) {
      assertThat(netcdfFile).isNotNull();
      assertThat(netcdfFile.getRootGroup().getDimensions().size()).isEqualTo(10);
      assertThat(netcdfFile.getVariables().size()).isEqualTo(25);

      assertThat(netcdfFile.findVariable("agg1").getShape()).isEqualTo(new int[] {2});
      assertThat(netcdfFile.findVariable("agg2").getShape()).isEqualTo(new int[] {2});
    }
  }
}
