package thredds.server.catalog;

import static com.google.common.truth.Truth.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestDataRoot {

  @Parameterized.Parameters
  public static List<Object[]> getTestParameters() {
    return Arrays.asList(
        // Negative cases
        new Object[] {"notAMatchingRoot/file.txt", "root", "/file/path/", false, null},

        // Local files
        new Object[] {"root/file.txt", "root", "/file/path/", false, "/file/path/file.txt"},
        new Object[] {"root/subDir/file.txt", "root", "/file/path/", false, "/file/path/subDir/file.txt"},
        new Object[] {"root/file.txt", "root", "/path/", false, "/path/file.txt"},
        new Object[] {"root/file.txt", "root", "/path", false, "/path/file.txt"},
        new Object[] {"/root/file.txt", "root", "/path/", false, "/path/file.txt"},

        // Feature collections
        new Object[] {"root/file.txt", "root", "/path/", true, "/path/file.txt"},
        new Object[] {"root/files/file.txt", "root", "/path/", true, "/path/file.txt"},

        // S3
        new Object[] {"/root/foo/", "root", "cdms3:bucket", false, "cdms3:bucket?foo/"},
        new Object[] {"root/foo", "root", "cdms3:bucket", false, "cdms3:bucket?foo"},
        new Object[] {"root/foo", "root", "cdms3:bucket?key/", false, "cdms3:bucket?key/foo"},
        new Object[] {"root/foo", "root", "cdms3:bucket?key", false, "cdms3:bucket?keyfoo"},
        new Object[] {"root/foo", "root", "cdms3:bucket#delimiter=/", false, "cdms3:bucket?foo#delimiter=/"},
        new Object[] {"root/foo", "root", "cdms3:bucket?key/#delimiter=/", false, "cdms3:bucket?key/foo#delimiter=/"},
        new Object[] {"root/foo", "root", "cdms3:bucket?key#delimiter=/", false, "cdms3:bucket?key/foo#delimiter=/"});
  }

  @Parameterized.Parameter(0)
  public String requestPath;
  @Parameterized.Parameter(1)
  public String rootPath;
  @Parameterized.Parameter(2)
  public String rootLocation;
  @Parameterized.Parameter(3)
  public boolean isFeatureCollection;
  @Parameterized.Parameter(4)
  public String expectedFileLocation;

  @Test
  public void shouldGetFileLocation() {
    final String location =
        DataRoot.getFileLocationFromRequestPath(requestPath, rootPath, rootLocation, isFeatureCollection);
    assertThat(location).isEqualTo(expectedFileLocation);
  }
}
