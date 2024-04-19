package thredds.server.wms;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import java.lang.invoke.MethodHandles;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.util.cache.FileCacheIF;
import ucar.unidata.io.RandomAccessFile;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml"})
public class TestWmsCache {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String DIR = "src/test/content/thredds/public/testdata/";
  private static final Path TEST_FILE = Paths.get(DIR, "testGridAsPoint.nc");
  private Path TEMP_FILE;// = Paths.get(DIR, "testUpdate.nc");
  private String TEST_PATH;// = "localContent/testUpdate.nc";
  private static String FILENAME = "testUpdate.nc";
  private static final String S3_TEST_PATH = "s3-thredds-test-data/ncml/nc/namExtract/20060925_0600.nc";
  private static final String AGGREGATION_RECHECK_MSEC_PATH = "aggRecheck/millisecond";
  private static final String AGGREGATION_RECHECK_MINUTE_PATH = "aggRecheck/minute";

  final private ThreddsWmsServlet threddsWmsServlet = new ThreddsWmsServlet();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder(new File(DIR));

  // @BeforeClass happens before TdsInit so we need @Before
  @Before
  public void disableRafCache() {
    // Avoid file locks which prevent files from being updated on windows
    final FileCacheIF rafCache = RandomAccessFile.getGlobalFileCache();
    if (rafCache != null) {
      rafCache.disable();
    }
  }

  @AfterClass
  public static void enableRafCache() {
    final FileCacheIF rafCache = RandomAccessFile.getGlobalFileCache();
    if (rafCache != null) {
      rafCache.enable();
    }
  }

  @Before
  public void createTestFiles() throws IOException {
    File tempFile = temporaryFolder.newFile(FILENAME);
    TEMP_FILE = tempFile.toPath();
    TEST_PATH = "localContent/" + new File(DIR).toURI().relativize(tempFile.toURI());
    Files.copy(TEST_FILE, TEMP_FILE, StandardCopyOption.REPLACE_EXISTING);
  }

  @Before
  public void clearNetcdfFileCache() {
    final FileCacheIF cache = NetcdfDatasets.getNetcdfFileCache();
    cache.clearCache(true);
    assertNoneLockedInNetcdfFileCache();
  }

  @After
  public void clearCache() {
    ThreddsWmsServlet.resetCache();
    assertThat(ThreddsWmsServlet.getNumberOfEntries()).isEqualTo(0);
    assertNoneLockedInNetcdfFileCache();
  }

  @Test
  public void shouldCacheFile() throws IOException, ServletException {
    assertAddedToCache(TEST_PATH);
    assertUsedCache(TEST_PATH);
  }

  // TODO also test updating an S3 file (currently not implemented through MFileS3)
  @Test
  public void shouldCacheS3File() throws IOException, ServletException {
    assertAddedToCache(S3_TEST_PATH);
    assertUsedCache(S3_TEST_PATH);
  }

  @Test
  public void shouldNotUseOutdatedCacheFile() throws IOException, ServletException {
    assertAddedToCache(TEST_PATH);
    updateTestFile();
    assertAddedToCache(TEST_PATH);
  }

  @Test
  public void shouldNotUseCacheFileWithNewerModified() throws IOException, ServletException {
    final File testFile = new File(TEMP_FILE.toUri());
    final long lastModified = testFile.lastModified();
    assertAddedToCache(TEST_PATH);

    // Change test file to have older last modified date
    assertThat(testFile.setLastModified(lastModified - 1)).isTrue();

    assertAddedToCache(TEST_PATH);
  }

  @Test
  public void shouldUseUnchangedAggregation() throws IOException, ServletException {
    assertAddedToCache(AGGREGATION_RECHECK_MINUTE_PATH);
    assertUsedCache(AGGREGATION_RECHECK_MINUTE_PATH);
  }

  @Test
  public void shouldUseRecheckedButUnchangedAggregation() throws IOException, ServletException {
    assertAddedToCache(AGGREGATION_RECHECK_MSEC_PATH);

    // Will be rechecked after 1 ms
    assertUsedCache(AGGREGATION_RECHECK_MSEC_PATH);
  }

  @Test
  public void shouldNotUseOutdatedAggregation() throws IOException, ServletException {
    assertAddedToCache(AGGREGATION_RECHECK_MSEC_PATH);
    updateTestFile();
    assertAddedToCache(AGGREGATION_RECHECK_MSEC_PATH);
  }

  @Test
  public void shouldNotLockFileInNetcdfFileCacheAfterCacheReset() throws IOException, ServletException {
    final String filename = "testGridAsPoint.nc";
    final String testPath = "localContent/" + filename;
    getCapabilities(testPath);
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(testPath)).isTrue();
    ThreddsWmsServlet.resetCache();

    // check file is not locked in netcdf file cache
    final FileCacheIF cache = NetcdfDatasets.getNetcdfFileCache();
    final List<String> entries = cache.showCache();
    assertThat(entries.size()).isGreaterThan(0);
    final boolean isLocked = entries.stream().filter(e -> e.contains(filename)).anyMatch(e -> e.startsWith("true"));
    assertWithMessage(cache.showCache().toString()).that(isLocked).isFalse();
  }

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void shouldNotLockAggregationInNetcdfFileCacheAfterCacheReset() throws IOException, ServletException {
    final String testPath = "ExampleNcML/Agg.nc";
    getCapabilities(testPath);
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(testPath)).isTrue();
    ThreddsWmsServlet.resetCache();

    assertNotLockedInNetcdfFileCache(testPath);
  }

  @Test
  public void shouldNotLockFileInCacheAfterExceptionIsThrown() throws ServletException, IOException {
    final String filename = "1day.nc";
    final String testPath = "localContent/" + filename;
    getCapabilities(testPath, HttpServletResponse.SC_BAD_REQUEST);
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(testPath)).isFalse();

    assertNotLockedInNetcdfFileCache(filename);
  }

  private void assertNoneLockedInNetcdfFileCache() {
    final FileCacheIF cache = NetcdfDatasets.getNetcdfFileCache();
    final List<String> entries = cache.showCache();
    final boolean isLocked = entries.stream().anyMatch(e -> e.startsWith("true"));
    assertWithMessage(cache.showCache().toString()).that(isLocked).isFalse();
  }

  private void assertNotLockedInNetcdfFileCache(String path) {
    final FileCacheIF cache = NetcdfDatasets.getNetcdfFileCache();
    final List<String> entries = cache.showCache();
    assertThat(entries.size()).isGreaterThan(0);
    final boolean isLocked = entries.stream().filter(e -> e.contains(path)).anyMatch(e -> e.startsWith("true"));
    assertWithMessage(cache.showCache().toString()).that(isLocked).isFalse();
  }

  private void updateTestFile() {
    final File testFile = new File(TEMP_FILE.toUri());
    assertThat(testFile.setLastModified(DateTime.now().getMillis())).isTrue();
  }

  private void assertUsedCache(String path) throws ServletException, IOException {
    long loads = ThreddsWmsServlet.getCacheLoads();
    getCapabilities(path);
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(path)).isTrue();
    assertThat(ThreddsWmsServlet.getCacheLoads()).isEqualTo(loads);
  }

  private void assertAddedToCache(String path) throws ServletException, IOException {
    long loads = ThreddsWmsServlet.getCacheLoads();
    getCapabilities(path);
    assertThat(ThreddsWmsServlet.containsCachedCatalogue(path)).isTrue();
    assertThat(ThreddsWmsServlet.getCacheLoads()).isEqualTo(loads + 1);
  }

  private void getCapabilities(String path) throws ServletException, IOException {
    getCapabilities(path, HttpServletResponse.SC_OK);
  }

  private void getCapabilities(String path, int expectedResponseCode) throws ServletException, IOException {
    final String uri = "/thredds/wms/" + path;
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
    request.setParameter("service", "WMS");
    request.setParameter("version", "1.3.0");
    request.setParameter("request", "GetCapabilities");
    request.setPathInfo(path);
    final MockHttpServletResponse response = new MockHttpServletResponse();

    threddsWmsServlet.service(request, response);
    assertThat(response.getStatus()).isEqualTo(expectedResponseCode);
  }
}
