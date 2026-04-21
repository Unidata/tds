/*
 * Copyright (c) 1998-2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.catalog;

import org.jdom2.Element;
import java.util.List;
import java.util.Optional;

/**
 * DatasetScan Configuration object
 *
 * @author John
 * @since 1/12/2015
 */
public class DatasetScanConfig {
  public String name;
  public String path;
  public String scanDir;
  public String restrictAccess;

  // public boolean addDatasetSize = true;
  public Optional<Boolean> isSortIncreasing = Optional.empty();

  public Element ncmlElement;
  public List<Filter> filters;
  public List<Namer> namers;
  public AddLatest addLatest;
  public AddTimeCoverage addTimeCoverage;
  // include empty directories in scan to preserve behavior by default
  public boolean excludeEmptyDirs = false;

  @Override
  public String toString() {
    return "DatasetScanConfig{" + "name='" + name + '\'' + ", path='" + path + '\'' + ", scanDir='" + scanDir + '\''
        + ", restrictAccess='" + restrictAccess + '\'' + ", isSortIncreasing=" + getSortFilesAscending()
        + ", ncmlElement=" + ncmlElement + ", filters=" + filters + ", namers=" + namers + ", proxies=" + addLatest
        + ", addTimeCoverage=" + addTimeCoverage + ", excludeEmptyDirs=" + excludeEmptyDirs + '}';
  }

  public boolean getSortFilesAscending() {
    if (isSortIncreasing.isPresent())
      return isSortIncreasing.get();
    return true; // default true
  }

  public static class Filter {
    String regExpAttVal, wildcardAttVal;
    long lastModLimitAttVal;
    boolean atomic, collection, includer;

    public Filter(String regExpAttVal, String wildcardAttVal, long lastModLimitAttVal, boolean atomic,
        boolean collection, boolean includer) {
      this.regExpAttVal = regExpAttVal;
      this.wildcardAttVal = wildcardAttVal;
      this.lastModLimitAttVal = lastModLimitAttVal;
      this.atomic = atomic;
      this.collection = collection;
      this.includer = includer;
    }
  }

  public static class Namer {
    boolean onName;
    String regExp, replaceString;

    public Namer(boolean onName, String regExp, String replaceString) {
      this.onName = onName;
      this.regExp = regExp;
      this.replaceString = replaceString;
    }
  }

  public static class AddLatest {
    String latestName, serviceName;
    boolean latestOnTop;
    long lastModLimit; // millisecs

    public AddLatest() {
      latestName = "latest.xml";
      serviceName = "Resolver";
      latestOnTop = true;
      lastModLimit = -1;
    }

    public AddLatest(String latestName, String serviceName, boolean latestOnTop, long lastModLimit) {
      this.latestName = latestName;
      this.serviceName = serviceName;
      this.latestOnTop = latestOnTop;
      this.lastModLimit = lastModLimit;
    }
  }

  public static class AddTimeCoverage {
    String matchName, matchPath, subst, duration;

    public AddTimeCoverage(String matchName, String matchPath, String subst, String duration) {
      this.matchName = matchName;
      this.matchPath = matchPath;
      this.subst = subst;
      this.duration = duration;
    }
  }
}
