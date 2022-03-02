package thredds.server.ncss.view.dsg.any_point;

import org.springframework.http.HttpHeaders;
import thredds.server.ncss.exception.NcssException;
import thredds.server.ncss.view.dsg.DsgSubsetWriter;
import thredds.server.ncss.view.dsg.HttpHeaderWriter;
import thredds.server.ncss.view.dsg.point.PointSubsetWriterXML;
import thredds.server.ncss.view.dsg.station.StationProfileSubsetWriterXML;
import thredds.server.ncss.view.dsg.station.StationSubsetWriterXML;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft2.coverage.SubsetParams;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;

public class MixedFeatureTypeSubsetWriterXML extends AbstractMixedFeatureTypeSubsetWriter {
  private final XMLStreamWriter staxWriter;

  public MixedFeatureTypeSubsetWriterXML(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, OutputStream out)
      throws NcssException, XMLStreamException, IOException {
    super(fdPoint, ncssParams, out);
    XMLOutputFactory factory = XMLOutputFactory.newInstance();
    staxWriter = factory.createXMLStreamWriter(out, "UTF-8");
    initWriters();
  }

  @Override
  DsgSubsetWriter newInstance(FeatureType featureType, int collectionIndex)
      throws XMLStreamException, NcssException, IOException {
    switch (featureType) {
      case POINT:
        return new PointSubsetWriterXML(fdPoint, ncssParams, out, collectionIndex, staxWriter);
      case STATION:
        return new StationSubsetWriterXML(fdPoint, ncssParams, out, collectionIndex, staxWriter);
      case STATION_PROFILE:
        return new StationProfileSubsetWriterXML(fdPoint, ncssParams, out, collectionIndex, staxWriter);
      default:
        throw new UnsupportedOperationException(String.format("%s feature type is not yet supported.", featureType));
    }
  }

  @Override
  public HttpHeaders getHttpHeaders(String datasetPath, boolean isStream) {
    return HttpHeaderWriter.getHttpHeadersForXML(datasetPath, isStream);
  }

  protected void writeHeader() throws XMLStreamException {
    staxWriter.writeStartDocument("UTF-8", "1.0");
    staxWriter.writeCharacters("\n");
    staxWriter.writeStartElement("FeatureCollection");
    staxWriter.writeCharacters("\n");
  }

  protected void writeFooter() throws XMLStreamException {
    staxWriter.writeEndElement();
    staxWriter.writeCharacters("\n");
    staxWriter.writeEndDocument();
    staxWriter.close();
  }

  @Override
  public void write() throws Exception {
    writeHeader();
    for (DsgSubsetWriter writer : writers) {
      writer.write();
    }
    writeFooter();
  }
}
