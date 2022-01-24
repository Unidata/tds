package thredds.server.notebook;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import thredds.core.AllowedServices;
import thredds.core.CatalogManager;
import thredds.core.StandardService;
import thredds.core.TdsRequestedDataset;
import thredds.server.config.TdsContext;
import thredds.server.exception.ServiceNotAllowed;
import thredds.util.Constants;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notebook")
public class NotebookController {

  private final String DS_REPLACE_TEXT = "{{datasetName}}";
  private final String CAT_REPLACE_TEXT = "{{catUrl}}";

  @Autowired
  TdsContext tdsContext;

  @Autowired
  CatalogManager catalogManager;

  @Autowired
  AllowedServices allowedServices;

  @Autowired
  JupyterNotebookServiceCache jupyterNotebooks;

  @RequestMapping(value = "**", params = "filename", method = RequestMethod.GET)
  public void getNotebook(@RequestParam("filename") String filename, HttpServletRequest req, HttpServletResponse res,
      @Valid NotebookParamsBean params, BindingResult validationResult)
      throws ServiceNotAllowed, IOException, BindException, URISyntaxException {

    if (!allowedServices.isAllowed(StandardService.jupyterNotebook) || filename.isEmpty()
        || !validateRequestedFile(filename)) {
      throw new ServiceNotAllowed(StandardService.jupyterNotebook.toString());
    }
    if (validationResult.hasErrors()) {
      throw new BindException(validationResult);
    }

    // Get notebook
    File responseFile = getNotebookFile(filename);
    if (responseFile == null) {
      throw new FileNotFoundException(filename);
    }

    // Get Dataset
    String catalogName = params.catalog;
    Dataset dataset = getDataset(catalogName, req);

    // Transform each notebook with dataset id;
    String fileContents = new String(Files.readAllBytes(Paths.get(responseFile.getAbsolutePath())));

    // Build catalog URL
    String catUrlString = req.getRequestURL().toString();
    catUrlString = catUrlString.substring(0, catUrlString.indexOf(getBase())) + StandardService.catalogRemote.getBase()
        + catalogName;

    fileContents = fileContents.replace(DS_REPLACE_TEXT, dataset.getName()).replace(CAT_REPLACE_TEXT, catUrlString);

    // Set headers...
    res.setHeader(Constants.Content_Disposition, Constants.setContentDispositionValue(responseFile.getName()));
    res.setHeader(Constants.Content_Length, Integer.toString(fileContents.length()));

    // Set content...
    String mimeType = "application/x-ipynb+json";
    res.setContentType(mimeType);
    res.getOutputStream().write(fileContents.getBytes());
    res.flushBuffer();
    res.getOutputStream().close();
    res.setStatus(HttpServletResponse.SC_OK);
  }

  @RequestMapping(value = "**", method = RequestMethod.GET)
  public void getNotebooksForDataset(HttpServletRequest req, HttpServletResponse res, @Valid NotebookParamsBean params,
      BindingResult validationResult)
      throws IllegalArgumentException, ServiceNotAllowed, IOException, URISyntaxException, BindException {

    if (!allowedServices.isAllowed(StandardService.jupyterNotebook)) {
      throw new ServiceNotAllowed(StandardService.jupyterNotebook.toString());
    }
    if (validationResult.hasErrors()) {
      throw new BindException(validationResult);
    }

    // Get Dataset
    String catalogName = params.catalog;
    Dataset dataset = getDataset(catalogName, req);

    JSONArray notebooks = getNotebookParams(dataset);

    // Set content...
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    res.getWriter().write(notebooks.toString());
    res.getWriter().flush();
    res.getWriter().close();
    res.setStatus(HttpServletResponse.SC_OK);
  }

  protected String getBase() {
    return StandardService.jupyterNotebook.getBase();
  }

  private boolean validateRequestedFile(String filename) {
    // check file extension
    if (!Arrays.asList(".py", ".ipynb").stream().anyMatch(ext -> filename.endsWith(ext))) {
      return false;
    }
    // do not allow filenames that include parent dirs
    if (filename.contains("../")) {
      return false;
    }
    return true;
  }

  private Dataset getDataset(String catalogName, HttpServletRequest req)
      throws URISyntaxException, IOException, IllegalArgumentException {
    if (catalogName == null) {
      throw new IllegalArgumentException("Argument 'catalog' cannot be null.");
    }
    String datasetId = new TdsRequestedDataset(req, getBase()).getPath();
    Catalog catalog = getCatalog(catalogName, req);
    return catalog.findDatasetByID(datasetId);
  }

  private Catalog getCatalog(String catalogName, HttpServletRequest req) throws URISyntaxException, IOException {

    String catalogReqBase = StandardService.catalogRemote.getBase();

    // replace /notebook/ with /catalog/ and remove datasetId from path
    String baseUriString = req.getRequestURL().toString().replace(getBase(), catalogReqBase);
    baseUriString = baseUriString.substring(0, baseUriString.indexOf(catalogReqBase) + catalogReqBase.length());

    Catalog catalog;
    URI baseUri;
    try {
      baseUri = new URI(baseUriString);
      catalog = catalogManager.getCatalog(catalogName, baseUri);
    } catch (URISyntaxException e) {
      String msg = "Bad URI syntax [" + baseUriString + "]: " + e.getMessage();
      throw new URISyntaxException(msg, e.getReason());
    }

    if (catalog == null)
      throw new FileNotFoundException(baseUriString + catalogName);

    return catalog;
  }

  private JSONArray getNotebookParams(Dataset ds) {
    List<JSONObject> objs =
        jupyterNotebooks.getMappedNotebooks(ds).stream().map(nb -> nb.getParams()).collect(Collectors.toList());
    return new JSONArray(objs);
  }

  private File getNotebookFile(String filename) throws IOException {
    if (filename.isEmpty()) {
      return null;
    }

    File notebooksDir = new File(tdsContext.getThreddsDirectory(), "notebooks");

    if (notebooksDir.exists() && notebooksDir.isDirectory()) {
      File jupyterViewer = new File(notebooksDir, filename);
      // extra safety check
      if (!jupyterViewer.getCanonicalPath().startsWith(notebooksDir.getCanonicalPath())) {
        throw new AccessDeniedException(filename);
      }
      if (jupyterViewer.exists()) {
        return jupyterViewer;
      }
    }
    return null;
  }
}
