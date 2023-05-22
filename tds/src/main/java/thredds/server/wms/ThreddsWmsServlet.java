/*
 * Copyright (c) 2015 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 * authors or contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package thredds.server.wms;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ucar.nc2.dataset.NetcdfDatasets;
import uk.ac.rdg.resc.edal.graphics.exceptions.EdalLayerNotFoundException;
import uk.ac.rdg.resc.edal.wms.RequestParams;
import uk.ac.rdg.resc.edal.wms.WmsCatalogue;
import uk.ac.rdg.resc.edal.wms.WmsServlet;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import thredds.core.TdsRequestedDataset;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * An example {@link WmsServlet} which uses the THREDDS catalogue to supply
 * data.
 * 
 * This is example is well commented and demonstrates how to properly integrate
 * the EDAL WMS into the THREDDS catalogue. It doesn't show how to implement
 * caching, or WMS-specific configuration, but these things are recommended in
 * the final version.
 *
 * @author Guy Griffiths
 */
@SuppressWarnings("serial")
@Controller
@RequestMapping("/wms")
public class ThreddsWmsServlet extends WmsServlet {

  private static class CachedWmsCatalogue {
    public final ThreddsWmsCatalogue wmsCatalogue;
    public final long lastModified;

    public CachedWmsCatalogue(ThreddsWmsCatalogue wmsCatalogue, long lastModified) {
      this.wmsCatalogue = wmsCatalogue;
      this.lastModified = lastModified;
    }
  }

  private static final Map<String, CachedWmsCatalogue> catalogueCache = new HashMap<>();

  static void resetCache() {
    catalogueCache.clear();
  }

  @Override
  @RequestMapping(value = "**", method = {RequestMethod.GET})
  protected void dispatchWmsRequest(String request, RequestParams params, HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, WmsCatalogue catalogue) throws Exception {
    /*
     * The super implementation of this gets called with a servlet-wide
     * catalogue, which "should" have been injected with the
     * WmsServlet.setCatalogue() method. Since we want one catalogue per
     * dataset, we never call setCatalogue(), but instead we generate a
     * WmsCatalogue (or more likely in a final version, retrieve a cached
     * one) on each request, and pass that to the super implementation.
     */

    /*
     * Map the request to a file path
     */
    // Look - is setting this to null the right thing to do??
    String removePrefix = null;
    TdsRequestedDataset tdsDataset = new TdsRequestedDataset(httpServletRequest, removePrefix);
    if (useCachedCatalogue(tdsDataset.getPath())) {
      catalogue = catalogueCache.get(tdsDataset.getPath()).wmsCatalogue;
    } else {
      NetcdfFile ncf = TdsRequestedDataset.getNetcdfFile(httpServletRequest, httpServletResponse, tdsDataset.getPath());
      NetcdfDataset ncd;
      if (TdsRequestedDataset.useNetcdfJavaBuilders()) {
        ncd = NetcdfDatasets.enhance(ncf, NetcdfDataset.getDefaultEnhanceMode(), null);
      } else {
        ncd = NetcdfDataset.wrap(ncf, NetcdfDataset.getDefaultEnhanceMode());
      }

      String netcdfFilePath = ncf.getLocation();

      /*
       * Generate a new catalogue for the given dataset
       * 
       * In the full system, we should keep a cache of these
       * ThreddsWmsCatalogues, but in this example we just create each new one
       * on the fly.
       * 
       * If a feature cache is required on the WMS (a Good Idea), I recommend
       * a single cache in this servlet which gets passed to each WmsCatalogue
       * upon construction (i.e. HERE). That's a TDS implementation detail
       * though, hence not in this example.
       */
      if (netcdfFilePath == null) {
        throw new EdalLayerNotFoundException("The requested dataset is not available on this server");
      }
      catalogue = new ThreddsWmsCatalogue(ncd, tdsDataset.getPath());
      final CachedWmsCatalogue cachedWmsCatalogue =
          new CachedWmsCatalogue((ThreddsWmsCatalogue) catalogue, ncd.getLastModified());
      catalogueCache.put(tdsDataset.getPath(), cachedWmsCatalogue);
    }

    /*
     * Now that we've got a WmsCatalogue, we can pass this request to the
     * super implementation which will handle things from here.
     */
    super.dispatchWmsRequest(request, params, httpServletRequest, httpServletResponse, catalogue);
  }

  // package private for testing
  static boolean useCachedCatalogue(String tdsDatasetPath) {
    if (containsCachedCatalogue(tdsDatasetPath)) {
      // This date last modified will be updated e.g. in the case of an aggregation with a recheckEvery
      final long netcdfDatasetLastModified = catalogueCache.get(tdsDatasetPath).wmsCatalogue.getLastModified();
      final long cacheLastModified = catalogueCache.get(tdsDatasetPath).lastModified;
      return cacheLastModified >= netcdfDatasetLastModified;
    }
    return false;
  }

  // package private for testing
  static boolean containsCachedCatalogue(String tdsDatasetPath) {
    return catalogueCache.containsKey(tdsDatasetPath);
  }
}
