/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.viewer;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import thredds.client.catalog.*;
import ucar.nc2.util.IO;
import ucar.unidata.util.StringUtil2;
import thredds.server.wms.Godiva3Viewer;

@Component
public class ViewerServiceImpl implements ViewerService, InitializingBean {
  private static Logger logger = LoggerFactory.getLogger(ViewerServiceImpl.class);

  public static ViewerLinkProvider getStaticView() {
    return new StaticView();
  }

  private List<Viewer> viewers = new ArrayList<>();
  private HashMap<String, String> templates = new HashMap<>();

  @Override
  public List<Viewer> getViewers() {
    return null;
  }

  @Override
  public Viewer getViewer(String viewer) {
    return null;
  }

  @Override
  public boolean registerViewer(Viewer v) {
    return viewers.add(v);
  }

  public boolean registerViewers(List<Viewer> v) {
    return viewers.addAll(v);
  }

  @Deprecated
  @Override
  public String getViewerTemplate(String path) {

    String template = templates.get(path);
    if (template != null)
      return template;

    try {
      template = IO.readFile(path);
    } catch (IOException ioe) {
      return null;
    }

    templates.put(path, template);
    return template;

  }

  @Override
  public void showViewers(Formatter out, Dataset dataset, HttpServletRequest req) {

    int count = 0;
    for (Viewer viewer : viewers) {
      if (viewer.isViewable(dataset))
        count++;
    }
    if (count == 0)
      return;

    out.format("<h3>Viewers:</h3><ul>\r\n");

    for (Viewer viewer : viewers) {
      if (viewer.isViewable(dataset)) {
        if (viewer instanceof ViewerLinkProvider) {
          List<ViewerLinkProvider.ViewerLink> sp = ((ViewerLinkProvider) viewer).getViewerLinks(dataset, req);
          for (ViewerLinkProvider.ViewerLink vl : sp) {
            if (vl.getUrl() != null && !vl.getUrl().equals(""))
              out.format("<li><a href='%s'>%s</a></li>\r\n", vl.getUrl(),
                  vl.getTitle() != null ? vl.getTitle() : vl.getUrl());
          }

        } else {
          String viewerLinkHtml = viewer.getViewerLinkHtml(dataset, req);
          if (viewerLinkHtml != null) {
            out.format("  <li> %s</li>\r\n", viewerLinkHtml);
          }
        }
      }
    }
    out.format("</ul>\r\n");
  }

  @Override
  public List<ViewerLinkProvider.ViewerLink> getViewerLinks(Dataset dataset, HttpServletRequest req) {
    List<ViewerLinkProvider.ViewerLink> viewerLinks = new ArrayList<>();

    for (Viewer viewer : viewers) {
      if (viewer.isViewable(dataset)) {
        if (viewer instanceof ViewerLinkProvider) {
          viewerLinks.addAll(((ViewerLinkProvider) viewer).getViewerLinks(dataset, req));
        } else {
          viewerLinks.add(viewer.getViewerLink(dataset, req));
        }
      }
    }
    return viewerLinks;
  }

  @SuppressWarnings("unused")
  public void afterPropertiesSet() {
    registerViewer(new Godiva3Viewer());
    registerViewer(new StaticView());
  }

  // Viewers...

  // LOOK whats this for ??
  private static final String propertyNamePrefix = "viewer";

  private static class StaticView implements ViewerLinkProvider {

    public boolean isViewable(Dataset ds) {
      return hasViewerProperties(ds);
    }

    public String getViewerLinkHtml(Dataset ds, HttpServletRequest req) {
      List<ViewerLink> viewerLinks = getViewerLinks(ds, req);
      if (viewerLinks.isEmpty())
        return null;
      ViewerLink firstLink = viewerLinks.get(0);
      return "<a href='" + firstLink.getUrl() + "'>" + firstLink.getTitle() + "</a>";
    }

    @Override
    public ViewerLink getViewerLink(Dataset ds, HttpServletRequest req) {
      List<ViewerLink> viewerLinks = getViewerLinks(ds, req);
      if (viewerLinks.isEmpty())
        return null;
      return viewerLinks.get(0);
    }

    @Override
    public List<ViewerLink> getViewerLinks(Dataset ds, HttpServletRequest req) {
      List<Property> viewerProperties = findViewerProperties(ds);
      if (viewerProperties.isEmpty())
        return Collections.emptyList();
      List<ViewerLink> result = new ArrayList<>();
      for (Property p : viewerProperties) {
        ViewerLink viewerLink = parseViewerPropertyValue(p.getName(), p.getValue(), ds);
        if (viewerLink != null)
          result.add(viewerLink);
      }
      return result;
    }

    private ViewerLink parseViewerPropertyValue(String viewerName, String viewerValue, Dataset ds) {
      // get viewer URL
      String[] viewerLinkParts = viewerValue.split(",");
      String viewerUrl = viewerLinkParts[0];
      if (viewerUrl.isEmpty()) {
        return null;
      }
      viewerUrl = sub(viewerUrl, ds); // add dataset info to URL

      // get additional viewer info
      int nParts = viewerLinkParts.length;
      String viewerTitle = nParts > 1 ? viewerLinkParts[1] : viewerName;
      if (viewerTitle.isEmpty()) {
        viewerTitle = viewerName;
      }

      String description = nParts > 2 ? viewerLinkParts[2] : "";
      ViewerLink.ViewerType viewerType =
          nParts > 3 ? parseViewerType(viewerLinkParts[3]) : ViewerLink.ViewerType.Unknown;

      return new ViewerLink(viewerTitle, viewerUrl, description, viewerType);
    }

    private ViewerLink.ViewerType parseViewerType(String type) {
      try {
        return ViewerLink.ViewerType.valueOf(type);
      } catch (IllegalArgumentException ex) {
        return ViewerLink.ViewerType.Unknown;
      }
    }

    private boolean hasViewerProperties(Dataset ds) {
      for (Property p : ds.getProperties())
        if (p.getName().startsWith(propertyNamePrefix))
          return true;

      return false;
    }

    private List<Property> findViewerProperties(Dataset ds) {
      List<Property> result = new ArrayList<>();
      for (Property p : ds.getProperties())
        if (p.getName().startsWith(propertyNamePrefix))
          result.add(p);

      return result;
    }

    private String sub(String org, Dataset ds) {
      List<Access> access = ds.getAccess();
      if (access.size() == 0)
        return org;

      // look through all access for {serviceName}
      for (Access acc : access) {
        String sname = "{" + acc.getService().getServiceTypeName() + "}";
        if (org.contains(sname)) {
          URI uri = acc.getStandardUri();
          if (uri != null)
            return StringUtil2.substitute(org, sname, uri.toString());
        }
      }

      String sname = "{url}";
      if ((org.contains(sname)) && (access.size() > 0)) {
        Access acc = access.get(0); // just use the first one
        URI uri = acc.getStandardUri();
        if (uri != null)
          return StringUtil2.substitute(org, sname, uri.toString());
      }

      return org;
    }
  }

}
