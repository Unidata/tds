/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.exp.enhancement.vectorize;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import thredds.client.catalog.Access;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import thredds.client.catalog.ServiceType;
import thredds.client.catalog.tools.DataFactory;
import thredds.server.catalog.TdsLocalCatalog;
import ucar.ma2.Array;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.nc2.dataset.NetcdfDataset;

public class TestVectorize {
  private static Dataset dsNcmlFile;
  private static Dataset dsNcmlCat;
  private static DataFactory fac = new DataFactory();

  @BeforeClass
  public static void setup() {
    Catalog cat = TdsLocalCatalog.open("/catalog/catalog.xml");
    dsNcmlFile = cat.findDatasetByID("vectorizeNcmlFile");
    dsNcmlCat = cat.findDatasetByID("vectorizeNcmlCat");
    assert dsNcmlFile != null;
    assert dsNcmlCat != null;
  }

  @Test
  public void testTestVectorizeFileOpendap() throws IOException {
    Access access = dsNcmlFile.getAccess(ServiceType.OPENDAP);
    assertThat(access).isNotNull();
    testAccessMethod(access);
  }

  @Test
  public void testTestVectorizeCatOpendap() throws IOException {
    Access access = dsNcmlCat.getAccess(ServiceType.OPENDAP);
    assertThat(access).isNotNull();
    testAccessMethod(access);
  }

  @Test
  public void testTestVectorizeFileCdmremote() throws IOException {
    Access access = dsNcmlFile.getAccess(ServiceType.CdmRemote);
    assertThat(access).isNotNull();
    testAccessMethod(access);
  }

  @Test
  public void testTestVectorizeCatCdmremote() throws IOException {
    Access access = dsNcmlCat.getAccess(ServiceType.CdmRemote);
    assertThat(access).isNotNull();
    testAccessMethod(access);
  }

  private void testAccessMethod(Access access) throws IOException {
    NetcdfDataset ds = fac.openDataset(access, false, null, null);
    assertThat(ds).isNotNull();
    checkSpeed(ds);
    checkDir(ds);
  }

  private void checkSpeed(NetcdfDataset ds) throws IOException {
    Array speed = ds.findVariable("cspd").read();
    MinMax minMaxSpeed = MAMath.getMinMax(speed);
    assertThat(minMaxSpeed.min).isWithin(0.01).of(0.05);
    assertThat(minMaxSpeed.max).isWithin(0.01).of(0.27);
  }

  private void checkDir(NetcdfDataset ds) throws IOException {
    Array dir = ds.findVariable("cdir").read();
    MinMax minMaxDir = MAMath.getMinMax(dir);
    assertThat(minMaxDir.min).isWithin(0.1).of(121.1);
    assertThat(minMaxDir.max).isWithin(0.1).of(223.9);
  }
}
