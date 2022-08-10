/*
 * Copyright (c) Applied Science Associates and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package ucar.nc2.dataset.conv;

import java.util.List;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.CDM;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.internal.dataset.CoordSystemBuilder;
import ucar.nc2.internal.dataset.spi.CFSubConventionProvider;
import ucar.nc2.internal.dataset.conv.CF1Convention;

/**
 *
 * @author Kyle
 */
public class UGridConvention extends CF1Convention {

  @Override
  public AxisType getAxisType(VariableDS.Builder vb) {
    AxisType at = super.getAxisType(vb);
    // TODO: Add nodal support

    return at;
  }

  private static String CONVENTION_NAME_BASE = "UGRID";
  public static String CONVENTION_NAME = CONVENTION_NAME_BASE + "-1.X";

  private UGridConvention(NetcdfDataset.Builder<?> datasetBuilder) {
    super(datasetBuilder);
    this.conventionName = CONVENTION_NAME;
  }

  public static class Factory implements CFSubConventionProvider {
    @Override
    public boolean isMine(NetcdfFile ncfile) {
      boolean mine = false;
      Attribute conventionAttr = ncfile.findGlobalAttributeIgnoreCase(CDM.CONVENTIONS);
      if (conventionAttr != null) {
        String conventionValue = conventionAttr.getStringValue();
        if (conventionValue != null) {
          mine = conventionValue.startsWith(CONVENTION_NAME_BASE);
        }
      }
      return mine;
    }

    @Override
    public boolean isMine(List<String> convs) {
      boolean mine = false;
      for (String conv : convs) {
        if (conv.startsWith(CONVENTION_NAME_BASE)) {
          mine = true;
          break;
        }
      }
      return mine;
    }

    @Override
    public String getConventionName() {
      return CONVENTION_NAME;
    }

    @Override
    public CoordSystemBuilder open(NetcdfDataset.Builder datasetBuilder) {
      return new UGridConvention(datasetBuilder);
    }
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
