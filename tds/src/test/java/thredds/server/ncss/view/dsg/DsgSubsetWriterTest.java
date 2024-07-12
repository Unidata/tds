/*
 * Copyright (c) 1998-2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.ncss.view.dsg;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.server.ncss.controller.NcssDiskCache;
import thredds.server.ncss.format.SupportedFormat;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft2.coverage.SubsetParams;
import ucar.nc2.jni.netcdf.Nc4Iosp;
import ucar.nc2.ogc.MarshallingUtil;
import ucar.nc2.time.Calendar;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.nc2.util.DiskCache2;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonRect;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

/**
 * Created by cwardgar on 2014/05/27.
 */
@RunWith(Parameterized.class)
public class DsgSubsetWriterTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  @Rule
  public final TemporaryFolder tempFolder = new TemporaryFolder();

  private static final boolean isClibraryPresent;
  static {
    isClibraryPresent = Nc4Iosp.isClibraryPresent();
    if (!isClibraryPresent) {
      logger.error("Could not load the NetCDF-4 C library. Tests that require it will be skipped.");
    }
  }

  private static NcssDiskCache ncssDiskCache;

  private static SubsetParams subsetParamsAll;
  private static SubsetParams subsetParamsPoint;
  private static SubsetParams subsetParamsStation1;
  private static SubsetParams subsetParamsStation2;
  private static SubsetParams subsetParamsProfile1;
  private static SubsetParams subsetParamsProfile2;

  // @BeforeClass <-- Can't use: JUnit won't invoke this method before getTestParameters().
  public static void setupClass() {
    // The WaterML marshaller usually initializes wml2:generationDate and om:resultTime to "now". This is a problem,
    // because those values will always differ from the fixed values we have in our expectedResultResource files.
    // So, to facilitate testing, we're going to fix the values that the marshaller emits.
    MarshallingUtil.fixedGenerationDate = CalendarDate.of(Calendar.gregorian, 1970, 1, 1, 0, 0, 0);
    MarshallingUtil.fixedResultTime = CalendarDate.of(Calendar.gregorian, 1970, 1, 1, 0, 0, 0);

    ncssDiskCache = new NcssDiskCache(DiskCache2.getDefault().getRootDirectory());

    subsetParamsAll = new SubsetParams();
    subsetParamsAll.setVariables(Arrays.asList("pr", "tas"));

    subsetParamsPoint = new SubsetParams();
    subsetParamsPoint.setVariables(Arrays.asList("pr"));
    subsetParamsPoint.setTime(CalendarDate.parseISOformat(null, "1970-01-01 02:00:00Z"));
    // Full extension is (40.0, -100.0) to (68.0, -58.0).
    LatLonRect bbox = new LatLonRect(LatLonPoint.create(40.0, -100.0), LatLonPoint.create(53.0, -58.0));
    subsetParamsPoint.setLatLonBoundingBox(bbox);

    subsetParamsStation1 = new SubsetParams();
    subsetParamsStation1.setVariables(Arrays.asList("tas"));
    CalendarDate start = CalendarDate.parseISOformat(null, "1970-01-05T00:00:00Z");
    CalendarDate end = CalendarDate.parseISOformat(null, "1970-02-05T00:00:00Z");
    subsetParamsStation1.setTimeRange(CalendarDateRange.of(start, end));
    subsetParamsStation1.setStations(Arrays.asList("AAA", "CCC"));

    subsetParamsStation2 = new SubsetParams();
    subsetParamsStation2.setVariables(Arrays.asList("pr", "tas"));
    // The nearest will be "1970-01-21 00:00:00Z"
    subsetParamsStation2.setTime(CalendarDate.parseISOformat(null, "1970-01-21 01:00:00Z"));

    // subset vars, time, time range, vertical level. stations
    subsetParamsProfile1 = new SubsetParams();
    subsetParamsProfile1.setVariables(Arrays.asList("tas"));
    subsetParamsProfile1.setTimeRange(CalendarDateRange.of(start, end));
    subsetParamsProfile1.setStations(Arrays.asList("AAA", "CCC"));

    subsetParamsProfile2 = new SubsetParams();
    subsetParamsProfile2.setVariables(Arrays.asList("pr", "tas"));
    // The nearest will be "1970-01-21 00:00:00Z"
    subsetParamsProfile2.setTime(CalendarDate.parseISOformat(null, "1970-01-21 01:00:00Z"));
  }

  @Parameterized.Parameters(name = "{0}/{1}/{3}")
  public static List<Object[]> getTestParameters() {
    // Normally, we'd annotate setupClass() with @BeforeClass and let JUnit call it. Unfortunately, that won't
    // work as expected when @Parameters gets involved.
    // See http://feraldeveloper.blogspot.co.uk/2013/12/beforeclass-and-parametrized-junit-tests.html
    // I like this workaround better than <clinit> because subclasses can potentially override setupClass().
    setupClass();

    return Arrays.asList(new Object[][] {
        // Point
        {FeatureType.POINT, SupportedFormat.CSV_FILE, subsetParamsAll, "outputAll.csv"},
        {FeatureType.POINT, SupportedFormat.CSV_FILE, subsetParamsPoint, "outputSubset.csv"},

        {FeatureType.POINT, SupportedFormat.XML_FILE, subsetParamsAll, "outputAll.xml"},
        {FeatureType.POINT, SupportedFormat.XML_FILE, subsetParamsPoint, "outputSubset.xml"},

        {FeatureType.POINT, SupportedFormat.NETCDF3, subsetParamsAll, "outputAll.ncml"},
        {FeatureType.POINT, SupportedFormat.NETCDF3, subsetParamsPoint, "outputSubset.ncml"},

        {FeatureType.POINT, SupportedFormat.NETCDF4, subsetParamsAll, "outputAll.ncml"},
        {FeatureType.POINT, SupportedFormat.NETCDF4, subsetParamsPoint, "outputSubset.ncml"},

        // Station
        {FeatureType.STATION, SupportedFormat.CSV_FILE, subsetParamsAll, "outputAll.csv"},
        {FeatureType.STATION, SupportedFormat.CSV_FILE, subsetParamsStation1, "outputSubset1.csv"},
        {FeatureType.STATION, SupportedFormat.CSV_FILE, subsetParamsStation2, "outputSubset2.csv"},

        {FeatureType.STATION, SupportedFormat.XML_FILE, subsetParamsAll, "outputAll.xml"},
        {FeatureType.STATION, SupportedFormat.XML_FILE, subsetParamsStation1, "outputSubset1.xml"},
        {FeatureType.STATION, SupportedFormat.XML_FILE, subsetParamsStation2, "outputSubset2.xml"},

        {FeatureType.STATION, SupportedFormat.WATERML2, subsetParamsAll, "outputAll.xml"},
        {FeatureType.STATION, SupportedFormat.WATERML2, subsetParamsStation1, "outputSubset1.xml"},
        {FeatureType.STATION, SupportedFormat.WATERML2, subsetParamsStation2, "outputSubset2.xml"},

        {FeatureType.STATION, SupportedFormat.NETCDF3, subsetParamsAll, "outputAll.ncml"},
        {FeatureType.STATION, SupportedFormat.NETCDF3, subsetParamsStation1, "outputSubset1.ncml"},
        {FeatureType.STATION, SupportedFormat.NETCDF3, subsetParamsStation2, "outputSubset2.ncml"},

        {FeatureType.STATION, SupportedFormat.NETCDF4, subsetParamsAll, "outputAll.ncml"},
        {FeatureType.STATION, SupportedFormat.NETCDF4, subsetParamsStation1, "outputSubset1.ncml"},
        {FeatureType.STATION, SupportedFormat.NETCDF4, subsetParamsStation2, "outputSubset2.ncml"},

        // profile
        {FeatureType.STATION_PROFILE, SupportedFormat.CSV_FILE, subsetParamsAll, "outputAll.csv"},
        {FeatureType.STATION_PROFILE, SupportedFormat.CSV_FILE, subsetParamsProfile1, "outputSubset1.csv"},
        {FeatureType.STATION_PROFILE, SupportedFormat.CSV_FILE, subsetParamsProfile2, "outputSubset2.csv"},

        {FeatureType.STATION_PROFILE, SupportedFormat.XML_FILE, subsetParamsAll, "outputAll.xml"},
        {FeatureType.STATION_PROFILE, SupportedFormat.XML_FILE, subsetParamsProfile1, "outputSubset1.xml"},
        {FeatureType.STATION_PROFILE, SupportedFormat.XML_FILE, subsetParamsProfile2, "outputSubset2.xml"},

        {FeatureType.STATION_PROFILE, SupportedFormat.NETCDF3, subsetParamsAll, "outputAll.ncml"},
        {FeatureType.STATION_PROFILE, SupportedFormat.NETCDF3, subsetParamsProfile1, "outputSubset1.ncml"},
        {FeatureType.STATION_PROFILE, SupportedFormat.NETCDF3, subsetParamsProfile2, "outputSubset2.ncml"},

        {FeatureType.STATION_PROFILE, SupportedFormat.NETCDF4, subsetParamsAll, "outputAll.ncml"},
        {FeatureType.STATION_PROFILE, SupportedFormat.NETCDF4, subsetParamsProfile1, "outputSubset1.ncml"},
        {FeatureType.STATION_PROFILE, SupportedFormat.NETCDF4, subsetParamsProfile2, "outputSubset2.ncml"}});
  }

  private final FeatureType wantedType;
  private final String datasetResource;
  private final SupportedFormat format;
  private final SubsetParams subsetParams;
  private final String expectedResultResource;

  public DsgSubsetWriterTest(FeatureType wantedType, SupportedFormat format, SubsetParams subsetParams,
      String expectedResultResource) {
    this.wantedType = wantedType;
    this.datasetResource = wantedType.name().toLowerCase() + "/input.ncml";
    this.format = format;
    this.subsetParams = subsetParams;
    this.expectedResultResource =
        wantedType.name().toLowerCase() + "/" + format.name().toLowerCase() + "/" + expectedResultResource;
  }

  @Test
  public void testWrite() throws Exception {
    if ((format == SupportedFormat.NETCDF4/* || format == SupportedFormat.NETCDF4EXT */) && !isClibraryPresent) {
      return; // Skip NetCDF 4 test.
    }

    File datasetFile = new File(getClass().getResource(datasetResource).toURI());
    File expectedResultFile = new File(getClass().getResource(expectedResultResource).toURI());
    File actualResultFile = tempFolder.newFile();

    try (FeatureDatasetPoint fdPoint = openPointDataset(wantedType, datasetFile);
        OutputStream outFileStream = new BufferedOutputStream(new FileOutputStream(actualResultFile))) {
      DsgSubsetWriter subsetWriterFile =
          DsgSubsetWriterFactory.newInstance(fdPoint, subsetParams, ncssDiskCache, outFileStream, format);
      subsetWriterFile.write();
    }

    // outFileStream must be closed before we compare actualResultFile.
    // That happens at the end of the try block above.
    if (format == SupportedFormat.NETCDF3 || format == SupportedFormat.NETCDF4) {
      Assert.assertTrue(
          String.format("Files differed:\n\texpected: %s\n\tactual: %s", expectedResultFile, actualResultFile),
          DsgSubsetTestUtils.compareNetCDF(expectedResultFile, actualResultFile));
    } else {
      Assert.assertTrue(
          String.format("Files differed:\n\texpected: %s\n\tactual: %s", expectedResultFile, actualResultFile),
          DsgSubsetTestUtils.compareText(expectedResultFile, actualResultFile));
    }
  }

  public static FeatureDatasetPoint openPointDataset(FeatureType wantedType, File datasetFile) throws IOException {
    Formatter errlog = new Formatter();
    FeatureDataset fDset = FeatureDatasetFactoryManager.open(wantedType, datasetFile.getAbsolutePath(), null, errlog);

    assert fDset != null : "No factory found: " + errlog;
    return (FeatureDatasetPoint) fDset;
  }
}
