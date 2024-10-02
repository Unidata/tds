package thredds.server.radar;

import jakarta.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import thredds.test.util.TestOnLocalServer;
import thredds.util.xml.XmlUtil;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@Category(NeedsCdmUnitTest.class)
@RunWith(Parameterized.class)
public class TestRadarServerQuery {
  private static final Namespace NS =
      Namespace.getNamespace("ns", "http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0");

  private final String l2Url;
  private final String l3Url;

  @Parameterized.Parameters(name = "{0}")
  public static List<String> getTestParameters() {
    return Arrays.asList("", "s3/");
  }

  public TestRadarServerQuery(String datasetPathPrefix) {
    l2Url = "/radarServer/" + datasetPathPrefix + "nexrad/level2/IDD";
    l3Url = "/radarServer/" + datasetPathPrefix + "nexrad/level3/IDD";
  }

  @Test
  public void shouldReturnAllDatasetsForStation() throws IOException, JDOMException {
    String endpoint = l2Url + "?stn=KDGX&temporal=all";
    verifyNumberOfDatasets(endpoint, 3);
  }

  @Test
  public void shouldReturnZeroDatasetsForNonOverlappingTimeRange() throws IOException, JDOMException {
    String endpoint = l2Url + "?stn=KDGX&time_start=2000-01-01T12:00:00&time_end=2001-01-01T12:00:00";
    verifyNumberOfDatasets(endpoint, 0);
  }

  @Test
  public void shouldReturnOneDatasetForOverlappingTimeRange() throws IOException, JDOMException {
    String endpoint = l2Url + "?stn=KDGX&time_start=2014-06-02T23:52:00&time_end=2014-06-02T23:53:00";
    verifyNumberOfDatasets(endpoint, 1);
  }

  @Test
  public void shouldReturnOneDatasetForOverlappingTimeDuration() throws IOException, JDOMException {
    String endpoint = l2Url + "?stn=KDGX&time_start=2014-06-02T23:52:00&time_duration=PT1M";
    verifyNumberOfDatasets(endpoint, 1);
  }

  @Test
  public void shouldReturnOneDatasetForTime() throws IOException, JDOMException {
    String endpoint = l2Url + "?stn=KDGX&time=2014-06-02T23:52:00";
    verifyNumberOfDatasets(endpoint, 1);
  }

  @Test
  public void shouldReturnZeroDatasetsForNonExistentStation() throws IOException, JDOMException {
    String endpoint = l2Url + "?stn=ABCD&temporal=all";
    verifyNumberOfDatasets(endpoint, 0);
  }

  @Test
  public void shouldReturnErrorForNonOverlappingBox() throws IOException, JDOMException {
    String endpoint = l2Url + "?north=10&south=0&west=-100&east=-80&temporal=all";
    byte[] result = TestOnLocalServer.getContent(TestOnLocalServer.withHttpPath(endpoint), HttpServletResponse.SC_OK,
        "text/plain;charset=iso-8859-1");
    assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("No stations found for specified coordinates.");
  }

  @Test
  public void shouldReturnAllDatasetsForOverlappingBox() throws IOException, JDOMException {
    String endpoint = l2Url + "?north=50&south=30&west=-100&east=-80&temporal=all";
    verifyNumberOfDatasets(endpoint, 3);
  }

  @Test
  public void shouldReturnAllDatasetsForLonLat() throws IOException, JDOMException {
    String endpoint = l2Url + "?latitude=30&longitude=-90&temporal=all";
    verifyNumberOfDatasets(endpoint, 3);
  }

  @Test
  public void shouldReturnAllLevel3Datasets() throws IOException, JDOMException {
    String endpoint = l3Url + "?temporal=all&var=N0R&stn=UDX";
    verifyNumberOfDatasets(endpoint, 329);
  }

  @Test
  public void shouldReturnErrorWithoutVar() throws IOException, JDOMException {
    String endpoint = l3Url + "?temporal=all&stn=UDX";
    byte[] result = TestOnLocalServer.getContent(TestOnLocalServer.withHttpPath(endpoint), HttpServletResponse.SC_OK,
        "text/plain;charset=iso-8859-1");
    assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("One or more variables required.");
  }

  private static void verifyNumberOfDatasets(String endpoint, int expectedNumber) throws IOException, JDOMException {
    byte[] result = TestOnLocalServer.getContent(TestOnLocalServer.withHttpPath(endpoint), HttpServletResponse.SC_OK,
        "application/xml");

    Document doc = XmlUtil.getStringResponseAsDoc(result);

    XPathExpression<Element> xpathDoc = XPathFactory.instance()
        .compile("ns:catalog/ns:dataset/ns:metadata/ns:documentation", Filters.element(), null, NS);
    Element documentation = xpathDoc.evaluateFirst(doc);
    assertThat(documentation.getTextTrim()).isEqualTo(expectedNumber + " datasets found for query");

    XPathExpression<Element> xpathDatasets =
        XPathFactory.instance().compile("ns:catalog/ns:dataset/ns:dataset", Filters.element(), null, NS);
    List<Element> datasets = xpathDatasets.evaluate(doc);
    assertThat(datasets.size()).isEqualTo(expectedNumber);
  }
}
