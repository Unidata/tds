/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.viewer;

import thredds.client.catalog.Dataset;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Interface for plugging in Viewers.
 * Generally, these are implemented with jnlp files in /content/thredds/view/views/*.jnlp
 * You can customizing by adding parameters to the jnlp file, eg {@code parm=subst&name=value}.
 * Then all instances of "{param}" will be replaced by subst, and
 * all instances of "{name}" will be replaced by value, etc.
 */
public interface ViewerLinkProvider extends Viewer {

  /**
   * Get an HTML fragment link to the viewer JNLP file, for this dataset.
   * Example:
   * return "<a href='" + req.getContextPath() + "/view/idv.jnlp?url="+dataURI.toString()+"'>Integrated Data Viewer
   * (IDV) (webstart)</a>";
   *
   * @param ds the dataset to view
   * @param req the request
   * @return HTML fragment string
   */
  List<ViewerLink> getViewerLinks(Dataset ds, HttpServletRequest req);

  class ViewerLink {
    private String title;
    private String url;
    private String description;
    private ViewerType type;

    public ViewerLink(String title, String url) {
      this(title, url, "", ViewerType.Unknown);
    }

    public ViewerLink(String title, String url, String description) {
      this(title, url, description, ViewerType.Unknown);
    }

    public ViewerLink(String title, String url, String description, ViewerType type) {
      this.title = title;
      this.url = url;
      this.description = description;
      this.type = type;
    }

    public String getTitle() {
      return title;
    }

    public String getUrl() {
      return url;
    }

    public String getDescription() {
      return description;
    }

    public String getType() {
      return type.getName();
    }

    public static enum ViewerType {
      Application("Application"), Browser("Browser"), JupyterNotebook("Jupyter Notebook"), Unknown("Unknown");

      protected final String name;

      private ViewerType(String name) {
        this.name = name;
      }

      public String getName() {
        return this.name;
      }
    }
  }
}
