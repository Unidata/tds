/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.tds;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.test.util.TestOnLocalServer;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import thredds.client.catalog.tools.DataFactory;
import thredds.server.catalog.TdsLocalCatalog;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Formatter;

@Category(NeedsCdmUnitTest.class)
public class TestTdsNcml {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Test
  public void testNcmlInDataset() throws IOException {
    final Catalog cat = TdsLocalCatalog.openDefaultCatalog();

    final Dataset ds = cat.findDatasetByID("ExampleNcMLModified");
    Assert.assertNotNull("cant find dataset 'ExampleNcMLModified'", ds);
    Assert.assertEquals(FeatureType.GRID, ds.getFeatureType());

    // ncml should not be sent to the client
    // assert null == ds.getNcmlElement();

    final DataFactory fac = new DataFactory();
    final Formatter log = new Formatter();

    try (NetcdfDataset ncd = fac.openDataset(ds, false, null, log)) {
      Assert.assertNotNull(log.toString(), ncd);

      // LOOK - no way to open single dataset in NcML with addRecords="true" in new API
      // Variable v = ncd.findVariable("record");
      // assert v != null;

      Assert.assertEquals("value", ncd.getRootGroup().findAttributeString("name", ""));

      Assert.assertNotNull(ncd.findVariable("Temperature"));
      Assert.assertNull(ncd.findVariable("T"));

      final Variable reletiveHumidity = ncd.findVariable("ReletiveHumidity");
      Assert.assertNotNull(reletiveHumidity);
      Assert.assertEquals("relatively humid", reletiveHumidity.findAttributeString("long_name", null));
      Assert.assertNull(reletiveHumidity.findAttribute("description"));
    }
  }

  @Test
  public void testNcmlInDatasetScan() throws IOException {
    final Catalog cat = TdsLocalCatalog.openDefaultCatalog();

    final Dataset catref = cat.findDatasetByID("ModifyDatasetScan");
    Assert.assertNotNull("cant find dataset by id 'ModifyDatasetScan'", catref);
    catref.getDatasetsLogical(); // reads in the referenced catalog
    final Dataset ds = catref.findDatasetByName("example1.nc");
    Assert.assertNotNull("cant find dataset by name 'example1'", ds);

    Assert.assertEquals(FeatureType.GRID, ds.getFeatureType());

    // ncml should not be sent to the client
    Assert.assertNull(ds.getNcmlElement());

    final DataFactory fac = new DataFactory();
    final Formatter log = new Formatter();

    try (NetcdfDataset ncd = fac.openDataset(ds, false, null, log)) {
      Assert.assertNotNull(log.toString(), ncd);

      final Variable record = ncd.findVariable("record");
      Assert.assertNotNull(record);

      Assert.assertEquals("value", ncd.getRootGroup().findAttributeString("name", ""));

      Assert.assertNotNull(ncd.findVariable("Temperature"));
      Assert.assertNull(ncd.findVariable("T"));

      final Variable reletiveHumidity = ncd.findVariable("ReletiveHumidity");
      Assert.assertNotNull(reletiveHumidity);
      final Attribute att = reletiveHumidity.findAttribute("long_name");
      Assert.assertNotNull(att);
      Assert.assertEquals("relatively humid", att.getStringValue());
    }
  }

  @Test
  public void testAddMetadataToScan() throws IOException {
    final String endpoint = TestOnLocalServer.withHttpPath("cdmremote/testGridScan/GFS_CONUS_80km_20120229_1200.grib1");
    logger.debug("{}", endpoint);

    try (NetcdfFile ncd = NetcdfDatasets.openFile(endpoint, null)) {
      Assert.assertNotNull(ncd);

      final Attribute att = ncd.findGlobalAttribute("ncmlAdded");
      Assert.assertNotNull(att);
      Assert.assertEquals("stuff", att.getStringValue());
    }
  }
}
