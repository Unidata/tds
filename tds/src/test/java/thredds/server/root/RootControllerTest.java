package thredds.server.root;

import static com.google.common.truth.Truth.assertThat;

import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.mock.web.MockTdsContextLoader;
import ucar.unidata.util.test.category.NeedsContentRoot;
import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml"}, loader = MockTdsContextLoader.class)
@Category(NeedsContentRoot.class)
public class RootControllerTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  private RequestBuilder requestBuilder;

  @PostConstruct
  public void init() {
    mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void testRootRedirect() throws Exception {
    requestBuilder = MockMvcRequestBuilders.get("/");
    MvcResult mvc = this.mockMvc.perform(requestBuilder).andReturn();
    // Check that "/" is redirected
    assertThat(mvc.getResponse().getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
    assertThat(mvc.getModelAndView()).isNotNull();
    assertThat(mvc.getModelAndView().getViewName()).isEqualTo("redirect:/catalog/catalog.html");
  }

  @Test
  public void testStaticContent() throws Exception {
    requestBuilder = MockMvcRequestBuilders.get("/tdsCat.css");
    MvcResult mvc = this.mockMvc.perform(requestBuilder).andReturn();
    // Check that "/" is redirected
    assertThat(mvc.getResponse().getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    String content = mvc.getResponse().getContentAsString();
    logger.debug("content='{}'", content);
    // Assert.assertNotNull(mvc.getModelAndView());
    // assertEquals("redirect:/catalog/catalog.html", mvc.getModelAndView().getViewName());
  }


}
