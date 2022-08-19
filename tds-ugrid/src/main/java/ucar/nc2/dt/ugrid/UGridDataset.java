/*
 * Copyright (c) 1998-2022 University Corporation for Atmospheric Research/Unidata and Applied Science Associates
 * See LICENSE for license information.
 */

package ucar.nc2.dt.ugrid;

import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.Attribute;
import ucar.nc2.AttributeContainer;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.dataset.*;
import ucar.nc2.dataset.NetcdfDataset.Enhance;
import ucar.nc2.dataset.conv.UGridConvention;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.UGridDatatype;
import ucar.nc2.dt.grid.internal.spi.GridDatasetProvider;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.nc2.util.cache.FileCache;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.units.DateRange;
import ucar.nc2.util.cache.FileCacheIF;
import ucar.unidata.geoloc.LatLonRect;

import java.io.IOException;
import java.net.URISyntaxException;

import ucar.nc2.constants.CF;
import ucar.nc2.dt.ugrid.geom.LatLonPoint2D;
import ucar.nc2.dt.ugrid.geom.LatLonPolygon2D;
import ucar.nc2.dt.ugrid.geom.LatLonRectangle2D;
import ucar.nc2.dt.ugrid.utils.NcdsFactory;
import ucar.nc2.dt.ugrid.utils.NcdsFactory.NcdsTemplate;
import ucar.unidata.geoloc.ProjectionRect;

import javax.annotation.Nullable;

/**
 * Make a UGridDataset into a collection of Meshsets
 */

public class UGridDataset implements ucar.nc2.dt.UGridDataset, ucar.nc2.ft.FeatureDataset {
  private static final Logger logger = LoggerFactory.getLogger(UGridDataset.class);
  // A dummy variable defining a Mesh is defined by the cf_role "mesh_topology"
  private static final String TOPOLOGY_VARIABLE = "mesh_topology";

  private NetcdfDataset ds;
  private ArrayList<MeshVariable> meshVariables = new ArrayList<MeshVariable>();
  private Map<String, Meshset> meshsetHash = new HashMap<String, Meshset>();

  /**
   * Open a netcdf dataset, using NetcdfDataset.defaultEnhanceMode plus CoordSystems
   * and turn into a UGridDataset.
   *
   * @param location netcdf dataset to open, using NetcdfDataset.acquireDataset().
   * @return GridDataset
   * @throws java.io.IOException on read error
   * @see ucar.nc2.dataset.NetcdfDataset#acquireDataset
   */
  static public UGridDataset open(String location) throws java.io.IOException {
    return open(location, NetcdfDataset.getDefaultEnhanceMode());
  }

  /**
   * Open a netcdf dataset, using NetcdfDataset.defaultEnhanceMode plus CoordSystems
   * and turn into a UGridDataset.
   *
   * @param location netcdf dataset to open, using NetcdfDataset.acquireDataset().
   * @param enhanceMode open netcdf dataset with this enhanceMode
   * @return GridDataset
   * @throws java.io.IOException on read error
   * @see ucar.nc2.dataset.NetcdfDataset#acquireDataset
   */
  static public UGridDataset open(String location, Set<NetcdfDataset.Enhance> enhanceMode) throws java.io.IOException {
    DatasetUrl durl = DatasetUrl.findDatasetUrl(location);
    NetcdfDataset ds = ucar.nc2.dataset.NetcdfDatasets.acquireDataset(null, durl, enhanceMode, -1, null, null);
    return new UGridDataset(ds);
  }

  /**
   * Create a UGridDataset from a NetcdfDataset.
   *
   * @param ds underlying NetcdfDataset, will do Enhance.CoordSystems if not already done.
   * @throws java.io.IOException on read error
   */
  public UGridDataset(NetcdfDataset ds) throws IOException {
    this(ds, null);
  }

  /**
   * Create a UGridDataset from a NetcdfDataset.
   *
   * @param ds underlying NetcdfDataset, will do Enhance.CoordSystems if not already done.
   * @param parseInfo put parse info here, may be null
   * @throws java.io.IOException on read error
   */
  public UGridDataset(NetcdfDataset ds, Formatter parseInfo) throws IOException {
    this.ds = ds;
    NetcdfDatasets.enhance(ds, NetcdfDataset.getDefaultEnhanceMode(), null);
    // look for Meshes
    if (parseInfo != null)
      parseInfo.format("UGridDataset looking for MeshVariables\n");
    List<Variable> vars = ds.getVariables();
    for (Variable var : vars) {
      // See how many "Mesh" are defined in the dataset
      if ((var.attributes().findAttributeIgnoreCase(CF.CF_ROLE)) != null
          && (var.attributes().findAttributeIgnoreCase(CF.CF_ROLE).getStringValue().equals(TOPOLOGY_VARIABLE))) {
        VariableEnhanced varDS = (VariableEnhanced) var;
        constructMeshVariable(ds, varDS, parseInfo);
      }
    }
  }

  private void constructMeshVariable(NetcdfDataset ds, VariableEnhanced v, Formatter parseInfo) {
    // Add Mesh to the "meshes" Hash
    if (v instanceof StructureDS) {
      StructureDS s = (StructureDS) v;
      List<Variable> members = s.getVariables();
      for (Variable nested : members) {
        constructMeshVariable(ds, (VariableEnhanced) nested, parseInfo);
      }
    } else {
      Mesh m = new Mesh(ds, v);
      addMesh((VariableDS) v, m, parseInfo);
    }
  }

  private void addMesh(VariableDS varDS, Mesh m, Formatter parseInfo) {
    Meshset meshset;
    if (null == (meshset = meshsetHash.get(m.getName()))) {
      meshset = new Meshset(m, varDS);
      meshsetHash.put(m.getName(), meshset);
      if (parseInfo != null)
        parseInfo.format(" -make new Mesh= %s\n", m.getName());
      // m.makeVerticalTransform(this, parseInfo);
      setVariables(meshset);
    }
  }

  private void setVariables(Meshset meshset) {
    for (Variable v : ds.getVariables()) {
      if (v.attributes().findAttributeIgnoreCase("mesh") != null) {
        if (v.attributes().findAttributeIgnoreCase("mesh").getStringValue().toLowerCase()
            .contains(meshset.getMesh().getName().toLowerCase())) {
          MeshVariable mv = new MeshVariable(this, (VariableDS) v, meshset);
          if (!meshVariables.contains(mv)) {
            meshVariables.add(mv);
            meshset.add(mv);
          }
        }
      }
    }
  }

  @Nullable
  public UGridDatatype getMeshVariableByName(String name) {
    UGridDatatype z = null;
    for (UGridDatatype m : meshVariables) {
      if (m.getName().equals(name)) {
        z = m;
        break;
      }
    }
    return z;
  }

  public List<UGridDatatype> getMeshVariables() {
    return new ArrayList<UGridDatatype>(meshVariables);
  }

  public void calcBounds() throws java.io.IOException {
    // not needed
  }

  public List<Attribute> getGlobalAttributes() {
    return ds.getGlobalAttributes();
  }

  public String getTitle() {
    Attribute titleAttr = ds.findGlobalAttributeIgnoreCase("title");
    String title = titleAttr != null ? titleAttr.getStringValue() : null;
    return (title == null) ? getName() : title;
  }

  public String getDescription() {
    Attribute descAttr = ds.findGlobalAttributeIgnoreCase("description");
    String desc = descAttr != null ? descAttr.getStringValue() : null;
    if (desc == null) {
      Attribute histAttr = ds.findGlobalAttributeIgnoreCase("history");
      desc = histAttr != null ? histAttr.getStringValue() : null;
    }
    return (desc == null) ? getName() : desc;
  }

  public String getName() {
    return ds.getLocation();
  }

  public String getLocation() {
    return ds.getLocation();
  }

  public String getLocationURI() {
    return ds.getLocation();
  }

  private DateRange dateRangeMax = null;
  private LatLonRect llbbMax = null;

  private void makeRanges() {

    for (ucar.nc2.dt.UGridDataset.Meshset ms : getMeshsets()) {
      Mesh m = ms.getMesh();
      LatLonRect llbb = m.getLatLonBoundingBox();
      if (llbbMax == null) {
        llbbMax = llbb;
      } else {
        llbbMax.extend(llbb);
      }
      /*
       * DateRange dateRange = m.getDateRange();
       * if (dateRange != null) {
       * if (dateRangeMax == null)
       * dateRangeMax = dateRange;
       * else
       * dateRangeMax.extend(dateRange);
       * }
       */
    }
  }

  public Date getStartDate() {
    if (dateRangeMax == null)
      makeRanges();
    return (dateRangeMax == null) ? null : dateRangeMax.getStart().getCalendarDate().toDate();
  }

  public Date getEndDate() {
    if (dateRangeMax == null)
      makeRanges();
    return (dateRangeMax == null) ? null : dateRangeMax.getEnd().getCalendarDate().toDate();
  }

  public LatLonRect getBoundingBox() {
    if (llbbMax == null)
      makeRanges();
    return llbbMax;
  }

  @Override
  public AttributeContainer attributes() {
    return null;
  }

  public Attribute findGlobalAttributeIgnoreCase(String name) {
    return ds.findGlobalAttributeIgnoreCase(name);
  }

  public List<VariableSimpleIF> getDataVariables() {
    List<VariableSimpleIF> result = new ArrayList<VariableSimpleIF>(meshVariables.size());
    for (UGridDatatype mv : getMeshVariables()) {
      if (mv.getVariable() != null)
        result.add(mv.getVariable());
    }
    return result;
  }

  public VariableSimpleIF getDataVariable(String shortName) {
    return ds.findVariable(shortName);
  }

  public NetcdfFile getNetcdfFile() {
    return ds;
  }

  public FeatureType getFeatureType() {
    return FeatureType.UGRID;
  }

  public DateRange getDateRange() {
    if (dateRangeMax == null)
      makeRanges();
    return dateRangeMax;
  }

  public String getImplementationName() {
    return ds.getConventionUsed();
  }


  public synchronized void close() throws java.io.IOException {
    if (fileCache != null) {
      fileCache.release(this);
    } else {
      try {
        if (ds != null)
          ds.close();
      } finally {
        ds = null;
      }
    }
  }

  public boolean syncExtend() throws IOException {
    // ds.syncExtend() has been deprecated. Just return false for now.
    // return (ds != null) ? ds.syncExtend() : false;
    return false;
  }

  protected FileCache fileCache;

  public void setFileCache(FileCache fileCache) {
    this.fileCache = fileCache;
  }

  // TODO: Show info about Grid and Coord systems
  private void getInfo(Formatter buf) {}

  public String getDetailInfo() {
    Formatter buff = new Formatter();
    getDetailInfo(buff);
    return buff.toString();
  }

  public NetcdfDataset getNetcdfDataset() {
    return ds;
  }

  public void getDetailInfo(Formatter buff) {
    getInfo(buff);
    buff.format("\n\n----------------------------------------------------\n");
    NetcdfDatasetInfo info = null;
    try {
      info = new NetcdfDatasetInfo(ds.getLocation());
      buff.format("%s", info.getParseInfo());
    } catch (IOException e) {
      buff.format("NetcdfDatasetInfo failed");
    } finally {
      if (info != null)
        try {
          info.close();
        } catch (IOException ee) {
        } // do nothing
    }
    buff.format("\n\n----------------------------------------------------\n");
    buff.format("%s", ds.toString());
    buff.format("\n\n----------------------------------------------------\n");
  }

  public List<ucar.nc2.dt.UGridDataset.Meshset> getMeshsets() {
    return new ArrayList<ucar.nc2.dt.UGridDataset.Meshset>(meshsetHash.values());
  }

  public UGridDataset subset(LatLonRect bounds) {
    // Create a new subsat UGridDataset and return
    NetcdfDataset ncd = null;
    try {
      ncd = NcdsFactory.getNcdsFromTemplate(NcdsTemplate.UGRID);
      for (Attribute a : this.getGlobalAttributes()) {
        ncd.addAttribute(null, a);
      }
      ncd.addAttribute(null,
          new Attribute("History", "Subset by NetCDF-Java UGRID Library; Translation date = " + new Date() + ";"));

      Mesh m4;
      LatLonRectangle2D r = new LatLonRectangle2D(
          new LatLonPoint2D.Double(bounds.getUpperLeftPoint().getLatitude(), bounds.getUpperLeftPoint().getLongitude()),
          new LatLonPoint2D.Double(bounds.getLowerRightPoint().getLatitude(),
              bounds.getLowerRightPoint().getLongitude()));
      LatLonPolygon2D p = new LatLonPolygon2D.Double(r);
      List<Cell> containedCells;
      for (ucar.nc2.dt.UGridDataset.Meshset ms3 : this.getMeshsets()) {
        ncd.addVariable(null, ms3.getDescriptionVariable());

        m4 = ms3.getMesh();
        m4.buildRTree();
        containedCells = m4.getCellsInPolygon(p);

        /*
         * Create the subsat Topology
         */
        m4.getTopology().subsetToDataset(this, ncd, containedCells);

        /*
         * MeshVariables which are on this Meshset
         */
        List<UGridDatatype> mvs = ms3.getMeshVariables();
        for (UGridDatatype mv : mvs) {
          ((MeshVariable) mv).subsetToDataset(this, ncd, containedCells);
        }
      }
      ncd.finish();

      // Coordinate Systems
      for (CoordinateSystem cs : this.getNetcdfDataset().getCoordinateSystems()) {
        for (Dimension d : cs.getDomain()) {
          if (ncd.findDimension(d.getFullName()) == null) {
            ncd.addDimension(null, d);
            Variable vd = this.getNetcdfDataset().findVariable(d.getFullName());
            if (vd == null) {
              ncd.addVariable(null, new VariableDS(null, vd, true));
            }
          }
          ncd.finish();
        }
        ncd.addCoordinateSystem(cs);
        ncd.finish();
      }

      // Coordinate Axes
      for (CoordinateAxis ax : this.getNetcdfDataset().getCoordinateAxes()) {
        if (ncd.findCoordinateAxis(ax.getFullNameEscaped()) == null) {
          ncd.addCoordinateAxis(ax);
          ncd.finish();
        }
      }

      return new UGridDataset(ncd);
    } catch (URISyntaxException | IOException e) {
      if (ncd == null) {
        logger.error("Unable to read NetcdfDataset UGRID template", e);
      } else {
        logger.error("Error creating UGridDataset", e);
      }
    }
    return null;
  }

  public CalendarDateRange getCalendarDateRange() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public CalendarDate getCalendarDateStart() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public CalendarDate getCalendarDateEnd() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public long getLastModified() {
    return ds == null ? 0 : ds.getLastModified();
  }

  @Override
  public void setFileCache(FileCacheIF fileCache) {

  }

  @Override
  public void release() throws IOException {

  }

  @Override
  public void reacquire() throws IOException {

  }

  @Override
  public List<GridDatatype> getGrids() {
    return new ArrayList<>(meshVariables);
  }

  @Override
  public GridDatatype findGridDatatype(String name) {
    return null;
  }

  @Override
  public GridDatatype findGridByShortName(String shortName) {
    return null;
  }

  @Override
  public ProjectionRect getProjBoundingBox() {
    return null;
  }

  @Override
  public List<Gridset> getGridsets() {
    return null;
  }

  /**
   * This is a set of MeshVariables with the same Mesh and Topology
   */
  public class Meshset implements ucar.nc2.dt.UGridDataset.Meshset {

    private Mesh mesh;
    private VariableDS description_variable;
    private List<UGridDatatype> meshVariables = new ArrayList<>();

    public Meshset(Mesh m, VariableDS conn) {
      this.mesh = m;
      this.description_variable = conn;
    }

    private void add(MeshVariable mv) {
      meshVariables.add(mv);
    }

    /**
     * Get list of MeshVariable objects
     */
    public List<UGridDatatype> getMeshVariables() {
      return meshVariables;
    }

    public UGridDatatype getMeshVariableByName(String name) {
      UGridDatatype z = null;
      for (UGridDatatype m : meshVariables) {
        if (m.getName().equals(name)) {
          z = m;
          break;
        }
      }
      return z;
    }

    public Mesh getMesh() {
      return mesh;
    }

    public VariableDS getDescriptionVariable() {
      return description_variable;
    }

  }

  public static class Factory implements GridDatasetProvider {

    @Override
    public boolean isMine(NetcdfDataset ncd) {
      boolean mine = false;
      if (ncd != null) {
        Attribute csbAttr = ncd.findGlobalAttribute(_Coordinate._CoordSysBuilder);
        if (csbAttr != null) {
          String csb = csbAttr.getStringValue();
          if (csb != null) {
            mine = csb == UGridConvention.class.getCanonicalName();
          }
        }
      }
      return mine;
    }

    @Override
    public boolean isMine(String location, Set<Enhance> enhanceMode) {
      boolean mine = false;
      try (NetcdfDataset ncd =
          NetcdfDatasets.acquireDataset(null, DatasetUrl.findDatasetUrl(location), enhanceMode, -1, null, null)) {
        mine = isMine(ncd);
      } catch (IOException e) {
        logger.warn("Could not test if {} is a GridDatasetProvider", location, e);
      }
      return mine;
    }

    @Nullable
    @Override
    public GridDataset open(String location, Set<Enhance> enhanceMode) throws IOException {
      return UGridDataset.open(location, enhanceMode);
    }

    @Nullable
    @Override
    public GridDataset open(NetcdfDataset ncd, Formatter parseInfo) throws IOException {
      return new UGridDataset(ncd, parseInfo);
    }
  }
}
