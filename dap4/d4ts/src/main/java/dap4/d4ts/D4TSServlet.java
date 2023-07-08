/*
 * Copyright 2012, UCAR/Unidata.
 * See the LICENSE file for more information.
 */

package dap4.d4ts;

import dap4.core.util.DapContext;
import dap4.core.util.DapException;
import dap4.core.util.DapUtil;
import dap4.dap4lib.DapCodes;
import dap4.dap4lib.DapLog;
import dap4.servlet.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import static dap4.d4ts.FrontPage.Root;


public class D4TSServlet extends DapController {

  //////////////////////////////////////////////////
  // Constants

  static final boolean DEBUG = false;

  static final boolean PARSEDEBUG = false;

  static final String RESOURCEPATH = "/testfiles";

  //////////////////////////////////////////////////
  // Type Decls

  //////////////////////////////////////////////////
  // Instance variables

  protected List<Root> defaultroots = null;

  //////////////////////////////////////////////////
  // Constructor(s)

  public D4TSServlet() {
    super();
  }

  @Override
  public void initialize() {
    super.initialize();
    DapLog.info("Initializing d4ts servlet");
  }

  //////////////////////////////////////////////////

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, java.io.IOException {
    super.handleRequest(req, resp);
  }

  //////////////////////////////////////////////////////////
  // Capabilities processors

  @Override
  protected void doFavicon(String icopath, DapContext cxt) throws IOException {
    DapRequest drq = (DapRequest) cxt.get(DapRequest.class);
    String favfile = drq.getResourcePath(icopath);
    if (favfile != null) {
      try (FileInputStream fav = new FileInputStream(favfile);) {
        byte[] content = DapUtil.readbinaryfile(fav);
        OutputStream out = drq.getOutputStream();
        out.write(content);
      }
    }
  }

  @Override
  protected void doCapabilities(DapRequest drq, DapContext cxt) throws IOException {
    addCommonHeaders(drq);

    // Generate the front page
    FrontPage front = getFrontPage(drq, cxt);
    String frontpage = front.buildPage();

    if (frontpage == null)
      throw new DapException("Cannot create front page").setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    // // Convert to UTF-8 and then to byte[]
    byte[] frontpage8 = DapUtil.extract(DapUtil.UTF8.encode(frontpage));
    OutputStream out = drq.getOutputStream();
    out.write(frontpage8);

  }

  @Override
  public long getBinaryWriteLimit() {
    return DEFAULTBINARYWRITELIMIT;
  }

  /**
   * Isolate front page builder so we can override if desired for testing.
   *
   * @param drq
   * @param cxt
   * @return FrontPage object
   */
  protected FrontPage getFrontPage(DapRequest drq, DapContext cxt) throws DapException {
    if (this.defaultroots == null) {
      // Figure out the directory containing
      // the files to display.
      String testroot = drq.getResourcePath("/");
      if (testroot == null)
        throw new DapException("Cannot locate dataset  directory");
      this.defaultroots = new ArrayList<>();
      this.defaultroots.add(new Root(testroot, RESOURCEPATH));
    }
    return new FrontPage(this.defaultroots, drq);
  }

  @Override
  public String getServletID() {
    return "/d4ts";
  }

  @Override
  public String getWebContentRoot(DapRequest drq) throws DapException {
    try {
      String servletpath = getServletContext().getResource("/").getPath();
      String path = servletpath + "WEB-INF";
      File f = new File(path);
      if (!f.exists() || !f.canRead() || !f.isDirectory())
        throw new DapException("Cannot find WEB-INF").setCode(DapCodes.SC_NOT_FOUND);
      return path;
    } catch (IOException ioe) {
      throw new DapException(ioe);
    }
  }

  /**
   * Convert a URL path for a dataset into an absolute file path
   *
   * @param drq dap request
   * @param location suffix of url path
   * @return path in a string builder so caller can extend.
   * @throws IOException
   */
  public String getResourcePath(DapRequest drq, String location) throws DapException {
    try {
      String root = getWebContentRoot(drq);
      if (root == null)
        throw new DapException("Cannot find WEB-INF").setCode(DapCodes.SC_NOT_FOUND);
      StringBuilder path = new StringBuilder(DapUtil.canonicalpath(root));
      if (location.charAt(0) != '/')
        path.append('/');
      path.append(location);
      String result = path.toString();
      File f = new File(result);
      if (!f.exists() || !f.canRead())
        throw new DapException("Cannot find Resource path: " + result).setCode(DapCodes.SC_NOT_FOUND);
      return result;
    } catch (IOException ioe) {
      throw new DapException(ioe).setCode(DapCodes.SC_NOT_FOUND);
    }
  }

}
