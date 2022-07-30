/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * Node.java
 *
 * Created on 06-01-09
 */

package ucar.nc2.dt.ugrid.rtree;


import java.io.Serializable;

import ucar.nc2.dt.ugrid.geom.LatLonPolygon2D;
import ucar.nc2.dt.ugrid.geom.LatLonRectangle2D;


/**
 * The <code>Node</code> class represents a data node for an RTree. This class contains references to child nodes as
 * polygons with
 * associated ids
 *
 * NOTE: findEntry is not implemented. This is used in node deletion by the RTree.
 *
 * @author [TPL] <tlarocque@asascience.com>
 */
public class Node implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = -8760796047443657871L;
  int nodeId = 0;
  LatLonRectangle2D mbr = null;
  LatLonPolygon2D[] entries = null;
  int[] ids = null;
  int level;
  int entryCount;



  Node(int nodeId, int level, int maxNodeEntries) {
    this.nodeId = nodeId;
    this.level = level;
    entries = new LatLonPolygon2D[maxNodeEntries];
    ids = new int[maxNodeEntries];
  }

  void addEntryCopy(final LatLonRectangle2D r, int id) {
    LatLonPolygon2D p = new LatLonPolygon2D.Double(r);
    ids[entryCount] = id;
    entries[entryCount] = p;

    entryCount++;
    if (mbr == null) {
      mbr = r.copy();
    } else {
      mbr = expandMBR(mbr, r);
    }
  }

  void addEntryCopy(final LatLonPolygon2D p, int id) {
    ids[entryCount] = id;
    entries[entryCount] = p.copy();

    entryCount++;
    if (mbr == null) {
      mbr = p.getBouningLatLonRectangle2D();
    } else {
      mbr = expandMBR(mbr, p.getBoundingLatLonValues());
    }
  }

  /**
   * This is effectively the same as addEntryCopy()
   *
   * @param r
   * @param id
   */
  @Deprecated
  void addEntryNoCopy(LatLonRectangle2D r, int id) {
    LatLonPolygon2D p = new LatLonPolygon2D.Double(r);
    ids[entryCount] = id;
    entries[entryCount] = p;
    entryCount++;
    if (mbr == null) {
      mbr = r.copy();
    } else {
      mbr = expandMBR(mbr, r);
    }
  }

  void addEntryNoCopy(LatLonPolygon2D p, int id) {
    ids[entryCount] = id;
    entries[entryCount] = p;
    entryCount++;
    if (mbr == null) {
      mbr = p.getBouningLatLonRectangle2D();
    } else {
      mbr = expandMBR(mbr, p.getBoundingLatLonValues());
    }
  }



  // Return the index of the found entry, or -1 if not found
  // TODO find out why the id and the rectangle bounds must match
  // essentially.. why can't we find based on id alone?
  /**
   * This method is not implemented TODO implement this method
   */
  int findEntry(LatLonPolygon2D r, int id) {

    /*
     * for (int i = 0; i < entryCount; i++) {
     * if (id == ids[i] && r.equals(entries[i])) {
     * return i;
     * }
     * }
     */
    return -1;
  }



  // delete entry. This is done by setting it to null and copying the last
  // entry into its space.
  void deleteEntry(int i, int minNodeEntries) {
    int lastIndex = entryCount - 1;
    LatLonPolygon2D deletedRectangle = entries[i];
    entries[i] = null;
    if (i != lastIndex) {
      entries[i] = entries[lastIndex];
      ids[i] = ids[lastIndex];
      entries[lastIndex] = null;
    }
    entryCount--;

    // if there are at least minNodeEntries, adjust the MBR.
    // otherwise, don't bother, as the node will be
    // eliminated anyway.
    if (entryCount >= minNodeEntries) {
      recalculateMBR(deletedRectangle);
    }
  }

  // TODO: this methods algorithm may be less then optimal [TPL]
  // oldRectangle is a rectangle that has just been deleted or made smaller.
  // Thus, the MBR is only recalculated if the OldRectangle influenced the old
  // MBR
  void recalculateMBR(LatLonPolygon2D deletedRectangle) {
    // FIXME: implement this method for entry deletion
    /*
     * if (mbr.edgeOverlaps(deletedRectangle)) {
     * mbr.set(entries[0].min, entries[0].max);
     * 
     * for (int i = 1; i < entryCount; i++) {
     * mbr.add(entries[i]);
     * }
     * }
     */
    throw new java.lang.UnsupportedOperationException("Recalculate method not implemented");
  }


  /**
   * Adds the the given <code>poly</code> to <code>mbr</code> as if mbr were a rectangle. Neither objects are modified.
   *
   * @param mbr
   *        the minimum bounding rectangle
   * @param poly
   *        a polygon to be fit inside the mbr
   *
   * @return the bounding rectangle of the union between mbr and poly as polgon2d object
   */
  // static LatLonRectangle2D expandMBR(final LatLonRectangle2D mbr, double bottom, double left, double right, top) {
  //
  // TODO: write this ... mbr.extend(null)
  //
  // return mbr;
  // }



  /**
   * Adds the the given <code>poly</code> to <code>mbr</code> as if mbr were a rectangle. Neither objects are modified.
   *
   * @param mbr
   *        the minimum bounding rectangle
   * @param poly
   *        a polygon to be fit inside the mbr
   *
   * @return the bounding rectangle of the union between mbr and poly as polgon2d object
   */
  public static LatLonRectangle2D expandMBR(final LatLonRectangle2D mbr, final LatLonPolygon2D poly) {
    mbr.extend(poly);

    return mbr;
  }



  /**
   * Adds the the given <code>rect</code> to <code>mbr</code> as if mbr were a rectangle. Neither objects are modified.
   *
   * @param mbr
   *        the minimum bounding rectangle
   * @param poly
   *        a polygon to be fit inside the mbr
   *
   * @return the bounding rectangle of the union between mbr and poly as polgon2d object
   */
  public static LatLonRectangle2D expandMBR(final LatLonRectangle2D mbr, final LatLonRectangle2D rect) {
    mbr.extend(rect);

    return mbr;
  }


  /**
   * Adds the the given <code>rect</code> to <code>mbr</code> as if mbr were a rectangle. Neither objects are modified.
   *
   * @param mbr
   *        the minimum bounding rectangle
   * @param poly
   *        a polygon to be fit inside the mbr
   *
   * @return the bounding rectangle of the union between mbr and poly as polgon2d object
   */
  public static LatLonRectangle2D expandMBR(final LatLonRectangle2D mbr, final double[] latLons) {
    mbr.extend(latLons);

    return mbr;
  }



  public int getEntryCount() {
    return entryCount;
  }

  public LatLonPolygon2D getEntry(int index) {
    if (index < entryCount) {
      return entries[index];
    }
    return null;
  }

  public int getId(int index) {
    if (index < entryCount) {
      return ids[index];
    }
    return -1;
  }


  public static long reorgTime = 0;

  /**
   * eliminate null entries, move all entries to the start of the source node
   */
  void reorganize(RTree rtree) {
    long curTime = System.currentTimeMillis();
    int countdownIndex = rtree.maxNodeEntries - 1;
    for (int index = 0; index < entryCount; index++) {
      if (entries[index] == null) {
        while (entries[countdownIndex] == null && countdownIndex > index) {
          countdownIndex--;
        }
        entries[index] = entries[countdownIndex];
        ids[index] = ids[countdownIndex];
        entries[countdownIndex] = null;
      }
    }
    reorgTime += (System.currentTimeMillis() - curTime);
  }

  boolean isLeaf() {
    return (level == 1);
  }

  public int getLevel() {
    return level;
  }

  public LatLonRectangle2D getMBR() {
    return mbr;
  }
}
