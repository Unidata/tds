package thredds.server.ncss.view.dsg.station;

import org.springframework.http.HttpHeaders;
import thredds.server.ncss.exception.NcssException;
import thredds.server.ncss.view.dsg.HttpHeaderWriter;
import ucar.ma2.Array;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.StationProfileFeature;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.ft2.coverage.SubsetParams;
import ucar.nc2.time.CalendarDateFormatter;
import ucar.unidata.util.Format;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class StationProfileSubsetWriterCSV extends AbstractStationProfileSubsetWriter {

  final protected PrintWriter writer;

  public StationProfileSubsetWriterCSV(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, OutputStream out)
      throws NcssException, IOException {
    this(fdPoint, ncssParams, out, 0);
  }

  public StationProfileSubsetWriterCSV(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, OutputStream out,
      int collectionIndex) throws NcssException, IOException {
    super(fdPoint, ncssParams, collectionIndex);
    this.writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
  }

  @Override
  public HttpHeaders getHttpHeaders(String datasetPath, boolean isStream) {
    return HttpHeaderWriter.getHttpHeadersForCSV(datasetPath, isStream);
  }

  @Override
  protected void writeHeader(StationProfileFeature stn) throws Exception {
    String altUnits = stn.getAltUnits();
    writer.print("time,alt[unit=\"" + altUnits
        + "\"],station,latitude[unit=\"degrees_north\"],longitude[unit=\"degrees_east\"]");
    for (VariableSimpleIF wantedVar : wantedVariables) {
      writer.print(",");
      writer.print(wantedVar.getShortName());
      if (wantedVar.getUnitsString() != null)
        writer.print("[unit=\"" + wantedVar.getUnitsString() + "\"]");
    }
    writer.println();
  }

  @Override
  protected void writeStationPointFeature(StationProfileFeature stn, StationPointFeature pointFeat) throws IOException {
    writer.print(CalendarDateFormatter.toDateTimeStringISO(pointFeat.getObservationTimeAsCalendarDate()));
    writer.print(',');
    writer.print(pointFeat.getLocation().getAltitude());
    writer.print(',');
    writer.print(stn.getName());
    writer.print(',');
    writer.print(Format.dfrac(stn.getLatitude(), 3));
    writer.print(',');
    writer.print(Format.dfrac(stn.getLongitude(), 3));

    for (VariableSimpleIF wantedVar : wantedVariables) {
      writer.print(',');
      Array dataArray = pointFeat.getDataAll().getArray(wantedVar.getShortName());
      writer.print(dataArray.toString().trim());
    }
    writer.println();
  }

  @Override
  protected void writeFooter() throws IOException {
    writer.flush();
  }
}
