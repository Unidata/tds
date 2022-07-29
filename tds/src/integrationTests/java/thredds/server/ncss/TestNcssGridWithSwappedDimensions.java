package thredds.server.ncss;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

@RunWith(Parameterized.class)
public class TestNcssGridWithSwappedDimensions {

  private static final String FILENAME = "localContent/testDimensionOrder.nc";

  @Parameterized.Parameters(name = "{0}")
  public static List<Object[]> getTestParameters() {
    List<Object[]> testCases = new ArrayList<>();

    testCases.add(new Object[] {"lat_lon", 2, 6});
    testCases.add(new Object[] {"lon_lat", 2, 6});
    testCases.add(new Object[] {"time_lat_lon", 3, 6});
    testCases.add(new Object[] {"time_lon_lat", 3, 6});
    testCases.add(new Object[] {"time_z_lat_lon", 4, 12});
    testCases.add(new Object[] {"time_z_lon_lat", 4, 12});
    testCases.add(new Object[] {"time_lat_lon_z", 4, 12});
    testCases.add(new Object[] {"time_lon_lat_z", 4, 12});

    return testCases;
  }

  private final String variableName;
  private final int rank;
  private final int size;

  public TestNcssGridWithSwappedDimensions(String variableName, int rank, int size) {
    this.variableName = variableName;
    this.rank = rank;
    this.size = size;
  }

  @Test
  public void shouldReturnDataWhenDimensionsAreSwapped() throws IOException {
    final String endpoint = TestOnLocalServer.withHttpPath("/ncss/grid/" + FILENAME + "?var=" + variableName);

    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.netcdf);

    try (NetcdfFile netcdfFile = NetcdfFiles.openInMemory("test_data.nc", content)) {
      final Variable variable = netcdfFile.findVariable(variableName);
      assertThat(variable != null).isTrue();
      assertThat(variable.getRank()).isEqualTo(rank);

      final Array values = variable.read();
      assertThat(values.getSize()).isEqualTo(size);

      for (int i = 0; i < values.getSize(); i++) {
        assertThat(values.getInt(i)).isEqualTo(i);
      }
    }
  }
}
