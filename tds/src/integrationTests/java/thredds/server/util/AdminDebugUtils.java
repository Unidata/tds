package thredds.server.util;

import javax.servlet.http.HttpServletResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import thredds.test.util.TestOnLocalServer;
import thredds.util.ContentType;

public class AdminDebugUtils {
  public static final String urlPrefix = "https://localhost:8443/thredds/";
  public static final Credentials goodCred = new UsernamePasswordCredentials("tds", "secret666");

  public static void disableRafCache() {
    final String endpoint = urlPrefix + "admin/debug?Caches/disableRAFCache";
    TestOnLocalServer.getContent(goodCred, endpoint, new int[] {HttpServletResponse.SC_OK}, ContentType.html);
  }

  public static void enableRafCache() {
    final String endpoint = urlPrefix + "admin/debug?Caches/enableRAFCache";
    TestOnLocalServer.getContent(goodCred, endpoint, new int[] {HttpServletResponse.SC_OK}, ContentType.html);
  }
}
