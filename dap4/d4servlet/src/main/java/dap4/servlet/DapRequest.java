/*
 * Copyright 2009, UCAR/Unidata and OPeNDAP, Inc.
 * See the LICENCE file for more information.
 */

package dap4.servlet;

import dap4.core.util.*;
import dap4.dap4lib.RequestMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.ByteOrder;
import java.util.Map;

/**
 * User requests get cached here so that downstream code can access
 * the details of the request information.
 * <p>
 * Modified by Heimbigner for DAP4.
 *
 * @author Nathan Potter
 * @author Dennis Heimbigner
 */

public class DapRequest {
  //////////////////////////////////////////////////
  // Constants

  static final boolean DEBUG = false;

  protected static final String WEBINFPATH = "WEB-INF";
  protected static final String RESOURCEDIRNAME = "resources";

  //////////////////////////////////////////////////
  // Instance variables
  protected DapController controller = null;
  protected HttpServletRequest request = null;
  protected HttpServletResponse response = null;

  protected XURI xuri = null; // without any query and as with any modified dataset path

  protected RequestMode mode = null; // .dmr, .dap, or .dsr
  protected ResponseFormat format = null; // e.g. .xml when given .dmr.xml

  protected ByteOrder order = ByteOrder.nativeOrder();
  protected ChecksumMode checksummode = null;

  // path from URL relative to the Servlet
  protected String datasetpath = null;

  // The last path element from datasetpath
  protected String dataset = null;

  //////////////////////////////////////////////////
  // Constructor(s)

  public DapRequest(DapController controller, HttpServletRequest request, HttpServletResponse response)
      throws DapException {
    this.request = request;
    this.response = response;
    this.controller = controller;
    try {
      parseURI(); // Pull Info from the URI
    } catch (IOException ioe) {
      throw new DapException(ioe);
    }
  }

  //////////////////////////////////////////////////
  // Request path parsing

  /**
   * The goal of parse() is to extract info
   * from the underlying HttpRequest and cache it
   * in this object.
   * <p>
   * In particular, the incoming URL needs to be decomposed
   * into multiple pieces. Certain assumptions are made:
   * 1. every incoming url is of the form
   * (a) http(s)://host:port/d4ts/
   * or
   * (b) http(s)://host:port/d4ts/<datasetpath>?query
   * Case a indicates that the front page is to be returned.
   * Case b indicates a request for a dataset (or dsr), and its
   * value is determined by its extensions. The query may be absent.
   * We want to extract the following pieces.
   * 1. (In URI parlance) The scheme plus the authority:
   * http://host:port
   * 3. The return type: depending on the last extension (e.g. ".txt").
   * 4. The requested value: depending on the next to last extension (e.g. ".dap").
   * 5. The suffix path specifying the actual dataset: datasetpath
   * with return and request type extensions removed.
   * 6. The url path = servletpath + datasetpath.
   * 7. The query part.
   */

  protected void parseURI() throws IOException {
    try {
      // Unfortunately getRequestURL does not include the query
      StringBuffer fullurl = request.getRequestURL();
      if (request.getQueryString() != null) {
        fullurl.append("?");
        fullurl.append(Escape.urlDecode(request.getQueryString()));
      }
      xuri = new XURI(fullurl.toString());
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
    this.datasetpath = request.getServletPath();
    if (this.datasetpath.equals("/") || this.datasetpath.equals(""))
      this.datasetpath = null; // canonical value
    if (this.datasetpath == null) {
      this.mode = RequestMode.CAPABILITIES;
    } else {
      String path = this.datasetpath;
      // Break dataset path into prefix/ + dataset
      int index = path.lastIndexOf('/');
      if (index < 0)
        index = 0;
      String prefix = path.substring(0, index);
      String file = path.substring(index, path.length());
      file = DapUtil.relativize(file);
      for (;;) { // Iterate until we find a non-mode|format extension or no extension
        // Decompose dataset by '.'
        index = file.lastIndexOf('.');
        if (index < 0)
          index = file.length();
        String extension = file.substring(index, file.length());
        if (extension == null || extension.equals(""))
          break;
        // Figure out what this extension represents
        int modepos = 0;
        // We assume that the set of response formats does not intersect the set of request modes
        RequestMode mode = RequestMode.modeFor(extension);
        ResponseFormat format = ResponseFormat.formatFor(extension);
        if (mode == null && format == null)
          break; // stop here
        if (mode != null) {
          if (this.mode != null)
            throw new DapException("Multiple request modes specified: " + extension)
                .setCode(HttpServletResponse.SC_BAD_REQUEST);
          this.mode = mode;
        } else if (format != null) {
          if (this.format != null)
            throw new DapException("Multiple response formats specified: " + extension)
                .setCode(HttpServletResponse.SC_BAD_REQUEST);
          this.format = format;
        }
        file = file.substring(0, index); // Remove consumed extension
      }
      // Set the final global values
      this.dataset = file;
      this.datasetpath = prefix + "/" + this.dataset;
    }

    if (this.mode == null)
      this.mode = RequestMode.DSR;
    if (this.format == null)
      this.format = ResponseFormat.NONE;

    // For testing purposes, get the desired endianness to use with replies
    String p = queryLookup(DapConstants.DAP4ENDIANTAG);
    if (p != null) {
      Integer oz = DapUtil.stringToInteger(p);
      if (oz == null)
        this.order = ByteOrder.LITTLE_ENDIAN;
      else
        this.order = (oz != 0 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
    }

    // Ditto for checksum
    p = queryLookup(DapConstants.CHECKSUMTAG);
    if (p != null) {
      this.checksummode = ChecksumMode.modeFor(p);
    }
  }

  //////////////////////////////////////////////////
  // Accessor(s)

  public ByteOrder getOrder() {
    return this.order;
  }

  public ChecksumMode getChecksumMode() {
    return this.checksummode;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public String getWebContentRoot() throws DapException {
    return controller.getWebContentRoot(this);
  }

  public String getServletID() throws DapException {
    return controller.getServletID();
  }

  /**
   * Convert a URL path for a dataset into an absolute file path
   *
   * @param location suffix of url path
   * @return path in a string builder so caller can extend.
   * @throws IOException
   */
  public String getResourcePath(String location) throws DapException {
    return controller.getResourcePath(this, location);
  }

  /**
   * Convert a URL path for a web-content related file into an absolute file path
   *
   * @param location suffix of url path
   * @return path in a string builder so caller can extend.
   * @throws IOException
   */
  public String getWebContentPath(String location) throws DapException {
    String path = getWebContentRoot();
    path = DapUtil.canonicalpath(path);
    location = DapUtil.relativize(location);
    path += "/" + location;
    return path;
  }

  public OutputStream getOutputStream() throws IOException {
    return response.getOutputStream();
  }

  public String getURL() {
    return this.xuri.toString();
  }

  public String getDatasetPath() {
    return this.datasetpath;
  }

  public String getDataset() {
    // Strip off any leading prefix
    return this.dataset;
  }

  public RequestMode getMode() {
    return this.mode;
  }

  public ResponseFormat getFormat() {
    return this.format;
  }

  /**
   * Set a request header
   *
   * @param name the header name
   * @param value the header value
   */
  public void setResponseHeader(String name, String value) {
    this.response.setHeader(name, value);
  }

  public String queryLookup(String name) {
    return this.xuri.getQueryFields().get(name.toLowerCase());
  }

  public Map<String, String> getQueries() {
    return this.xuri.getQueryFields();
  }

  static String makeQueryString(HttpServletRequest req) {
    Map<String, String[]> map = req.getParameterMap();
    if (map == null || map.size() == 0)
      return null;
    StringBuilder q = new StringBuilder();
    for (Map.Entry<String, String[]> entry : map.entrySet()) {
      String[] values = entry.getValue();
      if (values == null || values.length == 0) {
        q.append("&");
        q.append(entry.getKey());
      } else
        for (int i = 0; i < values.length; i++) {
          q.append("&");
          q.append(entry.getKey());
          q.append("=");
          q.append(values[i]);
        }
    }
    if (q.length() > 0)
      q.deleteCharAt(0);// leading &
    return q.toString();
  }
}

