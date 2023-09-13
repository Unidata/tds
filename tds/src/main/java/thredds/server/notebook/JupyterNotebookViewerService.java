package thredds.server.notebook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.server.exception.MethodNotImplementedException;
import thredds.client.catalog.Dataset;
import thredds.core.StandardService;
import thredds.server.viewer.Viewer;
import thredds.server.viewer.ViewerLinkProvider;
import thredds.server.viewer.ViewerService;
import thredds.util.StringValidateEncodeUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class JupyterNotebookViewerService implements ViewerService {

  private JupyterNotebookServiceCache jupyterNotebooks;

  private String contentDir;

  private List<Viewer> viewers = new ArrayList<>();

  public JupyterNotebookViewerService(JupyterNotebookServiceCache jupyterNotebooks, String contentDir) {
    this.jupyterNotebooks = jupyterNotebooks;
    this.contentDir = contentDir;
    this.buildViewerList();
  }

  @Override
  public List<Viewer> getViewers() {
    return viewers;
  }

  @Override
  public Viewer getViewer(String viewer) {
    return null;
  }

  @Override
  public String getViewerTemplate(String template) {
    throw new MethodNotImplementedException("JupyterNotebookViewerService.getViewerTemplate is not implemented");
  }

  @Override
  public boolean registerViewer(Viewer v) {
    return viewers.add(v);
  }

  @Override
  public boolean registerViewers(List<Viewer> v) {
    return viewers.addAll(v);
  }

  @Override
  public void showViewers(Formatter sbuff, Dataset dataset, HttpServletRequest req) {
    throw new MethodNotImplementedException("JupyterNotebookViewerService.showViewers is not implemented");
  }

  @Override
  public List<ViewerLinkProvider.ViewerLink> getViewerLinks(Dataset dataset, HttpServletRequest req) {
    return null;
  }

  private void buildViewerList() {
    jupyterNotebooks.getAllNotebooks()
        .forEach(notebook -> registerViewer(new JupyterNotebookViewer(notebook, contentDir)));
  }

  public static class JupyterNotebookViewer implements Viewer {
    static private final Logger logger = LoggerFactory.getLogger(JupyterNotebookViewer.class);

    private static final ViewerLinkProvider.ViewerLink.ViewerType type =
        ViewerLinkProvider.ViewerLink.ViewerType.JupyterNotebook;

    private String contentDir;

    private NotebookMetadata notebook;

    public JupyterNotebookViewer(NotebookMetadata notebook, String contentDir) {
      this.notebook = notebook;
      this.contentDir = contentDir;
    }

    public boolean isViewable(Dataset ds) {
      return notebook.isValidForDataset(ds);
    }

    public String getViewerLinkHtml(Dataset ds, HttpServletRequest req) {
      ViewerLinkProvider.ViewerLink viewerLink = this.getViewerLink(ds, req);
      return "<a href='" + viewerLink.getUrl() + "'>" + viewerLink.getTitle() + "</a>";
    }

    public ViewerLinkProvider.ViewerLink getViewerLink(Dataset ds, HttpServletRequest req) {
      String catUrl = ds.getCatalogUrl();
      if (catUrl.indexOf('#') > 0)
        catUrl = catUrl.substring(0, catUrl.lastIndexOf('#'));
      if (catUrl.indexOf(contentDir) > -1) {
        catUrl = catUrl.substring(catUrl.indexOf(contentDir) + contentDir.length());
      }
      String catalogServiceBase = StandardService.catalogRemote.getBase();
      catUrl =
          catUrl.substring(catUrl.indexOf(catalogServiceBase) + catalogServiceBase.length()).replace("html", "xml");

      String requestQuery;
      try {
        requestQuery = "?" + "catalog=" + URLEncoder.encode(catUrl, StringValidateEncodeUtils.CHARACTER_ENCODING_UTF_8)
            + "&filename="
            + URLEncoder.encode(notebook.getFilename(), StringValidateEncodeUtils.CHARACTER_ENCODING_UTF_8);
      } catch (UnsupportedEncodingException e) {
        logger.warn("JupyterNotebookViewer URL=" + req.getRequestURL().toString(), e);
        return null;
      }
      String url = req.getContextPath() + StandardService.jupyterNotebook.getBase() + ds.getID() + requestQuery;

      return new ViewerLinkProvider.ViewerLink(notebook.getFilename(), url, notebook.getDescription(), type);
    }
  }
}
