/*
 * Copyright 2012, UCAR/Unidata.
 * See the LICENSE file for more information.
 */

package dap4.servlet;

import dap4.core.dmr.DapDimension;
import dap4.core.util.DapException;
import dap4.core.util.DapUtil;
import dap4.core.util.Slice;

import java.util.List;

/**
 * Factory class for Odometers
 */

abstract public class OdometerFactory {

  public static Odometer factoryScalar() {
    return new ScalarOdometer();
  }

  public static Odometer factory(List<Slice> slices) throws DapException {
    return factory(slices, null);
  }

  public static Odometer factory(List<Slice> slices, List<DapDimension> dimset) throws DapException {
    // check for scalar case
    if (dimset != null && dimset.size() == 0) {
      if (!DapUtil.isScalarSlices(slices))
        throw new DapException("Cannot build scalar odometer with non-scalar slices");
      return factoryScalar();
    }
    boolean multi = false;
    if (slices != null) {
      for (int i = 0; i < slices.size(); i++) {
        if (slices.get(i).getSort() == Slice.Sort.Multi) {
          multi = true;
          break;
        }
      }
    }
    if (slices == null || slices.size() == 0)
      return factoryScalar();
    else if (multi)
      return new MultiOdometer(slices, dimset);
    else
      return new Odometer(slices, dimset);
  }
}
