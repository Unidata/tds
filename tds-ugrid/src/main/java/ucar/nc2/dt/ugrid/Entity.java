/*
 * Copyright (c) 2011-2014 Applied Science Associates
 * See LICENSE for license information.
 */

package ucar.nc2.dt.ugrid;

import ucar.nc2.dt.ugrid.geom.LatLonPoint2D;

/**
 *
 * @author Kyle
 */
public abstract class Entity {

  private LatLonPoint2D geopoint;
  private Cell[] connecting_cells;
  private int data_index;

  public void setDataIndex(int i) {
    data_index = i;
  }

  public int getDataIndex() {
    return data_index;
  }

  public void setGeoPoint(LatLonPoint2D point) {
    geopoint = point;
  }

  public LatLonPoint2D getGeoPoint() {
    return geopoint;
  }

  public void setConnectingCells(Cell[] cells) {
    connecting_cells = cells;
  }

  public Cell[] getConnectingCells() {
    return connecting_cells;
  }

  public boolean isBoundry() {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Entity other = (Entity) obj;
    if (this.geopoint != other.geopoint && (this.geopoint == null || !this.geopoint.equals(other.geopoint))) {
      return false;
    }
    if (this.data_index != other.data_index) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 17 + geopoint.hashCode();
    hash = hash * 31 + data_index;
    return hash;
  }

}
