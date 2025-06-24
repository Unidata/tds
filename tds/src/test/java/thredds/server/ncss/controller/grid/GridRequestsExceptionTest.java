/*
 * Copyright (c) 1998-2025 University Corporation for Atmospheric Research/Unidata
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.mock.web.MockTdsContextLoader;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author mhermida
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml", "/WEB-INF/spring-servlet.xml"},
    loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class GridRequestsExceptionTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;
  private String path = "/ncss/grid/gribCollection/GFS_CONUS_80km/best";

  @Before
  public void setUp() throws IOException {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void checkBadGridRequestWhenNoParams() throws Exception {
    System.out.printf("path= %s%n", path);
    MvcResult result = this.mockMvc.perform(get(path).servletPath(path)) // note make it both the request an the servlet
                                                                         // path (!)
        .andExpect(status().is(400)).andExpect(content().string(containsString("No variables requested"))).andReturn();

    System.out.printf("content= %s%n", result.getResponse().getContentAsString());
  }

  @Test
  public void checkBadGridRequestWhenEmptyVarParams() throws Exception {
    System.out.printf("path= %s%n", path);
    MvcResult result = this.mockMvc.perform(get(path).servletPath(path).param("var", "")).andExpect(status().is(400))
        .andExpect(content().string(containsString("No variables requested"))).andReturn();

    System.out.printf("content= %s%n", result.getResponse().getContentAsString());
  }

  @Test
  public void testMultipleVerticalCoordinates() throws Exception {
    System.out.printf("path= %s%n", path);
    MvcResult result = this.mockMvc.perform(get(path).servletPath(path).param("var", "all").param("vertCoord", "200.0"))
        .andExpect(status().is(400))
        .andExpect(content().string(containsString("must have variables with same vertical levels"))).andReturn();

    System.out.printf("content= %s%n", result.getResponse().getContentAsString());
  }

  @Test
  public void testTimeDoesNotIntersect() throws Exception {
    System.out.printf("path= %s%n", path);
    MvcResult result = this.mockMvc
        .perform(get(path).servletPath(path).param("var", "Pressure_reduced_to_MSL_msl").param("time",
            "2012-04-18T15:00:00Z"))
        // .param("time_window", "PT1H"))
        .andExpect(status().is(400)).andExpect(content().string(containsString("does not intersect actual time range")))
        .andReturn();

    System.out.printf("content= %s%n", result.getResponse().getContentAsString());
  }

}
