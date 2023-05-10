package thredds.server.notebook;

import org.json.*;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class NotebookMetadata {

  private String filename;

  private AcceptedDatasetTypes acceptedDatasetTypes;

  private String description;

  private JSONObject params;

  public NotebookMetadata(File notebookFile) throws InvalidJupyterNotebookException, FileNotFoundException {
    if (!notebookFile.exists()) {
      throw new FileNotFoundException(notebookFile.getName());
    }

    JSONObject jobj = parseFile(notebookFile);
    if (jobj == null) {
      throw new InvalidJupyterNotebookException(
          String.format("Notebook %s could not be parsed", notebookFile.getName()));
    }

    this.filename = notebookFile.getName();
    this.description = tryGetStringFromJSON(NotebookMetadataKeys.description.key, jobj);
    this.acceptedDatasetTypes = new AcceptedDatasetTypes(jobj);
    this.params = new JSONObject().put("filename", filename).put("description", description);
  }

  public boolean isValidForDataset(Dataset ds) {
    return acceptedDatasetTypes.acceptsDataset(ds);
  }

  public JSONObject getParams() {
    return params;
  }

  public String getFilename() {
    return filename;
  }

  public String getDescription() {
    return description;
  }

  private static JSONObject parseFile(File notebookFile) {
    InputStream is;
    try {
      is = new FileInputStream(notebookFile);
    } catch (FileNotFoundException e) {
      return null;
    }

    JSONTokener tokener = new JSONTokener(is);
    JSONObject jobj;
    try {
      jobj = new JSONObject(tokener);
      return jobj.getJSONObject(NotebookMetadataKeys.metadata.key).getJSONObject(NotebookMetadataKeys.viewerInfo.key);

    } catch (JSONException e) {
      return null;
    }
  }

  private static boolean tryGetBoolFromJSON(String key, JSONObject jobj) {
    try {
      return jobj.getBoolean(key);
    } catch (JSONException e) {
      return false;
    }
  }

  private static String tryGetStringFromJSON(String key, JSONObject jobj) {
    try {
      return jobj.getString(key);
    } catch (JSONException e) {
      return "";
    }
  }

  private static JSONObject tryGetJSONObjectFromJSON(String key, JSONObject jobj) {
    try {
      return jobj.getJSONObject(key);
    } catch (JSONException e) {
      return new JSONObject();
    }
  }

  private static Set<String> tryGetSetFromJSON(String key, JSONObject jobj) {
    Set<String> set = new HashSet<>();
    try {
      JSONArray jArray = jobj.getJSONArray(key);
      if (jArray != null)
        for (int i = 0; i < jArray.length(); i++) {
          set.add(jArray.getString(i));
        }
      return set;
    } catch (JSONException e) {
      return set;
    }
  }

  private class AcceptedDatasetTypes {

    private boolean accept_all;

    private Set<String> accept_datasetIDs;

    private Set<String> accept_catalogs;

    private Set<String> accept_dataset_types;

    public AcceptedDatasetTypes(JSONObject nb) {
      JSONObject jobj = tryGetJSONObjectFromJSON(NotebookMetadataKeys.acceptObject.key, nb);

      this.accept_all = jobj.isEmpty() ? true : tryGetBoolFromJSON(NotebookMetadataKeys.acceptAll.key, jobj);
      this.accept_datasetIDs = tryGetSetFromJSON(NotebookMetadataKeys.acceptDatasetIDs.key, jobj);
      this.accept_catalogs = tryGetSetFromJSON(NotebookMetadataKeys.acceptCatalogs.key, jobj);
      this.accept_dataset_types = tryGetSetFromJSON(NotebookMetadataKeys.acceptDatasetTypes.key, jobj);
    }

    public boolean acceptsDataset(Dataset ds) {
      if (this.accept_all) {
        return true;
      }
      if (this.accept_datasetIDs.contains(ds.getID())) {
        return true;
      }
      if (this.accept_dataset_types.contains(ds.getFeatureTypeName())) {
        return true;
      }
      // check for loose catalog match
      Catalog parent = ds.getParentCatalog();
      if (parent == null) {
        return false;
      }
      String catName = parent.getName();
      if (catName == null) {
        return false;
      }
      String catUrl = parent.getUriString();
      return this.accept_catalogs.stream().anyMatch(str -> str.equals(catUrl) || catName.contains(str));
    }
  }

  private enum NotebookMetadataKeys {
    metadata("metadata"),
    viewerInfo("viewer_info"),
    description("description"),
    acceptObject("accepts"),
    acceptAll("accept_all"),
    acceptDatasetIDs("accept_datasetIDs"),
    acceptCatalogs("accept_catalogs"),
    acceptDatasetTypes("accept_dataset_types");

    final String key;

    NotebookMetadataKeys(String key) {
      this.key = key;
    }
  }

  class InvalidJupyterNotebookException extends Exception {
    public InvalidJupyterNotebookException(String message) {
      super(message);
    }
  }
}
