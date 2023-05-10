/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.Immutable;
import java.lang.invoke.MethodHandles;
import thredds.inventory.MFile;
import thredds.inventory.MFiles;

/**
 * A DataRoot matches URLs to the objects that can serve them.
 * A DataRootPathMatcher manages a hash tree of path -> DataRoot
 * <p/>
 * Possible design:
 * catKey : which catalog defined this? not present at the moment
 * directory : if its a simple DataRoot
 * fc : prob is this drags in entire configCat
 * dscan : ditto
 *
 * @author caron
 * @since 1/23/2015
 */
@Immutable
public class DataRoot {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public enum Type {
    datasetRoot, datasetScan, catalogScan, featureCollection
  }

  private final String path; // match this path to 1 of the following:
  private final String dirLocation; // 1) this directory (not null)
  private final String name; // 1) this directory (not null)
  private final DatasetScan datasetScan; // 2) the DatasetScan that created this (may be null)
  private final FeatureCollectionRef featCollection; // 3) the FeatureCollection that created this (may be null)
  private final CatalogScan catScan; // 4) the CatalogScan that created this (may be null)
  private final Type type;
  private final String restrict;

  public DataRoot(FeatureCollectionRef featCollection) {
    this.path = featCollection.getPath();
    this.dirLocation = featCollection.getTopDirectoryLocation();
    this.datasetScan = null;
    this.catScan = null;
    this.featCollection = featCollection;
    this.type = Type.featureCollection;
    this.name = featCollection.getCollectionName();
    this.restrict = featCollection.getRestrictAccess();
    logger.debug(" DataRoot %s==%s%n", path, dirLocation);
  }

  public DataRoot(DatasetScan scan) {
    this.path = scan.getPath();
    this.dirLocation = scan.getScanLocation();
    this.datasetScan = scan;
    this.catScan = null;
    this.featCollection = null;
    this.type = Type.datasetScan;
    this.name = scan.getName();
    this.restrict = scan.getRestrictAccess();
    logger.debug(" DataRoot %s==%s%n", path, dirLocation);
  }

  public DataRoot(CatalogScan catScan) {
    this.path = catScan.getPath();
    this.dirLocation = catScan.getLocation();
    this.datasetScan = null;
    this.catScan = catScan;
    this.featCollection = null;
    this.type = Type.catalogScan;
    this.name = catScan.getName();
    this.restrict = null;
    logger.debug(" DataRoot %s==%s%n", path, dirLocation);
  }

  public DataRoot(String path, String dirLocation, String restrict) {
    this.path = path;
    this.dirLocation = dirLocation;
    this.datasetScan = null;
    this.catScan = null;
    this.featCollection = null;
    this.type = Type.datasetRoot;
    this.name = null;
    this.restrict = restrict;
    logger.debug(" DataRoot %s==%s%n", path, dirLocation);
  }

  public String getPath() {
    return path;
  }

  public String getDirLocation() {
    return dirLocation;
  }

  public Type getType() {
    return type;
  }

  public DatasetScan getDatasetScan() {
    return datasetScan;
  }

  public CatalogScan getCatalogScan() {
    return catScan;
  }

  public FeatureCollectionRef getFeatureCollection() {
    return featCollection;
  }

  public String getName() {
    return name;
  }

  public String getRestrict() {
    return restrict;
  }

  // used by PathMatcher
  public String toString() {
    return path;
  }

  /**
   * Instances which have same path are equal.
   */
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DataRoot root = (DataRoot) o;
    return path.equals(root.path);
  }

  public int hashCode() {
    return path.hashCode();
  }

  ////////////////

  public String getFileLocationFromRequestPath(String reqPath) {

    if (datasetScan != null) {
      // LOOK should check to see if its been filtered out by scan
      return getFileLocationFromRequestPath(reqPath, datasetScan.getPath(), datasetScan.getScanLocation(), false);

    } else if (catScan != null) {
      // LOOK should check to see if its allowed in fc
      return getFileLocationFromRequestPath(reqPath, catScan.getPath(), catScan.getLocation(), false);

    } else if (featCollection != null) {
      // LOOK should check to see if its allowed in fc
      return getFileLocationFromRequestPath(reqPath, featCollection.getPath(), featCollection.getTopDirectoryLocation(),
          true);

    } else { // must be a datasetRoot
      // LOOK should check to see if it exists ??
      return getFileLocationFromRequestPath(reqPath, getPath(), getDirLocation(), false);
    }

  }

  // Package private for testing
  static String getFileLocationFromRequestPath(String reqPath, String rootPath, String rootLocation,
      boolean isFeatureCollection) {
    if (reqPath == null)
      return null;
    if (reqPath.length() == 0)
      return null;

    if (reqPath.startsWith("/"))
      reqPath = reqPath.substring(1);

    if (!reqPath.startsWith(rootPath))
      return null;

    final String relativeLocation = getRelativeLocation(reqPath, rootPath, isFeatureCollection);

    final MFile rootMFile = MFiles.create(rootLocation);
    final MFile mFile = rootMFile.getChild(relativeLocation);
    return mFile == null ? null : mFile.getPath();
  }

  private static String getRelativeLocation(String reqPath, String rootPath, boolean isFeatureCollection) {
    // remove the matching part, the rest is the "data directory"
    String locationRelative = reqPath.substring(rootPath.length());
    if (isFeatureCollection && locationRelative.startsWith("/files")) {
      locationRelative = locationRelative.substring(7);
    }

    if (locationRelative.startsWith("/")) {
      locationRelative = locationRelative.substring(1);
    }

    return locationRelative;
  }
}
