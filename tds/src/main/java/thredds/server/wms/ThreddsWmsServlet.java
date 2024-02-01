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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.util.Formatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ucar.nc2.dataset.NetcdfDatasets;
import uk.ac.rdg.resc.edal.graphics.exceptions.EdalLayerNotFoundException;
import uk.ac.rdg.resc.edal.wms.RequestParams;
import uk.ac.rdg.resc.edal.wms.WmsCatalogue;
import uk.ac.rdg.resc.edal.wms.WmsServlet;
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

  private static final Cache<String, CachedWmsCatalogue> catalogueCache =
      CacheBuilder.newBuilder().maximumSize(200).recordStats().build();
  private static int cacheLoads = 0;


  @Override
  @RequestMapping(value = "**", method = {RequestMethod.GET})
  protected void dispatchWmsRequest(String request, RequestParams params, HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, WmsCatalogue wmsCatalogue) throws Exception {
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
    try (NetcdfDataset ncd = acquireNetcdfDataset(httpServletRequest, httpServletResponse, tdsDataset.getPath())) {
      ThreddsWmsCatalogue catalogue = acquireCatalogue(ncd, tdsDataset.getPath());

      /*
       * Now that we've got a WmsCatalogue, we can pass this request to the
       * super implementation which will handle things from here.
       */
      super.dispatchWmsRequest(request, params, httpServletRequest, httpServletResponse, catalogue);
    }
  }

  private ThreddsWmsCatalogue acquireCatalogue(NetcdfDataset ncd, String tdsDatasetPath) throws IOException {
    if (ncd.getLocation() == null) {
      throw new EdalLayerNotFoundException("The requested dataset is not available on this server");
    }

    final CachedWmsCatalogue cachedWmsCatalogue = catalogueCache.getIfPresent(tdsDatasetPath);
    final long lastModified = ncd.getLastModified();

    if (cachedWmsCatalogue != null && cachedWmsCatalogue.lastModified >= lastModified) {
      // Must update NetcdfDataset to ensure file resources are reacquired, as this has been closed.
      // But we don't need to recreate the ThreddsWmsCatalogue as it is up-to-date according to the last modified
      cachedWmsCatalogue.wmsCatalogue.setNetcdfDataset(ncd);
      return cachedWmsCatalogue.wmsCatalogue;
    } else {
      // Create and put/ replace in cache
      ThreddsWmsCatalogue threddsWmsCatalogue = new ThreddsWmsCatalogue(ncd, tdsDatasetPath);
      catalogueCache.put(tdsDatasetPath, new CachedWmsCatalogue(threddsWmsCatalogue, lastModified));
      return threddsWmsCatalogue;
    }
  }

  private static NetcdfDataset acquireNetcdfDataset(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, String tdsDatasetPath) throws IOException {
    NetcdfFile ncf = TdsRequestedDataset.getNetcdfFile(httpServletRequest, httpServletResponse, tdsDatasetPath);
    if (TdsRequestedDataset.useNetcdfJavaBuilders()) {
      return NetcdfDatasets.enhance(ncf, NetcdfDataset.getDefaultEnhanceMode(), null);
    } else {
      return NetcdfDataset.wrap(ncf, NetcdfDataset.getDefaultEnhanceMode());
    }
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
