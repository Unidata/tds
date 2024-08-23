package thredds.server.opendap;

import static com.google.common.truth.Truth.assertThat;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.test.context.web.WebAppConfiguration;
import thredds.mock.web.MockTdsContextLoader;
import ucar.nc2.constants.CDM;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import jakarta.servlet.ServletConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml"}, loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class OpendapServletTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private ServletConfig servletConfig;

  private OpendapServlet opendapServlet;
  private final String path = "/gribCollection/GFS_CONUS_80km/GFS_CONUS_80km_20120229_1200.grib1";

  @Before
  public void setUp() throws Exception {
    opendapServlet = new OpendapServlet();
    opendapServlet.init(servletConfig);
    opendapServlet.init();
  }

  @Test
  public void asciiDataRequestTest() {
    String mockURI = "/thredds/dodsC" + path + ".ascii";
    String mockQueryString = "Temperature_height_above_ground[0:1:0][0:1:0][41][31]";
    MockHttpServletRequest request = new MockHttpServletRequest("GET", mockURI);
    request.setContextPath("/thredds");
    request.setQueryString(mockQueryString);
    request.setPathInfo(path + ".ascii");
    MockHttpServletResponse response = new MockHttpServletResponse();
    opendapServlet.doGet(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
  }

  @Test
  public void asciiDataRequestTest2() throws UnsupportedEncodingException {
    String mockURI = "/thredds/dodsC" + path + ".ascii";
    String mockQueryString = "Temperature_height_above_ground[0:1:0][0:1:0][0:10:64][0:10:92]";
    MockHttpServletRequest request = new MockHttpServletRequest("GET", mockURI);
    request.setContextPath("/thredds");
    request.setQueryString(mockQueryString);
    request.setPathInfo(path + ".ascii");
    MockHttpServletResponse response = new MockHttpServletResponse();
    opendapServlet.doGet(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);

    String strResponse = response.getContentAsString();
    logger.debug(strResponse);
  }

  @Test
  public void shouldReturnAttributesWithEmptyAndNullValues() throws UnsupportedEncodingException {
    final String path = "/scanLocal/testEmptyAndNullAttributes.nc4.html";
    final MockHttpServletResponse response = mockGetRequest(path);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);

    final String stringResponse = response.getContentAsString();
    assertThat(stringResponse).contains("emptyString");
    assertThat(stringResponse).contains("nullString");
    assertThat(stringResponse).contains("globalEmptyString");
    assertThat(stringResponse).contains("globalNullString");
  }

  @Test
  public void shouldNotContainNCPropertiesAttribute() throws IOException {
    final String path = "/scanLocal/testEmptyAndNullAttributes.nc4.html";
    final MockHttpServletResponse response = mockGetRequest(path);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);

    final String stringResponse = response.getContentAsString();
    assertThat(stringResponse).doesNotContain(CDM.NCPROPERTIES);
  }

  @Test
  public void dodsDataRequestTest() throws IOException {
    String mockURI = "/thredds/dodsC" + path + ".dods";
    String mockQueryString = "Temperature_height_above_ground[0:1:0][0:1:0][41][31]";
    MockHttpServletRequest request = new MockHttpServletRequest("GET", mockURI);
    request.setContextPath("/thredds");
    request.setQueryString(mockQueryString);
    request.setPathInfo(path + ".dods");
    MockHttpServletResponse response = new MockHttpServletResponse();
    opendapServlet.doGet(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    // not set by servlet mocker :: assertEquals("application/octet-stream", response.getContentType());

    String strResponse = response.getContentAsString();
    logger.debug(strResponse);
  }

  @Test
  public void shouldReturnBadRequestForMalformedTime() {
    final String path = "/testGFSfmrc/runs/GFS_CONUS_80km_nc_RUN_2012-19T00:00:00Z.html";
    final MockHttpServletResponse response = mockGetRequest(path);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
  }

  private MockHttpServletResponse mockGetRequest(String path) {
    final String mockURI = "/thredds/dodsC" + path;
    MockHttpServletRequest request = new MockHttpServletRequest("GET", mockURI);
    request.setContextPath("/thredds");
    request.setPathInfo(path);
    final MockHttpServletResponse response = new MockHttpServletResponse();
    opendapServlet.doGet(request, response);
    return response;
  }
}
