package thredds.tds;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.test.util.TestOnLocalServer;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.write.Ncdump;
import ucar.unidata.util.test.Assert2;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
@Category(NeedsCdmUnitTest.class)
public class TestTdsNcmlAggregation {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Parameterized.Parameters(name = "{0}")
  public static Collection<String> getTestParameters() {
    return List.of("dodsC/ExampleNcML/Agg.nc", "dodsC/S3ExampleNcML/Agg.nc");
  }

  @Parameterized.Parameter(value = 0)
  public String path;

  @Test
  public void testAggregationExisting() throws IOException {
    final String endpoint = TestOnLocalServer.withHttpPath(path);
    logger.debug("{}", endpoint);

    try (NetcdfFile ncfile = NetcdfDatasets.openFile(endpoint, null)) {
      final Variable time = ncfile.findVariable("time");
      Assert.assertNotNull(time);
      Assert.assertEquals(DataType.DOUBLE, time.getDataType());

      final String units = time.getUnitsString();
      Assert.assertNotNull(units);
      Assert.assertEquals("Hour since 2006-09-25T06:00:00Z", units);

      int count = 0;
      Array data = time.read();
      logger.debug(Ncdump.printArray(data, "time", null));

      while (data.hasNext()) {
        Assert2.assertNearlyEquals(data.nextInt(), (count + 1) * 3);
        count++;
      }

      // test attributes added in NcML
      final String testAtt = ncfile.getRootGroup().findAttributeString("ncmlAdded", null);
      Assert.assertNotNull(testAtt);
      Assert.assertEquals("stuff", testAtt);

      final Variable lat = ncfile.findVariable("lat");
      Assert.assertNotNull(lat);
      final String latTestAtt = lat.findAttributeString("ncmlAdded", null);
      Assert.assertNotNull(latTestAtt);
      Assert.assertEquals("lat_stuff", latTestAtt);
    }
  }
}
