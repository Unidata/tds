/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.catalog;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.test.util.TestOnLocalServer;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.builder.CatalogBuilder;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;

/**
 * Test catalog utilities
 */
@Category(NeedsCdmUnitTest.class)
public class TdsLocalCatalog {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static boolean showValidationMessages = false;

  public static Catalog openFromURI(URI uri) throws IOException {
    String catPath = uri.toString();
    CatalogBuilder builder = new CatalogBuilder();
    Catalog cat = builder.buildFromLocation(catPath, null);
    if (builder.hasFatalError()) {
      Assert.fail("Validate failed " + catPath + " = \n<" + builder.getErrorMessage() + ">");
    } else if (showValidationMessages)
      logger.debug("Validate ok " + catPath + " = \n<" + builder.getErrorMessage() + ">");

    return cat;
  }


  public static Catalog open(String catalogName) throws IOException {
    String catalogPath = TestOnLocalServer.withHttpPath(catalogName);
    logger.debug("\n open= " + catalogPath);

    CatalogBuilder builder = new CatalogBuilder();
    Catalog cat = builder.buildFromLocation(catalogPath, null);
    if (builder.hasFatalError()) {
      Assert.fail("Validate failed " + catalogName + " = \n<" + builder.getErrorMessage() + ">");
    } else if (showValidationMessages)
      logger.debug("Validate ok " + catalogName + " = \n<" + builder.getErrorMessage() + ">");

    return cat;
  }

  public static Catalog openDefaultCatalog() throws IOException {
    return open("/catalog.xml");
  }

  @Test
  public void readCatalog() throws IOException {
    Catalog mainCat = openDefaultCatalog();
    Assert.assertNotNull(mainCat);
  }

}
