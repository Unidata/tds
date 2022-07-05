/*
 * Copyright (c) 1998-2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.catalog;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.client.catalog.*;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.lang.invoke.MethodHandles;
import java.util.List;

@Category(NeedsCdmUnitTest.class)
public class TestTdsDatasetScan {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Test
  public void testSort() {
    Catalog cat = TdsLocalCatalog.open("catalog/scanCdmUnitTests/tds/ncep/catalog.xml");

    Dataset last = null;
    for (Dataset ds : cat.getDatasetsLocal()) {
      if (last != null)
        assertThat(ds.getName().compareTo(last.getName())).isGreaterThan(0);
      last = ds;
    }
  }

  @Test
  public void testDatasetScanForLatest() {
    Catalog parent = TdsLocalCatalog.open("catalog/testGridScan/catalog.xml");
    Service latestService = parent.findService("Resolver");
    assertThat(latestService).isNotNull();

    List<Dataset> topDatasets = parent.getDatasetsLocal();
    assertThat(topDatasets.size()).isEqualTo(1);
    Dataset topDataset = topDatasets.get(0);

    List<Dataset> dss = topDataset.getDatasetsLocal();
    assertThat(dss.size()).isGreaterThan(0);

    Dataset latestDataset = topDataset.findDatasetByName("latest.xml");
    assertThat(latestDataset).isNotNull();
    Access latestAccess = latestDataset.getAccess(ServiceType.Resolver);
    assertThat(latestAccess).isNotNull();
    assertThat(latestAccess.getService()).isEqualTo(latestService);
  }

  @Test
  public void testLatestResolver() {
    Catalog cat = TdsLocalCatalog.open("catalog/testGridScan/latest.xml");

    List<Dataset> dss = cat.getDatasetsLocal();
    assertThat(dss.size()).isEqualTo(1);

    Dataset ds = dss.get(0);
    assertThat(ds.hasAccess()).isTrue();
    assertThat(ds.getDatasetsLocal().size()).isEqualTo(0);

    assertThat(ds.getID()).isNotNull();
    assertThat(ds.getDataSize()).isGreaterThan(0);
    assertThat(ds.getID()).endsWith("GFS_CONUS_80km_20120229_1200.grib1");
  }

  @Test
  public void testHarvest() {
    Catalog cat = TdsLocalCatalog.open("catalog/testEnhanced/catalog.xml");
    assertThat(cat).isNotNull();
    List<Dataset> topList = cat.getDatasetsLocal();
    assertThat(topList.size()).isEqualTo(1);
    Dataset top = topList.get(0);
    assertThat(top).isNotNull();
    assertThat(top.isHarvest()).isTrue();

    List<Dataset> dss = top.getDatasetsLocal();
    assertThat(dss.size()).isGreaterThan(0);
    Dataset nested = dss.get(0);
    assertThat(nested.isHarvest()).isFalse();

    cat = TdsLocalCatalog.open("/catalog.xml");
    Dataset ds = cat.findDatasetByID("testDataset");
    assertThat(ds).isNotNull();
    assertThat(ds.isHarvest()).isFalse();
  }

  @Test
  public void testNestedDirs() {
    Catalog cat = TdsLocalCatalog.open("catalog/station/profiler/wind/06min/catalog.xml");

    List<Dataset> topList = cat.getDatasetsLocal();
    assertThat(topList.size()).isEqualTo(1);
    Dataset top = topList.get(0);
    assertThat(top).isNotNull();
    List<Dataset> children = top.getDatasetsLocal();
    assertThat(children.size()).isEqualTo(3); // latest + 2
  }

  /*
   * see http://www.freeformatter.com/url-encoder.html
   * 
   * Current State in 4,6:
   * 1) no encoding in the XML:
   * 
   * <dataset name="encoding" ID="scanCdmUnitTests/encoding">
   * <catalogRef xlink:href="d2.nc%3Bchunk%3D0/catalog.xml" xlink:title="d2.nc%3Bchunk%3D0"
   * ID="scanCdmUnitTests/encoding/d2.nc%3Bchunk%3D0" name=""/>
   * <catalogRef xlink:href="d2.nc;chunk=0/catalog.xml" xlink:title="d2.nc;chunk=0"
   * ID="scanCdmUnitTests/encoding/d2.nc;chunk=0" name=""/>
   * <catalogRef xlink:href="dir mit blank/catalog.xml" xlink:title="dir mit blank"
   * ID="scanCdmUnitTests/encoding/dir mit blank" name=""/>
   * </dataset>
   * 
   * 2) no url encoding in the HTML:
   * 
   * <a href='d2.nc%3Bchunk%3D0/catalog.html'><tt>d2.nc%3Bchunk%3D0/</tt></a></td>
   * <a href='d2.nc;chunk=0/catalog.html'><tt>d2.nc;chunk=0/</tt></a></td>
   * <a href='dir mit blank/catalog.xml'><tt>dir mit blank/</tt></a></td>
   * 
   * 3) drill further in
   * 3.1) encoding/d2.nc%3Bchunk%3D0/catalog.xml gets returned and unencoded to encoding/d2.nc;chunk=0/20070301.nc"
   * http://localhost:8081/thredds/dodsC/scanCdmUnitTests/encoding/d2.nc;chunk=0/20070301.nc.html fails (wrong
   * directory)
   * 
   * 3.2) http://localhost:8081/thredds/catalog/scanCdmUnitTests/encoding/d2.nc;chunk=0/catalog.xml does not get
   * urlencoded by browser
   * HEAD /thredds/catalog/scanCdmUnitTests/encoding/d2.nc;chunk=0/catalog.html
   * fails with 404
   * 
   * 3.3) dir mit blank/catalog.xml gets URLencoded by browser to dir%20mit%20blank/catalog.xml
   * all seems to work ok (with exception of the containing catalog)
   * notice that "dir mit blank/catalog.xml" ends in xml (!) : getting an exception in HtmlWriter
   * 
   */

  @Test
  public void testEncodingWithBlanks() {
    Catalog cat = TdsLocalCatalog.open("catalog/scanCdmUnitTests/encoding/catalog.xml");

    List<Dataset> ds = cat.getDatasetsLocal();
    assertThat(ds.size()).isEqualTo(1);
    Dataset top = ds.get(0);

    List<Dataset> children = top.getDatasetsLocal();
    assertThat(children.size()).isEqualTo(3);
  }

  //////////////////////////////////////////////////////
  // catalog5


  @Test
  public void testGlobalServices() {
    String catalog = "/catalog/testStationScan.v5/catalog.xml"; // serviceName ="all" from root catalog
    Catalog cat = TdsLocalCatalog.open(catalog);

    Dataset top = cat.getDatasetsLocal().get(0);
    assertThat(top.hasAccess()).isFalse();
    Service orgServices = cat.findService("all");
    assertThat(orgServices).isNotNull();
    assertThat(orgServices.getType()).isEqualTo(ServiceType.Compound);
    assertThat(orgServices.getNestedServices()).isNotNull();
    assertThat(orgServices.getNestedServices().size()).isEqualTo(11);
    boolean hasFileServer = false;
    for (Service sn : orgServices.getNestedServices())
      if (ServiceType.HTTPServer == sn.getType())
        hasFileServer = true;
    assertThat(hasFileServer).isTrue();
  }

}
