/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.featurecollection.cache;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Formatter;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.cache.DiskPersistedCache;
import thredds.cache.DiskPersistedCache.Builder;
import thredds.inventory.MFile;
import ucar.nc2.ft.fmrc.GridDatasetInv;
import ucar.nc2.internal.dataset.ft.fmrc.InventoryCacheProvider;

public class GridInventoryDiskPersistedCache implements InventoryCacheProvider {

  private static final Logger logger = LoggerFactory.getLogger(GridInventoryDiskPersistedCache.class);
  private static final int DEFAULT_ENTRIES = 1000;

  private static DiskPersistedCache<String, byte[]> cache;

  /**
   * Initialize the inventory cache
   *
   * @param cacheDir path to the cache directory. This location will be created if it does not exist.
   * @throws IOException error managing cache directory
   */
  public static void init(Path cacheDir) throws IOException {
    init(cacheDir, DEFAULT_ENTRIES);
  }

  /**
   * Initialize the inventory cache
   *
   * @param cacheDir path to the cache directory. This location will be created if it does not exist.
   * @param maxEntries number of entries in the in-memory cache, at most
   * @throws IOException error managing cache directory
   */
  public static void init(Path cacheDir, int maxEntries) throws IOException {
    if (cache == null) {
      Builder<String, byte[]> builder = DiskPersistedCache.at(cacheDir);
      builder.named("GridInventoryCache").maxInMemoryEntities(maxEntries);
      cache = builder.build();
    }
  }

  public static String name() {
    return cache == null ? "uninitialized" : cache.name();
  }

  /**
   * Shutdown hook to close the cache.
   */
  public static void shutdown() {
    if (cache != null && cache.running()) {
      logger.info("Shutting down GridInventoryDiskPersistedCache...");
      cache.shutdown();
      logger.info("GridInventoryDiskPersistedCache shutdown");
    }
  }

  @Nullable
  @Override
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
   * Display cache info
   */
  public static void showCache(Formatter formatter) {
    if (cache == null) {
      formatter.format("%nFMRC GridInventoryCache: turned off%n");
    } else {
      formatter.format("%nFMRC GridInventoryCache:%n");
      formatter.format("DiskPersistedCache %s%n", cache.name());
      formatter.format("Level 1:%n");
      cache.showL1Db(formatter);
      formatter.format("Level 2:%n");
      cache.showL2Db(formatter, 10);
      formatter.format("%n");
    }
  }

  // For testing
  static void resetCache() {
    shutdown();
    cache = null;
  }
}
