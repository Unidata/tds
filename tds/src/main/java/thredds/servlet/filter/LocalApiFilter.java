/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import thredds.util.LocalApiSigner;

/**
 * Handle access to the local api of the TDS.
 * the TDS.
 * <p/>
 * <p/>
 * This is an attempt to filter out and reject requests not originating on the
 * same machine as the TDS. This implementation is slightly more restrictive,
 * in that it will filter out requests not originating from the same network
 * interface as was received by the TDS. Additionally, a time-limited, signature
 * based authorization is used to further protect from spoofed requests.
 *
 * The client is responsible for managing the key, and must ensure the user
 * running the TDS has the necessary permissions to read the key file (location
 * defined by the tds.local.api.key property). This implementation assumes the
 * client may update the key without informing the TDS (rotating keys are ok).
 *
 * @since 5.7
 */

@WebFilter(urlPatterns = "/local/*")
public class LocalApiFilter extends HttpFilter implements Filter {

  private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("threddsServlet");
  private final String SIGNING_KEY = System.getProperty("tds.local.api.key", "");

  LocalApiSigner localApiSigner;

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    log.debug("Filtering request to local API ");
    if (!(request instanceof HttpServletRequest httpRequest)) {
      log.error("doFilter(): Not an HTTP request! How did this filter get here?");
      chain.doFilter(request, response);
      return;
    }

    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if (!request.getRemoteAddr().equals(request.getLocalAddr())) {
      log.error("Request to local API from non-local address.");
      httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    if (!httpRequest.getMethod().equals("GET")) {
      log.error("Only GET requests to local API are supported.");
      httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    if (httpRequest.getHeader(LocalApiSigner.LOCAL_API_SIGNATURE_HEADER_V1) == null) {
      log.error("Local API request is not signed.");
      httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    } else if (!verifyRequest(httpRequest)) {
      // local TDM api key may have updated - reinit once and try again
      initLocalApiSigner(true);
      if (!verifyRequest(httpRequest)) {
        log.error("Local API signature mismatch.");
        httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
    }

    // continue along the filter chain
    chain.doFilter(request, response);
  }

  private void initLocalApiSigner(boolean reinit) {
    if (localApiSigner == null || reinit) {
      // read key from TDM directory (requires access)
      if (SIGNING_KEY.isEmpty()) {
        log.error("Local API cannot be used without key.");
        return;
      }
      Path tdmKey = Paths.get(SIGNING_KEY);
      try {
        localApiSigner = new LocalApiSigner(Files.readString(tdmKey));
      } catch (IOException e) {
        log.error("Local API cannot be used without key.", e);
      }
    }
  }

  private boolean verifyRequest(HttpServletRequest httpRequest) {
    String expected = httpRequest.getHeader(LocalApiSigner.LOCAL_API_SIGNATURE_HEADER_V1);
    if (expected == null) {
      log.error("Local API request missing {} header.", LocalApiSigner.LOCAL_API_SIGNATURE_HEADER_V1);
      return false;
    }
    if (localApiSigner == null) {
      initLocalApiSigner(false);
    }
    String url = httpRequest.getRequestURI();
    // getRequestUri is only the path, so check for query
    if (httpRequest.getQueryString() != null) {
      url = String.format("%s?%s", url, httpRequest.getQueryString());
    }
    return localApiSigner.verifySignatureGet(url, expected);
  }
}
