/*
 * Copyright (c) 1998-2025 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.ncss.controller.gridaspoint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
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
import org.springframework.web.context.WebApplicationContext;
import thredds.mock.web.MockTdsContextLoader;
import thredds.server.ncss.format.SupportedFormat;
import thredds.server.ncss.format.SupportedOperation;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml", "/WEB-INF/spring-servlet.xml"},
    loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class TestGridAsPointMisc {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void fileNotFound() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get("/ncss/grid/cdmUnitTest/ncss/GFS/CONUS_80km/baddie.nc")
        .servletPath("/ncss/grid/cdmUnitTest/ncss/GFS/CONUS_80km/baddie.nc").param("accept", "netcdf")
        .param("var", "Relative_humidity_height_above_ground", "Temperature_height_above_ground")
        .param("latitude", "40.019").param("longitude", "-105.293");

    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(404));
  }

  @Test
  public void getGridAsPointSubsetAllSupportedFormats() throws Exception {
    for (SupportedFormat sf : SupportedOperation.GRID_AS_POINT_REQUEST.getSupportedFormats()) {
      RequestBuilder rb = MockMvcRequestBuilders.get("/ncss/grid/testGFSfmrc/GFS_CONUS_80km_nc_best.ncd")
          .servletPath("/ncss/grid/testGFSfmrc/GFS_CONUS_80km_nc_best.ncd").param("accept", sf.toString())
          .param("var", "Relative_humidity_height_above_ground", "Temperature_height_above_ground")
          .param("latitude", "40.019").param("longitude", "-105.293");

      System.out.printf("getGridAsPointSubsetAllSupportedFormats return type=%s%n", sf);

      MvcResult mvcResult = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
      String ct = mvcResult.getResponse().getContentType();
      Assert.assertTrue(ct.startsWith(sf.getMimeType()));
    }
  }

  @Test
  public void getGridAsProfileSubsetAllSupportedFormats() throws Exception {
    for (SupportedFormat sf : SupportedOperation.GRID_AS_POINT_REQUEST.getSupportedFormats()) {
      RequestBuilder rb = MockMvcRequestBuilders.get("/ncss/grid/testGFSfmrc/GFS_CONUS_80km_nc_best.ncd")
          .servletPath("/ncss/grid/testGFSfmrc/GFS_CONUS_80km_nc_best.ncd").param("accept", sf.toString())
          .param("var", "Relative_humidity", "Temperature").param("latitude", "40.019").param("longitude", "-105.293");

      System.out.printf("getGridAsProfileSubsetAllSupportedFormats return type=%s%n", sf);

      MvcResult mvcResult = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
      String ct = mvcResult.getResponse().getContentType();
      Assert.assertTrue(ct.startsWith(sf.getMimeType()));
    }
  }

  @Test
  public void getGridAsPointAndProfileSubsetAllSupportedFormats() throws Exception {
    List<SupportedFormat> formats = Arrays.asList(new SupportedFormat[] {SupportedFormat.CSV_FILE,
        SupportedFormat.CSV_STREAM, SupportedFormat.XML_FILE, SupportedFormat.XML_STREAM});
    for (SupportedFormat sf : formats) {
      RequestBuilder rb = MockMvcRequestBuilders.get("/ncss/grid/testGFSfmrc/GFS_CONUS_80km_nc_best.ncd")
          .servletPath("/ncss/grid/testGFSfmrc/GFS_CONUS_80km_nc_best.ncd").param("accept", sf.toString())
          .param("var", "Relative_humidity_height_above_ground", "Temperature").param("latitude", "40.019")
          .param("longitude", "-105.293");

      System.out.printf("getGridAsPointAndProfileSubsetAllSupportedFormats return type=%s%n", sf);

      MvcResult mvcResult = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
      String ct = mvcResult.getResponse().getContentType();
      Assert.assertTrue(ct.startsWith(sf.getMimeType()));
    }
  }
}
