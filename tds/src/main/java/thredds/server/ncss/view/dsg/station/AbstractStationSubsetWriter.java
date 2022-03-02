/*
 * Copyright (c) 1998-2018 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.ncss.view.dsg.station;

import thredds.server.ncss.exception.FeaturesNotFoundException;
import thredds.server.ncss.exception.NcssException;
import thredds.server.ncss.view.dsg.DsgSubsetWriter;
import ucar.ma2.StructureData;
import ucar.nc2.ft.*;
import ucar.nc2.ft.point.PointIteratorFiltered;
import ucar.nc2.ft.point.StationFeature;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.ft.point.StationTimeSeriesFeatureImpl;
import ucar.nc2.ft2.coverage.SubsetParams;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Created by cwardgar on 2014/05/20.
 */
public abstract class AbstractStationSubsetWriter extends DsgSubsetWriter {
  protected final StationTimeSeriesFeatureCollection stationFeatureCollection;
  protected final List<StationFeature> wantedStations;
  protected boolean headerDone = false;

  public AbstractStationSubsetWriter(FeatureDatasetPoint fdPoint, SubsetParams ncssParams)
      throws NcssException, IOException {
    this(fdPoint, ncssParams, 0);
  }

  public AbstractStationSubsetWriter(FeatureDatasetPoint fdPoint, SubsetParams ncssParams, int collectionIndex)
      throws NcssException, IOException {
    super(fdPoint, ncssParams);

    List<DsgFeatureCollection> featColList = fdPoint.getPointFeatureCollectionList();
    assert featColList.size() > collectionIndex : "Could not find feature collection.";
    assert featColList.get(
        collectionIndex) instanceof StationTimeSeriesFeatureCollection : "This class only deals with StationTimeSeriesFeatureCollections.";

    this.stationFeatureCollection = (StationTimeSeriesFeatureCollection) featColList.get(collectionIndex);
    this.wantedStations = StationWriterUtils.getStationsInSubset(stationFeatureCollection, ncssParams);

    if (this.wantedStations.isEmpty()) {
      throw new FeaturesNotFoundException("No stations found in subset.");
    }
  }

  protected abstract void writeHeader(StationPointFeature stationPointFeat) throws Exception;

  protected abstract void writeStationPointFeature(StationPointFeature stationPointFeat) throws Exception;

  protected abstract void writeFooter() throws Exception;

  @Override
  public void write() throws Exception {

    // Perform spatial subset.
    StationTimeSeriesFeatureCollection subsettedStationFeatCol =
        stationFeatureCollection.subsetFeatures(wantedStations);
    int count = 0;

    for (StationTimeSeriesFeature stationFeat : subsettedStationFeatCol) {

      // Perform temporal subset. We do this even when a time instant is specified, in which case wantedRange
      // represents a sanity check (i.e. "give me the feature closest to the specified time, but it must at
      // least be within an hour").
      StationTimeSeriesFeature subsettedStationFeat = stationFeat.subset(wantedRange);

      if (ncssParams.getTime() != null) {
        CalendarDate wantedTime = ncssParams.getTime();
        subsettedStationFeat =
            new ClosestTimeStationFeatureSubset((StationTimeSeriesFeatureImpl) subsettedStationFeat, wantedTime);
      }

      count += writeStationTimeSeriesFeature(subsettedStationFeat);
    }

    if (count == 0) {
      throw new NcssException("No features are in the requested subset");
    }

    writeFooter();
  }

  protected int writeStationTimeSeriesFeature(StationTimeSeriesFeature stationFeat) throws Exception {
    int count = 0;
    for (PointFeature pointFeat : stationFeat) {
      assert pointFeat instanceof StationPointFeature : "Expected pointFeat to be a StationPointFeature, not a "
          + pointFeat.getClass().getSimpleName();

      if (!headerDone) {
        writeHeader((StationPointFeature) pointFeat);
        headerDone = true;
      }
      writeStationPointFeature((StationPointFeature) pointFeat);
      count++;
    }
    return count;
  }

  protected static class ClosestTimeStationFeatureSubset extends StationTimeSeriesFeatureImpl {
    private final StationTimeSeriesFeature stationFeat;
    private CalendarDate closestTime;

    protected ClosestTimeStationFeatureSubset(StationTimeSeriesFeatureImpl stationFeat, CalendarDate wantedTime)
        throws IOException {
      super(stationFeat, stationFeat.getTimeUnit(), stationFeat.getAltUnits(), -1);
      this.stationFeat = stationFeat;
      CalendarDateRange cdr = stationFeat.getCalendarDateRange();
      if (cdr != null) {
        getInfo();
        info.setCalendarDateRange(cdr);
      }

      long smallestDiff = Long.MAX_VALUE;

      stationFeat.resetIteration();
      try {
        while (stationFeat.hasNext()) {
          PointFeature pointFeat = stationFeat.next();
          CalendarDate obsTime = pointFeat.getObservationTimeAsCalendarDate();
          long diff = Math.abs(obsTime.getMillis() - wantedTime.getMillis());

          if (diff < smallestDiff) {
            closestTime = obsTime;
          }
        }
      } finally {
        stationFeat.finish();
      }
    }

    @Nonnull
    @Override
    public StructureData getFeatureData() throws IOException {
      return stationFeat.getFeatureData();
    }

    // Filter out PointFeatures that don't have the wantedTime.
    protected static class TimeFilter implements PointFeatureIterator.Filter {
      private final CalendarDate wantedTime;

      protected TimeFilter(CalendarDate wantedTime) {
        this.wantedTime = wantedTime;
      }

      @Override
      public boolean filter(PointFeature pointFeature) {
        return pointFeature.getObservationTimeAsCalendarDate().equals(wantedTime);
      }
    }

    @Override
    public PointFeatureIterator getPointFeatureIterator() throws IOException {
      if (closestTime == null) {
        return stationFeat.getPointFeatureIterator();
      } else {
        return new PointIteratorFiltered(stationFeat.getPointFeatureIterator(), new TimeFilter(closestTime));
      }
    }
  }
}
