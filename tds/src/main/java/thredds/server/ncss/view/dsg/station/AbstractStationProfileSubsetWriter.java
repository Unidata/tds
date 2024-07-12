package thredds.server.ncss.view.dsg.station;

import thredds.server.ncss.exception.FeaturesNotFoundException;
import thredds.server.ncss.exception.NcssException;
import thredds.server.ncss.view.dsg.DsgSubsetWriter;
import ucar.ma2.StructureData;
import ucar.nc2.ft.*;
import ucar.nc2.ft.point.*;
import ucar.nc2.ft2.coverage.SubsetParams;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.nc2.util.IOIterator;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public abstract class AbstractStationProfileSubsetWriter extends DsgSubsetWriter {
  protected final StationProfileFeatureCollection stationFeatureCollection;
  protected final List<StationFeature> wantedStations;
  protected boolean headerDone = false;

  public AbstractStationProfileSubsetWriter(FeatureDatasetPoint fdPoint, SubsetParams ncssParams)
      throws NcssException, IOException {
    this(fdPoint, ncssParams, 0);
  }

  public AbstractStationProfileSubsetWriter(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, int collectionIndex)
      throws NcssException, IOException {
    super(fdPoint, ncssParams);

    List<DsgFeatureCollection> featColList = fdPoint.getPointFeatureCollectionList();
    assert featColList.size() > collectionIndex : "Could not find feature collection.";

    assert featColList.get(
        collectionIndex) instanceof StationProfileFeatureCollection : "This class only deals with StationProfileFeatureCollections.";

    this.stationFeatureCollection = (StationProfileFeatureCollection) featColList.get(collectionIndex);
    this.wantedStations = StationWriterUtils.getStationsInSubset(stationFeatureCollection, ncssParams);

    if (this.wantedStations.isEmpty()) {
      throw new FeaturesNotFoundException("No stations found in subset.");
    }
  }

  protected abstract void writeHeader(StationProfileFeature stn) throws Exception;

  protected void writeProfileFeature(StationProfileFeature stn, ProfileFeature profileFeat) throws Exception {
    for (PointFeature feat : profileFeat) {
      assert feat instanceof StationPointFeature : "Expected StationPointFeature, not " + feat.getClass().toString();
      writeStationPointFeature(stn, (StationPointFeature) feat);
    }
  }

  protected abstract void writeStationPointFeature(StationProfileFeature stn, StationPointFeature stationPointFeat)
      throws Exception;

  protected abstract void writeFooter() throws Exception;

  @Override
  public void write() throws Exception {
    // Perform spatial subset
    StationFeatureCollection subsettedStationFeatCol = stationFeatureCollection.subset(wantedStations);
    int count = 0;

    for (StationFeature profileFeat : subsettedStationFeatCol.getStationFeatures()) {
      assert profileFeat instanceof StationProfileFeature : "Expected StationProfileFeature, not "
          + profileFeat.getClass().toString();
      // Perform temporal subset. We do this even when a time instant is specified, in which case wantedRange
      // represents a sanity check (i.e. "give me the feature closest to the specified time, but it must at least be
      // within an hour").
      if (wantedRange != null) {
        profileFeat = ((StationProfileFeature) profileFeat).subset(wantedRange);
      }

      if (ncssParams.getTime() != null) {
        CalendarDate wantedTime = ncssParams.getTime();
        profileFeat = new ClosestTimeStationProfileFeatureSubset((StationProfileFeatureImpl) profileFeat, wantedTime);
      }
      count += writeStationProfileTimeSeriesFeature((StationProfileFeature) profileFeat);
    }

    if (count == 0) {
      throw new NcssException("No features are in the requested subset");
    }

    writeFooter();
  }

  protected int writeStationProfileTimeSeriesFeature(StationProfileFeature stationProfileFeat) throws Exception {
    if (!headerDone) {
      writeHeader(stationProfileFeat);
      headerDone = true;
    }
    // iterate profiles
    int count = 0;
    for (ProfileFeature profileFeat : stationProfileFeat) {
      writeProfileFeature(stationProfileFeat, profileFeat);
      count++;
    }
    return count;
  }

  protected static class ClosestTimeStationProfileFeatureSubset extends StationProfileFeatureImpl {
    private final StationProfileFeature stationProfileFeat;
    private CalendarDate closestTime;

    protected ClosestTimeStationProfileFeatureSubset(StationProfileFeatureImpl stationFeat, CalendarDate wantedTime)
        throws IOException {
      super(stationFeat, stationFeat.getTimeUnit(), stationFeat.getAltUnits(), -1);
      this.stationProfileFeat = stationFeat;
      CalendarDateRange cdr = stationFeat.getCalendarDateRange();
      if (cdr != null) {
        getInfo();
        info.setCalendarDateRange(cdr);
      }

      long smallestDiff = Long.MAX_VALUE;

      try (PointFeatureCollectionIterator iter = stationFeat.getPointFeatureCollectionIterator()) {
        while (iter.hasNext()) {
          ProfileFeature profileFeat = (ProfileFeature) iter.next();
          try (PointFeatureIterator pfIter = profileFeat.getPointFeatureIterator()) {
            // we're assuming all points in the profile will have the same time
            PointFeature point = pfIter.hasNext() ? pfIter.next() : null;

            CalendarDate obsTime = point.getObservationTimeAsCalendarDate();
            long diff = Math.abs(obsTime.getMillis() - wantedTime.getMillis());

            if (diff < smallestDiff) {
              closestTime = obsTime;
              smallestDiff = diff;
            }
          }
        }
      }
    }

    @Override
    public List<CalendarDate> getTimes() {
      return null;
    }

    @Override
    public ProfileFeature getProfileByDate(CalendarDate date) throws IOException {
      return null;
    }

    @Nonnull
    @Override
    public StructureData getFeatureData() throws IOException {
      return stationProfileFeat.getFeatureData();
    }

    // Filter out ProfileFeatures that don't have the wantedTime.
    protected static class TimeFilter implements PointFeatureCollectionIterator.Filter {
      private final CalendarDate wantedTime;

      protected TimeFilter(CalendarDate wantedTime) {
        this.wantedTime = wantedTime;
      }

      @Override
      public boolean filter(PointFeatureCollection pointFeatureCollection) {
        try (PointFeatureIterator iter = pointFeatureCollection.getPointFeatureIterator()) {
          PointFeature point = iter.hasNext() ? iter.next() : null;
          if (point == null) {
            return false;
          }
          return point.getObservationTimeAsCalendarDate().equals(wantedTime);
        } catch (IOException ex) {
          return false;
        }
      }
    }

    @Override
    public IOIterator<PointFeatureCollection> getCollectionIterator() throws IOException {
      return getPointFeatureCollectionIterator();
    }

    @Override
    public PointFeatureCollectionIterator getPointFeatureCollectionIterator() throws IOException {
      if (closestTime == null) {
        return stationProfileFeat.getPointFeatureCollectionIterator();
      } else {
        return new PointCollectionIteratorFiltered(stationProfileFeat.getPointFeatureCollectionIterator(),
            new TimeFilter(closestTime));
      }
    }
  }
}
