/*
 * Copyright (c) 1998-2021 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.ncss.controller.grid;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.test.util.TdsTestDir;
import thredds.junit4.SpringJUnit4ParameterizedClassRunner;
import thredds.junit4.SpringJUnit4ParameterizedClassRunner.Parameters;
import thredds.mock.params.GridDataParameters;
import thredds.mock.params.GridPathParams;
import thredds.mock.web.MockTdsContextLoader;
import thredds.server.ncss.format.SupportedFormat;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.ffi.netcdf.NetcdfClibrary;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * @author marcos
 *
 */
@RunWith(SpringJUnit4ParameterizedClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml"}, loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class VariableSpaceSubsettingTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;
  private RequestBuilder requestBuilder;

  private SupportedFormat format;
  private String accept;
  private String pathInfo;
  private int[][] expectedShapes;
  private List<String> vars;

  @Parameters
  public static Collection<Object[]> getTestParameters() {

    return Arrays.asList(new Object[][] {
        {SupportedFormat.NETCDF3, new int[][] {{1, 65, 93}, {1, 65, 93}}, GridPathParams.getPathInfo().get(4),
            GridDataParameters.getVars().get(0)}, // No vertical levels
        {SupportedFormat.NETCDF3, new int[][] {{1, 1, 65, 93}, {1, 1, 65, 93}}, GridPathParams.getPathInfo().get(3),
            GridDataParameters.getVars().get(1)}, // Same vertical level (one level)
        {SupportedFormat.NETCDF3, new int[][] {{1, 29, 65, 93}, {1, 29, 65, 93}}, GridPathParams.getPathInfo().get(3),
            GridDataParameters.getVars().get(2)}, // Same vertical level (multiple level)
        {SupportedFormat.NETCDF3, new int[][] {{1, 65, 93}, {1, 29, 65, 93}, {1, 1, 65, 93}},
            GridPathParams.getPathInfo().get(3), GridDataParameters.getVars().get(3)}, // No vertical levels and
                                                                                       // vertical levels
        {SupportedFormat.NETCDF3, new int[][] {{1, 1, 65, 93}, {1, 29, 65, 93}}, GridPathParams.getPathInfo().get(3),
            GridDataParameters.getVars().get(4)}, // Different vertical levels

        {SupportedFormat.NETCDF4, new int[][] {{1, 65, 93}, {1, 65, 93}}, GridPathParams.getPathInfo().get(4),
            GridDataParameters.getVars().get(0)}, // No vertical levels
        {SupportedFormat.NETCDF4, new int[][] {{1, 1, 65, 93}, {1, 1, 65, 93}}, GridPathParams.getPathInfo().get(3),
            GridDataParameters.getVars().get(1)}, // Same vertical level (one level)
        {SupportedFormat.NETCDF4, new int[][] {{1, 29, 65, 93}, {1, 29, 65, 93}}, GridPathParams.getPathInfo().get(3),
            GridDataParameters.getVars().get(2)}, // Same vertical level (multiple level)
        {SupportedFormat.NETCDF4, new int[][] {{1, 65, 93}, {1, 29, 65, 93}, {1, 1, 65, 93}},
            GridPathParams.getPathInfo().get(3), GridDataParameters.getVars().get(3)}, // No vertical levels and
                                                                                       // vertical levels
        {SupportedFormat.NETCDF4, new int[][] {{1, 1, 65, 93}, {1, 29, 65, 93}}, GridPathParams.getPathInfo().get(3),
            GridDataParameters.getVars().get(4)}, // Different vertical levels
    });
  }

  public VariableSpaceSubsettingTest(SupportedFormat format, int[][] result, String pathInfo, List<String> vars) {
    this.format = format;
    this.accept = format.getAliases().get(0);
    this.expectedShapes = result;
    this.pathInfo = pathInfo;
    this.vars = vars;
  }

  @Before
  public void setUp() throws IOException {
    String servletPath = pathInfo;
    mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

    Iterator<String> it = vars.iterator();
    String varParamVal = it.next();
    while (it.hasNext()) {
      String next = it.next();
      varParamVal = varParamVal + "," + next;
    }

    requestBuilder = MockMvcRequestBuilders.get(servletPath).servletPath(servletPath).param("accept", accept)
        .param("var", varParamVal);
  }

  @Test
  public void shouldGetVariablesSubset() throws Exception {
    skipTestIfNetCDF4NotPresent();

    mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(new ResultMatcher() {
      public void match(MvcResult result) throws Exception {
        // Open the binary response in memory
        NetcdfFile nf;
        ucar.nc2.dt.grid.GridDataset gdsDataset;

        nf = NetcdfFiles.openInMemory("test_data.ncs", result.getResponse().getContentAsByteArray());
        gdsDataset =
            new ucar.nc2.dt.grid.GridDataset(NetcdfDatasets.enhance(nf, NetcdfDataset.getDefaultEnhanceMode(), null));

        assertTrue(gdsDataset.getCalendarDateRange().isPoint());

        int[][] shapes = new int[vars.size()][];
        int count = 0;
        for (String varName : vars) {
          GeoGrid grid = gdsDataset.findGridByShortName(varName);
          shapes[count++] = grid.getShape();
        }
        assertArrayEquals(expectedShapes, shapes);
      }
    });
  }

  private void skipTestIfNetCDF4NotPresent() {
    if (format == SupportedFormat.NETCDF4) {
      assumeTrue(NetcdfClibrary.isLibraryPresent());
    }
  }
}
