/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.ncss.view.dsg.station;

import org.springframework.http.HttpHeaders;
import thredds.server.ncss.controller.NcssDiskCache;
import thredds.server.ncss.exception.NcssException;
import thredds.server.ncss.view.dsg.HttpHeaderWriter;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.constants.CDM;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.ft.point.writer.CFPointWriterConfig;
import ucar.nc2.ft.point.writer.WriterCFStationCollection;
import ucar.nc2.ft2.coverage.SubsetParams;
import ucar.nc2.time.CalendarDateUnit;
import ucar.nc2.util.IO;
import ucar.unidata.geoloc.Station;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cwardgar on 2014/05/29.
 */
public class StationSubsetWriterNetcdf extends AbstractStationSubsetWriter {
  private final OutputStream out;
  private final NetcdfFileWriter.Version version;

  private final File netcdfResult;
  private final WriterCFStationCollection cfWriter;
  private final NcssDiskCache ncssDiskCache;

  public StationSubsetWriterNetcdf(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, NcssDiskCache ncssDiskCache,
      OutputStream out, NetcdfFileWriter.Version version) throws NcssException, IOException {
    super(fdPoint, ncssParams);

    assert fdPoint.getPointFeatureCollectionList().size() == 1
        : "Multiple feature collections cannot be written as a CF dataset";

    this.ncssDiskCache = ncssDiskCache;
    this.out = out;
    this.version = version;

    this.netcdfResult = ncssDiskCache.getDiskCache().createUniqueFile("ncss-station", ".nc");
    List<Attribute> attribs = new ArrayList<>();
    attribs.add(new Attribute(CDM.TITLE, "Extracted data from TDS Feature Collection " + fdPoint.getLocation()));

    // get the timeUnit and altUnit from the FeatureCollection
    CalendarDateUnit timeUnit = this.stationFeatureCollection.getTimeUnit();
    if (timeUnit == null) {
      timeUnit = CalendarDateUnit.unixDateUnit;
    }
    String altUnit = this.stationFeatureCollection.getAltUnits();

    this.cfWriter = new WriterCFStationCollection(netcdfResult.getAbsolutePath(), attribs, wantedVariables, timeUnit,
        altUnit, new CFPointWriterConfig(version));
  }

  @Override
  public HttpHeaders getHttpHeaders(String datasetPath, boolean isStream) {
    return HttpHeaderWriter.getHttpHeadersForNetcdf(datasetPath, ncssDiskCache, version);
  }

  @Override
  protected void writeHeader(StationPointFeature stationPointFeat) throws Exception {
    cfWriter.writeHeader(wantedStations, stationPointFeat);
  }

  @Override
  protected void writeStationPointFeature(StationPointFeature stationPointFeat) throws Exception {
    Station station = stationPointFeat.getStation();
    cfWriter.writeRecord(station, stationPointFeat, stationPointFeat.getFeatureData());
  }

  @Override
  protected void writeFooter() throws Exception {
    cfWriter.finish();
    IO.copyFileB(netcdfResult, out, 60000); // Copy the file in to the OutputStream.
    out.flush();
  }
}
