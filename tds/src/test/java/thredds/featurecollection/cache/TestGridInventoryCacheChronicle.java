package thredds.featurecollection.cache;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Charsets;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.filesystem.MFileOS;
import thredds.inventory.MFile;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.fmrc.GridDatasetInv;
import ucar.nc2.time.CalendarDate;
import ucar.unidata.util.test.TestDir;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

public class TestGridInventoryCacheChronicle {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // takes up 316 bytes in the cache
  private static final String SMALL_FILE = "src/test/content/thredds/public/testdata/testData.nc";
  // takes up 15661 bytes in the cache
  private static final String LARGE_FILE =
      TestDir.cdmUnitTestDir + "ft/fmrc/cache/SILAM-AQ-glob06_v5_8_2022090900_002.nc4";

  @Rule
  public final TemporaryFolder tempFolder = new TemporaryFolder();

  @BeforeClass
  public static void resetBeforeClass() {
    GridInventoryCacheChronicle.resetCache();
  }

  @After
  public void resetAfterEachTest() {
    GridInventoryCacheChronicle.resetCache();
  }

  @Test
  public void shouldInitializeDefaultCache() throws IOException {
    GridInventoryCacheChronicle.init(tempFolder.getRoot().toPath());
    final File[] files = tempFolder.getRoot().listFiles();
    assertThat(files).isNotNull();
    assertThat(files.length).isEqualTo(1);
    assertThat(files[0].getName()).endsWith("GridDatasetInv.dat");
  }

  @Test
  public void shouldRetrieveFromCache() throws IOException {
    GridInventoryCacheChronicle.init(tempFolder.getRoot().toPath());
    final GridInventoryCacheChronicle cache = new GridInventoryCacheChronicle();

    final MFile mFile = new MFileOS(SMALL_FILE);
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

  @Test
  public void shouldFitMaxEntriesInCacheWithDefaultAverageValueSize() throws IOException {
    final int maxEntries = 10;
    GridInventoryCacheChronicle.init(tempFolder.getRoot().toPath(), maxEntries, 1);
    assertThat(GridInventoryCacheChronicle.getRemainingAutoResizes()).isEqualTo(1);

    putInCache(maxEntries, SMALL_FILE);
    assertThat(GridInventoryCacheChronicle.getRemainingAutoResizes()).isEqualTo(1);
    assertThat(GridInventoryCacheChronicle.getNumberOfEntries()).isEqualTo(maxEntries);
  }

  @Test
  public void shouldFitMaxEntriesInCacheWithMediumAverageValueSize() throws IOException {
    final int maxEntries = 10;
    GridInventoryCacheChronicle.init(tempFolder.getRoot().toPath(), maxEntries, 1, "MEDIUM");
    assertThat(GridInventoryCacheChronicle.getRemainingAutoResizes()).isEqualTo(1);

    putInCache(maxEntries, SMALL_FILE);
    assertThat(GridInventoryCacheChronicle.getRemainingAutoResizes()).isEqualTo(1);
    assertThat(GridInventoryCacheChronicle.getNumberOfEntries()).isEqualTo(maxEntries);
  }

  @Category(NeedsCdmUnitTest.class)
  @Test
  public void shouldFitLessThanMaxEntriesInCacheForLargeFileWithSmallAverageValueSize() throws IOException {
    final int maxEntries = 10;
    GridInventoryCacheChronicle.init(tempFolder.getRoot().toPath(), maxEntries, 1, "Small");
    assertThat(GridInventoryCacheChronicle.getRemainingAutoResizes()).isEqualTo(1);

    try {
      putInCache(10, LARGE_FILE);
    } catch (IllegalStateException e) {
      assertThat(GridInventoryCacheChronicle.getRemainingAutoResizes()).isEqualTo(0);
      assertThat(GridInventoryCacheChronicle.getNumberOfEntries()).isLessThan(10);
      assertThat(GridInventoryCacheChronicle.getNumberOfEntries()).isGreaterThan(2);
    }
  }

  @Category(NeedsCdmUnitTest.class)
  @Test
  public void shouldFitMaxEntriesInCacheForLargeFileWithLargeAverageValueSize() throws IOException {
    final int maxEntries = 10;
    GridInventoryCacheChronicle.init(tempFolder.getRoot().toPath(), maxEntries, 1, "large");
    assertThat(GridInventoryCacheChronicle.getRemainingAutoResizes()).isEqualTo(1);

    putInCache(maxEntries, LARGE_FILE);
    assertThat(GridInventoryCacheChronicle.getRemainingAutoResizes()).isEqualTo(1);
    assertThat(GridInventoryCacheChronicle.getNumberOfEntries()).isEqualTo(10);
  }

  @Category(NeedsCdmUnitTest.class)
  @Test
  public void shouldResizeCacheWithDefaultAverageValueSize() throws IOException {
    final int maxEntries = 10;
    final int maxBloatFactor = 5;
    GridInventoryCacheChronicle.init(tempFolder.getRoot().toPath(), maxEntries, maxBloatFactor);

    try {
      putInCache(100, LARGE_FILE);
    } catch (IllegalStateException e) {
      assertThat(GridInventoryCacheChronicle.getRemainingAutoResizes()).isEqualTo(0);
      assertThat(GridInventoryCacheChronicle.getNumberOfEntries()).isAtLeast(10);
    }
  }

  @Category(NeedsCdmUnitTest.class)
  @Test
  public void shouldResizeCacheWithLargeAverageValueSize() throws IOException {
    final int maxEntries = 10;
    final int maxBloatFactor = 5;
    GridInventoryCacheChronicle.init(tempFolder.getRoot().toPath(), maxEntries, maxBloatFactor, "LARGE");

    try {
      putInCache(400, LARGE_FILE);
    } catch (IllegalStateException e) {
      assertThat(GridInventoryCacheChronicle.getRemainingAutoResizes()).isEqualTo(0);
      assertThat(GridInventoryCacheChronicle.getNumberOfEntries()).isAtLeast(100);
    }
  }

  private void putInCache(int numberOfTimes, String filename) throws IOException {
    final GridInventoryCacheChronicle cache = new GridInventoryCacheChronicle();
    final GridDatasetInv gridDatasetInv = new GridDatasetInv(GridDataset.open(filename), CalendarDate.of(0));
    logger.debug("Value size (bytes): " + calculateValueSize(filename));

    for (int i = 0; i < numberOfTimes; i++) {
      final MFile mFile = new MFileOS("key" + i);
      cache.put(mFile, gridDatasetInv);
      logger.debug("Number of entries = {}, Remaining auto resizes = {}, % free space = {}",
          GridInventoryCacheChronicle.getNumberOfEntries(), GridInventoryCacheChronicle.getRemainingAutoResizes(),
          GridInventoryCacheChronicle.getPercentageFreeSpace());
    }
  }

  private static int calculateValueSize(String file) throws IOException {
    try (final GridDataset gridDataset = GridDataset.open(file)) {
      final GridDatasetInv inv = new GridDatasetInv(gridDataset, CalendarDate.of(0));
      return inv.writeCompactXML(new Date()).getBytes(Charsets.UTF_8).length;
    }
  }
}
