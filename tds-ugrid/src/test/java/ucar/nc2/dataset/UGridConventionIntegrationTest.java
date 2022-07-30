/*
 * Copyright (c) 2022 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package ucar.nc2.dataset;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ucar.nc2.Variable;
import ucar.nc2.dataset.conv.UGridConvention;

/**
 * Tests viewing UGRID datasets through ucar.nc2.NetcdfDataset
 */
public class UGridConventionIntegrationTest {
  final static String RESOURCE_PATH = "/cases/";
  static NetcdfDataset ncd;

  @BeforeClass
  public static void init() throws IOException {
    String ugridTestFile =
        UGridConventionIntegrationTest.class.getResource(RESOURCE_PATH + "fvcom/fvcom_delt.ncml").getPath();
    assertThat(ugridTestFile).isNotNull();
    ncd = NetcdfDatasets.openDataset(ugridTestFile);
    assertThat(ncd).isNotNull();
  }

  /**
   * Make sure we load the correct convention for the UGRID test dataset
   */
  @Test
  public void checkConvention() throws IOException {
    assertThat(ncd.getConventionUsed()).isEqualTo(UGridConvention.CONVENTION_NAME);
  }

  /**
   * Test coordinate axes found in the test dataset
   */
  @Test
  public void checkAxisTypes() {
    int numLatAxes = 0;
    int numLonAxes = 0;
    int numGeoZAxes = 0;
    int numTimeAxes = 0;
    ImmutableList<CoordinateAxis> coordnateAxes = ncd.getCoordinateAxes();
    assertThat(coordnateAxes).hasSize(9);
    for (CoordinateAxis ax : coordnateAxes) {
      switch (ax.axisType) {
        case Time:
          numTimeAxes += 1;
          break;
        case Lat:
          numLatAxes += 1;
          break;
        case Lon:
          numLonAxes += 1;
          break;
        case GeoZ:
          numGeoZAxes += 1;
      }
    }
    assertThat(numTimeAxes).isEqualTo(1);
    assertThat(numLatAxes).isEqualTo(2);
    assertThat(numLonAxes).isEqualTo(2);
    assertThat(numGeoZAxes).isEqualTo(4);
  }

  /**
   * Test UGRID node variable coordinate system
   */
  @Test
  public void checkNodeVariableCS() {
    String nodeVarName = "temperature_node";
    Variable var = ncd.findVariable(nodeVarName);
    assertThat(var != null).isTrue();
    assertThat(var instanceof VariableDS).isTrue();
    VariableDS varDs = (VariableDS) var;
    ImmutableList<CoordinateSystem> coordSystems = varDs.getCoordinateSystems();
    assertThat(coordSystems).hasSize(1);
    CoordinateSystem coordSys = coordSystems.get(0);
    assertThat(coordSys.getTaxis().getShortName()).isEqualTo("time");
    assertThat(coordSys.getLatAxis().getShortName()).isEqualTo("lat_node");
    assertThat(coordSys.getLonAxis().getShortName()).isEqualTo("lon_node");
    assertThat(coordSys.getZaxis().getShortName()).isEqualTo("siglay");
  }

  /**
   * Test UGRID face variable coordinate system
   */
  @Test
  public void checkFaceVariableCS() {
    String nodeVarName = "u_face";
    Variable var = ncd.findVariable(nodeVarName);
    assertThat(var != null).isTrue();
    assertThat(var instanceof VariableDS).isTrue();
    VariableDS varDs = (VariableDS) var;
    ImmutableList<CoordinateSystem> coordSystems = varDs.getCoordinateSystems();
    assertThat(coordSystems).hasSize(1);
    CoordinateSystem coordSys = coordSystems.get(0);
    assertThat(coordSys.getTaxis().getShortName()).isEqualTo("time");
    assertThat(coordSys.getLatAxis().getShortName()).isEqualTo("lat_face");
    assertThat(coordSys.getLonAxis().getShortName()).isEqualTo("lon_face");
    assertThat(coordSys.getZaxis().getShortName()).isEqualTo("siglay");
  }

  @AfterClass
  public static void cleanup() throws IOException {
    if (ncd != null) {
      ncd.close();
    }
  }
}
