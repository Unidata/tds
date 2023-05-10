/*
 * Copyright (c) 2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.featurecollection.cache;

import com.google.common.base.Charsets;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.inventory.MFile;
import ucar.nc2.ft.fmrc.GridDatasetInv;
import ucar.nc2.internal.dataset.ft.fmrc.InventoryCacheProvider;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Implementation of a persisted grid inventory cache using ChronicleMap.
 */
public class GridInventoryCacheChronicle implements InventoryCacheProvider {

  private static final Logger logger = LoggerFactory.getLogger(GridInventoryCacheChronicle.class);
  private static ChronicleMap<String, byte[]> cache;

  private static final int DEFAULT_ENTRIES = 1000;
  private static final int DEFAULT_BLOAT_FACTOR = 1;

  private enum AverageValueSize {
    small(4096), medium(16384), large(65536), defaultSize(small.size);

    private final int size;

    AverageValueSize(int size) {
      this.size = size;
    }
  }

  /**
   * Initialize the inventory cache
   *
   * @param cacheDir Path to the cache directory. This location will be created if it does not exist.
   * @throws IOException
   */
  public static void init(Path cacheDir) throws IOException {
    init(cacheDir, DEFAULT_ENTRIES, DEFAULT_BLOAT_FACTOR);
  }

  /**
   * Initialize the inventory cache
   *
   * @param cacheDir Path to the cache directory. This location will be created if it does not exist.
   * @param maxEntries number of entries in the cache, at most
   * @param maxBloatFactor max number of times the cache size can increase
   * @throws IOException
   */
  public static void init(Path cacheDir, int maxEntries, int maxBloatFactor) throws IOException {
    init(cacheDir, maxEntries, maxBloatFactor, AverageValueSize.defaultSize.size);
  }

  /**
   * Initialize the inventory cache
   *
   * @param cacheDir Path to the cache directory. This location will be created if it does not exist.
   * @param maxEntries number of entries in the cache, at most
   * @param maxBloatFactor max number of times the cache size can increase
   * @param averageValueSizeName a name of one of the {@link AverageValueSize} constants or null if the default should
   *        be used
   * @throws IOException
   */
  public static void init(Path cacheDir, int maxEntries, int maxBloatFactor, String averageValueSizeName)
      throws IOException {
    final int averageValueSize = averageValueSizeName == null ? AverageValueSize.defaultSize.size
        : AverageValueSize.valueOf(averageValueSizeName.toLowerCase(Locale.ROOT)).size;
    init(cacheDir, maxEntries, maxBloatFactor, averageValueSize);
  }

  /**
   * Initialize the inventory cache
   *
   * @param cacheDir Path to the cache directory. This location will be created if it does not exist.
   * @param maxEntries number of entries in the cache, at most
   * @param maxBloatFactor max number of times the cache size can increase
   * @param averageValueSize the average size of a value (a grid dataset inventory) in bytes
   * @throws IOException
   */
  private static void init(Path cacheDir, int maxEntries, int maxBloatFactor, int averageValueSize) throws IOException {
    if (!Files.exists(cacheDir)) {
      logger.info("Creating cache directory at {}", cacheDir.toString());
      Files.createDirectories(cacheDir);
    }
    Path dbFile = cacheDir.resolve("GridDatasetInv.dat");
    if (Files.exists(dbFile)) {
      logger.info("Previous grid inventory cache found. Using {}", dbFile.toString());
    } else {
      logger.info("Creating new grid inventory cache file at {}", dbFile.toString());
    }
    if (cache == null) {
      logger.info("Grid inventory cache built with: maxEntries={}, maxBloatFactor={}, averageValueSize={}", maxEntries,
          maxBloatFactor, averageValueSize);

      cache = ChronicleMapBuilder.of(String.class, byte[].class).name("GridDatasetInv")
          .averageKey("/data/project/analysis/file.ext").averageValueSize(averageValueSize).entries(maxEntries)
          .maxBloatFactor(maxBloatFactor).createOrRecoverPersistedTo(dbFile.toFile());
    }
  }

  @Override
  @Nullable
  public GridDatasetInv get(MFile mfile) throws IOException {
    GridDatasetInv inv = null;
    if (cache != null) {
      String mfileLoc = mfile.getPath();
      byte[] xmlBytes = cache.getOrDefault(mfileLoc, null);
      if (xmlBytes != null) {
        inv = GridDatasetInv.readXML(xmlBytes);
        // check if version requires regenerating the inventory
        if (inv.isXmlVersionCompatible()) {
          // check if file has changed
          long fileModifiedSecs = mfile.getLastModified() / 1000; // ignore msecs
          long xmlModifiedSecs = inv.getLastModified() / 1000; // ignore msecs
          if (xmlModifiedSecs >= fileModifiedSecs) { // LOOK if fileDate is -1, will always succeed
            logger.debug("cache ok {} >= {} for {}", xmlModifiedSecs, fileModifiedSecs, mfileLoc);
          } else {
            logger.info(" cache out of date {} < {} for {}. Removing cache entry.", xmlModifiedSecs, fileModifiedSecs,
                mfileLoc);
            cache.remove(mfileLoc);
          }
        } else {
          logger.error("GridDatasetInv xml version needs upgrade for {}. Removing cache entry.", mfileLoc);
          cache.remove(mfileLoc);
        }
      }
    }
    return inv;
  }

  @Override
  public void put(MFile mfile, GridDatasetInv inventory) throws IOException {
    if (cache != null) {
      String xml = inventory.writeCompactXML(new Date(mfile.getLastModified()));
      cache.put(mfile.getPath(), xml.getBytes(Charsets.UTF_8));
    }
  }

  /**
   * Shutdown hook to close the cache.
   */
  public static void shutdown() {
    if (cache != null) {
      cache.close();
    }
  }

  /**
   * Display cache info
   */
  public static void showCache(Formatter formatter) {
    if (cache == null) {
      formatter.format("%nFMRC GridInventoryCache: turned off%n");
    } else {
      formatter.format("%nFMRC GridInventoryCache:%n");
      formatter.format("numberOfEntries=%d, ", getNumberOfEntries());
      formatter.format("remainingAutoResizes=%d, ", getRemainingAutoResizes());
      formatter.format("percentageFreeSpace=%d, ", getPercentageFreeSpace());
      formatter.format("offHeapMemoryUsed=%d", getOffHeapMemoryUsed());
      formatter.format("%n");
    }
  }

  // For testing
  static void resetCache() {
    shutdown();
    cache = null;
  }

  static long getNumberOfEntries() {
    return cache.longSize();
  }

  static int getRemainingAutoResizes() {
    return cache.remainingAutoResizes();
  }

  static int getPercentageFreeSpace() {
    return cache.percentageFreeSpace();
  }

  static long getOffHeapMemoryUsed() {
    return cache.offHeapMemoryUsed();
  }
}
