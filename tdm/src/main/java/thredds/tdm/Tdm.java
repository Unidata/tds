/*
 * Copyright (c) 1998-2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.tdm;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import thredds.featurecollection.CollectionUpdater;
import thredds.featurecollection.FeatureCollectionConfig;
import thredds.featurecollection.FeatureCollectionType;
import thredds.inventory.CollectionUpdateEvent;
import thredds.inventory.CollectionUpdateType;
import thredds.tdm.TriggerableServer.ServerBuilder;
import thredds.util.LocalApiSigner;
import thredds.util.ThreddsConfigReader;
import ucar.httpservices.HTTPException;
import ucar.httpservices.HTTPFactory;
import ucar.httpservices.HTTPMethod;
import ucar.httpservices.HTTPSession;
import ucar.nc2.constants.CDM;
import ucar.nc2.grib.GribIndexCache;
import ucar.nc2.grib.collection.GribCdmIndex;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.util.AliasTranslator;
import ucar.nc2.util.DiskCache2;
import ucar.unidata.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * THREDDS Data Manager.
 * Currently only manages GRIB Collection indices.
 *
 * @author caron
 * @since 12/13/13
 */
public class Tdm {
  private static final org.slf4j.Logger tdmLogger = org.slf4j.LoggerFactory.getLogger(Tdm.class);
  private static final org.slf4j.Logger detailLogger = org.slf4j.LoggerFactory.getLogger("tdmDetail");
  private static final boolean debug = false;
  private static final boolean debugOpenFiles = false;
  private static final boolean debugTasks = true;
  private static final String localApiKey = UUID.randomUUID().toString();
  private static final LocalApiSigner localApiSigner = new LocalApiSigner(localApiKey);

  private EventBus eventBus;

  private CollectionUpdater collectionUpdater;

  private Path contentDir;
  private Path contentThreddsDir;
  private Path contentTdmDir;
  private Path threddsConfig;

  private String user, pass;
  private boolean sendTriggers;
  private String[] serverNames;
  private List<TriggerableServer> servers;

  private java.util.concurrent.ExecutorService executor;
  private Resource catalog;
  private boolean showOnly = false; // if true, just show dirs and exit
  private boolean forceOnStartup = false; // if true, just show dirs and exit

  List<Resource> catalogRoots = new ArrayList<>();

  public void setContentDir(String contentDir) throws IOException {
    this.contentDir = Paths.get(contentDir);
    System.out.printf("contentDir=%s%n", contentDir);
    contentThreddsDir = this.contentDir.resolve("thredds");
    threddsConfig = contentThreddsDir.resolve("threddsConfig.xml");
    contentTdmDir = this.contentDir.resolve("tdm");
    Path LocalApiKeyFile = contentTdmDir.resolve("localapi.key");

    // remove previous ID file
    if (Files.exists(LocalApiKeyFile)) {
      LocalApiKeyFile.toFile().delete();
    }

    try {
      Files.writeString(LocalApiKeyFile, localApiKey, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    } catch (IOException e) {
      System.err.printf("Error writing TDM localApi key file: %s.%n", e);
      System.err.println("Any triggers to localhost will fail.");
    }
  }

  public void setShowOnly(boolean showOnly) {
    this.showOnly = showOnly;
  }

  public void setNThreads(int n) {
    executor = Executors.newFixedThreadPool(n);
    tdmLogger.info(" TDM nthreads= {}", n);
  }

  public void setForceOnStartup(boolean forceOnStartup) {
    this.forceOnStartup = forceOnStartup;
  }

  // spring beaned
  public void setExecutor(ExecutorService executor) {
    this.executor = executor;
  }

  void setUpdater(CollectionUpdater collectionUpdater, EventBus eventBus) {
    this.collectionUpdater = collectionUpdater;
    this.eventBus = eventBus;
  }


  public void setCatalog(String catName) throws IOException {
    this.catalog = new FileSystemResource(catName);
    System.out.printf("catalog=%s%n", catalog.getFile().getPath());
  }

  public void setServerNames(String[] serverNames) {
    this.serverNames = serverNames;
  }

  public void initServers() {
    if (serverNames == null) {
      servers = new ArrayList<>(); // empty list
      return;
    }

    AtomicBoolean error = new AtomicBoolean(false);
    this.servers = Arrays.stream(this.serverNames).map(name -> {
      try {
        return ServerBuilder.create(name).user(user).pass(pass).build();
      } catch (HTTPException | IllegalArgumentException e) {
        System.err.printf("Error creating server %s: %s %n", name, e.getMessage());
        error.set(true);
        return null;
      }
    }).toList();
    if (error.get()) {
      System.exit(1);
    }
  }

  boolean init() throws HTTPException {
    initServers();

    System.setProperty("tds.log.dir", contentTdmDir.toString());

    if (!Files.exists(threddsConfig)) {
      tdmLogger.error("config file {} does not exist, set -Dtds.content.root.path=<dir>", threddsConfig);
      System.out.printf("threddsConfig does not exist=%s%n", threddsConfig);
      return false;
    }
    ThreddsConfigReader reader = new ThreddsConfigReader(threddsConfig.toString(), tdmLogger);

    for (String location : reader.getRootList("catalogRoot")) {
      Resource r = new FileSystemResource(contentThreddsDir.toString() + "/" + location);
      catalogRoots.add(r);
    }

    /* 4.3.15: grib index file placement, using DiskCache2 */
    String gribIndexDir = reader.get("GribIndex.dir", new File(contentThreddsDir.toString(), "cache/grib/").getPath());
    Boolean gribIndexAlwaysUse = reader.getBoolean("GribIndex.alwaysUse", false);
    Boolean gribIndexNeverUse = reader.getBoolean("GribIndex.neverUse", false);
    String gribIndexPolicy = reader.get("GribIndex.policy", null);
    DiskCache2 gribCache = gribIndexNeverUse ? DiskCache2.getNoop() : new DiskCache2(gribIndexDir, false, -1, -1);
    gribCache.setPolicy(gribIndexPolicy);
    gribCache.setAlwaysUseCache(gribIndexAlwaysUse);
    gribCache.setNeverUseCache(gribIndexNeverUse);
    GribIndexCache.setDiskCache2(gribCache);
    tdmLogger.info("TDM initialized {}", gribCache);

    return true;
  }

  void start() throws IOException {
    System.out.printf("Tdm startup at %s%n", new Date());
    collectionUpdater.setTdm(true);
    eventBus.register(this);

    List<FeatureCollectionConfig> fcList = new ArrayList<>();
    CatalogConfigReader reader = new CatalogConfigReader(contentThreddsDir, catalog);
    fcList.addAll(reader.getFcList());

    // do the catalogRoots
    for (Resource catr : catalogRoots) {
      CatalogConfigReader r = new CatalogConfigReader(contentThreddsDir, catr);
      fcList.addAll(r.getFcList());
    }

    if (showOnly) {
      List<String> result = new ArrayList<>();
      for (FeatureCollectionConfig config : fcList) {
        result.add(config.collectionName);
      }
      Collections.sort(result);

      System.out.printf("%nFeature Collection names:%n");
      for (String name : result)
        System.out.printf(" %s%n", name);

      System.out.printf("%nTriggers:%n");
      for (String name : result)
        System.out.printf(" trigger?%s%s%n", TriggerableServer.PARTIAL_TRIGGER_QUERY, name);

      executor.shutdown();
      collectionUpdater.shutdown();
      return;
    }

    for (FeatureCollectionConfig config : fcList) {
      if (config.type != FeatureCollectionType.GRIB1 && config.type != FeatureCollectionType.GRIB2)
        continue;
      System.out.printf("FeatureCollection %s scheduled %n", config.collectionName);

      if (forceOnStartup) // on startup, force rewrite of indexes
        config.tdmConfig.startupType = CollectionUpdateType.always;

      detailLogger.info("FeatureCollection config={}", config);

      // now wire for events
      fcMap.put(config.getCollectionName(), new Listener(config));
      collectionUpdater.scheduleTasks(config, null);
    }
  }

  // called by eventBus
  @Subscribe
  public void processEvent(CollectionUpdateEvent event) {
    Listener fc = fcMap.get(event.getCollectionName());
    if (fc == null) {
      tdmLogger.error("Unknown collection name from event bus {}", event);
      return;
    }
    fc.processEvent(event.getType());
  }

  Map<String, Listener> fcMap = new HashMap<>();

  // these objects receive events from quartz schedular via the EventBus
  // one listener for each fc.
  private class Listener {
    FeatureCollectionConfig config;
    AtomicBoolean inUse = new AtomicBoolean(false);
    // org.slf4j.Logger logger;

    private Listener(FeatureCollectionConfig config) {
      this.config = config;
    }

    public void processEvent(CollectionUpdateType event) {
      if (!inUse.compareAndSet(false, true)) {
        detailLogger.debug("Tdm event type '{}' already in use on {}", event, config.getCollectionName());
        return; // if already working, skip another execution
      }
      detailLogger.debug("Tdm event type '{}' scheduled for {}", event, config.getCollectionName());
      executor.execute(new IndexTask(config, this, event));
    }
  }

  private AtomicInteger indexTaskCount = new AtomicInteger();

  private class IndexTask implements Runnable {
    String name;
    FeatureCollectionConfig config;
    CollectionUpdateType updateType;
    Listener liz;

    private IndexTask(FeatureCollectionConfig config, Listener liz, CollectionUpdateType updateType) {
      this.name = config.collectionName;
      this.config = config;
      this.liz = liz;
      this.updateType = updateType;
    }

    @Override
    public void run() {
      try {
        // log.info("Tdm call GribCdmIndex.updateGribCollection "+config.collectionName);
        if (debug)
          System.out.printf("---------------------%nIndexTask updateGribCollection %s%n", config.collectionName);
        long start = System.currentTimeMillis();
        int taskNo = indexTaskCount.getAndIncrement();
        tdmLogger.debug("{} start {}", taskNo, config.collectionName);
        boolean changed = GribCdmIndex.updateGribCollection(config, updateType, null);

        long took = System.currentTimeMillis() - start;
        tdmLogger.debug("{} done {}: changed {} took {} ms", taskNo, config.collectionName, changed, took);
        System.out.printf("%s: %s changed %s took %d msecs%n", CalendarDate.present(), config.collectionName, changed,
            took);

        if (debugTasks) {
          System.out.printf("executor=%s%n", executor);
        }

        if (changed && config.tdmConfig.triggerOk && sendTriggers) { // send a trigger if enabled
          sendTriggers(name);
        }
      } catch (Throwable e) {
        tdmLogger.error("Tdm.IndexTask {}: {}", name, e);
      } finally {
        // tell liz that task is done
        if (!liz.inUse.getAndSet(false))
          tdmLogger.warn("Listener InUse should have been set");
      }

      if (debugOpenFiles) {
        List<String> openFiles = RandomAccessFile.getOpenFiles();
        if (openFiles.size() > 0) {
          System.out.printf("Open Files%n");
          for (String filename : openFiles) {
            System.out.printf("  %s%n", filename);
          }
          System.out.printf("End Open Files%n");
        }
      }

    }

    private void sendTriggers(String collectionName) {
      for (TriggerableServer server : servers) {
        String triggerEndpoint = server.getCollectionTrigger(collectionName);

        try (HTTPMethod m = HTTPFactory.Get(server.session, triggerEndpoint)) {
          detailLogger.debug("send trigger to {}", triggerEndpoint);
          if (server.local) {
            m.setRequestHeader(LocalApiSigner.LOCAL_API_SIGNATURE_HEADER_V1,
                localApiSigner.generateSignatureGet(triggerEndpoint));
          }
          int status = m.execute();

          if (status != 200) {
            tdmLogger.warn("FAIL send trigger to {} status = {}", triggerEndpoint, status);
            detailLogger.warn("FAIL send trigger to {} status = {}", triggerEndpoint, status);
          } else {
            int taskNo = indexTaskCount.get();
            tdmLogger.info("{} trigger sent {} status = {}", taskNo, triggerEndpoint, status);
            detailLogger.debug("return from {} status = {}", triggerEndpoint, status);
          }

        } catch (HTTPException e) {
          Throwable cause = e.getCause();
          if (cause instanceof ConnectException) {
            detailLogger.warn("server {} not running", server.name);
          } else {
            tdmLogger.error("FAIL send trigger to {} failed: {}", triggerEndpoint, cause);
            detailLogger.error("FAIL send trigger to {} failed: {}", triggerEndpoint, cause);
          }
        }
      }
    }
  }


  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /*
   * System.out.printf("usage: <Java> <Java_OPTS> -Dtds.content.root.path=<contentDir> [-catalog <cat>] [-tds
   * <tdsServer>]
   * [-cred <user:passwd>] [-showOnly] [-forceOnStartup]%n");
   * System.out.
   * printf("example: /opt/jdk/bin/java -Xmx3g -Dtds.content.root.path=/my/content -jar tdm-4.5.jar -tds http://thredds-dev.unidata.ucar.edu/%n"
   * );
   * // /opt/jdk/bin/java -d64 -Xmx3g -jar -Dtds.content.root.path=/opt/tds-dev/content tdm-4.5.jar -cred tdm:trigger
   * -tds "http://thredds-dev.unidata.ucar.edu/"
   * 
   */
  private static class CommandLine {
    @Parameter(names = {"-catalog"}, description = "name a specific catalog (relative to content dir)",
        required = false)
    public String catalog;

    @Parameter(names = {"-cred"}, description = "tds credentials (user:password)", required = false)
    public String cred;

    @Parameter(names = {"-forceOnStartup"}, description = "force read all collections on startup (override config)",
        required = false)
    public boolean forceOnStartup;

    @Parameter(names = {"-nthreads"}, description = "number of threads", required = false)
    public int nthreads = 1;

    @Parameter(names = {"-showOnly"}, description = "show collections and exit", required = false)
    public boolean showOnly;

    @Parameter(names = {"-tds"}, description = "list of tds programs to send triggers to", required = false)
    public String tds;

    @Parameter(names = {"-h", "--help"}, description = "Display this help and exit", help = true)
    public boolean help = false;

    private final JCommander jc;

    public CommandLine(String progName, String[] args) throws ParameterException {
      this.jc = new JCommander(this);
      this.jc.parse(args); // Parses args and uses them to initialize *this*.
      this.jc.setProgramName(progName); // Displayed in the usage information.
    }

    public void printUsage() {
      jc.usage();
      System.out.printf(
          "example: /opt/jdk/bin/java -Xmx3g -Dtds.content.root.path=/my/content -jar tdm-5.0.jar -tds http://thredds-dev.unidata.ucar.edu/ -cred tdm:trigger %n");
    }

  }

  public static void main(String[] args) throws IOException, InterruptedException {
    try (FileSystemXmlApplicationContext springContext =
        new FileSystemXmlApplicationContext("classpath:resources/application-config.xml")) {
      Tdm app = (Tdm) springContext.getBean("TDM");

      Map<String, String> aliases = (Map<String, String>) springContext.getBean("dataRootLocationAliasExpanders");
      for (Map.Entry<String, String> entry : aliases.entrySet())
        AliasTranslator.addAlias(entry.getKey(), entry.getValue());

      EventBus eventBus = (EventBus) springContext.getBean("fcTriggerEventBus");
      CollectionUpdater collectionUpdater = (CollectionUpdater) springContext.getBean("collectionUpdater");
      collectionUpdater.setEventBus(eventBus); // Autowiring not working
      app.setUpdater(collectionUpdater, eventBus);

      String contentDir = System.getProperty("tds.content.root.path");
      if (contentDir == null)
        contentDir = "../content";
      app.setContentDir(contentDir);

      // RandomAccessFile.setDebugLeaks(true);
      HTTPSession.setGlobalUserAgent("TDM v5.0");
      // GribCollection.getDiskCache2().setNeverUseCache(true);

      String progName = Tdm.class.getName();
      boolean needPassword = false;
      try {
        CommandLine cmdLine = new CommandLine(progName, args);
        if (cmdLine.help) {
          cmdLine.printUsage();
          return;
        }

        if (cmdLine.catalog != null) {
          app.setCatalog(cmdLine.catalog);
        } else {

          app.setCatalog(app.contentThreddsDir.resolve("catalog.xml").toString());
        }

        if (cmdLine.cred != null) { // LOOK could be http://user:password@server
          String[] split = cmdLine.cred.split(":");
          app.user = split[0];
          app.pass = split[1];
          app.sendTriggers = true;
        }

        if (cmdLine.forceOnStartup)
          app.setForceOnStartup(true);

        if (cmdLine.nthreads != 0)
          app.setNThreads(cmdLine.nthreads);

        if (cmdLine.showOnly)
          app.setShowOnly(true);

        if (cmdLine.tds != null) {
          if (cmdLine.tds.equalsIgnoreCase("none")) {
            app.setServerNames(null);
            app.sendTriggers = false;

          } else {
            String[] tdss = cmdLine.tds.split(","); // comma separated
            app.setServerNames(tdss);
            // do we need to ask for a password because a non-local TDS is in the list of TDSs to trigger?
            needPassword = Arrays.stream(tdss).anyMatch(server -> !server.contains("://localhost"));
            app.sendTriggers = true;
          }
        }

      } catch (ParameterException e) {
        System.err.println(e.getMessage());
        System.err.printf("Try \"%s --help\" for more information.%n", progName);
      }

      if (!app.showOnly && app.pass == null && app.sendTriggers && needPassword) {
        Scanner scanner = new Scanner(System.in, CDM.UTF8);
        String passw;
        while (true) {
          System.out.printf("%nEnter password for tds trigger: ");
          passw = scanner.nextLine();
          System.out.printf("%nPassword = '%s' OK (Y/N)?", passw);
          String ok = scanner.nextLine();
          if (ok.equalsIgnoreCase("Y"))
            break;
        }
        if (passw != null) {
          app.pass = passw;
          app.user = "tdm";
        } else {
          app.sendTriggers = false;
        }
      }

      if (app.init()) {
        app.start();
      } else {
        System.out.printf("%nEXIT DUE TO ERRORS");
        System.exit(1);
      }
    }
  }

}
