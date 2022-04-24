/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucar.nc2.dataset.conv;

import java.util.StringTokenizer;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableEnhanced;
import ucar.nc2.constants.CF;
import ucar.nc2.units.SimpleUnit;

/**
 *
 * @author Kyle
 */
public class UGridConvention extends CF1Convention {

  public UGridConvention() {
    this.conventionName = "UGRID-1.X";
  }

  @Override
  protected AxisType getAxisType(NetcdfDataset ncDataset, VariableEnhanced v) {
    AxisType at = super.getAxisType(ncDataset, v);

    // TODO: Add nodal support

    return at;
  }

  /**
   * The attribute "coordinates" is an alias for _CoordinateAxes.
   */
  /*
   * @Override
   * protected void findCoordinateAxes(NetcdfDataset ds) {
   * 
   * // *_coordinates are aliases for "coordinates", since they can be on
   * // many different locations along the polygons in UGrids.
   * 
   * for (VarProcess vp : varList) {
   * StringBuilder coordStr = new StringBuilder();
   * if (vp.coordAxes == null) {
   * for (Attribute att : vp.v.getAttributes()) {
   * if (att.getName().contains("_coordinates")) {
   * coordStr.append(att.getStringValue().trim()).append(",");
   * }
   * }
   * }
   * if (coordStr != null && coordStr.length() > 0) {
   * coordStr.deleteCharAt(coordStr.length() - 1);
   * vp.coordinates = coordStr.toString();
   * }
   * }
   * 
   * super.findCoordinateAxes(ds);
   * }
   * 
   * 
   * // Override to supposrt multiple coordinate systems in a variable
   * 
   * @Override
   * protected void findCoordinateAxes(VarProcess vp, String coordinates) {
   * if (coordinates.contains(",")) {
   * String[] vars = coordinates.split(",");
   * for (String v : vars) {
   * super.findCoordinateAxes(vp,v);
   * }
   * }
   * }
   */
}
