package thredds.server.wms;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import thredds.client.catalog.Dataset;
import thredds.client.catalog.ServiceType;
import thredds.server.viewer.ViewerLinkProvider;
import thredds.util.StringValidateEncodeUtils;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLDecoder;

import static thredds.client.catalog.Dataset.makeStandalone;
import static com.google.common.truth.Truth.assertThat;

public class TestGodiva3Viewer {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Test
  public void testGodivaUrlEncode() throws UnsupportedEncodingException {
    String filePath = "test/filenameWith+sign.nc";

    Dataset ds = makeStandalone(filePath, "", "", ServiceType.WMS.name());

    final String uri = "/thredds/catalog/catalog.html";
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);

    Godiva3Viewer viewer = new Godiva3Viewer();
    ViewerLinkProvider.ViewerLink link = viewer.getViewerLink(ds, request);

    String decodedUrl = URLDecoder.decode(link.getUrl(), StringValidateEncodeUtils.CHARACTER_ENCODING_UTF_8);

    assertThat(decodedUrl).contains(filePath);

  }

}
