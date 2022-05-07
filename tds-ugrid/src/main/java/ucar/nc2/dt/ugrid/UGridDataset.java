/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation. Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package ucar.nc2.dt.ugrid;

import ucar.nc2.*;
import ucar.nc2.dataset.*;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.UGridDatatype;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.nc2.util.cache.FileCache;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.units.DateRange;
import ucar.nc2.util.cache.FileCacheIF;
import ucar.unidata.geoloc.LatLonRect;

import java.util.*;
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
  private NetcdfDataset ds;
  private ArrayList<MeshVariable> meshVariables = new ArrayList<MeshVariable>();
  private Map<String, Meshset> meshsetHash = new HashMap<String, Meshset>();

  // A dummy variable defining a Mesh is defined by the cf_role "mesh_topology"
  private static final String TOPOLOGY_VARIABLE = "mesh_topology";

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
    try {
      NetcdfDataset ncd = NcdsFactory.getNcdsFromTemplate(NcdsTemplate.UGRID);
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
      System.out.println(e);
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
    return null;
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
}
