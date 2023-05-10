package thredds.server.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import thredds.mock.web.MockTdsContextLoader;
import ucar.nc2.util.DiskCache2;
import ucar.unidata.util.test.category.NeedsContentRoot;
import java.lang.invoke.MethodHandles;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml"}, loader = MockTdsContextLoader.class)
@Category(NeedsContentRoot.class)
public class ThreddsConfigTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private TdsContext tdsContext;

  private String threddsConfigPath;

  @Before
  public void setUp() {
    threddsConfigPath = tdsContext.getContentRootPathProperty() + "/thredds/threddsConfig.xml";
    ThreddsConfig.init(threddsConfigPath);
  }

  @Test
  public void testGet() {
    assertEquals("THREDDS Support", ThreddsConfig.get("serverInformation.contact.name", null));
    assertEquals("true", ThreddsConfig.get("CatalogServices.allowRemote", null));
    assertEquals(null, ThreddsConfig.get("WMS.allow", null));
    assertEquals(52428800, ThreddsConfig.getBytes("NetcdfSubsetService.maxFileDownloadSize", -1L));
    assertEquals(1024, ThreddsConfig.getInt("FeatureCollection.maxEntries", 1000));
    assertEquals(2, ThreddsConfig.getInt("FeatureCollection.maxBloatFactor", 1));
    assertEquals("medium", ThreddsConfig.get("FeatureCollection.averageValueSize", null));
  }

  // Tests the "cachePathPolicy" element, added in response to this message on the thredds mailing list:
  // https://www.unidata.ucar.edu/mailing_lists/archives/thredds/2016/msg00001.html
  @Test
  public void testCachePathPolicy() {
    String policyStr = ThreddsConfig.get("AggregationCache.cachePathPolicy", null);
    assertEquals("OneDirectory", policyStr);

    DiskCache2.CachePathPolicy policyObj = DiskCache2.CachePathPolicy.valueOf(policyStr);
    assertSame(DiskCache2.CachePathPolicy.OneDirectory, policyObj);
  }

  @Test
  public void testNetcdf4ClibraryUseForReading() {
    assertFalse(ThreddsConfig.getBoolean("Netcdf4Clibrary.useForReading", true));
  }
}
