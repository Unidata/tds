package thredds.server.services;

import java.nio.charset.StandardCharsets;
import org.apache.http.HttpStatus;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import ucar.unidata.util.test.category.NotPullRequest;

/**
 * Test Admin services, needs authentication
 *
 * @author caron
 * @since 7/6/2015
 */
@Category(NotPullRequest.class)
@RunWith(Parameterized.class)
public class TestAdminDebug {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static String urlPrefix = "https://localhost:8443/thredds/";
  private static Credentials goodCred = new UsernamePasswordCredentials("tds", "secret666");
  private static Credentials badCred = new UsernamePasswordCredentials("bad", "worse");

  @Parameterized.Parameters(name = "{0}")
  public static List<Object[]> getTestParameters() {
    List<Object[]> result = new ArrayList<>(10);
    result.add(new Object[] {"admin/debug?General/showTdsContext"});
    result.add(new Object[] {"admin/dir/content/thredds/logs/"});
    // TODO serverBaseDir_tomcat10/logs does not exist as logDir can no longer be configured through gretty
    // result.add(new Object[] {"admin/dir/logs/"});
    result.add(new Object[] {"admin/dir/catalogs/"});
    result.add(new Object[] {"admin/spring/showControllers"});
    return result;
  }

  ///////////////////////////////

  String path;

  public TestAdminDebug(String path) {
    this.path = path;
  }

  @Test
  public void testOpenHtml() {
    String endpoint = urlPrefix + path;
    byte[] response = TestOnLocalServer.getContent(goodCred, endpoint, new int[] {200}, ContentType.html);
    if (response != null) {
      logger.debug(new String(response, StandardCharsets.UTF_8));
    }
  }

  @Test
  public void testOpenHtmlFail() {
    String endpoint = urlPrefix + path;
    byte[] response = TestOnLocalServer.getContent(badCred, endpoint,
        new int[] {HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_FORBIDDEN}, ContentType.html);

    if (response != null) {
      logger.debug(new String(response, StandardCharsets.UTF_8));
    }
  }
}
