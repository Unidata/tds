/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.ncss.format;

import thredds.util.ContentType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An enumeration of the possible ncss download formats
 */
public enum SupportedFormat {

  CSV_STREAM("csv", true, false, ".csv", ContentType.csv, "csv_stream", "text/csv"),
  CSV_FILE("csv_file", false, false, ".csv", ContentType.csv),

  XML_STREAM("xml", true, false, ".xml", ContentType.xml, "xml_stream"),
  XML_FILE("xml_file", false, false, ".xml", ContentType.xml),

  NETCDF3("netcdf3", false, true, ".nc", ContentType.netcdf, "netcdf"),
  NETCDF4("netcdf4-classic", false, true, ".nc4", ContentType.netcdf, "netcdf4"),
  NETCDF4EXT("netcdf4ext", false, true, ".nc4", ContentType.netcdf),

  JSON("json", false, false, ".json", ContentType.json, "geojson"),
  WKT("wkt", false, false, ".txt", ContentType.text),

  WATERML2("waterml2", true, false, ".xml", ContentType.xml, "waterml2");

  private final List<String> aliases;
  private final String formatName;
  private final String fileSuffix;
  private final ContentType contentType;
  private final boolean isStream;
  private final boolean isBinary;

  SupportedFormat(String formatName, boolean isStream, boolean isBinary, String fileSuffix, ContentType contentType,
      String... aliases) {
    this.formatName = formatName;
    this.isStream = isStream;
    this.isBinary = isBinary;
    this.fileSuffix = fileSuffix;
    this.contentType = contentType;
    List<String> aliasesList = new ArrayList<>();
    Collections.addAll(aliasesList, aliases);
    this.aliases = Collections.unmodifiableList(aliasesList);
  }

  public String getFormatName() {
    return formatName;
  }

  public String getMimeType() {
    return contentType.toString();
  }

  public List<String> getAliases() {
    return aliases;
  }

  public boolean isAlias(String want) {
    if (want.equalsIgnoreCase(formatName))
      return true;
    // if (want.equalsIgnoreCase(mimeType)) return true;
    for (String have : aliases)
      if (have.equalsIgnoreCase(want))
        return true;
    return false;
  }

  public boolean isStream() {
    return isStream;
  }

  public boolean isBinary() {
    return isBinary;
  }

  public boolean isText() {
    return !isBinary;
  }

  public String getFileSuffix() {
    return fileSuffix;
  }
}
