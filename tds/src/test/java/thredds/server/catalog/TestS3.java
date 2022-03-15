package thredds.server.catalog;

import org.junit.Assert;
import org.junit.Test;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import java.io.IOException;
import java.util.List;

public class TestS3 {
  private static final String CATALOG = "../tds/src/test/content/thredds/tds-s3.xml";

  @Test
  public void shouldCreateCatalogWithS3Data() throws IOException {
    final Catalog catalog = TestConfigCatalogBuilder.open("file:" + CATALOG);
    Assert.assertNotNull(catalog);

    final List<Dataset> datasets = catalog.getDatasetsLocal();
    Assert.assertFalse(datasets.isEmpty());

    final Dataset s3Dataset = catalog.findDatasetByName("S3 Dataset");
    Assert.assertEquals("s3-thredds-test-data/ncml/nc/namExtract/20060925_0600.nc", s3Dataset.getUrlPath());

    final Dataset aggregation = catalog.findDatasetByName("S3 Example NcML Aggregation");
    Assert.assertEquals("S3ExampleNcML/Agg.nc", aggregation.getUrlPath());

    final Dataset featureCollectionWithFolders = catalog.findDatasetByName("S3 Feature Collection With Folders");
    Assert.assertNotNull(featureCollectionWithFolders);

    final Dataset featureCollectionWithoutFolders = catalog.findDatasetByName("S3 Feature Collection Without Folders");
    Assert.assertNotNull(featureCollectionWithoutFolders);
  }
}
