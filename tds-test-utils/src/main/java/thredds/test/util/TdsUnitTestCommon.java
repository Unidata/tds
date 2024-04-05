/*
 * Copyright (c) 2021 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.test.util;

import ucar.httpservices.HTTPFactory;
import ucar.httpservices.HTTPMethod;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.DatasetUrl;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.write.Ncdump;
import ucar.unidata.util.test.Diff;
import ucar.unidata.util.test.SysStreamLogger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;

public abstract class TdsUnitTestCommon {
  //////////////////////////////////////////////////
  // Static Constants

  public static boolean LOGSTDIO = System.getProperty("intellij") == null;

  public static final boolean DEBUG = false;

  public static final Charset UTF8 = StandardCharsets.UTF_8;

  protected static final int[] OKCODES = {200, 404};

  // Look for these to verify we have found the thredds root
  static final String[] DEFAULTSUBDIRS = {"opendap", "dap4"};

  // NetcdfDataset enhancement to use: need only coord systems
  static final Set<NetcdfDataset.Enhance> ENHANCEMENT = EnumSet.of(NetcdfDataset.Enhance.CoordSystems);

  protected static String threddsroot = null;

  static {
    // Compute the root path
    threddsroot = locateThreddsRoot();
    assert threddsroot != null : "Cannot locate /thredds parent dir";
  }

  //////////////////////////////////////////////////
  // Static methods

  // Walk around the directory structure to locate
  // the path to the thredds root (which may not
  // be names "thredds").
  // Same as code in UnitTestCommon, but for
  // some reason, Intellij will not let me import it.

  static String locateThreddsRoot() {
    // Walk up the user.dir path looking for a node that has
    // all the directories in SUBROOTS.

    // It appears that under Jenkins, the Java property "user.dir" is
    // set incorrectly for our purposes. In this case, we want
    // to use the WORKSPACE environment variable set by Jenkins.
    String workspace = System.getenv("WORKSPACE");
    System.err.println("WORKSPACE=" + (workspace == null ? "null" : workspace));
    System.err.flush();

    String userdir = System.getProperty("user.dir");

    String path = (workspace != null ? workspace : userdir); // Pick one

    // clean up the path
    path = path.replace('\\', '/'); // only use forward slash
    assert (path != null);
    if (path.endsWith("/"))
      path = path.substring(0, path.length() - 1);

    File prefix = new File(path);
    for (; prefix != null; prefix = prefix.getParentFile()) {// walk up the tree
      int found = 0;
      String[] subdirs = prefix.list();
      for (String dirname : subdirs) {
        for (String want : DEFAULTSUBDIRS) {
          if (dirname.equals(want)) {
            found++;
            break;
          }
        }
      }
      if (found == DEFAULTSUBDIRS.length)
        try {// Assume this is it
          String root = prefix.getCanonicalPath();
          // clean up the root path
          root = root.replace('\\', '/'); // only use forward slash
          return root;
        } catch (IOException ioe) {
        }
    }
    return null;
  }

  //////////////////////////////////////////////////
  // Instance variables

  // System properties
  protected boolean prop_ascii = true;
  protected boolean prop_diff = true;
  protected boolean prop_baseline = false;
  protected boolean prop_visual = false;
  protected boolean prop_debug = DEBUG;
  protected boolean prop_generate = true;
  protected String prop_controls = null;
  protected boolean prop_display = false;

  protected String title = "Testing";
  protected String name = "testcommon";

  //////////////////////////////////////////////////
  // Constructor(s)

  public TdsUnitTestCommon() {
    this("Testing");
  }

  public TdsUnitTestCommon(String name) {
    this.title = name;
    setSystemProperties();
  }

  /**
   * Try to get the system properties
   */
  protected void setSystemProperties() {
    if (System.getProperty("nodiff") != null)
      prop_diff = false;
    if (System.getProperty("baseline") != null)
      prop_baseline = true;
    if (System.getProperty("nogenerate") != null)
      prop_generate = false;
    if (System.getProperty("debug") != null)
      prop_debug = true;
    if (System.getProperty("visual") != null)
      prop_visual = true;
    if (System.getProperty("ascii") != null)
      prop_ascii = true;
    if (System.getProperty("utf8") != null)
      prop_ascii = false;
    if (System.getProperty("hasdisplay") != null)
      prop_display = true;
    if (prop_baseline && prop_diff)
      prop_diff = false;
    prop_controls = System.getProperty("controls", "");
  }

  //////////////////////////////////////////////////
  // Accessor

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return this.title;
  }

  public String getThreddsroot() {
    return this.threddsroot;
  }

  public String getName() {
    return this.name;
  }

  public String getResourceDir() {
    throw new UnsupportedOperationException();
  }

  //////////////////////////////////////////////////
  // Instance Utilities

  public void visual(String header, String captured) {
    visual(header, captured, '-');
  }

  public void visual(String header, String captured, char marker) {
    if (!captured.endsWith("\n"))
      captured = captured + "\n";
    // Dump the output for visual comparison
    System.err.println("Testing " + getName() + ": " + header + ":");
    StringBuilder sep = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      sep.append(marker);
    }
    System.err.println(sep.toString());
    System.err.println("Testing " + title + ": " + header + ":");
    System.err.println("===============");
    System.err.println(captured);
    System.err.println(sep.toString());
    System.err.println("===============");
  }

  public static String compare(String tag, String baseline, String testresult) {
    // Check for empty testresult
    if (testresult.trim().length() == 0)
      return ">>>> EMPTY TEST RESULT";
    try {
      // Diff the two print results
      Diff diff = new Diff(tag);
      StringWriter sw = new StringWriter();
      boolean pass = !diff.doDiff(baseline, testresult, sw);
      return (pass ? null : sw.toString());
    } catch (Exception e) {
      System.err.println("UnitTest: Diff failure: " + e);
      return null;
    }
  }

  public static boolean same(String tag, String baseline, String testresult) {
    String result = compare(tag, baseline, testresult);
    if (result == null) {
      System.err.println("Files are Identical");
      return true;
    } else {
      System.err.println(result);
      return false;
    }
  }

  protected boolean checkServer(String candidate) {
    if (candidate == null)
      return false;
    // ping to see if we get a response
    System.err.print("Checking for sourceurl: " + candidate);
    try {
      try (HTTPMethod method = HTTPFactory.Get(candidate)) {
        method.execute();
        System.err.println(" ; found");
        return true;
      }
    } catch (IOException ie) {
      System.err.println(" ; fail");
      return false;
    }
  }

  protected void bindstd() {
    if (LOGSTDIO)
      SysStreamLogger.bindSystemStreams();
  }

  protected void unbindstd() {
    if (LOGSTDIO)
      SysStreamLogger.unbindSystemStreams();
  }

  //////////////////////////////////////////////////
  // Static utilities

  // Copy result into the a specified dir
  public static void writefile(String path, String content) throws IOException {
    File f = new File(path);
    if (f.exists())
      f.delete();
    FileWriter out = new FileWriter(f);
    out.write(content);
    out.close();
  }

  // Copy result into the a specified dir
  public static void writefile(String path, byte[] content) throws IOException {
    File f = new File(path);
    if (f.exists())
      f.delete();
    FileOutputStream out = new FileOutputStream(f);
    out.write(content);
    out.close();
  }

  public static String readfile(String filename) throws IOException {
    StringBuilder buf = new StringBuilder();
    Path file = Paths.get(filename);
    try (BufferedReader rdr = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      String line;
      while ((line = rdr.readLine()) != null) {
        if (line.startsWith("#"))
          continue;
        buf.append(line + "\n");
      }
      return buf.toString();
    }
  }

  public static byte[] readbinaryfile(String filename) throws IOException {
    FileInputStream stream = new FileInputStream(filename);
    byte[] result = readbinaryfile(stream);
    stream.close();
    return result;
  }

  public static byte[] readbinaryfile(InputStream stream) throws IOException {
    // Extract the stream into a bytebuffer
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    byte[] tmp = new byte[1 << 16];
    for (;;) {
      int cnt;
      cnt = stream.read(tmp);
      if (cnt <= 0)
        break;
      bytes.write(tmp, 0, cnt);
    }
    return bytes.toByteArray();
  }

  // Properly access a dataset
  static public NetcdfDataset openDatasetDap4Tests(String url) throws IOException {
    DatasetUrl durl = DatasetUrl.findDatasetUrl(url);
    return NetcdfDataset.acquireDataset(null, durl, ENHANCEMENT, -1, null, null);
  }

  // Fix up a filename reference in a string
  public static String shortenFileName(String text, String filename) {
    // In order to achieve diff consistentcy, we need to
    // modify the output to change "netcdf .../file.nc {...}"
    // to "netcdf file.nc {...}"
    String fixed = filename.replace('\\', '/');
    String shortname = filename;
    if (fixed.lastIndexOf('/') >= 0)
      shortname = filename.substring(fixed.lastIndexOf('/') + 1, filename.length());
    text = text.replaceAll(filename, shortname);
    return text;
  }

  public static String canonjoin(String... pieces) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < pieces.length; i++) {
      // invariant buf does not end with ('/')
      String piece = pieces[i];
      if (piece == null)
        continue;
      piece = canonicalpath(piece);
      if (i == 0)
        buf.append(piece);
      else {// i>=0
        if (!piece.startsWith("/"))
          buf.append("/");
        buf.append(piece);
      }
    }
    return buf.toString();
  }

  /**
   * Convert path to:
   * 1. use '/' consistently
   * 2. remove any trailing '/'
   * 3. trim blanks
   *
   * @param path convert this path
   * @return canonicalized version
   */
  public static String canonicalpath(String path) {
    if (path == null)
      return null;
    path = path.trim();
    path = path.replace('\\', '/');
    if (path.endsWith("/"))
      path = path.substring(0, path.length() - 1);
    // As a last step, lowercase the drive letter, if any
    if (hasDriveLetter(path))
      path = path.substring(0, 1).toLowerCase() + path.substring(1);
    return path;
  }

  /**
   * return true if this path appears to start with a windows drive letter
   *
   * @param path to check
   * @return true, if path has drive letter
   */

  public static boolean hasDriveLetter(String path) {
    if (path != null && path.length() >= 2) {
      return (DRIVELETTERS.indexOf(path.charAt(0)) >= 0 && path.charAt(1) == ':');
    }
    return false;
  }

  public static final String DRIVELETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase();


  public static String extractDatasetname(String urlorpath, String suffix) {
    try {
      URI x = new URI(urlorpath);
      StringBuilder path = new StringBuilder(x.getPath());
      int index = path.lastIndexOf("/");
      if (index < 0)
        index = 0;
      if (index > 0)
        path.delete(0, index + 1);
      if (suffix != null) {
        path.append('.');
        path.append(suffix);
      }
      return path.toString();
    } catch (URISyntaxException e) {
      assert (false);
    }
    return null;
  }

  protected static String ncdumpmetadata(NetcdfFile ncfile, String datasetname) throws Exception {
    StringWriter sw = new StringWriter();
    StringBuilder args = new StringBuilder("-strict");
    if (datasetname != null) {
      args.append(" -datasetname ");
      args.append(datasetname);
    }
    // Print the meta-databuffer using these args to NcdumpW
    try {
      Ncdump.ncdump(ncfile, args.toString(), sw, null);
    } catch (IOException ioe) {
      throw new Exception("NcdumpW failed", ioe);
    }
    sw.close();
    return sw.toString();
  }

  protected static String ncdumpdata(NetcdfFile ncfile, String datasetname) throws Exception {
    StringBuilder args = new StringBuilder("-strict -vall");
    if (datasetname != null) {
      args.append(" -datasetname ");
      args.append(datasetname);
    }
    // Dump the databuffer
    StringWriter sw = new StringWriter();
    try {
      Ncdump.ncdump(ncfile, args.toString(), sw, null);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new Exception("NCdumpW failed", ioe);
    }
    sw.close();
    return sw.toString();
  }

  protected static boolean check(int code) {
    return check(code, OKCODES);
  }

  protected static boolean check(int code, int[] ok) {
    for (int okcode : ok) {
      if (okcode == code)
        return true;
    }
    return false;
  }
}
