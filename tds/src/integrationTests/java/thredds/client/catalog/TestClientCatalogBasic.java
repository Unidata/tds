/*
 * Copyright (c) 2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.client.catalog;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.test.util.ClientCatalogUtil;
import thredds.test.util.TestOnLocalServer;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;

/**
 * @author cwardgar
 * @since 2015-10-12
 */
@RunWith(Enclosed.class)
public class TestClientCatalogBasic {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @RunWith(Parameterized.class)
  public static class TestLocalClientCatalogBasic {

    @Parameterized.Parameters(name = "{0}")
    public static List<String> getTestParameters() {
      return Arrays.asList("enhancedCat.xml", "TestAlias.xml", "testMetadata.xml", "nestedServices.xml",
          "TestHarvest.xml", "TestFilter.xml");
    }

    @Parameterized.Parameter()
    public String catalogFragment;

    @Test
    public void shouldParseLocalCatalog() throws IOException {
      final Catalog catalog = ClientCatalogUtil.open(catalogFragment);
      assertThat(catalog).isNotNull();

      for (Dataset ds : catalog.getDatasetsLocal()) {
        testDatasets(ds);
      }
    }
  }

  @RunWith(Parameterized.class)
  @Category(NeedsCdmUnitTest.class) // For Metar_Station_Data_fc.cdmr.
  public static class TestRemoteClientCatalogBasic {

    @Parameterized.Parameters(name = "{0}")
    public static List<String> getTestParameters() {
      return Arrays.asList("catalog.xml",
          "catalog/testStationFeatureCollection/catalog.xml?dataset=testStationFeatureCollection/Metar_Station_Data_fc.cdmr");
    }

    @Parameterized.Parameter()
    public String catalogFragment;

    @Test
    public void shouldParseRemoteCatalog() throws IOException {
      final Catalog catalog = ClientCatalogUtil.open(TestOnLocalServer.withHttpPath(catalogFragment));
      assertThat(catalog).isNotNull();

      for (Dataset ds : catalog.getDatasetsLocal()) {
        testDatasets(ds);
      }
    }
  }

  private static void testDatasets(Dataset d) {
    testAccess(d);
    testProperty(d);
    testDocs(d);
    testMetadata(d);
    testContributors(d);
    testKeywords(d);
    testProjects(d);
    testPublishers(d);
    testVariables(d);

    for (Dataset ds : d.getDatasetsLocal()) {
      testDatasets(ds);
    }
  }

  private static void testAccess(Dataset d) {
    for (Access a : d.getAccess()) {
      assertThat(a.getService()).isNotNull();
      assertThat(a.getUrlPath()).isNotNull();
      assertThat(a.getDataset()).isEqualTo(d);
      testService(a.getService());
    }
  }

  private static void testProperty(Dataset d) {
    for (Property p : d.getProperties()) {
      logger.debug("{}", p);
    }
  }

  private static void testDocs(Dataset d) {
    for (Documentation doc : d.getDocumentation()) {
      logger.debug("{}", doc);
    }
  }

  private static void testService(Service s) {
    List<Service> n = s.getNestedServices();
    if (n == null) {
      return;
    }
    if (s.getType() == ServiceType.Compound) {
      assertThat(n.size()).isGreaterThan(0);
    } else {
      assertThat(n.size()).isEqualTo(0);
    }
  }

  private static void testMetadata(Dataset d) {
    for (ThreddsMetadata.MetadataOther m : d.getMetadataOther()) {
      logger.debug("{}", m.xlinkHref);
    }
  }

  private static void testContributors(Dataset d) {
    for (ThreddsMetadata.Contributor m : d.getContributors()) {
      logger.debug("{}", m.getName());
    }
  }

  private static void testKeywords(Dataset d) {
    for (ThreddsMetadata.Vocab m : d.getKeywords()) {
      logger.debug("{}", m.getText());
    }
  }

  private static void testProjects(Dataset d) {
    for (ThreddsMetadata.Vocab m : d.getProjects()) {
      logger.debug("{}", m.getText());
    }
  }

  private static void testPublishers(Dataset d) {
    for (ThreddsMetadata.Source m : d.getPublishers()) {
      logger.debug("{}", m.getName());
    }
  }

  private static void testVariables(Dataset d) {
    for (ThreddsMetadata.VariableGroup m : d.getVariables()) {
      logger.debug("{}", m.getVocabulary());
    }
  }
}
