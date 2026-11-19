/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.wfs;

import org.junit.Test;
import thredds.test.util.TestOnLocalServer;

public class TestWfsDisabledDefault {

  @Test
  public void checkDisabled() throws Exception {
    // make sure dataset is wired up and OPeNDAP access works
    final String enabledPath = "/dodsC/defaultWfsOff/testgrid1.nc.das";
    final String enabledEndpoint = TestOnLocalServer.withHttpPath(enabledPath);
    TestOnLocalServer.getContent(enabledEndpoint, 200);

    // make sure wfs is disabled (although it is listed as a service in the catalog;
    // see tds/src/test/content/thredds/defaultServices.xml)
    final String disabledPath = "/wfs/defaultWfsOff/testgrid1.nc?service=WFS&version=2.0.0&request=GetCapabilities";
    final String disabledEndpoint = TestOnLocalServer.withHttpPath(disabledPath);
    TestOnLocalServer.getContent(disabledEndpoint, 403);
  }
}
