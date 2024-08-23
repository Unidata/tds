package thredds.server.catalogservice;

import static com.google.common.truth.Truth.assertThat;

import java.lang.invoke.MethodHandles;
import jakarta.servlet.ServletContext;
import org.apache.http.HttpStatus;
import org.hamcrest.core.StringContains;
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
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.mock.web.MockTdsContextLoader;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml", "/WEB-INF/spring-servlet.xml"},
    loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class TestContext {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  public void shouldReturnCorrectContext() throws Exception {
    final String path = "/catalog/enhancedCatalog.xml";
    final ServletContext servletContext = webApplicationContext.getServletContext();
    assertThat(servletContext).isNotNull();
    final String context = servletContext.getContextPath();
    RequestBuilder requestBuilder = MockMvcRequestBuilders.get(path).servletPath(path);

    final String expectedResult = "catalogRef xlink:href=\"/" + context + "/catalog/testEnhanced/catalog.xml\"";
    this.mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is(HttpStatus.SC_OK))
        .andExpect(MockMvcResultMatchers.content().string(new StringContains(expectedResult))).andReturn();
  }
}
