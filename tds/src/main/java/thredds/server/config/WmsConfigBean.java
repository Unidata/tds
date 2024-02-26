/*
 * Copyright (c) 1998-2021 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.config;

import org.springframework.stereotype.Component;
import thredds.server.wms.config.WmsDetailedConfig;

/**
 * WMS config bean
 *
 * @author edavis
 * @since 4.1
 */
@Component
public class WmsConfigBean {
  private boolean allow;
  private boolean allowRemote;
  private String paletteLocationDir;
  private String stylesLocationDir;
  private int maxImageWidth;
  private int maxImageHeight;

  // set in TdsConfigMapper
  private WmsDetailedConfig wmsDetailedConfig;

  public boolean isAllow() {
    return allow;
  }

  public void setAllow(boolean allow) {
    this.allow = allow;
  }

  public boolean isAllowRemote() {
    return allowRemote;
  }

  public void setAllowRemote(boolean allowRemote) {
    this.allowRemote = allowRemote;
  }

  public String getPaletteLocationDir() {
    return paletteLocationDir;
  }

  public void setPaletteLocationDir(String paletteLocationDir) {
    this.paletteLocationDir = paletteLocationDir;
  }

  public String getStylesLocationDir() {
    return stylesLocationDir;
  }

  public void setStylesLocationDir(String stylesLocationDir) {
    this.stylesLocationDir = stylesLocationDir;
  }

  public int getMaxImageWidth() {
    return maxImageWidth;
  }

  public void setMaxImageWidth(int maxImageWidth) {
    this.maxImageWidth = maxImageWidth;
  }

  public int getMaxImageHeight() {
    return maxImageHeight;
  }

  public void setMaxImageHeight(int maxImageHeight) {
    this.maxImageHeight = maxImageHeight;
  }

  public WmsDetailedConfig getWmsDetailedConfig() {
    return wmsDetailedConfig;
  }

  public void setWmsDetailedConfig(WmsDetailedConfig wmsDetailedConfig) {
    this.wmsDetailedConfig = wmsDetailedConfig;
  }

}
