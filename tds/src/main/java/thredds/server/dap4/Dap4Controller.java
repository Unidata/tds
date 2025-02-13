/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.dap4;

import dap4.core.util.DapContext;
import dap4.core.util.DapException;
import dap4.core.util.DapUtil;
import dap4.dap4lib.DapCodes;
import dap4.servlet.CDMWrap;
import dap4.servlet.DapController;
import dap4.servlet.DapRequest;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import thredds.core.TdsRequestedDataset;
import thredds.server.config.TdsContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDataset.Enhance;

@Controller
@RequestMapping("/dap4")
public class Dap4Controller extends DapController {

  //////////////////////////////////////////////////
  // Constants

  static final boolean DEBUG = false;

  static final boolean PARSEDEBUG = false;

  static final String SERVICEID = "/dap4";

  // NetcdfDataset enhancement to use: need only coord systems
  // match dap4.servlet.CDMWrap.ENHANCEMENT
  static Set<Enhance> ENHANCEMENT = EnumSet.of(NetcdfDataset.Enhance.CoordSystems);

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

  public CDMWrap getCDMWrap(DapRequest drq) throws IOException {
    String datasetPath = drq.getDatasetPath();
    String prefix = SERVICEID + "/";
    if (datasetPath.startsWith(prefix)) {
      datasetPath = datasetPath.substring(prefix.length());
    }
    NetcdfFile ncf = TdsRequestedDataset.getNetcdfFile(drq.getRequest(), drq.getResponse(), datasetPath);
    NetcdfDataset ncd;
    ncd = NetcdfDataset.wrap(ncf, ENHANCEMENT);
    return new CDMWrap().open(ncd);
  }

}


