/*
 * Copyright (c) 1998-2021 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import thredds.server.wms.TdsEnhancedVariableMetadata;
import thredds.server.wms.ThreddsWmsCatalogue;
import thredds.server.wms.config.WmsDetailedConfig;
import uk.ac.rdg.resc.edal.graphics.utils.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.utils.SldTemplateStyleCatalogue;

/**
 * Centralize the mapping of threddsConfig.xml configuration settings to the data objects used by
 * the various servlets. Supports earlier versions (some deprecated) of threddsConfig.xml config settings.
 * <p/>
 * called from TdsInit
 *
 * @author edavis
 * @since 4.1
 */
@Component
class TdsConfigMapper {
  // ToDo Not yet using ThreddsConfig.get<type>() methods.

  @Autowired
  private TdsServerInfoBean tdsServerInfo;
  @Autowired
  private HtmlConfigBean htmlConfig;
  @Autowired
  private WmsConfigBean wmsConfig;
  @Autowired
  private TdsUpdateConfigBean tdsUpdateConfig;

  private static final Logger startupLog = org.slf4j.LoggerFactory.getLogger("serverStartup");

  // static so can be called from static enum classes
  private static String getValueFromThreddsConfig(String key, String alternateKey, String defaultValue) {
    String value = ThreddsConfig.get(key, null);
    if (value == null && alternateKey != null)
      value = ThreddsConfig.get(alternateKey, null);
    if (value == null)
      value = defaultValue;
    return value;
  }

  enum ServerInfoMappings {
    SERVER_NAME("serverInformation.name", "htmlSetup.installName", "Initial TDS Installation"),
    SERVER_LOGO_URL("serverInformation.logoUrl", "htmlSetup.installLogoUrl", "threddsIcon.png"),
    SERVER_LOGO_ALT_TEXT("serverInformation.logoAltText", "htmlSetup.installLogoAlt", "Initial TDS Installation"),
    SERVER_ABSTRACT("serverInformation.abstract", null, "Scientific Data"),
    SERVER_KEYWORDS("serverInformation.keywords", null, "meteorology, atmosphere, climate, ocean, earth science"),
    SERVER_CONTACT_NAME("serverInformation.contact.name", null, ""),
    SERVER_CONTACT_ORGANIZATION("serverInformation.contact.organization", null, ""),
    SERVER_CONTACT_EMAIL("serverInformation.contact.email", null, ""),
    SERVER_CONTACT_PHONE("serverInformation.contact.phone", null, ""),
    SERVER_HOST_INSTITUTION_NAME("serverInformation.hostInstitution.name", "htmlSetup.hostInstName", ""),
    SERVER_HOST_INSTITUTION_WEBSITE("serverInformation.hostInstitution.webSite", "htmlSetup.hostInstUrl", ""),
    SERVER_HOST_INSTITUTION_LOGO_URL("serverInformation.hostInstitution.logoUrl", "htmlSetup.hostInstLogoUrl", ""),
    SERVER_HOST_INSTITUTION_LOGO_ALT_TEXT("serverInformation.hostInstitution.logoAltText", "htmlSetup.hostInstLogoAlt",
        "");

    private String key;
    private String alternateKey; // deprecated
    private String defaultValue;

    ServerInfoMappings(String key, String alternateKey, String defaultValue) {
      if (key == null || defaultValue == null)
        throw new IllegalArgumentException("The key and default value may not be null.");

      this.key = key;
      this.alternateKey = alternateKey;
      this.defaultValue = defaultValue;
    }

    String getValueFromThreddsConfig() {
      return TdsConfigMapper.getValueFromThreddsConfig(this.key, this.alternateKey, this.defaultValue);
    }

    static void load(TdsServerInfoBean info) {
      info.setName(SERVER_NAME.getValueFromThreddsConfig());
      info.setLogoUrl(SERVER_LOGO_URL.getValueFromThreddsConfig());
      info.setLogoAltText(SERVER_LOGO_ALT_TEXT.getValueFromThreddsConfig());
      info.setSummary(SERVER_ABSTRACT.getValueFromThreddsConfig());
      info.setKeywords(SERVER_KEYWORDS.getValueFromThreddsConfig());

      info.setContactName(SERVER_CONTACT_NAME.getValueFromThreddsConfig());
      info.setContactOrganization(SERVER_CONTACT_ORGANIZATION.getValueFromThreddsConfig());
      info.setContactEmail(SERVER_CONTACT_EMAIL.getValueFromThreddsConfig());
      info.setContactPhone(SERVER_CONTACT_PHONE.getValueFromThreddsConfig());

      info.setHostInstitutionName(SERVER_HOST_INSTITUTION_NAME.getValueFromThreddsConfig());
      info.setHostInstitutionWebSite(SERVER_HOST_INSTITUTION_WEBSITE.getValueFromThreddsConfig());
      info.setHostInstitutionLogoUrl(SERVER_HOST_INSTITUTION_LOGO_URL.getValueFromThreddsConfig());
      info.setHostInstitutionLogoAltText(SERVER_HOST_INSTITUTION_LOGO_ALT_TEXT.getValueFromThreddsConfig());
      // make the server info available to the ThreddsWmsCatalogue class, which handles exposing server contact info
      // via the wms service.
      ThreddsWmsCatalogue.setTdsServerInfo(info);
    }
  }


  enum HtmlConfigMappings {
    HTML_STANDARD_CSS_URL("htmlSetup.standardCssUrl", null, ""),
    HTML_CATALOG_CSS_URL("htmlSetup.catalogCssUrl", null, ""),
    HTML_DATASET_CSS_URL("htmlSetup.datasetCssUrl", null, ""),
    HTML_OPENDAP_CSS_URL("htmlSetup.openDapCssUrl", null, "tdsDap.css"),
    GOOGLE_TRACKING_CODE("htmlSetup.googleTrackingCode", null, ""),

    HTML_FOLDER_ICON_URL("htmlSetup.folderIconUrl", null, "folder.gif"),
    HTML_FOLDER_ICON_ALT("htmlSetup.folderIconAlt", null, "Folder"),
    HTML_DATASET_ICON_URL("htmlSetup.datasetIconUrl", null, ""),
    HTML_DATASET_ICON_ALT("htmlSetup.datasetIconAlt", null, ""),
    HTML_USE_REMOTE_CAT_SERVICE("htmlSetup.useRemoteCatalogService", null, "true"),
    HTML_GENERATE_DATASET_JSON_LD("htmlSetup.generateDatasetJsonLD", null, "false");

    private String key;
    private String alternateKey;
    private String defaultValue;

    HtmlConfigMappings(String key, String alternateKey, String defaultValue) {
      if (key == null || defaultValue == null)
        throw new IllegalArgumentException("The key and default value may not be null.");

      this.key = key;
      this.alternateKey = alternateKey;
      this.defaultValue = defaultValue;
    }

    String getValueFromThreddsConfig() {
      return TdsConfigMapper.getValueFromThreddsConfig(this.key, this.alternateKey, this.defaultValue);
    }

    static void load(HtmlConfigBean htmlConfig, TdsContext tdsContext, TdsServerInfoBean serverInfo) {
      htmlConfig.init(tdsContext.getWebappDisplayName(), tdsContext.getWebappVersion(),
          tdsContext.getTdsVersionBuildDate(), tdsContext.getContextPath());

      htmlConfig.setInstallName(serverInfo.getName());
      htmlConfig.setInstallLogoUrl(serverInfo.getLogoUrl());
      htmlConfig.setInstallLogoAlt(serverInfo.getLogoAltText());

      htmlConfig.setHostInstName(serverInfo.getHostInstitutionName());
      htmlConfig.setHostInstUrl(serverInfo.getHostInstitutionWebSite());
      htmlConfig.setHostInstLogoUrl(serverInfo.getHostInstitutionLogoUrl());
      htmlConfig.setHostInstLogoAlt(serverInfo.getHostInstitutionLogoAltText());

      htmlConfig.setPageCssUrl(HTML_STANDARD_CSS_URL.getValueFromThreddsConfig());
      htmlConfig.setCatalogCssUrl(HTML_CATALOG_CSS_URL.getValueFromThreddsConfig());
      htmlConfig.setDatasetCssUrl(HTML_DATASET_CSS_URL.getValueFromThreddsConfig());
      htmlConfig.setOpenDapCssUrl(HTML_OPENDAP_CSS_URL.getValueFromThreddsConfig());
      htmlConfig.setGoogleTrackingCode(GOOGLE_TRACKING_CODE.getValueFromThreddsConfig());

      htmlConfig.setFolderIconUrl(HTML_FOLDER_ICON_URL.getValueFromThreddsConfig());
      htmlConfig.setFolderIconAlt(HTML_FOLDER_ICON_ALT.getValueFromThreddsConfig());
      htmlConfig.setDatasetIconUrl(HTML_DATASET_ICON_URL.getValueFromThreddsConfig());
      htmlConfig.setDatasetIconAlt(HTML_DATASET_ICON_ALT.getValueFromThreddsConfig());

      htmlConfig
          .setUseRemoteCatalogService(Boolean.parseBoolean(HTML_USE_REMOTE_CAT_SERVICE.getValueFromThreddsConfig()));
      htmlConfig
          .setGenerateDatasetJsonLD(Boolean.parseBoolean(HTML_GENERATE_DATASET_JSON_LD.getValueFromThreddsConfig()));
    }
  }

  enum WmsConfigMappings {
    WMS_ALLOW("WMS.allow", null, "true"),
    WMS_ALLOW_REMOTE("WMS.allowRemote", null, "false"),
    WMS_PALETTE_LOCATION_DIR("WMS.paletteLocationDir", null, null),
    WMS_STYLES_LOCATION_DIR("WMS.stylesLocationDir", null, null),
    WMS_MAXIMUM_IMAGE_WIDTH("WMS.maxImageWidth", null, "2048"),
    WMS_MAXIMUM_IMAGE_HEIGHT("WMS.maxImageHeight", null, "2048"),
    WMS_CONFIG_FILE("WMS.configFile", null, null);

    private String key;
    private String alternateKey;
    private String defaultValue;

    WmsConfigMappings(String key, String alternateKey, String defaultValue) {
      if (key == null)
        throw new IllegalArgumentException("The key may not be null.");

      this.key = key;
      this.alternateKey = alternateKey;
      this.defaultValue = defaultValue;
    }

    String getDefaultValue() {
      return this.defaultValue;
    }

    String getValueFromThreddsConfig() {
      return TdsConfigMapper.getValueFromThreddsConfig(this.key, this.alternateKey, this.defaultValue);
    }

    static void load(WmsConfigBean wmsConfig, TdsContext tdsContext) {
      final String defaultPaletteLocation = tdsContext.getThreddsDirectory() + "/wmsPalettes";
      final String defaultStylesLocation = tdsContext.getThreddsDirectory() + "/wmsStyles";
      final String defaultWmsConfigFile = tdsContext.getThreddsDirectory() + "/wmsConfig.xml";

      wmsConfig.setAllow(Boolean.parseBoolean(WMS_ALLOW.getValueFromThreddsConfig()));
      wmsConfig.setAllowRemote(Boolean.parseBoolean(WMS_ALLOW_REMOTE.getValueFromThreddsConfig()));

      final String paletteLocation =
          getValueFromThreddsConfigOrDefault(WMS_PALETTE_LOCATION_DIR, defaultPaletteLocation);
      wmsConfig.setPaletteLocationDir(paletteLocation);
      try {
        startupLog.info("Loading custom WMS palette files from " + paletteLocation);
        ColourPalette.addPaletteDirectory(new File(paletteLocation));
      } catch (FileNotFoundException e) {
        // If there is an error adding a custom palette, it is logged in the server startup log by edal-java.
        // If there is an error with the directory itself, it will throw a FileNotFoundException.
        // However, let's skip logging if the palette location is the default location, since an admin might not have
        // created a custom palette directory.
        if (!paletteLocation.equals(defaultPaletteLocation)) {
          startupLog.warn("Could not find custom palette directory {}", paletteLocation, e);
        }
      }

      final String stylesLocation = getValueFromThreddsConfigOrDefault(WMS_STYLES_LOCATION_DIR, defaultStylesLocation);
      wmsConfig.setStylesLocationDir(stylesLocation);
      try {
        startupLog.info("Loading custom WMS style files from " + stylesLocation);
        SldTemplateStyleCatalogue.getStyleCatalogue().addStylesInDirectory(new File(stylesLocation));
      } catch (FileNotFoundException e) {
        if (!stylesLocation.equals(defaultStylesLocation)) {
          startupLog.warn("Could not find custom styles directory {}", stylesLocation, e);
        }
      }

      final String wmsConfigFile = getValueFromThreddsConfigOrDefault(WMS_CONFIG_FILE, defaultWmsConfigFile);

      WmsDetailedConfig wdc = WmsDetailedConfig.fromLocation(wmsConfigFile);
      if (wdc == null) {
        String defaultWmsConfig = "/WEB-INF/altContent/startup/wmsConfig.xml";
        try (InputStream in = tdsContext.getServletContext().getResourceAsStream(defaultWmsConfig)) {
          wdc = WmsDetailedConfig.loadFromStream(in);
        } catch (IOException e) {
          startupLog.error("Cannot read wmsConfig.xml from TDS war file:");
          startupLog.error(e.getMessage());
          startupLog.error("Failed to configure WMS server. Disabling the service.");
          wmsConfig.setAllow(false);
          wmsConfig.setAllowRemote(false);
        }
      }

      wmsConfig.setWmsDetailedConfig(wdc);

      try {
        wmsConfig.setMaxImageWidth(Integer.parseInt(WMS_MAXIMUM_IMAGE_WIDTH.getValueFromThreddsConfig()));
      } catch (NumberFormatException e) {
        // If the given maxImageWidth value is not a number, try the default value.
        wmsConfig.setMaxImageWidth(Integer.parseInt(WMS_MAXIMUM_IMAGE_WIDTH.getDefaultValue()));
      }

      try {
        wmsConfig.setMaxImageHeight(Integer.parseInt(WMS_MAXIMUM_IMAGE_HEIGHT.getValueFromThreddsConfig()));
      } catch (NumberFormatException e) {
        // If the given maxImageHeight value is not a number, try the default value.
        wmsConfig.setMaxImageHeight(Integer.parseInt(WMS_MAXIMUM_IMAGE_HEIGHT.getDefaultValue()));
      }
      // make the wmsConfig available to the TdsEnhancedVariableMetadata and ThreddsWmsCatalogue classes,
      // which handle the default WMS values as well as WMS values based on standard names or paths.
      TdsEnhancedVariableMetadata.setWmsConfig(wmsConfig);
      ThreddsWmsCatalogue.setWmsConfig(wmsConfig);
    }
  }

  private static String getValueFromThreddsConfigOrDefault(WmsConfigMappings property, String defaultValue) {
    final String value = property.getValueFromThreddsConfig();
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  enum TdsUpdateConfigMappings {
    TDSUPDAATE_LOGVERSIONINFO("TdsUpdateConfig.logVersionInfo", null, "true");

    private String key;
    private String alternateKey;
    private String defaultValue;

    TdsUpdateConfigMappings(String key, String alternateKey, String defaultValue) {
      if (key == null) {
        throw new IllegalArgumentException("The key may not be null.");
      }

      this.key = key;
      this.alternateKey = alternateKey;
      this.defaultValue = defaultValue;
    }

    String getDefaultValue() {
      return this.defaultValue;
    }

    String getValueFromThreddsConfig() {
      return TdsConfigMapper.getValueFromThreddsConfig(this.key, this.alternateKey, this.defaultValue);
    }

    static void load(TdsUpdateConfigBean tdsUpdateConfig) {
      tdsUpdateConfig.setLogVersionInfo(Boolean.parseBoolean(TDSUPDAATE_LOGVERSIONINFO.getValueFromThreddsConfig()));
    }
  }

  /////////////////////////////////////////////////////////////////////

  TdsConfigMapper() {}

  void init(TdsContext tdsContext) {
    ServerInfoMappings.load(tdsServerInfo);
    HtmlConfigMappings.load(htmlConfig, tdsContext, tdsServerInfo);
    WmsConfigMappings.load(wmsConfig, tdsContext);
    TdsUpdateConfigMappings.load(tdsUpdateConfig);
  }

}
