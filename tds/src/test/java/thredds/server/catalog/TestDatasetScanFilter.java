/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.catalog;

import static com.google.common.truth.Truth.assertThat;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import thredds.client.catalog.Service;
import thredds.client.catalog.builder.CatalogBuilder;
import thredds.client.catalog.tools.CatalogXmlWriter;
import thredds.core.StandardService;
import thredds.inventory.MFile;
import ucar.nc2.util.AliasTranslator;
import ucar.unidata.util.test.TestDir;
import ucar.unidata.util.test.TestFileDirUtils;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDatasetScanFilter {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @ClassRule
  public static final TemporaryFolder tempFolder = new TemporaryFolder();

  @BeforeClass
  public static void setupTestDataDir() throws IOException {
    File tmpTestDataDir = tempFolder.newFolder();
    logger.debug("tmpLocalRootDataDir = {}", tmpTestDataDir);

    AliasTranslator.addAlias("${tmpDir}", tmpTestDataDir.getPath());
    AliasTranslator.addAlias("${cdmUnitTest}", TestDir.cdmUnitTestDir);

    File tmpTestDir = TestFileDirUtils.addDirectory(tmpTestDataDir, "testDatafilesInDateTimeNestedDirs");
    assertThat(tmpTestDir).isNotNull();
    assertThat(tmpTestDir.exists()).isTrue();
    assertThat(tmpTestDir.canRead()).isTrue();
    assertThat(tmpTestDir.canWrite()).isTrue();

    File profilesDir = TestFileDirUtils.addDirectory(tmpTestDir, "profiles");
    File firstDayDir = TestFileDirUtils.addDirectory(profilesDir, "20131106");

    TestFileDirUtils.addFile(firstDayDir, "PROFILER_wind_06min_20131106_2341.nc");
    TestFileDirUtils.addFile(firstDayDir, "PROFILER_wind_06min_20131106_2348.nc");
    TestFileDirUtils.addFile(firstDayDir, "PROFILER_wind_06min_20131106_2354.nc");

    File secondDayDir = TestFileDirUtils.addDirectory(profilesDir, "20131107");
    TestFileDirUtils.addFile(secondDayDir, "PROFILER_wind_06min_20131107_0001.nc");
    TestFileDirUtils.addFile(secondDayDir, "PROFILER_wind_06min_20131107_0008.nc");
    TestFileDirUtils.addFile(secondDayDir, "PROFILER_wind_06min_20131107_0014.nc");
    TestFileDirUtils.addFile(secondDayDir, "PROFILER_wind_06min_20131108_0016.nc");

    StandardService ss = StandardService.resolver;
    Service latest = new Service(ss.getType().toString(), ss.getBase(), ss.getType().toString(),
        ss.getType().getDescription(), null, null, null, ss.getType().getAccessType());
    StandardService ss2 = StandardService.httpServer;
    Service httpServer = new Service(ss2.getType().toString(), ss2.getBase(), ss2.getType().toString(),
        ss.getType().getDescription(), null, null, null, ss.getType().getAccessType());

    DatasetScan.setSpecialServices(latest, httpServer);
  }

  /*
   * public void createEtaDirWithCvsAndDotGitDirs( File targetDir) {
   * 
   * tmpTestDataCrDs = createMFile( targetDir.getPath(), targetDir.getName());
   * 
   * List<String> dirNamesToIgnore = new ArrayList<String>();
   * dirNamesToIgnore.add("CVS");
   * dirNamesToIgnore.add(".git");
   * 
   * List<String> dataFileNames = new ArrayList<String>();
   * dataFileNames.add("2004050300_eta_211.nc");
   * dataFileNames.add("2004050312_eta_211.nc");
   * dataFileNames.add("2004050400_eta_211.nc");
   * dataFileNames.add("2004050412_eta_211.nc");
   * 
   * for ( String dirName : dirNamesToIgnore )
   * TestFileDirUtils.addDirectory( targetDir, dirName);
   * 
   * for ( String fileName : dataFileNames )
   * TestFileDirUtils.addFile( targetDir, fileName );
   * 
   * allFiles_FullPathNames = new ArrayList<String>();
   * dataFiles_FullPathNames = new ArrayList<String>();
   * 
   * for ( String fileName : dirNamesToIgnore )
   * allFiles_FullPathNames.add( String.format( "%s/%s", tmpTestDataCrDs.getPath(), fileName));
   * 
   * for ( String fileName : dataFileNames ) {
   * String path = String.format("%s/%s", tmpTestDataCrDs.getPath(), fileName);
   * allFiles_FullPathNames.add( path);
   * dataFiles_FullPathNames.add( path);
   * }
   * 
   * }
   */

  @Test
  public void testWildcardFilter() throws IOException {
    ConfigCatalog cat = TestConfigCatalogBuilder.getFromResource("thredds/server/catalog/TestDatasetScan.xml");
    assertThat(cat).isNotNull();

    Dataset ds = cat.findDatasetByID("testGridScan");
    assertThat(ds).isNotNull();
    assertThat(ds).isInstanceOf(DatasetScan.class);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertThat(serviceName).isEqualTo("all");

    DatasetScanConfig config = dss.getConfig();
    logger.debug(config.toString());

    Catalog scanCat = dss.makeCatalogForDirectory("testGridScan", cat.getBaseURI()).makeCatalog();
    assertThat(scanCat).isNotNull();

    CatalogXmlWriter writer = new CatalogXmlWriter();
    logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    Dataset root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(1);

    scanCat = dss.makeCatalogForDirectory("testGridScan/testDatafilesInDateTimeNestedDirs/profiles", cat.getBaseURI())
        .makeCatalog();
    logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(2);

    scanCat = dss
        .makeCatalogForDirectory("testGridScan/testDatafilesInDateTimeNestedDirs/profiles/20131106", cat.getBaseURI())
        .makeCatalog();
    logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(3);

    scanCat = dss
        .makeCatalogForDirectory("testGridScan/testDatafilesInDateTimeNestedDirs/profiles/20131107", cat.getBaseURI())
        .makeCatalog();
    logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(4);
  }

  @Test
  public void testRegexpFilter() throws IOException {
    ConfigCatalog cat = TestConfigCatalogBuilder.getFromResource("thredds/server/catalog/TestDatasetScan.xml");
    assertThat(cat).isNotNull();

    Dataset ds = cat.findDatasetByID("testGridScanReg");
    assertThat(ds).isNotNull();
    assertThat(ds).isInstanceOf(DatasetScan.class);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertThat(serviceName).isEqualTo("all");

    Catalog scanCat = dss.makeCatalogForDirectory("testGridScanReg", cat.getBaseURI()).makeCatalog();
    assertThat(scanCat).isNotNull();

    CatalogXmlWriter writer = new CatalogXmlWriter();
    logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    Dataset root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(1);

    scanCat =
        dss.makeCatalogForDirectory("testGridScanReg/testDatafilesInDateTimeNestedDirs/profiles", cat.getBaseURI())
            .makeCatalog();
    logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(2);

    scanCat = dss.makeCatalogForDirectory("testGridScanReg/testDatafilesInDateTimeNestedDirs/profiles/20131106",
        cat.getBaseURI()).makeCatalog();
    logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(3);

    scanCat = dss.makeCatalogForDirectory("testGridScanReg/testDatafilesInDateTimeNestedDirs/profiles/20131107",
        cat.getBaseURI()).makeCatalog();
    logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(3);
  }

  @Test
  public void testExcludeDir() throws IOException {
    ConfigCatalog cat = TestConfigCatalogBuilder.getFromResource("thredds/server/catalog/TestDatasetScan.xml");
    assertThat(cat).isNotNull();
    CatalogXmlWriter writer = new CatalogXmlWriter();
    logger.debug(writer.writeXML(cat));

    Dataset ds = cat.findDatasetByID("testExclude");
    assertThat(ds).isNotNull();
    assertThat(ds).isInstanceOf(DatasetScan.class);
    DatasetScan dss = (DatasetScan) ds;
    String serviceName = dss.getServiceNameDefault();
    assertThat(serviceName).isEqualTo("all");

    Catalog scanCat = dss.makeCatalogForDirectory("testExclude", cat.getBaseURI()).makeCatalog();
    assertThat(scanCat).isNotNull();

    logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    Dataset root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(1);

    scanCat = dss.makeCatalogForDirectory("testExclude/testDatafilesInDateTimeNestedDirs/profiles", cat.getBaseURI())
        .makeCatalog();
    logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(1);

    scanCat =
        dss.makeCatalogForDirectory("testExclude/testDatafilesInDateTimeNestedDirs/profiles/20131106", cat.getBaseURI())
            .makeCatalog();
    logger.debug(writer.writeXML(scanCat));
    assertThat(scanCat.getDatasets().size()).isEqualTo(1);
    root = scanCat.getDatasets().get(0);
    assertThat(root.getDatasets().size()).isEqualTo(3);
  }

  @Test
  public void testExcludeDirFails() throws IOException {
    ConfigCatalog cat = TestConfigCatalogBuilder.getFromResource("thredds/server/catalog/TestDatasetScan.xml");;
    assertThat(cat).isNotNull();

    Dataset ds = cat.findDatasetByID("testExclude");
    assertThat(ds).isNotNull();
    assertThat(ds).isInstanceOf(DatasetScan.class);
    DatasetScan dss = (DatasetScan) ds;
    CatalogBuilder catb = dss.makeCatalogForDirectory(
        "testGridScanReg/testDatafilesInDateTimeNestedDirs/profiles/20131107", cat.getBaseURI());
    assertThat(catb).isNull();
  }

  @Test
  public void testRegexp() throws IOException {
    testOne("PROFILER_wind_06min_2013110[67]_[0-9]{4}.nc", "PROFILER_wind_06min_20131107_0001.nc", true);
    testOne("PROFILER_wind_06min_2013110[67]_[0-9]{4}\\.nc", "PROFILER_wind_06min_20131107_0001.nc", true);
    testOne("PROFILER_wind_06min_2013110[67]_[0-9]{4}\\\\.nc", "PROFILER_wind_06min_20131107_0001.nc", false);
  }

  public static void testOne(String ps, String match, boolean expect) {
    Pattern pattern = Pattern.compile(ps);
    Matcher matcher = pattern.matcher(match);
    assertThat(matcher.matches()).isEqualTo(expect);
  }
}
