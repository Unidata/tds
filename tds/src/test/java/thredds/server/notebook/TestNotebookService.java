package thredds.server.notebook;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import ucar.nc2.constants.FeatureType;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class TestNotebookService {

  @Test
  public void testNotebookRegistration()
      throws NotebookMetadata.InvalidJupyterNotebookException, FileNotFoundException, URISyntaxException {

    final String test_file = "src/test/data/testNotebookMetadata.json";
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
}
