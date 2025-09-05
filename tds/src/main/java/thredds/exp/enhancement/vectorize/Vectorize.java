/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.exp.enhancement.vectorize;

import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.filter.Enhancement;

public abstract class Vectorize implements Enhancement {
  private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Vectorize.class);

  protected Variable uVar;
  protected Variable vVar;
  private String convention;
  protected float convention_offset;
  private int nDims;
  protected int[] shape;
  protected int[] n_dimensional_array;

  public Vectorize(Variable var) {
    try {
      Attribute att = var.findAttribute(getAttributeName());
      String[] vars = att.getStringValue().split("/");
      this.uVar = var.getParentGroup().findVariableLocal(vars[0]);
      this.vVar = var.getParentGroup().findVariableLocal(vars[1]);

      this.convention = vars[2];
      if ("to".equals(this.convention)) {
        this.convention_offset = 0.0f;
      } else if ("from".equals(this.convention)) {
        this.convention_offset = 180.0f;
      } else {
        throw new IllegalArgumentException("The convention must be either 'to' or 'from'.");
      }

      this.shape = var.getShape();
      this.nDims = this.shape.length;
      if (!validateDims()) {
        return;
      }

      this.n_dimensional_array = new int[this.nDims];
      for (int d = 0; d < this.nDims; d++) {
        this.n_dimensional_array[d] = 1;
      }
    } catch (NullPointerException ex) {
      logger.error("Could not parse attribute {}", getAttributeName());
    }
  }

  protected int[] indexToCoords(int index) {
    int[] coords = new int[this.nDims];
    int innerDims = 1;
    for (int i = this.nDims - 1; i >= 0; i--) {
      coords[i] = (index / innerDims) % this.shape[i];
      innerDims *= this.shape[i];
    }
    return coords;
  }

  private boolean validateDims() {
    return checkDims(this.uVar) && checkDims(this.vVar);
  }

  private boolean checkDims(Variable var) {
    if (this.nDims != var.getShape().length) {
      return false;
    }
    for (int i = 0; i < this.nDims; i++) {
      if (this.shape[i] != var.getShape()[i]) {
        return false;
      }
    }
    return true;
  }

  abstract protected String getAttributeName();
}
