/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.ncss.view.dsg.point;

import org.springframework.http.HttpHeaders;
import thredds.server.ncss.controller.NcssDiskCache;
import thredds.server.ncss.exception.NcssException;
import thredds.server.ncss.view.dsg.HttpHeaderWriter;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter.Version;
import ucar.nc2.constants.CDM;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.point.writer.CFPointWriterConfig;
import ucar.nc2.ft.point.writer.WriterCFPointCollection;
import ucar.nc2.ft2.coverage.SubsetParams;
import ucar.nc2.time.CalendarDateUnit;
import ucar.nc2.util.IO;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cwardgar on 2014/06/04.
 */
public class PointSubsetWriterNetcdf extends AbstractPointSubsetWriter {
  private final NcssDiskCache ncssDiskCache;
  private final OutputStream out;
  private final Version version;

  private final File netcdfResult;
  private final WriterCFPointCollection cfWriter;

  public PointSubsetWriterNetcdf(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, NcssDiskCache ncssDiskCache,
      OutputStream out, Version version) throws NcssException, IOException {
    super(fdPoint, ncssParams);

    assert fdPoint.getPointFeatureCollectionList()
        .size() == 1 : "Multiple feature collections cannot be written as a CF dataset";

    this.ncssDiskCache = ncssDiskCache;
    this.out = out;
    this.version = version;

    this.netcdfResult = ncssDiskCache.getDiskCache().createUniqueFile("ncss-point", ".nc");
    List<Attribute> attribs = new ArrayList<>();
    attribs.add(new Attribute(CDM.TITLE, "Extracted data from TDS Feature Collection " + fdPoint.getLocation()));

    // get the timeUnit and altUnit from the FeatureCollection
    CalendarDateUnit timeUnit = this.pointFeatureCollection.getTimeUnit();
    if (timeUnit == null) {
      timeUnit = CalendarDateUnit.unixDateUnit;
    }
    String altUnit = this.pointFeatureCollection.getAltUnits();

    this.cfWriter = new WriterCFPointCollection(netcdfResult.getAbsolutePath(), attribs, wantedVariables, timeUnit,
        altUnit, new CFPointWriterConfig(version));
  }

  @Override
  public HttpHeaders getHttpHeaders(String datasetPath, boolean isStream) {
    return HttpHeaderWriter.getHttpHeadersForNetcdf(datasetPath, ncssDiskCache, version);
  }

  @Override
  public void writeHeader(PointFeature pf) throws IOException {
    cfWriter.writeHeader(pf);
  }

  @Override
  public void writePoint(PointFeature pointFeat) throws Exception {
    cfWriter.writeRecord(pointFeat, pointFeat.getDataAll());
  }

  @Override
  public void writeFooter() throws IOException {
    cfWriter.finish();
    IO.copyFileB(netcdfResult, out, 60000); // Copy the file in to the OutputStream.
    out.flush();
  }
}
