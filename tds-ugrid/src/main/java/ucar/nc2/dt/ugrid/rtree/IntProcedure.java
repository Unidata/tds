/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * IntProcedure.java
 *
 * Created on 06-09-09
 */

package ucar.nc2.dt.ugrid.rtree;


/**
 * Interface that defines a procedure to be executed, that takes an int parameter (as modeled after the COLT
 * IntProcedure)
 * 
 * @version 1.0
 */
public interface IntProcedure {
  /**
   * @param id
   *        integer value
   * 
   * @return flag to indicate whether to continue executing the procedure. Return true to continue executing, or false
   *         to prevent any more
   *         calls to this method.
   */
  public boolean execute(int id);
}
