/*
 * Access and use of this software shall impose the following
 * obligations and understandings on the user. The user is granted the
 * right, without any fee or cost, to use, copy, modify, alter, enhance
 * and distribute this software, and any derivative works thereof, and
 * its supporting documentation for any purpose whatsoever, provided
 * that this entire notice appears in all copies of the software,
 * derivative works and supporting documentation. Further, the user
 * agrees to credit NOAA/NGDC in any publications that result from
 * the use of this software or in any product that includes this
 * software. The names NOAA/NGDC, however, may not be used
 * in any advertising or publicity to endorse or promote any products
 * or commercial entity unless specific written permission is obtained
 * from NOAA/NGDC. The user also understands that NOAA/NGDC
 * is not obligated to provide the user with any support, consulting,
 * training or assistance of any kind with regard to the use, operation
 * and performance of this software nor to provide the user with any
 * updates, revisions, new versions or "bug fixes".
 *
 * THIS SOFTWARE IS PROVIDED BY NOAA/NGDC "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL NOAA/NGDC BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
 * CONTRACT, NEGLIGENCE OR OTHER TORTUOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package thredds.server.metadata.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import thredds.core.TdsRequestedDataset;
import thredds.servlet.ServletUtil;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;

/**
 * DatasetHandlerAdapter
 * 
 * @author: dneufeld
 *          Date: Jul 19, 2010
 */
public class DatasetHandlerAdapter {
  static private org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger(DatasetHandlerAdapter.class);

  /**
   * Open a NetcdfDataset based on the incoming url request.
   *
   * @param req incoming url request
   * @param res outgoing web based response
   * @return dataset a NetcdfDataset as specified in the request
   */
  public static NetcdfDataset openDataset(final HttpServletRequest req, final HttpServletResponse res,
      String datasetPath) throws Exception {

    NetcdfFile netcdfFile = null;
    NetcdfDataset dataset = null;
    // String datasetPath = req.getPathInfo();

    if (datasetPath == null) { // passing in a dataset URL, presumably
      // opendap
      datasetPath = ServletUtil.getParameterIgnoreCase(req, "dataset");
      _log.debug("opendap datasetPath: " + datasetPath);
      try {
        dataset = NetcdfDatasets.openDataset(datasetPath);
      } catch (IOException e) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        _log.debug("Failed to open dataset <" + datasetPath + ">: " + e.getMessage());
      }
    } else {

      try {
        netcdfFile = TdsRequestedDataset.getNetcdfFile(req, res, datasetPath);

        _log.debug("datasetPath: " + datasetPath + " netcdfFile location: " + netcdfFile.getLocation());

        dataset = NetcdfDatasets.enhance(netcdfFile, NetcdfDataset.getDefaultEnhanceMode(), null);

      } catch (FileNotFoundException fnfe) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        _log.debug("Failed to get NetcdfFile <" + datasetPath + ">: " + fnfe.getMessage(), fnfe);
      } catch (IOException ioe) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        _log.debug("Failed to get NetcdfFile <" + datasetPath + "> using " + netcdfFile.getLocation() + ": "
            + ioe.getMessage(), ioe);
      }
    }
    return dataset;

  }

  /**
   * Close a NetcdfDataset.
   *
   * @param dataset the NetcdfDataset to close
   */
  public static void closeDataset(final NetcdfDataset dataset) {
    if (dataset == null)
      return;
    try {
      dataset.close();
    } catch (IOException ioe) {
      _log.warn("Failed to properly close the dataset", ioe);
    }
  }
}
