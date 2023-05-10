/*
 * Copyright (c) 1998 - 2012. University Corporation for Atmospheric Research/Unidata
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation. Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package thredds.server.ncss.controller.grid;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertArrayEquals;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thredds.junit4.SpringJUnit4ParameterizedClassRunner;
import thredds.junit4.SpringJUnit4ParameterizedClassRunner.Parameters;
import thredds.mock.web.MockTdsContextLoader;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.util.IO;
import ucar.unidata.geoloc.ProjectionRect;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;

@RunWith(SpringJUnit4ParameterizedClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/WEB-INF/applicationContext.xml"}, loader = MockTdsContextLoader.class)
@Category(NeedsCdmUnitTest.class)
public class GridCoverageSubsettingTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Parameters
  public static Collection<Object[]> getTestParameters() {

    return Arrays.asList(new Object[][] {
        {"/ncss/grid/scanCdmUnitTests/tds/ncep/RR_CONUS_13km_20121028_0000.grib2", "Pressure_surface", false, 700.0,
            2700.0, -2000, 666,
            new Expected(new int[] {1, 149, 198}, new ProjectionRect(-2004.745, 697.882, 663.620, 2702.542))},
        {"/ncss/grid/scanCdmUnitTests/tds/ncep/RR_CONUS_13km_20121028_0000.grib2", "Pressure_surface", false, 0, 4000.0,
            1234, 4000,
            new Expected(new int[] {1, 294, 114},
                new ProjectionRect(1232.509766, -6.457754, 2763.094727, 3962.227295))},
        {"/ncss/grid/scanCdmUnitTests/tds/ncep/RR_CONUS_13km_20121028_0000.grib2", "Pressure_surface", false, -4000,
            444, 1234, 4000,
            new Expected(new int[] {1, 77, 114}, new ProjectionRect(1232.510, -588.893, 2763.095, 440.527))},
        {"/ncss/grid/scanCdmUnitTests/tds/ncep/RR_CONUS_13km_20121028_0000.grib2", "Pressure_surface", false, 2000,
            4000, -4000, -833.102753,
            new Expected(new int[] {1, 146, 186}, new ProjectionRect(-3332.155, 1998.202, -826.330, 3962.227))},

        {"/ncss/grid/scanLocal/GFS_Global_onedeg_20120515_1200.grib2.nc", "Temperature_surface", true, -90, 90, -180,
            180, new Expected(new int[] {1, 181, 360}, new ProjectionRect(-90, 0, 90, 359))},
        {"/ncss/grid/scanLocal/GFS_Global_onedeg_20120515_1200.grib2.nc", "Temperature_surface", true, -90, 90, 0, 360,
            new Expected(new int[] {1, 181, 360}, new ProjectionRect(-90, 0, 90, 359))},
        {"/ncss/grid/scanLocal/GFS_Global_onedeg_20120515_1200.grib2.nc", "Temperature_surface", true, 0, 90, -180, 180,
            new Expected(new int[] {1, 91, 360}, new ProjectionRect(0, 0, 90, 359))},
        {"/ncss/grid/scanLocal/GFS_Global_onedeg_20120515_1200.grib2.nc", "Temperature_surface", true, 0, 90, 0, 360,
            new Expected(new int[] {1, 91, 360}, new ProjectionRect(0, 0, 90, 359))},

    });
  }

  private static class Expected {
    int[] shape;
    ProjectionRect rect;

    public Expected(int[] shape, ProjectionRect rect) {
      this.shape = shape;
      this.rect = rect;
    }
  }

  @Before
  public void setUp() throws IOException {
    mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  private final String pathInfo;
  private final Expected expect;
  private final String vars;
  private final double north;
  private final double south;
  private final double east;
  private final double west;
  private final boolean isLatLon;

  public GridCoverageSubsettingTest(String pathInfo, String vars, boolean isLatLon, double south, double north,
      double west, double east, Expected expect) {
    this.pathInfo = pathInfo;
    this.vars = vars;
    this.north = north;
    this.south = south;
    this.east = east;
    this.west = west;
    this.expect = expect;
    this.isLatLon = isLatLon;
  }

  @Test
  public void shouldSubsetGrid() throws Exception {
    RequestBuilder requestBuilder;
    if (isLatLon) {
      requestBuilder = MockMvcRequestBuilders.get(pathInfo).servletPath(pathInfo).param("var", vars)
          .param("north", Double.valueOf(north).toString()).param("south", Double.valueOf(south).toString())
          .param("east", Double.valueOf(east).toString()).param("west", Double.valueOf(west).toString());
    } else {
      requestBuilder = MockMvcRequestBuilders.get(pathInfo).servletPath(pathInfo).param("var", vars)
          .param("maxy", Double.valueOf(north).toString()).param("miny", Double.valueOf(south).toString())
          .param("maxx", Double.valueOf(east).toString()).param("minx", Double.valueOf(west).toString());
    }

    MvcResult mvc = this.mockMvc.perform(requestBuilder).andReturn();
    assertThat(mvc.getResponse().getStatus()).isEqualTo(HttpStatus.SC_OK);

    // Save the result
    String fileOut = tempFolder.newFile().getAbsolutePath();
    try (FileOutputStream fout = new FileOutputStream(fileOut)) {
      ByteArrayInputStream bis = new ByteArrayInputStream(mvc.getResponse().getContentAsByteArray());
      IO.copy(bis, fout);
    }

    // Open the binary response in memory
    NetcdfFile nf = NetcdfFiles.open(fileOut);
    Variable v = nf.getRootGroup().findVariableLocal(isLatLon ? "lat" : "x");
    assertThat((Object) v).isNotNull();
    Array x = v.read();
    v = nf.getRootGroup().findVariableLocal(isLatLon ? "lon" : "y");
    assertThat((Object) v).isNotNull();
    Array y = v.read();

    int nx = (int) x.getSize();
    int ny = (int) y.getSize();
    ProjectionRect prect = new ProjectionRect(x.getDouble(0), y.getDouble(0), x.getDouble(nx - 1), y.getDouble(ny - 1));

    v = nf.getRootGroup().findVariableLocal(vars);
    assertArrayEquals(expect.shape, v.getShape());
    assertThat(expect.rect.nearlyEquals(prect)).isTrue();
  }
}
