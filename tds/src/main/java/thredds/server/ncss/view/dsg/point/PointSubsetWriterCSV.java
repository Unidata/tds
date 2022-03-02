/*
 * (c) 1998-2016 University Corporation for Atmospheric Research/Unidata
 */

package thredds.server.ncss.view.dsg.point;

import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import thredds.server.ncss.exception.NcssException;
import thredds.server.ncss.view.dsg.HttpHeaderWriter;
import ucar.ma2.Array;
import ucar.ma2.StructureData;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft2.coverage.SubsetParams;
import ucar.nc2.time.CalendarDateFormatter;
import ucar.unidata.geoloc.EarthLocation;
import ucar.unidata.util.Format;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by cwardgar on 2014/06/02.
 */
public class PointSubsetWriterCSV extends AbstractPointSubsetWriter {
  final protected PrintWriter writer;

  public PointSubsetWriterCSV(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, OutputStream out)
      throws NcssException {
    this(fdPoint, ncssParams, out, 0);
  }

  public PointSubsetWriterCSV(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, OutputStream out,
      int collectionIndex) throws NcssException {
    super(fdPoint, ncssParams, collectionIndex);
    this.writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
  }

  @Override
  public HttpHeaders getHttpHeaders(String datasetPath, boolean isStream) {
    return HttpHeaderWriter.getHttpHeadersForCSV(datasetPath, isStream);
  }

  @Override
  public void writeHeader(PointFeature pf) {
    writer.print("time,latitude[unit=\"degrees_north\"],longitude[unit=\"degrees_east\"]");
    for (VariableSimpleIF wantedVar : wantedVariables) {
      writer.print(",");
      writer.print(wantedVar.getShortName());
      if (wantedVar.getUnitsString() != null)
        writer.print("[unit=\"" + wantedVar.getUnitsString() + "\"]");
    }
    writer.println();
  }

  @Override
  public void writePoint(PointFeature pointFeat) throws IOException {
    EarthLocation loc = pointFeat.getLocation();

    writer.print(CalendarDateFormatter.toDateTimeStringISO(pointFeat.getObservationTimeAsCalendarDate()));
    writer.print(',');
    writer.print(Format.dfrac(loc.getLatitude(), 3));
    writer.print(',');
    writer.print(Format.dfrac(loc.getLongitude(), 3));

    StructureData structureData = pointFeat.getDataAll();
    for (VariableSimpleIF wantedVar : wantedVariables) {
      writer.print(',');
      Array dataArray = structureData.getArray(wantedVar.getShortName());
      writer.print(dataArray.toString().trim());
    }
    writer.println();
  }

  @Override
  public void writeFooter() {
    writer.flush();
  }
}
