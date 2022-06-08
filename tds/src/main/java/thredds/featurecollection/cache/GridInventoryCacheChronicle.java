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
      cache = ChronicleMapBuilder.of(String.class, byte[].class).name("GridDatasetInv")
          .averageKey("/data/project/analysis/file.ext").averageValueSize(4096).entries(maxEntries)
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
      String xml = inventory.writeXML(new Date(mfile.getLastModified()));
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
}
