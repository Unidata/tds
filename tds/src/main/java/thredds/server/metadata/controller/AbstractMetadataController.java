package thredds.server.metadata.controller;

import java.io.IOException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.ServletContextAware;
import thredds.client.catalog.Dataset;
import thredds.core.TdsRequestedDataset;

public abstract class AbstractMetadataController implements ServletContextAware, IMetadataContoller {
  private static org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(AbstractMetadataController.class);

  protected static org.slf4j.Logger _logServerStartup = org.slf4j.LoggerFactory.getLogger("serverStartup");

  protected boolean _allow = false;
  protected String _metadataServiceType = "";
  protected String _servletPath = "";

  protected ServletContext sc;

  public void setServletContext(ServletContext sc) {
    this.sc = sc;
  }

  protected void isAllowed(final boolean allow, final String metadataServiceType, final HttpServletResponse res)
      throws Exception {
    // Check whether TDS is configured to support service.
    if (!allow) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN, metadataServiceType + " service not supported");
      return;
    }
  }

  protected void returnError(final String message, final String metadataServiceType, final HttpServletResponse res)
      throws Exception {
    res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, metadataServiceType + " service failed. " + message);
    return;
  }

  /**
   * All metadata controllers must implement a handleMetadataRequest method.
   *
   * @param req incoming url request
   * @param res outgoing web based response
   * @throws ServletException if ServletException occurred
   * @throws IOException if IOException occurred
   */
  public void handleMetadataRequest(final HttpServletRequest req, final HttpServletResponse res)
      throws ServletException, IOException {}

  /**
   * Get the THREDDS dataset object
   * where catalogString and dataset are passed in the request string
   *
   * @param req incoming url request
   */
  protected Dataset getThreddsDataset(final HttpServletRequest req, final HttpServletResponse res) {
    try {
      return (Dataset) TdsRequestedDataset.getGridDataset(req, res, null);
    } catch (IOException e) {
      _log.error("IOException while trying to get GridDataset:\n" + e.getLocalizedMessage());
      return null;
    }
  }

  protected abstract String getPath();

  protected String getInfoPath(HttpServletRequest req) {
    String servletPath = req.getServletPath();
    String pathInfo = servletPath.substring(getPath().length(), servletPath.length());
    return pathInfo;
  }


}
