package thredds.server.opendap;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import thredds.test.util.TestOnLocalServer;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.unidata.util.test.category.NeedsExternalResource;

public class TestS3Dap2 {

  @Test
  @Category(NeedsExternalResource.class)
  public void testS3Dap2() throws IOException {
    String filename = TestOnLocalServer.withHttpPath(
        "dodsC/s3-test/ABI-L1b-RadC/2019/363/21/OR_ABI-L1b-RadC-M6C16_G16_s20193632101189_e20193632103574_c20193632104070.nc");
    NetcdfDataset ncd = NetcdfDatasets.openDataset(filename, true, null);
    Assert.assertNotNull(ncd);
    Assert.assertEquals(44, ncd.getVariables().size());
    Assert.assertEquals(9, ncd.getCoordinateAxes().size());
    Assert.assertEquals(6, ncd.getCoordinateSystems().size());
  }

}
