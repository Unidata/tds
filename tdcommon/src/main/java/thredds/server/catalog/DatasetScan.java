/*
 * Copyright (c) 1998 - 2014. University Corporation for Atmospheric Research/Unidata
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation. Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package thredds.server.catalog;

import thredds.client.catalog.*;
import thredds.client.catalog.builder.AccessBuilder;
import thredds.client.catalog.builder.CatalogBuilder;
import thredds.client.catalog.builder.CatalogRefBuilder;
import thredds.client.catalog.builder.DatasetBuilder;
import thredds.inventory.CollectionConfig;
import thredds.inventory.MController;
import thredds.inventory.MControllers;
import thredds.inventory.MFile;
import thredds.inventory.MFileFilter;
import thredds.inventory.MFiles;
import thredds.inventory.filter.CompositeMFileFilter;
import thredds.inventory.filter.LastModifiedLimit;
import thredds.inventory.filter.RegExpMatchOnName;
import thredds.inventory.filter.WildcardMatchOnName;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.units.DateRange;
import ucar.nc2.units.DateType;
import ucar.nc2.units.TimeDuration;
import javax.annotation.concurrent.Immutable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * DatasetScan
 * Look: how to check if this directory should not be shown, eg not in a catalog?
 *
 * @author John
 * @since 1/12/2015
 */
@Immutable
public class DatasetScan extends CatalogRef {
  static private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatasetScan.class);
  static private Service latestService, fileService;
  static private AllowedServicesIF allowedServices;

  static public void setSpecialServices(Service _latestService, Service _fileService) {
    if (latestService != null && !latestService.equals(_latestService)) // mocking framework sets multiple times
      throw new RuntimeException("latestService cannot be changed once set");
    latestService = _latestService;
    fileService = _fileService;
  }

  static public void setAllowedServices(AllowedServicesIF _allowedServices) {
    allowedServices = _allowedServices;
  }

  private final DatasetScanConfig config;
  private final AddTimeCoverageEnhancer addTimeCoverage;
  private final List<RegExpNamer> namers;
  private final CompositeMFileFilter fileFilters;
  private final CompositeMFileFilter dirFilters;

  public DatasetScan(DatasetNode parent, String name, String xlink, Map<String, Object> flds,
      List<AccessBuilder> accessBuilders, List<DatasetBuilder> datasetBuilders, DatasetScanConfig config) {
    super(parent, name, xlink, flds, accessBuilders, datasetBuilders);
    this.config = config;

    addTimeCoverage = (config.addTimeCoverage != null) ? new AddTimeCoverageEnhancer(config.addTimeCoverage) : null;

    // namers
    if (config.namers != null && config.namers.size() > 0) {
      namers = new ArrayList<>();
      for (DatasetScanConfig.Namer cname : config.namers)
        namers.add(new RegExpNamer(cname));
    } else {
      namers = null;
    }

    // filters
    if (config.filters != null && config.filters.size() > 0) {
      fileFilters = new CompositeMFileFilter();
      dirFilters = new CompositeMFileFilter();
      for (DatasetScanConfig.Filter cfilter : config.filters) {
        makeFilter(cfilter);
      }
    } else {
      fileFilters = null;
      dirFilters = null;
    }

  }

  private void makeFilter(DatasetScanConfig.Filter cfilter) {
    MFileFilter filter;
    if (cfilter.wildcardAttVal != null) {
      filter = new WildcardMatchOnName(cfilter.wildcardAttVal); // always on name, not path

    } else if (cfilter.regExpAttVal != null) {
      filter = new RegExpMatchOnName(cfilter.regExpAttVal);

    } else if (cfilter.lastModLimitAttVal > 0) {
      filter = new LastModifiedLimit(cfilter.lastModLimitAttVal);

    } else {
      log.error("Unimplemented DatasetScan filter " + cfilter);
      return;
    }

    if (cfilter.collection)
      dirFilters.addFilter(filter, cfilter.includer);
    if (cfilter.atomic)
      fileFilters.addFilter(filter, cfilter.includer);
  }

  public String getPath() {
    return config.path;
  }

  public String getScanLocation() {
    return config.scanDir;
  }

  DatasetScanConfig getConfig() {
    return config;
  }

  /////////////////////////////////////////////////////////

  /**
   * Called from DataRootManager.makeDynamicCatalog(), called from LocalCatalogServiceController ...
   * <p>
   * Build a catalog for the given path by scanning the location
   * associated with this DatasetScan. The given path must start with the path of this DatasetScan.
   *
   * @param orgPath the part of the baseURI that is the path
   * @param baseURI the base URL for the catalog, used to resolve relative URLs.
   * @return the catalog for this path or null if build unsuccessful.
   */
  public CatalogBuilder makeCatalogForDirectory(String orgPath, URI baseURI) throws IOException {

    // Get the dataset location.
    String dataDirRelative = translatePathToReletiveLocation(orgPath, config.path);
    if (dataDirRelative == null) {
      String tmpMsg =
          "makeCatalogForDirectory(): Requesting path <" + orgPath + "> must start with \"" + config.path + "\".";
      log.error(tmpMsg);
      return null;
    }
    if (!dataDirRelative.endsWith("/"))
      dataDirRelative += "/";
    String parentPath = (dataDirRelative.length() > 1) ? config.path + "/" + dataDirRelative : config.path + "/";
    String id = this.getId();
    if (id == null)
      id = config.path;
    String parentId = (dataDirRelative.length() > 1) ? id + "/" + dataDirRelative : id + "/";
    String dataDirComplete = getDataDir(config.scanDir, dataDirRelative);

    // Setup and create catalog builder.
    CatalogBuilder catBuilder = new CatalogBuilder();
    catBuilder.setBaseURI(baseURI);
    assert this.getParentCatalog() != null;
    for (Service s : this.getParentCatalog().getServices())
      catBuilder.addService(s);

    DatasetBuilder top = new DatasetBuilder(null);
    String name = (dataDirRelative.length() > 1) ? dataDirRelative : getName();
    top.transferMetadata(this, true);
    top.setName(name);
    top.put(Dataset.Id, null); // no id for top

    // move service name, dataType to inherited
    String serviceName = getServiceNameDefault();
    String featureTypeName = getFeatureTypeName();

    if (serviceName == null && featureTypeName != null) {
      ucar.nc2.constants.FeatureType ft = ucar.nc2.constants.FeatureType.getType(featureTypeName);
      Service stdService = (ft != null && allowedServices != null) ? allowedServices.getStandardServices(ft) : null;
      if (stdService != null) {
        catBuilder.addService(stdService);
        top.putInheritedField(ServiceName, stdService.getName());
      }
    }

    if (serviceName != null) {
      top.put(ServiceName, null);
      top.putInheritedField(ServiceName, serviceName);
    }

    if (featureTypeName != null) {
      top.put(FeatureType, null);
      top.putInheritedField(FeatureType, featureTypeName);
    }

    catBuilder.addDataset(top);

    MFile directory = MFiles.create(dataDirComplete);
    if (!directory.exists()) {
      throw new FileNotFoundException("Directory does not exist. URL path = " + orgPath);
    }
    if (!directory.isDirectory()) {
      throw new FileNotFoundException("Not a directory. URL path = " + orgPath);
    }

    // scan and sort the directory
    List<MFile> mfiles = getSortedFiles(directory, config.getSortFilesAscending());

    if (config.addLatest != null && config.addLatest.latestOnTop)
      top.addDataset(makeLatestProxy(top, parentId));

    // create Datasets
    for (MFile mfile : mfiles) {
      DatasetBuilder ds;

      if (mfile.isDirectory()) {
        CatalogRefBuilder catref = new CatalogRefBuilder(top);
        catref.setTitle(makeName(mfile));
        catref.setHref(mfile.getName() + "/catalog.xml");
        catref.addToList(Dataset.Properties, new Property("DatasetScan", "true"));
        top.addDataset(catref);
        ds = catref;

      } else {
        ds = new DatasetBuilder(top);
        ds.setName(makeName(mfile));
        String urlPath = parentPath + mfile.getName();
        ds.put(Dataset.UrlPath, urlPath);
        ds.put(Dataset.DataSize, mfile.getLength()); // <dataSize units="Kbytes">54.73</dataSize>
        CalendarDate date = CalendarDate.of(mfile.getLastModified());
        ds.put(Dataset.Dates, new DateType(date).setType("modified")); // <date
                                                                       // type="modified">2011-09-02T20:50:58.288Z</date>

        if (addTimeCoverage != null)
          addTimeCoverage.addMetadata(ds, mfile);

        if (allowedServices != null && !allowedServices.isAThreddsDataset(mfile.getName())) {
          ds.addToList(Dataset.Properties, new Property(NotAThreddsDataset, "true"));
          top.put(ServiceName, fileService.getName());
        }

        top.addDataset(ds);
      }

      ds.put(Dataset.Id, parentId + mfile.getName());
    }

    if (config.addLatest != null && !config.addLatest.latestOnTop)
      top.addDataset(makeLatestProxy(top, parentId));

    // make the catalog
    return catBuilder;
  }

  private static String getDataDir(String scanDir, String dataDirRelative) {
    if (dataDirRelative.length() <= 1) {
      return scanDir;
    }
    final MFile rootMFile = MFiles.create(scanDir);
    final MFile mFile = rootMFile.getChild(dataDirRelative);
    return mFile == null ? scanDir : mFile.getPath();
  }

  ///////////////////////
  // Scan and sort

  private List<MFile> getSortedFiles(MFile directory, final boolean isSortIncreasing) throws IOException {

    // scan the directory
    List<MFile> mfiles = getFiles(directory);

    // sort them
    Collections.sort(mfiles, new Comparator<MFile>() {
      public int compare(MFile o1, MFile o2) {
        if (o1.isDirectory() != o2.isDirectory())
          return o1.isDirectory() ? 1 : -1;

        if (isSortIncreasing)
          return o1.getName().compareTo(o2.getName());
        else
          return o2.getName().compareTo(o1.getName());
      }
    });

    return mfiles;
  }

  private List<MFile> getFiles(MFile directory) throws IOException {
    final MController mController = MControllers.create(directory.getPath());
    final List<MFile> mFiles = new ArrayList<>();

    final CollectionConfig files = new CollectionConfig("files", directory.getPath(), true, fileFilters, null);
    final Iterator<MFile> fileIterator = mController.getInventoryTop(files, true);
    fileIterator.forEachRemaining(mFiles::add);

    final CollectionConfig dirs = new CollectionConfig("dirs", directory.getPath(), true, dirFilters, null);
    final Iterator<MFile> dirIterator = mController.getSubdirs(dirs, true);
    dirIterator.forEachRemaining(mFiles::add);

    return mFiles;
  }

  ////////////////////////////////////////////////
  // Naming

  private String makeName(MFile mfile) {
    if (namers == null)
      return mfile.getName();
    for (RegExpNamer namer : namers) {
      String result = namer.rename(mfile);
      if (result != null)
        return result;
    }
    return mfile.getName();
  }

  private static class RegExpNamer {
    private java.util.regex.Pattern pattern;
    DatasetScanConfig.Namer namer;

    RegExpNamer(DatasetScanConfig.Namer namer) {
      this.pattern = java.util.regex.Pattern.compile(namer.regExp);
      this.namer = namer;
    }

    public String rename(MFile mfile) {
      String name = namer.onName ? mfile.getName() : mfile.getPath();
      java.util.regex.Matcher matcher = this.pattern.matcher(name);
      if (!matcher.find())
        return null;

      StringBuffer startTime = new StringBuffer();
      matcher.appendReplacement(startTime, namer.replaceString);
      startTime.delete(0, matcher.start());

      if (startTime.length() == 0)
        return null;
      return startTime.toString();
    }
  }

  //////////////////////////////////////////////////////////
  // add TimeCovergae
  private static class AddTimeCoverageEnhancer {
    private DatasetScanConfig.AddTimeCoverage atc;
    private boolean matchOnName;
    private String matchPattern;
    private java.util.regex.Pattern pattern;

    AddTimeCoverageEnhancer(DatasetScanConfig.AddTimeCoverage atc) {
      this.atc = atc;
      this.matchOnName = (atc.matchName != null);
      this.matchPattern = (atc.matchName != null) ? atc.matchName : atc.matchPath;
      try {
        this.pattern = java.util.regex.Pattern.compile(this.matchPattern);
      } catch (java.util.regex.PatternSyntaxException e) {
        log.error("ctor(): bad match pattern <" + this.matchPattern + ">, failed to compile: " + e.getMessage());
        this.pattern = null;
      }
    }

    boolean addMetadata(DatasetBuilder dataset, MFile crDataset) {
      if (this.pattern == null)
        return false;

      String matchTargetString = (this.matchOnName) ? crDataset.getName() : crDataset.getPath();

      java.util.regex.Matcher matcher = this.pattern.matcher(matchTargetString);
      if (!matcher.find()) {
        return (false); // Pattern not found.
      }
      StringBuffer startTime = new StringBuffer();
      try {
        matcher.appendReplacement(startTime, atc.subst);
      } catch (IndexOutOfBoundsException e) {
        log.error("addMetadata(): capture group mismatch between match pattern <" + this.matchPattern
            + "> and substitution pattern <" + atc.subst + ">: " + e.getMessage());
        return (false);
      }
      startTime.delete(0, matcher.start());

      try {
        DateRange dateRange =
            new DateRange(new DateType(startTime.toString(), null, null), null, new TimeDuration(atc.duration), null);
        dataset.put(Dataset.TimeCoverage, dateRange);

      } catch (Exception e) {
        log.warn("addMetadata(): Start time <" + startTime.toString() + "> or duration <" + atc.duration
            + "> not parsable" + " (crDataset.getName() <" + crDataset.getName() + ">, this.matchPattern() <"
            + this.matchPattern + ">, this.substitutionPattern() <" + atc.subst + ">): " + e.getMessage());
        return (false);
      }

      return (true);
    }
  }

  /*
   * <dataset name="latest.xml" ID="testGridScan/latest.xml" urlPath="latest.xml">
   * <serviceName>latest</serviceName>
   * </dataset>
   */
  private DatasetBuilder makeLatestProxy(DatasetBuilder parent, String parentId) {
    DatasetBuilder proxy = new DatasetBuilder(parent);
    proxy.setName(config.addLatest.latestName);
    proxy.put(Dataset.UrlPath, config.addLatest.latestName);
    proxy.put(Dataset.Id, parentId + config.addLatest.latestName);
    proxy.put(Dataset.ServiceName, latestService.getName());
    proxy.addServiceToCatalog(latestService);
    return proxy;
  }

  /**
   * Build a catalog for the given resolver path by scanning the
   * location associated with this InvDatasetScan. The given path must start
   * with the path of this DatasetScan and refer to a resolver
   * ProxyDatasetHandler that is part of this InvDatasetScan.
   *
   * @param orgPath the part of the baseURI that is the path
   * @param baseURI the base URL for the catalog, used to resolve relative URLs.
   * @return the resolver catalog for this path (uses version 1.1) or null if build unsuccessful.
   */

  public CatalogBuilder makeCatalogForLatest(String orgPath, URI baseURI) throws IOException {
    // Get the dataset location.
    String dataDirRelative = translatePathToReletiveLocation(orgPath, config.path);
    if (dataDirRelative == null) {
      String tmpMsg =
          "makeCatalogForDirectory(): Requesting path <" + orgPath + "> must start with \"" + config.path + "\".";
      log.error(tmpMsg);
      return null;
    }
    if (!dataDirRelative.endsWith("/"))
      dataDirRelative += "/";
    String parentPath = (dataDirRelative.length() > 1) ? config.path + "/" + dataDirRelative : config.path + "/";
    String id = this.getId();
    if (id == null)
      id = config.path;
    String parentId = (dataDirRelative.length() > 1) ? id + "/" + dataDirRelative : id + "/";
    String dataDirComplete = (dataDirRelative.length() > 1) ? config.scanDir + "/" + dataDirRelative : config.scanDir;

    // Setup and create catalog builder.
    CatalogBuilder catBuilder = new CatalogBuilder();
    catBuilder.setBaseURI(baseURI);
    assert this.getParentCatalog() != null;

    for (Service s : this.getParentCatalog().getServices())
      catBuilder.addService(s);

    MFile directory = MFiles.create(dataDirComplete);
    if (!directory.exists()) {
      throw new FileNotFoundException("Directory does not exist. URL path = " + orgPath);
    }
    if (!directory.isDirectory()) {
      throw new FileNotFoundException("Not a directory. URL path = " + orgPath);
    }

    // scan and sort the directory
    List<MFile> mfiles = getSortedFiles(directory, false); // latest on top

    long now = System.currentTimeMillis();

    for (MFile mfile : mfiles) {
      if (mfile.isDirectory())
        continue;

      if (config.addLatest.lastModLimit > 0) {
        if (now - mfile.getLastModified() < config.addLatest.lastModLimit)
          continue;
      }

      // this is the one we want
      DatasetBuilder ds = new DatasetBuilder(null);
      ds.transferMetadata(this, true);

      ds.setName(makeName(mfile));
      String urlPath = parentPath + mfile.getName();
      ds.put(Dataset.UrlPath, urlPath);
      ds.put(Dataset.DataSize, mfile.getLength()); // <dataSize units="Kbytes">54.73</dataSize>
      CalendarDate date = CalendarDate.of(mfile.getLastModified());
      ds.put(Dataset.Dates, new DateType(date).setType("modified")); // <date
                                                                     // type="modified">2011-09-02T20:50:58.288Z</date>
      ds.put(Dataset.Id, parentId + mfile.getName());

      if (addTimeCoverage != null)
        addTimeCoverage.addMetadata(ds, mfile);

      catBuilder.addDataset(ds);
      break; // only the one
    }

    // make the catalog
    return catBuilder;
  }


}
