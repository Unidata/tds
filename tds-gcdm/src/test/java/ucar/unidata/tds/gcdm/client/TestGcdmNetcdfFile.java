/*
 * Copyright (c) 2025-2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package ucar.unidata.tds.gcdm.client;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Formatter;
import org.junit.Ignore;
import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.util.CompareNetcdf2;

public class TestGcdmNetcdfFile {

  @Test
  public void testGcdmNetcdfFile() throws IOException {
    String localFile = "../tds/src/test/data/testdata2/temp_air_01082000.nc";
    String gcdmUrl = "gcdm://localhost:16111/" + Paths.get(localFile).toAbsolutePath().normalize();
    try (GcdmNetcdfFile gcdmFile = GcdmNetcdfFile.builder().setRemoteURI(gcdmUrl).build()) {
      assertThat(gcdmFile).isNotNull();
    }
    try (NetcdfFile ncFile = NetcdfDatasets.openFile(localFile, null);
        GcdmNetcdfFile gcdmFile = GcdmNetcdfFile.builder().setRemoteURI(gcdmUrl).build();
        NetcdfFile gcdmNcFile = NetcdfDatasets.openFile(gcdmUrl, null)) {
      assertThat(ncFile).isNotNull();
      assertThat(gcdmFile).isNotNull();
      assertThat(gcdmNcFile).isNotNull();

      Formatter formatter = new Formatter();
      boolean ok = CompareNetcdf2.compareFiles(ncFile, gcdmFile, formatter, true, true, true);
      assertWithMessage(formatter.toString()).that(ok).isTrue();
      formatter.close();

      formatter = new Formatter();
      ok = CompareNetcdf2.compareFiles(ncFile, gcdmNcFile, formatter, true, true, true);
      assertWithMessage(formatter.toString()).that(ok).isTrue();
    }
  }

  @Test
  @Ignore("A properly configured Python-based gcdm server must be running.")
  public void testGcdmIcechunk() throws IOException {
    String gcdmUrl = "gcdm://localhost:1234/";
    try (GcdmNetcdfFile gcdmFile = GcdmNetcdfFile.builder().setRemoteURI(gcdmUrl).build()) {
      assertThat(gcdmFile).isNotNull();
      Attribute units = gcdmFile.findVariable("time").findAttribute("units");
      assertThat(units).isNotNull();
      assertThat(units.getStringValue()).isEqualTo("seconds since 1970-10-10T00:00:00");
      assertThat(gcdmFile.getVariables().stream().map(Variable::getFullName).toList())
          .containsAtLeast("time", "latitude", "longitude");

    }
  }
}