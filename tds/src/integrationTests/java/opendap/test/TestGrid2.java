/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package opendap.test;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.test.util.TdsUnitTestCommon;
import thredds.test.util.TestOnLocalServer;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.write.Ncdump;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;

/**
 * Test nc2 dods in the JUnit framework.
 * Dataset {
 * Grid {
 * ARRAY:
 * Float32 var[time=2][time=2];
 * MAPS:
 * Float32 time[time=2];
 * } testgrid_samedim
 * data:
 * var = 0.0, 1.0, 2.0, 3.0, 4.0;
 * time = 17.0, 23.0;
 * } testgrid2;
 */

public class TestGrid2 extends TdsUnitTestCommon {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  static final protected String URLPATH = "dodsC/scanLocal/testgrid2.nc";

  public TestGrid2() {
    setTitle("DAP Grid with repeated dimension");
    setSystemProperties();
  }

  @Test
  public void testGrid2() throws Exception {
    System.out.println("TestGrid2:");
    String url = "dods://" + TestOnLocalServer.server + URLPATH;
    boolean pass = true;
    try (NetcdfDataset ncfile = NetcdfDatasets.openDataset(url)) {
      System.out.println("url: " + url);
      String metadata = ncdumpMetadata(ncfile);

      if (prop_visual) {
        visual(getTitle() + ".dds", metadata);
      }
      String data = ncdumpData(ncfile);
      if (prop_visual) {
        visual(getTitle() + ".dods", data);
        // Read the baseline file(s)
        String diffs = compare("TestGrid2", BASELINE, data);
        if (diffs != null) {
          System.err.println(diffs);
        }
      }
    } catch (Exception e) {
      pass = false;
    }

    Assert.assertTrue("XFAIL : Testing TestGrid2" + getTitle(), pass);
  }

  private String ncdumpMetadata(NetcdfDataset ncfile) throws Exception {
    try (StringWriter sw = new StringWriter()) {
      // Print the meta-databuffer using these args to NcdumpW
      try {
        Ncdump.ncdump(ncfile, null, sw, null);
      } catch (IOException ioe) {
        throw new Exception("NcdumpW failed", ioe);
      }
      return sw.toString();
    }
  }

  private String ncdumpData(NetcdfDataset ncfile) throws Exception {
    try (StringWriter sw = new StringWriter()) {
      try {
        Ncdump.ncdump(ncfile, "-vall", sw, null);
      } catch (IOException ioe) {
        ioe.printStackTrace();
        throw new Exception("NCdumpW failed", ioe);
      }
      return sw.toString();
    }
  }


  static protected final String BASELINE = "netcdf " + TestOnLocalServer.withDodsPath("dodsC/scanLocal/testgrid2.nc")
      + " {\n" + "  dimensions:\n" + "    time = 2;\n" + "  variables:\n" + "    double var(time=2, time=2);\n" + "\n"
      + "    float time(time=2);\n" + "\n" + "  // global attributes:\n"
      + "  :_CoordSysBuilder = \"ucar.nc2.dataset.conv.DefaultConvention\";\n" + " data:\n" + "var =\n" + "  {\n"
      + "    {0.0, 1.0},\n" + "    {2.0, 3.0}\n" + "  }\n" + "time =\n" + "  {17.0, 23.0}\n" + "}\n";
}
