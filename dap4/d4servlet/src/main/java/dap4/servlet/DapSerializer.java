/*
 * Copyright 2012, UCAR/Unidata.
 * See the LICENSE file for more information.
 */

package dap4.servlet;

import dap4.core.ce.CEConstraint;
import dap4.core.dmr.*;
import dap4.core.interfaces.ArrayScheme;
import dap4.core.util.*;
import ucar.ma2.Index;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given a DSP, serialize
 * possibly constrained data.
 */

public class DapSerializer {
  //////////////////////////////////////////////////
  // Instance variables

  protected OutputStream stream = null;
  protected SerialWriter writer = null;
  protected CDMWrap cdmwrap = null;
  protected CEConstraint ce = null;
  protected ByteOrder order = null;
  protected ChecksumMode checksummode = null;

  protected Map<DapVariable, Long> checksums = null; // record variable checkums

  //////////////////////////////////////////////////
  // Constructor(s)

  public DapSerializer() {}

  /**
   * Primary constructor
   *
   * @param c4 The DSP to write
   * @param constraint Any applicable constraint
   * @param stream Write to this stream
   * @param order The byte order to use
   */
  public DapSerializer(CDMWrap c4, CEConstraint constraint, OutputStream stream, ByteOrder order, ChecksumMode mode)
      throws IOException {
    this.cdmwrap = c4;
    this.order = order;
    this.checksummode = mode;
    this.stream = stream;
    this.ce = constraint;
  }

  //////////////////////////////////////////////////
  // Accessors

  public Map<DapVariable, Long> getChecksums() {
    return this.checksums;
  }

  //////////////////////////////////////////////////
  // Writers

  public void write(DapDataset dmr) throws IOException {
    writer = new SerialWriter(this.stream, this.order, this.checksummode);
    writer.flush(); // If stream is ChunkWriter, then dump DMR
    // Iterate over the top-level variables in the constraint
    for (DapVariable var : dmr.getTopVariables()) {
      if (!this.ce.references(var) || var.getCount() == 0)
        continue;
      CDMData vardata = this.cdmwrap.getVariableData(var);
      if (vardata == null)
        throw new dap4.core.util.DapException("DapSerializer: cannot find  Variable data " + var.getFQN());
      writeVariable(vardata, writer);
    }
  }

  //////////////////////////////////////////////////
  // Recursive variable writer

  /**
   * @param data - cursor referencing what to write
   * @param dst - where to write
   * @throws IOException
   */
  protected void writeVariable(CDMData data, SerialWriter dst) throws IOException {
    DapVariable template = (DapVariable) data.getTemplate();
    dst.startVariable();
    switch (data.getScheme()) {
      case ATOMIC:
        writeAtomicVariable(data, dst);
        break;
      case STRUCTARRAY:
        // case STRUCTURE:
        writeStructure(data, dst);
        break;
      case SEQARRAY:
        // case SEQUENCE:
        writeSequence(data, dst);
        break;
      default:
        assert false : "Unexpected variable type: " + data.toString();
    }
    dst.endVariable();
    if (this.checksummode == ChecksumMode.TRUE) {
      if (this.checksums == null)
        this.checksums = new HashMap<>();
      long checksum = dst.getLastChecksum();
      this.checksums.put(template, (Long) checksum);
    }
  }

  /**
   * Write out an atomic variable.
   *
   * @param data
   * @param dst
   * @throws dap4.core.util.DapException
   */
  protected void writeAtomicVariable(CDMData data, SerialWriter dst) throws IOException {
    DapVariable template = (DapVariable) data.getTemplate();
    assert (this.ce.references(template));
    DapType basetype = template.getBaseType();
    // get the slices from constraint
    List<Slice> slices = ce.getConstrainedSlices(template);
    if (slices == null)
      throw new DapException("Unknown variable: " + template.getFQN());
    Object values = data.read(slices);
    dst.writeAtomicArray(basetype, values);
  }

  /**
   * Write out a scalar or array structure instance
   *
   * @param data
   * @param dst - where to write
   * @throws dap4.core.util.DapException
   */

  protected void writeStructure(CDMData data, SerialWriter dst) throws IOException {
    DapVariable template = (DapVariable) data.getTemplate();
    DapStructure ds = (DapStructure) template.getBaseType();
    assert (this.ce.references(template));
    List<Slice> slices = ce.getConstrainedSlices(template);
    Odometer odom = OdometerFactory.factory(slices, template.getDimensions());
    while (odom.hasNext()) {
      Index index = odom.next();
      CDMData[] instance = (CDMData[]) data.read(index);
      writeStructure1(instance[0], dst);
    }
  }

  /**
   * Write out a single structure instance
   *
   * @param instance
   * @param dst - where to write
   * @throws dap4.core.util.DapException
   */

  protected void writeStructure1(CDMData instance, SerialWriter dst) throws IOException {
    assert instance.getScheme() == ArrayScheme.STRUCTURE;
    DapVariable template = (DapVariable) instance.getTemplate();
    assert (this.ce.references(template));
    DapStructure ds = (DapStructure) template.getBaseType();

    List<DapVariable> fields = ds.getFields();
    for (int i = 0; i < fields.size(); i++) {
      DapVariable field = fields.get(i);
      if (!this.ce.references(field))
        continue; // not in the view
      CDMData df = (CDMData) instance.readField(i);
      writeVariable(df, dst);
    }
  }

  /**
   * Write out a single or array sequence instance
   *
   * @param data
   * @param dst - where to write
   * @throws dap4.core.util.DapException
   */

  protected void writeSequence(CDMData data, SerialWriter dst) throws IOException {
    DapVariable template = (DapVariable) data.getTemplate();
    DapSequence ds = (DapSequence) template.getBaseType();
    assert (this.ce.references(template));
    List<Slice> slices = ce.getConstrainedSlices(template);
    Odometer odom = OdometerFactory.factory(slices, template.getDimensions());
    CDMData[] instances = (CDMData[]) data.read(slices);
    for (int i = 0; i < instances.length; i++) {
      writeSequence1(instances[i], dst);
    }
  }

  /**
   * Write out a single Sequence of records
   * (Eventually use any filter in the DapVariable)
   *
   * @param instance the sequence instance
   * @param dst write target
   * @throws dap4.core.util.DapException
   */

  protected void writeSequence1(CDMData instance, SerialWriter dst) throws IOException {
    DapVariable template = (DapVariable) instance.getTemplate();
    DapSequence seq = (DapSequence) template.getBaseType();
    assert (this.ce.references(template));
    long nrecs = instance.getRecordCount();
    dst.writeCount(nrecs);
    for (long i = 0; i < nrecs; i++) {
      CDMData record = instance.readRecord(i);
      writeRecord(record, dst);
    }
  }

  /**
   * Write out a single Record instance.
   *
   * @param record the record data cursor
   * @param dst to which to write
   * @throws dap4.core.util.DapException
   */

  protected void writeRecord(CDMData record, SerialWriter dst) throws IOException {
    DapVariable template = (DapVariable) record.getTemplate();
    DapSequence seq = (DapSequence) template.getBaseType();
    List<DapVariable> fields = seq.getFields();
    for (int i = 0; i < fields.size(); i++) {
      DapVariable field = fields.get(i);
      if (!this.ce.references(field))
        continue; // not in the view
      CDMData df = (CDMData) record.readField(i);
      writeVariable(df, dst);
    }
  }

}


