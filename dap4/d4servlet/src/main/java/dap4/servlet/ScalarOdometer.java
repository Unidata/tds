/*
 * Copyright 2012, UCAR/Unidata.
 * See the LICENSE file for more information.
 */

package dap4.servlet;

import dap4.core.util.Slice;
import dap4.dap4lib.D4Index;

import java.util.NoSuchElementException;

/**
 * A implementation of an odometer for scalar variables.
 */

public class ScalarOdometer extends Odometer {
  //////////////////////////////////////////////////
  // Constants

  public ScalarOdometer() {
    this.state = STATE.INITIAL;
    this.index = new D4Index(0);
    this.slices = Slice.SCALARSLICES;
  }

  public long index() {
    return 0;
  }

  public long totalSize() {
    return 1;
  }

  public boolean hasNext() {
    return this.state != STATE.DONE;
  }

  public D4Index next() {
    if (this.state == STATE.DONE)
      throw new NoSuchElementException();
    this.state = STATE.DONE;
    return D4Index.SCALAR;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isScalar() {
    return true;
  }

}
