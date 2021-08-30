/*
 * Copyright (c) 1998-2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.cdmr;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import thredds.client.catalog.tools.DataFactory;
import thredds.server.catalog.TdsLocalCatalog;
import ucar.ma2.DataType;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.util.Misc;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

@Category(NeedsCdmUnitTest.class)
public class TestCdmRemoteServer {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /*
   * Relies on:
   * <dataset name="Test Single Grid Dataset" ID="testSingleGridDataset" serviceName="all"
   * urlPath="cdmUnitTest/ncss/CONUS_80km_nc/GFS_CONUS_80km_20120419_0000.nc" dataType="Grid"/>
   * in tds/src/test/content/thredds/catalog.xml
   */
  @Test
  public void testSingleDataset() throws IOException {
    Catalog cat = TdsLocalCatalog.open(null);

    Dataset ds = cat.findDatasetByID("testSingleGridDataset");
    assert (ds != null) : "cant find dataset 'testSingleGridDataset'";
    assert ds.getFeatureType() == FeatureType.GRID;

    DataFactory fac = new DataFactory();
    try (DataFactory.Result dataResult = fac.openFeatureDataset(ds, null)) {
      if (dataResult.fatalError) {
        System.out.printf("fatalError= %s%n", dataResult.errLog);
        assert false;
      }
      assert dataResult.featureDataset != null;

      GridDataset gds = (GridDataset) dataResult.featureDataset;
      String gridName = "Pressure_reduced_to_MSL";
      VariableSimpleIF vs = gds.getDataVariable(gridName);
      Assert.assertNotNull(gridName, vs);

      GridDatatype grid = gds.findGridByShortName(gridName);
      Assert.assertNotNull(gridName, grid);

      GridCoordSystem gcs = grid.getCoordinateSystem();
      Assert.assertNotNull(gcs);

      assert null == gcs.getVerticalAxis();

      CoordinateAxis time = gcs.getTimeAxis();
      Assert.assertNotNull("time axis", time);
      double[] expect = new double[] {0., 6.0, 12.0, 18.0};
      double[] have = (double[]) time.read().get1DJavaArray(DataType.DOUBLE);
      Assert.assertArrayEquals(expect, have, Misc.defaultMaxRelativeDiffDouble);
    }
  }
}
