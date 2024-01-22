/*
 * Copyright 2009, UCAR/Unidata and OPeNDAP, Inc.
 * See the LICENSE file for more information.
 */

package dap4.servlet;

import dap4.core.util.*;
import dap4.dap4lib.DapProtocol;
import dap4.dap4lib.RequestMode;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Generate the DSR for a dataset.
 * Currently only generates a minimal DSR.
 */

public class DapDSR {

  //////////////////////////////////////////////////
  // Constants

  static final boolean DEBUG = false;

  static final String DSRXMLTEMPLATE = "/templates/dap4.dsr.xml.template";
  static final String DSRHTMLTEMPLATE = "/templates/dap4.dsr.html.template";

  static final String URL_FORMAT = DapConstants.HTTPSCHEME + "//%s/%s/%s";

  //////////////////////////////////////////////////
  // Static Variables

  static private String dap4TestServerPropName = "d4ts";
  public static String dap4TestServer = null;; // mutable
  protected static String servletprefix = null;
  protected static String servletsuffix = null;

  static {
    String d4ts = System.getProperty(dap4TestServerPropName);
    if (d4ts != null && d4ts.length() > 0)
      dap4TestServer = d4ts;
  }

  //////////////////////////////////////////////////
  // Instance Variables

  DapRequest drq;
  DapContext cxt;

  //////////////////////////////////////////////////
  // Constructor(s)

  public DapDSR(DapRequest drq, DapContext cxt) throws IOException {
    this.drq = drq;
    this.cxt = cxt;

    // Figure out the test server
    if (this.dap4TestServer == null) {
      try {
        URL url = new URL(drq.getRequest().getRequestURL().toString());
        this.dap4TestServer = url.getHost();
        if (url.getPort() > 0)
          this.dap4TestServer += ":" + url.getPort();
        this.servletprefix = drq.getRequest().getContextPath();
        this.servletprefix = DapUtil.relativize(this.servletprefix);
        this.servletsuffix = drq.getRequest().getServletPath();

      } catch (MalformedURLException mue) {
        throw new DapException(mue).setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
    if (this.dap4TestServer == null) {
      throw new DapException("Cannot determine test server host").setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    if (this.servletprefix == null) {
      throw new DapException("Cannot determine test servlet prefix")
          .setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    if (this.servletsuffix == null) {
      throw new DapException("Cannot determine test servlet suffix")
          .setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  //////////////////////////////////////////////////
  // API

  public String generate(ResponseFormat format, String datasetpath, String dataset) throws IOException {
    // Normalize to relative path
    datasetpath = DapUtil.relativize(datasetpath); // Get the DSR template
    String template = getTemplate(format);
    StringBuilder dsr = new StringBuilder(template);
    substitute(dsr, "DAP_VERSION", DapConstants.X_DAP_VERSION);
    substitute(dsr, "DAP_SERVER", DapConstants.X_DAP_SERVER);
    substitute(dsr, "DATASET", dataset);
    // Compute the URL
    String url = String.format(URL_FORMAT, this.dap4TestServer, this.servletprefix, datasetpath);
    substitute(dsr, "URL", url);
    return dsr.toString();
  }

  protected String getTemplate(ResponseFormat format) throws IOException {
    StringBuilder buf = new StringBuilder();
    // Get template as resource stream
    String template = null;
    switch (format) {
      case XML:
        template = DSRXMLTEMPLATE;
        break;
      case NONE:
      case HTML:
        template = DSRHTMLTEMPLATE;
        break;
      default:
        throw new IOException("Unsupported DSR Response Format: " + format.toString());
    }
    String templatepath = drq.getWebContentPath(template);
    try (InputStream stream = new FileInputStream(templatepath)) {
      int ch;
      while ((ch = stream.read()) >= 0) {
        buf.append((char) ch);
      }
    }
    return buf.toString();
  }

  protected void substitute(StringBuilder buf, String macro, String value) {
    int from = 0;
    String tag = "${" + macro + "}";
    int taglen = tag.length();
    int valuelen = value.length();
    for (;;) {
      int index = buf.indexOf(tag, from);
      if (index < 0)
        break;
      buf.replace(index, index + taglen, value);
      from = index + valuelen;
    }
  }


} // DapDSR
