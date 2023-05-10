package thredds.server.fileserver;

import static com.google.common.truth.Truth.assertThat;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.lang.invoke.MethodHandles;

/**
 * @author cwardgar
 * @since 2016-10-18
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml"})
@Category(NeedsCdmUnitTest.class)
public class FileServerControllerTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private MockMvc mockMvc;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.standaloneSetup(new FileServerController()).build();
  }

  @Test
  public void testLastModified() throws Exception {
    String path = "/fileServer/testNAMfmrc/files/20060925_0600.nc";
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path);

    MvcResult result = mockMvc.perform(rb).andReturn();
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.SC_OK);

    // We want this statement to succeed without exception.
    // Throws NullPointerException if header doesn't exist
    // Throws IllegalArgumentException if header value is not a valid date.
    result.getResponse().getDateHeader("Last-Modified");
  }

  @Test
  public void shouldReturnFileWithDatasetRootInUrlPathAndLocationInNcml() throws Exception {
    final String path = "/fileServer/localContent/ncmlLocation";
    final RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path);

    mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
  }

  @Test
  public void shouldReturnFileWithFeatureCollectionPathInUrlPathAndLocationInNcml() throws Exception {
    final String path = "/fileServer/testGFSfmrc/ncmlLocation";
    final RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path);

    mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
  }
}
