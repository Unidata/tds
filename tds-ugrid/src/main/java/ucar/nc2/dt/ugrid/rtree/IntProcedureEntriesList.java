/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * IntProcedureIntList.java
 *
 * Created on Jun 2, 2009 @ 3:56:03 PM
 */

package ucar.nc2.dt.ugrid.rtree;


import cern.colt.list.AbstractIntList;
import cern.colt.list.IntArrayList;


/**
 * The <code>IntProcedureEntriesList</code> class provides an implementation of an <code>IntProcedure</code> and records
 * node id's from its
 * execution in a list.<br />
 * <br />
 * Simply pass an instance of this class to an rtree search and access the retrieved nodes' indices with
 * {@link #get(int)} and
 * {@link #getValues())}. These indices are returned as <code>Integer</code> objects.
 * 
 * 
 * @author TPL <tplarocque@asascience.com>
 */
public class IntProcedureEntriesList implements IntProcedure {

  private IntArrayList entryList;


  /**
   * Initializes an empty instance of a <code>IntProcedureEntriesList</code>
   */
  public IntProcedureEntriesList() {
    entryList = new IntArrayList();
  }



  /**
   * Caches the id passed from the calling method to the list. This method is utilized primarily for the purpose of
   * rtree searches and is
   * usually not invoked explicitly
   * 
   * @param id
   *        a node id (from an RTree)
   * 
   * @return <code>true</code> if execution succeeds. Execution may be halted by returning <code>false</code>.
   */
  public boolean execute(int id) {
    entryList.add(id);
    return true;
  }



  /**
   * Removes all elements from the internal list. The list will be empty after this call returns, but will maintain its
   * current capacity.
   */
  public void clear() {
    entryList.clear();
  }



  /**
   * Returns the value of the element at the specified position in the internal list.
   * 
   * @param index
   *        the index of the element to return
   * 
   * @return the <code>int</code> value of the entry at the given index
   * 
   * @throws IndexOutOfBoundsException
   *         if index is out of range (index < 0 || index >= size())
   */
  public int get(int index) throws IndexOutOfBoundsException {
    return entryList.get(index);
  }



  /**
   * Returns the number of elements contained in this object's internal list.
   * 
   * @return the number of elements contained in this object's internal list.
   */
  public int getSize() {
    return entryList.size();
  }



  /**
   * Retrieves a deep copy of the internal entry list
   * 
   * @return an <code>AbstractIntList</code>
   */
  public AbstractIntList getValues() {
    return entryList.copy();
  }
}
