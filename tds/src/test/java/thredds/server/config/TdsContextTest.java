package thredds.server.config;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import thredds.mock.web.MockTdsContextLoader;
import ucar.unidata.util.test.category.NeedsContentRoot;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import ucar.unidata.util.test.category.NeedsExternalResource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml"}, loader = MockTdsContextLoader.class)
@Category({NeedsContentRoot.class, NeedsExternalResource.class})
public class TdsContextTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private TdsUpdateConfigBean tdsUpdateConfig;

  @Autowired
  private TdsContext tdsContext;

  @Test
  public void testVersionRetrieval() {
    String version = tdsContext.getVersionInfo();
    Map<String, String> latestVersionInfo = tdsUpdateConfig.getLatestVersionInfo(version);

    assertThat(latestVersionInfo).isNotEmpty();

    String releaseKey = "release";
    assertThat(latestVersionInfo).containsKey(releaseKey);
    assertThat(latestVersionInfo.get(releaseKey)).isNotEmpty();

    String developmentKey = "development";
    assertThat(latestVersionInfo).containsKey(developmentKey);
    assertThat(latestVersionInfo.get(developmentKey)).isNotEmpty();
  }
}
