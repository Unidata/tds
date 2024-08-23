package thredds.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import thredds.core.DataRootManager;
import thredds.util.TdsPathUtils;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDatasets;

/**
 * DatasetSource test LOOK this doesnt work
 *
 * @author caron
 * @since 2/17/11
 */
public class DatasetSourceExample implements thredds.servlet.DatasetSource {
  static final String prefix = "/special/";
  static final int prefixLen = prefix.length();

  public DatasetSourceExample() {
    System.out.printf("%s%n", "YO");
  }

  public boolean isMine(HttpServletRequest req) {
    String path = TdsPathUtils.extractPath(req, null);
    return path.startsWith(prefix);
  }

  public NetcdfFile getNetcdfFile(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String path = TdsPathUtils.extractPath(req, prefix);

    DataRootManager.DataRootMatch match = DataRootManager.getInstance().findDataRootMatch(path);
    if (match == null) {
      res.sendError(HttpServletResponse.SC_NOT_FOUND, path);
      return null;
    }

    int pos = match.remaining.lastIndexOf('.');
    String filename = match.remaining.substring(0, pos);

    File file = new File(match.dirLocation + filename);
    if (!file.exists()) {
      res.sendError(HttpServletResponse.SC_NOT_FOUND, match.dirLocation + filename);
      return null;
    }

    NetcdfFile ncfile = NetcdfDatasets.openFile(file.getPath(), null);
    ncfile.addAttribute(null, new Attribute("Special", req.getRequestURI()));
    ncfile.finish();
    return ncfile;
  }

}
