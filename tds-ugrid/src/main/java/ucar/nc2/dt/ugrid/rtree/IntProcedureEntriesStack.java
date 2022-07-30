/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * IntProcedureEntriesStack.java
 *
 * Created on 06-01-09
 */

package ucar.nc2.dt.ugrid.rtree;


import java.util.ArrayList;
import java.util.List;



/**
 * The <code>IntProcedureEntriesStack</code> class provides an implementation of an <code>IntProcedure</code> and
 * records node id's from its
 * execution in a stack.<br />
 * <br />
 * Simply pass an instance of this class to an rtree search and access the retrieved node indices with {@link #peek()},
 * {@link #peek(int)},
 * and {@link #pop()}. These indices are returned as <code>Integer</code> objects.
 * 
 * 
 * @author tlarocque
 */
public class IntProcedureEntriesStack implements IntProcedure {
  /*
   * FIXME: this class should be backed by a stack instead of a list.
   * try using the colt libraries primitive int stack
   */
  private List<Integer> indices;



  /**
   * Initializes an empty <code>IntProcedureEntriesStack</code>.
   */
  public IntProcedureEntriesStack() {
    indices = new ArrayList<Integer>();
  }



  /**
   * Caches the id passed from the calling method to the stack. This method is utilized primarily for the purpose of
   * rtree searches and is
   * typically not invoked explicitly
   * 
   * @param id
   *        a node id (from an RTree)
   * 
   * @return <code>true</code> if execution succeeds, otherwise <code>false</code>
   */
  public boolean execute(int id) {
    return this.indices.add((Integer) id);
  }



  /**
   * Removes the <code>Integer</code> value from the top of this stack
   * 
   * @return the value removed from the top of this stack or <code>null</code> if there are no values on the stack
   */
  public Integer pop() {

    if (indices.size() <= 0) {
      return null;
    }

    return indices.remove(indices.size() - 1);
  }



  /**
   * Removes all items from this stack. Use {@link #clear()} instead, if the values returned by this methods invokation
   * are not going to
   * be used.
   * 
   * @return a copy of the values removed from this stack in an <code>Integer</code> list
   */
  @SuppressWarnings("unchecked")
  public List<Integer> popAll() {
    /*
     * Create a clone of the list...
     * List does not have clone method so we must cast to ArrayList
     */
    List<Integer> result = (List<Integer>) ((ArrayList<Integer>) indices).clone();
    clear();

    return result;
  }



  /**
   * Pushes the given value to the top of this stack
   * 
   * @param value
   *        an <code>Integer</code>
   */
  public void push(int value) {
    indices.add((Integer) value);
  }



  /**
   * Retrieves the value of the top-most item on this stack
   * 
   * @return an <code>Integer</code> value, or <code>null</code> if no values have been pushed to this stack
   * 
   */
  public Integer peek() {
    Integer retval;

    try {
      retval = indices.get(indices.size() - 1);
    } catch (IndexOutOfBoundsException e) {
      retval = null;
    }


    return retval;

  }



  /**
   * Retrieves the value of the top-most item on this stack
   * 
   * @param indices
   *        an integer indices
   * 
   * @return an <code>Integer</code> value
   * 
   * @throws IndexOutOfBoundsException
   *         if the indices is out of range (indices < 0 || indices >= size())
   */
  public Integer peek(int index) throws IndexOutOfBoundsException {
    return this.indices.get(index);
  }



  /**
   * Retrieves an <code>Integer</code> list of all items on this stack
   * 
   * @return a copy of the internal <code>Integer</code> list
   */
  @SuppressWarnings("unchecked")
  public List<Integer> peekAll() {
    return (List<Integer>) ((ArrayList<Integer>) indices).clone();
  }



  /**
   * Returns the number of elements in this stack. If this stack contains more than Integer.MAX_VALUE elements, returns
   * Integer.MAX_VALUE.
   * 
   * @return the number of elements in this stack.
   */
  public int size() {
    return indices.size();
  }



  /**
   * Removes all of the elements from this stack (optional operation). This list will be empty after this call returns
   * (unless it throws
   * an exception).
   * 
   * @return <code>true</code> on success; otherwise <code>false</code>
   */
  public boolean clear() {
    boolean result = true;

    try {
      indices.clear();
    } catch (UnsupportedOperationException e) {
      result = false;
    }


    return result;
  }


  /**
   * Builds a dump of the indexed values within this stack
   * 
   * @return a <code>String</code>
   */
  public String dump() {
    StringBuilder sb = new StringBuilder();

    sb.append("Data within this stack includes:\n");
    sb.append("-------------------------------------------------------------\n");
    sb.append("Item Count = " + this.size());

    for (int ctr = 0; ctr < indices.size(); ctr++) {
      sb.append("\n  - Index (" + ctr + "), Value (" + indices.get(ctr) + ")");
    }
    sb.append("\n-------------------------------------------------------------");



    return sb.toString();
  }



}
