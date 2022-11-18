package thredds.server.wms;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.servlet.ServletException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import java.lang.invoke.MethodHandles;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml"})
public class TestWmsCache {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String DIR = "src/test/content/thredds/public/testdata/";
  private static final Path TEST_FILE = Paths.get(DIR, "testGridAsPoint.nc");
  private static final Path TEMP_FILE = Paths.get(DIR, "testUpdate.nc");
  private static final String TEST_PATH = "localContent/testUpdate.nc";
  private static final String S3_TEST_PATH = "s3-thredds-test-data/ncml/nc/namExtract/20060925_0600.nc";
  private static final String AGGREGATION_RECHECK_MSEC_PATH = "aggRecheck/millisecond";
  private static final String AGGREGATION_RECHECK_MINUTE_PATH = "aggRecheck/minute";

  final private ThreddsWmsServlet threddsWmsServlet = new ThreddsWmsServlet();

  @Before
  public void createTestFiles() throws IOException {
    Files.copy(TEST_FILE, TEMP_FILE);
  }

  @After
  public void cleanupTestFiles() throws IOException {
    Files.delete(TEMP_FILE);
  }

  @Test
  public void shouldCacheFile() throws IOException, ServletException {
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(TEST_PATH)).isFalse();
    getCapabilities(TEST_PATH);
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(TEST_PATH)).isTrue();
    assertThat(ThreddsWmsServlet.useCachedCatalogue(TEST_PATH)).isTrue();
  }

  // TODO also test updating an S3 file (currently not implemented through MFileS3)
  @Test
  public void shouldCacheS3File() throws IOException, ServletException {
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(S3_TEST_PATH)).isFalse();
    getCapabilities(S3_TEST_PATH);
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(S3_TEST_PATH)).isTrue();
    assertThat(ThreddsWmsServlet.useCachedCatalogue(S3_TEST_PATH)).isTrue();
  }

  @Test
  public void shouldNotUseOutdatedCacheFile() throws IOException, ServletException {
    getCapabilities(TEST_PATH);
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(TEST_PATH)).isTrue();
    assertThat(ThreddsWmsServlet.useCachedCatalogue(TEST_PATH)).isTrue();

    updateTestFile();
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(TEST_PATH)).isTrue();
    assertThat(ThreddsWmsServlet.useCachedCatalogue(TEST_PATH)).isFalse();
  }

  @Test
  public void shouldUseUnchangedAggregation() throws IOException, ServletException {
    getCapabilities(AGGREGATION_RECHECK_MINUTE_PATH);
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(AGGREGATION_RECHECK_MINUTE_PATH)).isTrue();
    assertThat(ThreddsWmsServlet.useCachedCatalogue(AGGREGATION_RECHECK_MINUTE_PATH)).isTrue();
  }

  @Test
  public void shouldUseRecheckedButUnchangedAggregation() throws IOException, ServletException {
    getCapabilities(AGGREGATION_RECHECK_MSEC_PATH);
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(AGGREGATION_RECHECK_MSEC_PATH)).isTrue();
    assertThat(ThreddsWmsServlet.useCachedCatalogue(AGGREGATION_RECHECK_MSEC_PATH)).isTrue();

    // Will be rechecked after 1 ms
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(AGGREGATION_RECHECK_MSEC_PATH)).isTrue();
    assertThat(ThreddsWmsServlet.useCachedCatalogue(AGGREGATION_RECHECK_MSEC_PATH)).isTrue();
  }

  @Test
  public void shouldNotUseOutdatedAggregation() throws IOException, ServletException {
    getCapabilities(AGGREGATION_RECHECK_MSEC_PATH);
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(AGGREGATION_RECHECK_MSEC_PATH)).isTrue();
    assertThat(ThreddsWmsServlet.useCachedCatalogue(AGGREGATION_RECHECK_MSEC_PATH)).isTrue();

    updateTestFile();
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(AGGREGATION_RECHECK_MSEC_PATH)).isTrue();
    assertThat(ThreddsWmsServlet.useCachedCatalogue(AGGREGATION_RECHECK_MSEC_PATH)).isFalse();
  }

  private void updateTestFile() throws IOException {
    Files.copy(TEST_FILE, TEMP_FILE, StandardCopyOption.REPLACE_EXISTING);
  }

  private void getCapabilities(String path) throws ServletException, IOException {
    final String uri = "/thredds/wms/" + path;
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
    request.setParameter("service", "WMS");
    request.setParameter("version", "1.3.0");
    request.setParameter("request", "GetCapabilities");
    request.setPathInfo(path);
    final MockHttpServletResponse response = new MockHttpServletResponse();

    threddsWmsServlet.service(request, response);
    assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_OK);
  }
}
