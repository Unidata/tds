package thredds.tds;

import static com.google.common.truth.Truth.assertThat;

import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import thredds.test.util.TestOnLocalServer;

public class TestS3Ncml {
  final private static String S3_NCML_PATH = "dodsC/s3-dataset-scan/ncml/";

  @Test
  public void shouldOpenNcmlOnS3() {
    final String endpoint = TestOnLocalServer.withHttpPath(S3_NCML_PATH + "testStandalone.ncml.ascii?time");
    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK);
    final String stringContent = new String(content, StandardCharsets.UTF_8);

    assertThat(stringContent).contains("time[2]");
    assertThat(stringContent).contains("6, 18");
  }

  @Test
  public void shouldOpenNcmlWithXmlExtensionOnS3() {
    final String endpoint = TestOnLocalServer.withHttpPath(S3_NCML_PATH + "testStandalone.xml.ascii?time");
    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK);
    final String stringContent = new String(content, StandardCharsets.UTF_8);

    assertThat(stringContent).contains("time[2]");
    assertThat(stringContent).contains("6, 18");
  }

  @Test
  public void shouldOpenNcmlWithOtherExtensionOnS3() {
    // Can't currently open an S3 NcML file with an extension other than xml or ncml.
    // Peaking inside the file to check if it's ncml is too slow
    final String endpoint = TestOnLocalServer.withHttpPath(S3_NCML_PATH + "testStandaloneNcml.otherExt.ascii?time");
    TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }

  @Test
  public void shouldOpenAggregationWithRelativePathsOnS3() {
    final String endpoint = TestOnLocalServer.withHttpPath(S3_NCML_PATH + "nc/namExtract/test_agg.ncml.ascii?time");
    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK);
    final String stringContent = new String(content, StandardCharsets.UTF_8);

    assertThat(stringContent).contains("time[8]");
    assertThat(stringContent).contains("3.0, 6.0, 9.0, 12.0, 15.0, 18.0, 21.0, 24.0");
  }

  @Test
  public void shouldOpenAggregationWithAbsolutePathsOnS3() {
    final String endpoint =
        TestOnLocalServer.withHttpPath(S3_NCML_PATH + "nc/namExtract/test_agg_absolute_paths.ncml.ascii?time");
    final byte[] content = TestOnLocalServer.getContent(endpoint, HttpServletResponse.SC_OK);
    final String stringContent = new String(content, StandardCharsets.UTF_8);

    assertThat(stringContent).contains("time[8]");
    assertThat(stringContent).contains("3.0, 6.0, 9.0, 12.0, 15.0, 18.0, 21.0, 24.0");
  }
}
