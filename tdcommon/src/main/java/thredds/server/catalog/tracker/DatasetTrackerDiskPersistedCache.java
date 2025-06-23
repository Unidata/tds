/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.catalog.tracker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Formatter;
import java.util.stream.Stream;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import thredds.cache.DiskPersistedCache;
import thredds.cache.DiskPersistedCache.Builder;
import thredds.client.catalog.Access;
import thredds.client.catalog.Dataset;
import thredds.client.catalog.ServiceType;
import thredds.server.catalog.DatasetScan;
import thredds.server.catalog.FeatureCollectionRef;

public class DatasetTrackerDiskPersistedCache implements DatasetTracker, AutoCloseable {

  static private org.slf4j.Logger catalogInitLog = org.slf4j.LoggerFactory.getLogger("catalogInit");

  private DiskPersistedCache<String, DatasetExt> datasetMap;
  private Builder<String, DatasetExt> builder;


  // delete old databases
  public static void cleanupBefore(String pathname, long trackerNumber) {
    for (long tnum = trackerNumber - 1; tnum > 0; tnum--) {
      Path location = trackerLocation(pathname, tnum);
      if (Files.exists(location) && Files.isDirectory(location)) {
        try (Stream<Path> tree = Files.walk(location)) {
          tree.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
          catalogInitLog.info("DatasetTrackerDiskPersistedCache deleted {} ", location);
        } catch (IOException ioe) {
          catalogInitLog.error("DatasetTrackerDiskPersistedCache not able to delete {}", location, ioe);
        }
      }
    }
  }

  private static Path trackerLocation(String trackerDir, long trackerNumber) {
    return Paths.get(trackerDir).resolve(String.format("store_%s", trackerNumber));
  }

  public DatasetTrackerDiskPersistedCache(String trackerDir, long maxDatasets) {
    this(trackerDir, 0, maxDatasets);
  }

  public DatasetTrackerDiskPersistedCache(String trackerDir, long trackerNumber, long maxDatasets) {
    Path location = trackerLocation(trackerDir, trackerNumber);
    try {
      builder = DiskPersistedCache.at(location);
      builder.maxInMemoryEntities(maxDatasets).named(String.format("datasetTracker_%s", trackerNumber));
      datasetMap = builder.build();
    } catch (IOException e) {
      catalogInitLog.error("Failed to setup DatasetTracker at {}", location, e);
    }
  }

  @Override
  public void save() throws IOException {
    // no-op
  }

  @Override
  public void close() throws IOException {
    datasetMap.shutdown();
  }

  @Override
  public boolean exists() {
    return datasetMap != null && datasetMap.running();
  }

  @Override
  public boolean reinit() {
    datasetMap.reinit();
    return true;
  }

  @Override
  public boolean trackDataset(long catId, Dataset dataset, DatasetTracker.Callback callback) {
    if (callback != null) {
      callback.hasDataset(dataset);
      boolean track = false;
      if (dataset.getRestrictAccess() != null) {
        callback.hasRestriction(dataset);
        track = true;
      }
      if (dataset.getNcmlElement() != null) {
        callback.hasNcml(dataset);
        track = true;
      }
      if (track)
        callback.hasTrackedDataset(dataset);
    }

    boolean hasRestrict = dataset.getRestrictAccess() != null;
    boolean hasNcml = (dataset.getNcmlElement() != null) && !(dataset instanceof DatasetScan)
        && !(dataset instanceof FeatureCollectionRef);
    if (!hasRestrict && !hasNcml)
      return false;

    String path = null;
    if (dataset instanceof DatasetScan) {
      path = ((DatasetScan) dataset).getPath();

    } else if (dataset instanceof FeatureCollectionRef) {
      path = ((FeatureCollectionRef) dataset).getPath();

    } else { // regular dataset
      for (Access access : dataset.getAccess()) {
        ServiceType st = access.getService().getType();
        if (st == null || !st.isStandardTdsService()) // skip non-TDS services
          continue;

        String accessPath = access.getUrlPath();
        if (accessPath == null) {
          catalogInitLog.warn("trackDataset {} access {} has null path", dataset, access);
          continue;
        }

        if (path == null)
          path = accessPath;
        else if (!path.equals(accessPath)) { // LOOK must put all for restrict
          System.out.printf(" paths differ: %s%n %s%n%n", path, accessPath);
          catalogInitLog.warn(" paths differ: {} != {}", path, accessPath);
        }
      }
      // if this is a regular dataset which uses default services, those services are not
      // part of the configuration catalog, and thus the above check will fail due to
      // a lack of access methods. Get path from the urlPath of the dataset.
      if (path == null) {
        path = dataset.getUrlPath();
      }
    }

    if (path == null) {
      catalogInitLog.debug("trackDataset {} has null path", dataset);
      return false;
    }

    String ncml = null;
    if (hasNcml) {
      // want the ncml string representation
      Element ncmlElem = dataset.getNcmlElement();
      XMLOutputter xmlOut = new XMLOutputter(Format.getCompactFormat());
      ncml = xmlOut.outputString(ncmlElem);
    }

    // changed = true;
    DatasetExt dsext = new DatasetExt(catId, dataset.getRestrictAccess(), ncml);
    if (datasetMap == null) {
      catalogInitLog.error("DatasetTracker cache not properly initialized. Can't track dataset {}", dataset);
      return false;
    }
    datasetMap.put(path, dsext);
    return true;
  }

  @Override
  public String findResourceControl(String path) {
    if (datasetMap == null) {
      return null;
    }
    DatasetExt dext = datasetMap.get(path);
    if (dext == null)
      return null;
    return dext.getRestrictAccess();
  }

  @Override
  public String findNcml(String path) {
    if (datasetMap == null) {
      return null;
    }
    DatasetExt dext = datasetMap.get(path);
    if (dext == null)
      return null;
    return dext.getNcml();
  }

  @Override
  public void showDB(Formatter f) {
    if (datasetMap == null) {
      return;
    }
    f.format("DiskPersistedCache %s%n", datasetMap.name());
    f.format("Level 1:%n");
    datasetMap.showL1Db(f);
    f.format("%n");
    f.format("Level 2:%n");
    datasetMap.showL2Db(f, 10);
  }

  long getCount() {
    return datasetMap != null ? datasetMap.numL2Keys() : -1;
  }
}
