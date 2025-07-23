/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.admin.collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.mock.web.MockTdsContextLoader;
import thredds.servlet.filter.LocalhostFilter;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml", "/WEB-INF/spring-servlet.xml"},
    loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class TestLocalCollectionController {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).addFilter(new LocalhostFilter()).build();
  }

  @Test
  public void checkLocalRequest() throws Exception {
    String path = "/local/collection/showStatus";
    // make sure request has same address as local server
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).with(request -> {
      request.setRemoteAddr(request.getLocalAddr());
      return request;
    });

    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200)).andReturn();
  }

  @Test
  public void checkNonLocalRequest() throws Exception {
    String path = "/local/collection/showStatus";

    // set request address as Quad9 DNS (could be anything that is NOT the local server address)
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).with(request -> {
      request.setRemoteAddr("9.9.9.9");
      return request;
    });

    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(404)).andReturn();
  }
}
