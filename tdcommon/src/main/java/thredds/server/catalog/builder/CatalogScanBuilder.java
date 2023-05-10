/* Copyright */
package thredds.server.catalog.builder;

import thredds.client.catalog.DatasetNode;
import thredds.client.catalog.builder.DatasetBuilder;
import thredds.server.catalog.CatalogScan;
import ucar.unidata.util.StringUtil2;

/**
 * CatalogScan Builder
 *
 * @author caron
 * @since 6/17/2015
 */
public class CatalogScanBuilder extends DatasetBuilder {
  String path, location, watch;
  String context = "thredds"; // default servlet context

  public CatalogScanBuilder(DatasetBuilder parent, String name, String path, String location, String watch,
      String context) {
    super(parent);
    this.name = name;
    this.path = path;
    this.location = location;
    this.watch = watch;
    this.context = StringUtil2.trim(context, '/');
  }

  public CatalogScanBuilder(DatasetBuilder parent, CatalogScan from, String context) {
    super(parent, from);
    this.path = from.getPath();
    this.location = from.getLocation();
    this.watch = from.getWatch();
    this.context = StringUtil2.trim(context, '/');
  }

  /**
   * @deprecated Use {@link #CatalogScanBuilder(DatasetBuilder, CatalogScan, String)} instead
   */
  @Deprecated
  public CatalogScanBuilder(DatasetBuilder parent, CatalogScan from) {
    this(parent, from, "thredds");
  }

  public CatalogScan makeDataset(DatasetNode parent) {
    String xlink = "/" + context + "/catalog/" + path + "/catalogScan.xml";
    return new CatalogScan(parent, name, xlink, flds, path, location, watch);
  }
}
