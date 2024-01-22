/*
 * Copyright 2012, UCAR/Unidata.
 * See the LICENSE file for more information.
 */


package dap4.servlet;

import dap4.core.ce.CEConstraint;
import dap4.core.util.ChecksumMode;
import dap4.core.dmr.*;
import dap4.core.util.*;
import dap4.dap4lib.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.zip.Checksum;

abstract public class DapController extends HttpServlet {

  //////////////////////////////////////////////////
  // Constants

  public static boolean DEBUG = false;

  protected static final String BIG_ENDIAN = "Big-Endian";
  protected static final String LITTLE_ENDIAN = "Little-Endian";

  // Is this machine big endian?
  protected static boolean IS_BIG_ENDIAN = (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);

  protected static final String DMREXT = ".dmr";
  protected static final String DATAEXT = ".dap";
  protected static final String DSREXT = ".dsr";
  protected static final String[] ENDINGS = {DMREXT, DATAEXT, DSREXT};

  protected static final String FAVICON = "favicon.ico"; // relative to resource dir

  public static final long DEFAULTBINARYWRITELIMIT = 100 * 1000000; // in bytes

  //////////////////////////////////////////////////
  // static variables

  //////////////////////////////////////////////////
  // Static accessors

  public static String printDMR(DapDataset dmr) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    DMRPrinter printer = new DMRPrinter(dmr, pw);
    try {
      printer.print();
      pw.close();
      sw.close();
    } catch (IOException e) {
    }
    return sw.toString();
  }

  //////////////////////////////////////////////////
  // Instance variables

  protected boolean initialized = false; // Was initialize() called?

  // Cache the value of getWebDontentRoot()
  protected String webContentRoot = null;

  // Cache the value of getTestDataRoot()
  protected String testDataRoot = null;

  //////////////////////////////////////////////////
  // Constructor(s)

  public DapController() {}

  //////////////////////////////////////////////////////////
  // Abstract methods

  /**
   * Process a favicon request.
   *
   * @param icopath The path to the icon
   * @param cxt The dap context
   */

  abstract protected void doFavicon(String icopath, DapContext cxt) throws IOException;

  /**
   * Process a capabilities request.
   * Currently, does nothing (but see D4TSServlet.doCapabilities).
   *
   * @param cxt The dapontext
   */

  abstract protected void doCapabilities(DapRequest drq, DapContext cxt) throws IOException;

  /**
   * Get the maximum # of bytes per request
   *
   * @return size
   */
  abstract public long getBinaryWriteLimit();

  /**
   * Get the servlet name (with no leading or trailing slashes)
   *
   * @return name
   */
  abstract public String getServletID();

  /**
   * The DAP4 code requires access to two absolute paths.
   * 1. The path to a directory containing web content:
   * - .template files
   * - .ico files
   * 2. The path to the directory containing the test data
   */

  /**
   * Get the absolute address of the web-content directory
   *
   * @param drq dap request
   * @return the web content directory absolute path
   * @throws IOException
   */
  abstract protected String getWebContentRoot(DapRequest drq) throws DapException;

  /**
   * Convert a URL path for a dataset into an absolute file path
   *
   * @param drq dap request
   * @param location suffix of url path
   * @return path in a string builder so caller can extend.
   * @throws IOException
   */
  abstract protected String getResourcePath(DapRequest drq, String location) throws DapException;

  //////////////////////////////////////////////////////////

  public void init() {
    org.slf4j.Logger logServerStartup = org.slf4j.LoggerFactory.getLogger("serverStartup");
    logServerStartup.info(getClass().getName() + " initialization start");
    initialize();
  }

  /**
   * Initialize servlet/controller
   */
  public void initialize() {
    if (this.initialized)
      return;
    this.initialized = true;
  }


  //////////////////////////////////////////////////////////
  // Primary Controller Entry Point

  public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
    DapLog.debug("doGet(): User-Agent = " + req.getHeader("User-Agent"));
    if (!this.initialized)
      initialize();
    DapRequest daprequest = getRequestState(req, res);
    DapContext dapcxt = buildDapContext(daprequest);

    if (this.webContentRoot == null) {
      this.webContentRoot = getWebContentRoot(daprequest);
      this.webContentRoot = DapUtil.canonicalpath(this.webContentRoot);
    }

    String url = daprequest.getURL();
    if (url.endsWith(FAVICON)) {
      doFavicon(FAVICON, dapcxt);
      return;
    }
    String datasetpath = daprequest.getDatasetPath();
    datasetpath = DapUtil.nullify(DapUtil.canonicalpath(datasetpath));
    try {
      if (datasetpath == null) {
        // This is the case where a request was made without a dataset;
        // According to the spec, I think we should return the
        // services/capabilities document
        doCapabilities(daprequest, dapcxt);
      } else {
        RequestMode mode = daprequest.getMode();
        if (mode == null)
          throw new DapException("Unrecognized request extension").setCode(HttpServletResponse.SC_BAD_REQUEST);
        switch (mode) {
          case DMR:
            doDMR(daprequest, dapcxt);
            break;
          case DAP:
            doData(daprequest, dapcxt);
            break;
          case DSR:
            doDSR(daprequest, dapcxt);
            break;
          default:
            throw new DapException("Unrecognized request extension").setCode(HttpServletResponse.SC_BAD_REQUEST);
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
      int code = HttpServletResponse.SC_BAD_REQUEST;
      if (t instanceof DapException) {
        DapException e = (DapException) t;
        code = e.getCode();
        if (code <= 0)
          code = DapCodes.SC_BAD_REQUEST;
        e.setCode(code);
      } else if (t instanceof FileNotFoundException)
        code = DapCodes.SC_NOT_FOUND;
      else if (t instanceof UnsupportedOperationException)
        code = DapCodes.SC_FORBIDDEN;
      else if (t instanceof MalformedURLException)
        code = DapCodes.SC_NOT_FOUND;
      else if (t instanceof IOException)
        code = DapCodes.SC_BAD_REQUEST;
      else
        code = DapCodes.SC_INTERNAL_SERVER_ERROR;
      senderror(daprequest, code, t);
    } // catch
  }

  //////////////////////////////////////////////////////////
  // Extension processors

  /**
   * Process a DSR request.
   * * @param cxt The dap context
   */

  protected void doDSR(DapRequest drq, DapContext cxt) throws IOException {
    try {
      DapDSR dsrbuilder = new DapDSR(drq, cxt);
      String dsr = dsrbuilder.generate(drq.getFormat(), drq.getDatasetPath(), drq.getDataset());
      OutputStream out = drq.getOutputStream();
      addCommonHeaders(drq);// Add relevant headers
      // Wrap the outputstream with a Chunk writer
      ByteOrder order = (ByteOrder) cxt.get(DapConstants.DAP4ENDIANTAG);
      ChunkWriter cw = new ChunkWriter(out, RequestMode.DSR, order);
      cw.writeDSR(dsr);
      cw.close();
    } catch (IOException ioe) {
      throw new DapException("DSR generation error", ioe).setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Process a DMR request.
   *
   * @param cxt The dap context
   */

  protected void doDMR(DapRequest drq, DapContext cxt) throws IOException {
    // Convert the url to an absolute path
    String realpath = drq.getResourcePath(drq.getDatasetPath());

    CDMWrap c4 = new CDMWrap().open(realpath); // Create the wrapper
    DapDataset dmr = c4.getDMR();
    CEConstraint ce = constrainDapContext(cxt, dmr);
    ChecksumMode csummode = (ChecksumMode) cxt.get(DapConstants.CHECKSUMTAG);
    ByteOrder order = (ByteOrder) cxt.get(DapConstants.DAP4ENDIANTAG);

    // If the user calls for checksums, then we need to compute them
    if (csummode == ChecksumMode.TRUE) {
      Map<DapVariable, Long> checksummap = computeDMRChecksums(c4, cxt);
      // Add to context
      cxt.put("checksummap", checksummap);
    }

    // Provide a PrintWriter for capturing the DMR.
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    // Get the DMR as a string
    DMRPrinter dapprinter = new DMRPrinter(dmr, ce, pw, drq.getFormat(), cxt);
    dapprinter.print();
    pw.close();
    sw.close();

    String sdmr = sw.toString();
    if (DEBUG)
      System.err.println("Sending: DMR:\n" + sdmr);

    addCommonHeaders(drq);// Add relevant headers

    OutputStream out = drq.getOutputStream();
    // Wrap the outputstream with a Chunk writer
    ChunkWriter cw = new ChunkWriter(out, RequestMode.DMR, order);
    cw.cacheDMR(sdmr);
    cw.close();

    c4.close();

  }

  /**
   * Process a DataDMR request.
   * Note that if this throws an exception,
   * then it has not yet started to output
   * a response. It a response had been initiated,
   * then the exception would produce an error chunk.
   * <p>
   * * @param cxt The dap context
   */

  protected void doData(DapRequest drq, DapContext cxt) throws IOException {
    // Convert the url to an absolute path
    String realpath = drq.getResourcePath(drq.getDatasetPath());

    CDMWrap c4 = new CDMWrap().open(realpath);
    if (c4 == null)
      throw new DapException("No such file: " + realpath);

    DapDataset dmr = c4.getDMR();
    CEConstraint ce = constrainDapContext(cxt, dmr);
    ChecksumMode csummode = (ChecksumMode) cxt.get(DapConstants.CHECKSUMTAG);
    ByteOrder order = (ByteOrder) cxt.get(DapConstants.DAP4ENDIANTAG);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    // If the user calls for checksums, then we need to compute them
    // This, unfortunately, will require computation twice: one to insert
    // into the DMR and one to insert into the serialized DAP stream. Sigh!
    if (csummode == ChecksumMode.TRUE) {
      Map<DapVariable, Long> checksummap = computeDMRChecksums(c4, cxt);
      // Add to context
      cxt.put("checksummap", checksummap);
    }

    // Get the DMR as a string
    DMRPrinter dapprinter = new DMRPrinter(dmr, ce, pw, drq.getFormat(), cxt);
    dapprinter.print();
    pw.close();
    sw.close();

    // Wrap the outputstream with a Chunk writer
    OutputStream out = drq.getOutputStream();
    ChunkWriter cw = new ChunkWriter(out, RequestMode.DAP, order);
    cw.setWriteLimit(getBinaryWriteLimit());
    String sdmr = sw.toString();
    cw.cacheDMR(sdmr);
    cw.flush();

    addCommonHeaders(drq);

    // Dump the databuffer part
    switch (drq.getFormat()) {
      case TEXT:
      case XML:
      case HTML:
        throw new IOException("Unsupported return format: " + drq.getFormat());
      /*
       * sw = new StringWriter();
       * DAPPrint dp = new DAPPrint(sw);
       * dp.print(dsp.getDataset(), ce);
       * break;
       */
      case NONE:
      default:
        DapSerializer writer = new DapSerializer(c4, ce, cw, order, drq.getChecksumMode());
        writer.write(c4.getDMR());
        cw.flush();
        cw.close();
        break;
    }
    c4.close();
  }

  //////////////////////////////////////////////////////////
  // Utility Methods

  protected void addCommonHeaders(DapRequest drq) throws DapException {
    // Add relevant headers
    ResponseFormat format = drq.getFormat();
    if (format == null)
      format = ResponseFormat.NONE;
    RequestMode mode = drq.getMode();
    if (mode == null)
      mode = RequestMode.CAPABILITIES;
    DapProtocol.ContentType contenttype = DapProtocol.contenttypes.get(DapProtocol.contentKey(mode, format));
    if (contenttype == null)
      throw new DapException("Cannot find Content-Type for: " + mode.id() + "," + format.id())
          .setCode(DapCodes.SC_BAD_REQUEST);
    // If we go by what OPeNDAP does, then the header rules are as follow:
    // 1. Set the Content-Description to the application/vnd.opendap.dap4...
    // 2. If the Response type is not-null, then use the appropriate mime type such as text/xml.
    // 3. If the Response type is null, then use the same value as Content-Description.
    // Unfortunately, my HTTP-foo is not good enough to handle rule 3; a web-browser does
    // not seem to figure out the actual mime-type from the "application/vnd.opendap.dap4..."
    // content type. So, we use a different rule:
    // 3a. If the response type is null, then set Content-Type to the proper mime-type.

    // Rule 1.
    drq.setResponseHeader("Content-Description", contenttype.contenttype);

    // Rules 2 and 3.
    String header = contenttype.mimetype;
    header = header + "; charset=utf-8";
    drq.setResponseHeader("Content-Type", header);
  }

  /**
   * Merge the servlet inputs into a single object
   * for easier transport as well as adding value.
   *
   * @param rq A Servlet request object
   * @param rsp A Servlet response object
   * @return the union of the
   *         servlet request and servlet response arguments
   *         from the servlet engine.
   */

  protected DapRequest getRequestState(HttpServletRequest rq, HttpServletResponse rsp) throws IOException {
    return new DapRequest(this, rq, rsp);
  }

  //////////////////////////////////////////////////////////
  // Error Methods

  /*
   * Note that these error returns are assumed to be before
   * any DAP4 response has been generated. So they will
   * set the header return code and an Error Response as body.
   * Error chunks are handled elsewhere.
   */

  /**
   * Generate an error based on the parameters
   *
   * @param drq DapRequest
   * @param httpcode 0=>no code specified
   * @param t exception that caused the error; may be null
   * @throws IOException
   */
  protected void senderror(DapRequest drq, int httpcode, Throwable t) throws IOException {
    if (httpcode == 0)
      httpcode = HttpServletResponse.SC_BAD_REQUEST;
    ErrorResponse err = new ErrorResponse();
    err.setCode(httpcode);
    if (t == null) {
      err.setMessage("Servlet error: " + drq.getURL());
    } else {
      StringWriter sw = new StringWriter();
      PrintWriter p = new PrintWriter(sw);
      t.printStackTrace(p);
      p.close();
      sw.close();
      err.setMessage(sw.toString());
    }
    err.setContext(drq.getURL());
    String errormsg = err.buildXML();
    if (false) {
      drq.getResponse().sendError(httpcode, errormsg);
    } else {
      OutputStream out = drq.getOutputStream();
      PrintWriter prw = new PrintWriter(new OutputStreamWriter(out, DapUtil.UTF8));
      prw.println(errormsg);
      prw.flush();
    }
  }

  public DapContext buildDapContext(DapRequest daprequest) throws DapException {
    DapContext dapcxt = new DapContext();
    // Add entries to the context
    dapcxt.put(DapRequest.class, daprequest);
    ByteOrder order = daprequest.getOrder();
    if (order != null) {
      dapcxt.put(DapConstants.DAP4ENDIANTAG, order);
      ChecksumMode checksummode = daprequest.getChecksumMode();
      if (checksummode != null)
        dapcxt.put(DapConstants.CHECKSUMTAG, ChecksumMode.asTrueFalse(checksummode));
      // Transfer all other queries
      Map<String, String> queries = daprequest.getQueries();
      for (Map.Entry<String, String> entry : queries.entrySet()) {
        if (dapcxt.get(entry.getKey()) == null)
          dapcxt.put(entry.getKey(), entry.getValue());
      }
    }
    return dapcxt;
  }

  public CEConstraint constrainDapContext(DapContext dapcxt, DapDataset dmr) throws DapException {
    // Add additional entries to the context
    // Process any constraint view
    DapRequest drq = (DapRequest) dapcxt.get(DapRequest.class);
    String sce = drq.queryLookup(DapConstants.CONSTRAINTTAG);
    CEConstraint ce = CEConstraint.compile(sce, dmr);
    dapcxt.put(CEConstraint.class, ce);
    return ce;
  }

  protected Map<DapVariable, Long> computeDMRChecksums(CDMWrap c4, DapContext cxt) throws DapException {
    ByteOrder order = (ByteOrder) cxt.get(DapConstants.DAP4ENDIANTAG);
    ChecksumMode csum = (ChecksumMode) cxt.get(DapConstants.CHECKSUMTAG);
    CEConstraint ce = (CEConstraint) cxt.get(CEConstraint.class);
    OutputStream out = new NullOutputStream();
    DapDataset dmr = ce.getDMR();
    try {
      ChunkWriter cw = new ChunkWriter(out, RequestMode.DAP, order);
      cw.cacheDMR(dmr, cxt);
      cw.setWriteLimit(1000000000);
      DapSerializer writer = new DapSerializer(c4, ce, cw, order, csum);
      writer.write(dmr);
      cw.flush();
      cw.close();
      return writer.getChecksums();
    } catch (IOException ioe) {
      throw new DapException(ioe);
    }
  }

}


