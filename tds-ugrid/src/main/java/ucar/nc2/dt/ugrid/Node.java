/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
