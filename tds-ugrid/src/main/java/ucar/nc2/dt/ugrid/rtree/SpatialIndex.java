/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * SpatialIndex.java
 *
 * Created on 06-01-09
 */

package ucar.nc2.dt.ugrid.rtree;


import java.util.Properties;

import ucar.nc2.dt.ugrid.geom.LatLonPoint2D;
import ucar.nc2.dt.ugrid.geom.LatLonPolygon2D;
import ucar.nc2.dt.ugrid.geom.LatLonRectangle2D;


/**
 * The <code>SpatialIndex</code> interfaces is a contract for spacial indices. Implemented by RTree
 * 
 * @version 1.0
 */
public interface SpatialIndex {

  /**
   * Initializes any implementation dependent properties of the spatial index. For example, RTree implementations will
   * have a NodeSize
   * property.
   * 
   * @param props
   *        The set of properties used to initialize the spatial index.
   */
  public void init(Properties props);



  /**
   * Adds a new polygon to the spatial index
   * 
   * @param p
   *        The polygon to add to the spatial index.
   * @param id
   *        The ID of the polygon to add to the spatial index. The result of adding more than one polygon with the same
   *        ID is
   *        undefined.
   */
  public void add(LatLonPolygon2D p, int id);



  /**
   * Deletes a polygon from the spatial index
   * 
   * @param p
   *        The polygon to delete from the spatial index
   * @param id
   *        The ID of the polygon to delete from the spatial index
   * 
   * @return <code>true</code> if the polygon was deleted, <code>false</code> if the polygon was not found, or the
   *         polygon was found but
   *         with a different ID
   */
  public boolean delete(LatLonPolygon2D p, int id);



  /**
   * Finds the polygon which is nearest to the passed point, and calls apply() on the passed IntProcedure for each one.
   * If multiple
   * nearest polygons are equadistant from the given point, apply() will be invoked for each of them.
   * 
   * @param p
   *        The point for which this method finds the nearest neighbours.
   * @param v
   *        The IntProcedure whose apply() method which is called for each nearest neighbour.
   * 
   * @param searchRadius
   *        The furthest distance away from a polygon to search. Polygons further than this will not be found.
   * 
   *        This should be as small as possible to minimize the search time.
   * 
   *        Use Double.POSITIVE_INFINITY to guarantee that the nearest polygon is found, no matter how far away,
   *        although this will
   *        slow down the algorithm.
   */
  public void nearestNeighbors(LatLonPoint2D p, IntProcedure v, double searchRadius);



  /**
   * Finds all polygons that intersect the passed polygon.
   * 
   * @param r
   *        The polygon for which this method finds intersecting polygons.
   * 
   * @param ip
   *        The IntProcedure whose execute() method is is called for each intersecting polygon.
   */
  public void intersects(LatLonPolygon2D r, IntProcedure ip);



  /**
   * Finds all polygons contained by the passed polygon.
   * 
   * @param r
   *        The polygon for which this method finds contained polygons.
   * 
   * @param v
   *        The visitor whose visit() method is is called for each contained polygon.
   */
  public void contains(LatLonPolygon2D r, IntProcedure v);

  /**
   * Returns the number of entries in the spatial index
   */
  public int size();


  /**
   * Returns the bounds of all the entries in the spatial index, or null if there are no entries.
   */
  public LatLonRectangle2D getBounds();

  /**
   * Returns a string identifying the type of spatial index, and the version number, eg "SimpleIndex-0.1"
   */
  public String getVersion();

}
