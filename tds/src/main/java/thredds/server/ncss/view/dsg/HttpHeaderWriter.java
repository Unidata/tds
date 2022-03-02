package thredds.server.ncss.view.dsg;

import org.springframework.http.HttpHeaders;
import thredds.server.ncss.controller.NcssDiskCache;
import thredds.util.ContentType;
import thredds.util.TdsPathUtils;
import ucar.nc2.NetcdfFileWriter;

public class HttpHeaderWriter {

  public static HttpHeaders getHttpHeadersForCSV(String datasetPath, boolean isStream) {
    HttpHeaders httpHeaders = new HttpHeaders();

    if (!isStream) {
      httpHeaders.set("Content-Location", datasetPath);
      String fileName = TdsPathUtils.getFileNameForResponse(datasetPath, ".csv");
      httpHeaders.set("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
      httpHeaders.add(ContentType.HEADER, ContentType.csv.getContentHeader());
    } else {
      // The problem is that the browser won't display text/csv inline.
      httpHeaders.add(ContentType.HEADER, ContentType.text.getContentHeader());
    }

    return httpHeaders;
  }

  public static HttpHeaders getHttpHeadersForXML(String datasetPath, boolean isStream) {
    HttpHeaders httpHeaders = new HttpHeaders();

    if (!isStream) {
      httpHeaders.set("Content-Location", datasetPath);
      String fileName = TdsPathUtils.getFileNameForResponse(datasetPath, ".xml");
      httpHeaders.set("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    }

    httpHeaders.set(ContentType.HEADER, ContentType.xml.getContentHeader());
    return httpHeaders;
  }

  public static HttpHeaders getHttpHeadersForNetcdf(String datasetPath, boolean isStream, NcssDiskCache ncssDiskCache,
      NetcdfFileWriter.Version version) {
    HttpHeaders httpHeaders = new HttpHeaders();

    String fileName = TdsPathUtils.getFileNameForResponse(datasetPath, version);
    String url = ncssDiskCache.getServletCachePath() + fileName;

    if (version == NetcdfFileWriter.Version.netcdf3) {
      httpHeaders.set(ContentType.HEADER, ContentType.netcdf.getContentHeader());
    } else if (version == NetcdfFileWriter.Version.netcdf4 || version == NetcdfFileWriter.Version.netcdf4_classic) {
      httpHeaders.set(ContentType.HEADER, ContentType.netcdf.getContentHeader());
    }

    httpHeaders.set("Content-Location", url);
    httpHeaders.set("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

    return httpHeaders;
  }
}
