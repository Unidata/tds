/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.wcs.v1_0_0_1;

import com.google.common.collect.ImmutableList;
import thredds.server.wcs.Request;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.ft2.coverage.*;
import ucar.nc2.ft2.coverage.writer.CFGridCoverageWriter;
import ucar.nc2.geotiff.GeotiffWriter;
import ucar.nc2.time.CalendarDateRange;
import ucar.nc2.util.DiskCache2;
import ucar.nc2.util.NamedObject;
import ucar.nc2.write.NetcdfFormatWriter;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ogc.EPSG_OGC_CF_Helper;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WcsCoverage {
  private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WcsCoverage.class);

  // ToDo WCS 1.0Plus - change FROM coverage for each parameter TO coverage for each coordinate system
  private WcsDataset wcsDataset;
  private CoverageCollection dataset;
  private Coverage coverage;
  private CoverageCoordSys coordSys;
  private String nativeCRS;

  private String defaultRequestCrs;

  private List<Request.Format> supportedCoverageFormatList;

  private WcsRangeField range;

  public WcsCoverage(@Nonnull Coverage coverage, @Nonnull CoverageCoordSys coordSys, @Nonnull WcsDataset wcsDataset) {
    this.wcsDataset = wcsDataset;
    this.coverage = coverage;
    this.coordSys = coordSys;

    dataset = wcsDataset.getDataset();
    Projection proj = coordSys.getProjection();
    this.nativeCRS = EPSG_OGC_CF_Helper.getWcs1_0CrsId(proj);

    this.defaultRequestCrs = "OGC:CRS84";

    this.supportedCoverageFormatList = new ArrayList<>();
    // this.supportedCoverageFormatList.add("application/x-netcdf");
    this.supportedCoverageFormatList.add(Request.Format.GeoTIFF);
    this.supportedCoverageFormatList.add(Request.Format.GeoTIFF_Float);
    this.supportedCoverageFormatList.add(Request.Format.GeoTIFF_Palette);
    this.supportedCoverageFormatList.add(Request.Format.NetCDF3);

    CoverageCoordAxis zaxis = coordSys.getZAxis();
    WcsRangeField.Axis vertAxis;
    if (zaxis != null && zaxis.getDependenceType() == CoverageCoordAxis.DependenceType.independent) {
      List<String> names = new ArrayList<>();
      for (NamedObject val : ((CoverageCoordAxis1D) zaxis).getCoordValueNames())
        names.add(val.getName());
      vertAxis = new WcsRangeField.Axis("Vertical", zaxis.getName(), zaxis.getDescription(), true, names);
    } else
      vertAxis = null;

    range = new WcsRangeField(this.getName(), this.getLabel(), this.getDescription(), vertAxis);
  }


  public String getName() {
    return coverage.getName();
  }

  public String getLabel() {
    return coverage.getDescription();
  }

  public String getDescription() {
    return coverage.getDescription();
  }

  public CoverageCoordSys getCoordinateSystem() {
    return coordSys;
  }

  public boolean hasMissingData() {
    return coverage.hasMissing();
  }

  public String getDefaultRequestCrs() {
    return defaultRequestCrs;
  }

  public String getNativeCrs() {
    return nativeCRS;
  }

  public List<Request.Format> getSupportedCoverageFormatList() {
    return supportedCoverageFormatList;
  }

  public boolean isSupportedCoverageFormat(Request.Format covFormat) {
    return supportedCoverageFormatList.contains(covFormat);
  }

  public WcsRangeField getRangeField() {
    return range;
  }

  static private DiskCache2 diskCache = null;

  static public void setDiskCache(DiskCache2 _diskCache) {
    diskCache = _diskCache;
  }

  static private DiskCache2 getDiskCache() {
    if (diskCache == null) {
      log.error("getDiskCache(): Disk cache has not been set.");
      throw new IllegalStateException("Disk cache must be set before calling GetCoverage.getDiskCache().");
    }
    return diskCache;
  }

  public File writeCoverageDataToFile(Request.Format format, LatLonRect bboxLatLonRect, VerticalRange verticalRange,
      CalendarDateRange timeRange) throws WcsException {

    SubsetParams params = new SubsetParams();
    if (bboxLatLonRect != null)
      params.set(SubsetParams.latlonBB, bboxLatLonRect);
    if (timeRange != null)
      params.set(SubsetParams.timeRange, timeRange);
    if (verticalRange != null) {
      double[] vr = new double[] {verticalRange.min, verticalRange.max};
      params.set(SubsetParams.vertRange, vr);
    }

    /////////
    try {
      if (format == Request.Format.GeoTIFF || format == Request.Format.GeoTIFF_Float || format == Request.Format.GeoTIFF_Palette) {
        File dir = new File(getDiskCache().getRootDirectory());
        File tifFile = File.createTempFile("WCS", ".tif", dir);
        if (log.isDebugEnabled())
          log.debug("writeCoverageDataToFile(): tifFile=" + tifFile.getPath() + " format=" + format);
        // GridCoverage subset = this.coverage.makeSubset(tRange, zRange, bboxLatLonRect, 1, 1, 1); // LOOK do you need
        // to subset it?
        GeoReferencedArray array = coverage.readData(params);

        try (GeotiffWriter writer = new GeotiffWriter(tifFile.getPath())) {
          if (format == Request.Format.GeoTIFF_Palette) {
            log.info("Processing Palette");
            // Set the color table
            Attribute flag_values_attr = coverage.findAttributeIgnoreCase("flag_values");
            Attribute flag_colors_attr = coverage.findAttributeIgnoreCase("flag_colors");
            if (flag_values_attr == null || flag_colors_attr == null) {
              // FIXME: Maybe use info from supplied styling info?
              log.error("Requested a paletted geotiff, but could not find a color table");
              throw new WcsException(WcsException.Code.UNKNOWN, "",
                  "No color table found for coverage [" + this.coverage.getName() + "] for format GeoTIFF_Palette.");
            }
            if (!flag_colors_attr.isString()) {
              log.error("Invalid flag_colors attribute");
              throw new WcsException(WcsException.Code.UNKNOWN, "",
                  "Invalid flag_colors attribute for coverage [" + this.coverage.getName() + "] for format GeoTIFF_Palette.");
            }

            int[] flag_values = new int[flag_values_attr.getLength()];
            for (int i = 0; i < flag_values.length; i++) {
              flag_values[i] = flag_values_attr.getNumericValue(i).intValue();
              log.info("flag_values[i]=" + flag_values[i] + " i=" + i);
            }
            String[] flag_colors = flag_colors_attr.getStringValue().split("\\s+");
            log.info("flag_values count:" + flag_values.length + " flag_colors count:" + flag_colors.length);
            writer.setColorTable(GeotiffWriter.createColorMap(flag_values, flag_colors));
          }
          writer.writeGrid(array, format == Request.Format.GeoTIFF,
                           format == Request.Format.GeoTIFF_Palette ? DataType.UBYTE : null);

        } catch (Throwable e) {
          log.error("writeCoverageDataToFile(): Failed to write file for requested coverage <" + this.coverage.getName()
              + ">: ", e);
          throw new WcsException(WcsException.Code.UNKNOWN, "",
              "Problem creating coverage [" + this.coverage.getName() + "].");
        }

        return tifFile;
      } else if (format == Request.Format.NetCDF3) {
        File dir = new File(getDiskCache().getRootDirectory());
        File outFile = File.createTempFile("WCS", ".nc", dir);
        if (log.isDebugEnabled())
          log.debug("writeCoverageDataToFile(): ncFile=" + outFile.getPath());

        // write the file
        // default chunking - let user control at some point
        NetcdfFormatWriter.Builder writerb = NetcdfFormatWriter.builder().setLocation(outFile.getAbsolutePath());
        CFGridCoverageWriter.Result result = CFGridCoverageWriter.write(this.wcsDataset.getDataset(),
            ImmutableList.of(this.coverage.getName()), params, true, writerb, 0);

        if (!result.wasWritten()) {
          throw new WcsException(result.getErrorMessage());
        }

        return outFile;

      } else {
        log.error("writeCoverageDataToFile(): Unsupported response encoding format [" + format + "].");
        throw new WcsException(WcsException.Code.InvalidFormat, "Format",
            "Unsupported response encoding format [" + format + "].");
      }

    } catch (InvalidRangeException e) {
      log.error(
          "writeCoverageDataToFile(): Failed to subset coverage <" + this.coverage.getName() + ">: " + e.getMessage());
      throw new WcsException(WcsException.Code.CoverageNotDefined, "",
          "Failed to subset coverage [" + this.coverage.getName() + "].");
    } catch (IOException e) {
      log.error("writeCoverageDataToFile(): Failed to create or write temporary file for requested coverage <"
          + this.coverage.getName() + ">: " + e.getMessage());
      throw new WcsException(WcsException.Code.UNKNOWN, "",
          "Problem creating coverage [" + this.coverage.getName() + "].");
    }
  }

  public static class VerticalRange {
    private double min, max;
    private int stride;
    private boolean singlePoint = false;

    public VerticalRange(double point, int stride) {
      this(point, point, stride);
      this.singlePoint = true;

    }

    public VerticalRange(double minimum, double maximum, int stride) {
      if (minimum > maximum) {
        log.error("VerticalRange(): Minimum <" + minimum + "> is greater than maximum <" + maximum + ">.");
        throw new IllegalArgumentException(
            "VerticalRange minimum <" + minimum + "> greater than maximum <" + maximum + ">.");
      }
      if (stride < 1) {
        log.error("VerticalRange(): stride <" + stride + "> less than one (1 means all points).");
        throw new IllegalArgumentException("VerticalRange stride <" + stride + "> less than one (1 means all points).");
      }
      this.min = minimum;
      this.max = maximum;
      this.stride = stride;
    }

    public double getMinimum() {
      return min;
    }

    public double getMaximum() {
      return max;
    }

    public int getStride() {
      return stride;
    }

    public boolean isSinglePoint() {
      return singlePoint;
    }

    public String toString() {
      return "[min=" + min + ",max=" + max + ",stride=" + stride + "]";
    }
  }
}
