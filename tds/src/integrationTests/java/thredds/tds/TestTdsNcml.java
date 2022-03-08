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
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.write.Ncdump;
import ucar.unidata.util.test.Assert2;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Formatter;

@Category(NeedsCdmUnitTest.class)
public class TestTdsNcml {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Test
  public void testNcmlInDataset() throws IOException {
    Catalog cat = TdsLocalCatalog.openDefaultCatalog();

    Dataset ds = cat.findDatasetByID("ExampleNcMLModified");
    Assert.assertNotNull("cant find dataset 'ExampleNcMLModified'", ds);
    Assert.assertEquals(FeatureType.GRID, ds.getFeatureType());

    // ncml should not be sent to the client
    // assert null == ds.getNcmlElement();

    DataFactory fac = new DataFactory();
    Formatter log = new Formatter();

    try (NetcdfDataset ncd = fac.openDataset(ds, false, null, log)) {
      Assert.assertNotNull(log.toString(), ncd);

      // LOOK - no way to open single dataset in NcML with addRecords="true" in new API
      // Variable v = ncd.findVariable("record");
      // assert v != null;

      Assert.assertEquals("value", ncd.getRootGroup().findAttributeString("name", ""));

      Assert.assertNotNull(ncd.findVariable("Temperature"));
      Assert.assertNull(ncd.findVariable("T"));

      Variable v = ncd.findVariable("ReletiveHumidity");
      Assert.assertNotNull(v);
      Assert.assertEquals("relatively humid", v.findAttributeString("long_name", null));
      Assert.assertNull(v.findAttribute("description"));
    }
  }

  @Test
  public void testNcmlInDatasetScan() throws IOException {
    Catalog cat = TdsLocalCatalog.openDefaultCatalog();

    Dataset catref = cat.findDatasetByID("ModifyDatasetScan");
    Assert.assertNotNull("cant find dataset by id 'ModifyDatasetScan'", catref);
    catref.getDatasetsLogical(); // reads in the referenced catalog
    Dataset ds = catref.findDatasetByName("example1.nc");
    Assert.assertNotNull("cant find dataset by name 'example1'", ds);

    Assert.assertEquals(FeatureType.GRID, ds.getFeatureType());

    // ncml should not be sent to the client
    Assert.assertNull(ds.getNcmlElement());

    DataFactory fac = new DataFactory();
    Formatter log = new Formatter();

    try (NetcdfDataset ncd = fac.openDataset(ds, false, null, log)) {
      Assert.assertNotNull(log.toString(), ncd);

      Variable v = ncd.findVariable("record");
      Assert.assertNotNull(v);

      Assert.assertEquals("value", ncd.getRootGroup().findAttributeString("name", ""));

      Assert.assertNotNull(ncd.findVariable("Temperature"));
      Assert.assertNull(ncd.findVariable("T"));

      v = ncd.findVariable("ReletiveHumidity");
      Assert.assertNotNull(v);
      Attribute att = v.findAttribute("long_name");
      Assert.assertNotNull(att);
      Assert.assertEquals("relatively humid", att.getStringValue());
    }
  }

  @Test
  public void testAggExisting() throws IOException, InvalidRangeException {
    String endpoint = TestOnLocalServer.withHttpPath("dodsC/ExampleNcML/Agg.nc");
    logger.debug("{}", endpoint);

    try (NetcdfFile ncfile = NetcdfDatasets.openFile(endpoint, null)) {
      Variable v = ncfile.findVariable("time");
      Assert.assertNotNull(v);
      Assert.assertEquals(DataType.DOUBLE, v.getDataType());

      String units = v.getUnitsString();
      Assert.assertNotNull(units);
      Assert.assertEquals("Hour since 2006-09-25T06:00:00Z", units);

      int count = 0;
      Array data = v.read();
      logger.debug(Ncdump.printArray(data, "time", null));

      while (data.hasNext()) {
        Assert2.assertNearlyEquals(data.nextInt(), (count + 1) * 3);
        count++;
      }

      // test attributes added in NcML
      String testAtt = ncfile.getRootGroup().findAttributeString("ncmlAdded", null);
      Assert.assertNotNull(testAtt);
      Assert.assertEquals("stuff", testAtt);

      v = ncfile.findVariable("lat");
      Assert.assertNotNull(v);
      testAtt = v.findAttributeString("ncmlAdded", null);
      Assert.assertNotNull(testAtt);
      Assert.assertEquals("lat_stuff", testAtt);
    }
  }

  @Test
  public void testAddMetadataToScan() throws IOException, InvalidRangeException {
    String endpoint = TestOnLocalServer.withHttpPath("cdmremote/testGridScan/GFS_CONUS_80km_20120229_1200.grib1");
    logger.debug("{}", endpoint);

    try (NetcdfFile ncd = NetcdfDatasets.openFile(endpoint, null)) {
      Assert.assertNotNull(ncd);

      Attribute att = ncd.findGlobalAttribute("ncmlAdded");
      Assert.assertNotNull(att);
      Assert.assertEquals("stuff", att.getStringValue());
    }
  }
}
