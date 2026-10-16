/*
 * Copyright (c) 2015-2026 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.catalog.tracker;

import java.io.Closeable;
import java.io.IOException;
import java.util.Formatter;
import java.util.Set;
import javax.annotation.Nullable;
import thredds.client.catalog.Dataset;
import thredds.client.catalog.ServiceType;
import thredds.server.catalog.ConfigCatalog;

/**
 * DatasetTracker interface
 *
 * @author caron
 * @since 6/6/2015
 */
public interface DatasetTracker extends Closeable {
  /*
   * return true on success
   * boolean init(String dirPath, long maxDatasets) throws IOException;
   */

  void save() throws IOException;

  void close() throws IOException;

  boolean exists(); // detect if database exists

  boolean reinit(); // throw out all and start again

  // datasets
  default boolean trackDataset(long catId, Dataset ds, Callback callback) {
    return trackDataset(catId, ds, null, callback);
  }

  /**
   * Track a dataset.
   *
   * @param declaredServices the services declared for this dataset in the config catalog, or null if none recorded
   */
  boolean trackDataset(long catId, Dataset ds, @Nullable Set<ServiceType> declaredServices, Callback callback);

  String findResourceControl(String path);

  String findNcml(String path);

  /**
   * The services declared for the dataset at this request path.
   *
   * @return the declared service types, or null if none are recorded for this path
   */
  @Nullable
  Set<ServiceType> findAllowedServices(String path);

  // debug
  void showDB(Formatter f);

  interface Callback {
    void hasDataRoot(DataRootExt dataRoot);

    void hasDataset(Dataset dd);

    void hasTrackedDataset(Dataset dd);

    void hasNcml(Dataset dd);

    void hasRestriction(Dataset dd);

    void hasCatalogRef(ConfigCatalog dd);

    void finish();
  }
}
