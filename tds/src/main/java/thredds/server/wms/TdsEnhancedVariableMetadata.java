/*
 * Copyright (c) 2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.wms;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import thredds.server.config.WmsConfigBean;
import thredds.server.wms.config.LayerSettings;
import thredds.server.wms.config.WmsDetailedConfig;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.graphics.utils.EnhancedVariableMetadata;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingStyleParameters;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * Use the custom settings in wmsConfig.xml to set default visualization values based on:
 *
 * <ul>
 * <li>global defaults</li>
 * <li>TDS URL Path based defaults</li>
 * <li>Standard Name based defaults</li>
 * <li>variable name based defaults</li>
 * </ul>
 *
 * with variable name defaults taking highest precedence.
 */
public class TdsEnhancedVariableMetadata implements EnhancedVariableMetadata {

  static WmsConfigBean wmsConfig;

  final VariableMetadata metadata;
  final ThreddsWmsCatalogue layer;
  final LayerSettings layerSettings;

  public TdsEnhancedVariableMetadata(ThreddsWmsCatalogue layer, VariableMetadata metadata) {
    this.metadata = metadata;
    this.layer = layer;
    WmsDetailedConfig detailedWmsConfig = wmsConfig.getWmsDetailedConfig();
    // this is where the wmsConfig.xml settings get applied to the layer.
    layerSettings = detailedWmsConfig.getSettings(layer, metadata);
  }

  public static void setWmsConfig(WmsConfigBean config) {
    wmsConfig = config;
  }

  /**
   * @return The ID of the variable this {@link EnhancedVariableMetadata} is
   *         associated with
   */
  @Override
  public String getId() {
    return metadata.getId();
  }

  /**
   * @return The title of this layer to be displayed in the menu and the
   *         Capabilities document
   */
  @Override
  public String getTitle() {
    /*
     * Should perhaps be more meaningful/configurable?
     */
    return metadata.getParameter().getTitle();
  }

  /**
   * @return A brief description of this layer to be displayed in the
   *         Capabilities document
   */
  @Override
  public String getDescription() {
    return metadata.getParameter().getDescription();
  }

  /**
   * @return Copyright information about this layer to be displayed be clients
   */
  @Override
  public String getCopyright() {
    return null;
  }

  /**
   * @return More information about this layer to be displayed be clients
   */
  @Override
  public String getMoreInfo() {
    return null;
  }

  /**
   * @return The default plot settings for this variable - this may not return
   *         <code>null</code>, but any of the defined methods within the
   *         returned {@link PlottingStyleParameters} object may do.
   */
  @Override
  public PlottingStyleParameters getDefaultPlottingParameters() {
    List<Extent<Float>> scaleRanges = Collections.singletonList(layerSettings.getDefaultColorScaleRange());
    String palette = layerSettings.getDefaultPaletteName();
    Color aboveMaxColour = layerSettings.getDefaultAboveMaxColor();
    Color belowMinColour = layerSettings.getDefaultBelowMinColor();
    Color noDataColour = layerSettings.getDefaultNoDataColor();
    Float opacity = layerSettings.getDefaultOpacity();
    Boolean logScaling = layerSettings.isLogScaling();
    Integer numColourBands = layerSettings.getDefaultNumColorBands();

    return new PlottingStyleParameters(scaleRanges, palette, aboveMaxColour, belowMinColour, noDataColour, logScaling,
        numColourBands, opacity);
  }

  /**
   * @return Whether or not this layer can be queried with GetFeatureInfo requests
   */
  @Override
  public boolean isQueryable() {
    return layerSettings.isAllowFeatureInfo();
  };

  /**
   * @return Whether or not this layer can be downloaded in CSV/CoverageJSON format
   */
  @Override
  public boolean isDownloadable() {
    return layer.isDownloadable(metadata.getId());
  }

  /**
   * @return Whether this layer is disabled
   */
  @Override
  public boolean isDisabled() {
    return layer.isDisabled(metadata.getId());
  }

}
