/*
 * Copyright (c) 2010 The University of Reading
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 * authors or contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package thredds.server.wms.config;

import java.awt.Color;
import org.jdom2.Element;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.EdalParseException;
import uk.ac.rdg.resc.edal.graphics.utils.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.utils.GraphicsUtils;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * Simple Java bean encapsulating the settings (allowFeatureInfo, defaultColorScaleRange, defaultPaletteName and
 * logScaling) at a particular part of the config XML document. A Null value for a field implies that that field has
 * not been set in the document and a default should be used.
 *
 * Ported to edal-java by sarms (2020-05-30)
 *
 * @author Jon
 */
public class LayerSettings {

  private Boolean allowFeatureInfo = null;
  private Extent<Float> defaultColorScaleRange = null;
  private Color defaultAboveMaxColor, defaultBelowMinColor, defaultNoDataColor = null;
  private Float defaultOpacity = null;
  private String defaultPaletteName = null;
  private Boolean logScaling = null;
  private Boolean intervalTime = null;
  private Integer defaultNumColorBands = null;

  LayerSettings(Element parentElement) throws WmsConfigException {
    if (parentElement == null)
      return; // Create a set of layer settings with all-null fields
    this.allowFeatureInfo = getBoolean(parentElement, "allowFeatureInfo");
    this.defaultColorScaleRange = getRange(parentElement, "defaultColorScaleRange");
    this.defaultAboveMaxColor = getColor(parentElement, "defaultAboveMaxColor");
    this.defaultBelowMinColor = getColor(parentElement, "defaultBelowMinColor");
    this.defaultNoDataColor = getColor(parentElement, "defaultNoDataColor");
    this.defaultOpacity = getFloat(parentElement, "defaultOpacity", Extents.newExtent(0f, 100f));
    this.defaultPaletteName = parentElement.getChildTextTrim("defaultPaletteName");
    // If the default palette name tag is used, it must be populated
    // TODO: can we check this against the installed palettes?
    if (this.defaultPaletteName != null && this.defaultPaletteName.isEmpty()) {
      throw new WmsConfigException("defaultPaletteName must contain a value");
    }
    this.defaultNumColorBands =
        getInteger(parentElement, "defaultNumColorBands", Extents.newExtent(5, ColourPalette.MAX_NUM_COLOURS));
    this.logScaling = getBoolean(parentElement, "logScaling");
    this.intervalTime = getBoolean(parentElement, "intervalTime");
    if (this.intervalTime == null) {
      this.intervalTime = false;
    }
  }

  /**
   * Package-private constructor, sets all fields to null
   */
  LayerSettings() {}


  private static Boolean getBoolean(Element parentElement, String childName) throws WmsConfigException {
    String str = parentElement.getChildTextTrim(childName);
    if (str == null)
      return null;
    if (str.equalsIgnoreCase("true"))
      return Boolean.TRUE;
    if (str.equalsIgnoreCase("false"))
      return Boolean.FALSE;
    throw new WmsConfigException("Value of " + childName + " must be true or false");
  }

  private static Integer getInteger(Element parentElement, String childName, Extent<Integer> validRange)
      throws WmsConfigException {
    String str = parentElement.getChildTextTrim(childName);
    if (str == null)
      return null;
    int val;
    try {
      val = Integer.parseInt(str);
    } catch (NumberFormatException nfe) {
      throw new WmsConfigException(nfe);
    }
    if (val < validRange.getLow())
      return validRange.getLow();
    else if (val > validRange.getHigh())
      return validRange.getHigh();
    else
      return val;
  }

  private static Float getFloat(Element parentElement, String childName, Extent<Float> validRange)
      throws WmsConfigException {
    String str = parentElement.getChildTextTrim(childName);
    if (str == null)
      return null;
    float val;
    try {
      val = Float.parseFloat(str);
    } catch (NumberFormatException nfe) {
      throw new WmsConfigException(nfe);
    }
    if (val < validRange.getLow())
      return validRange.getLow();
    else if (val > validRange.getHigh())
      return validRange.getHigh();
    else
      return val;
  }

  private static Color getColor(Element parentElement, String childName) throws WmsConfigException {
    String str = parentElement.getChildTextTrim(childName);
    if (str == null)
      return null;
    try {
      return GraphicsUtils.parseColour(str);
    } catch (EdalParseException e) {
      throw new WmsConfigException(String.format("Invalid color value in %s", str));
    }
  }

  private static Extent<Float> getRange(Element parentElement, String childName) throws WmsConfigException {
    String str = parentElement.getChildTextTrim(childName);
    if (str == null)
      return null;
    String[] els = str.split(" ");
    if (els.length != 2) {
      throw new WmsConfigException("Invalid range format");
    }
    try {
      float min = Float.parseFloat(els[0]);
      float max = Float.parseFloat(els[1]);
      return Extents.newExtent(min, max);
    } catch (NumberFormatException nfe) {
      throw new WmsConfigException("Invalid floating-point value in range");
    }
  }

  public Boolean isAllowFeatureInfo() {
    return allowFeatureInfo;
  }

  public Extent<Float> getDefaultColorScaleRange() {
    return defaultColorScaleRange;
  }

  public Color getDefaultAboveMaxColor() {
    return defaultAboveMaxColor;
  }

  public Color getDefaultBelowMinColor() {
    return defaultBelowMinColor;
  }

  public Color getDefaultNoDataColor() {
    return defaultNoDataColor;
  }

  public Float getDefaultOpacity() {
    return defaultOpacity;
  }

  public String getDefaultPaletteName() {
    return defaultPaletteName;
  }

  public Boolean isLogScaling() {
    return logScaling;
  }

  /**
   * * @deprecated Only used in capabilities_xml*.jsp, remove in v6
   */
  @Deprecated
  public boolean isIntervalTime() {
    return intervalTime;
  }

  public Integer getDefaultNumColorBands() {
    return defaultNumColorBands;
  }

  /**
   * Replaces all unset values in this object with values from the given LayerSettings object.
   */
  void replaceNullValues(thredds.server.wms.config.LayerSettings newSettings) {
    if (this.allowFeatureInfo == null)
      this.allowFeatureInfo = newSettings.allowFeatureInfo;
    if (this.defaultColorScaleRange == null)
      this.defaultColorScaleRange = newSettings.defaultColorScaleRange;
    if (this.defaultAboveMaxColor == null)
      this.defaultAboveMaxColor = newSettings.defaultAboveMaxColor;
    if (this.defaultBelowMinColor == null)
      this.defaultBelowMinColor = newSettings.defaultBelowMinColor;
    if (this.defaultNoDataColor == null)
      this.defaultNoDataColor = newSettings.defaultNoDataColor;
    if (this.defaultOpacity == null)
      this.defaultOpacity = newSettings.defaultOpacity;
    if (this.defaultPaletteName == null)
      this.defaultPaletteName = newSettings.defaultPaletteName;
    if (this.logScaling == null)
      this.logScaling = newSettings.logScaling;
    if (this.intervalTime == null)
      this.intervalTime = newSettings.intervalTime;
    if (this.defaultNumColorBands == null)
      this.defaultNumColorBands = newSettings.defaultNumColorBands;
  }

  void setDefaultColorScaleRange(Extent<Float> defaultColorScaleRange) {
    this.defaultColorScaleRange = defaultColorScaleRange;
  }

  @Override
  public String toString() {
    return String.format(
        "allowFeatureInfo = %s, defaultColorScaleRange = %s, defaultAboveMaxColor = %s, defaultBelowMinColor = %s, defaultNoDataColor = %s, defaultOpacity = %s, defaultPaletteName = %s, defaultNumColorBands = %s, logScaling = %s",
        this.allowFeatureInfo, this.defaultColorScaleRange, this.defaultAboveMaxColor, this.defaultBelowMinColor,
        this.defaultNoDataColor, this.defaultOpacity, this.defaultPaletteName, this.defaultNumColorBands,
        this.logScaling);
  }
}
