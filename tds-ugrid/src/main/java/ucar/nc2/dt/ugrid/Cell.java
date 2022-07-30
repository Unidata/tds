/*
 * Copyright (c) 2011-2014 Applied Science Associates
 * See LICENSE for license information.
 */

package ucar.nc2.dt.ugrid;

import ucar.nc2.dt.ugrid.geom.LatLonPolygon2D;
import java.util.ArrayList;

/**
 *
 * @author Kyle
 */
public class Cell {

  private LatLonPolygon2D polygon;
  private ArrayList<Entity> entities;
  private ArrayList<Node> nodes;
  private ArrayList<Edge> edges;
  private ArrayList<Face> faces;
  private int connectivity_index;


  public Cell() {
    // Initialize the entities ArrayList
    entities = new ArrayList<Entity>(3);
  }

  private void setEntities(Entity e) {
    if (!entities.contains(e)) {
      entities.add(e);
    }
  }

  private void setEntities(ArrayList<? extends Entity> ets) {
    for (Entity e : ets) {
      setEntities(e);
    }
  }

  public ArrayList<Entity> getEntities() {
    return entities;
  }

  public int[] getEntityIndexes() {
    int[] r = new int[entities.size()];
    for (int i = 0; i < entities.size(); i++) {
      r[i] = entities.get(i).getDataIndex();
    }
    return r;
  }

  public LatLonPolygon2D getPolygon() {
    return polygon;
  }

  public void setPolygon(LatLonPolygon2D poly) {
    polygon = poly;
  }

  public void setPolygon() {
    polygon = new LatLonPolygon2D.Double();
    for (Node n : getNodes()) {
      polygon.lineTo(n.getGeoPoint());
    }
  }

  public boolean hasNodes() {
    return nodes != null;
  }

  public ArrayList<Node> getNodes() {
    return nodes;
  }

  public int[] getNodeIndexes() {
    int[] r = new int[nodes.size()];
    for (int i = 0; i < nodes.size(); i++) {
      r[i] = nodes.get(i).getDataIndex();
    }
    return r;
  }

  public void setNodes(ArrayList<Node> nodes) {
    this.nodes = nodes;
    this.setPolygon();
    setEntities(nodes);
  }

  public boolean hasEdges() {
    return edges != null;
  }

  public ArrayList<Edge> getEdges() {
    return edges;
  }

  public void setEdges(ArrayList<Edge> edges) {
    this.edges = edges;
    setEntities(edges);
  }

  public boolean hasFaces() {
    return faces != null;
  }

  public ArrayList<Face> getFaces() {
    return faces;
  }

  public void setFaces(ArrayList<Face> faces) {
    this.faces = faces;
    setEntities(faces);
  }

  public void setConnectivityIndex(int connectivity_index) {
    this.connectivity_index = connectivity_index;
  }

}
