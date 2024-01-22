/*
 * Copyright 2009, UCAR/Unidata and OPeNDAP, Inc.
 * See the LICENCE file for more information.
 */

package dap4.d4ts;

import dap4.core.util.DapConstants;
import dap4.core.util.DapException;
import dap4.core.util.DapUtil;
import dap4.dap4lib.DapLog;
import dap4.servlet.DapRequest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Given a directory, return a front page of HTML
 * that lists all of the files in that page.
 *
 * @author Dennis Heimbigner
 */

public class FrontPage {

  static final boolean DUMPFILELIST = false;

  static final String FPTEMPLATE = "/templates/dap4.frontpage.html.template";

  static final String DFALTTESTSERVER = "remotetest.unidata.ucar.edu";

  //////////////////////////////////////////////////
  // Constants

  static final protected boolean NO_VLEN = false; // ignore vlen datasets for now

  static final protected String[] expatterns = new String[0];

  // Define the file sources of interest
  static final protected FileSource[] SOURCES = new FileSource[] {new FileSource(".nc", "netCDF")};

  public static class Root {
    public String prefix;

    public String dir;

    public List<FileSource> files;

    public String toString() {
      return String.format("{'%s/%s'}", this.prefix, this.dir);
    }

    public Root(String prefix, String dir) {
      this.prefix = DapUtil.canonicalpath(prefix);
      this.dir = dir;
    }

    public String getFullPath() {
      return this.prefix + "/" + this.dir;
    };

    public void setFiles(List<FileSource> files) {
      this.files = files;
    }
  }

  private static String dap4TestServerPropName = "d4ts";
  public static String dap4TestServer = null;; // mutable

  static {
    String d4ts = System.getProperty(dap4TestServerPropName);
    if (d4ts != null && d4ts.length() > 0)
      dap4TestServer = d4ts;
  }

  //////////////////////////////////////////////////

  static class FileSource {
    public String ext = null;
    public String tag = null;
    public List<File> files = null;

    public FileSource(String ext, String tag) {
      this.ext = ext;
      this.tag = tag;
    }
  }

  //////////////////////////////////////////////////
  // Instance Variables

  protected DapRequest drq = null;
  protected List<Root> roots = null; // root paths to the displayed files
  protected StringBuilder frontpage = null;

  //////////////////////////////////////////////////
  // Constructor(s)

  /**
   * @param rootinfo the file directory roots
   * @throws DapException
   */
  public FrontPage(List<Root> rootinfo, DapRequest req) throws DapException {
    this.drq = req;
    this.roots = rootinfo;
    for (Root root : this.roots) {
      // Construct the list of usable files
      buildFileList(root);
    }
    // Figure out the test server
    if (this.dap4TestServer == null) {
      try {
        URL url = new URL(drq.getRequest().getRequestURL().toString());
        this.dap4TestServer = url.getHost();
        if (url.getPort() > 0)
          this.dap4TestServer += ":" + url.getPort();
      } catch (MalformedURLException mue) {
        this.dap4TestServer = null;
      }
    }
    if (this.dap4TestServer == null)
      this.dap4TestServer = DFALTTESTSERVER;

    // Get the template and fill in selected macros
    try {
      this.frontpage = initialPage(this.dap4TestServer);
    } catch (IOException ioe) {
      throw new DapException(ioe).setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
  //////////////////////////////////////////////////

  protected void buildFileList(Root rootinfo) throws DapException {
    File root = new File(rootinfo.getFullPath());
    if (!root.isDirectory())
      throw new DapException("FrontPage: specified root directory is not a directory: " + rootinfo.getFullPath());
    if (!root.canRead())
      throw new DapException("FrontPage: specified root directory is not readable: " + rootinfo.getFullPath());

    // take files from set of files immediately under root
    File[] candidates = root.listFiles();
    List<FileSource> activesources = new ArrayList<FileSource>();
    // Capture lists of files for each FileSource
    for (FileSource src : SOURCES) {
      List<File> matches = new ArrayList<File>();
      for (File candidate : candidates) {
        String name = candidate.getName();
        boolean excluded = false;
        for (String exclude : expatterns) {
          if (name.indexOf(exclude) >= 0) {
            excluded = true;
            break;
          }
        }
        if (excluded)
          continue;
        if (!name.endsWith(src.ext))
          continue;
        if (!candidate.canRead()) {
          DapLog.info("FrontPage: file not readable: " + candidate);
          continue;
        }
        matches.add(candidate);
      }
      if (matches.size() > 0) {
        // Sort the set of files
        matches.sort(new Comparator<File>() {
          public int compare(File f1, File f2) {
            return f1.getName().compareTo(f2.getName());
          }
        });
        if (DUMPFILELIST) {
          for (File x : matches) {
            System.err.printf("file: %s/%s%n", rootinfo.dir, x.getName());
          }
        }
        FileSource clone = new FileSource(src.ext, src.tag);
        clone.files = matches;
        activesources.add(clone);
      }
    }
    rootinfo.setFiles(activesources);
  }

  protected String buildPage() throws DapException {
    StringBuilder rootnames = new StringBuilder();
    for (Root root : this.roots) {
      if (rootnames.length() > 0)
        rootnames.append(",");
      rootnames.append(root.dir);
    }
    StringBuilder sources = new StringBuilder();
    for (Root root : this.roots) {
      try {
        for (FileSource src : root.files) {
          StringBuilder allrows = new StringBuilder();
          for (File file : src.files) {
            String name = file.getName();
            String rootpath = root.dir;
            if (rootpath.startsWith("/"))
              rootpath = rootpath.substring(1);
            String url = String.format(HTML_URL_FORMAT, this.dap4TestServer, rootpath, name);
            StringBuilder row = new StringBuilder(HTML_ROW);
            substitute(row, "dataset", name);
            substitute(row, "url", url);
            allrows.append(row);
          }
          StringBuilder source = new StringBuilder(HTML_SOURCE);
          substitute(source, "source", src.tag);
          substitute(source, "rows", allrows.toString());
          sources.append(source);
        }
      } catch (Exception e) {
        sendtrace(drq, e);
      }
    }
    substitute(this.frontpage, "sources", sources.toString());
    return this.frontpage.toString();
  }

  protected StringBuilder initialPage(String testserver) throws IOException {
    // Get the FrontPage template
    StringBuilder page = new StringBuilder();
    String template = drq.getWebContentPath(FPTEMPLATE);
    try (InputStream stream = new FileInputStream(template)) {
      int ch;
      while ((ch = stream.read()) >= 0) {
        page.append((char) ch);
      }
    }
    // Fill in initial parameters
    substitute(page, "dap4TestServer", testserver);
    return page;
  }

  protected void substitute(StringBuilder buf, String macro, String value) {
    int from = 0;
    String tag = "${" + macro + "}";
    int taglen = tag.length();
    int valuelen = value.length();
    for (;;) {
      int index = buf.indexOf(tag, from);
      if (index < 0)
        break;
      buf.replace(index, index + taglen, value);
      from = index + valuelen;
    }
  }

  public static void sendtrace(DapRequest drq, Exception e) {
    try {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      pw.close();
      sw.close();
      sendtext(drq, sw.toString());
    } catch (IOException ioe) {
      assert false;
    }
  }

  public static void sendtext(DapRequest drq, String s) {
    try {
      // s = s.replace("<", "&lt;");
      // s = s.replace(">", "&gt;");
      // s = s + "\n";
      s = "+++++++++\n" + s;
      s = s + "+++++++++\n";
      byte[] bytes = DapUtil.extract(DapUtil.UTF8.encode(s));
      OutputStream out = drq.getOutputStream();
      out.write(bytes);
      out.flush();
    } catch (IOException ioe) {
      assert false;
    }
  }

  /**
   * Get a flattened list of all files immediately
   * under a given root with given extension(s).
   */

  /*
   * static List<File>
   * collectFiles(File dir)
   * {
   * List<File> result = new ArrayList<File>();
   * if(!dir.isDirectory())
   * DapLog.info("FrontPage: specified root is not a directory: " + dir);
   * if(!dir.canRead())
   * DapLog.info("FrontPage: specified root directory is not readable: " + dir);
   * 
   * File[] contents = dir.listFiles();
   * Arrays.sort(contents);
   * for(File f : contents) {
   * if(f.isDirectory()) {
   * List<File> subfiles = walkDir(f);
   * result.addAll(subfiles);
   * } else {
   * result.add(f);
   * }
   * }
   * return result;
   * }
   */

  //////////////////////////////////////////////////
  // HTML Text Pieces
  // (Remember that java does not allow Strings to cross lines)

  static final String HTML_URL_FORMAT = DapConstants.HTTPSCHEME + "//%s/d4ts/%s/%s";

  static final String HTML_SOURCE = "<h3>${source} Based Test Files</h3>\n<table>\n${rows}\n</table>";

  static final String HTML_ROW = String.join("\n", "<tr>", "<td halign='right'><b>${dataset}:</b></td>",
      "<td halign='center'><a href='${url}.dmr'> DMR </a></div></td>",
      "<td halign='center'><a href='${url}.dap'> DAP </a></div></td>",
      "<td halign='center'><a href='${url}.dsr'> DSR </a></div></td>", "</tr>", "");

} // FrontPage
