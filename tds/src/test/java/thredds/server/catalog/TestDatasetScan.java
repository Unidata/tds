/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.catalog;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import thredds.client.catalog.Service;
import thredds.client.catalog.tools.CatalogXmlWriter;
import thredds.core.StandardService;
import ucar.nc2.time.CalendarDateFormatter;
import ucar.nc2.units.DateRange;
import ucar.nc2.units.TimeDuration;
import ucar.nc2.util.AliasTranslator;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import ucar.unidata.util.test.TestDir;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.util.List;

/**
 * Test DatasetScan
 *
 * @author caron
 * @since 1/21/2015
 *
 *        Execution notes:
 *        If you plan to run this under Intellij IDE,
 *        you will need to modify the 'Before Launch' window
 *        in the Edit Configuration window and add the following
 *        two gradle tasks in the thredds:tds project
 *        1. processResources
 *        2. processTestResources
 *        For both of them, you will need to ensure that the following
 *        VM arguments are defined.
 *        1. -Dunidata.testdata.path=...
 *        2. -Dtds.content.root.path=.../tds/src/test/content
 */

@Category(NeedsCdmUnitTest.class)
public class TestDatasetScan {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final boolean showCats = true;

  @Before
  public void setup() {
    AliasTranslator.addAlias("${cdmUnitTest}", TestDir.cdmUnitTestDir);
    StandardService ss = StandardService.resolver;
    Service latest = new Service(ss.getType().toString(), ss.getBase(), ss.getType().toString(),
        ss.getType().getDescription(), null, null, null, ss.getType().getAccessType());
    StandardService ss2 = StandardService.httpServer;
    Service httpServer = new Service(ss2.getType().toString(), ss2.getBase(), ss2.getType().toString(),
        ss2.getType().getDescription(), null, null, null, ss2.getType().getAccessType());

    DatasetScan.setSpecialServices(latest, httpServer);
  }

  @Test
  public void testMakeCatalog() throws IOException {
    String filePath = "../tds/src/test/content/thredds/catalog.xml";
    ConfigCatalog cat = TestConfigCatalogBuilder.open("file:" + filePath);
    assertThat(cat).isNotNull();
    CatalogXmlWriter writer = new CatalogXmlWriter();

    List<DatasetRootConfig> roots = cat.getDatasetRoots();
    for (DatasetRootConfig root : roots)
      logger.debug("DatasetRoot {} -> {}", root.path, root.location);
    assertThat(roots.size()).isEqualTo(5);

    Dataset ds = cat.findDatasetByID("scanCdmUnitTests");
    assertThat(ds).isNotNull();
    assertThat(ds).isInstanceOf(DatasetScan.class);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertThat(serviceName).isEqualTo("all");
    assertThat(ds.hasProperty("DatasetScan")).isTrue();

    DatasetScanConfig config = dss.getConfig();
    logger.debug(config.toString());

    Catalog scanCat = dss.makeCatalogForDirectory("scanCdmUnitTests", cat.getBaseURI()).makeCatalog();
    assertThat(scanCat).isNotNull();
    logger.debug(writer.writeXML(scanCat));

    scanCat = dss.makeCatalogForDirectory("scanCdmUnitTests/ncss/test", cat.getBaseURI()).makeCatalog();
    logger.debug(writer.writeXML(scanCat));
  }

  @Test
  public void testReverseSort() throws IOException {
    ConfigCatalog cat = TestConfigCatalogBuilder.getFromResource("thredds/server/catalog/TestDatasetScan.xml");
    assertThat(cat).isNotNull();

    Dataset ds = cat.findDatasetByID("NWS/NPN/6min");
    assertThat(ds).isNotNull();
    assertThat(ds).isInstanceOf(DatasetScan.class);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertThat(serviceName).isEqualTo("all");

    DatasetScanConfig config = dss.getConfig();
    logger.debug(config.toString());

    Catalog scanCat = dss.makeCatalogForDirectory("station/profiler/wind/06min", cat.getBaseURI()).makeCatalog();
    assertThat(scanCat).isNotNull();

    CatalogXmlWriter writer = new CatalogXmlWriter();
    if (showCats)
      logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    Dataset root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(3);

    // directories get reverse sorted
    List<Dataset> list = root.getDatasets();
    String name0 = list.get(0).getName();
    String name1 = list.get(1).getName();
    assertThat(name0.compareTo(name1)).isGreaterThan(0);

    scanCat = dss.makeCatalogForDirectory("station/profiler/wind/06min/20131102", cat.getBaseURI()).makeCatalog();
    assertThat(scanCat).isNotNull();
    if (showCats)
      logger.debug(writer.writeXML(scanCat));

    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(3);

    // files get reverse sorted
    list = root.getDatasets();
    name0 = list.get(0).getName();
    name1 = list.get(1).getName();
    assertThat(name0.compareTo(name1)).isGreaterThan(0);
  }

  @Test
  public void testTimeCoverage() throws IOException, ParseException {
    ConfigCatalog cat = TestConfigCatalogBuilder.getFromResource("thredds/server/catalog/TestDatasetScan.xml");
    assertThat(cat).isNotNull();

    Dataset ds = cat.findDatasetByID("NWS/NPN/6min");
    assertThat(ds).isNotNull();
    assertThat(ds).isInstanceOf(DatasetScan.class);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertThat(serviceName).isEqualTo("all");

    DatasetScanConfig config = dss.getConfig();
    logger.debug(config.toString());

    Catalog scanCat =
        dss.makeCatalogForDirectory("station/profiler/wind/06min/20131102", cat.getBaseURI()).makeCatalog();
    assertThat(scanCat).isNotNull();

    CatalogXmlWriter writer = new CatalogXmlWriter();
    if (showCats)
      logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    Dataset root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(3);

    List<Dataset> list = root.getDatasets();
    Dataset ds0 = list.get(1); // first one is latest
    Dataset ds1 = list.get(2);

    DateRange dr0 = ds0.getTimeCoverage();
    assertThat(dr0).isNotNull();
    assertThat(dr0.getStart().getCalendarDate())
        .isEqualTo(CalendarDateFormatter.isoStringToCalendarDate(null, "2013-11-02T23:54:00"));
    assertThat(dr0.getDuration()).isEqualTo(new TimeDuration("1 hour"));

    DateRange dr1 = ds1.getTimeCoverage();
    assertThat(dr1).isNotNull();
    assertThat(dr1.getStart().getCalendarDate())
        .isEqualTo(CalendarDateFormatter.isoStringToCalendarDate(null, "2013-11-02T23:48:00"));
    assertThat(dr1.getDuration()).isEqualTo(new TimeDuration("1 hour"));
  }

  @Test
  public void testLatest() throws IOException {
    ConfigCatalog cat = TestConfigCatalogBuilder.getFromResource("thredds/server/catalog/TestDatasetScan.xml");
    assertThat(cat).isNotNull();

    Dataset ds = cat.findDatasetByID("NWS/NPN/6min");
    assertThat(ds).isNotNull();
    assertThat(ds).isInstanceOf(DatasetScan.class);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertThat(serviceName).isEqualTo("all");

    DatasetScanConfig config = dss.getConfig();
    logger.debug(config.toString());

    Catalog scanCat =
        dss.makeCatalogForDirectory("station/profiler/wind/06min/20131102", cat.getBaseURI()).makeCatalog();
    assertThat(scanCat).isNotNull();

    CatalogXmlWriter writer = new CatalogXmlWriter();
    if (showCats)
      logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    Dataset root = scanCat.getDatasets().get(0);

    Service latestService = null;
    for (Service s : scanCat.getServices()) {
      if (s.getName().equalsIgnoreCase("Resolver"))
        latestService = s;
    }
    assertThat(latestService).isNotNull();

    Dataset latestDataset = null;
    for (Dataset nds : root.getDatasets()) {
      Service s = nds.getServiceDefault();
      assertThat(s).isNotNull();
      if (s.equals(latestService))
        latestDataset = nds;
    }
    assertThat(latestDataset).isNotNull();
  }

  @Test
  public void testEsgfProblems() throws IOException {
    String filePath = "../tds/src/test/content/thredds/testEsgfProblems.xml";
    ConfigCatalog cat = TestConfigCatalogBuilder.open("file:" + filePath);
    assertThat(cat).isNotNull();

    Dataset ds = cat.findDatasetByID("gass-ytoc-mip");
    assertThat(ds).isNotNull();
    assertThat(ds).isInstanceOf(DatasetScan.class);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertThat(serviceName).isEqualTo("fileservice");

    DatasetScanConfig config = dss.getConfig();
    logger.debug(config.toString());

    Catalog scanCat = dss.makeCatalogForDirectory("gass-ytoc-mip", cat.getBaseURI()).makeCatalog();
    assertThat(scanCat).isNotNull();

    CatalogXmlWriter writer = new CatalogXmlWriter();
    if (showCats)
      logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    Dataset root = scanCat.getDatasets().get(0);
    String sn = root.getServiceNameDefault();
    assertThat(sn).isNotNull();
    assertThat(sn).isEqualTo("fileservice");

    for (Dataset nds : root.getDatasets()) {
      if (nds.getName().equals("latest.xml"))
        continue;
      Service s = nds.getServiceDefault();
      assertThat(s).isNotNull();
      assertThat(s.getName()).isEqualTo("fileservice");
    }
  }
}
