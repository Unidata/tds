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
import com.google.common.cache.RemovalListener;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.io.IOException;
import java.util.Collections;
import java.util.Formatter;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ucar.nc2.dataset.NetcdfDatasets;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.exceptions.EdalLayerNotFoundException;
import uk.ac.rdg.resc.edal.wms.RequestParams;
import uk.ac.rdg.resc.edal.wms.WmsCatalogue;
import uk.ac.rdg.resc.edal.wms.WmsServlet;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
  private static final Logger logger = LoggerFactory.getLogger(ThreddsWmsServlet.class);

  private static final Map<String, String> defaultStyles = Collections.singletonMap("styles", "default");

  private static class CachedWmsCatalogue {
    public final ThreddsWmsCatalogue wmsCatalogue;
    public final long lastModified;

    public CachedWmsCatalogue(ThreddsWmsCatalogue wmsCatalogue, long lastModified) {
      this.wmsCatalogue = wmsCatalogue;
      this.lastModified = lastModified;
    }
  }

  private static final RemovalListener<String, CachedWmsCatalogue> removalListener = notification -> {
    try {
      notification.getValue().wmsCatalogue.close();
    } catch (IOException e) {
      logger.warn("Could not close {}, exception = {}", notification.getKey(), e);
    }
  };

  private static final Cache<String, CachedWmsCatalogue> catalogueCache =
      CacheBuilder.newBuilder().maximumSize(100).removalListener(removalListener).recordStats().build();

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
    ThreddsWmsCatalogue catalogue = acquireCatalogue(httpServletRequest, httpServletResponse, tdsDataset.getPath());

    // set default style if needed
    if (request.equals("GetMap") && params.getString("styles", "").isEmpty()) {
      params = params.mergeParameters(defaultStyles);
    }

    /*
     * Now that we've got a WmsCatalogue, we can pass this request to the
     * super implementation which will handle things from here.
     */
    super.dispatchWmsRequest(request, params, httpServletRequest, httpServletResponse, catalogue);
  }

  private ThreddsWmsCatalogue acquireCatalogue(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, String tdsDatasetPath) throws IOException {

    invalidateIfOutdated(tdsDatasetPath);

    try {
      CachedWmsCatalogue catalogue = catalogueCache.get(tdsDatasetPath, () -> {
        NetcdfDataset ncd = acquireNetcdfDataset(httpServletRequest, httpServletResponse, tdsDatasetPath);
        if (ncd.getLocation() == null) {
          ncd.close();
          throw new EdalLayerNotFoundException("The requested dataset is not available on this server");
        }

        try {
          ThreddsWmsCatalogue threddsWmsCatalogue = new ThreddsWmsCatalogue(ncd, tdsDatasetPath);
          return new CachedWmsCatalogue(threddsWmsCatalogue, ncd.getLastModified());
        } catch (EdalException e) {
          ncd.close();
          throw e;
        }
      });

      return catalogue.wmsCatalogue;
    } catch (ExecutionException e) {
      throw new IOException(e);
    } catch (UncheckedExecutionException e) {
      if (e.getCause() instanceof EdalException) {
        throw (EdalException) e.getCause();
      } else {
        throw e;
      }
    }
  }

  private static void invalidateIfOutdated(String tdsDatasetPath) {
    final CachedWmsCatalogue cachedWmsCatalogue = catalogueCache.getIfPresent(tdsDatasetPath);

    if (cachedWmsCatalogue != null
        && cachedWmsCatalogue.lastModified != cachedWmsCatalogue.wmsCatalogue.getLastModified()) {
      catalogueCache.invalidate(tdsDatasetPath);
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

  public static void showCache(Formatter formatter) {
    formatter.format("%nWmsCache:%n");
    formatter.format("numberOfEntries=%d, ", getNumberOfEntries());
    formatter.format("loads=%d, ", getCacheLoads());
    formatter.format("evictionCount=%d ", catalogueCache.stats().evictionCount());
    formatter.format("%nentries:%n");
    for (Map.Entry<String, CachedWmsCatalogue> entry : catalogueCache.asMap().entrySet()) {
      formatter.format("  %s%n", entry.getKey());
    }
  }

  public static void resetCache() {
    catalogueCache.invalidateAll();
  }

  // package private for testing
  static boolean containsCachedCatalogue(String tdsDatasetPath) {
    return catalogueCache.asMap().containsKey(tdsDatasetPath);
  }

  static long getNumberOfEntries() {
    return catalogueCache.size();
  }

  static long getCacheLoads() {
    return catalogueCache.stats().loadCount();
  }
}
