package thredds.server.ncss.controller.grid;

import java.util.List;
import java.util.Optional;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
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
import thredds.util.xml.XmlUtil;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import java.io.IOException;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

import static com.google.common.truth.Truth.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@Category(NeedsCdmUnitTest.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml", "/WEB-INF/spring-servlet.xml"},
    loader = MockTdsContextLoader.class)
public class NcmlTest {
  private static final String NCML_DATASET = "/ncss/grid/ExampleNcML/Modified.nc";
  private static final String NCML_DATASET_SCAN = "/ncss/grid/ModifyDatasetScan/example1.nc";

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Before
  public void setUp() throws IOException {
    mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void shouldShowNcmlModifiedVariableOnDatasetPage() throws Exception {
    assertShowsNcmlModifiedVariableOnDatasetPage(NCML_DATASET + "/dataset.xml");
  }

  @Test
  public void shouldShowNcmlModifiedVariableOnDatasetPageForDatasetScan() throws Exception {
    assertShowsNcmlModifiedVariableOnDatasetPage(NCML_DATASET_SCAN + "/dataset.xml");
  }

  @Test
  public void shouldReturnNcmlModifiedVariable() throws Exception {
    assertReturnsNcmlModifiedVariable(NCML_DATASET);
  }

  @Test
  public void shouldReturnNcmlModifiedVariableForDatasetScan() throws Exception {
    assertReturnsNcmlModifiedVariable(NCML_DATASET_SCAN);
  }

  private void assertShowsNcmlModifiedVariableOnDatasetPage(String servletPath) throws Exception {
    final RequestBuilder requestBuilder = MockMvcRequestBuilders.get(servletPath).servletPath(servletPath);
    final MvcResult mvcResult =
        mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    final Document doc = XmlUtil.getStringResponseAsDoc(mvcResult.getResponse());
    final List<Element> grids = XmlUtil.evaluateXPath(doc, "//grid");
    assertThat(grids).isNotNull();

    final Optional<Element> origVar =
        grids.stream().filter(e -> e.getAttribute("name").getValue().equals("rh")).findFirst();
    assertThat(origVar.isPresent()).isFalse();

    final Optional<Element> modifiedVar =
        grids.stream().filter(e -> e.getAttribute("name").getValue().equals("ReletiveHumidity")).findFirst();
    assertThat(modifiedVar.isPresent()).isTrue();
    assertThat(modifiedVar.get().getAttribute("name").getValue()).isEqualTo("ReletiveHumidity");
    assertThat(modifiedVar.get().getAttribute("desc").getValue()).isEqualTo("relatively humid");
  }

  private void assertReturnsNcmlModifiedVariable(String servletPath) throws Exception {
    final RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get(servletPath).servletPath(servletPath).param("var", "all");
    final MvcResult mvcResult =
        mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    try (NetcdfFile ncf = NetcdfFiles.openInMemory("ncmlTest.nc", mvcResult.getResponse().getContentAsByteArray())) {
      final Variable origVar = ncf.findVariable("rh");
      assertThat((Object) origVar).isNull();

      final Variable modifiedVar = ncf.findVariable("ReletiveHumidity");
      assertThat((Object) modifiedVar).isNotNull();

      final Attribute att = modifiedVar.findAttribute("long_name");
      assertThat(att).isNotNull();
      assertThat(att.getStringValue()).isEqualTo("relatively humid");
    }
  }
}

