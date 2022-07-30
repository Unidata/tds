/*
 * Copyright (c) 2011-2014 Applied Science Associates
 * See LICENSE for license information.
 */

package ucar.nc2.dt.ugrid;

/**
 *
 * @author Kyle
 */
public class Node extends Entity {

  @Override
  public boolean isBoundry() {
    if (getConnectingCells().length == 0)
      return true;
    else
      return false;
  }
}
