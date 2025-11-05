/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package dap4.mock.mock;

import dap4.core.util.DapContext;
import dap4.core.util.DapException;
import dap4.servlet.CDMWrap;
import dap4.servlet.DapController;
import dap4.servlet.DapRequest;
import java.io.IOException;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * The TestDapControllerBase is a minimally mocked implementation of DapController.
 * <p>
 * This class provides a test implementation of DapController with minimal functionality
 * for testing purposes. It allows injecting a test NetcdfDataset, which will be used for
 * responding to DAP4 requests thanks to the implemented getCDMWrap method.
 */
public class TestDapControllerBase extends DapController {

  private NetcdfDataset testDataset = null;

  public TestDapControllerBase(NetcdfDataset testDataset) {
    super();
    this.testDataset = testDataset;
  }

  @Override
  protected void doFavicon(String icopath, DapContext cxt) throws IOException {

  }

  @Override
  protected void doCapabilities(DapRequest drq, DapContext cxt) throws IOException {

  }

  @Override
  public long getBinaryWriteLimit() {
    return 25 * 1024 * 1024;
  }

  @Override
  public String getServletID() {
    return "mockDap4Servlet";
  }

  @Override
  protected String getWebContentRoot(DapRequest drq) throws DapException {
    return "";
  }

  /* no matter the request, return the dataset the controller was constructed with */
  @Override
  protected CDMWrap getCDMWrap(DapRequest drq) throws IOException {
    return new CDMWrap().open(testDataset);
  }
}
