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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.test.util.TdsTestDir;
import thredds.junit4.SpringJUnit4ParameterizedClassRunner;
import thredds.junit4.SpringJUnit4ParameterizedClassRunner.Parameters;
import thredds.mock.params.GridPathParams;
import thredds.mock.web.MockTdsContextLoader;
import thredds.server.ncss.format.SupportedFormat;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.ffi.netcdf.NetcdfClibrary;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * @author mhermida
 */
@RunWith(SpringJUnit4ParameterizedClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml"}, loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class TemporalSpaceSubsettingTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;
  private MockHttpServletRequestBuilder requestBuilder;

  private SupportedFormat format;
  private String pathInfo;
  private int lengthTimeDim; // Expected time dimension length

  @Parameters
  public static Collection<Object[]> getTestParameters() {


    return Arrays.asList(new Object[][] {
        {SupportedFormat.NETCDF3, 1, GridPathParams.getPathInfo().get(4), null, null, null, null, null, null}, // No
                                                                                                               // time
                                                                                                               // subset
                                                                                                               // provided
        {SupportedFormat.NETCDF3, 6, GridPathParams.getPathInfo().get(3), "all", null, null, null, null, null}, // Requesting
                                                                                                                // all
        {SupportedFormat.NETCDF3, 6, GridPathParams.getPathInfo().get(3), "", "all", null, null, null, null}, // Requesting
                                                                                                              // all
        {SupportedFormat.NETCDF3, 1, GridPathParams.getPathInfo().get(0), "", "2012-04-19T12:00:00.000Z", null, null,
            null, null}, // Single time on singleDataset
        {SupportedFormat.NETCDF3, 1, GridPathParams.getPathInfo().get(0), "", "2012-04-19T15:30:00.000Z", "PT3H", null,
            null, null}, // Single time in range with time_window
        {SupportedFormat.NETCDF3, 6, GridPathParams.getPathInfo().get(3), "", null, null, "2012-04-18T12:00:00.000Z",
            "2012-04-19T18:00:00.000Z", null}, // Time series on Best time series
        {SupportedFormat.NETCDF3, 5, GridPathParams.getPathInfo().get(3), "", null, null, "2012-04-18T12:00:00.000Z",
            null, "PT24H"}, // Time series on Best time series
        {SupportedFormat.NETCDF4, 1, GridPathParams.getPathInfo().get(4), null, null, null, null, null, null}, // No
                                                                                                               // time
                                                                                                               // subset
                                                                                                               // provided
        {SupportedFormat.NETCDF4, 6, GridPathParams.getPathInfo().get(3), "all", null, null, null, null, null}, // Requesting
                                                                                                                // all
        {SupportedFormat.NETCDF4, 6, GridPathParams.getPathInfo().get(3), "", "all", null, null, null, null}, // Requesting
                                                                                                              // all
        {SupportedFormat.NETCDF4, 1, GridPathParams.getPathInfo().get(0), "", "2012-04-19T12:00:00.000Z", null, null,
            null, null}, // Single time on singleDataset
        {SupportedFormat.NETCDF4, 1, GridPathParams.getPathInfo().get(0), "", "2012-04-19T15:30:00.000Z", "PT3H", null,
            null, null}, // Single time in range with time_window
        {SupportedFormat.NETCDF4, 6, GridPathParams.getPathInfo().get(3), "", null, null, "2012-04-18T12:00:00.000Z",
            "2012-04-19T18:00:00.000Z", null}, // Time series on Best time series
        {SupportedFormat.NETCDF4, 5, GridPathParams.getPathInfo().get(3), "", null, null, "2012-04-18T12:00:00.000Z",
            null, "PT24H"} // Time series on Best time series

    });
  }

  public TemporalSpaceSubsettingTest(SupportedFormat format, int expectedLengthTimeDim, String pathInfoForTest,
      String temporal, String time, String time_window, String time_start, String time_end, String time_duration) {
    this.format = format;
    lengthTimeDim = expectedLengthTimeDim;
    pathInfo = pathInfoForTest;
    String servletPath = pathInfo;

    requestBuilder = MockMvcRequestBuilders.get(servletPath).servletPath(servletPath)
        .param("accept", format.getAliases().get(0)).param("temporal", temporal).param("time", time)
        .param("time_window", time_window).param("time_duration", time_duration).param("time_start", time_start)
        .param("time_end", time_end).param("var", "Temperature");
  }

  @Before
  public void setUp() throws IOException {
    mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void shouldGetTimeRange() throws Exception {
    skipTestIfNetCDF4NotPresent();

    MvcResult mvc = this.mockMvc.perform(requestBuilder).andReturn();

    SpatialSubsettingTest.showRequest(mvc.getRequest());

    if (mvc.getResponse().getStatus() != 200) {
      System.out.printf("FAIL %s%n", mvc.getResponse().getContentAsString());
      assert false;
    }

    // byte[] result = mvc.getResponse().getContentAsByteArray();
    // ByteArrayInputStream is = new ByteArrayInputStream(result);
    // IO.writeToFile(is, "C:/temp/shouldGetTimeRange.nc");

    // Open the binary response in memory
    NetcdfFile nf;
    NetcdfDataset ds;
    nf = NetcdfFiles.openInMemory("test_data.ncs", mvc.getResponse().getContentAsByteArray());
    ds = NetcdfDatasets.enhance(nf, NetcdfDataset.getDefaultEnhanceMode(), null);
    Dimension time = ds.findDimension("time");

    assertEquals(lengthTimeDim, time.getLength());

  }

  private void skipTestIfNetCDF4NotPresent() {
    if (format == SupportedFormat.NETCDF4) {
      assumeTrue(NetcdfClibrary.isLibraryPresent());
    }
  }
}
