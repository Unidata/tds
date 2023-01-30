package thredds.featurecollection;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import thredds.test.util.TestOnLocalServer;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

public class TestGribFeatureCollection {

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void shouldReturnCorrectTimeRange() throws IOException {
    final String collection = "HRRR/analysis/";
    final String topPartition = collection + "TP";
    final String singleFile = collection + "HRRR_CONUS_2p5km_ana_20150706_2000.grib2";

    assertTimeHasSizeAndRank(topPartition, 4, 1);
    assertTimeHasSizeAndRank(singleFile, 1, 1);
  }

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void shouldReturnCorrectTimeRangeFor2DTimeCollection() throws IOException {
    final String collection = "gribCollection/gfsConus80_file/gfsConus80_file-20141024/";
    final String twoD = collection + "TwoD";
    final String singleFile = collection + "GFS_CONUS_80km_20141024_0000.grib1";
    final String best = collection + "Best";

    assertTimeHasSizeAndRank(twoD, 84, 2);
    assertTimeHasSizeAndRank(singleFile, 1, 1);
    assertTimeHasSizeAndRank(best, 4, 1);
  }

  private static void assertTimeHasSizeAndRank(String path, int size, int rank) throws IOException {
    final String endpoint = TestOnLocalServer.withHttpPath("dodsC/" + path);
    try (NetcdfFile ncfile = NetcdfDatasets.openFile(endpoint, null)) {
      final Variable time = ncfile.findVariable("time");
      assertThat((Object) time).isNotNull();
      assertThat(time.getSize()).isEqualTo(size);
      assertThat(time.getRank()).isEqualTo(rank);
    }
  }
}
