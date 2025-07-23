/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.server.admin.collection;

import com.coverity.security.Escape;
import com.google.common.eventbus.EventBus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import thredds.core.DataRootManager;
import thredds.core.DatasetManager;
import thredds.featurecollection.CollectionUpdater;
import thredds.featurecollection.FeatureCollectionConfig;
import thredds.featurecollection.FeatureCollectionType;
import thredds.featurecollection.InvDatasetFeatureCollection;
import thredds.inventory.CollectionUpdateEvent;
import thredds.inventory.CollectionUpdateType;
import thredds.server.admin.DebugCommands;
import thredds.server.catalog.FeatureCollectionRef;
import thredds.server.config.TdsContext;
import thredds.servlet.ServletUtil;
import thredds.util.ContentType;
import ucar.nc2.grib.collection.GribCdmIndex;
import ucar.nc2.util.IO;
import ucar.unidata.util.StringUtil2;

@Component
public class CollectionControllerCommon implements InitializingBean {

  private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

  private static final String PATH = "/admin/collection";
  private static final String COLLECTION = "collection";
  static final String SHOW_COLLECTION = "showCollection";
  static final String SHOW = "showStatus";
  static final String SHOW_CSV = "showStatus.csv";
  static final String DOWNLOAD = "download";
  static final String DOWNLOAD_ALL = "downloadAll";
  static final String TRIGGER = "trigger";

  final DebugCommands debugCommands;

  final DataRootManager dataRootManager;

  private final TdsContext tdsContext;

  private final DatasetManager datasetManager;

  private final EventBus eventBus;

  final CollectionUpdater collectionUpdater;

  public CollectionControllerCommon(DebugCommands debugCommands, DataRootManager dataRootManager, TdsContext tdsContext,
      DatasetManager datasetManager, @Qualifier("fcTriggerEventBus") EventBus eventBus,
      CollectionUpdater collectionUpdater) {
    this.debugCommands = debugCommands;
    this.dataRootManager = dataRootManager;
    this.tdsContext = tdsContext;
    this.datasetManager = datasetManager;
    this.eventBus = eventBus;
    this.collectionUpdater = collectionUpdater;
  }

  public void afterPropertiesSet() {
    DebugCommands.Category debugHandler = debugCommands.findCategory("Collections");
    DebugCommands.Action act;

    act = new DebugCommands.Action("showCollection", "Show Collections") {
      public void doAction(DebugCommands.Event e) {
        // get sorted list of collections
        List<FeatureCollectionRef> fcList = dataRootManager.getFeatureCollections();
        fcList.sort(Comparator.comparing(FeatureCollectionRef::getCollectionName));

        for (FeatureCollectionRef fc : fcList) {
          String uriParam = Escape.uriParam(fc.getCollectionName());
          String url = tdsContext.getContextPath() + PATH + "/" + SHOW_COLLECTION + "?" + COLLECTION + "=" + uriParam;
          e.pw.printf("<p/><a href='%s'>%s</a> (%s)%n", url, fc.getCollectionName(), fc.getName());
          FeatureCollectionConfig config = fc.getConfig();
          if (config != null)
            e.pw.printf("%s%n", config.spec);
        }

        String url = tdsContext.getContextPath() + PATH + "/" + SHOW;
        e.pw.printf("<p/><a href='%s'>Show All Collection Status</a>%n", url);

        url = tdsContext.getContextPath() + PATH + "/" + SHOW_CSV;
        e.pw.printf("<p/><a href='%s'>Collection Status CSV</a>%n", url);

        url = tdsContext.getContextPath() + PATH + "/" + DOWNLOAD_ALL;
        e.pw.printf("<p/><a href='%s'>Download All top-level collection indices</a>%n", url);
      }
    };
    debugHandler.addAction(act);

    act = new DebugCommands.Action("sched", "Show FeatureCollection update scheduler") {
      public void doAction(DebugCommands.Event e) {
        org.quartz.Scheduler scheduler = collectionUpdater.getScheduler();
        if (scheduler == null)
          return;

        try {
          e.pw.println(scheduler.getMetaData());

          // enumerate each job group
          for (String group : scheduler.getJobGroupNames()) {
            e.pw.println("Group " + group);

            // enumerate each job in group
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(group))) {
              e.pw.println("  Job " + jobKey.getName());
              e.pw.println("    " + scheduler.getJobDetail(jobKey));
            }

            // enumerate each trigger in group
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.groupEquals(group))) {
              e.pw.println("  Trigger " + triggerKey.getName());
              e.pw.println("    " + scheduler.getTrigger(triggerKey));
            }
          }
        } catch (SchedulerException se) {
          e.pw.println("Error on scheduler " + se.getMessage());
          log.error("Error on scheduler {}", se.getMessage());
        }
      }
    };
    debugHandler.addAction(act);
  }

  ResponseEntity<String> showCollection(String collection) {
    Formatter out = new Formatter();

    FeatureCollectionRef want = dataRootManager.findFeatureCollection(collection);

    HttpStatus status = HttpStatus.OK;
    if (want == null) {
      status = HttpStatus.NOT_FOUND;
      out.format("NOT FOUND");

    } else {
      out.format("<h3>Collection %s</h3>%n%n", Escape.html(collection));
      showFeatureCollection(out, want);

      String uriParam = Escape.uriParam(want.getCollectionName());
      String url =
          tdsContext.getContextPath() + PATH + "/" + TRIGGER + "?" + COLLECTION + "=" + uriParam + "&" + TRIGGER + "=";
      out.format("<p/><a href='%s'>Send 'nocheck' trigger to %s</a>%n", url + CollectionUpdateType.nocheck,
          Escape.html(want.getCollectionName()));
      out.format("<p/><a href='%s'>Send 'test' trigger to %s</a>%n", url + CollectionUpdateType.test,
          Escape.html(want.getCollectionName()));

      String url2 = tdsContext.getContextPath() + PATH + "/" + DOWNLOAD + "?" + COLLECTION + "=" + uriParam;
      out.format("<p/><a href='%s'>Download index file for %s</a>%n", url2, Escape.html(want.getCollectionName()));
    }

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(MediaType.TEXT_HTML);
    return new ResponseEntity<>(out.toString(), responseHeaders, status);
  }

  ResponseEntity<String> showCollectionStatus() {
    Formatter out = new Formatter();

    // get sorted list of collections
    List<FeatureCollectionRef> fcList = dataRootManager.getFeatureCollections();
    fcList.sort(Comparator.comparing(FeatureCollectionRef::getCollectionName));

    for (FeatureCollectionRef fc : fcList) {
      String uriParam = Escape.uriParam(fc.getCollectionName());
      String url = tdsContext.getContextPath() + PATH + "/" + SHOW_COLLECTION + "?" + COLLECTION + "=" + uriParam;
      out.format("<p/><a href='%s'>%s</a> (%s)%n", url, fc.getCollectionName(), fc.getName());
      try {
        InvDatasetFeatureCollection fcd = datasetManager.openFeatureCollection(fc);
        out.format("<pre>%s</pre>%n", fcd.showStatusShort("txt"));
      } catch (IOException e) {
        out.format("<pre> %s</pre>%n", e.getMessage());
      }
    }

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(MediaType.TEXT_HTML);
    return new ResponseEntity<>(out.toString(), responseHeaders, HttpStatus.OK);
  }

  ResponseEntity<String> showCollectionStatusCsv() {
    Formatter out = new Formatter();

    // get sorted list of collections
    List<FeatureCollectionRef> fcList = dataRootManager.getFeatureCollections();
    fcList.sort(Comparator.comparing((FeatureCollectionRef o) -> o.getConfig().type.toString())
        .thenComparing(FeatureCollectionRef::getCollectionName));

    out.format("%s, %s, %s, %s, %s, %s, %s, %s, %s%n", "collection", "ed", "type", "group", "nrecords", "ndups", "%",
        "nmiss", "%");
    for (FeatureCollectionRef fc : fcList) {
      if (fc.getConfig().type != FeatureCollectionType.GRIB1 && fc.getConfig().type != FeatureCollectionType.GRIB2)
        continue;
      try {
        InvDatasetFeatureCollection fcd = datasetManager.openFeatureCollection(fc);
        out.format("%s", fcd.showStatusShort("csv"));
      } catch (IOException ioe) {
        out.format("<pre> %s</pre>%n", ioe.getMessage());
      }
    }

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(MediaType.TEXT_PLAIN);
    return new ResponseEntity<>(out.toString(), responseHeaders, HttpStatus.OK);
  }

  ResponseEntity<String> triggerFeatureCollection(HttpServletRequest req, HttpServletResponse res) {
    Formatter out = new Formatter();

    CollectionUpdateType triggerType = null;
    String triggerTypeS = req.getParameter(TRIGGER);
    try {
      triggerType = CollectionUpdateType.valueOf(triggerTypeS);
    } catch (Throwable t) {
      // noop
    }

    if (triggerType == null) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      out.format(" TRIGGER Type '%s' not legal%n", Escape.html(triggerTypeS));
      return null;
    }

    String collectName = StringUtil2.unescape(req.getParameter(COLLECTION)); // this is the collection name
    FeatureCollectionRef want = dataRootManager.findFeatureCollection(collectName);

    if (want == null) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      out.format("NOT FOUND");

    } else {
      out.format("<h3>Collection %s</h3>%n", Escape.html(collectName));

      if (!want.getConfig().isTrigggerOk()) {
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        out.format(" TRIGGER NOT ENABLED%n");

      } else {
        eventBus.post(new CollectionUpdateEvent(triggerType, collectName, "trigger"));
        // CollectionUpdater.INSTANCE.triggerUpdate(collectName, triggerType);
        out.format(" TRIGGER SENT%n");
      }
    }

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(MediaType.TEXT_HTML);
    return new ResponseEntity<>(out.toString(), responseHeaders, HttpStatus.OK);
  }

  protected void downloadAll(HttpServletRequest req, HttpServletResponse res) throws IOException {
    File tempFile = File.createTempFile("CollectionIndex", ".zip");
    tempFile.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(tempFile.getPath())) {
      ZipOutputStream zout = new ZipOutputStream(fos);
      for (FeatureCollectionRef fc : dataRootManager.getFeatureCollections()) {
        File idxFile = GribCdmIndex.getTopIndexFileFromConfig(fc.getConfig());
        if (idxFile == null)
          continue;
        ZipEntry entry = new ZipEntry(idxFile.getName());
        zout.putNextEntry(entry);
        IO.copyFile(idxFile.getPath(), zout);
        zout.closeEntry();
      }
      zout.close();
    }

    ServletUtil.returnFile(req, res, tempFile, ContentType.binary.toString());
  }

  protected ResponseEntity<String> downloadIndex(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String collectName = StringUtil2.unescape(req.getParameter(COLLECTION)); // this is the collection name
    FeatureCollectionRef want = dataRootManager.findFeatureCollection(collectName);

    if (want == null) {
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.TEXT_PLAIN);
      return new ResponseEntity<>(Escape.html(collectName) + " NOT FOUND", responseHeaders, HttpStatus.NOT_FOUND);
    }

    File idxFile = GribCdmIndex.getTopIndexFileFromConfig(want.getConfig());
    if (idxFile == null) {
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.TEXT_PLAIN);
      return new ResponseEntity<>(Escape.html(collectName) + " NOT FOUND", responseHeaders, HttpStatus.NOT_FOUND);
    }

    ServletUtil.returnFile(req, res, idxFile, ContentType.binary.toString());
    return null;
  }

  private void showFeatureCollection(Formatter out, FeatureCollectionRef fc) {
    FeatureCollectionConfig config = fc.getConfig(); // LOOK
    if (config != null) {
      Formatter f = new Formatter();
      config.show(f);
      out.format("%n<pre>%s%n</pre>", f);
    }

    try {
      InvDatasetFeatureCollection fcd = datasetManager.openFeatureCollection(fc);
      out.format("%n<pre>%n");
      fcd.showStatus(out);
      out.format("%n</pre>%n");
    } catch (IOException ioe) {
      out.format("<pre> %s</pre>%n", ioe.getMessage());
    }
  }
}
