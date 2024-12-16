/*
 * Copyright (c) 2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.wms.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.Logger;
import thredds.server.wms.ThreddsWmsCatalogue;
import ucar.nc2.units.SimpleUnit;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.Extents;

import javax.annotation.Nullable;

/**
 * Parse and provide interface to wmsConfig.xml
 *
 * wmsConfig.xml allows a TDS Admin a way to set default values to be used for generating images from the wms service.
 * The following properties can be set in the global defaults section:
 *
 * <ul>
 * <li>allowFeatureInfo</li>
 * <li>defaultColorScaleRange</li>
 * <li>defaultAboveMaxColor</li>
 * <li>defaultBelowMinColor</li>
 * <li>defaultNoDataColor</li>
 * <li>defaultOpacity</li>
 * <li>defaultPaletteName</li>
 * <li>defaultNumColorBands</li>
 * <li>logScaling</li>
 * <li>intervalTime</li>
 * </ul>
 *
 * The global default settings, except for allowFeatureInfo, can be overridden by matching standard_name attribute
 * of a variable. All settings can be overridden by matching on TDS URL path. Finally, with the exception of
 * allowFeatureInfo, settings can be overridden by matching on the variable name. The order of precedence on which
 * settings are determined is as follows:
 *
 * {@literal default < standard_name match < url path match < variable name match}
 *
 */
public class WmsDetailedConfig {

  private static final Logger startupLog = org.slf4j.LoggerFactory.getLogger("serverStartup");

  private LayerSettings defaultSettings;

  /** Maps standard names to corresponding default settings */
  private final Map<String, StandardNameSettings> standardNames = new HashMap<>();

  /** Maps dataset paths to corresponding default settings */
  private final Map<String, DatasetPathSettings> datasetPaths = new HashMap<>();

  /** Private constructor to prevent direct instantiation */
  private WmsDetailedConfig() {}

  /**
   * Parses the XML file from the given location on disk.
   * 
   * @return a new WmsDetailedConfig object, if and only if parsing was successful.
   */
  @Nullable
  public static WmsDetailedConfig fromLocation(String wmsConfigFileLocation) {
    WmsDetailedConfig wmsConfig = null;
    boolean loadingFailure = true;

    try (InputStream in = new FileInputStream(wmsConfigFileLocation)) {
      wmsConfig = loadFromStream(in);
      loadingFailure = false;
    } catch (IOException e) {
      startupLog.warn("Cannot read wmsConfig.xml:");
      startupLog.warn(e.getMessage());
    }

    if (loadingFailure) {
      startupLog.info("Will try using the default wmsConfig.xml file shipped with the TDS war file.");
    }

    return wmsConfig;
  }

  /**
   * Load wmsConfig.xml from its InputStream
   *
   * @param in InputStream of the wmsConfig.xml file
   * @return A new WmdDetailedConfig object. May be empty if there is an issue parsing wmsConfig.xml
   */
  public static WmsDetailedConfig loadFromStream(InputStream in) {
    WmsDetailedConfig wmsConfig = new WmsDetailedConfig();
    try {
      SAXBuilder builder = new SAXBuilder();
      builder.setXMLReaderFactory(XMLReaders.NONVALIDATING);
      builder.setExpandEntities(false);
      Document doc = builder.build(in);
      // Load the global default settings
      XPathExpression<Element> defaultSettingsExpression = XPathFactory.instance().compile("/wmsConfig/global/defaults",
          Filters.element(), null, Namespace.NO_NAMESPACE);
      Element defaultSettingsEl = defaultSettingsExpression.evaluateFirst(doc);

      // We don't have to check for a null return value since we validated
      // the document against the DTD upon reading. Similarly, we know that
      // all the default settings are non-null: null values would have caused
      // a validation error
      wmsConfig.defaultSettings = new LayerSettings(defaultSettingsEl);

      // Load the overrides for specific standard names
      XPathExpression<Element> standardNamesExpression = XPathFactory.instance()
          .compile("/wmsConfig/global/standardNames/standardName", Filters.element(), null, Namespace.NO_NAMESPACE);
      List<Element> standardNamesList = standardNamesExpression.evaluate(doc);

      for (Element standardNameEl : standardNamesList) {
        StandardNameSettings sns = new StandardNameSettings(standardNameEl);
        wmsConfig.standardNames.put(sns.getStandardName(), sns);
      }

      // Load the overrides for specific dataset paths
      XPathExpression<Element> datasetPathsExpression = XPathFactory.instance()
          .compile("/wmsConfig/overrides/datasetPath", Filters.element(), null, Namespace.NO_NAMESPACE);
      List<Element> datasetPathsList = datasetPathsExpression.evaluate(doc);

      for (Element datasetPathEl : datasetPathsList) {
        DatasetPathSettings pathSettings = new DatasetPathSettings(datasetPathEl);
        wmsConfig.datasetPaths.put(pathSettings.getPathSpec(), pathSettings);
      }
    } catch (IOException e) {
      startupLog.warn("Cannot read wmsConfig.xml:");
      startupLog.warn(e.getMessage());
    } catch (JDOMException | WmsConfigException ex) {
      startupLog.warn("Cannot parse wmsConfig.xml:");
      startupLog.warn(ex.getMessage());
    }
    return wmsConfig;
  }

  /**
   * Gets the settings for the given {@link thredds.server.wms.ThreddsWmsCatalogue}.
   *
   * None of the fields will be null in the returned object.
   *
   * @param layer WMS catalog associated with the layer
   * @param variableMetadata metadata associated with the layer
   * @return Visualization settings associated with the layer
   */
  public LayerSettings getSettings(ThreddsWmsCatalogue layer, VariableMetadata variableMetadata) {
    String standardName = variableMetadata.getParameter().getStandardName();
    String unit = variableMetadata.getParameter().getUnits();
    LayerSettings settings = new LayerSettings();

    // See if there are specific overrides for this layer's dataset
    String dsPath = layer.getTdsDatasetPath();
    DatasetPathSettings dpSettings = this.getBestDatasetPathMatch(dsPath);
    if (dpSettings != null) {
      // First we look for the most specific settings, i.e. those for the variable
      LayerSettings varSettings = dpSettings.getSettingsPerVariable().get(variableMetadata.getId());
      if (varSettings != null)
        settings.replaceNullValues(varSettings);
      // Now we look at the default settings for the dataset and use them
      // to insert any currently-unset values
      LayerSettings pathDefaults = dpSettings.getDefaultSettings();
      if (pathDefaults != null)
        settings.replaceNullValues(pathDefaults);
    }

    // Now look for any per-standard name defaults
    if (standardName != null) {
      StandardNameSettings stdNameSettings = this.standardNames.get(standardName);
      if (stdNameSettings != null) {
        boolean defaultColorScaleRangeUnset = settings.getDefaultColorScaleRange() == null;
        // Set the remaining unset values
        settings.replaceNullValues(stdNameSettings.getSettings());

        // If the default color scale range was previously unset, we
        // must check the units of the new color scale range.
        if (defaultColorScaleRangeUnset && stdNameSettings.getSettings().getDefaultColorScaleRange() != null) {
          Extent<Float> newColorScaleRange =
              convertUnits(stdNameSettings.getSettings().getDefaultColorScaleRange(), stdNameSettings.getUnits(), unit);
          // If the units are not convertible, we'll set back to null
          settings.setDefaultColorScaleRange(newColorScaleRange);
        }
      }
    }
    // Use the global defaults to set any remaining unset values
    settings.replaceNullValues(this.defaultSettings);
    return settings;
  }

  /**
   * Converts the given range of values to a new unit.
   *
   * @param floatRange The range of values to convert
   * @param oldUnits The units of {@code floatRange}
   * @param newUnits The units into which the range is to be converted
   * @return a new Range object containing the same values as {@code floatRange}
   *         but in the new units. If the units are not convertible, this method shall
   *         return null.
   */
  @Nullable
  private static Extent<Float> convertUnits(Extent<Float> floatRange, String oldUnits, String newUnits) {
    SimpleUnit oldUnit = SimpleUnit.factory(oldUnits);
    SimpleUnit newUnit = SimpleUnit.factory(newUnits);
    if (oldUnit == null || newUnit == null) {
      return null;
    }
    try {
      return Extents.newExtent((float) oldUnit.convertTo(floatRange.getLow(), newUnit),
          (float) oldUnit.convertTo(floatRange.getHigh(), newUnit));
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Find the dataset path settings that best match the given url path, or null
   * if there is no match. If multiple settings match the url path, an exact
   * match will "win". If there is no exact match the longest pattern in the config
   * file "wins" (crudely, this is probably the most precise match).
   *
   * @param urlPath The TDS URL path of a dataset
   * @return the layer settings based on closest match to the TDS URL path of the dataset
   */
  @Nullable
  private DatasetPathSettings getBestDatasetPathMatch(String urlPath) {
    // First look for an exact match (small optimization)
    DatasetPathSettings settings = this.datasetPaths.get(urlPath);
    if (settings != null)
      return settings;

    // Now look through all the settings for a pattern match, retaining the
    // match with the longest pattern.
    int longestPatternMatchLength = 0;
    DatasetPathSettings bestMatch = null;
    for (DatasetPathSettings dpSettings : this.datasetPaths.values()) {
      if (dpSettings.pathSpecMatches(urlPath)) {
        if (dpSettings.getPathSpec().length() > longestPatternMatchLength) {
          longestPatternMatchLength = dpSettings.getPathSpec().length();
          bestMatch = dpSettings;
        }
      }
    }
    return bestMatch;
  }
}
