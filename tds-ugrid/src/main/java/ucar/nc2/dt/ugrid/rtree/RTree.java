/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * RTree.java
 *
 * Created on 06-01-09
 */

package ucar.nc2.dt.ugrid.rtree;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.colt.list.AbstractIntList;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntObjectHashMap;

import ucar.nc2.dt.ugrid.geom.LatLonPoint2D;
import ucar.nc2.dt.ugrid.geom.LatLonPolygon2D;
import ucar.nc2.dt.ugrid.geom.LatLonRectangle2D;



/**
 * <p>
 * This is a lightweight RTree implementation, specifically designed for the following features (in order of
 * importance):
 * <ul>
 * <li>Fast intersection query performance. To achieve this, the RTree uses only main memory to store entries. Obviously
 * this will only
 * improve performance if there is enough physical memory to avoid paging.</li>
 * <li>Low memory requirements.</li>
 * <li>Fast add performance.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The main reason for the high speed of this RTree implementation is the avoidance of the creation of unnecessary
 * objects, mainly achieved
 * by using primitive collections from the COLT library.
 * </p>
 *
 * NOTE: Node.findEntry() is not implemented and recalculateMBR() is not implemented. These are used in node deletion,
 * therefore nodes
 * cannot be deleted at this time. 06-05-09
 *
 * @author TPL <tlarocque@asascience.com>
 * @version 1.0
 */
public class RTree implements SpatialIndex, Serializable {

  /* TODO: overload intersects and contains to accept a centerpoint and radius for circular AOI's */
  /* TODO: fix the scope of these variables. Many of them do not require global scope */
  /* TODO: implement the necessary methods for node deletion and test them fully */
  /*
   * TODO: many new methods have been added to this class. Make sure this is properly abstracted
   * and the methods are added to this classes parent: SpatialIndex
   */


  /** Static Fields */
  private static final long serialVersionUID = 4027952803059081589L;
  private static final String VERSION = "1.0";
  private static final Logger logger = LoggerFactory.getLogger(RTree.class);
  private final static int DEFAULT_MAX_NODE_ENTRIES = 10;
  // internal consistency checking - set to true if debugging tree corruption
  private final static boolean INTERNAL_CONSISTENCY_CHECKING = false;
  // used to mark the status of entries during a node split
  private final static int ENTRY_STATUS_ASSIGNED = 0;
  private final static int ENTRY_STATUS_UNASSIGNED = 1;



  /** Instance Fields */
  int maxNodeEntries;
  int minNodeEntries;

  // map of nodeId -> node object
  // [x] TODO eliminate this map - it should not be needed. Nodes
  // can be found by traversing the tree.
  /*
   * [TPL] not sure why the original author wanted this nodeMap deleted. It appears to
   * be required considering its widespread use throughout this class.
   */
  private OpenIntObjectHashMap nodeMap = new OpenIntObjectHashMap();


  private byte[] entryStatus = null;
  private byte[] initialEntryStatus = null;

  // stacks used to store nodeId and entry index of each node
  // from the root down to the leaf. Enables fast lookup
  // of nodes when a split is propagated up the tree.
  private Stack<Integer> parents = new Stack<Integer>();
  private Stack<Integer> parentsEntry = new Stack<Integer>();

  /* Initialization: leaves are always at level 1 */
  private int treeHeight = 1;

  private int rootNodeId = 0;
  private int size = 0;

  /* Enables creation of new nodes */
  private int highestUsedNodeId = rootNodeId;

  // Deleted node objects are retained in the nodeMap,
  // so that they can be reused. Store the IDs of nodes
  // which can be reused.
  private Stack<Integer> deletedNodeIds = new Stack<Integer>();

  // List of nearest rectangles. Use a member variable to
  // avoid recreating the object each time nearest() is called.
  // private TIntArrayList nearestIds = new TIntArrayList();
  private IntArrayList nearestIds = new IntArrayList();
  private OpenIntObjectHashMap nearestEntriesMap = new OpenIntObjectHashMap();



  /**
   * Inner class used as a bridge (adapter) between the COLT TIntProcedure and the SpatialIndex IntProcedure. This is
   * used because the
   * nearest rectangles must be stored as they are found, in case a closer one is found later.
   *
   * A single instance of this class is used to avoid creating a new one every time nearest() is called.
   */
  private static class TIntProcedureVisit implements cern.colt.function.IntProcedure, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 3278783388762254894L;
    public IntProcedure m_intProcedure = null;

    public void setProcedure(IntProcedure ip) {
      m_intProcedure = ip;
    }

    public boolean apply(int index) {
      m_intProcedure.execute(index);
      return true;
    }
  };

  private TIntProcedureVisit visitProc = new TIntProcedureVisit();

  /**
   * Constructor. Use init() method to initialize parameters of the RTree.
   */
  public RTree() {
    Properties props = new Properties();
    props.setProperty("MaxNodeEntries", "12");
    props.setProperty("MinNodeEntries", "6");

    init(props);
  }

  public RTree(Properties props) {
    init(props);
  }

  // -------------------------------------------------------------------------
  // public implementation of SpatialIndex interface:
  // init(Properties)
  // add(Rectangle2D, int)
  // delete(Rectangle2D, int)
  // nearest(Point2D, IntProcedure, double)
  // intersects(Rectangle2D, IntProcedure)
  // contains(Rectangle2D, IntProcedure)
  // size()
  // -------------------------------------------------------------------------
  /**
   * <p>
   * Initialize implementation dependent properties of the RTree. Currently implemented properties are:
   * <ul>
   * <li>MaxNodeEntries</li> This specifies the maximum number of entries in a node. The default value is 10, which is
   * used if the
   * property is not specified, or is less than 2.
   * <li>MinNodeEntries</li> This specifies the minimum number of entries in a node. The default value is half of the
   * MaxNodeEntries value
   * (rounded down), which is used if the property is not specified or is less than 1.
   * </ul>
   * </p>
   *
   * @see com.asascience.data.rtree.SpatialIndex#init(Properties)
   */
  public void init(Properties props) {
    maxNodeEntries = Integer.parseInt(props.getProperty("MaxNodeEntries", "0"));
    minNodeEntries = Integer.parseInt(props.getProperty("MinNodeEntries", "0"));


    // Obviously a node with less than 2 entries cannot be split.
    // The node splitting algorithm will work with only 2 entries
    // per node, but will be inefficient.
    if (maxNodeEntries < 2) {
      // log.warn("Invalid MaxNodeEntries = " + maxNodeEntries +
      // " Resetting to default value of "
      // + DEFAULT_MAX_NODE_ENTRIES);
      maxNodeEntries = DEFAULT_MAX_NODE_ENTRIES;
    }

    // The MinNodeEntries must be less than or equal to (int)
    // (MaxNodeEntries / 2)
    if (minNodeEntries < 1 || minNodeEntries > maxNodeEntries / 2) {
      // log.warn("MinNodeEntries must be between 1 and MaxNodeEntries / 2");
      minNodeEntries = maxNodeEntries / 2;
    }

    entryStatus = new byte[maxNodeEntries];
    initialEntryStatus = new byte[maxNodeEntries];

    for (int i = 0; i < maxNodeEntries; i++) {
      initialEntryStatus[i] = ENTRY_STATUS_UNASSIGNED;
    }

    Node root = new Node(rootNodeId, 1, maxNodeEntries);
    nodeMap.put(rootNodeId, root);

    // log.info("init() " + " MaxNodeEntries = " + maxNodeEntries +
    // ", MinNodeEntries = " + minNodeEntries);
  }

  /**
   * @see com.asascience.data.rtree.SpatialIndex#add(Rectangle2D, int)
   */
  public void add(LatLonPolygon2D r, int id) {
    // if (log.isDebugEnabled()) {
    // log.debug("Adding rectangle " + r + ", id " + id);
    // }

    add(r.copy(), id, 1);

    size++;
  }

  public static long addTime = 0;

  /**
   * Adds a new entry at a specified level in the tree
   */
  private void add(LatLonPolygon2D r, int id, int level) {
    long curTime = System.currentTimeMillis();
    /*
     * I1 [Find position for new record]
     * Invoke ChooseLeaf to select a leaf node L in which to place r
     */
    Node leaf = chooseNode(r, level);
    Node newLeaf = null;

    /*
     * I2 [Add record to leaf node]
     * If L has room for another entry, install E. Otherwise
     * invoke SplitNode to obtain L and LL containing E and all
     * the old entries of L
     */
    if (leaf.entryCount < maxNodeEntries) {
      leaf.addEntryNoCopy(r, id);
    } else {
      // n.reorganize(this);
      newLeaf = splitNode(leaf, r, id);
    }

    /*
     * I3 [Propagate changes upwards]
     * Invoke AdjustTree on L, also passing LL if a split was performed
     */
    Node newNode = adjustTree(leaf, newLeaf);

    /*
     * I4 [Grow tree taller]
     * If node split propagation caused the root to split, create a
     * new root whose children are the two resulting nodes.
     */
    if (newNode != null) {
      // log.info("* * * * Adj Node = " + newNode.mbr);
      Node oldRoot = getNode(rootNodeId);

      rootNodeId = getNextNodeId();
      treeHeight++;
      Node root = new Node(rootNodeId, treeHeight, maxNodeEntries);
      root.addEntryCopy(newNode.mbr, newNode.nodeId);
      root.addEntryCopy(oldRoot.mbr, oldRoot.nodeId);
      nodeMap.put(rootNodeId, root);
    }

    if (INTERNAL_CONSISTENCY_CHECKING) {
      checkConsistency(rootNodeId, treeHeight, null);
    }
    addTime += System.currentTimeMillis() - curTime;
  }

  /*
   * @see com.asascience.data.rtree.SpatialIndex#delete(LatLonRectangle2D, int)
   */
  public boolean delete(LatLonPolygon2D r, int id) {
    // FindLeaf algorithm inlined here. Note the "official" algorithm
    // searches all overlapping entries. This seems inefficient to me,
    // as an entry is only worth searching if it contains (NOT overlaps)
    // the rectangle we are searching for.
    //
    // Also the algorithm has been changed so that it is not recursive.

    // FL1 [Search subtrees] If root is not a leaf, check each entry
    // to determine if it contains r. For each entry found, invoke
    // findLeaf on the node pointed to by the entry, until r is found or
    // all entries have been checked.
    parents.clear();
    parents.push(rootNodeId);

    parentsEntry.clear();
    parentsEntry.push(-1);
    Node n = null;
    int foundIndex = -1; // index of entry to be deleted in leaf

    while (foundIndex == -1 && parents.size() > 0) {
      n = getNode(parents.peek());
      int startIndex = parentsEntry.peek() + 1;

      if (!n.isLeaf()) {
        // deleteLog.debug("searching node " + n.nodeId +
        // ", from index " + startIndex);
        boolean contains = false;
        for (int i = startIndex; i < n.entryCount; i++) {
          if (n.entries[i].getBouningLatLonRectangle2D().contains(r)) {
            parents.push(n.ids[i]);
            parentsEntry.pop();
            parentsEntry.push(i); // this becomes the start index
            // when the child has been
            // searched
            parentsEntry.push(-1);
            contains = true;
            break; // ie go to next iteration of while()
          }
        }
        if (contains) {
          continue;
        }
      } else {
        foundIndex = n.findEntry(r, id);
      }

      parents.pop();
      parentsEntry.pop();
    } // while not found

    if (foundIndex != -1) {
      n.deleteEntry(foundIndex, minNodeEntries);
      condenseTree(n);
      size--;
    }

    // shrink the tree if possible (i.e. if root node has exactly one
    // entry,and that
    // entry is not a leaf node, delete the root (it's entry becomes the new
    // root)
    Node root = getNode(rootNodeId);
    while (root.entryCount == 1 && treeHeight > 1) {
      root.entryCount = 0;
      rootNodeId = root.ids[0];
      treeHeight--;
      root = getNode(rootNodeId);
    }

    return (foundIndex != -1);
  }

  public int nearest(final Point2D p, double searchRadius) {
    return nearest(new LatLonPoint2D.Double(p), searchRadius);
  }

  /**
   * Convenience method for nearestNeighbor search where the IntProcedure is handled internally. Uses the passed search
   * radius.
   *
   * @param p
   *        the query point.
   * @param searchRadius
   *        the searchRadius to use in the units of the loaded polygons
   * @return the index of the nearest neighbor to the input point. returns -1 if no points are found within the
   *         searchRadius.
   */
  public int nearest(final LatLonPoint2D p, double searchRadius) {
    IntProcedureEntriesList ip = new IntProcedureEntriesList();
    this.nearest(p, ip, searchRadius);
    AbstractIntList res = ip.getValues();
    res.trimToSize();
    if (res.isEmpty()) {
      return -1;
    }
    return res.elements()[0];
  }

  public int nearest(final Point2D p) {
    return nearest(new LatLonPoint2D.Double(p));
  }

  /**
   * Convenience method for nearestNeighbor search where the IntProcedure is handled internally. Uses the default search
   * radius of 0.5
   * units.
   *
   * @param p
   *        the query point.
   * @return the index of the nearest neighbor to the input point. returns -1 if no points are found within the search
   *         radius.
   */
  public int nearest(final LatLonPoint2D p) {
    IntProcedureEntriesList ip = new IntProcedureEntriesList();
    this.nearest(p, ip);
    AbstractIntList res = ip.getValues();
    res.trimToSize();
    if (res.isEmpty()) {
      return -1;
    }
    return res.elements()[0];
  }

  /**
   * Finds the entry (polygon) which is closest to the given point, and invokes apply() on the given
   * <code>IntProcedure</code> for that
   * entry. Any entries found with a distance greater than 0.5 will not be returned. Also, if two or more entries are
   * equidistant from the
   * given point, <code>apply()</code> will only be invoked for the point closest by centroid.
   *
   * @param p
   *        The point for which this method finds the nearest neighbors
   * @param v
   *        The IntProcedure whose execute() method is is called for each nearest neighbor.
   * @param searchRadius
   *        The furthest distance away from a polygon to search. Polygons further than this will not be found. This
   *        should be as small
   *        as possible to minimize the search time. Use Double.POSITIVE_INFINITY to guarantee that the nearest polygon
   *        is found, no
   *        matter how far away, although this will slow down the algorithm.
   */
  public void nearest(final LatLonPoint2D p, IntProcedure v, double searchRadius) {
    /* Get all the nearest neighbors to this point */
    /* ******************************************* */
    nearestNeighborsExclusive(p, getNode(rootNodeId), searchRadius);



    /* If multiple entries are returned, find the closest by centroid */
    /* ************************************************************** */
    if (nearestEntriesMap.size() > 1) {



      /**
       * Local Inner Class for finding closest entry of a set of nearest neighbors. Used by
       * <code>nearestEntriesMap.forEachKey()</code>
       *
       * @author tlarocque
       */
      class FindClosestEntryProc implements cern.colt.function.IntProcedure, Serializable {

        /**
         *
         */
        private static final long serialVersionUID = -3036812482409644397L;
        private double distanceSquared = Double.POSITIVE_INFINITY;
        private int closestIndex = -1;

        /**
         * Sets <code>closestIndex</code> to the specified key if the centroid of that keys polygon is closer than the
         * last one
         * checked.
         */
        public boolean apply(int key) {
          LatLonPoint2D tempPoint = ((LatLonPolygon2D) nearestEntriesMap.get(key)).getCentroid();
          double tempDist = tempPoint.distanceSq(p);

          if (tempDist < distanceSquared) {
            distanceSquared = tempDist;
            closestIndex = key;
          } /* else: ignore additional equidistant entries */


          return true;
        }

        /**
         * @return the index value stored in this procedure which is associated with the closest polygon entry
         */
        public int getClosestIndex() {
          return closestIndex;
        }



      }; /* End local inner class */



      FindClosestEntryProc proc = new FindClosestEntryProc();
      nearestEntriesMap.forEachKey(proc);
      nearestIds.clear();
      nearestIds.add(proc.getClosestIndex());
    }


    /* Process the nearest entry (1) */
    visitProc.setProcedure(v);
    nearestIds.forEach(visitProc);
    nearestIds.clear();
  }



  /**
   * Finds the entry (polygon) which is closest to the given point, and invokes apply() on the given
   * <code>IntProcedure</code> for that
   * entry. Any entries found with a distance greater than 0.5 will not be returned. Also, if two or more entries are
   * equidistant from the
   * given point, <code>apply()</code> will only be invoked for the point closest by centroid.
   *
   * @param p
   *        The point for which this method finds the nearest neighbors
   * @param v
   *        The IntProcedure whose execute() method is is called for each nearest neighbor.
   */
  public void nearest(LatLonPoint2D p, IntProcedure v) {
    nearest(p, v, 0.5);
  }


  //
  // OLD CODE
  //
  // /*
  // * @see com.asascience.data.rtree.SpatialIndex#nearest(Point2D,
  // IntProcedure, double)
  // */
  // public void nearestNeighbors(Point2D p, IntProcedure v, double
  // searchRadius) {
  // Node rootNode = getNode(rootNodeId);
  //
  // nearestNeighbors(p, rootNode, searchRadius);
  //
  // visitProc.setProcedure(v);
  // nearestIds.forEach(visitProc);
  // nearestIds.clear();
  // }
  //
  //
  // OLD CODE
  //
  //
  // /**
  // * Finds the entry (polygon) which is closest to the given point, and
  // * invokes apply() on the given <code>IntProcedure</code> for this entry.
  // * Any entries found with a distance greater than 0.5 will not be
  // returned.
  // * Also, if two or more entries are equidistant from the given point,
  // * <code>apply()</code> will be invoked for each of these.
  // *
  // * @param p
  // * The point for which this method finds the nearest neighbors
  // * @param v
  // * The IntProcedure whose execute() method is is called for each
  // * nearest neighbor.
  // */
  // public void nearestNeighbors(Point2D p, IntProcedure v) {
  // nearestNeighbors(p, v, 0.5);
  // }

  /**
   * Convenience method for intersects search where the IntProcedure is handled internally. Returns only those cells
   * that are completely
   * contained within the <code>r</code> polygon.
   *
   * @param r
   *        the bounding polygon to search within
   * @return a list of indexes partially or completely contained within the bounding polygon
   */
  public IntArrayList intersects(LatLonPolygon2D r) {
    IntProcedureEntriesList ip = new IntProcedureEntriesList();
    this.intersects(r, ip);
    AbstractIntList res = ip.getValues();
    res.trimToSize();
    if (res.isEmpty()) {
      return new IntArrayList();
    }
    return (IntArrayList) res;
  }

  /*
   * @see com.asascience.data.rtree.SpatialIndex#intersects(LatLonRectangle2D, IntProcedure)
   */
  public void intersects(LatLonPolygon2D r, IntProcedure v) {
    Node rootNode = getNode(rootNodeId);
    intersects(r, v, rootNode);
  }

  /**
   * Convenience method for contains search where the IntProcedure is handled internally. Returns only those cells that
   * are completely
   * contained within the <code>r</code> polygon.
   *
   * @param r
   *        the bounding polygon to search within
   * @return a list of indexes completely contained within the bounding polygon
   */
  public IntArrayList contains(LatLonPolygon2D r) {
    IntProcedureEntriesList ip = new IntProcedureEntriesList();
    this.contains(r, ip);
    AbstractIntList res = ip.getValues();
    res.trimToSize();
    if (res.isEmpty()) {
      return new IntArrayList();
    }
    return (IntArrayList) res;
  }


  /**
   * @see com.asascience.data.rtree.SpatialIndex#contains(LatLonRectangle2D, IntProcedure)
   */
  public void contains(LatLonPolygon2D r, IntProcedure v) {
    // find all rectangles in the tree that are contained by the passed
    // rectangle
    // written to be non-recursive (should model other searches on this?)

    parents.clear();
    parents.push(rootNodeId);

    parentsEntry.clear();
    parentsEntry.push(-1);

    // TODO: possible shortcut here - could test for intersection with the
    // MBR of the root node. If no intersection, return immediately.

    while (parents.size() > 0) {
      Node n = getNode(parents.peek());
      int startIndex = parentsEntry.peek() + 1;

      if (!n.isLeaf()) {
        // go through every entry in the index node to check
        // if it intersects the passed rectangle. If so, it
        // could contain entries that are contained.
        boolean intersects = false;
        for (int i = startIndex; i < n.entryCount; i++) {
          if (r.intersects(n.entries[i])) {
            parents.push(n.ids[i]);
            parentsEntry.pop();
            parentsEntry.push(i); // this becomes the start index
            // when the child has been
            // searched
            parentsEntry.push(-1);
            intersects = true;
            break; // ie go to next iteration of while()
          }
        }
        if (intersects) {
          continue;
        }
      } else {
        // go through every entry in the leaf to check if
        // it is contained by the passed rectangle
        for (int i = 0; i < n.entryCount; i++) {
          if (r.contains(n.entries[i])) {
            v.execute(n.ids[i]);
          }
        }
      }
      parents.pop();
      parentsEntry.pop();
    }
  }



  /**
   * @see com.asascience.data.rtree.SpatialIndex#size()
   */
  public int size() {
    return size;
  }

  /**
   * @see com.asascience.data.rtree.SpatialIndex#getBounds()
   */
  public LatLonRectangle2D getBounds() {
    LatLonRectangle2D bounds = null;

    Node n = getNode(getRootNodeId());
    if (n != null && n.getMBR() != null) {
      bounds = n.getMBR().copy();
    }
    return bounds;
  }

  /**
   * @see com.asascience.data.rtree.SpatialIndex#getVersion()
   */
  public String getVersion() {
    return "RTree-" + VERSION;
  }

  /**
   * Get the tree height (a.k.a. # of levels)
   * 
   * @return the height of the tree
   */
  public int getTreeHeight() {
    return treeHeight;
  }

  // -------------------------------------------------------------------------
  // end of SpatialIndex methods
  // -------------------------------------------------------------------------

  /**
   * Get the next available node ID. Reuse deleted node IDs if possible
   */
  private int getNextNodeId() {
    int nextNodeId = 0;
    if (deletedNodeIds.size() > 0) {
      nextNodeId = deletedNodeIds.pop();
    } else {
      nextNodeId = ++highestUsedNodeId;
    }
    return nextNodeId;
  }

  /**
   * Get a node object, given the ID of the node.
   */
  public Node getNode(int index) {
    return (Node) nodeMap.get(index);
  }

  /**
   * @return Get the number of nodes in this RTree. This is not the total number of entries. Entry total is obtained via
   *         <code>size()</code>
   */
  public int getNodeCount() {
    return nodeMap.size();
  }

  /**
   * Get the highest used node's ID. This may not be the same number as size(), since node deletion does not reorient
   * the remaining nodes.
   */
  public int getHighestUsedNodeId() {
    return highestUsedNodeId;
  }

  /**
   * Get the root node's ID
   */
  public int getRootNodeId() {
    return rootNodeId;
  }

  public static long splitTime = 0;

  /**
   * Split a node. Algorithm is taken pretty much verbatim from Guttman's original paper.
   *
   * @return new node object.
   */
  private Node splitNode(Node n, LatLonPolygon2D newRect, int newId) {

    long curTime = System.currentTimeMillis();
    /**
     * QS1 [Pick first entry for each group]
     *
     * Apply algorithm pickSeeds to choose two entries to be the first elements of the groups. Assign each to a group.
     */

    // debug code
    // double initialArea = 0;
    // if (log.isDebugEnabled()) {
    // GeoRectangle.Double union = new GeoRectangle.Double();
    // GeoRectangle.union(n.mbr.getBoundsGeo(), newRect.getBoundsGeo(),
    // union);
    // initialArea = union.getArea();
    // }
    System.arraycopy(initialEntryStatus, 0, entryStatus, 0, maxNodeEntries);

    Node newNode = new Node(getNextNodeId(), n.level, maxNodeEntries);
    nodeMap.put(newNode.nodeId, newNode);

    /* this also sets the entryCount to 1 */
    pickSeeds(n, newRect, newId, newNode);

    /**
     * QS2 [Check if done]
     *
     * If all entries have been assigned, stop. If one group has so few entries that all the rest must be assigned to it
     * in order for it
     * to have the minimum number m, assign them and stop.
     */
    while (n.entryCount + newNode.entryCount < maxNodeEntries + 1) {
      if (maxNodeEntries + 1 - newNode.entryCount == minNodeEntries) {
        // assign all remaining entries to original node
        for (int i = 0; i < maxNodeEntries; i++) {
          if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {
            entryStatus[i] = ENTRY_STATUS_ASSIGNED;
            n.mbr = Node.expandMBR(n.mbr, n.entries[i].getBoundingLatLonValues());
            n.entryCount++;
          }
        }
        break;
      }
      if (maxNodeEntries + 1 - n.entryCount == minNodeEntries) {
        // assign all remaining entries to new node
        for (int i = 0; i < maxNodeEntries; i++) {
          if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {
            entryStatus[i] = ENTRY_STATUS_ASSIGNED;
            newNode.addEntryNoCopy(n.entries[i], n.ids[i]);
            n.entries[i] = null;
          }
        }
        break;
      }

      /**
       * QS3 [Select entry to assign]
       *
       * Invoke algorithm pickNext() to choose the next entry to assign. Add it to the group whose covering rectangle
       * will have to be
       * enlarged least to accommodate it. Resolve ties [equal area differences] by adding the entry to the group with
       * smaller area,
       * then to the the one with fewer entries, then to either. Repeat from S2
       */
      pickNextAndAssign(n, newNode);



    }

    n.reorganize(this);

    // check that the MBR stored for each node is correct.
    if (INTERNAL_CONSISTENCY_CHECKING) {
      if (!n.mbr.equals(calculateMBR(n))) {
        logger.error("Error: splitNode old node " + n.nodeId + " MBR wrong");
      }

      if (!newNode.mbr.equals(calculateMBR(newNode))) {
        logger.error("Error: splitNode new node " + newNode.nodeId + " MBR wrong");
      }
    }

    // debug code
    // if (logger.isDebugEnabled()) {
    // double newArea = n.getMBR().getArea() + newNode.getMBR().getArea();
    // double percentageIncrease = (100 * (newArea - initialArea)) /
    // initialArea;
    // logger.debug("Node " + n.nodeId + " split. New area increased by " +
    // percentageIncrease + "%");
    // }
    splitTime += (System.currentTimeMillis() - curTime);
    return newNode;
  }

  public static long pickSeedsTime = 0;

  /**
   * Pick the seeds used to split a node. Select two entries to be the first elements of the groups. These two entries
   * would be the ones
   * with the largest wasted space (greatest bounding rectangle) if they were in the same group. By using these two
   * entries as seeds, they
   * will not be placed in the same group.
   */
  private void pickSeeds(Node n, LatLonPolygon2D newRect, int newId, Node newNode) {
    long curTime = System.currentTimeMillis();
    /**
     * LPS1 [Find extreme rectangles along all dimensions]
     *
     * Along each dimension, find the entry whose rectangle has the highest low side, and the one with the lowest high
     * side. Record the
     * separation.
     *
     * [TPL] This has been modified from the Gutman RTree abstract such that separations along various dimensions are
     * not normalized
     * until the maximal separations are determined. Then from those separations the indices of the most extreme pairs
     * are determined as
     * if they were normalized by comparing the values unto which they would be normalized (the mbr's widths in each
     * dimension).
     *
     * Normalizing in this fashion removes some of the mathematics from being performed every iteration, thereby
     * optimizing rtee
     * insertion speed.
     */
    final int NUM_DIMENSIONS = 2;
    double[] maxSeparation = new double[NUM_DIMENSIONS];
    int[] highestLowIndex = new int[NUM_DIMENSIONS];
    int[] lowestHighIndex = new int[NUM_DIMENSIONS];
    final double[] newRectLatLons = newRect.getBoundingLatLonValues();


    /*
     * For the purposes of picking seeds, take the MBR of the node to
     * include the new rectangle as well.
     */
    n.mbr = Node.expandMBR(n.mbr, newRect.getBoundingLatLonValues());


    double[] mbrWidths = new double[2];
    /* lat */mbrWidths[0] = n.mbr.getHeight();
    /* lon */mbrWidths[1] = n.mbr.getWidth();


    /* [TPL] d = dimensions. For now, this is 2 */
    // for (int d = 0; d < Rectangle.DIMENSIONS; d++) {
    for (int d = 0; d < NUM_DIMENSIONS; d++) {
      /* -1 indicates the new rectangle (newRect) is the seed */
      int tempHighestLowIndex = -1;
      int tempLowestHighIndex = -1;
      double tempHighestLowValue = newRectLatLons[(0 + d)];
      double tempLowestHighValue = newRectLatLons[(2 + d)];

      for (int i = 0; i < n.entryCount; i++) {
        double[] entryLatLons = n.entries[i].getBoundingLatLonValues();
        double tempLow = entryLatLons[(0 + d)];
        if (tempLow >= tempHighestLowValue) {
          tempHighestLowValue = tempLow;
          tempHighestLowIndex = i;
        } else {
          /*
           * Ensure that the same index cannot be both lowestHigh and
           * highestLow
           */
          double tempHigh = entryLatLons[(2 + d)];
          if (tempHigh <= tempLowestHighValue) {
            tempLowestHighValue = tempHigh;
            tempLowestHighIndex = i;
          }
        }

        /**
         * LPS2 [Adjust for shape of the rectangle cluster]
         *
         * Normalize the separations by dividing by the widths of the entire set along the corresponding dimension
         */
        // double normalizedSeparation = Math.abs((tempHighestLowValue - tempLowestHighValue) / mbrWidths[d]);
        double separation = Math.abs(tempHighestLowValue - tempLowestHighValue);

        /*
         * Throw a warning if normalized separation doesn't
         * make sense mathematically...
         * (only during debugging because this may cause scrolling blindness)
         */
        // if (logger.isDebugEnabled() && normalizedSeparation > 1) {
        // logger.warn("Invalid normalized separation. Value: " + normalizedSeparation);
        // }

        // if (log.isDebugEnabled()) {
        // log.debug("Entry " + i + ", dimension " + d +
        // ": HighestLow = " + tempHighestLow + " (index "
        // + tempHighestLowIndex + ")" + ", LowestHigh = " +
        // tempLowestHigh + " (index "
        // + tempLowestHighIndex + ", NormalizedSeparation = " +
        // normalizedSeparation);
        // }

        /**
         * LPS3 [Select the most extreme pair]
         *
         * Choose the pair with the greatest normalized separation along any dimension.
         */
        if (separation > maxSeparation[d]) {
          maxSeparation[d] = separation;
          highestLowIndex[d] = (tempHighestLowIndex >= 0) ? tempHighestLowIndex : highestLowIndex[d];
          lowestHighIndex[d] = (tempLowestHighIndex >= 0) ? tempLowestHighIndex : lowestHighIndex[d];
        }
      }
    }

    /** Normalize the separations to determine the index with the largest one */
    int mostExtremeDim = 0;
    double maxNormalizedSeparation = -1;
    double tempNormalizedSeparation;
    for (int ctr = 0; ctr < NUM_DIMENSIONS; ctr++) {
      tempNormalizedSeparation = maxSeparation[ctr] / mbrWidths[ctr];
      if (tempNormalizedSeparation > maxNormalizedSeparation) {
        maxNormalizedSeparation = tempNormalizedSeparation;
        mostExtremeDim = ctr;
      }
    }



    /* highestLowIndex is the seed for the new node. */
    if (highestLowIndex[mostExtremeDim] == -1) {
      newNode.addEntryCopy(newRect, newId);
    } else {
      newNode.addEntryNoCopy(n.entries[highestLowIndex[mostExtremeDim]], n.ids[highestLowIndex[mostExtremeDim]]);
      n.entries[highestLowIndex[mostExtremeDim]] = null;

      // move the new rectangle into the space vacated by the seed for the
      // new node
      n.entries[highestLowIndex[mostExtremeDim]] = newRect;
      n.ids[highestLowIndex[mostExtremeDim]] = newId;
    }

    // lowestHighIndex is the seed for the original node.
    if (lowestHighIndex[mostExtremeDim] == -1) {
      lowestHighIndex[mostExtremeDim] = highestLowIndex[mostExtremeDim];
    }

    entryStatus[lowestHighIndex[mostExtremeDim]] = ENTRY_STATUS_ASSIGNED;
    n.entryCount = 1;
    /* Set the nodes mbr to the lowest high index's mbr [TPL] */
    n.mbr = n.entries[lowestHighIndex[mostExtremeDim]].getBouningLatLonRectangle2D();

    pickSeedsTime += (System.currentTimeMillis() - curTime);

  }

  public static long pickNextTime = 0;

  // /**
  // * Pick the next entry to be assigned to a group during a node split.
  // *
  // * [Determine cost of putting each entry in each group] For each entry not yet in a group, calculate the area
  // increase required in the
  // * covering rectangles of each group
  // */
  // private int pickNextAndAssign(Node n, Node newNode) {
  // long curTime = System.currentTimeMillis();
  // double maxDifference = Double.NEGATIVE_INFINITY;
  // int next = 0;
  // int nextGroup = 0;
  //
  // maxDifference = Double.NEGATIVE_INFINITY;
  //
  // // if (log.isDebugEnabled()) {
  // // log.debug("pickNext()");
  // // }
  //
  // for (int i = 0; i < maxNodeEntries; i++) {
  // if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {
  //
  // // if (n.entries[i] == null) {
  // // log.error("Error: Node " + n.nodeId + ", entry " + i +
  // // " is null");
  // // }
  //
  // double nIncrease = getEnlargementArea(n.mbr, n.entries[i].getBouningLatLonRectangle2D());
  // double newNodeIncrease = getEnlargementArea(newNode.mbr, n.entries[i].getBouningLatLonRectangle2D());
  // double difference = Math.abs(nIncrease - newNodeIncrease);
  //
  // if (difference > maxDifference) {
  // next = i;
  //
  // if (nIncrease < newNodeIncrease) {
  // nextGroup = 0;
  // } else if (newNodeIncrease < nIncrease) {
  // nextGroup = 1;
  // } else if (n.mbr.getArea() < newNode.mbr.getArea()) {
  // nextGroup = 0;
  // } else if (newNode.mbr.getArea() < n.mbr.getArea()) {
  // nextGroup = 1;
  // } else if (newNode.entryCount < maxNodeEntries / 2) {
  // nextGroup = 0;
  // } else {
  // nextGroup = 1;
  // }
  // maxDifference = difference;
  // }
  // // if (log.isDebugEnabled()) {
  // // log.debug("Entry " + i + " group0 increase = " + nIncrease +
  // // ", group1 increase = "
  // // + newNodeIncrease + ", diff = " + difference + ", MaxDiff = "
  // // + maxDifference + " (entry "
  // // + next + ")");
  // // }
  // }
  // }
  //
  // entryStatus[next] = ENTRY_STATUS_ASSIGNED;
  //
  // if (nextGroup == 0) {
  // n.mbr = Node.expandMBR(n.mbr, n.entries[next].getBoundingLatLonValues());
  // n.entryCount++;
  // } else {
  // // move to new node.
  // newNode.addEntryNoCopy(n.entries[next], n.ids[next]);
  // n.entries[next] = null;
  // }
  //
  // pickNextTime += (System.currentTimeMillis() - curTime);
  // return next;
  // }


  private int pickNextAndAssign(Node oldNode, Node newNode) {

    long curTime = System.currentTimeMillis();


    /** Grab the next unassigned entry */
    int nextEntry = -1;
    for (int i = 0; i < maxNodeEntries; i++) {
      if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {
        nextEntry = i;
        break;
      }
    }



    /** No more entries to assign.. return */
    if (-1 == nextEntry) {
      return -1;
    }



    /** Determine which node needs the least expansion (area) */
    double[] node1Values = oldNode.mbr.getBoundingLatLonValues();
    double[] node2Values = newNode.mbr.getBoundingLatLonValues();

    double node1EnlargeArea = getEnlargementArea(node1Values, oldNode.entries[nextEntry].getBoundingLatLonValues());
    double node2EnlargeArea = getEnlargementArea(node2Values, oldNode.entries[nextEntry].getBoundingLatLonValues());



    /** Determine which node should accept the next entry */
    boolean addEntryToOldNode = false;

    if (node1EnlargeArea < node2EnlargeArea) {
      addEntryToOldNode = true;
    } else if (node2EnlargeArea < node1EnlargeArea) {
      addEntryToOldNode = false;
    } else { /* enlargement areas are equal */

      double node1Area = LatLonPolygon2D.Double.calculateLLArrayArea(node1Values);
      double node2Area = LatLonPolygon2D.Double.calculateLLArrayArea(node2Values);

      if (node1Area < node2Area) {
        addEntryToOldNode = true;
      } else if (node2Area < node1Area) {
        addEntryToOldNode = false;
      } else if (oldNode.entryCount < newNode.entryCount) {
        addEntryToOldNode = true;
      }

    }


    entryStatus[nextEntry] = ENTRY_STATUS_ASSIGNED;


    if (addEntryToOldNode) {
      /* Mark entry as belonging to oldNode */
      oldNode.mbr = Node.expandMBR(oldNode.mbr, oldNode.entries[nextEntry].getBoundingLatLonValues());
      oldNode.entryCount++;
    } else {
      /* Add Entry to the new node */
      newNode.addEntryNoCopy(oldNode.entries[nextEntry], oldNode.ids[nextEntry]);
      oldNode.entries[nextEntry] = null;
    }


    pickNextTime += (System.currentTimeMillis() - curTime);
    return nextEntry;
  }



  /**
   * Finds the entries which are within the circular search area given by the point <code>p</code> and the radius
   * <code>searchRadius</code>.Next invoke apply() on the given <code>IntProcedure</code> for each entry found.
   *
   * @param p
   *        The point for which this method finds the nearest neighbors
   * @param v
   *        The IntProcedure whose execute() method is is called for each nearest neighbor.
   *
   * @param searchRadius
   *        The distance from <code>p</code> to look for entries
   *
   */
  public void nearestNeighbors(LatLonPoint2D p, IntProcedure v, double searchRadius) {
    Node rootNode = getNode(rootNodeId);

    nearestIds.clear();
    nearestNeighbors(p, rootNode, searchRadius * searchRadius);

    visitProc.setProcedure(v);
    nearestIds.forEach(visitProc);
    nearestIds.clear();
  }


  // inclusive
  private void nearestNeighbors(LatLonPoint2D p, Node n, double searchRadiusSquared) {
    for (int i = 0; i < n.entryCount; i++) {
      double tempDistance = n.entries[i].distanceSq(p);


      if (tempDistance <= searchRadiusSquared) {

        if (n.isLeaf()) {
          /* Add this node to the list */
          nearestIds.add(n.ids[i]);
        } else {
          /* Search this nodes children */
          nearestNeighbors(p, getNode(n.ids[i]), searchRadiusSquared);
        }

      }

    }
  }


  /**
   * Recursively searches the tree for the nearest entry. Other queries call execute() on an IntProcedure when a
   * matching entry is found;
   * however nearest() must store the entry Ids as it searches the tree, in case a nearer entry is found. Uses the
   * member variable
   * nearestIds to store the nearest entry IDs. Since some nearest entries may be equiquadistant to the given point, the
   * entries polygon
   * is cached in the variable nearestEntriesMap so that the calling method may (if needed) sort the entries further by
   * centroid or other
   * unique characteristic.
   *
   * [x] TODO rewrite this to be non-recursive?
   */
  private double nearestNeighborsExclusive(LatLonPoint2D p, Node n, double searchRadius) {
    for (int i = 0; i < n.entryCount; i++) {
      double tempDistance = n.entries[i].distance(p);
      if (n.isLeaf()) {
        /*
         * [TPL] When checking leaf nodes, the tempDistance is the
         * actual distance to the point p. Otherwise tempDistance is
         * just a distance used in adjusting the search radius. Breaking
         * down into the following two 'if' statements allows for the two
         * occasions where: 1) This node is closer than all the rest.
         * This way the nearestIds must be cleared, and since nearest =
         * temp, this node will be the only one added to the nearestIds
         * list. 2) This node is an equal distance from the point as
         * other nodes, so it should simply be added to nearestIds.
         */
        if (tempDistance < searchRadius) {
          searchRadius = tempDistance;
          nearestEntriesMap.clear();
          nearestIds.clear();
        }
        if (tempDistance <= searchRadius) {
          nearestEntriesMap.put(n.ids[i], n.entries[i]);
          nearestIds.add(n.ids[i]);
        }
      } else {
        /*
         * [TPL] EACH parent node within range ( distance < nearest )
         * could possibly have a closer node because of overlapping.
         * Therefore, each parent node within range must be traversed,
         * instead of traversing the tree at the closest parent. This is
         * the reason why minimizing your nearestDistance variable will
         * decrease query time.
         */
        if (tempDistance <= searchRadius) {
          /* Search the child node */
          searchRadius = nearestNeighborsExclusive(p, getNode(n.ids[i]), searchRadius);
        }
      }
    }
    return searchRadius;
  }



  /**
   * Recursively searches the tree for all intersecting entries. Immediately calls execute() on the passed IntProcedure
   * when a matching
   * entry is found.
   *
   * NOTE: this method always returns false for polygons with 1 vertex (points) TODO: fix the above issue
   *
   * [x] TODO rewrite this to be non-recursive? Make sure it doesn't slow it down.
   */
  private void intersects(LatLonPolygon2D r, IntProcedure v, Node n) {
    for (int i = 0; i < n.entryCount; i++) {

      if (r.intersects(n.entries[i])) {
        if (n.isLeaf()) {
          v.execute(n.ids[i]);
        } else {
          intersects(r, v, getNode(n.ids[i]));
        }
      }
    }
  }



  /**
   * Used by delete(). Ensures that all nodes from the passed node up to the root have the minimum number of entries.
   *
   * Note that the parent and parentEntry stacks are expected to contain the nodeIds of all parents up to the root.
   */
  private LatLonPolygon2D oldRectangle = new LatLonPolygon2D.Double(0, 0);

  private void condenseTree(Node l) {
    // CT1 [Initialize] Set n=l. Set the list of eliminated
    // nodes to be empty.
    Node n = l;
    Node parent = null;
    int parentEntry = 0;

    Stack<Integer> eliminatedNodeIds = new Stack<Integer>();

    // CT2 [Find parent entry] If N is the root, go to CT6. Otherwise
    // let P be the parent of N, and let En be N's entry in P
    while (n.level != treeHeight) {
      parent = getNode(parents.pop());
      parentEntry = parentsEntry.pop();

      // CT3 [Eliminiate under-full node] If N has too few entries,
      // delete En from P and add N to the list of eliminated nodes
      if (n.entryCount < minNodeEntries) {
        parent.deleteEntry(parentEntry, minNodeEntries);
        eliminatedNodeIds.push(n.nodeId);
      } else {
        // CT4 [Adjust covering rectangle] If N has not been eliminated,
        // adjust EnI to tightly contain all entries in N
        if (!n.mbr.equals(parent.entries[parentEntry])) {
          oldRectangle = parent.entries[parentEntry].copy();
          parent.entries[parentEntry] = new LatLonPolygon2D.Double(n.mbr);
          // oldRectangle.set(parent.entries[parentEntry].min,
          // parent.entries[parentEntry].max);
          // parent.entries[parentEntry].set(n.mbr.min, n.mbr.max);


          parent.recalculateMBR(oldRectangle);
        }
      }
      // CT5 [Move up one level in tree] Set N=P and repeat from CT2
      n = parent;
    }

    // CT6 [Reinsert orphaned entries] Reinsert all entries of nodes in set
    // Q.
    // Entries from eliminated leaf nodes are reinserted in tree leaves as
    // in
    // Insert(), but entries from higher level nodes must be placed higher
    // in
    // the tree, so that leaves of their dependent subtrees will be on the
    // same
    // level as leaves of the main tree
    while (eliminatedNodeIds.size() > 0) {
      Node e = getNode(eliminatedNodeIds.pop());
      for (int j = 0; j < e.entryCount; j++) {
        add(e.entries[j], e.ids[j], e.level);
        e.entries[j] = null;
      }
      e.entryCount = 0;
      deletedNodeIds.push(e.nodeId);
    }
  }

  public static long chooseTime = 0;

  /**
   * Used by add(). Chooses a leaf to add the rectangle to.
   */
  private Node chooseNode(LatLonPolygon2D r, int level) {
    long curTime = System.currentTimeMillis();
    // CL1 [Initialize] Set N to be the root node
    Node n = getNode(rootNodeId);
    parents.clear();
    parentsEntry.clear();

    // CL2 [Leaf check] If N is a leaf, return N
    while (n.level != level) {
      // if (n == null) {
      // log.error("Could not get root node (" + rootNodeId + ")");
      // }

      // if (n.level == level) {
      // chooseTime += (System.currentTimeMillis() - curTime);
      // return n;
      // }

      // CL3 [Choose subtree] If N is not at the desired level, let F be
      // the entry in N
      // whose rectangle FI needs least enlargement to include EI. Resolve
      // ties by choosing the entry with the rectangle of smaller area.

      int index = 0; // index of rectangle in subtree
      double[] rectBounds = r.getBoundingLatLonValues();
      double leastEnlargement = getEnlargementArea(n.getEntry(0).getBoundingLatLonValues(), rectBounds);

      LatLonPolygon2D tempPoly = null;
      double tempEnlargement = 0.0;

      for (int i = 1; i < n.entryCount; i++) {
        tempPoly = n.getEntry(i);
        tempEnlargement = getEnlargementArea(tempPoly.getBoundingLatLonValues(), rectBounds);
        if ((tempEnlargement < leastEnlargement) || ((tempEnlargement == leastEnlargement) && (tempPoly
            .getBouningLatLonRectangle2D().getArea() < n.getEntry(index).getBouningLatLonRectangle2D().getArea()))) {
          index = i;
          leastEnlargement = tempEnlargement;
        }
      }

      parents.push(n.nodeId);
      parentsEntry.push(index);

      // CL4 [Descend until a leaf is reached] Set N to be the child node
      // pointed to by Fp and repeat from CL2
      n = getNode(n.ids[index]);
    }

    chooseTime += (System.currentTimeMillis() - curTime);
    return n;
  }

  public static long adjustTime = 0;

  /**
   * Ascend from a leaf node L to the root, adjusting covering rectangles and propagating node splits as necessary.
   */
  private Node adjustTree(Node n, Node nn) {
    long curtime = System.currentTimeMillis();


    /*
     * AT1 [Initialize]
     * Set N=L. If L was split previously, set NN to be the resulting second node.
     */

    /* AT2 [Check if done] If N is the root, stop */
    while (n.level != treeHeight) {

      /*
       * AT3 [Adjust covering rectangle in parent entry]
       * Let P be the parent node of N, and let En be N's entry in P.
       * Adjust EnI so that it tightly encloses all entry rectangles in N.
       */
      Node parent = getNode(parents.pop());
      int entry = parentsEntry.pop();

      // if (parent.ids[entry] != n.nodeId) {
      // log.error("Error: entry " + entry + " in node " +
      // parent.nodeId + " should point to node " + n.nodeId
      // + "; actually points to node " + parent.ids[entry]);
      // }

      /* If n changed (the entry in parent doesnt match) ... */
      if (!parent.entries[entry].getBouningLatLonRectangle2D().equals(n.mbr)) {

        /* Update the parents entry with the nodes new MBR */
        parent.entries[entry] = new LatLonPolygon2D.Double(n.mbr);

        /* Expand the parent to include the new MBR */
        Node.expandMBR(parent.mbr, parent.entries[entry].getBoundingLatLonValues());


        // parent.mbr = parent.entries[0].getBouningLatLonRectangle2D();
        // for (int i = 1; i < parent.entryCount; i++) {
        // parent.mbr = Node.expandMBR(parent.mbr, parent.entries[i].getBoundingLatLonValues());
        // }

      }

      /*
       * AT4 [Propagate node split upward]
       * If N has a partner NN resulting from an earlier split, create a new entry Enn with Ennp
       * pointing to NN and Enni enclosing all rectangles in NN. Add Enn to P if there is room.
       * Otherwise, invoke splitNode to produce P and PP containing Enn and all P's old entries.
       */
      Node newNode = null;
      if (nn != null) {
        if (parent.entryCount < maxNodeEntries) {
          parent.addEntryCopy(nn.mbr, nn.nodeId);
        } else {
          newNode = splitNode(parent, new LatLonPolygon2D.Double(nn.mbr), nn.nodeId);
        }
      }

      // AT5 [Move up to next level] Set N = P and set NN = PP if a split
      // occurred. Repeat from AT2
      n = parent;
      nn = newNode;

      // parent = null;
      // newNode = null;
    }
    adjustTime += (System.currentTimeMillis() - curtime);
    return nn;
  }

  /**
   * Check the consistency of the tree.
   */
  private void checkConsistency(int nodeId, int expectedLevel, LatLonPolygon2D expectedMBR) {
    /* Go through the tree, and check that the internal data structures of the tree are not corrupted. */
    Node n = getNode(nodeId);



    if (n == null) {
      logger.error("Error: Could not read node " + nodeId + " of " + getNodeCount() + " at level: " + expectedLevel);
    }

    if (n.level != expectedLevel) {
      logger.error("Error: Node " + nodeId + " of " + getNodeCount() + ", expected level " + expectedLevel
          + ", actual level " + n.level);
    }

    LatLonRectangle2D calculatedMBR = calculateMBR(n);

    if (!n.mbr.equals(calculatedMBR)) {
      logger.error("Error: Node: " + nodeId + " of " + getNodeCount() + " at level: " + expectedLevel
          + ", calculated MBR does not equal stored MBR");
    }

    if (expectedMBR != null && !n.mbr.equals(expectedMBR.getBouningLatLonRectangle2D())) {
      logger.error("Error: Node " + nodeId + " of " + getNodeCount() + " at level: " + expectedLevel
          + ", expected MBR (from parent) does not equal stored MBR");
    }

    /* Check for corruption where a parent entry is the same object as the child MBR */
    /* It is ok if these objects have the same bounds, however */
    if ((expectedMBR != null) && ((Object) n.mbr == (Object) expectedMBR)) {
      logger.error("Error: Node " + nodeId + " of " + getNodeCount() + " at level: " + expectedLevel
          + " MBR using same rectangle object as parent's entry");
    }

    /* Recursively check all children of this node */
    for (int i = 0; i < n.entryCount; i++) {
      if (n.entries[i] == null) {
        logger.error("Error: Node " + nodeId + " of " + getNodeCount() + " at level: " + expectedLevel + ", Entry " + i
            + " is null");
      }

      if (n.level > 1) { /* if not a leaf */
        checkConsistency(n.ids[i], n.level - 1, n.entries[i]);
      }
    }
  }



  /**
   * Given a node object, calculate the node MBR from it's entries. Used in consistency checking
   */
  private LatLonRectangle2D calculateMBR(Node n) {
    LatLonRectangle2D mbr = n.entries[0].getBouningLatLonRectangle2D();

    for (int i = 1; i < n.entryCount; i++) {
      mbr = Node.expandMBR(mbr, n.entries[i].getBoundingLatLonValues());
    }
    return mbr;
  }


  /**
   * Calculates the area of the given rectangle
   *
   * @param rect
   *        a double rectangle from which an area will be calculated
   *
   * @return a <code>double</code> value
   */
  /*
   * private double getArea(GeoRectangle.Double rect) {
   * return (rect.getUpperRight().getX() - rect.getLowerLeft().getX()) *
   * (rect.getUpperRight().getY() - rect.getLowerLeft().getY());
   * }
   */



  /**
   * Builds a 2 length double array containing the minimum x and y values from the given rectangle. The 0 index of this
   * array contains the
   * minimum x value, and the 1 index contains the minimum y value.
   *
   * @param rect
   *        a LatLonRectangle2D object
   *
   * @return a 2 length <code>double</code> array, containing x and y coordinates.
   */
  public static double[] getRectMin(LatLonRectangle2D rect) {
    /* TODO: remove this method and change the way min/max is accessed in above methods */
    double[] result = new double[2];

    result[0] = rect.getLonMin();
    result[1] = rect.getLatMin();


    return result;
  }


  /**
   * Builds a 2 length double array containing the maximum x and y values from the given rectangle. The 0 index of this
   * array contains the
   * max x value, and the 1 index contains the max y value.
   *
   * @param rect
   *        a LatLonRectangle2D object
   *
   * @return a 2 length <code>double</code> array, containing x and y coordinates.
   */
  public static double[] getRectMax(LatLonRectangle2D rect) {
    /* TODO: remove this method and change the way min/max is accessed in above methods */
    double[] result = new double[2];

    result[0] = rect.getLonMax();
    result[1] = rect.getLatMax();


    return result;
  }



  /**
   * Produces a union of the two rectangles, and determines how much <code>src</code> would have to be enlarged to
   * encompass this union.
   * The original source rectangles will remain unchanged.<br />
   * <br />
   * If <code>src</code> fully contains <code>addition</code> this will always return 0, since <code>src</code> would
   * not have to be
   * enlarged.
   *
   * @param src
   *        a source rectangle
   * @param addition
   *        the rectangle which will be added to the source
   *
   * @return the area which <code>src1</code> would have to be enlarged to, in order to encompass the union of itself
   *         and another
   *         rectangle ( <code>src2</code>)
   */
  public static double getEnlargementArea(final LatLonRectangle2D src, final LatLonRectangle2D addition) {
    /*
     * TODO changing this to getEnlargementArea(poly, poly) and using the min and max extents of each object
     * to determine the enlargement area may be faster than requiring a LatLonRectangle2D since all objects
     * are originally Polygon2D and each node must be iterated to create an equal LatLonRectangle2D.
     * That is... each node of each object passed is checked twice unnecessarily
     */
    double result = 0.0;


    if (!src.contains(addition)) {
      LatLonRectangle2D union = src.copy();
      union.extend(addition);
      double srcArea = src.getArea();
      double unionArea = union.getArea();

      result = unionArea - srcArea;
    }

    /* the following should always return false */
    if (result < 0) {
      result = 0;
    }


    return result;


    // double[] srcVals = src.getBoundingLatLonValues();
    // double[] addVals = addition.getBoundingLatLonValues();
    //
    // return getEnlargementArea(srcVals, addVals);
  }



  /**
   * Produces a union of the two rectangles, and determines how much <code>src</code> would have to be enlarged to
   * encompass this union.
   * The original source rectangles will remain unchanged.<br />
   * <br />
   * If <code>src</code> fully contains <code>addition</code> this will always return 0, since <code>src</code> would
   * not have to be
   * enlarged.
   *
   * @param src
   *        a source rectangle
   * @param addition
   *        the rectangle which will be added to the source
   *
   * @return the area which <code>src1</code> would have to be enlarged to, in order to encompass the union of itself
   *         and another
   *         rectangle ( <code>src2</code>)
   */
  public static double getEnlargementArea(final double[] src, final double[] addition) {

    return getEnlargementArea(new LatLonRectangle2D(src[0], src[1], src[2], src[3]),
        new LatLonRectangle2D(addition[0], addition[1], addition[2], addition[3]));

    // /** Create the encompasing rectangle */
    // double total[] = new double[4];
    // total[0] = Math.min(src[0], addition[0]);
    // total[1] = Math.min(src[1], addition[1]);
    // total[2] = Math.max(src[2], addition[2]);
    // total[3] = Math.max(src[3], addition[3]);
    //
    //
    //
    // /** Check if src contains addition (total == src) */
    // boolean contains = true;
    // for (int ctr = 0; ctr < 4; ctr++) {
    // if (src[ctr] != total[ctr]) {
    // contains = false;
    // break;
    // }
    // }
    // if (contains) {
    // return 0;
    // }
    //
    //
    //
    // /** Calculate the areas */
    // double oldArea = LatLonPolygon2D.Double.calculateLLArrayArea(src);
    // double newArea = LatLonPolygon2D.Double.calculateLLArrayArea(total);
    //
    //
    //
    // /** Return the difference in area */
    // assert (newArea >= oldArea);
    // return newArea - oldArea;
  }



  /**
   * Constructs a string representation of this <code>RTree</code> as a tab-indented node list. If this
   * <code>RTree</code> contains many
   * entries this string may be rather long.
   *
   * @returns a hierarchical <code>String</code> representation of this <code>RTree</code>
   */
  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " {entries:" + this.size + ", nodes:" + this.nodeMap.size()
        + ", tree-height:" + this.treeHeight + ", max-node-entries:" + this.maxNodeEntries + ", min-node-entries:"
        + this.minNodeEntries + "}";
  }



  /**
   * Recursively builds a string representation of the given rtree starting at nodeId. Starting at node id
   * rtree.getRootNodeId() is
   * recommended.
   *
   * @param rtree
   *        the rtree to traverse
   * @param nodeId
   *        the starting node for the tree building
   *
   * @return a string representation of the given rtree
   */
  public static StringBuilder getNodeMap(RTree rtree, int nodeId) {
    StringBuilder result = new StringBuilder();
    Node n = rtree.getNode(nodeId);
    int numIndents = rtree.getNode(rtree.getRootNodeId()).getLevel() - n.getLevel();

    /* If this is NOT a leaf node */
    if (n.getLevel() > 1) {

      result.append(repeatString("    ", numIndents));
      result.append("Node: ").append(nodeId).append("\n");

      /* Recursively build the string tree */
      for (int ctr = 0; ctr < n.getEntryCount(); ctr++) {
        result.append(getNodeMap(rtree, n.getId(ctr)));
      }

    } else {

      /* This node is a leaf */
      result.append(repeatString("    ", numIndents));
      result.append("Leaf: ").append(nodeId).append("\n");

      /* Display all child entries */
      for (int ctr = 0; ctr < n.getEntryCount(); ctr++) {
        result.append(repeatString("    ", numIndents + 1));
        result.append("Entry: ").append(n.getId(ctr)).append("\n");
      }

    }


    return result;
  }

  public static void writeFullNodeMap(java.io.BufferedWriter writer, RTree rtree, int nodeId) {
    if (writer != null) {
      try {
        Node n = rtree.getNode(nodeId);
        int numIndents = rtree.getNode(rtree.getRootNodeId()).getLevel() - n.getLevel();

        /* If this is NOT a leaf node */
        if (n.getLevel() > 1) {

          writer.write(repeatString("    ", numIndents));
          writer.write("Node {" + nodeId + "}: " + n.getMBR());
          writer.newLine();

          /* Recursively build the string tree */
          for (int ctr = 0; ctr < n.getEntryCount(); ctr++) {
            writeFullNodeMap(writer, rtree, n.getId(ctr));
          }

        } else {

          /* This node is a leaf */
          writer.write(repeatString("    ", numIndents));
          writer.write("Leaf {" + nodeId + "}: " + n.getMBR());
          writer.newLine();

          /* Display all child entries */
          for (int ctr = 0; ctr < n.getEntryCount(); ctr++) {
            writer.write(repeatString("    ", numIndents + 1));
            writer.write("Entry {" + n.getId(ctr) + "}: " + n.getEntry(ctr));
            writer.newLine();
          }

        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }



  /**
   * Creates a string with <code>s</code> repeated <code>n</code> times
   *
   * @param s
   *        a string
   * @param n
   *        an int
   *
   * @return a string with <code>s</code> repeated <code>n</code> times
   */
  private static String repeatString(String s, int n) {
    /* TODO: move this method to asa utilities library */
    StringBuilder result = new StringBuilder();

    for (int ctr = 0; ctr < n; ctr++) {
      result.append(s);
    }

    return result.toString();
  }


}
