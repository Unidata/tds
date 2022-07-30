/*
 * Copyright (c) 1998-2022 University Corporation for Atmospheric Research/Unidata and Applied Science Associates
 * See LICENSE for license information.
 */

package ucar.nc2.dt;

import java.util.*;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.ugrid.Mesh;
import ucar.unidata.geoloc.LatLonRect;

/**
 * A dataset containing Grid objects.
 * 
 * @author caron
 */

public interface UGridDataset extends ucar.nc2.dt.GridDataset {

  /**
   * get the list of GridDatatype objects contained in this dataset.
   * 
   * @return list of GridDatatype
   */
  public List<UGridDatatype> getMeshVariables();

  public UGridDatatype getMeshVariableByName(String n);

  public List<Meshset> getMeshsets();

  public ucar.nc2.dt.ugrid.UGridDataset subset(LatLonRect r);

  public interface Meshset {

    /**
     * Get list of UGridDatatype objects with same Meshset
     * 
     * @return list of UGridDatatype
     */
    public List<ucar.nc2.dt.UGridDatatype> getMeshVariables();

    public UGridDatatype getMeshVariableByName(String n);

    /**
     * all the UGridDatatype in this Meshset use this topology
     * 
     * @return the common topology
     */
    public VariableDS getDescriptionVariable();

    public Mesh getMesh();
  }

}
