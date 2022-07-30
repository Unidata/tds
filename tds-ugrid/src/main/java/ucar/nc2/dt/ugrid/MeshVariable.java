/*
 * Copyright (c) 2011-2014 Applied Science Associates
 */

package ucar.nc2.dt.ugrid;

import ucar.nc2.dt.ugrid.topology.Topology;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.UGridDataset.Meshset;
import ucar.nc2.dt.UGridDatatype;
import ucar.nc2.dt.ugrid.geom.LatLonPoint2D;
import ucar.nc2.dt.ugrid.geom.LatLonPolygon2D;
import ucar.nc2.dt.ugrid.geom.LatLonRectangle2D;
import ucar.nc2.dt.ugrid.utils.NcdsFactory;
import ucar.nc2.dt.ugrid.utils.NcdsFactory.NcdsTemplate;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.util.Format;

/**
 *
 * @author Kyle
 */
public class MeshVariable implements UGridDatatype {

  private VariableDS vs;
  private UGridDataset dataset;
  private Meshset meshset;
  private List<Dimension> mydims;
  private String cellLocation;

  public MeshVariable(UGridDataset dataset, VariableDS vs, Meshset meshset) {
    this.vs = vs;
    this.meshset = meshset;
    this.dataset = dataset;
    this.mydims = vs.getDimensions();
    this.cellLocation = vs.findAttributeIgnoreCase("location").getStringValue();
  }

  public String getName() {
    return vs.getFullName();
  }

  public String getNameEscaped() {
    return vs.getFullNameEscaped();
  }

  public Topology getConnectivityVariable() {
    return (Topology) meshset.getMesh().getTopology();
  }

  public String getDescription() {
    return vs.getDescription();
  }

  public String getUnitsString() {
    return vs.getUnitsString();
  }

  public DataType getDataType() {
    return vs.getDataType();
  }

  public int getRank() {
    return vs.getRank();
  }

  public String getCellLocation() {
    return cellLocation;
  }

  public int[] getShape() {
    int[] shape = new int[mydims.size()];
    for (int i = 0; i < mydims.size(); i++) {
      Dimension d = mydims.get(i);
      shape[i] = d.getLength();
    }
    return shape;
  }

  public List<Attribute> getAttributes() {
    return vs.getAttributes();
  }

  public Attribute findAttributeIgnoreCase(String name) {
    return vs.findAttributeIgnoreCase(name);
  }

  public String findAttValueIgnoreCase(String attName, String defaultValue) {
    return dataset.getNetcdfDataset().findAttValueIgnoreCase((Variable) vs, attName, defaultValue);
  }

  public List<Dimension> getDimensions() {
    return mydims;
  }

  public Dimension getDimension(int i) {
    return mydims.get(i);
  }

  public Dimension getTimeDimension() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Dimension getZDimension() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Dimension getYDimension() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Dimension getXDimension() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Dimension getEnsembleDimension() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Dimension getRunTimeDimension() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int getTimeDimensionIndex() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int getZDimensionIndex() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int getYDimensionIndex() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int getXDimensionIndex() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int getEnsembleDimensionIndex() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int getRunTimeDimensionIndex() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Meshset getMeshset() {
    return meshset;
  }

  public ProjectionImpl getProjection() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean hasMissingData() {
    return vs.hasMissing();
  }

  public boolean isMissingData(double val) {
    return vs.isMissing(val);
  }

  public MinMax getMinMaxSkipMissingData(Array a) {
    if (!hasMissingData()) {
      return MAMath.getMinMax(a);
    }

    IndexIterator iter = a.getIndexIterator();
    double max = -Double.MAX_VALUE;
    double min = Double.MAX_VALUE;
    while (iter.hasNext()) {
      double val = iter.getDoubleNext();
      if (isMissingData(val)) {
        continue;
      }
      if (val > max) {
        max = val;
      }
      if (val < min) {
        min = val;
      }
    }
    return new MAMath.MinMax(min, max);
  }

  public float[] setMissingToNaN(float[] values) {
    if (!vs.hasMissing()) {
      return values;
    }
    final int length = values.length;
    for (int i = 0; i < length; i++) {
      double value = values[i];
      if (vs.isMissing(value)) {
        values[i] = Float.NaN;
      }
    }
    return values;
  }

  public Array readDataSlice(int rt_index, int e_index, int t_index, int z_index, int y_index, int x_index)
      throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Array readDataSlice(int t_index, int z_index, int y_index, int x_index) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Array readVolumeData(int t_index) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public UGridDatatype makeSubset(Range rt_range, Range e_range, Range t_range, Range z_range, Range y_range,
      Range x_range) throws InvalidRangeException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public UGridDatatype makeSubset(Range t_range, Range z_range, LatLonRect bbox, int z_stride, int y_stride,
      int x_stride) throws InvalidRangeException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void subsetToDataset(UGridDataset ugd, NetcdfDataset ncd, List<Cell> containedCells) {

    // Get unique node indexes, so we know how large the Node array is going to be.
    List<Entity> list = new ArrayList<Entity>();
    if (cellLocation.equals("node")) {
      for (Cell c : containedCells) {
        for (Node n : c.getNodes()) {
          list.add(n);
        }
      }
    } else if (cellLocation.equals("face")) {
      for (Cell c : containedCells) {
        for (Face n : c.getFaces()) {
          list.add(n);
        }
      }
    } else if (cellLocation.equals("edge")) {
      for (Cell c : containedCells) {
        for (Edge n : c.getEdges()) {
          list.add(n);
        }
      }
    }
    Set set = new HashSet(list);
    ArrayList<Entity> unique_entities = new ArrayList<Entity>(set);

    /*
     * Add all coordinate axis that this MeshVariable uses (ie. lat, lon, depth, time)
     * Only add them if they don't exists.
     * Need to subset the lat and lon variables.
     * The Topology is already subsat, but not reindexed.
     */

    try {
      int time_index = -1;
      int time_length = -1;
      int z_index = -1;
      int z_length = -1;
      int data_index = -1;

      for (CoordinateSystem cs : vs.getCoordinateSystems()) {
        /*
         * Time
         */
        if (cs.hasTimeAxis()) {
          VariableDS t;
          if (ncd.findCoordinateAxis(cs.getTaxis().getFullNameEscaped()) == null) {
            ncd.addCoordinateSystem(cs);
            ncd.addCoordinateAxis(cs.getTaxis());
            t = new VariableDS(null, cs.getTaxis().getOriginalVariable(), true);
            ncd.addVariable(null, t);
            t.setCachedData(cs.getTaxis().getOriginalVariable().read());
            ncd.finish();
          }
          time_index = vs.findDimensionIndex(cs.getTaxis().getDimension(0).getFullName());
          time_length = vs.getDimension(time_index).getLength();
        }
        /*
         * Z
         */
        if (cs.hasVerticalAxis()) {
          VariableDS z;
          if (cs.getZaxis() != null) {
            if (ncd.findCoordinateAxis(cs.getZaxis().getFullNameEscaped()) == null) {
              ncd.addCoordinateSystem(cs);
              ncd.addCoordinateAxis(cs.getZaxis());
              z = new VariableDS(null, cs.getZaxis().getOriginalVariable(), true);
              z.setCachedData(cs.getZaxis().getOriginalVariable().read());
              ncd.addVariable(null, z);
            }
            z_index = vs.findDimensionIndex(cs.getZaxis().getDimension(0).getFullName());
            z_length = vs.getDimension(z_index).getLength();
          }
          if (cs.getHeightAxis() != null) {
            if (ncd.findCoordinateAxis(cs.getHeightAxis().getFullNameEscaped()) == null) {
              ncd.addCoordinateSystem(cs);
              ncd.addCoordinateAxis(cs.getHeightAxis());
              z = new VariableDS(null, cs.getHeightAxis().getOriginalVariable(), true);
              z.setCachedData(cs.getHeightAxis().getOriginalVariable().read());
              ncd.addVariable(null, z);
            }
            z_index = vs.findDimensionIndex(cs.getHeightAxis().getDimension(0).getFullName());
            z_length = vs.getDimension(z_index).getLength();
          }
          if (cs.getPressureAxis() != null) {
            if (ncd.findCoordinateAxis(cs.getPressureAxis().getFullNameEscaped()) == null) {
              ncd.addCoordinateSystem(cs);
              ncd.addCoordinateAxis(cs.getPressureAxis());
              z = new VariableDS(null, cs.getPressureAxis().getOriginalVariable(), true);
              z.setCachedData(cs.getPressureAxis().getOriginalVariable().read());
              ncd.addVariable(null, z);
            }
            z_index = vs.findDimensionIndex(cs.getPressureAxis().getDimension(0).getFullName());
            z_length = vs.getDimension(z_index).getLength();
          }
          ncd.finish();
        }
        /*
         * Lat / Lon
         */
        if (cs.isLatLon()) {

          Dimension node_dim = ncd.findDimension(cs.getLatAxis().getDimension(0).getFullName());
          if (node_dim == null) {
            node_dim = ncd.addDimension(null,
                new Dimension(cs.getLatAxis().getDimension(0).getFullName(), unique_entities.size()));
          }
          data_index = vs.findDimensionIndex(node_dim.getFullName());
          ncd.finish();

          int count;
          // Lat
          Variable newLat = ncd.findVariable(cs.getLatAxis().getFullNameEscaped());
          if (newLat == null) {
            newLat = new VariableDS(ugd.getNetcdfDataset(), null, null, cs.getLatAxis().getShortName(),
                cs.getLatAxis().getOriginalDataType(), cs.getLatAxis().getDimensionsString(),
                cs.getLatAxis().getUnitsString(), cs.getLatAxis().getDescription());
            for (Attribute a : (List<Attribute>) cs.getLatAxis().getAttributes()) {
              newLat.addAttribute(a);
            }
            newLat.setDimension(0, node_dim);
            ncd.addVariable(null, newLat);
            ncd.addCoordinateAxis(new VariableDS(null, newLat, true));
            Array lats = Array.factory(cs.getLatAxis().getOriginalDataType(), newLat.getShape());
            count = 0;
            for (Entity k : unique_entities) {
              lats.setObject(count, k.getGeoPoint().getLatitude());
              count++;
            }
            newLat.setCachedData(lats);
          }
          ncd.finish();

          // Lon
          Variable newLon = ncd.findVariable(cs.getLonAxis().getFullNameEscaped());
          if (newLon == null) {
            newLon = new VariableDS(ugd.getNetcdfDataset(), null, null, cs.getLonAxis().getShortName(),
                cs.getLonAxis().getOriginalDataType(), cs.getLonAxis().getDimensionsString(),
                cs.getLonAxis().getUnitsString(), cs.getLonAxis().getDescription());
            for (Attribute a : (List<Attribute>) cs.getLonAxis().getAttributes()) {
              newLon.addAttribute(a);
            }
            newLon.setDimension(0, node_dim);
            ncd.addVariable(null, newLon);
            ncd.addCoordinateAxis(new VariableDS(null, newLon, true));
            Array lons = Array.factory(cs.getLonAxis().getOriginalDataType(), newLon.getShape());
            count = 0;
            for (Entity k : unique_entities) {
              lons.setObject(count, k.getGeoPoint().getLongitude());
              count++;
            }
            newLon.setCachedData(lons);
            ncd.finish();
          }
          ncd.finish();
        }
      }

      /*
       * Now add this actual MeshVariable, now that the file has
       * been set up with the correct Dimensions.
       * TODO: Subset this variable.
       * 
       * Options:
       * 1.) Iterate over the containedCells.get(i).getNodes().get(j).getDataIndex()
       * and slice out each data index from this variable into a new variable.
       * Update the topology with the new data index.
       * 2.) Get the min and max data indexes from the topology and only
       * reindex that section of the data variables. Don't have to take
       * individual slices from the data variable doing it this way, but
       * still need to reindex the topology.
       * 3.) Set the unused data indexes to its FillValue
       */

      Variable newVar = ncd.findVariable(vs.getFullNameEscaped());
      if (newVar == null) {
        newVar = new VariableDS(ugd.getNetcdfDataset(), null, null, vs.getShortName(), vs.getOriginalDataType(),
            vs.getDimensionsString(), vs.getUnitsString(), vs.getDescription());
        for (Attribute a : (List<Attribute>) vs.getAttributes()) {
          newVar.addAttribute(a);
        }
        ncd.addVariable(null, newVar);
        ncd.finish();
      }

      ArrayList<Range> r = new ArrayList<Range>();
      // Time
      if (time_index != -1) {
        r.add(time_index, new Range(time_length));
      }
      // Vertical
      if (z_index != -1) {
        r.add(z_index, new Range(z_length));
      }
      // Data
      // System.out.println(newVar);
      if (data_index != -1) {
        r.add(data_index, new Range(0, 10));
      }
      newVar.setCachedData(vs.read(r));
      ncd.finish();

    } catch (IOException ioe) {

    } catch (InvalidRangeException ivre) {

    }
  }

  public UGridDataset subsetToSelf(LatLonRect bounds) {
    LatLonRectangle2D r = new LatLonRectangle2D(
        new LatLonPoint2D.Double(bounds.getUpperLeftPoint().getLatitude(), bounds.getUpperLeftPoint().getLongitude()),
        new LatLonPoint2D.Double(bounds.getLowerRightPoint().getLatitude(),
            bounds.getLowerRightPoint().getLongitude()));
    LatLonPolygon2D p = new LatLonPolygon2D.Double(r);
    List<Cell> containedCells = meshset.getMesh().getCellsInPolygon(p);

    // Create a new subsat UGridDataset and return
    try {
      NetcdfDataset ncd = NcdsFactory.getNcdsFromTemplate(NcdsTemplate.UGRID);

      for (Attribute a : dataset.getGlobalAttributes()) {
        ncd.addAttribute(null, a);
      }

      Dimension ds = new Dimension("nodes", containedCells.size());

      for (CoordinateSystem cs : dataset.getNetcdfDataset().getCoordinateSystems()) {
        for (Dimension d : cs.getDomain()) {
          if (ncd.findDimension(d.getFullName()) == null) {
            ncd.addDimension(null, d);
            Variable vd = dataset.getNetcdfFile().findVariable(d.getFullName());
            if (vd != null) {
              ncd.addVariable(null, new VariableDS(null, vd, true));
            }
          }
          ncd.finish();
        }
        ncd.addCoordinateSystem(cs);
        ncd.finish();
      }

      for (CoordinateAxis ax : dataset.getNetcdfDataset().getCoordinateAxes()) {
        ncd.addCoordinateAxis(ax);
        ncd.finish();
      }

      // Variable describing the Mesh (Mesh1, Mesh2, etc)
      ncd.addVariable(null, meshset.getDescriptionVariable());

      // Connectivity Variable for this MeshVariable
      ncd.addVariable(null, getConnectivityVariable().subsetToVariable(containedCells));

      // ncd.addVariable(null, subsetToVariable(containedCells));

      // Now add the data
      // vs.setCachedData(null);

      ncd.finish();

      return new UGridDataset(ncd);
    } catch (URISyntaxException e) {
      System.out.println(e);
    } catch (FileNotFoundException e) {
      System.out.println(e);
    } catch (IOException e) {
      System.out.println(e);
    }
    return null;
  }

  public double readPointData(LatLonPoint point) throws IOException {
    // Find the closest R-Tree Cell
    final LatLonPoint2D p = new LatLonPoint2D.Double(point.getLatitude(), point.getLongitude());
    Cell c = meshset.getMesh().getCellFromLatLon(p);
    double z = -1;

    List<? extends Entity> e;
    if (cellLocation.equals("node")) {
      e = c.getNodes();
    } else if (cellLocation.equals("face")) {
      e = c.getFaces();
    } else if (cellLocation.equals("edge")) {
      e = c.getEdges();
    } else {
      e = null;
    }
    if ((e != null) && (e.size() > 0)) {

      // Sort the collection of Entities by distance from the query point.
      // This should offer different ways to calculate the distance of
      // the closest point.

      // "e" is all of the Entities in the Cell that are on the variable's
      // location (Node OR Edge OR Face).

      Collections.sort(e, new Comparator() {

        public int compare(Object o1, Object o2) {
          Entity e1 = (Entity) o1;
          Entity e2 = (Entity) o2;
          if (e1.getGeoPoint().distance(p) == e2.getGeoPoint().distance(p)) {
            return 0;
          } else if (e1.getGeoPoint().distance(p) > e2.getGeoPoint().distance(p)) {
            return 1;
          } else {
            return -1;
          }
        }
      });

      // Get the closest Entities DataIndex into the NetCDF file.
      int in = e.get(0).getDataIndex();
      try {

        // Need to compute actual ranges here, not assume it is (time,z,entity)
        List<Range> r = new ArrayList<Range>();
        // Time (first)
        r.add(new Range(0, 0));
        // Sigma (first)
        r.add(new Range(0, 0));
        // Data (DataIndex from Cell)
        r.add(new Range(in, in));

        float[] ret1D = (float[]) vs.read(r).copyTo1DJavaArray();
        z = ret1D[0];
      } catch (InvalidRangeException ex) {
        System.out.println(ex);
      }
    }
    return z;
  }

  public String getInfo() {
    StringBuilder buf = new StringBuilder(200);
    buf.setLength(0);
    buf.append(getName());
    Format.tab(buf, 30, true);
    buf.append(getUnitsString());
    Format.tab(buf, 60, true);
    buf.append(hasMissingData());
    Format.tab(buf, 66, true);
    buf.append(getDescription());
    return buf.toString();
  }

  public VariableDS getVariable() {
    return vs;
  }

  public int compareTo(UGridDatatype g) {
    return getNameEscaped().compareTo(g.getNameEscaped());
  }
}
