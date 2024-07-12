package thredds.server.ncss.view.dsg.station;

import org.springframework.http.HttpHeaders;
import thredds.server.ncss.controller.NcssDiskCache;
import thredds.server.ncss.exception.NcssException;
import thredds.server.ncss.view.dsg.HttpHeaderWriter;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.constants.CDM;
import ucar.nc2.ft.*;
import ucar.nc2.ft.point.StationFeature;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.ft.point.writer.CFPointWriterConfig;
import ucar.nc2.ft.point.writer.WriterCFStationProfileCollection;
import ucar.nc2.ft2.coverage.SubsetParams;
import ucar.nc2.time.CalendarDateUnit;
import ucar.nc2.util.IO;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class StationProfileSubsetWriterNetcdf extends AbstractStationProfileSubsetWriter {
  private final OutputStream out;
  private final NetcdfFileWriter.Version version;

  private final File netcdfResult;
  private final WriterCFStationProfileCollection cfWriter;
  private final NcssDiskCache ncssDiskCache;

  public StationProfileSubsetWriterNetcdf(FeatureDatasetPoint fdPoint, SubsetParams ncssParams,
      NcssDiskCache ncssDiskCache, OutputStream out, NetcdfFileWriter.Version version)
      throws NcssException, IOException {
    super(fdPoint, ncssParams);

    assert fdPoint.getPointFeatureCollectionList()
        .size() == 1 : "Multiple feature collections cannot be written as a CF dataset";

    this.ncssDiskCache = ncssDiskCache;
    this.out = out;
    this.version = version;

    this.netcdfResult = ncssDiskCache.getDiskCache().createUniqueFile("ncss-station", ".nc");
    List<Attribute> attribs = new ArrayList<>();
    attribs.add(new Attribute(CDM.TITLE, "Extracted data from TDS Feature Collection " + fdPoint.getLocation()));

    String altUnits = stationFeatureCollection.getAltUnits();
    CalendarDateUnit timeUnit = stationFeatureCollection.getTimeUnit();
    if (timeUnit == null) {
      timeUnit = CalendarDateUnit.unixDateUnit;
    }

    this.cfWriter = new WriterCFStationProfileCollection(netcdfResult.getAbsolutePath(), attribs, wantedVariables,
        timeUnit, altUnits, new CFPointWriterConfig(version));
    cfWriter.setStations(wantedStations);

    int name_strlen = 0;
    int countProfiles = 0;
    for (StationFeature spf : stationFeatureCollection.getStationFeatures()) {
      assert spf instanceof StationProfileFeature : "Expected StationProfileFeature, not " + spf.getClass().toString();
      final String name = spf.getName();
      if (wantedStations.stream().noneMatch(stn -> stn.getName().equals(name))) {
        continue;
      }
      if (wantedRange != null) {
        spf = ((StationProfileFeature) spf).subset(wantedRange);
      }
      name_strlen = Math.max(name_strlen, spf.getName().length());
      if (((StationProfileFeature) spf).size() >= 0) {
        countProfiles += ((StationProfileFeature) spf).size();
      } else {
        for (ProfileFeature pf : ((StationProfileFeature) spf)) {
          countProfiles++;
        }
      }
    }
    cfWriter.setFeatureAuxInfo(countProfiles, name_strlen);
  }

  @Override
  public HttpHeaders getHttpHeaders(String datasetPath, boolean isStream) {
    return HttpHeaderWriter.getHttpHeadersForNetcdf(datasetPath, ncssDiskCache, version);
  }

  @Override
  protected void writeHeader(StationProfileFeature stn) throws Exception {
    ArrayList<StationFeature> asList = new ArrayList<>();
    asList.add(stn);
    cfWriter.writeHeader(asList);
  }

  @Override
  protected void writeProfileFeature(StationProfileFeature stn, ProfileFeature profileFeat) throws Exception {
    cfWriter.writeProfile(stn, profileFeat);
  }

  @Override
  protected void writeStationPointFeature(StationProfileFeature stn, StationPointFeature stationPointFeat)
      throws Exception {
    cfWriter.writeObsData(stationPointFeat);
  }

  @Override
  protected void writeFooter() throws Exception {
    cfWriter.finish();
    IO.copyFileB(netcdfResult, out, 60000); // Copy the file in to the OutputStream.
    out.flush();
  }
}
