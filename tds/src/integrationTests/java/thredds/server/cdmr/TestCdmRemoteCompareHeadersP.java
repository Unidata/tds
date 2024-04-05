/*
 * Copyright (c) 1998-2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.cdmr;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.test.util.TestOnLocalServer;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.stream.CdmRemote;
import ucar.nc2.util.CompareNetcdf2;
import ucar.unidata.util.test.TestDir;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

@RunWith(Parameterized.class)
@Category(NeedsCdmUnitTest.class)
public class TestCdmRemoteCompareHeadersP {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  static String contentRoot = TestDir.cdmUnitTestDir + "formats";
  static String urlPath = "cdmremote/scanCdmUnitTests/formats";

  static int total, success;

  @AfterClass
  static public void show() {
    System.out.printf("success = %d/%d %n", success, total);
  }

  @Parameterized.Parameters(name = "{0}")
  public static List<Object[]> getTestParameters() {

    List<Object[]> result = new ArrayList<>(500);

    addFromScan(result, contentRoot + "/netcdf3/", new SuffixFileFilter(".nc"));
    addFromScan(result, contentRoot + "/netcdf4/", new SuffixFileFilter(".nc"));

    addFromScan(result, contentRoot + "/hdf5/", new FileFilter() {
      public boolean accept(File pathname) {
        return pathname.getPath().endsWith(".h5") || pathname.getPath().endsWith(".he5");
      }
    });
    addFromScan(result, contentRoot + "/hdf4/", new FileFilter() {
      public boolean accept(File pathname) {
        return pathname.getPath().endsWith(".hdf") || pathname.getPath().endsWith(".eos");
      }
    });
    addFromScan(result, contentRoot + "/grib1/", new FileFilter() {
      public boolean accept(File pathname) {
        String name = pathname.getName();
        return !name.contains(".gbx") && !name.contains(".ncx");
      }
    });
    addFromScan(result, contentRoot + "/grib2/", new FileFilter() {
      public boolean accept(File pathname) {
        String name = pathname.getName();
        return !name.contains(".gbx") && !name.contains(".ncx");
      }
    });
    addFromScan(result, contentRoot + "/gini/", new SuffixFileFilter(".gini"));
    addFromScan(result, contentRoot + "/gempak/", new SuffixFileFilter(".gem"));

    return result;
  }

  static void addFromScan(final List<Object[]> list, String dirName, FileFilter ff) {
    try {
      TestDir.actOnAll(dirName, ff, new TestDir.Act() {
        public int doAct(String filename) throws IOException {
          list.add(new Object[] {filename});
          return 1;
        }
      }, true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /////////////////////////////////////////////////////////////

  public TestCdmRemoteCompareHeadersP(String filename) {
    this.filename = filename;
  }

  String filename;

  @Test
  public void doOne() throws IOException {
    String name = filename.substring(contentRoot.length()).replace("\\", "/");
    String remote = TestOnLocalServer.withHttpPath(urlPath + name);
    total++;
    success += compareDatasets(filename, remote, false);
  }

  static int compareDatasets(String local, String remote, boolean readData) throws IOException {
    return compareDatasetsNew(local, remote, readData);
  }

  private static int compareDatasetsNew(String local, String remote, boolean readData) throws IOException {
    try (NetcdfFile ncfile = NetcdfDatasets.openFile(local, null); NetcdfFile ncremote = new CdmRemote(remote)) {

      Formatter f = new Formatter();
      CompareNetcdf2 mind = new CompareNetcdf2(f, false, false, readData);
      boolean ok = mind.compare(ncfile, ncremote, new NcstreamObjFilter());
      if (!ok) {
        System.out.printf("--Compare %s to %s%n", local, remote);
        System.out.printf("  %s%n", f);
      }
      Assert.assertTrue(local + " != " + remote, ok);
    }
    return 1;
  }

  private static int compareDatasetsOld(String local, String remote, boolean readData) throws IOException {
    try (NetcdfFile ncfile = NetcdfDataset.openFile(local, null); NetcdfFile ncremote = new CdmRemote(remote)) {

      Formatter f = new Formatter();
      CompareNetcdf2 mind = new CompareNetcdf2(f, false, false, readData);
      boolean ok = mind.compare(ncfile, ncremote, new NcstreamObjFilter());
      if (!ok) {
        System.out.printf("--Compare %s to %s%n", local, remote);
        System.out.printf("  %s%n", f);
      }
      Assert.assertTrue(local + " != " + remote, ok);
    }
    return 1;
  }

  public static class NcstreamObjFilter implements CompareNetcdf2.ObjFilter {

    @Override
    public boolean attCheckOk(Variable v, Attribute att) {
      // if (v != null && v.isMemberOfStructure()) return false;
      String name = att.getShortName();

      if (name.equals(_Coordinate.Axes))
        return false;

      return true;
    }

    @Override
    public boolean varDataTypeCheckOk(Variable v) {
      return true;
    }

  }

}
