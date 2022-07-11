package thredds.tds;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.lang.invoke.MethodHandles;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;
import ucar.unidata.util.test.category.NotPullRequest;

/**
 * Tests a fresh installation of TDS. An installation is considered "fresh" if the "tds.content.root.path" property:
 * - points to a non-existent directory. In this case, TDS will create that directory, if it can.
 * - points to an existing directory that does not contain a "catalog.xml" file.
 *
 * @author cwardgar
 * @since 2017-04-21
 */
@Category(NotPullRequest.class)
public class TestFreshTdsInstall {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String propName = "tds.content.root.path";

  @Test
  public void shouldCopyOverDefaultStartUpFiles() {
    final String contentRootPath = System.getProperty(propName);
    assertThat(contentRootPath).isNotNull();
    final File threddsDirectory = new File(contentRootPath, "thredds");

    // TDS created 'threddsDirectory'
    assertThat(threddsDirectory.exists()).isTrue();

    // it copied over the default startup files
    // See TdsContext.afterPropertiesSet(), in the "Copy default startup files, if necessary" section.
    assertThat(new File(contentRootPath, "README.txt").exists()).isTrue();
    assertThat(new File(threddsDirectory, "README.txt").exists()).isTrue();
    assertThat(new File(threddsDirectory, "catalog.xml").exists()).isTrue();
    assertThat(new File(threddsDirectory, "enhancedCatalog.xml").exists()).isTrue();
    assertThat(new File(new File(threddsDirectory, "public"), "testdata").exists()).isTrue();
    assertThat(new File(threddsDirectory, "threddsConfig.xml").exists()).isTrue();
    assertThat(new File(threddsDirectory, "wmsConfig.xml").exists()).isTrue();

    // it created the 'logs' directory
    assertThat(new File(threddsDirectory, "logs").exists()).isTrue();
  }

  @Test
  public void shouldReturnExpectedClientCatalog() {
    // Identify control file for this test. It's located in src/freshInstallTest/resources/thredds/tds/
    final String controlFileName = "enhancedCatalog.xml";

    // retrieve the sever base URI (set by Gretty) and construct catalog endpoint with it
    final String preferredBaseURI = System.getProperty("gretty.preferredBaseURI");
    assertThat(preferredBaseURI).isNotNull();
    final String endpoint = preferredBaseURI + "/catalog/enhancedCatalog.xml";

    // server responds with HTTP code 200 and XML content. method contains JUnit assertions
    final byte[] response = TestOnLocalServer.getContent(endpoint, 200, ContentType.xml);

    // compare expected XML (read from test resource) with server response, ignoring comments and whitespace
    final Diff diff = DiffBuilder.compare(Input.fromStream(getClass().getResourceAsStream(controlFileName)))
        .withTest(Input.fromByteArray(response)).ignoreComments().normalizeWhitespace().build();
    assertThat(diff.hasDifferences()).isFalse();
  }
}
