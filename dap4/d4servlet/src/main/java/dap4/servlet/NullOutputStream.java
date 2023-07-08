/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package dap4.servlet;

import java.io.OutputStream;

/**
 * OutputStream that discards anything written to it.
 * Roughly /dev/null
 */

public class NullOutputStream extends OutputStream {

  @Override
  public void write(byte[] b) {}

  @Override
  public void write(byte[] b, int off, int len) {}

  public void write(int b) {}
}
