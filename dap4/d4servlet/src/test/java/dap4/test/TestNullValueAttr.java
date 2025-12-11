/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package dap4.test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dap4.mock.MockResponseOutputStream;
import dap4.mock.TestDapControllerBase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.unidata.util.test.TestDir;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

public class TestNullValueAttr {

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void testDMR() throws IOException {
    String path = "/thredds/dap4/sentinel.nc.dmr.xml";

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getRequestURL()).thenReturn(new StringBuffer("https://tds.org/" + path));
    when(request.getServletPath()).thenReturn(path);

    try (MockResponseOutputStream out = new MockResponseOutputStream();
        NetcdfDataset ncd = NetcdfDatasets.openDataset(TestDir.cdmUnitTestDir
            + "formats/hdf5/sentinel/S5P_OFFL_L1B_IR_SIR_20180430T001950_20180430T020120_02818_01_010000_20180430T035011.nc")) {
      when(response.getOutputStream()).thenReturn(out);
      TestDapControllerBase controller = new TestDapControllerBase(ncd);
      controller.handleRequest(request, response);
      String dmr = new String(out.getOutput(), StandardCharsets.UTF_8);
      assertThat(dmr).isNotEmpty();
      // DMR for this dataset should include an attribute named gmd:date with a null value
      // as indicated by <Value/>
      Pattern regex = Pattern.compile("<Attribute name=\"gmd:date\" type=\"String\">\\s*<Value/>\\s*</Attribute>");
      assertThat(dmr).containsMatch(regex);
    }
  }
}
