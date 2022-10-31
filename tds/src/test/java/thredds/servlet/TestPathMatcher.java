package thredds.servlet;

import static com.google.common.truth.Truth.assertThat;

import java.lang.invoke.MethodHandles;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.server.catalog.DataRoot;
import thredds.server.catalog.DataRootPathMatcher;
import thredds.server.catalog.tracker.DataRootExt;
import thredds.server.catalog.tracker.DataRootTracker;

/**
 * Test PathMatcher
 *
 * @author caron
 * @since 10/30/13
 */
@RunWith(Parameterized.class)
public class TestPathMatcher {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String[] DATA_ROOTS = new String[] {"/thredds/dods/test/longer", "/thredds/dods/test",
      "/thredds-test/dods/test", "/thredds/dods/tester", "/thredds/dods/short", "/actionable", "myworld", "mynot",
      "ncmodels", "ncmodels/bzipped"};

  private final String path;
  private final String match;
  private static DataRootPathMatcher matcher;

  public TestPathMatcher(String path, String match) {
    this.path = path;
    this.match = match;
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[][] getTestParameters() {
    return new Object[][] {{"nope", null}, {"/thredds/dods/test", "/thredds/dods/test"},
        {"/thredds/dods/test/lo", "/thredds/dods/test"}, {"/thredds-test/dods/test/longer", "/thredds-test/dods/test"},
        {"/thredds/dods/test/longer/donger", "/thredds/dods/test/longer"}, {"myworldly", null}, {"/my", null},
        {"mysnot", null}, {"ncmodels/canonical", "ncmodels"}};
  }

  @BeforeClass
  public static void before() {
    final DataRootTracker tracker = new DataRootTracker("path", true, null);
    for (String dataRoot : DATA_ROOTS) {
      final DataRootExt dataRootExt = new DataRootExt(new DataRoot(dataRoot, null, null), null);
      tracker.trackDataRoot(dataRootExt);
    }
    matcher = new DataRootPathMatcher(null, tracker);
  }

  @Test
  public void shouldReturnLongestMatchingPath() {
    final String result = matcher.findLongestPathMatch(path);
    assertThat(result).isEqualTo(match);
  }
}
