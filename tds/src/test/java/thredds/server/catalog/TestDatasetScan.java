/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.catalog;

import org.junit.Assert;
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
import static org.junit.Assert.assertEquals;

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

  static private final boolean showCats = true;

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
    Assert.assertNotNull(cat);
    CatalogXmlWriter writer = new CatalogXmlWriter();

    List<DatasetRootConfig> roots = cat.getDatasetRoots();
    for (DatasetRootConfig root : roots)
      System.out.printf("DatasetRoot %s -> %s%n", root.path, root.location);
    assertEquals("Incorrect # of catalog roots", 5, roots.size());

    Dataset ds = cat.findDatasetByID("scanCdmUnitTests");
    Assert.assertNotNull("Null dataset", ds);
    Assert.assertTrue("dataset not DatasetScan", ds instanceof DatasetScan);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertEquals("Servicename default is not 'all'", "all", serviceName);
    Assert.assertTrue("Does not have DatasetScan property", ds.hasProperty("DatasetScan"));

    DatasetScanConfig config = dss.getConfig();
    System.out.printf("%s%n", config);

    Catalog scanCat = dss.makeCatalogForDirectory("scanCdmUnitTests", cat.getBaseURI()).makeCatalog();
    Assert.assertNotNull(scanCat);
    System.out.printf("%n%s%n", writer.writeXML(scanCat));

    scanCat = dss.makeCatalogForDirectory("scanCdmUnitTests/ncss/test", cat.getBaseURI()).makeCatalog();
    System.out.printf("%s%n", writer.writeXML(scanCat));
  }

  @Test
  public void testReverseSort() throws IOException {
    ConfigCatalog cat = TestConfigCatalogBuilder.getFromResource("thredds/server/catalog/TestDatasetScan.xml");
    Assert.assertNotNull(cat);

    Dataset ds = cat.findDatasetByID("NWS/NPN/6min");
    Assert.assertNotNull(ds);
    Assert.assertTrue(ds instanceof DatasetScan);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertEquals("all", serviceName);

    DatasetScanConfig config = dss.getConfig();
    System.out.printf("%s%n", config);

    Catalog scanCat = dss.makeCatalogForDirectory("station/profiler/wind/06min", cat.getBaseURI()).makeCatalog();
    Assert.assertNotNull(scanCat);

    CatalogXmlWriter writer = new CatalogXmlWriter();
    if (showCats)
      System.out.printf("%n%s%n", writer.writeXML(scanCat));
    assertEquals(1, scanCat.getDatasets().size());
    Dataset root = scanCat.getDatasets().get(0);
    assertEquals(3, root.getDatasets().size());

    // directories get reverse sorted
    List<Dataset> list = root.getDatasets();
    String name0 = list.get(0).getName();
    String name1 = list.get(1).getName();
    Assert.assertTrue(name0.compareTo(name1) > 0);

    scanCat = dss.makeCatalogForDirectory("station/profiler/wind/06min/20131102", cat.getBaseURI()).makeCatalog();
    Assert.assertNotNull(scanCat);
    if (showCats)
      System.out.printf("%n%s%n", writer.writeXML(scanCat));

    assertEquals(1, scanCat.getDatasets().size());
    root = scanCat.getDatasets().get(0);
    assertEquals(3, root.getDatasets().size());

    // files get reverse sorted
    list = root.getDatasets();
    name0 = list.get(0).getName();
    name1 = list.get(1).getName();
    Assert.assertTrue(name0.compareTo(name1) > 0);
  }

  @Test
  public void testTimeCoverage() throws IOException, ParseException {
    ConfigCatalog cat = TestConfigCatalogBuilder.getFromResource("thredds/server/catalog/TestDatasetScan.xml");
    Assert.assertNotNull(cat);

    Dataset ds = cat.findDatasetByID("NWS/NPN/6min");
    Assert.assertNotNull(ds);
    Assert.assertTrue(ds instanceof DatasetScan);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertEquals("all", serviceName);

    DatasetScanConfig config = dss.getConfig();
    System.out.printf("%s%n", config);

    Catalog scanCat =
        dss.makeCatalogForDirectory("station/profiler/wind/06min/20131102", cat.getBaseURI()).makeCatalog();
    Assert.assertNotNull(scanCat);

    CatalogXmlWriter writer = new CatalogXmlWriter();
    if (showCats)
      System.out.printf("%n%s%n", writer.writeXML(scanCat));
    assertEquals(1, scanCat.getDatasets().size());
    Dataset root = scanCat.getDatasets().get(0);
    assertEquals(3, root.getDatasets().size());

    List<Dataset> list = root.getDatasets();
    Dataset ds0 = list.get(1); // first one is latest
    Dataset ds1 = list.get(2);

    DateRange dr0 = ds0.getTimeCoverage();
    Assert.assertNotNull(dr0);
    assertEquals(CalendarDateFormatter.isoStringToCalendarDate(null, "2013-11-02T23:54:00"),
        dr0.getStart().getCalendarDate());
    assertEquals(new TimeDuration("1 hour"), dr0.getDuration());

    DateRange dr1 = ds1.getTimeCoverage();
    Assert.assertNotNull(dr1);
    assertEquals(CalendarDateFormatter.isoStringToCalendarDate(null, "2013-11-02T23:48:00"),
        dr1.getStart().getCalendarDate());
    assertEquals(new TimeDuration("1 hour"), dr1.getDuration());
  }

  @Test
  public void testLatest() throws IOException {
    ConfigCatalog cat = TestConfigCatalogBuilder.getFromResource("thredds/server/catalog/TestDatasetScan.xml");
    Assert.assertNotNull(cat);

    Dataset ds = cat.findDatasetByID("NWS/NPN/6min");
    Assert.assertNotNull(ds);
    Assert.assertTrue(ds instanceof DatasetScan);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertEquals("all", serviceName);

    DatasetScanConfig config = dss.getConfig();
    System.out.printf("%s%n", config);

    Catalog scanCat =
        dss.makeCatalogForDirectory("station/profiler/wind/06min/20131102", cat.getBaseURI()).makeCatalog();
    Assert.assertNotNull(scanCat);

    CatalogXmlWriter writer = new CatalogXmlWriter();
    if (showCats)
      System.out.printf("%n%s%n", writer.writeXML(scanCat));
    assertEquals(1, scanCat.getDatasets().size());
    Dataset root = scanCat.getDatasets().get(0);

    Service latestService = null;
    for (Service s : scanCat.getServices()) {
      if (s.getName().equalsIgnoreCase("Resolver"))
        latestService = s;
    }
    Assert.assertNotNull(latestService);

    Dataset latestDataset = null;
    for (Dataset nds : root.getDatasets()) {
      Service s = nds.getServiceDefault();
      Assert.assertNotNull(s);
      if (s.equals(latestService))
        latestDataset = nds;
    }
    Assert.assertNotNull(latestDataset);
  }

  @Test
  public void testEsgfProblems() throws IOException {
    String filePath = "../tds/src/test/content/thredds/testEsgfProblems.xml";
    ConfigCatalog cat = TestConfigCatalogBuilder.open("file:" + filePath);
    Assert.assertNotNull(cat);

    Dataset ds = cat.findDatasetByID("gass-ytoc-mip");
    Assert.assertNotNull(ds);
    Assert.assertTrue(ds instanceof DatasetScan);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertEquals("fileservice", serviceName);

    DatasetScanConfig config = dss.getConfig();
    System.out.printf("%s%n", config);

    Catalog scanCat = dss.makeCatalogForDirectory("gass-ytoc-mip", cat.getBaseURI()).makeCatalog();
    Assert.assertNotNull(scanCat);

    CatalogXmlWriter writer = new CatalogXmlWriter();
    if (showCats)
      System.out.printf("%n%s%n", writer.writeXML(scanCat));
    assertEquals(1, scanCat.getDatasets().size());
    Dataset root = scanCat.getDatasets().get(0);
    String sn = root.getServiceNameDefault();
    Assert.assertNotNull(sn);
    assertEquals("fileservice", sn);

    for (Dataset nds : root.getDatasets()) {
      if (nds.getName().equals("latest.xml"))
        continue;
      Service s = nds.getServiceDefault();
      Assert.assertNotNull(s);
      assertEquals("fileservice", s.getName());
    }
  }


}
