/*
 * Copyright (c) 2023-2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE.txt for license information.
 */

package thredds.server.metadata;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.util.Predicate;
import thredds.mock.web.MockTdsContextLoader;
import thredds.util.ContentType;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml", "/WEB-INF/spring-servlet.xml"},
    loader = MockTdsContextLoader.class)
public class NcIsoIT {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void shouldReturnNcml() throws Exception {
    String path = "/ncml/scanLocal/testgrid1.nc";
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path);

    MvcResult result = mockMvc.perform(rb).andReturn();
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.SC_OK);
    byte[] response = result.getResponse().getContentAsByteArray();

    final String expectedOutput = "/thredds/server/ncIso/testgrid1.ncml.xml";
    final Predicate<Node> filter = node -> !(node.hasAttributes() && node.getAttributes().getNamedItem("name") != null
        && (node.getAttributes().getNamedItem("name").getNodeValue().equals("metadata_creation")
            || node.getAttributes().getNamedItem("name").getNodeValue().equals("nciso_version")));
    compare(response, expectedOutput, ContentType.xml, filter);
  }

  @Test
  public void shouldReturnIso() throws Exception {
    String path = "/iso/scanLocal/testgrid1.nc";
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path);

    MvcResult result = mockMvc.perform(rb).andReturn();
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.SC_OK);
    byte[] response = result.getResponse().getContentAsByteArray();

    final String expectedOutput = "/thredds/server/ncIso/testgrid1.iso.xml";
    final Predicate<Node> filter =
        node -> !node.getTextContent().startsWith("This record was translated from NcML using")
            && !node.getNodeName().startsWith("gco:Date");
    compare(response, expectedOutput, ContentType.xml, filter);
  }

  @Test
  public void shouldReturnUddc() throws Exception {
    String path = "/uddc/scanLocal/testgrid1.nc";
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path);

    MvcResult result = mockMvc.perform(rb).andReturn();
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.SC_OK);
    byte[] response = result.getResponse().getContentAsByteArray();

    final String expectedOutput = "/thredds/server/ncIso/testgrid1.uddc.html";
    compare(response, expectedOutput, ContentType.html, node -> true);
  }

  private void compare(byte[] response, String expectedOutput, ContentType expectedType, Predicate<Node> filter) {

    final Diff diff = DiffBuilder.compare(Input.fromStream(getClass().getResourceAsStream(expectedOutput)))
        .withTest(Input.fromByteArray(response)).normalizeWhitespace()
        // don't compare elements with e.g. version/ current datetime
        .withNodeFilter(filter).build();
    assertWithMessage(diff.toString()).that(diff.hasDifferences()).isFalse();
  }
}
