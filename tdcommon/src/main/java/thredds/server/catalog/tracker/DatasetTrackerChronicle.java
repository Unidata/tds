package thredds.server.catalog.tracker;

import java.util.Locale;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import thredds.client.catalog.Access;
import thredds.client.catalog.Dataset;
import thredds.client.catalog.ServiceType;
import thredds.server.catalog.DatasetScan;
import thredds.server.catalog.FeatureCollectionRef;
import java.io.File;
import java.io.IOException;
import java.util.Formatter;
import java.util.Map;

/**
 * DatasetTracker using ChronicleMap
 *
 * @author John
 * @since 6/8/2015
 */
public class DatasetTrackerChronicle implements DatasetTracker {
  static private org.slf4j.Logger catalogInitLog = org.slf4j.LoggerFactory.getLogger("catalogInit");
  static private final String datasetName = "/chronicle.datasets.dat";
  // average size (bytes) of key for database, which is the path to a given dataset.
  // LOOK: is 512 a good average size? There is no length on file path, so hard to set a maximum.
  private static final int averagePathLength = 512;
  private final int averageValueSize;

  private enum AverageValueSize {
    small(200), medium(2_000), large(200_000), defaultSize(small.size);

    private final int size;

    AverageValueSize(int size) {
      this.size = size;
    }
  }

  // delete old databases
  public static void cleanupBefore(String pathname, long trackerNumber) {
    for (long tnum = trackerNumber - 1; tnum > 0; tnum--) {
      File oldDatabaseFile = new File(pathname + datasetName + "." + tnum);
      if (!oldDatabaseFile.exists())
        break;
      if (oldDatabaseFile.delete()) {
        catalogInitLog.info("DatasetTrackerChronicle deleted {} ", oldDatabaseFile.getAbsolutePath());
      } else {
        catalogInitLog.error("DatasetTrackerChronicle not able to delete {} ", oldDatabaseFile.getAbsolutePath());
      }
    }
  }

  private boolean alreadyExists;
  private boolean changed;
  private File dbFile;
  private long maxDatasets;
  private ChronicleMap<String, DatasetExt> datasetMap;

  public DatasetTrackerChronicle(String pathname, long maxDatasets, long number) {
    this(pathname, maxDatasets, number, AverageValueSize.defaultSize.size);
  }

  public DatasetTrackerChronicle(String pathname, long maxDatasets, long number, String averageValueSizeName) {
    this(pathname, maxDatasets, number, averageValueSizeName == null ? AverageValueSize.defaultSize.size
        : AverageValueSize.valueOf(averageValueSizeName.toLowerCase(Locale.ROOT)).size);
  }

  private DatasetTrackerChronicle(String pathname, long maxDatasets, long number, int averageValueSize) {
    dbFile = new File(pathname + datasetName + "." + number);
    alreadyExists = dbFile.exists();
    this.maxDatasets = maxDatasets;
    this.averageValueSize = averageValueSize;

    try {
      open();
      catalogInitLog.info("DatasetTrackerChronicle opened success on '" + dbFile.getAbsolutePath() + "'");

    } catch (Throwable e) {
      catalogInitLog.error(
          "DatasetTrackerChronicle failed on '" + dbFile.getAbsolutePath() + "', delete catalog cache and reload ", e);
      reinit();
    }
  }

  public void save() throws IOException {
    if (changed) {
      System.out.printf("datasetMap was saved%n");
      datasetMap.close();
      open();
    }
  }

  public void close() throws IOException {
    if (datasetMap != null) {
      datasetMap.close();
      System.out.printf("datasetMap.close() was called%n");
    }
  }

  public boolean exists() {
    return alreadyExists;
  }

  public boolean reinit() {
    if (datasetMap != null) {
      datasetMap.close();
    }
    if (dbFile.exists()) {
      if (!dbFile.delete()) {
        catalogInitLog.error("DatasetTrackerChronicle not able to delete {} ", dbFile.getAbsolutePath());
        return false;
      }
    }

    try {
      open();
      alreadyExists = true;
      return true;

    } catch (Throwable e) {
      catalogInitLog.error(
          "DatasetTrackerChronicle failed on '" + dbFile.getAbsolutePath() + "', delete catalog cache and reload ", e);
      return false;
    }
  }

  private void open() throws IOException {
    ChronicleMapBuilder<String, DatasetExt> builder = ChronicleMapBuilder.of(String.class, DatasetExt.class)
        .averageValueSize(averageValueSize).entries(maxDatasets).averageKeySize(averagePathLength)
        .valueMarshaller(DatasetExtBytesMarshaller.INSTANCE).skipCloseOnExitHook(true);
    datasetMap = builder.createPersistedTo(dbFile);
    changed = false;
  }

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
    datasetMap.put(path, dsext);
    changed = true;
    return true;
  }

  public String findResourceControl(String path) {
    DatasetExt dext = datasetMap.get(path);
    if (dext == null)
      return null;
    return dext.getRestrictAccess();
  }

  public String findNcml(String path) {
    DatasetExt dext = datasetMap.get(path);
    if (dext == null)
      return null;
    return dext.getNcml();
  }

  @Override
  public void showDB(Formatter f) {
    f.format("ChronicleMap %s%n", dbFile.getPath());
    int count = 0;
    for (Map.Entry<String, DatasetExt> entry : datasetMap.entrySet()) {
      f.format("%4d: '%s' == %s%n", count++, entry.getKey(), entry.getValue());
      if (count % 10 == 0)
        f.format("%n");
    }
  }

  // Package private for testing
  long getCount() {
    return datasetMap.longSize();
  }
}
