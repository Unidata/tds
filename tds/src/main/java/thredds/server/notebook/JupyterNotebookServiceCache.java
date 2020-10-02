package thredds.server.notebook;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import thredds.client.catalog.Dataset;
import thredds.core.StandardService;
import thredds.server.config.TdsContext;
import thredds.server.viewer.ViewerService;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JupyterNotebookServiceCache {

  static private final Logger logger = LoggerFactory.getLogger(JupyterNotebookServiceCache.class);

  @Autowired
  TdsContext tdsContext;

  @Autowired
  ViewerService viewerService;

  private Set<NotebookMetadata> allNotebooks;

  private Cache<String, Set<NotebookMetadata>> notebookMappingCache;

  public void init(int maxAge, int maxSize) {
    this.allNotebooks = new HashSet<>();
    this.notebookMappingCache =
        CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(maxAge)).maximumSize(maxSize).build();
    buildNotebookList();
    viewerService
        .registerViewers(new JupyterNotebookViewerService(this, tdsContext.getContentRootPathProperty()).getViewers());
  }

  public Set<NotebookMetadata> getMappedNotebooks(Dataset ds) {
    try {
      Set<NotebookMetadata> nbmd = this.notebookMappingCache.get(ds.getID(), () -> {
        return getNotebookMapping(ds);
      });
      return nbmd;
    } catch (Exception e) {
      logger.warn(e.getMessage());
      return null;
    }
  }

  private void buildNotebookList() {

    File notebooksDir = new File(tdsContext.getThreddsDirectory(), "notebooks");
    if (notebooksDir.exists() && notebooksDir.isDirectory()) {

      File[] files = notebooksDir.listFiles(pathname -> pathname.getName().endsWith(".ipynb"));

      for (File notebookFile : files) {
        try {
          NotebookMetadata nb = new NotebookMetadata(notebookFile);
          this.allNotebooks.add(nb);
        } catch (NotebookMetadata.InvalidJupyterNotebookException e) {
          logger.warn(e.getMessage());
          continue;
        } catch (FileNotFoundException e) {
          logger.warn(e.getMessage());
          continue;
        }
      }
    }
  }

  private Set<NotebookMetadata> getNotebookMapping(Dataset ds) {
    Set<NotebookMetadata> matches = new HashSet<>();
    for (NotebookMetadata nbmd : this.allNotebooks) {
      if (nbmd.isValidForDataset(ds)) {
        matches.add(nbmd);
      }
    }
    return matches;
  }

  public Set<NotebookMetadata> getAllNotebooks() {
    return this.allNotebooks;
  }

  // public Map<String, Set<NotebookMetadata>> getNotebookMapping() {
  // return this.notebookMappingCache.asMap();
  // }
}
