package thredds.server.ncss.controller.point;

import static com.google.common.truth.Truth.assertThat;

import org.jdom2.Document;
import org.jdom2.Element;
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
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.junit4.SpringJUnit4ParameterizedClassRunner;
import thredds.mock.web.MockTdsContextLoader;
import thredds.util.ContentType;
import thredds.util.xml.XmlUtil;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Test ncss point dataset info
 *
 * @author caron
 * @since 11/1/13
 */
@RunWith(SpringJUnit4ParameterizedClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml", "/WEB-INF/spring-servlet.xml"},
    loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class TestPointFCController {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @SpringJUnit4ParameterizedClassRunner.Parameters
  public static Collection<Object[]> getTestParameters() {
    return Arrays
        .asList(new Object[][] {{"/ncss/point/testBuoyFeatureCollection/Surface_Buoy_Point_Data_fc.cdmr", 54, "point"},
            {"/ncss/point/testSurfaceSynopticFeatureCollection/Surface_Synoptic_Point_Data_fc.cdmr", 41, "point"},});
  }

  String path, type;
  int nvars;

  public TestPointFCController(String path, int nvars, String type) {
    this.path = path;
    this.nvars = nvars;
    this.type = type;
  }

  @Test
  public void getDatasetXml() throws Exception {
    String xmlpath = path + "/dataset.xml";
    logger.debug("request=" + xmlpath);
    RequestBuilder rb = MockMvcRequestBuilders.get(xmlpath).servletPath(xmlpath);
    MvcResult result = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(ContentType.xml.getContentHeader())).andReturn();

    String ress = result.getResponse().getContentAsString();
    logger.debug(ress);

    Document doc = XmlUtil.getStringResponseAsDoc(result.getResponse());

    List<Element> vars = XmlUtil.evaluateXPath(doc, "//variable");
    assertThat(vars).isNotNull();
    int nVars = vars.size();
    logger.debug("nvars = " + nVars);
    assertThat(nVars).isEqualTo(this.nvars);

    List<Element> elems = XmlUtil.evaluateXPath(doc, "capabilities/featureDataset");
    assertThat(elems).isNotNull();
    assertThat(elems.size()).isEqualTo(1);
    Element fdx = elems.get(0);
    assertThat(fdx.getAttributeValue("type")).isEqualTo(type);
    assertThat(fdx.getAttributeValue("url")).isEqualTo(path);
  }

  @Test
  public void getDatasetHtml() throws Exception {
    String htmlpath = path + "/dataset.html";
    logger.debug("request=" + htmlpath);
    RequestBuilder rb = MockMvcRequestBuilders.get(htmlpath).servletPath(htmlpath);
    MvcResult result = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(ContentType.html.getContentHeader())).andReturn();

    logger.debug(result.getResponse().getContentAsString());
  }

}

