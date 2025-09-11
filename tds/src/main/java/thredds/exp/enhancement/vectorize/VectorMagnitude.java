/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.exp.enhancement.vectorize;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import ucar.ma2.DataType;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset.Enhance;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.filter.Enhancement;
import ucar.nc2.filter.EnhancementProvider;

public class VectorMagnitude extends Vectorize {

  private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(VectorMagnitude.class);

  public static final String ATTRIBUTE_NAME = "vectorize_mag";

  public VectorMagnitude(Variable var) {
    super(var);
  }

  ReentrantLock lock = new ReentrantLock();

  @Override
  public double convert(double num) {
    lock.lock();
    try {
      double u_val = uVar.read(indexToCoords((int) num), this.n_dimensional_array).getDouble(0);
      double v_val = vVar.read(indexToCoords((int) num), this.n_dimensional_array).getDouble(0);
      return Math.sqrt(u_val * u_val + v_val * v_val);
    } catch (Exception ex) {
      logger.error("error converting u and v to magnitude", ex);
      return Double.NaN;
    } finally {
      lock.unlock();
    }
  }

  @Override
  protected String getAttributeName() {
    return ATTRIBUTE_NAME;
  }

  public static class Provider implements EnhancementProvider {

    @Override
    public String getAttributeName() {
      return ATTRIBUTE_NAME;
    }

    @Override
    public boolean appliesTo(Set<Enhance> enhance, DataType dt) {
      return dt.isNumeric();
    }

    @Override
    public Enhancement create(VariableDS var) {
      return new VectorMagnitude(var);
    }
  }
}
