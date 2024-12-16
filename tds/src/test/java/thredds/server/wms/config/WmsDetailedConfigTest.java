/*
 * Copyright (c) 2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.wms.config;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import thredds.server.wms.ThreddsWmsCatalogue;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.Extents;

import java.awt.Color;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests the parsing and application of wms settings contained in wmsConfig.xml
 *
 * Settings should be determined using the following order of precedence:
 *
 * default < standard_name match < url path match < variable name match
 */
public class WmsDetailedConfigTest {

  private static final String configFileLocation = "../tds/src/main/webapp/WEB-INF/altContent/startup/wmsConfig.xml";
  private static final String testFile =
      "../tds/src/main/webapp/WEB-INF/altContent/startup/public/testdata/2004050300_eta_211.nc";

  private static final String varNameInAWmsConfig = "Z_sfc";
  private static final String standardNameInWmsConfig = "sea_water_potential_temperature";
  private static final String tdsDatasetPathInWwsConfig = "testAll/2004050300_eta_211.nc";

  private static final String varNameNotInAWmsConfig = "SupDawg";
  private static final String standardNameNotInWmsConfig = "nonstandardNameHa";
  private static final String tdsDatasetPathNotInWwsConfig = "yoloBoiiiii";

  private static final Extent<Float> defaultExtent = Extents.newExtent(-50.0f, 50.0f);
  private static final Extent<Float> standardNameExtent = Extents.newExtent(268.0f, 308.0f);
  private static final String defaultColorPalette = "psu-viridis";
  private static final boolean defaultAllowFeatureInfo = true;

  private static WmsDetailedConfig wmsConfig;
  private static NetcdfDataset ncd;

  @BeforeClass
  public static void setup() throws IOException {
    wmsConfig = WmsDetailedConfig.fromLocation(configFileLocation);
    ncd = NetcdfDatasets.openDataset(testFile);
  }

  @Test
  public void testSettingsDefault() throws IOException {
    // Create layer setting that does not match an entry in the wmsConfig.xml file based on either path, standard name,
    // or variable name
    Parameter var = new Parameter(varNameNotInAWmsConfig, "title", "description", "units", standardNameNotInWmsConfig);
    VariableMetadata variableMetadata = new VariableMetadata(var, null, null, null);
    ThreddsWmsCatalogue layerCatalog = new ThreddsWmsCatalogue(ncd, tdsDatasetPathNotInWwsConfig);
    LayerSettings settings = wmsConfig.getSettings(layerCatalog, variableMetadata);

    assertThat(settings.getDefaultPaletteName()).isEqualTo(defaultColorPalette);
    assertThat(settings.isAllowFeatureInfo()).isEqualTo(defaultAllowFeatureInfo);
    assertThat(settings.getDefaultColorScaleRange()).isEqualTo(defaultExtent);
  }

  @Test
  public void testSettingsStandardName() throws IOException {
    // Create layer setting that does not match an entry in the wmsConfig.xml file based on either path or variable name
    // but does match on standard name with same units as in config - extent should be the same as found in
    // wmsConfig.xml
    Parameter var = new Parameter(varNameNotInAWmsConfig, "title", "description", "K", standardNameInWmsConfig);
    VariableMetadata variableMetadata = new VariableMetadata(var, null, null, null);
    ThreddsWmsCatalogue layerCatalog = new ThreddsWmsCatalogue(ncd, tdsDatasetPathNotInWwsConfig);
    LayerSettings settings = wmsConfig.getSettings(layerCatalog, variableMetadata);

    assertThat(settings.getDefaultPaletteName()).isEqualTo(defaultColorPalette);
    assertThat(settings.isAllowFeatureInfo()).isEqualTo(defaultAllowFeatureInfo);
    assertThat(settings.getDefaultColorScaleRange()).isEqualTo(standardNameExtent);
  }

  @Test
  public void testSettingsStandardNameDifferentUnit() throws IOException {
    // Create layer setting that does not match an entry in the wmsConfig.xml file based on either path or variable name
    // but does match on standard name with a different udunit compatible unit - extent values should be converted to
    // new unit
    Parameter var =
        new Parameter(varNameNotInAWmsConfig, "title", "description", "degree_celsius", standardNameInWmsConfig);
    VariableMetadata variableMetadata = new VariableMetadata(var, null, null, null);
    ThreddsWmsCatalogue layerCatalog = new ThreddsWmsCatalogue(ncd, tdsDatasetPathNotInWwsConfig);
    LayerSettings settings = wmsConfig.getSettings(layerCatalog, variableMetadata);

    assertThat(settings.getDefaultPaletteName()).isEqualTo(defaultColorPalette);
    assertThat(settings.isAllowFeatureInfo()).isEqualTo(defaultAllowFeatureInfo);
    assertThat(settings.getDefaultColorScaleRange()).isEqualTo(Extents.newExtent(-5.15f, 34.85f));
  }

  @Test
  public void testSettingsStandardNameNonUdunit() throws IOException {
    // Create layer setting that does not match an entry in the wmsConfig.xml file based on either path or variable name
    // but does match on standard name with a non udunit compatible unit - should fallback to use the default extent
    Parameter var = new Parameter(varNameNotInAWmsConfig, "title", "description", "wut", standardNameInWmsConfig);
    VariableMetadata variableMetadata = new VariableMetadata(var, null, null, null);
    ThreddsWmsCatalogue layerCatalog = new ThreddsWmsCatalogue(ncd, tdsDatasetPathNotInWwsConfig);
    LayerSettings settings = wmsConfig.getSettings(layerCatalog, variableMetadata);

    assertThat(settings.getDefaultPaletteName()).isEqualTo(defaultColorPalette);
    assertThat(settings.isAllowFeatureInfo()).isEqualTo(defaultAllowFeatureInfo);
    assertThat(settings.getDefaultColorScaleRange()).isEqualTo(defaultExtent);
  }

  @Test
  public void testSettingsPathMatch() throws IOException {
    // Create layer setting that matches an entry in the wmsConfig.xml file based on path, but not variable name or
    // standard name
    // should get palette name and isAllowFeature from path match
    Parameter var = new Parameter(varNameNotInAWmsConfig, "title", "description", "units", standardNameNotInWmsConfig);
    VariableMetadata variableMetadata = new VariableMetadata(var, null, null, null);
    ThreddsWmsCatalogue layerCatalog = new ThreddsWmsCatalogue(ncd, tdsDatasetPathInWwsConfig);
    LayerSettings settings = wmsConfig.getSettings(layerCatalog, variableMetadata);

    assertThat(settings.getDefaultPaletteName()).isEqualTo("x-Occam");
    assertThat(settings.isAllowFeatureInfo()).isFalse();
    assertThat(settings.getDefaultColorScaleRange()).isEqualTo(defaultExtent);
  }

  @Test
  public void testSettingsPathVarNameMatch() throws IOException {
    // create layer setting that matches an entry in wmsConfig based on path and variable name, but not standard name
    // should get palette name and isAllowFeature from path match, extent from variable name match
    Parameter var = new Parameter(varNameInAWmsConfig, "title", "description", "m", standardNameNotInWmsConfig);
    VariableMetadata variableMetadata = new VariableMetadata(var, null, null, null);
    ThreddsWmsCatalogue layerCatalog = new ThreddsWmsCatalogue(ncd, tdsDatasetPathInWwsConfig);
    LayerSettings settings = wmsConfig.getSettings(layerCatalog, variableMetadata);

    assertThat(settings.getDefaultPaletteName()).isEqualTo("x-Occam");
    assertThat(settings.isAllowFeatureInfo()).isFalse();
    assertThat(settings.getDefaultColorScaleRange()).isEqualTo(Extents.newExtent(0.0f, 2920.0f));
  }

  @Test
  public void testSettingsPathVarStandardNameMatch() throws IOException {
    // create layer setting that matches an entry in wmsConfig based on path, variable name, and standard name
    // should get palette name and isAllowFeature from path match, extent from variable name match. standard_name match
    // should not impact this as it has a lower precedence than both path and variable name match.
    Parameter var = new Parameter(varNameInAWmsConfig, "title", "description", "m", standardNameInWmsConfig);
    VariableMetadata variableMetadata = new VariableMetadata(var, null, null, null);
    ThreddsWmsCatalogue layerCatalog = new ThreddsWmsCatalogue(ncd, tdsDatasetPathInWwsConfig);
    LayerSettings settings = wmsConfig.getSettings(layerCatalog, variableMetadata);

    assertThat(settings.getDefaultPaletteName()).isEqualTo("x-Occam");
    assertThat(settings.isAllowFeatureInfo()).isFalse();
    assertThat(settings.getDefaultColorScaleRange()).isEqualTo(Extents.newExtent(0.0f, 2920.0f));
    assertThat(settings.getDefaultAboveMaxColor()).isEqualTo(Color.BLACK);
    assertThat(settings.getDefaultBelowMinColor()).isEqualTo(Color.BLUE);
    assertThat(settings.getDefaultNoDataColor()).isEqualTo(new Color(0, 0, 0, 0));
    assertThat(settings.getDefaultOpacity()).isWithin(0.01f).of(95.0f);
  }

  @AfterClass
  public static void teardown() throws IOException {
    ncd.close();
  }
}
