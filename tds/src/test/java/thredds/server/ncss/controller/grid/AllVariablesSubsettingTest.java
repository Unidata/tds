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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.mock.params.GridPathParams;
import thredds.mock.web.MockTdsContextLoader;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mhermida
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml"}, loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class AllVariablesSubsettingTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;
  private RequestBuilder requestBuilder;


  @Before
  public void setUp() throws IOException {
    mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    String servletPath = GridPathParams.getPathInfo().get(0);
    requestBuilder = MockMvcRequestBuilders.get(servletPath).servletPath(servletPath).param("var", "all");
  }

  @Test
  public void shouldGetAllVariables() throws Exception {
    MvcResult mvc = this.mockMvc.perform(requestBuilder).andReturn();
    assertEquals(200, mvc.getResponse().getStatus());

    // Open the binary response in memory
    // NetcdfFile nf = NetcdfFile.openInMemory("test_data.ncs", response.getContentAsByteArray() );
    NetcdfFile nf;
    GridDataset gdsDataset;
    nf = NetcdfFiles.openInMemory("test_data.ncs", mvc.getResponse().getContentAsByteArray());
    gdsDataset =
        new ucar.nc2.dt.grid.GridDataset(NetcdfDatasets.enhance(nf, NetcdfDataset.getDefaultEnhanceMode(), null));

    assertTrue(gdsDataset.getCalendarDateRange().isPoint());
    assertEquals(7, gdsDataset.getDataVariables().size());

  }

}
