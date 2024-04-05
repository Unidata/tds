/*
 * Copyright (c) 1998-2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.ncss.controller.grid;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.junit4.SpringJUnit4ParameterizedClassRunner.Parameters;
import thredds.mock.params.GridDataParameters;
import thredds.mock.params.GridPathParams;
import thredds.server.ncss.format.SupportedFormat;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;


/**
 * not sure we are going to support vertical strides.
 *
 */
// @RunWith(SpringJUnit4ParameterizedClassRunner.class)
// @WebAppConfiguration
// @ContextConfiguration(locations = { "/WEB-INF/applicationContext.xml" }, loader = MockTdsContextLoader.class)
// @Category(NeedsCdmUnitTest.class)
public class VerticalStrideSubsettingTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;
  private RequestBuilder requestBuilder;

  // @Autowired
  // private GridDataController gridDataController;

  // private GridDataRequestParamsBean params;
  // private BindingResult validationResult;
  // private MockHttpServletResponse response ;

  private String accept;
  private String pathInfo;
  private int[][] expectedShapes;
  private List<String> vars;
  private Integer vertStride;

  @Parameters
  public static Collection<Object[]> getTestParameters() {

    return Arrays.asList(new Object[][] {
        {SupportedFormat.NETCDF3, new int[][] {{1, 65, 93}, {1, 65, 93}}, GridPathParams.getPathInfo().get(4),
            GridDataParameters.getVars().get(0), 1}, // No vertical levels
        {SupportedFormat.NETCDF3, new int[][] {{1, 1, 65, 93}, {1, 1, 65, 93}}, GridPathParams.getPathInfo().get(3),
            GridDataParameters.getVars().get(1), 1}, // Same vertical level (one level)
        {SupportedFormat.NETCDF3, new int[][] {{1, 10, 65, 93}, {1, 10, 65, 93}}, GridPathParams.getPathInfo().get(3),
            GridDataParameters.getVars().get(2), 3}, // Same vertical level (multiple level)
        {SupportedFormat.NETCDF3, new int[][] {{1, 65, 93}, {1, 10, 65, 93}, {1, 1, 65, 93}},
            GridPathParams.getPathInfo().get(3), GridDataParameters.getVars().get(3), 3}, // No vertical levels and
                                                                                          // vertical levels
        {SupportedFormat.NETCDF3, new int[][] {{1, 1, 65, 93}, {1, 6, 65, 93}}, GridPathParams.getPathInfo().get(3),
            GridDataParameters.getVars().get(4), 5}, // Different vertical levels

        {SupportedFormat.NETCDF4, new int[][] {{1, 65, 93}, {1, 65, 93}}, GridPathParams.getPathInfo().get(4),
            GridDataParameters.getVars().get(0), 1}, // No vertical levels
        {SupportedFormat.NETCDF4, new int[][] {{1, 1, 65, 93}, {1, 1, 65, 93}}, GridPathParams.getPathInfo().get(3),
            GridDataParameters.getVars().get(1), 1}, // Same vertical level (one level)
        {SupportedFormat.NETCDF4, new int[][] {{1, 10, 65, 93}, {1, 10, 65, 93}}, GridPathParams.getPathInfo().get(3),
            GridDataParameters.getVars().get(2), 3}, // Same vertical level (multiple level)
        {SupportedFormat.NETCDF4, new int[][] {{1, 65, 93}, {1, 10, 65, 93}, {1, 1, 65, 93}},
            GridPathParams.getPathInfo().get(3), GridDataParameters.getVars().get(3), 3}, // No vertical levels and
                                                                                          // vertical levels
        {SupportedFormat.NETCDF4, new int[][] {{1, 1, 65, 93}, {1, 6, 65, 93}}, GridPathParams.getPathInfo().get(3),
            GridDataParameters.getVars().get(4), 5}, // Different vertical levels

    });
  }

  public VerticalStrideSubsettingTest(SupportedFormat format, int[][] result, String pathInfo, List<String> vars,
      Integer vertStride) {
    this.accept = format.getAliases().get(0);
    this.expectedShapes = result;
    this.pathInfo = pathInfo;
    this.vars = vars;
    this.vertStride = vertStride;
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
        .param("var", varParamVal).param("vertStride", String.valueOf(vertStride));

  }

  // @Test
  public void shoudGetVerticalStridedSubset() throws Exception {

    mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(new ResultMatcher() {
      public void match(MvcResult result) throws Exception {
        // Open the binary response in memory
        NetcdfFile nf;
        ucar.nc2.dt.grid.GridDataset gdsDataset;
        nf = NetcdfFiles.openInMemory("test_data.ncs", result.getResponse().getContentAsByteArray());
        gdsDataset =
            new ucar.nc2.dt.grid.GridDataset(NetcdfDatasets.enhance(nf, NetcdfDataset.getDefaultEnhanceMode(), null));

        assertTrue(gdsDataset.getCalendarDateRange().isPoint());
        List<VariableSimpleIF> vars = gdsDataset.getDataVariables();
        int[][] shapes = new int[vars.size()][];
        int cont = 0;
        for (VariableSimpleIF var : vars) {
          // int[] shape =var.getShape();
          shapes[cont] = var.getShape();
          cont++;
          // String dimensions =var.getDimensions().toString();
          // int rank =var.getRank();
        }

        assertArrayEquals(expectedShapes, shapes);
      }
    });
    // assertEquals(expectedValue, Integer.valueOf( gdsDataset.getDataVariables().size()));
  }

}

