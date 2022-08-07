/*
 * Applied Science Associates, Inc.
 * Copyright 2010. All Rights Reserved.
 *
 * StreamGobbler.java
 *
 * Created on Feb 5, 2010 @ 8:30:40 AM
 */

package ucar.nc2.dt.ugrid.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;


/**
 * Utility class for consuming an InputStream and redirecting the output to <tt>System.out</tt>
 *
 * @author CBM <cmueller@asascience.com>
 */

public class StreamGobbler extends Thread {

  private Logger logger;
  private InputStream inStream;
  private String procID;
  private transient boolean terminate = false;
  private String newLine;
  private int outType = -1;

  public StreamGobbler(InputStream inStream, String procID) {
    this(inStream, procID, null);
  }

  public StreamGobbler(InputStream inStream, String procID, Logger outLog) {
    this.inStream = inStream;
    this.procID = procID;
    this.logger = outLog;
    if (logger != null) {
      outType = 0;
    }
    newLine = System.getProperty("line.separator");
  }

  @Override
  public void run() {
    InputStreamReader isr = new InputStreamReader(inStream);
    BufferedReader br = new BufferedReader(isr);
    try {
      String line = null;
      while (!terminate) {
        if ((line = br.readLine()) != null) {
          switch (outType) {
            case 0:
              logger.info(newLine + procID + " > " + line);
              break;
            default:
              logger.debug(procID + " > " + line);
              break;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        br.close();
      } catch (IOException e) {
        logger.warn("Could not properly close the gobbler's buffered reader", e);
      }
      try {
        isr.close();
      } catch (IOException e) {
        logger.warn("Could not properly close the gobbler's input stream", e);
      }
      br = null;
      isr = null;
      logger = null;
    }
  }

  public void terminate() {
    terminate = true;
  }
}
