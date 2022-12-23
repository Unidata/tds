/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE.txt for license information.
 */

package thredds.server.wms;

import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

import static com.google.common.truth.Truth.assertThat;

@Category(NeedsCdmUnitTest.class)
public class TestWmsServer {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Namespace NS_WMS = Namespace.getNamespace("wms", "http://www.opengis.net/wms");

  @Test
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
  public void testGetPng() {
    String endpoint =
        TestOnLocalServer.withHttpPath("/wms/scanCdmUnitTests/conventions/cf/ipcc/tas_A1.nc?service=WMS&version=1.3.0"
            + "&request=GetMap&CRS=CRS:84&WIDTH=512&HEIGHT=512&LAYERS=tas&BBOX=0,-90,360,90&format="
            + ContentType.png.toString() + "&time=1850-01-16T12:00:00Z");
    byte[] result = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.png.toString());
    // make sure we get a png back
    // first byte (unsigned) should equal 137 (decimal)
    assertThat(result[0] & 0xFF).isEqualTo(137);
    // bytes 1, 2, and 3, when interperted as ASCII, should be P N G
    assertThat(new String(((byte[]) Arrays.copyOfRange(result, 1, 4)), Charset.forName("US-ASCII"))).isEqualTo("PNG");
  }

  @Test
  public void testGetLegendGraphic() {
    String endpoint =
        TestOnLocalServer.withHttpPath("/wms/scanCdmUnitTests/conventions/cf/ipcc/tas_A1.nc?service=WMS&version=1.3.0"
            + "&request=GetLegendGraphic&Layers=tas&colorscalerange=225.0,310.0&style=default-scalar/x-Rainbow&format="
            + ContentType.png.toString());
    byte[] result = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.png.toString());
    // make sure we get a png back
    // first byte (unsigned) should equal 137 (decimal)
    assertThat(result[0] & 0xFF).isEqualTo(137);
    // bytes 1, 2, and 3, when interperted as ASCII, should be P N G
    assertThat(new String(((byte[]) Arrays.copyOfRange(result, 1, 4)), Charset.forName("US-ASCII"))).isEqualTo("PNG");
  }

  @Test
  public void testGetLegendGraphicWithSLD() {
    System.setProperty("httpservices.urlencode", "false");
    try {
      String endpoint = TestOnLocalServer.withHttpPath(
          "/wms/scanCdmUnitTests/conventions/cf/ipcc/tas_A1.nc?service=WMS&version=1.3.0&request=GetLegendGraphic&Layers=tas&format="
              + ContentType.png.toString()
              + "&SLD_BODY=%3C%3Fxml%20version%3D%221.0%22%20encoding%3D%22ISO-8859-1%22%3F%3E%20%3CStyledLayerDescriptor%20version%3D%221.1.0%22%20xsi%3AschemaLocation%3D%22http%3A%2F%2Fwww.opengis.net%2Fsld%20StyledLayerDescriptor.xsd%22%20xmlns%3D%22http%3A%2F%2Fwww.opengis.net%2Fsld%22%20xmlns%3Aogc%3D%22http%3A%2F%2Fwww.opengis.net%2Fogc%22%20xmlns%3Ase%3D%22http%3A%2F%2Fwww.opengis.net%2Fse%22%20xmlns%3Axlink%3D%22http%3A%2F%2Fwww.w3.org%2F1999%2Fxlink%22%20xmlns%3Axsi%3D%22http%3A%2F%2Fwww.w3.org%2F2001%2FXMLSchema-instance%22%3E%20%3CNamedLayer%3E%20%3Cse%3AName%3Etas%3C%2Fse%3AName%3E%20%3CUserStyle%3E%20%3Cse%3AName%3EThesholded%20colour%20scheme%3C%2Fse%3AName%3E%20%3Cse%3ACoverageStyle%3E%20%3Cse%3ARule%3E%20%3Cse%3ARasterSymbolizer%3E%20%3Cse%3AOpacity%3E1.0%3C%2Fse%3AOpacity%3E%20%3Cse%3AColorMap%3E%20%3Cse%3ACategorize%20fallbackValue%3D%22%2300000000%22%3E%20%3Cse%3ALookupValue%3ERasterdata%3C%2Fse%3ALookupValue%3E%20%3Cse%3AValue%3E%23FF0000FF%3C%2Fse%3AValue%3E%20%3Cse%3AThreshold%3E275.0%3C%2Fse%3AThreshold%3E%20%3Cse%3AValue%3E%23FF00FFFF%3C%2Fse%3AValue%3E%20%3Cse%3AThreshold%3E280.0%3C%2Fse%3AThreshold%3E%20%3Cse%3AValue%3E%23FF00FF00%3C%2Fse%3AValue%3E%20%3Cse%3AThreshold%3E285.0%3C%2Fse%3AThreshold%3E%20%3Cse%3AValue%3E%23FFFFFF00%3C%2Fse%3AValue%3E%20%3Cse%3AThreshold%3E290.0%3C%2Fse%3AThreshold%3E%20%3Cse%3AValue%3E%23FFFFC800%3C%2Fse%3AValue%3E%20%3Cse%3AThreshold%3E295.0%3C%2Fse%3AThreshold%3E%20%3Cse%3AValue%3E%23FFFFAFAF%3C%2Fse%3AValue%3E%20%3Cse%3AThreshold%3E300.0%3C%2Fse%3AThreshold%3E%20%3Cse%3AValue%3E%23FFFF0000%3C%2Fse%3AValue%3E%20%3C%2Fse%3ACategorize%3E%20%3C%2Fse%3AColorMap%3E%20%3C%2Fse%3ARasterSymbolizer%3E%20%3C%2Fse%3ARule%3E%20%3C%2Fse%3ACoverageStyle%3E%20%3C%2FUserStyle%3E%20%3C%2FNamedLayer%3E%20%3C%2FStyledLayerDescriptor%3E");
      byte[] result = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.png.toString());
      // make sure we get a png back
      // first byte (unsigned) should equal 137 (decimal)
      assertThat(result[0] & 0xFF).isEqualTo(137);
      // bytes 1, 2, and 3, when interperted as ASCII, should be P N G
      assertThat(new String(((byte[]) Arrays.copyOfRange(result, 1, 4)), Charset.forName("US-ASCII"))).isEqualTo("PNG");
    } finally {
      System.setProperty("httpservices.urlencode", "true");
    }
  }

  @Test
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
  public void shouldGetCapabilitiesForDatasetScanWithNcml() {
    final String endpoint = TestOnLocalServer
        .withHttpPath("/wms/ModifyDatasetScan/revOceanDJF2.nc?service=WMS&version=1.3.0&request=GetCapabilities");
    final byte[] result = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, ContentType.xmlwms);
    assertThat(result).isNotEmpty();
  }
}
