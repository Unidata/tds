package thredds.server.notebook;

import static com.google.common.truth.Truth.assertThat;

import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

public class TestNotebookServices {
  private static final String defaultViewer =
      "{\"filename\":\"default_viewer.ipynb\",\"description\":\"The TDS default viewer attempts to plot any Variable contained in the Dataset.\"}";
  private static final String testViewer =
      "{\"filename\":\"testNotebookMetadata.ipynb\",\"description\":\"Test notebook viewer accepts\"}";

  private static final String onlyDefaultViewer = "[" + defaultViewer + "]";
  private static final String defaultAndTestViewer = "[" + defaultViewer + "," + testViewer + "]";
  private static final String testAndDefaultViewer = "[" + testViewer + "," + defaultViewer + "]";

  private static final String topCatalog = "?catalog=catalog.xml";
  private static final String enhancedCatalog = "?catalog=testEnhanced/catalog.xml";

  @Test
  public void shouldHaveDefaultNotebook() {
    final String path = "/notebook/testDataset" + topCatalog;
    final String response = getStringContent(path);
    assertThat(response).isEqualTo(onlyDefaultViewer);
  }

  @Test
  public void shouldReturnNotFound() {
    final String path = "/notebook/notADatasetId" + topCatalog;
    final String endpoint = TestOnLocalServer.withHttpPath(path);
    TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void shouldMatchByDatasetId() {
    final String path = "/notebook/testGAP" + topCatalog;
    final String response = getStringContent(path);
    assertThat(response).isAnyOf(defaultAndTestViewer, testAndDefaultViewer);
  }

  @Test
  public void shouldMatchDatasetScanByDatasetIdRegExp() {
    final String path = "/notebook/testEnhanced/2004050412_eta_211.nc" + enhancedCatalog;
    final String response = getStringContent(path);
    assertThat(response).isAnyOf(defaultAndTestViewer, testAndDefaultViewer);
  }

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void shouldMatchFeatureCollectionDatasetByDatasetIdRegExp() {
    final String gribPath = "gribCollection/GFS_CONUS_80km/GFS_CONUS_80km_20120227_0000.grib1";
    final String gribCatalog = "?catalog=" + gribPath + "/catalog.xml";
    final String path = "/notebook/" + gribPath;
    final String response = getStringContent(path + gribCatalog);
    assertThat(response).isAnyOf(defaultAndTestViewer, testAndDefaultViewer);
  }

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void shouldMatchFeatureCollectionBestByDatasetIdRegExp() {
    final String gribPath = "gribCollection/GFS_CONUS_80km/";
    final String gribCatalog = "?catalog=" + gribPath + "catalog.xml";
    final String path = "/notebook/" + gribPath + "Best";
    final String response = getStringContent(path + gribCatalog);
    assertThat(response).isAnyOf(defaultAndTestViewer, testAndDefaultViewer);
  }

  private static String getStringContent(String path) {
    final String endpoint = TestOnLocalServer.withHttpPath(path);
    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.json);
    return new String(content, StandardCharsets.UTF_8);
  }
}
