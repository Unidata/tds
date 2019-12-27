/* Copyright */
package thredds.server.catalog.tracker;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import thredds.client.catalog.builder.CatalogBuilder;
import thredds.client.catalog.tools.CatalogCrawler;
import thredds.server.catalog.ConfigCatalog;
import thredds.server.catalog.TestConfigCatalogBuilder;

/**
 * Test the ability of the chronicle based DatasetTracker to deal with "big" NcML.
 *
 * @author sarms
 * @since 12/27/2019
 */
public class TestChronicleTrackerBigNcml {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Path tdsTestDataDir = Paths.get("../tds/src/test/data").toAbsolutePath().normalize();
  private Path bigNcmlConfigCatalog = tdsTestDataDir.resolve("configCatalogs/bigNcml.xml");

  /**
   * Manage a standalone dataset tracker for testing purposes
   */
  final class TestDatasetTracker implements Closeable {

    private final Path tempDbPath;
    private final DatasetTrackerChronicle datasetTracker;

    public TestDatasetTracker() throws IOException {
      tempDbPath = Files.createTempDirectory("tdsTests");
      datasetTracker = new DatasetTrackerChronicle(tempDbPath.toString(), 10, 0);
    }

    public DatasetTrackerChronicle getDatasetTracker() {
      return datasetTracker;
    }

    @Override
    public void close() throws IOException {
      datasetTracker.close();
      FileUtils.deleteDirectory(tempDbPath.toFile());
    }
  }

  /**
   * Build a client catalog from a config catalog
   * 
   * @param configCatalog URI to an on disk config catalog
   * @return a client catalog {@link Catalog} wrapped in an {@link Optional}. The Optional may be empty.
   * @throws IOException
   */
  private Optional<Catalog> buildClientCatalog(URI configCatalog) throws IOException {
    ConfigCatalog configCat = TestConfigCatalogBuilder.open(configCatalog.toString());
    CatalogBuilder catalogBuilder = configCat.makeCatalogBuilder();
    Catalog clientCatalog = catalogBuilder.makeCatalog();
    return Optional.ofNullable(clientCatalog);
  }

  /**
   * crawl a client catalog to get its datasets into the dataset tracker
   * 
   * @param catalog client catalog
   * @param datasetTracker dataset Tracker to use
   * @throws IOException
   */
  private void crawl(Catalog catalog, DatasetTracker datasetTracker) throws IOException {
    CatalogCrawler crawler = new CatalogCrawler(CatalogCrawler.Type.all, -1, null, new CatalogCrawler.Listener() {
      public void getDataset(Dataset dd, Object context) {
        datasetTracker.trackDataset(0, dd, null);
      }
    }, null, null, null);
    crawler.crawl(catalog);
  }

  private Element getNcmlFromClientCatalog(Catalog clientCatalog) {
    Dataset ds = clientCatalog.findDatasetByID("bigNcmlTest1");
    return ds.getNcmlElement();
  }

  /**
   * Read in a config catalog with some "really big" ncml (thanks to lots of comments).
   * Make sure the NcML can be added to the chronicle database and restored properly.
   * In order for this to work, the NcML must striped of unnecessary whitespace and
   * comments must be removed.
   *
   *
   *
   * @throws IOException
   */
  @Test
  public void testBigNcML() throws IOException {

    try (TestDatasetTracker datasetTracker = new TestDatasetTracker()) {
      DatasetTrackerChronicle datasetTrackerDb = datasetTracker.getDatasetTracker();

      // generate client catalog from config catalog
      Optional<Catalog> clientCatalogOp = buildClientCatalog(bigNcmlConfigCatalog.toUri());
      Catalog clientCatalog = clientCatalogOp.orElseThrow(() -> new RuntimeException(
          "Config Catalog %s did not produce a client catalog.".format(bigNcmlConfigCatalog.toUri().toString())));

      // get ncml string as found in catalog
      Element ncml = getNcmlFromClientCatalog(clientCatalog);
      String ncmlFromCatalog = (new XMLOutputter()).outputString(ncml);

      // read the client catalog, and add to dataset tracker
      crawl(clientCatalog, datasetTrackerDb);

      // persist to disk then reopen
      datasetTrackerDb.save();

      // get ncml as restored from dataset tracker
      String ncmlFromDb = datasetTrackerDb.findNcml("ExampleNcML/bigNcml1.nc");

      // compare actual ncml from catalog and ncml from dataset tracker
      // the ncml from the catalog should have comments, etc. Should not match
      Assert.assertNotEquals(ncmlFromCatalog, ncmlFromDb);

      // make the NcML from the catalog compact
      String compactNcmlFromCatalog = DatasetTrackerChronicle.ncmlToCompactString(ncml);
      Assert.assertEquals(compactNcmlFromCatalog, ncmlFromDb);
    }
  }
}
