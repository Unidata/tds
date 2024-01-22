/*
 * Copyright 2012, UCAR/Unidata.
 * See the LICENSE file for more information.
 */

package dap4.servlet;

import dap4.core.dmr.DapDimension;
import dap4.core.util.DapException;
import dap4.core.util.Slice;
import dap4.dap4lib.D4Index;
import dap4.dap4lib.cdm.CDMUtil;
import ucar.ma2.Index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A classic implementation of an odometer
 * taken from the netcdf-c code.
 * Extended to provide iterator interface
 */

public class Odometer implements Iterator<Index> {

  //////////////////////////////////////////////////
  // Types

  protected static enum STATE {
    INITIAL, STARTED, DONE;
  }

  //////////////////////////////////////////////////
  // Instance variables

  protected STATE state = STATE.INITIAL;

  protected int rank = 0;
  protected List<Slice> slices = null;
  protected List<DapDimension> dimset = null;

  // The current odometer indices
  protected D4Index index;

  // precompute this.slices[i].getLast() - this.slices[i].getStride()
  protected long[] endpoint;

  //////////////////////////////////////////////////
  // Constructor(s)

  protected Odometer() {}

  public Odometer(List<Slice> set, List<DapDimension> dimset) throws DapException {
    if (set == null)
      throw new DapException("Null slice list");
    assert (set != null && dimset != null);
    if (set.size() != dimset.size()) {
      if (!(dimset.size() == 0 && set.size() == 1))
        throw new DapException("Rank mismatch");
    }
    this.rank = set.size();
    assert (this.rank > 0);
    this.slices = new ArrayList<>();
    this.slices.addAll(set);
    if (dimset != null) {
      this.dimset = new ArrayList<>();
      this.dimset.addAll(dimset);
    }
    this.endpoint = new long[this.rank];
    int[] shape = new int[rank];
    for (int i = 0; i < this.rank; i++)
      shape[i] = slices.get(i).getMax();
    this.index = new D4Index(shape);
    reset();
  }

  protected void reset() {
    try {
      int[] newcounter = new int[this.rank];
      for (int i = 0; i < this.rank; i++) {
        slices.get(i).finish();
        newcounter[i] = this.slices.get(i).getFirst();
        this.endpoint[i] = this.slices.get(i).getLast() - this.slices.get(i).getStride();
      }
      this.index.set(newcounter);
    } catch (DapException de) {
      throw new IllegalArgumentException(de);
    }
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    int[] current = null;
    if (this.index != null)
      current = this.index.getCurrentCounter();
    for (int i = 0; i < rank; i++) {
      if (i > 0)
        buf.append(",");
      if (dimset != null)
        buf.append(dimset.get(i) != null ? dimset.get(i).getShortName() : "null");
      buf.append(this.slices.get(i).toString());
      buf.append(String.format("(%d)", this.slices.get(i).getCount()));
      if (this.index != null) {
        buf.append(String.format("@%d", current[i]));
      }
    }
    return buf.toString();
  }

  //////////////////////////////////////////////////
  // Odometer API

  /**
   * Return odometer rank
   */
  public int rank() {
    return this.rank;
  }

  /**
   * Return ith slice
   */
  public Slice slice(int i) {
    if (i < 0 || i >= this.rank)
      throw new IllegalArgumentException();
    return this.slices.get(i);
  }


  public List<Slice> getSlices() {
    return this.slices;
  }

  /**
   * Compute the linear index
   * from the current odometer indices.
   */
  public long index() {
    return index.currentElement();
  }

  /**
   * Return current set of indices
   */
  public D4Index indices() {
    return this.index;
  }

  /**
   * Compute the total number of elements.
   */
  public long totalSize() {
    long size = 1;
    for (int i = 0; i < this.rank; i++) {
      size *= this.slices.get(i).getCount();
    }
    return size;
  }

  //////////////////////////////////////////////////
  // Iterator API

  @Override
  public boolean hasNext() {
    int stop = this.rank;
    switch (this.state) {
      case INITIAL:
        return true;
      case STARTED:
        int i;
        int[] cur = this.index.getCurrentCounter();
        for (i = stop - 1; i >= 0; i--) { // walk backwards
          if (cur[i] <= this.endpoint[i])
            return true;
        }
        this.state = STATE.DONE;
        break;
      case DONE:
    }
    return false;
  }

  @Override
  public D4Index next() {
    int i;
    int lastpos = this.rank;
    int firstpos = 0;
    switch (this.state) {
      case INITIAL:
        this.state = STATE.STARTED;
        break;
      case STARTED:
        i = step(firstpos, lastpos);
        if (i < 0)
          this.state = STATE.DONE;
        break;
      case DONE:
        break;
    }
    if (this.state == STATE.DONE)
      throw new NoSuchElementException();
    return indices();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  //////////////////////////////////////////////////

  // on entry: indices are the last index set
  // on exit, the indices are the next value
  // return index of place where we have room to step;
  // return -1 if we have completed.
  public int step(int firstpos, int lastpos) {
    int pos = -1;
    int[] indices = this.index.getCurrentCounter();
    for (int i = lastpos - 1; i >= firstpos; i--) { // walk backwards
      if (indices[i] > this.endpoint[i])
        indices[i] = this.slices.get(i).getFirst(); // reset this position
      else {
        indices[i] += this.slices.get(i).getStride(); // move to next indices
        pos = i;
        break;
      }
    }
    this.index.set(indices);
    return pos;
  }

  public List<Odometer> getSubOdometers() {
    List<Odometer> list = new ArrayList<>();
    list.add(this);
    return list;
  }

  public boolean isMulti() {
    return false;
  }

  public boolean isScalar() {
    return false;
  }

}
