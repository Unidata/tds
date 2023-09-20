package thredds.server.threddsIso;

import static com.google.common.truth.Truth.assertWithMessage;

import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;

public class TestThreddsIsoServices {
  @Test
  public void shouldReturnNcml() {
    final String path = "ncml/scanLocal/testgrid1.nc";
    final String expectedOutput = "testgrid1.ncml.xml";
    compare(path, expectedOutput, ContentType.xml);
  }

  @Test
  public void shouldReturnIso() {
    final String path = "iso/scanLocal/testgrid1.nc";
    final String expectedOutput = "testgrid1.iso.xml";
    compare(path, expectedOutput, ContentType.xml);
  }

  @Test
  public void shouldReturnUddc() {
    final String path = "uddc/scanLocal/testgrid1.nc";
    final String expectedOutput = "testgrid1.uddc.html";
    compare(path, expectedOutput, ContentType.html);
  }

  private void compare(String path, String expectedOutputFilename, ContentType expectedType) {
    final String endpoint = TestOnLocalServer.withHttpPath(path);
    final byte[] response = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK, expectedType);

    final Diff diff = DiffBuilder.compare(Input.fromStream(getClass().getResourceAsStream(expectedOutputFilename)))
        .withTest(Input.fromByteArray(response)).ignoreComments().normalizeWhitespace()
        // don't compare iso element with version and current datetime in it
        .withNodeFilter(node -> !node.getTextContent().startsWith("This record was translated from NcML using"))
        .build();
    assertWithMessage(diff.toString()).that(diff.hasDifferences()).isFalse();
  }
}
