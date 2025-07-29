/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.admin.collection;

import com.google.common.eventbus.EventBus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import thredds.core.DataRootManager;
import thredds.core.DatasetManager;
import thredds.featurecollection.CollectionUpdater;
import thredds.server.admin.DebugCommands;
import thredds.server.config.TdsContext;

/**
 * Allow local triggers for rereading Feature collections
 */
@Controller
@RequestMapping(value = {"/local/collection"})
public class LocalCollectionController extends CollectionControllerCommon {

  public LocalCollectionController(DebugCommands debugCommands, DataRootManager dataRootManager, TdsContext tdsContext,
      DatasetManager datasetManager, EventBus eventBus, CollectionUpdater collectionUpdater) {
    super(debugCommands, dataRootManager, tdsContext, datasetManager, eventBus, collectionUpdater);
  }

  @RequestMapping(value = "/" + SHOW_COLLECTION, method = RequestMethod.GET)
  protected ResponseEntity<String> showCollectionLocal(@RequestParam String collection) {
    return super.showCollection(collection);
  }

  @RequestMapping(value = {"/" + SHOW})
  protected ResponseEntity<String> showCollectionStatusLocal() {
    return super.showCollectionStatus();
  }

  @RequestMapping(value = {"/" + SHOW_CSV})
  protected ResponseEntity<String> showCollectionStatusCsvLocal() {
    return super.showCollectionStatusCsv();
  }

  @RequestMapping(value = {"/" + TRIGGER}) // LOOK should require collection and trigger type params
  protected ResponseEntity<String> triggerFeatureCollectionLocal(HttpServletRequest req, HttpServletResponse res) {
    return super.triggerFeatureCollection(req, res);
  }

  @RequestMapping(value = {"/" + DOWNLOAD_ALL})
  protected void downloadAllLocal(HttpServletRequest req, HttpServletResponse res) throws IOException {
    super.downloadAll(req, res);
  }

  @RequestMapping(value = {"/" + DOWNLOAD})
  protected ResponseEntity<String> downloadIndexLocal(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    return super.downloadIndex(req, res);
  }
}
