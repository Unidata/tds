package thredds.server.cdmremote;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.jdom2.Document;
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
import thredds.util.xml.NcmlParserUtil;
import thredds.util.xml.XmlUtil;
import ucar.nc2.stream.CdmRemote;
import ucar.nc2.stream.NcStream;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.io.ByteArrayInputStream;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;

@RunWith(SpringJUnit4ParameterizedClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml", "/WEB-INF/spring-servlet.xml"},
    loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class CdmrControllerTest {
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
    return Arrays.asList(new Object[][] {
        {"/cdmremote/NCOF/POLCOMS/IRISH_SEA/files/20060925_0600.nc", 5, 12, 16, "Precipitable_water(0:1,43:53,20:40)"}, // FMRC
        {"/cdmremote/testStationFeatureCollection/files/Surface_METAR_20060325_0000.nc", 9, 22, 47, "wind_speed(0:1)"}, // station
        {"/cdmremote/testBuoyFeatureCollection/files/Surface_Buoy_20130804_0000.nc", 5, 2, 58, "meanWind(0:1)"}, // point
        {"/cdmremote/testSurfaceSynopticFeatureCollection/files/Surface_Synoptic_20130804_0000.nc", 5, 2, 46,
            "humidity(0:1)"}, // point
        {"/cdmremote/s3-thredds-test-data/ncml/nc/namExtract/20060925_0600.nc", 5, 12, 16,
            "Precipitable_water(0:1,43:53,20:40)"}, // S3
    });
  }

  String path, dataReq;
  int ndims, natts, nvars;

  public CdmrControllerTest(String path, int ndims, int natts, int nvars, String dataReq) {
    this.path = path;
    this.ndims = ndims;
    this.natts = natts;
    this.nvars = nvars;
    this.dataReq = dataReq;
  }

  @Test
  public void cdmRemoteRequestCapabilitiesTest() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("req", "capabilities");

    logger.debug("{}?req=capabilities", path);

    MvcResult result = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(ContentType.xml.getContentHeader())).andReturn();

    /*
     * String content =
     * "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
     * "<cdmRemoteCapabilities location=\"http://localhost:80/cdmremote/NCOF/POLCOMS/IRISH_SEA/files/20060925_0600.nc\">"
     * +
     * "  <featureDataset type=\"GRID\" url=\"http://localhost:80/cdmremote/NCOF/POLCOMS/IRISH_SEA/files/20060925_0600.nc\" />"
     * +
     * "</cdmRemoteCapabilities>"; // LAME
     * 
     * assert content.equals(result.getResponse().getContentAsString());
     */
  }

  @Test
  public void cdmRemoteRequestCdlTest() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("req", "cdl");

    MvcResult result = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(ContentType.text.getContentHeader())).andReturn();

    // We want this statement to succeed without exception.
    // Throws NullPointerException if header doesn't exist
    // Throws IllegalArgumentException if header value is not a valid date.
    result.getResponse().getDateHeader("Last-Modified");
  }

  @Test
  public void cdmRemoteRequestNcmlTest() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("req", "ncml");

    MvcResult result = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(ContentType.xml.getContentHeader())).andReturn();

    // We want this statement to succeed without exception.
    // Throws NullPointerException if header doesn't exist
    // Throws IllegalArgumentException if header value is not a valid date.
    result.getResponse().getDateHeader("Last-Modified");

    Document doc = XmlUtil.getStringResponseAsDoc(result.getResponse());

    int hasDims = NcmlParserUtil.getNcmlElements("netcdf/dimension", doc).size();
    int hasAtts = NcmlParserUtil.getNcmlElements("netcdf/attribute", doc).size();
    int hasVars = NcmlParserUtil.getNcmlElements("//variable", doc).size();

    // Not really checking the content just the number of elements
    assertEquals(this.ndims, hasDims);
    assertEquals(this.natts, hasAtts);
    assertEquals(this.nvars, hasVars);
  }

  @Test
  public void cdmRemoteRequestHeaderTest() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("req", "header");

    MvcResult result = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(ContentType.binary.getContentHeader())).andReturn();

    // We want this statement to succeed without exception.
    // Throws NullPointerException if header doesn't exist
    // Throws IllegalArgumentException if header value is not a valid date.
    assertThat(result.getResponse().getDateHeader("Last-Modified")).isGreaterThan(0);

    // response is a ncstream
    ByteArrayInputStream bais = new ByteArrayInputStream(result.getResponse().getContentAsByteArray());
    CdmRemote cdmr = new CdmRemote(bais, "test");
    cdmr.close();
  }

  @Test
  public void cdmRemoteRequestDataTest() throws Exception {
    RequestBuilder rb = MockMvcRequestBuilders.get(path).servletPath(path).param("req", "data").param("var", dataReq);

    MvcResult result = this.mockMvc.perform(rb).andExpect(MockMvcResultMatchers.status().is(200))
        .andExpect(MockMvcResultMatchers.content().contentType(ContentType.binary.toString())).andReturn();

    ByteArrayInputStream bais = new ByteArrayInputStream(result.getResponse().getContentAsByteArray());
    assertTrue(NcStream.readAndTest(bais, NcStream.MAGIC_DATA2));
  }


  private boolean checkBytes(byte[] read, byte[] expected) {

    if (read.length != expected.length)
      return false;
    int count = 0;
    while (read[count] == expected[count] && count < expected.length - 1) {
      count++;
    }

    return count == expected.length - 1;
  }

}
