package thredds.server.notebook;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.mock.web.MockHttpServletRequest;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import thredds.server.viewer.ViewerLinkProvider;
import thredds.util.StringValidateEncodeUtils;
import ucar.nc2.constants.FeatureType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class TestNotebookService {
  private final String test_file = "src/test/data/testNotebookMetadata.json";

  @Test
  public void testNotebookRegistration()
      throws NotebookMetadata.InvalidJupyterNotebookException, FileNotFoundException, URISyntaxException {

    NotebookMetadata nbData = new NotebookMetadata(new File(test_file));

    // dataset that matches by ID
    Map<String, Object> fldsWithId = new HashMap<>();
    fldsWithId.put(Dataset.Id, "matchById");
    Dataset matchById = new Dataset(null, "match by Id", fldsWithId, null, null);
    assertThat(nbData.isValidForDataset(matchById)).isTrue();

    // dataset that matches featureType
    Map<String, Object> fldsWithFeatureType = new HashMap<>();
    fldsWithFeatureType.put(Dataset.FeatureType, FeatureType.GRID.name());
    Dataset matchByFeatureType = new Dataset(null, "match by feature type", fldsWithFeatureType, null, null);
    assertThat(nbData.isValidForDataset(matchByFeatureType)).isTrue();

    // create dummy parent catalogs
    Catalog parentByName = new Catalog(new URI("/other/URI"), "Parent catalog by name", new HashMap<>(), null);
    Catalog parentByURI = new Catalog(new URI("/parent/catalog/by/URI"), "Other parent name", new HashMap<>(), null);

    // dataset that matches catalog by name
    Dataset matchByCatalogName = new Dataset(parentByName, "match by parent catalog", new HashMap<>(), null, null);
    assertThat(nbData.isValidForDataset(matchByCatalogName)).isTrue();

    // dataset that matches catalog by URL
    Dataset matchByCatalogURI = new Dataset(parentByURI, "match by parent catalog", new HashMap<>(), null, null);
    assertThat(nbData.isValidForDataset(matchByCatalogURI)).isTrue();

    // dataset that doesn't match any accept param
    Map<String, Object> fldsNoMatch = new HashMap<>();
    fldsWithId.put(Dataset.Id, "noMatchById");
    fldsNoMatch.put(Dataset.FeatureType, FeatureType.POINT.name());
    Catalog otherParent = new Catalog(new URI(""), "", new HashMap<>(), null);
    Dataset noMatch = new Dataset(otherParent, "Not a match", fldsNoMatch, null, null);
    assertThat(nbData.isValidForDataset(noMatch)).isFalse();
  }

  @Test
  public void testMatchByDatasetIdWithRegExp()
      throws NotebookMetadata.InvalidJupyterNotebookException, FileNotFoundException {
    NotebookMetadata nbData = new NotebookMetadata(new File(test_file));

    // dataset that matches by ID
    Map<String, Object> fldsWithId = new HashMap<>();
    fldsWithId.put(Dataset.Id, "matchByIdRegExp/foo");
    Dataset matchById = new Dataset(null, "match by Id with reg exp", fldsWithId, null, null);
    assertThat(nbData.isValidForDataset(matchById)).isTrue();

    // dataset that does not match
    Map<String, Object> fldsWithNotMatchingId = new HashMap<>();
    fldsWithNotMatchingId.put(Dataset.Id, "notMatching/matchByIdRegExp/foo");
    Dataset notAMatch = new Dataset(null, "not a match", fldsWithNotMatchingId, null, null);
    assertThat(nbData.isValidForDataset(notAMatch)).isFalse();
  }

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void testNotebookUrlEncode()
      throws URISyntaxException, NotebookMetadata.InvalidJupyterNotebookException, IOException {
    String parentUri = "/parent/catalog/by/URI";
    String catalogName = "my+catalog.xml";
    String catalogUri = "/thredds/catalog/" + catalogName;
    String fileName = "file+name.json";

    File tempFile = temporaryFolder.newFile(fileName);
    Files.copy(Paths.get(test_file), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    NotebookMetadata metadata = new NotebookMetadata(tempFile);

    Map<String, Object> fldsWithId = new HashMap<>();
    fldsWithId.put(Dataset.Id, "matchById");
    Catalog parentCatalog = new Catalog(new URI(catalogUri), "Other parent name", new HashMap<>(), null);
    Dataset dataset = new Dataset(parentCatalog, "match by parent catalog", fldsWithId, null, null);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", catalogUri);

    JupyterNotebookViewerService.JupyterNotebookViewer viewer =
        new JupyterNotebookViewerService.JupyterNotebookViewer(metadata, parentUri);
    ViewerLinkProvider.ViewerLink link = viewer.getViewerLink(dataset, request);
    String decodedUrl = URLDecoder.decode(link.getUrl(), StringValidateEncodeUtils.CHARACTER_ENCODING_UTF_8);

    assertThat(decodedUrl).contains("filename=" + fileName);
    assertThat(decodedUrl).contains("catalog=" + catalogName);

  }
}
