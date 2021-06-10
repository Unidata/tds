/*
 * Copyright (c) 2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.test.util;

public class TdsTestDir {
  public static String cdmUseBuildersPropName = "thredds.test.experimental.useNetcdfJavaBuilders";
  public static boolean cdmUseBuilders;

  // Remote Test server(s)
  public static String remoteTestServer = "localhost:8081";

  static {
    String useBuilderProp = System.getProperty(cdmUseBuildersPropName, "");
    // default (prop not set) true. Otherwise, check prop.
    cdmUseBuilders = useBuilderProp.equals("") ? Boolean.TRUE : Boolean.getBoolean(cdmUseBuildersPropName);
  }
}
