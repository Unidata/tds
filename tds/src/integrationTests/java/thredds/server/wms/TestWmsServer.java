/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE.txt for license information.
 */

package thredds.server.wms;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.*;
import jakarta.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;
import ucar.httpservices.HTTPException;
import ucar.httpservices.HTTPFactory;
import ucar.httpservices.HTTPSession;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

public class TestWmsServer {

  private static final double TOLERANCE = 1.0e-8;
  private final Namespace NS_WMS = Namespace.getNamespace("wms", "http://www.opengis.net/wms");

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void testCapabilities() throws IOException, JDOMException {
    String endpoint = TestOnLocalServer.withHttpPath(
        "/wms/scanCdmUnitTests/conventions/coards/sst.mnmean.nc?service=WMS&version=1.3.0&request=GetCapabilities");
    byte[] result = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.xmlwms);
    Reader in = new StringReader(new String(result, StandardCharsets.UTF_8));
    SAXBuilder sb = new SAXBuilder();
    sb.setExpandEntities(false);
    Document doc = sb.build(in);

    XPathExpression<Element> xpath = XPathFactory.instance().compile("//wms:Capability/wms:Layer/wms:Layer/wms:Layer",
        Filters.element(), null, NS_WMS);
    List<Element> elements = xpath.evaluate(doc);
    assertThat(elements.size()).isEqualTo(1);

    XPathExpression<Element> xpath2 = XPathFactory.instance()
        .compile("//wms:Capability/wms:Layer/wms:Layer/wms:Layer/wms:Name", Filters.element(), null, NS_WMS);
    Element emt = xpath2.evaluateFirst(doc);
    assertThat(emt.getTextTrim()).isEqualTo("sst");
  }

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void testGetPng() {
    String endpoint =
        TestOnLocalServer.withHttpPath("/wms/scanCdmUnitTests/conventions/cf/ipcc/tas_A1.nc?service=WMS&version=1.3.0"
            + "&request=GetMap&CRS=CRS:84&WIDTH=512&HEIGHT=512&LAYERS=tas&BBOX=0,-90,360,90&format=" + ContentType.png
            + "&time=1850-01-16T12:00:00Z");
    testGetPng(endpoint);
  }

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void testGetPngInAnotherProjection() {
    String endpoint =
        TestOnLocalServer.withHttpPath("/wms/scanCdmUnitTests/conventions/cf/ipcc/tas_A1.nc?service=WMS&version=1.3.0"
            + "&request=GetMap&CRS=EPSG:3857&WIDTH=512&HEIGHT=512&LAYERS=tas&BBOX=0,-90,360,90&format="
            + ContentType.png + "&time=1850-01-16T12:00:00Z");
    testGetPng(endpoint);
  }

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void testGetMapPaletteDefault() {
    String basePath = "/wms/scanLocal/2004050300_eta_211.nc?TRANSPARENT=TRUE&LAYERS=Z_sfc&"
        + "TIME=2003-09-25T00%3A00%3A00.000Z&COLORSCALERANGE=0%2C2920&NUMCOLORBANDS=20&ABOVEMAXCOLOR=0x000000&"
        + "BELOWMINCOLOR=0xFF0000FF&BGCOLOR=transparent&LOGSCALE=false&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&"
        + "SRS=EPSG%3A4326&BBOX=-171.36,32.88,-48.48,155.76&WIDTH=256&HEIGHT=256&format=" + ContentType.png;

    String endpointNoStyles = TestOnLocalServer.withHttpPath(basePath);
    String endpointEmptyStyles = TestOnLocalServer.withHttpPath(basePath + "&STYLES=");
    String endpointExpectedStyles = TestOnLocalServer.withHttpPath(basePath + "&STYLES=default-scalar%2Fx-Occam");
    String endpointDiffStyles = TestOnLocalServer.withHttpPath(basePath + "&STYLES=default-scalar%2Fx-Rainbow");

    assertThat(testGetPng(endpointNoStyles)).isEqualTo(testGetPng(endpointExpectedStyles));
    assertThat(testGetPng(endpointEmptyStyles)).isEqualTo(testGetPng(endpointExpectedStyles));
    assertThat(testGetPng(endpointDiffStyles)).isNotEqualTo(testGetPng(endpointExpectedStyles));
  }

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void testGetLegendGraphic() {
    String endpoint =
        TestOnLocalServer.withHttpPath("/wms/scanCdmUnitTests/conventions/cf/ipcc/tas_A1.nc?service=WMS&version=1.3.0"
            + "&request=GetLegendGraphic&Layers=tas&colorscalerange=225.0,310.0&style=default-scalar/x-Rainbow&format="
            + ContentType.png);

    testGetPng(endpoint);
  }

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void testGetLegendGraphicWithSLD() {
    System.setProperty("httpservices.urlencode", "false");
    try {
      String endpoint = TestOnLocalServer.withHttpPath(
          "/wms/scanCdmUnitTests/conventions/cf/ipcc/tas_A1.nc?service=WMS&version=1.3.0&request=GetLegendGraphic&Layers=tas&format="
              + ContentType.png
              + "&SLD_BODY=%3C%3Fxml%20version%3D%221.0%22%20encoding%3D%22ISO-8859-1%22%3F%3E%20%3CStyledLayerDescriptor%20version%3D%221.1.0%22%20xsi%3AschemaLocation%3D%22http%3A%2F%2Fwww.opengis.net%2Fsld%20StyledLayerDescriptor.xsd%22%20xmlns%3D%22http%3A%2F%2Fwww.opengis.net%2Fsld%22%20xmlns%3Aogc%3D%22http%3A%2F%2Fwww.opengis.net%2Fogc%22%20xmlns%3Ase%3D%22http%3A%2F%2Fwww.opengis.net%2Fse%22%20xmlns%3Axlink%3D%22http%3A%2F%2Fwww.w3.org%2F1999%2Fxlink%22%20xmlns%3Axsi%3D%22http%3A%2F%2Fwww.w3.org%2F2001%2FXMLSchema-instance%22%3E%20%3CNamedLayer%3E%20%3Cse%3AName%3Etas%3C%2Fse%3AName%3E%20%3CUserStyle%3E%20%3Cse%3AName%3EThesholded%20colour%20scheme%3C%2Fse%3AName%3E%20%3Cse%3ACoverageStyle%3E%20%3Cse%3ARule%3E%20%3Cse%3ARasterSymbolizer%3E%20%3Cse%3AOpacity%3E1.0%3C%2Fse%3AOpacity%3E%20%3Cse%3AColorMap%3E%20%3Cse%3ACategorize%20fallbackValue%3D%22%2300000000%22%3E%20%3Cse%3ALookupValue%3ERasterdata%3C%2Fse%3ALookupValue%3E%20%3Cse%3AValue%3E%23FF0000FF%3C%2Fse%3AValue%3E%20%3Cse%3AThreshold%3E275.0%3C%2Fse%3AThreshold%3E%20%3Cse%3AValue%3E%23FF00FFFF%3C%2Fse%3AValue%3E%20%3Cse%3AThreshold%3E280.0%3C%2Fse%3AThreshold%3E%20%3Cse%3AValue%3E%23FF00FF00%3C%2Fse%3AValue%3E%20%3Cse%3AThreshold%3E285.0%3C%2Fse%3AThreshold%3E%20%3Cse%3AValue%3E%23FFFFFF00%3C%2Fse%3AValue%3E%20%3Cse%3AThreshold%3E290.0%3C%2Fse%3AThreshold%3E%20%3Cse%3AValue%3E%23FFFFC800%3C%2Fse%3AValue%3E%20%3Cse%3AThreshold%3E295.0%3C%2Fse%3AThreshold%3E%20%3Cse%3AValue%3E%23FFFFAFAF%3C%2Fse%3AValue%3E%20%3Cse%3AThreshold%3E300.0%3C%2Fse%3AThreshold%3E%20%3Cse%3AValue%3E%23FFFF0000%3C%2Fse%3AValue%3E%20%3C%2Fse%3ACategorize%3E%20%3C%2Fse%3AColorMap%3E%20%3C%2Fse%3ARasterSymbolizer%3E%20%3C%2Fse%3ARule%3E%20%3C%2Fse%3ACoverageStyle%3E%20%3C%2FUserStyle%3E%20%3C%2FNamedLayer%3E%20%3C%2FStyledLayerDescriptor%3E");
      byte[] result = testGetPng(endpoint);
    } finally {
      System.setProperty("httpservices.urlencode", "true");
    }
  }

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void shouldGetMapForAggregationVariableThatDoesNotDependOnAggregationDimension() {
    final String endpoint = TestOnLocalServer.withHttpPath("/wms/aggJoinExisting?FORMAT=image/png&TRANSPARENT=TRUE"
        + "&STYLES=default-scalar/psu-viridis&LAYERS=Visibility&TIME=2006-09-26T00:00:00.000Z&COLORSCALERANGE=-50,50"
        + "&NUMCOLORBANDS=20&ABOVEMAXCOLOR=extend&BELOWMINCOLOR=extend&BGCOLOR=extend&LOGSCALE=false&SERVICE=WMS"
        + "&VERSION=1.1.1&REQUEST=GetMap&SRS=EPSG:4326"
        + "&BBOX=-71.317178725829,36.796624819867,-65.279244210597,42.834559335098&WIDTH=256&HEIGHT=256");
    final byte[] result = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.png.toString());
    assertThat(result).isNotEmpty();
  }

  @Test
  @Category(NeedsCdmUnitTest.class)
  public void shouldGetCapabilitiesForDatasetScanWithNcml() {
    final String endpoint = TestOnLocalServer
        .withHttpPath("/wms/ModifyDatasetScan/revOceanDJF2.nc?service=WMS&version=1.3.0&request=GetCapabilities");
    final byte[] result = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.xmlwms);
    assertThat(result).isNotEmpty();
  }

  @Test
  public void shouldApplyOffsetToData() throws IOException, JDOMException {
    final String datasetPath = "scanLocal/testOffset.nc";

    final String withOffsetEndpoint = createGetFeatureInfoEndpoint(datasetPath, "variableWithOffset");
    checkValue(withOffsetEndpoint, 7.5);

    final String withoutOffsetEndpoint = createGetFeatureInfoEndpoint(datasetPath, "variableWithoutOffset");
    checkValue(withoutOffsetEndpoint, 7.5);
  }

  @Test
  public void shouldApplyNcmlOffsetToData() throws IOException, JDOMException {
    final String datasetPath = "testOffsetWithNcml.nc";

    final String withOffsetEndpoint = createGetFeatureInfoEndpoint(datasetPath, "variableWithOffset");
    checkValue(withOffsetEndpoint, 7.5);

    final String withoutOffsetEndpoint = createGetFeatureInfoEndpoint(datasetPath, "variableWithoutOffset");
    checkValue(withoutOffsetEndpoint, -92.5);
  }

  @Test
  public void shouldGetMapInParallel() throws InterruptedException {
    final int nRequests = 100;
    final int nThreads = 10;

    final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
    final List<Callable<Integer>> tasks = new ArrayList<>();
    for (int i = 0; i < nRequests; i++) {
      tasks.add(this::getMap);
    }

    final List<Future<Integer>> results = executor.invokeAll(tasks);
    final List<Integer> resultCodes = results.stream().map(result -> {
      try {
        return result.get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    }).toList();

    assertWithMessage("result codes = " + Arrays.toString(resultCodes.toArray()))
        .that(resultCodes.stream().allMatch(code -> code.equals(HttpServletResponse.SC_OK))).isTrue();
  }

  private int getMap() {
    final String endpoint = TestOnLocalServer
        .withHttpPath("/wms/scanLocal/2004050300_eta_211.nc" + "?LAYERS=Z_sfc" + "&SERVICE=WMS" + "&VERSION=1.1.1"
            + "&REQUEST=GetMap" + "&SRS=EPSG%3A4326" + "&BBOX=-64,26,-35,55" + "&WIDTH=256" + "&HEIGHT=256");

    try (HTTPSession session = HTTPFactory.newSession(endpoint)) {
      return HTTPFactory.Get(session).execute();
    } catch (HTTPException e) {
      throw new RuntimeException(e);
    }
  }

  private String createGetFeatureInfoEndpoint(String path, String variableName) {
    return TestOnLocalServer.withHttpPath("/wms/" + path + "?LAYERS=" + variableName
        + "&service=WMS&version=1.3.0&CRS=CRS:84&BBOX=0,0,10,10&WIDTH=100&HEIGHT=100"
        + "&REQUEST=GetFeatureInfo&QUERY_LAYERS=" + variableName + "&i=0&j=0");
  }

  private void checkValue(String endpoint, double expectedValue) throws IOException, JDOMException {
    final byte[] result = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.xmlwms);

    final Reader reader = new StringReader(new String(result, StandardCharsets.UTF_8));
    final Document doc = new SAXBuilder().build(reader);
    final XPathExpression<Element> xpath = XPathFactory.instance().compile("//FeatureInfo/value", Filters.element());
    final Element element = xpath.evaluateFirst(doc);

    assertThat(element.getContentSize()).isEqualTo(1);
    assertThat(Double.valueOf(element.getText())).isWithin(TOLERANCE).of(expectedValue);
  }

  private static byte[] testGetPng(String endpoint) {
    byte[] result = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.png.toString());
    // make sure we get a png back
    // first byte (unsigned) should equal 137 (decimal)
    assertThat(result[0] & 0xFF).isEqualTo(137);
    // bytes 1, 2, and 3, when interpreted as ASCII, should be P N G
    assertThat(new String(Arrays.copyOfRange(result, 1, 4), StandardCharsets.US_ASCII)).isEqualTo("PNG");
    return result;
  }
}
