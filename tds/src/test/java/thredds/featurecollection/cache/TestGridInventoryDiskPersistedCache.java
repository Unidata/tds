/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.featurecollection.cache;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import thredds.filesystem.MFileOS;
import thredds.inventory.MFile;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.fmrc.GridDatasetInv;
import ucar.nc2.time.CalendarDate;

public class TestGridInventoryDiskPersistedCache {

  private static final String GRID_FILE = "src/test/content/thredds/public/testdata/testData.nc";

  @Rule
  public final TemporaryFolder tempFolder = new TemporaryFolder();

  @After
  public void tearDown() {
    GridInventoryDiskPersistedCache.resetCache();
  }

  @Test
  public void shouldInitializeDefaultCache() throws IOException {
    assertThat(GridInventoryDiskPersistedCache.name()).isEqualTo("uninitialized");
    GridInventoryDiskPersistedCache.init(tempFolder.getRoot().toPath());
    final File[] files = tempFolder.getRoot().listFiles();
    assertThat(files).isNotNull();
    assertThat(files.length).isEqualTo(2);
    assertThat(GridInventoryDiskPersistedCache.name()).isEqualTo("GridInventoryCache");
  }

  @Test
  public void shouldRetrieveFromCacheSmall() throws IOException {
    GridInventoryDiskPersistedCache.init(tempFolder.getRoot().toPath());
    final GridInventoryDiskPersistedCache cache = new GridInventoryDiskPersistedCache();
    final MFile mFile = new MFileOS(GRID_FILE);
    final CalendarDate date = CalendarDate.of(0);
    final GridDatasetInv gridDatasetInv = new GridDatasetInv(GridDataset.openIfce(mFile.getPath()), date);
    cache.put(mFile, gridDatasetInv);

    final GridDatasetInv retrievedGridDatasetInv = cache.get(mFile);
    assertThat(retrievedGridDatasetInv).isNotNull();
    assertThat(retrievedGridDatasetInv.getRunDate()).isEqualTo(date);
    assertThat(retrievedGridDatasetInv.getTimeCoords().size()).isEqualTo(gridDatasetInv.getTimeCoords().size());
    assertThat(retrievedGridDatasetInv.getVertCoords().size()).isEqualTo(gridDatasetInv.getVertCoords().size());
    assertThat(retrievedGridDatasetInv.findGrid("Z_sfc")).isNotNull();
  }
}
