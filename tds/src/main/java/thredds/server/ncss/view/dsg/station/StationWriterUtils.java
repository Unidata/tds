package thredds.server.ncss.view.dsg.station;

import ucar.nc2.ft.StationFeatureCollection;
import ucar.nc2.ft.point.StationFeature;
import ucar.nc2.ft2.coverage.SubsetParams;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPoints;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Station;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StationWriterUtils {

  // LOOK could do better : "all", and maybe HashSet<Name>
  public static List<StationFeature> getStationsInSubset(StationFeatureCollection stationFeatCol,
      SubsetParams ncssParams) throws IOException {
    List<StationFeature> wantedStations;

    // verify SpatialSelection has some stations
    if (ncssParams.getStations() != null) {
      List<String> stnNames = ncssParams.getStations();

      if (stnNames.get(0).equals("all")) {
        wantedStations = stationFeatCol.getStationFeatures();
      } else {
        wantedStations = stationFeatCol.getStationFeatures(stnNames);
      }
    } else if (ncssParams.getLatLonBoundingBox() != null) {
      LatLonRect llrect = ncssParams.getLatLonBoundingBox();
      wantedStations = stationFeatCol.getStationFeatures(llrect);

    } else if (ncssParams.getLatLonPoint() != null) {
      Station closestStation = findClosestStation(stationFeatCol, ncssParams.getLatLonPoint());
      List<String> stnList = new ArrayList<>();
      stnList.add(closestStation.getName());
      wantedStations = stationFeatCol.getStationFeatures(stnList);

    } else { // Want all.
      wantedStations = stationFeatCol.getStationFeatures();
    }

    return wantedStations;
  }

  /**
   * Find the station closest to the specified point.
   * The metric is (lat-lat0)**2 + (cos(lat0)*(lon-lon0))**2
   * 
   * @param stationFeatCol
   * @param pt
   * @return
   * @throws IOException
   */
  public static Station findClosestStation(StationFeatureCollection stationFeatCol, LatLonPoint pt) throws IOException {
    double lat = pt.getLatitude();
    double lon = pt.getLongitude();
    double cos = Math.cos(Math.toRadians(lat));
    List<StationFeature> stations = stationFeatCol.getStationFeatures();
    Station min_station = stations.get(0);
    double min_dist = Double.MAX_VALUE;

    for (Station s : stations) {
      double lat1 = s.getLatitude();
      double lon1 = LatLonPoints.lonNormal(s.getLongitude(), lon);
      double dy = Math.toRadians(lat - lat1);
      double dx = cos * Math.toRadians(lon - lon1);
      double dist = dy * dy + dx * dx;
      if (dist < min_dist) {
        min_dist = dist;
        min_station = s;
      }
    }
    return min_station;
  }
}
