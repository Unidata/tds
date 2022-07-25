/*
 * Copyright (c) 1998-2021 University Corporation for Atmospheric Research/Unidata
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
import org.springframework.mock.web.MockHttpServletRequest;
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
import thredds.server.ncss.controller.AbstractNcssController;
import thredds.server.ncss.dataservice.DatasetHandlerAdapter;
import thredds.server.ncss.format.SupportedFormat;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.ffi.netcdf.NetcdfClibrary;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;

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
public class SpatialSubsettingTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;
  private RequestBuilder requestBuilder;

  private SupportedFormat format;
  private String accept;
  private String pathInfo;
  private List<String> vars;
  private double[] latlonRectParams;
  private LatLonRect requestedBBOX;
  private LatLonRect datasetBBOX;

  @Parameters
  public static Collection<Object[]> getTestParameters() {

    return Arrays.asList(new Object[][] {
        {SupportedFormat.NETCDF3, GridPathParams.getPathInfo().get(4), GridDataParameters.getVars().get(0),
            GridDataParameters.getLatLonRect().get(0)}, // bounding box contained in the declared dataset bbox
        {SupportedFormat.NETCDF3, GridPathParams.getPathInfo().get(4), GridDataParameters.getVars().get(0),
            GridDataParameters.getLatLonRect().get(1)}, // bounding box that intersects the declared bbox

        {SupportedFormat.NETCDF4, GridPathParams.getPathInfo().get(4), GridDataParameters.getVars().get(0),
            GridDataParameters.getLatLonRect().get(0)}, // bounding box contained in the declared dataset bbox
        {SupportedFormat.NETCDF4, GridPathParams.getPathInfo().get(4), GridDataParameters.getVars().get(0),
            GridDataParameters.getLatLonRect().get(1)}, // bounding box that intersects the declared bbox

    });
  }

  public SpatialSubsettingTest(SupportedFormat format, String pathInfo, List<String> vars, double[] latlonRectParams) {
    this.format = format;
    this.accept = format.getAliases().get(0);
    this.pathInfo = pathInfo;
    this.vars = vars;
    this.latlonRectParams = latlonRectParams;
  }

  @Before
  public void setUp() throws IOException {
    mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    String servletPath = pathInfo;

    // Creates values for param var
    Iterator<String> it = vars.iterator();
    String varParamVal = it.next();
    while (it.hasNext()) {
      String next = it.next();
      varParamVal = varParamVal + "," + next;
    }

    requestBuilder = MockMvcRequestBuilders.get(servletPath).servletPath(servletPath).param("var", varParamVal)
        .param("west", String.valueOf(latlonRectParams[0])).param("south", String.valueOf(latlonRectParams[1]))
        .param("east", String.valueOf(latlonRectParams[2])).param("north", String.valueOf(latlonRectParams[3]))
        .param("accept", accept);

    String datasetPath = AbstractNcssController.getDatasetPath(this.pathInfo);
    GridDataset gds = DatasetHandlerAdapter.openGridDataset(datasetPath);
    assert (gds != null);

    requestedBBOX = new LatLonRect(LatLonPoint.create(latlonRectParams[1], latlonRectParams[0]),
        LatLonPoint.create(latlonRectParams[3], latlonRectParams[2]));
    datasetBBOX = gds.getBoundingBox();
    gds.close();
  }

  @Test
  public void shouldGetVariablesSubset() throws Exception {
    skipTestIfNetCDF4NotPresent();

    // gridDataController.getGridSubset(params, validationResult, response);

    this.mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(new ResultMatcher() {
          public void match(MvcResult result) throws Exception {
            NetcdfFile nf = NetcdfFile.openInMemory("test_data.ncs", result.getResponse().getContentAsByteArray());
            ucar.nc2.dt.grid.GridDataset gdsDataset = new ucar.nc2.dt.grid.GridDataset(new NetcdfDataset(nf));
            // Open the binary response in memory
            nf = NetcdfFiles.openInMemory("test_data.ncs", result.getResponse().getContentAsByteArray());
            gdsDataset = new ucar.nc2.dt.grid.GridDataset(
                NetcdfDatasets.enhance(nf, NetcdfDataset.getDefaultEnhanceMode(), null));
            LatLonRect responseBBox = gdsDataset.getBoundingBox();

            assertTrue(
                responseBBox.intersect((datasetBBOX)) != null && responseBBox.intersect((requestedBBOX)) != null);
            assertTrue(!responseBBox.equals(datasetBBOX));
          }
        });
  }

  public static void showRequest(MockHttpServletRequest req) {
    Formatter f = new Formatter();
    Enumeration<String> params = req.getParameterNames();
    while (params.hasMoreElements()) {
      String name = params.nextElement();
      f.format(" %s=%s%n", name, req.getParameter(name));
    }
    System.out.printf("%s%n%s%n", req.getRequestURI(), f);
  }

  private void skipTestIfNetCDF4NotPresent() {
    if (format == SupportedFormat.NETCDF4) {
      assumeTrue(NetcdfClibrary.isLibraryPresent());
    }
  }
}
