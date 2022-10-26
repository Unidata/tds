/*
 * Copyright (c) 1998-2022 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.catalog;

import static com.google.common.truth.Truth.assertThat;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.test.util.TestOnLocalServer;

public class TestAccessLinks {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Test
  public void shouldNotContainFilePathHref() {
    final String path = TestOnLocalServer.withHttpPath("/catalog/catalogs5/testServices.html?dataset=TESTsst");
    final String result = new String(TestOnLocalServer.getContent(path, HttpStatus.SC_OK), StandardCharsets.UTF_8);

    final List<String> hrefs = findHrefs(result);
    assertThat(hrefs.size()).isGreaterThan(0);

    for (String href : hrefs) {
      assertThat(href).doesNotContain("file:");
    }
  }

  private static List<String> findHrefs(String htmlContent) {
    Matcher matcher = Pattern.compile("href=\".*?\"").matcher(htmlContent);
    List<String> matches = new ArrayList<>();
    while (matcher.find()) {
      matches.add(matcher.group());
    }
    return matches;
  }
}
