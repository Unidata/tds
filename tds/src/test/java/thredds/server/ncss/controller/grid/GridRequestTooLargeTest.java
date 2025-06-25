/*
 * Copyright (c) 1998-2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.ncss.controller.grid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import thredds.mock.web.MockTdsContextLoader;
import thredds.server.ncss.controller.NcssGridController;
import thredds.server.exception.RequestTooLargeException;
import thredds.server.ncss.params.NcssGridParamsBean;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mhermida
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml", "/WEB-INF/spring-servlet.xml"},
    loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class GridRequestTooLargeTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private NcssGridController featureDatasetController;

  private MockHttpServletResponse response;
  private MockHttpServletRequest request;

  // <featureCollection featureType="GRIB" name="GFS_CONUS_80km" path="gribCollection/GFS_CONUS_80km">
  private String pathInfo = "/ncss/grid/gribCollection/GFS_CONUS_80km/best";

  @Before
  public void setUp() throws IOException {

    response = new MockHttpServletResponse();
    request = new MockHttpServletRequest();
    request.setPathInfo(pathInfo);
    request.setServletPath(pathInfo);

  }

  @Test(expected = RequestTooLargeException.class)
  public void testRequestTooLargeException() throws Exception {
    BindingResult validationResult;
    NcssGridParamsBean params = new NcssGridParamsBean();
    params.setTemporal("all");
    List<String> vars = new ArrayList<>();
    vars.add("u-component_of_wind_isobaric");
    vars.add("v-component_of_wind_isobaric");
    vars.add("Geopotential_height_isobaric");
    params.setVar(vars);
    validationResult = new BeanPropertyBindingResult(params, "params");
    featureDatasetController.handleRequest(request, response, params, validationResult);
  }

}
