package thredds.server.radar;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.test.util.TestOnLocalServer;
import ucar.httpservices.HTTPFactory;
import ucar.httpservices.HTTPMethod;
import ucar.nc2.util.IO;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
@Category(NeedsCdmUnitTest.class)
public class TestRadarServer {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Parameterized.Parameters(name = "{0}")
  public static java.util.Collection<Object[]> getTestParameters() {
    return Arrays.asList(new Object[][] {
        // {"/radar/radarCollections.xml"},
        {"/radarServer/nexrad/level2/IDD/dataset.xml"}, {"/radarServer/nexrad/level2/IDD/stations.xml"},
        {"/radarServer/nexrad/level2/IDD?stn=KDGX&time_start=2014-06-05T12:47:17&time_end=2014-06-05T16:07:17"},
        {"/radarServer/nexrad/level3/IDD/stations.xml"}, {"/radarServer/terminal/level3/IDD/stations.xml"},


        // s3 tests
        {"/radarServer/s3/nexrad/level2/IDD/dataset.xml"}, {"/radarServer/s3/nexrad/level2/IDD/stations.xml"},
        {"/radarServer/s3/nexrad/level2/IDD?stn=KDGX&time_start=2014-06-05T12:47:17&time_end=2014-06-05T16:07:17"},
        {"/radarServer/s3/nexrad/level3/IDD/stations.xml"},

    });
  }

  private static final String expectedContentType = "application/xml";
  String path;

  public TestRadarServer(String path) {
    this.path = TestOnLocalServer.withHttpPath(path);
  }

  @org.junit.Test
  public void testReadRadarXml() throws IOException {
    URI catUri = null;
    try {
      catUri = new URI(path);
    } catch (URISyntaxException e) {
      fail("Bad syntax in catalog URI [" + path + "]: " + e.getMessage());
    }

    try (HTTPMethod method = HTTPFactory.Get(path)) {
      int status = method.execute();
      assertThat(status).isEqualTo(200);
      assertThat(method.getResponseHeaderValue("Content-Type").get()).isEqualTo(expectedContentType);
      String responseContent = "";
      try (InputStream is = method.getResponseBodyAsStream()) {
        responseContent = IO.readContents(is);
      }
      assertThat(validateResponseContent(responseContent)).isTrue();
    }
  }

  private static boolean validateResponseContent(String responseContent) {
    return responseContent.contains("<dataset") || responseContent.contains("<stationsList>");
  }

}
