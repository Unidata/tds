/*
 * Copyright (c) 1998-2022 University Corporation for Atmospheric Research/Unidata and Applied Science Associates
 * See LICENSE for license information.
 */

package ucar.nc2.ft.ugrid;

import ucar.nc2.ft.FeatureDatasetFactory;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.util.CancelTask;
import ucar.nc2.dt.ugrid.UGridDataset;

import java.io.IOException;
import java.util.Formatter;

/**
 * FeatureDatasetFactory for UGrids, using standard coord sys analysis
 */
public class UGridDatasetStandardFactory implements FeatureDatasetFactory {

  public Object isMine(FeatureType wantFeatureType, NetcdfDataset ncd, Formatter errlog) throws IOException {
    // If they ask for a grid, and there seems to be some grids, go for it
    if (wantFeatureType == FeatureType.UGRID) {
      ucar.nc2.dt.ugrid.UGridDataset ugds = new ucar.nc2.dt.ugrid.UGridDataset(ncd);

      if (ugds.getMeshsets().size() > 0) {
        return ugds;
      }
    }
    return null;
  }

  public FeatureDataset open(FeatureType ftype, NetcdfDataset ncd, Object analysis, CancelTask task, Formatter errlog)
      throws IOException {
    // already been opened by isMine
    return (UGridDataset) analysis;
  }

  @Override
  public FeatureType[] getFeatureTypes() {
    return new FeatureType[0];
  }

  public FeatureType[] getFeatureType() {
    return new FeatureType[] {FeatureType.UGRID};
  }
}
