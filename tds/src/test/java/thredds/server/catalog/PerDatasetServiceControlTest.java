/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.catalog;

import static com.google.common.truth.Truth.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.core.AllowedServices;
import thredds.mock.web.MockTdsContextLoader;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

/**
 * Test that the services declared for a dataset in the configuration catalog control which services may actually
 * be used against it, in addition to the global service control of threddsConfig.xml.
 * <br>
 * The datasets exercised here are declared in {@code content/thredds/serviceControl.xml}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml", "/WEB-INF/spring-servlet.xml"},
    loader = MockTdsContextLoader.class)
public class PerDatasetServiceControlTest {

  private static final String NOT_ALLOWED = "Service Not Allowed";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private AllowedServices allowedServices;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @After
  public void restoreEnforcement() {
    allowedServices.setAllowOnlyDeclared(true);
  }

  //////////////////////////////////////////////////////////////////////////////
  // a dataset declaring httpServer only

  @Test
  public void shouldAllowDeclaredFileServer() throws Exception {
    assertStatus("/fileServer/serviceControl/testData.nc", HttpStatus.OK.value());
  }

  @Test
  public void shouldDenyUndeclaredOpendap() throws Exception {
    assertServiceNotAllowed("/dodsC/serviceControl/testData.nc.dds");
  }

  @Test
  public void shouldDenyUndeclaredNcssGrid() throws Exception {
    assertServiceNotAllowed("/ncss/grid/serviceControl/testData.nc/dataset.xml");
  }

  //////////////////////////////////////////////////////////////////////////////
  // a dataset declaring opendap only

  @Test
  public void shouldAllowDeclaredOpendap() throws Exception {
    assertStatus("/dodsC/serviceControl/testData2.nc.dds", HttpStatus.OK.value());
  }

  @Test
  public void shouldDenyUndeclaredFileServer() throws Exception {
    assertServiceNotAllowed("/fileServer/serviceControl/testData2.nc");
  }

  //////////////////////////////////////////////////////////////////////////////
  // compound services are expanded to their members

  @Test
  public void shouldAllowBothMembersOfCompoundService() throws Exception {
    assertStatus("/dodsC/serviceControl/testgrid1.nc.dds", HttpStatus.OK.value());
    assertStatus("/fileServer/serviceControl/testgrid1.nc", HttpStatus.OK.value());
  }

  @Test
  public void shouldDenyServiceNotInCompoundService() throws Exception {
    assertServiceNotAllowed("/ncss/grid/serviceControl/testgrid1.nc/dataset.xml");
  }

  //////////////////////////////////////////////////////////////////////////////
  // serviceName inherited from a parent dataset

  @Test
  public void shouldApplyInheritedServiceName() throws Exception {
    assertStatus("/fileServer/serviceControl/testgrid2.nc", HttpStatus.OK.value());
    assertServiceNotAllowed("/dodsC/serviceControl/testgrid2.nc.dds");
  }

  //////////////////////////////////////////////////////////////////////////////
  // datasetScan children are covered by the scan's declaration

  @Test
  public void shouldApplyScanServicesToGeneratedDataset() throws Exception {
    assertStatus("/dodsC/serviceControlScan/testData.nc.dds", HttpStatus.OK.value());
    assertServiceNotAllowed("/fileServer/serviceControlScan/testData.nc");
  }

  //////////////////////////////////////////////////////////////////////////////
  // featureCollection datasets are covered by the collection's declaration

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void shouldApplyFeatureCollectionServices() throws Exception {
    // testUsfWsfUgridFmrc declares the "ugrid" service, which does not include NetcdfSubset
    assertServiceNotAllowed("/ncss/grid/testUsfWsfUgridFmrc/USF_WSF_nc_best.ncd/dataset.xml");
  }

  //////////////////////////////////////////////////////////////////////////////
  // fail open, and the rollback switch

  @Test
  public void shouldNotDenyUnknownPath() throws Exception {
    final int status = getStatus("/dodsC/serviceControl/doesNotExist.nc.dds");
    assertThat(status).isNotEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(status).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  public void shouldAllowUndeclaredServiceWhenEnforcementDisabled() throws Exception {
    // only consult global service availability
    allowedServices.setAllowOnlyDeclared(false);
    // should work, even through only httpServer is declared in the catalog for
    // this dataset (pre 5.10 behavior)
    assertStatus("/dodsC/serviceControl/testData.nc.dds", HttpStatus.OK.value());
  }

  @Test
  public void shouldStillAllowDatasetsThatDeclareEverythingEnabled() throws Exception {
    // declares the "all" compound service, which covers every service these tests use
    assertStatus("/fileServer/localContent/testGridAsPoint.nc", HttpStatus.OK.value());
    assertStatus("/dodsC/localContent/testGridAsPoint.nc.dds", HttpStatus.OK.value());
  }

  //////////////////////////////////////////////////////////////////////////////

  private void assertServiceNotAllowed(String path) throws Exception {
    final MvcResult result = perform(path);
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(result.getResponse().getContentAsString()).contains(NOT_ALLOWED);
  }

  private void assertStatus(String path, int expectedStatus) throws Exception {
    assertThat(getStatus(path)).isEqualTo(expectedStatus);
  }

  private int getStatus(String path) throws Exception {
    return perform(path).getResponse().getStatus();
  }

  private MvcResult perform(String path) throws Exception {
    final RequestBuilder rb = get(path).servletPath(path);
    return mockMvc.perform(rb).andReturn();
  }
}
