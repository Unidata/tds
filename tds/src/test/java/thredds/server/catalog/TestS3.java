package thredds.server.catalog;

import org.junit.Assert;
import org.junit.Test;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import java.io.IOException;
import java.util.List;

public class TestS3 {
  private static final String CATALOG = "../tds/src/test/content/thredds/catalogS3.xml";

  @Test
  public void shouldCreateCatalogWithS3Data() throws IOException {
    final Catalog catalog = TestConfigCatalogBuilder.open("file:" + CATALOG);
    Assert.assertNotNull(catalog);

    final List<Dataset> datasets = catalog.getDatasetsLocal();
    Assert.assertEquals(3, datasets.size());

    final Dataset s3Dataset = datasets.get(0);
    Assert.assertEquals("S3 Dataset", s3Dataset.getName());
    Assert.assertEquals("s3-thredds-test-data/ncml/nc/namExtract/20060925_0600.nc", s3Dataset.getUrlPath());

    final Dataset aggregation = datasets.get(1);
    Assert.assertEquals("S3 Example NcML Aggregation", aggregation.getName());
    Assert.assertEquals("S3ExampleNcML/Agg.nc", aggregation.getUrlPath());

    final Dataset featureCollection = datasets.get(2);
    Assert.assertEquals("S3 Feature Collection", featureCollection.getName());
  }
}
