package thredds.server.ncss.view.dsg.any_point;

import thredds.server.ncss.exception.NcssException;
import thredds.server.ncss.view.dsg.DsgSubsetWriter;
import ucar.ma2.StructureData;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.*;
import ucar.nc2.ft2.coverage.SubsetParams;

import javax.validation.constraints.Null;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMixedFeatureTypeSubsetWriter extends DsgSubsetWriter {

  protected final List<DsgSubsetWriter> writers;

  protected final OutputStream out;

  public AbstractMixedFeatureTypeSubsetWriter(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, OutputStream out)
      throws NcssException {
    super(fdPoint, ncssParams);
    writers = new ArrayList<>();
    this.out = out;
  }

  protected void initWriters() throws NcssException, IOException, XMLStreamException {
    List<DsgFeatureCollection> featColList = fdPoint.getPointFeatureCollectionList();
    assert featColList.size() > 1 : "This class only deals with multiple Feature Collections.";
    List<String> allVars = ncssParams.getVariables();
    int i = 0;
    for (DsgFeatureCollection fc : featColList) {
      FeatureType featureType = fc.getCollectionFeatureType();
      if (!featureType.isPointFeatureType()) {
        throw new NcssException(String.format("Expected a point feature type, not %s", featureType));
      }
      final PointFeature pointFeature;
      try {
        switch (featureType) {
          case POINT:
            pointFeature = ((PointFeatureCollection) fc).hasNext() ? ((PointFeatureCollection) fc).next() : null;
            break;
          case STATION:
            PointFeatureCollection pfc = ((StationTimeSeriesFeatureCollection) fc).getCollectionIterator().next();
            pointFeature = pfc.hasNext() ? pfc.next() : null;
            break;
          case STATION_PROFILE:
            ProfileFeature profile = ((ProfileFeature) ((StationProfileFeature) ((StationProfileFeatureCollection) fc)
                .getStationFeatures().get(0)).getCollectionIterator().next());
            pointFeature = profile.hasNext() ? profile.next() : null;
            break;
          default:
            throw new UnsupportedOperationException(
                String.format("%s feature type is not yet supported.", featureType));
        }
      } catch (Exception e) {
        // catch IOException and NullPointerException
        throw new NcssException("Could not read point from feature collection " + fc.getName() + ": " + e.getMessage());
      }
      // subset wanted vars
      StructureData data = pointFeature.getDataAll();
      List<String> collectionVars =
          allVars.stream().filter(var -> data.findMember(var) != null).collect(Collectors.toList());
      ncssParams.setVariables(collectionVars);
      // get writer
      writers.add(newInstance(featureType, i));
      i++;
    }
  }

  abstract DsgSubsetWriter newInstance(FeatureType featureType, int collectionIndex)
      throws XMLStreamException, NcssException, IOException;

}
