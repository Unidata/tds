package thredds.server.catalogservice;

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
import org.springframework.web.context.WebApplicationContext;
import thredds.core.AllowedServices;
import thredds.core.StandardService;
import thredds.mock.web.MockTdsContextLoader;
import thredds.util.ContentType;
import ucar.unidata.util.test.category.NeedsContentRoot;
import ucar.unidata.util.test.category.NeedsExternalResource;
import java.lang.invoke.MethodHandles;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml"}, loader = MockTdsContextLoader.class)
@Category({NeedsContentRoot.class, NeedsExternalResource.class})
public class RemoteCatalogControllerTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private WebApplicationContext wac;
  private MockMvc mockMvc;

  @Autowired
  private AllowedServices allowedServices;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    allowedServices.setAllowService(StandardService.catalogRemote, true);
  }

  String dataset = "casestudies/ccs039/grids/netCDF/1998062912_eta.nc";
  String catalog = "https://thredds.ucar.edu/thredds/catalog/casestudies/ccs039/grids/netCDF/catalog.xml";

  String path = "/remoteCatalogService";
  String htmlContent = ContentType.html.getContentHeader();
  String xmlContent = ContentType.xml.getContentHeader();
  String defaultContent = htmlContent;

  @Test
  public void shouldFailForCatalogHtmlUri() throws Exception {
    String htmlCatalog = "https://thredds.ucar.edu/thredds/catalog/casestudies/ccs039/grids/netCDF/catalog.html";

    RequestBuilder rb =
        MockMvcRequestBuilders.get(path).servletPath(path).param("command", "SHOW").param("catalog", htmlCatalog);

    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(400)).andReturn();
  }

  @Test
  public void showCommandWithDefaultContentType() throws Exception {
    RequestBuilder rb =
        MockMvcRequestBuilders.get(path).servletPath(path).param("command", "SHOW").param("catalog", catalog);

    MvcResult result = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(defaultContent)).andReturn();

    logger.debug("showCommandTest status= " + result.getResponse().getStatus());
    logger.debug(result.getResponse().getContentAsString());
  }

  @Test
  public void showCommandWithXmlContent() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("command", "SHOW")
        .param("catalog", catalog).param("htmlView", "false");

    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(xmlContent)).andReturn();
  }

  @Test
  public void showCommandWithHtmlContent() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("command", "SHOW")
        .param("catalog", catalog).param("htmlView", "true");

    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(htmlContent)).andReturn();
  }

  @Test
  public void subsetCommandWithDefaultContentType() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("command", "SUBSET")
        .param("catalog", catalog).param("dataset", dataset);

    MvcResult result = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(defaultContent)).andReturn();

    logger.debug("subsetCommandTest status= " + result.getResponse().getStatus());
    logger.debug(result.getResponse().getContentAsString());
  }

  @Test
  public void subsetCommandWithXmlContent() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("command", "SUBSET")
        .param("catalog", catalog).param("dataset", dataset).param("htmlView", "false");

    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(xmlContent)).andReturn();
  }

  @Test
  public void subsetCommandWithHtmlContent() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("command", "SUBSET")
        .param("catalog", catalog).param("dataset", dataset).param("htmlView", "true");

    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(htmlContent)).andReturn();
  }

  @Test
  public void validateCommandWithDefaultContentType() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("command", "VALIDATE")
        .param("catalog", catalog).param("dataset", dataset);

    MvcResult result = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(defaultContent)).andReturn();

    logger.debug("validateCommandTest status= " + result.getResponse().getStatus());
    logger.debug(result.getResponse().getContentAsString());
  }

  @Test
  public void validateCommandWithXmlContent() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("command", "VALIDATE")
        .param("catalog", catalog).param("dataset", dataset).param("htmlView", "false");

    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(xmlContent)).andReturn();
  }

  @Test
  public void validateCommandWithHtmlContent() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("command", "VALIDATE")
        .param("catalog", catalog).param("dataset", dataset).param("htmlView", "true");

    this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(htmlContent)).andReturn();
  }
}
