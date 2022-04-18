package thredds.server.ncss.view.dsg;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import thredds.server.ncss.format.SupportedFormat;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft2.coverage.CoverageCollection;
import ucar.nc2.ft2.coverage.CoverageDatasetFactory;
import ucar.nc2.ft2.coverage.SubsetParams;
import ucar.nc2.ft2.coverage.writer.CoverageAsPoint;
import ucar.unidata.geoloc.LatLonPoint;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class TestMixedTypeSubsetWriter {
  @Rule
  public final TemporaryFolder tempFolder = new TemporaryFolder();

  @Parameterized.Parameters(name = "{0}")
  public static List<Object[]> getTestParameters() {
    return Arrays.asList(
        new Object[][] {{SupportedFormat.CSV_FILE, "outputAll.csv"}, {SupportedFormat.XML_FILE, "outputAll.xml"},});
  }

  private final SupportedFormat format;
  private final String expectedResultResource;

  private static SubsetParams subsetParams;
  private static FeatureDatasetPoint fdPoint;

  public TestMixedTypeSubsetWriter(SupportedFormat format, String expectedResultResource) throws IOException {
    this.format = format;
    this.expectedResultResource = expectedResultResource;

    CoverageCollection gds = CoverageDatasetFactory.open("src/test/content/thredds/public/testdata/testGridAsPoint.nc")
        .getCoverageCollections().get(0);

    final double lat = 3.0;
    final double lon = 5.0;
    final LatLonPoint latlon = LatLonPoint.create(lat, lon);
    List<String> varNames = new ArrayList<>();
    // y, x
    varNames.add("2D");
    // z, y, x
    varNames.add("3D");
    varNames.add("full3");
    // t, y, x
    varNames.add("T1noZ");
    // t, z, y, x
    varNames.add("4D");
    varNames.add("full4");

    subsetParams = new SubsetParams();
    subsetParams.setVariables(varNames);
    subsetParams.setLatLonPoint(latlon);
    fdPoint = new CoverageAsPoint(gds, varNames, subsetParams).asFeatureDatasetPoint();
  }

  @Test
  public void testMixedPointTypeWithCoverageAsPoint() throws Exception {
    File expectedResultFile = new File(
        getClass().getResource("any_point/" + format.name().toLowerCase() + "/" + expectedResultResource).toURI());
    File actualResultFile = tempFolder.newFile();

    try (OutputStream outFileStream = new BufferedOutputStream(new FileOutputStream(actualResultFile))) {
      DsgSubsetWriter subsetWriterFile =
          DsgSubsetWriterFactory.newInstance(fdPoint, subsetParams, null, outFileStream, format);
      subsetWriterFile.write();
    }
    Assert.assertTrue(
        String.format("Files differed:\n\texpected: %s\n\tactual: %s", expectedResultFile, actualResultFile),
        DsgSubsetTestUtils.compareText(expectedResultFile, actualResultFile));
  }
}
