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
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Reject any request not originating from the same network interface running
 * the TDS.
 * <p/>
 * <p/>
 * This is an attempt to filter out and reject requests not originating on the
 * same machine as the TDS. This implementation is slightly more restrictive,
 * in that it will filter out requests not originating from the same network
 * interface as was received by the TDS.
 *
 * @since 5.7
 */

@WebFilter(urlPatterns = "/local/*")
public class LocalhostFilter extends HttpFilter implements Filter {

  private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("threddsServlet");

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    log.debug("Filtering request to local API ");
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    if (!request.getRemoteAddr().equals(request.getLocalAddr())) {
      log.debug("Request to local API from non-local address");
      httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // continue along the filter chain
    chain.doFilter(request, response);
  }
}
