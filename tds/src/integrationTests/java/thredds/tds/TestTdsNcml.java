/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.tds;

import static com.google.common.truth.Truth.assertThat;

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
import ucar.nc2.dataset.VariableDS;
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
    assertThat(ds).isNotNull();
    assertThat(ds.getFeatureType()).isEqualTo(FeatureType.GRID);

    // ncml should not be sent to the client
    assertThat(ds.getNcmlElement()).isNull();

    final DataFactory fac = new DataFactory();
    final Formatter log = new Formatter();

    try (NetcdfDataset ncd = fac.openDataset(ds, false, null, log)) {
      assertThat(ncd).isNotNull();

      // LOOK - no way to open single dataset in NcML with addRecords="true" in new API
      // Variable v = ncd.findVariable("record");
      // assert v != null;

      assertThat(ncd.getRootGroup().findAttributeString("name", "")).isEqualTo("value");

      assertThat((Object) ncd.findVariable("Temperature")).isNotNull();
      assertThat((Object) ncd.findVariable("T")).isNull();

      final Variable reletiveHumidity = ncd.findVariable("ReletiveHumidity");
      assertThat((Object) reletiveHumidity).isNotNull();
      assertThat((Object) reletiveHumidity).isInstanceOf(VariableDS.class);
      assertThat(reletiveHumidity.findAttributeString("long_name", null)).isEqualTo("relatively humid");
      assertThat(reletiveHumidity.findAttribute("description")).isNull();

      final Variable lat = ncd.findVariable("lat");
      assertThat((Object) lat).isNotNull();
      assertThat((Object) lat).isInstanceOf(VariableDS.class);
    }
  }

  @Test
  public void testNcmlInDatasetScan() throws IOException {
    final Catalog cat = TdsLocalCatalog.openDefaultCatalog();

    final Dataset catref = cat.findDatasetByID("ModifyDatasetScan");
    assertThat(catref).isNotNull();
    catref.getDatasetsLogical(); // reads in the referenced catalog
    final Dataset ds = catref.findDatasetByName("example1.nc");
    assertThat(ds).isNotNull();

    assertThat(ds.getFeatureType()).isEqualTo(FeatureType.GRID);

    // ncml should not be sent to the client
    assertThat(ds.getNcmlElement()).isNull();

    final DataFactory fac = new DataFactory();
    final Formatter log = new Formatter();

    try (NetcdfDataset ncd = fac.openDataset(ds, false, null, log)) {
      assertThat(ncd).isNotNull();

      final Variable record = ncd.findVariable("record");
      assertThat((Object) record).isNotNull();

      assertThat(ncd.getRootGroup().findAttributeString("name", "")).isEqualTo("value");

      assertThat((Object) ncd.findVariable("Temperature")).isNotNull();
      assertThat((Object) ncd.findVariable("T")).isNull();

      final Variable reletiveHumidity = ncd.findVariable("ReletiveHumidity");
      assertThat((Object) reletiveHumidity).isNotNull();
      assertThat((Object) reletiveHumidity).isInstanceOf(VariableDS.class);
      final Attribute att = reletiveHumidity.findAttribute("long_name");
      assertThat(att).isNotNull();
      assertThat(att.getStringValue()).isEqualTo("relatively humid");

      final Variable lat = ncd.findVariable("lat");
      assertThat((Object) lat).isNotNull();
      assertThat((Object) lat).isInstanceOf(VariableDS.class);
    }
  }

  @Test
  public void testAddMetadataToScan() throws IOException {
    final String endpoint = TestOnLocalServer.withHttpPath("cdmremote/testGridScan/GFS_CONUS_80km_20120229_1200.grib1");
    logger.debug("{}", endpoint);

    try (NetcdfFile ncd = NetcdfDatasets.openFile(endpoint, null)) {
      assertThat(ncd).isNotNull();

      final Attribute att = ncd.findGlobalAttribute("ncmlAdded");
      assertThat(att).isNotNull();
      assertThat(att.getStringValue()).isEqualTo("stuff");
    }
  }
}
