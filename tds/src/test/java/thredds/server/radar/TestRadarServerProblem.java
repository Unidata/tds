/*
 * Copyright (c) 1998-2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.radar;

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import thredds.mock.web.MockTdsContextLoader;
import thredds.util.ContentType;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.lang.invoke.MethodHandles;

/**
 * Single problems in radarServer testing
 *
 * @author caron
 * @since 10/29/13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml", "/WEB-INF/spring-servlet.xml"},
    loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class TestRadarServerProblem {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private org.springframework.web.context.WebApplicationContext wac;

  private MockMvc mockMvc;
  private String path = "/radarServer/nexrad/level3/IDD/stations.xml";

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void cdmRemoteRequestCapabilitiesTest() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path);

    MvcResult result = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        // .andExpect(MockMvcResultMatchers.content().contentType(ContentType.xml.getContentHeader()))
        .andReturn();

    System.out.printf("content = %s%n", result.getResponse().getContentAsString());
  }

}
