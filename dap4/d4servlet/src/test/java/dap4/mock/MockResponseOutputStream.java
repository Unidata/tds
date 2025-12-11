/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package dap4.mock;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * MockResponseOutputStream is a mock implementation of ServletOutputStream
 * <p>
 * Write data to an internal ByteArrayOutputStream. This class is primarily
 * designed for mock testing purposes to capture data written to the response
 * output stream.
 */
public class MockResponseOutputStream extends ServletOutputStream {
  private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

  @Override
  public void write(int b) throws IOException {
    baos.write(b);
  }

  @Override
  public void setWriteListener(WriteListener writeListener) {}

  @Override
  public boolean isReady() {
    return true;
  }

  public byte[] getOutput() {
    return baos.toByteArray();
  }
}
