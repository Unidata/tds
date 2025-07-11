/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.tdm;

import static com.google.common.truth.Truth.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class CatalogConfigReaderTest {

  static Logger logger = (Logger) LoggerFactory.getLogger(CatalogConfigReader.class);
  private static final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

  @BeforeClass
  public static void setupLogger() {
    // create and start a ListAppender
    listAppender.start();
    logger.addAppender(listAppender);
  }

  @After
  public void clearLogs() {
    listAppender.list.clear();
  }

  @AfterClass
  public static void teardownLogger() {
    listAppender.stop();
    logger.detachAppender(listAppender);
  }

  @Test
  public void catalogRefCount() throws IOException {
    String catalogXml = "src/test/data/catalogs/cat_refs.xml";
    Resource catalog = new FileSystemResource(catalogXml);
    Path rootPath = Paths.get(catalogXml).toRealPath().getParent();
    assertThat(rootPath.toFile().exists()).isTrue();
    CatalogConfigReader catReader = new CatalogConfigReader(rootPath, catalog);
    assertThat(catReader).isNotNull();
    assertThat(catReader.errlog.toString()).isEmpty();
    assertThat(catReader.getFcList()).hasSize(1);
  }

  @Test
  public void httpCatalogRefs() throws IOException {
    String catalogXml = "src/test/data/catalogs/cat_refs.xml";
    Resource catalog = new FileSystemResource(catalogXml);
    Path rootPath = Paths.get(catalogXml).toRealPath().getParent();
    assertThat(rootPath.toFile().exists()).isTrue();
    CatalogConfigReader catReader = new CatalogConfigReader(rootPath, catalog);
    List<ILoggingEvent> logsList = listAppender.list;
    assertThat(catReader).isNotNull();
    assertThat(catReader.errlog.toString()).isEmpty();
    // check for specific message about relative catalog not existing (for when
    // catalogRef points to remote catalog
    assertThat(logsList.stream().filter(e -> e.getMessage().toLowerCase().startsWith("relative catalog")
        && e.getMessage().toLowerCase().endsWith("does not exist")).count()).isEqualTo(0);

    // should not have errors in general
    assertThat(logsList.stream().filter(e -> e.getLevel() == Level.ERROR).count()).isEqualTo(0);
  }
}
