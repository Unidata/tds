/*
 * Copyright 2012, UCAR/Unidata.
 * See the LICENSE file for more information.
 */

package dap4.servlet;

/**
 * Provide a factory for DSP instances
 */

abstract public class DSPFactory {

  //////////////////////////////////////////////////
  // Constructor(s)

  public DSPFactory() {
    // Subclasses should Register known DSP classes: order is important
    // in event that two or more dsps can match a given file
    // (e.q. FileDSP vs Nc4DSP).
    // Only used in server
  }



} // DSPFactory

