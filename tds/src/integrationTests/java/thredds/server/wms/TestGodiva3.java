/*
 * Copyright (c) 1998-2024 University Corporation for Atmospheric Research/Unidata
 * See LICENSE.txt for license information.
 */

package thredds.server.wms;

import static com.google.common.truth.Truth.assertThat;

import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import thredds.test.util.TestOnLocalServer;


public class TestGodiva3 {

  @Test
  public void testGodiva3GetConfig() {
    String endpoint = TestOnLocalServer.withHttpPath("/getconfig");
    byte[] result =
        TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, "application/json;charset=iso-8859-1");
    String jsonStr = new String(result, StandardCharsets.ISO_8859_1);
    assertThat(jsonStr).isEqualTo("{\"mapWidth\":1024,\"mapHeight\":512}");
  }
}
