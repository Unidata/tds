package thredds.server.ncss.view.dsg;

import org.apache.commons.io.IOUtils;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants.CDM;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.util.CompareNetcdf2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Formatter;

public class DsgSubsetTestUtils {
  public static boolean compareNetCDF(File expectedResultFile, File actualResultFile) throws IOException {
    try (NetcdfFile expectedNcFile = NetcdfDatasets.openDataset(expectedResultFile.getAbsolutePath());
        NetcdfFile actualNcFile = NetcdfDatasets.openDataset(actualResultFile.getAbsolutePath())) {
      Formatter formatter = new Formatter();
      boolean contentsAreEqual = new CompareNetcdf2(formatter, false, false, true).compare(expectedNcFile, actualNcFile,
          new NcssNetcdfObjFilter());

      if (!contentsAreEqual) {
        System.err.println(formatter);
      }

      return contentsAreEqual;
    }
  }

  private static class NcssNetcdfObjFilter implements CompareNetcdf2.ObjFilter {
    @Override
    public boolean attCheckOk(Variable v, Attribute att) {
      return !att.getShortName().equals(CDM.TITLE) && // Ignore the "title" attribute.
          !att.getShortName().equals(CDM.NCPROPERTIES);
    }

    @Override
    public boolean varDataTypeCheckOk(Variable v) {
      return true; // Check all variables.
    }
  }

  public static boolean compareText(File expectedResultFile, File actualResultFile) throws IOException {
    try (BufferedReader actualReader = new BufferedReader(new FileReader(actualResultFile));
        BufferedReader expectedReader = new BufferedReader(new FileReader(expectedResultFile))) {
      return IOUtils.contentEqualsIgnoreEOL(expectedReader, actualReader);
    }
  }
}
