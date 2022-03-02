package thredds.server.ncss.view.dsg.any_point;

import org.springframework.http.HttpHeaders;
import thredds.server.ncss.exception.NcssException;
import thredds.server.ncss.view.dsg.DsgSubsetWriter;
import thredds.server.ncss.view.dsg.HttpHeaderWriter;
import thredds.server.ncss.view.dsg.point.PointSubsetWriterCSV;
import thredds.server.ncss.view.dsg.station.StationProfileSubsetWriterCSV;
import thredds.server.ncss.view.dsg.station.StationSubsetWriterCSV;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.*;
import ucar.nc2.ft2.coverage.SubsetParams;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;

public class MixedFeatureTypeSubsetWriterCSV extends AbstractMixedFeatureTypeSubsetWriter {

  public MixedFeatureTypeSubsetWriterCSV(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, OutputStream out)
      throws NcssException, IOException, XMLStreamException {
    super(fdPoint, ncssParams, out);
    initWriters();
  }

  @Override
  DsgSubsetWriter newInstance(FeatureType featureType, int collectionIndex) throws NcssException, IOException {
    switch (featureType) {
      case POINT:
        return new PointSubsetWriterCSV(fdPoint, ncssParams, out, collectionIndex);
      case STATION:
        return new StationSubsetWriterCSV(fdPoint, ncssParams, out, collectionIndex);
      case STATION_PROFILE:
        return new StationProfileSubsetWriterCSV(fdPoint, ncssParams, out, collectionIndex);
      default:
        throw new UnsupportedOperationException(String.format("%s feature type is not yet supported.", featureType));
    }
  }

  @Override
  public HttpHeaders getHttpHeaders(String datasetPath, boolean isStream) {
    return HttpHeaderWriter.getHttpHeadersForCSV(datasetPath, isStream);
  }


  @Override
  public void write() throws Exception {
    for (DsgSubsetWriter writer : writers) {
      writer.write();
    }
  }
}
