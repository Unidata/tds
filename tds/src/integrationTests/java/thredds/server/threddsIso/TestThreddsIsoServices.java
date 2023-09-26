package thredds.server.threddsIso;

import static com.google.common.truth.Truth.assertWithMessage;

import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.util.Predicate;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;

public class TestThreddsIsoServices {
  @Test
  public void shouldReturnNcml() {
    final String path = "ncml/scanLocal/testgrid1.nc";
    final String expectedOutput = "testgrid1.ncml.xml";
    final Predicate<Node> filter = node -> !(node.hasAttributes() && node.getAttributes().getNamedItem("name") != null
        && node.getAttributes().getNamedItem("name").getNodeValue().equals("metadata_creation"));
    compare(path, expectedOutput, ContentType.xml, filter);
  }

  @Test
  public void shouldReturnIso() {
    final String path = "iso/scanLocal/testgrid1.nc";
    final String expectedOutput = "testgrid1.iso.xml";
    final Predicate<Node> filter =
        node -> !node.getTextContent().startsWith("This record was translated from NcML using")
            && !node.getNodeName().startsWith("gco:Date");
    compare(path, expectedOutput, ContentType.xml, filter);
  }

  @Test
  public void shouldReturnUddc() {
    final String path = "uddc/scanLocal/testgrid1.nc";
    final String expectedOutput = "testgrid1.uddc.html";
    compare(path, expectedOutput, ContentType.html, node -> true);
  }

  private void compare(String path, String expectedOutputFilename, ContentType expectedType, Predicate<Node> filter) {
    final String endpoint = TestOnLocalServer.withHttpPath(path);
    final byte[] response = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, expectedType);

    final Diff diff = DiffBuilder.compare(Input.fromStream(getClass().getResourceAsStream(expectedOutputFilename)))
        .withTest(Input.fromByteArray(response)).ignoreComments().normalizeWhitespace()
        // don't compare elements with e.g. version/ current datetime
        .withNodeFilter(filter).build();
    assertWithMessage(diff.toString()).that(diff.hasDifferences()).isFalse();
  }
}
