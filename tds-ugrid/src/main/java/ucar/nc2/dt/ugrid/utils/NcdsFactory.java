/*
 * Copyright (c) 2011-2022 Applied Science Associates and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package ucar.nc2.dt.ugrid.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URISyntaxException;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;

/**
 * Factory class with static methods for generating {@link NetcdfDataset} objects from one or more objects.
 *
 * @author cmueller / kwilcox
 */
public class NcdsFactory {

  final static String RESOURCE_PATH = "/netcdf/ncml/";

  public enum NcdsTemplate {

    UGRID("ugrid-template.xml");

    String resourceName = null;

    NcdsTemplate(String resourceName) {
      if (null == resourceName || resourceName.isEmpty()) {
        throw new IllegalArgumentException("Argument resourceName cannot be NULL or empty");
      }
      this.resourceName = resourceName;
    }

    public String getResourceName() {
      return resourceName;
    }
  }

  public static NetcdfDataset getNcdsFromTemplate(NcdsTemplate ncdsTemp)
      throws URISyntaxException, FileNotFoundException, IOException {
    File temp = File.createTempFile("temp_ncml", ".ncml");
    temp.deleteOnExit();

    getSchemaTemplate(temp, ncdsTemp.getResourceName());
    return NetcdfDataset.openDataset(temp.getCanonicalPath());
  }

  private static void getSchemaTemplate(File tempFile, String schemaName)
      throws URISyntaxException, FileNotFoundException, IOException {
    java.io.InputStream in = null;
    java.io.FileOutputStream fos = null;
    try {
      in = NcdsFactory.class.getResourceAsStream(RESOURCE_PATH + schemaName);
      fos = new java.io.FileOutputStream(tempFile);
      byte[] buff = new byte[1024];
      int len = 0;
      while ((len = in.read(buff)) > 0) {
        fos.write(buff, 0, len);
      }
    } finally {
      if (fos != null) {
        fos.close();
      }
      if (in != null) {
        in.close();
      }
    }
  }
}
