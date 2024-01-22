/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.dap4;

import dap4.core.util.DapContext;
import dap4.core.util.DapException;
import dap4.core.util.DapUtil;
import dap4.dap4lib.DapCodes;
import dap4.servlet.DapController;
import dap4.servlet.DapRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import thredds.core.TdsRequestedDataset;
import thredds.server.config.TdsContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping("/dap4")
public class Dap4Controller extends DapController {

  //////////////////////////////////////////////////
  // Constants

  static final boolean DEBUG = false;

  static final boolean PARSEDEBUG = false;

  static final String SERVICEID = "/dap4";

  // NetcdfDataset enhancement to use: need only coord systems
  // static Set<NetcdfDataset.Enhance> ENHANCEMENT = EnumSet.of(NetcdfDataset.Enhance.CoordSystems);

  //////////////////////////////////////////////////
  // Instance variables

  @Autowired
  private TdsContext tdscontext;

  //////////////////////////////////////////////////
  // Spring Elements

  @RequestMapping("**")
  public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
    // throw new UnsupportedOperationException("DAP4 is not currently functional, but we are working on it!");
    super.handleRequest(req, res);
  }

  //////////////////////////////////////////////////
  // Constructor(s)

  public Dap4Controller() {
    super();
  }

  //////////////////////////////////////////////////////////

  @Override
  protected void doFavicon(String icopath, DapContext cxt) throws IOException {
    throw new UnsupportedOperationException("Favicon");
  }

  @Override
  protected void doCapabilities(DapRequest drq, DapContext cxt) throws IOException {
    addCommonHeaders(drq);
    OutputStream out = drq.getOutputStream();
    PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, DapUtil.UTF8));
    pw.println("Capabilities page not supported");
    pw.flush();
  }

  @Override
  public long getBinaryWriteLimit() {
    return DEFAULTBINARYWRITELIMIT;
  }

  @Override
  public String getServletID() {
    String cp = tdscontext.getContextPath();
    if (cp == null || cp.length() == 0)
      cp = "dap4";
    StringBuilder id = new StringBuilder(cp);
    // Strip any trailing '/'
    if (id.charAt(id.length() - 1) == '/')
      id.deleteCharAt(id.length());
    // ensure it starts with '/'
    if (id.charAt(0) != '/')
      id.insert(0, '/');
    return id.toString();
  }

  @Override
  public String getWebContentRoot(DapRequest drq) throws DapException {
    File root = tdscontext.getServletRootDirectory();
    if (!root.exists() || !root.canRead() || !root.isDirectory())
      throw new DapException("Cannot locate WEB-INF root").setCode(DapCodes.SC_NOT_FOUND);
    String rootpath = root.getAbsolutePath() + "/WEB-INF";
    return DapUtil.canonicalpath(rootpath);
  }

  /**
   * Convert a URL path for a dataset into an absolute file path
   *
   * @param location suffix of url path
   * @return path in a string builder so caller can extend.
   * @throws IOException
   */
  public String getResourcePath(DapRequest drq, String location) throws DapException {
    assert (location.charAt(0) == '/');
    // Remove the leading service name, if any
    if (location.startsWith(SERVICEID))
      location = location.substring(SERVICEID.length());
    String path = TdsRequestedDataset.getLocationFromRequestPath(location);
    if (path == null || path.length() == 0)
      throw new DapException(String.format("getLocationFromRequestPath: location=|%s| path=null", location, path))
          .setCode(DapCodes.SC_NOT_FOUND);
    File f = new File(path);
    if (!f.exists() || !f.canRead() || !f.isFile())
      throw new DapException("Cannot locate resource: " + location).setCode(DapCodes.SC_NOT_FOUND);
    return DapUtil.canonicalpath(path);
  }

}


