package thredds.server.ncss.view.dsg.station;

import org.springframework.http.HttpHeaders;
import thredds.server.ncss.exception.NcssException;
import thredds.server.ncss.view.dsg.HttpHeaderWriter;
import ucar.ma2.Array;
import ucar.ma2.StructureData;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.CDM;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.StationProfileFeature;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.ft2.coverage.SubsetParams;
import ucar.nc2.time.CalendarDateFormatter;
import ucar.unidata.util.Format;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;

public class StationProfileSubsetWriterXML extends AbstractStationProfileSubsetWriter {
  private final XMLStreamWriter staxWriter;

  private final boolean isNested;

  public StationProfileSubsetWriterXML(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, OutputStream out)
      throws XMLStreamException, NcssException, IOException {
    this(fdPoint, ncssParams, out, 0, null);
  }

  public StationProfileSubsetWriterXML(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, OutputStream out,
      int collectionIndex, XMLStreamWriter staxWriter) throws XMLStreamException, NcssException, IOException {
    super(fdPoint, ncssParams, collectionIndex);
    if (staxWriter == null) {
      this.isNested = false;
      XMLOutputFactory factory = XMLOutputFactory.newInstance();
      this.staxWriter = factory.createXMLStreamWriter(out, "UTF-8");
    } else {
      this.isNested = true; // this is a subwriter for a list of feature collections
      this.staxWriter = staxWriter;
    }
  }

  @Override
  public HttpHeaders getHttpHeaders(String datasetPath, boolean isStream) {
    return HttpHeaderWriter.getHttpHeadersForXML(datasetPath, isStream);
  }

  @Override
  protected void writeHeader(StationProfileFeature stn) throws XMLStreamException {
    if (!isNested) {
      staxWriter.writeStartDocument("UTF-8", "1.0");
      staxWriter.writeCharacters("\n");
    }
    staxWriter.writeStartElement("stationProfileFeatureCollection");
  }

  @Override
  protected void writeStationPointFeature(StationProfileFeature stn, StationPointFeature stationPointFeat)
      throws XMLStreamException, IOException {
    staxWriter.writeCharacters("\n    ");
    staxWriter.writeStartElement("stationFeature");
    staxWriter.writeAttribute("date",
        CalendarDateFormatter.toDateTimeStringISO(stationPointFeat.getObservationTimeAsCalendarDate()));
    staxWriter.writeAttribute("altitude", Format.dfrac(stationPointFeat.getLocation().getAltitude(), 0));
    staxWriter.writeCharacters("\n        ");
    staxWriter.writeStartElement("station");
    staxWriter.writeAttribute("name", stn.getName());
    staxWriter.writeAttribute("latitude", Format.dfrac(stn.getLatitude(), 3));
    staxWriter.writeAttribute("longitude", Format.dfrac(stn.getLongitude(), 3));
    if (!Double.isNaN(stn.getAltitude())) {
      staxWriter.writeAttribute("altitude", Format.dfrac(stn.getAltitude(), 0));
    }
    if (stn.getDescription() != null) {
      staxWriter.writeCharacters(stn.getDescription());
    }
    staxWriter.writeEndElement();

    for (VariableSimpleIF wantedVar : wantedVariables) {
      staxWriter.writeCharacters("\n        ");
      staxWriter.writeStartElement("data");
      staxWriter.writeAttribute("name", wantedVar.getShortName());
      if (wantedVar.getUnitsString() != null) {
        staxWriter.writeAttribute(CDM.UNITS, wantedVar.getUnitsString());
      }

      Array dataArray = stationPointFeat.getDataAll().getArray(wantedVar.getShortName());
      String ss = dataArray.toString();
      Class elemType = dataArray.getElementType();
      if ((elemType == String.class) || (elemType == char.class) || (elemType == StructureData.class)) {
        ss = ucar.nc2.util.xml.Parse.cleanCharacterData(ss); // make sure no bad chars
      }
      staxWriter.writeCharacters(ss.trim());
      staxWriter.writeEndElement();
    }
    staxWriter.writeCharacters("\n    ");
    staxWriter.writeEndElement();
  }

  @Override
  protected void writeFooter() throws XMLStreamException {
    staxWriter.writeCharacters("\n");
    staxWriter.writeEndElement();
    staxWriter.writeCharacters("\n");
    if (!isNested) {
      staxWriter.writeEndDocument();
      staxWriter.close(); // This should flush the writer. The underlying output stream remains open.
    }
  }
}
