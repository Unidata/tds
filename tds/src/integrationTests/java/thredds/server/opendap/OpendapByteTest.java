/*
 * Copyright (c) 2022 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package thredds.server.opendap;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import thredds.test.util.TestOnLocalServer;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.constants.CDM;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.unidata.util.test.TestDir;
import ucar.unidata.util.test.category.NeedsCdmUnitTest;
import java.io.IOException;

/**
 * Test that we include the _Unsigned attribute for byte and ubyte variables
 * through the DAP2 server.
 */

@Category(NeedsCdmUnitTest.class)
public class OpendapByteTest {

  @Test
  public void testSignedByte() throws IOException {
    String path = "formats/netcdf3/standardVar.nc";
    String byteVarName = "t2";
    checkUnsignedAttr(path, byteVarName, false);
  }

  @Test
  public void testUnsignedByte() throws IOException {
    String path = "formats/nexrad/level2/Level2_KDIX_20150626_0639.ar2v";
    String ubyteVarName = "Reflectivity_HI";
    checkUnsignedAttr(path, ubyteVarName, true);
  }

  private void checkUnsignedAttr(String path, String varName, boolean isUnsigned) throws IOException {
    String dodsUrl = TestOnLocalServer.withHttpPath("dodsC/scanCdmUnitTests/" + path);
    try (NetcdfFile ncf = NetcdfFiles.open(TestDir.cdmUnitTestDir + path);
        NetcdfFile ncfDap2 = NetcdfDatasets.openFile(dodsUrl, null)) {
      Variable ubyteVar = ncf.findVariable(varName);
      assertThat(ubyteVar != null).isTrue();
      if (isUnsigned) {
        assertThat(ubyteVar.getDataType()).isEqualTo(DataType.UBYTE);
      } else {
        assertThat(ubyteVar.getDataType()).isEqualTo(DataType.BYTE);
      }
      Variable ubyteVarDap2 = ncfDap2.findVariable(varName);
      assertThat(ubyteVarDap2 != null).isTrue();
      Attribute unsignedAttr = ubyteVarDap2.findAttribute(CDM.UNSIGNED);
      assertThat(unsignedAttr).isNotNull();
      String val = unsignedAttr.getStringValue();
      assertThat(val).isNotNull();
      assertThat(val).isEqualTo(String.valueOf(isUnsigned));
    }
  }
}
