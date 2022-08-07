/*
 * Copyright (c) 2011-2014 Applied Science Associates
 * See LICENSE for license information.
 */

package ucar.nc2.dt.ugrid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.ugrid.topology.Topology;
import cern.colt.list.IntArrayList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Arrays;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableEnhanced;
import ucar.nc2.dt.ugrid.geom.LatLonPoint2D;
import ucar.nc2.dt.ugrid.geom.LatLonPolygon2D;
import ucar.nc2.dt.ugrid.geom.LatLonRectangle2D;
import ucar.nc2.dt.ugrid.rtree.RTree;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author Kyle
 */
public class Mesh {

  private static final Logger logger = LoggerFactory.getLogger(Mesh.class);
  // Standards (required)
  private static final String DIMENSION = "topology_dimension";
  private static final String NODE_COORDINATES = "node_coordinates";
  private static final String FACE_COORDINATES = "face_coordinates";
  private static final String FACE_NODE_CONNECTIVITY = "face_node_connectivity";

  private String name;
  private RTree rtree;
  private List<Cell> cells = new ArrayList<Cell>();
  // A Mesh should only have one connectivity array!
  private Topology topology = new Topology();
  private List<CoordinateSystem> coordinate_systems = new ArrayList<CoordinateSystem>();



  public Mesh(NetcdfDataset ds, VariableEnhanced v) {
    name = v.getFullName();

    Properties props = new Properties();
    props.setProperty("MaxNodeEntries", "30");
    props.setProperty("MinNodeEntries", "15");
    rtree = new RTree(props);

    processTopologyVariable(ds, v);
  }

  private void processTopologyVariable(NetcdfDataset ncd, VariableEnhanced v) {
    ArrayList<Attribute> foundCoords = new ArrayList<Attribute>();
    ArrayList<String> locations = new ArrayList<String>();
    Attribute att = null;

    Attribute dims = ((VariableDS) v).attributes().findAttributeIgnoreCase(DIMENSION);
    if (dims == null) {
      logger.debug("No '" + DIMENSION + "' attribute defined for the Mesh");
      return;
    }

    Attribute node_coord = ((VariableDS) v).attributes().findAttributeIgnoreCase(NODE_COORDINATES);
    if (node_coord == null) {
      logger.debug("No '" + NODE_COORDINATES + "' attribute defined for the Mesh");
      return;
    } else {
      foundCoords.add(node_coord);
      locations.add("node");
    }

    Attribute face_coord = ((VariableDS) v).attributes().findAttributeIgnoreCase(FACE_COORDINATES);
    if (face_coord == null) {
      logger.debug("No '" + FACE_COORDINATES + "' attribute defined for the Mesh");
      return;
    } else {
      foundCoords.add(face_coord);
      locations.add("face");
    }

    Attribute face_node_connect = ((VariableDS) v).attributes().findAttributeIgnoreCase(FACE_NODE_CONNECTIVITY);
    if (face_node_connect == null) {
      System.out
          .println("No '" + FACE_NODE_CONNECTIVITY + "' attribute defined for the Mesh. Only 2D and 3D supported.");
      return;
    } else {
      topology.setFaceNodeConnectivityVariable(ncd.findVariable(face_node_connect.getStringValue()));
    }

    // TODO: Support face/edge coordinates
    // TODO: Support face_face/face_edge/edge_node connectivities

    String attString;
    findCoord: for (Attribute attr : foundCoords) {
      List<String> elements = (List<String>) Arrays.asList(attr.getStringValue().toLowerCase().split((" ")));
      for (CoordinateSystem coord : ncd.getCoordinateSystems()) {
        try {
          if (elements.contains(coord.getLatAxis().getShortName())
              && elements.contains(coord.getLonAxis().getShortName())) {
            coordinate_systems.add(coord);
            break;
          }
        } catch (Exception e) {
          continue;
        }
      }
    }
    cells = topology.createCells(locations, coordinate_systems);
  }

  public void buildRTree() {
    for (int i = 0; i < cells.size(); i++) {
      rtree.add(cells.get(i).getPolygon(), i);
    }
  }

  public String getName() {
    return name;
  }

  public int getSize() {
    return cells.size();
  }

  public int getTreeSize() {
    return rtree.size();
  }

  public int getNodeSize() {
    int i = 0;
    for (Cell c : cells) {
      if (c.hasNodes()) {
        i += c.getNodes().size();
      }
    }
    return i;
  }

  public double[][] getNodeLatLons() {
    double[][] ll = new double[this.getUniqueNodeSize()][2];
    double[] d = new double[2];
    for (Node n : getUniqueNodes()) {
      d[0] = n.getGeoPoint().getLatitude();
      d[1] = n.getGeoPoint().getLongitude();
      ll[ll.length - 1] = d;
    }
    return ll;
  }

  public int[] getNodeIndexes() {
    int[] in = new int[this.getUniqueNodeSize()];
    for (Node n : getUniqueNodes()) {
      in[in.length - 1] = n.getDataIndex();
    }
    return in;
  }

  public ArrayList<Node> getUniqueNodes() {
    ArrayList<Node> ents = new ArrayList<Node>();
    for (Cell c : cells) {
      if (c.hasNodes()) {
        ents.addAll(c.getNodes());
      }
    }
    Set set = new HashSet(ents);
    ArrayList unique = new ArrayList(set);
    return unique;
  }

  public int getUniqueNodeSize() {
    return getUniqueNodes().size();
  }

  public ArrayList<Edge> getUniqueEdges() {
    ArrayList<Edge> ents = new ArrayList<Edge>();
    for (Cell c : cells) {
      if (c.hasEdges()) {
        ents.addAll(c.getEdges());
      }
    }
    Set set = new HashSet(ents);
    ArrayList unique = new ArrayList(set);
    return unique;
  }

  public int getEdgeSize() {
    int i = 0;
    for (Cell c : cells) {
      if (c.hasEdges()) {
        i += c.getEdges().size();
      }
    }
    return i;
  }

  public ArrayList<Face> getUniqueFaces() {
    ArrayList<Face> ents = new ArrayList<Face>();
    for (Cell c : cells) {
      if (c.hasFaces()) {
        ents.addAll(c.getFaces());
      }
    }
    Set set = new HashSet(ents);
    ArrayList unique = new ArrayList(set);
    return unique;
  }

  public int getFaceSize() {
    int i = 0;
    for (Cell c : cells) {
      if (c.hasFaces()) {
        i += c.getFaces().size();
      }
    }
    return i;
  }

  public LatLonRect getLatLonBoundingBox() {
    LatLonRectangle2D bounds = rtree.getBounds();
    return new LatLonRect((LatLonPoint) new LatLonPointImpl(bounds.getLatMin(), bounds.getLonMin()),
        (LatLonPoint) new LatLonPointImpl(bounds.getLatMax(), bounds.getLonMax()));
  }

  public Cell getCellFromLatLon(double lat, double lon) {
    LatLonPoint2D p = new LatLonPoint2D.Double(lat, lon);
    return cells.get(rtree.nearest(p));
  }

  public Cell getCellFromLatLon(LatLonPoint2D p) {
    return cells.get(rtree.nearest(p));
  }

  public ArrayList<Cell> getCellsInPolygon(LatLonPolygon2D p) {
    IntArrayList polys = rtree.intersects(p);
    ArrayList<Cell> containedCells = new ArrayList<Cell>(polys.size());
    for (int i : polys.elements()) {
      containedCells.add(this.cells.get(i));
    }
    return containedCells;
  }

  public List<Cell> getCells() {
    return cells;
  }

  public List<Cell> getPolygons() {
    return cells;
  }

  public Topology getTopology() {
    return topology;
  }

  public Mesh subset(LatLonRect bounds) {
    LatLonRectangle2D r = new LatLonRectangle2D(
        new LatLonPoint2D.Double(bounds.getUpperLeftPoint().getLatitude(), bounds.getUpperLeftPoint().getLongitude()),
        new LatLonPoint2D.Double(bounds.getLowerRightPoint().getLatitude(),
            bounds.getLowerRightPoint().getLongitude()));
    LatLonPolygon2D p = new LatLonPolygon2D.Double(r);
    ArrayList<Cell> containedCells = this.getCellsInPolygon(p);
    return null;
  }

  @Override
  public String toString() {
    // Don't use commas (,) in the string output.
    ArrayList<String> sb = new ArrayList<String>();
    sb.add(this.getName());
    sb.add("Mesh contains: " + this.getSize() + " cells (polygons)");
    sb.add("Mesh contains: " + this.getNodeSize() + " nodes (" + this.getUniqueNodeSize() + " unique) ");
    if (this.getSize() != 0) {
      sb.add("Mesh contains: " + this.getNodeSize() / this.getSize() + " nodes per cell");
    }
    sb.add("Mesh contains: " + this.getEdgeSize() + " edges.");
    sb.add("Mesh contains: " + this.getFaceSize() + " faces.");
    return sb.toString().replace(",", "\n");
  }

}
