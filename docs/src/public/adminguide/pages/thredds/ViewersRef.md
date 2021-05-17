---
title: TDS Dataset Viewer Links
last_updated: 2020-08-24
sidebar: admin_sidebar
toc: false
permalink: viewers_ref.html
---

Currently, the TDS automatically adds, when appropriate, the following _standard viewer links_ at the bottom of a datasets HTML web page:

* a [Godiva3](https://reading-escience-centre.gitbooks.io/ncwms-user-guide/content/04-usage.html#godiva3){:target="_blank"}  web-based WMS client link to any dataset that has a WMS service.
* a Jupyter Notebook viewer which provides the boilerplate python code to access remote data for a direct-access dataset.

#### Example

{% include image.html file="tds/reference/viewers/StandardViewers.png" alt="Viewers" caption="" %}

In previous versions of the TDS, we also included the following Java WebStart Viewers:

* a NetCDF-Java ToolsUI link to any direct dataset that has an ID.
* an IDV link to any dataset that has an OPeNDAP service and has a DataType of GRID.


{%include note.html content="
Java WebStart [has been deprecated as of version 9](https://www.oracle.com/technetwork/java/javase/9-deprecated-features-3745636.html#JDK-8184998){:target='_blank'}, and we have removed such viewers from the TDS.
"%}

The TDS also supports three ways to configure other custom viewer links.

*  Add a viewer link by [adding a `viewer` property element to dataset](#adding-viewer-links-with-viewer-property-elements), explicitly listing the URL of the viewer.
*  Add a viewer link by [creating a Java class](#create-a-viewer-implementation-java-class) that tells the TDS what datasets are viewable, and what HTML fragment to include.
*  If your viewer link points to a JNLP file, the TDS can [generate the JNLP file from a template file](#returning-a-jnlp-file).

Each of these methods are described in the sections below.
However, given the status of Java WebStart, _the third method is no longer recommended_.

## Adding Viewer Links With `viewer` Property Elements

Dataset viewer links can be added to dataset HTML pages using `viewer` property elements.
To add a dataset viewer link to a specific dataset, add a `property` element that has a name starting with `viewer`.
When the TDS generates a dataset HTML page, it looks for all `viewer` property elements and uses the value of each `property` element to generate a viewer link.
The value of the `viewer` property element must be a string containing a URL and a name separated by a comma.
An HTML link is built using the URL and the name.

#### Example

~~~xml
<dataset name="Test Single Dataset" ID="testDataset" serviceName="odap" urlPath="test/testData.nc" dataType="Grid">
  
  <property name="viewer" value="https://www.unidata.ucar.edu/staff/caron/,MyViewer"/>
  <property name="viewer2" value="https://www.unidata.ucar.edu/,MyOtherViewer"/>
</dataset>
~~~

results in the following HTML fragment:

~~~xml
<a href='https://www.unidata.ucar.edu/staff/caron/'>MyViewer</a>
<a href='https://www.unidata.ucar.edu/'>MyOtherViewer</a>
~~~

which looks like this on the TDS page:

{% include image.html file="tds/reference/viewers/AddViewers.png" alt="Added Viewers" caption="" %}

### Adding Viewer Links To Multiple Datasets

When a `viewer` property element is contained in an inherited `metadata` element, it will apply to all the descendants of the containing dataset.

#### Example

The following will result in viewer links for all children datasets:

~~~xml
<dataset name="Test inherited viewer" ID="tiv">
  <metadata inherited="true">
    <serviceName>all</serviceName>
    <property name="viewer" value="https://www.unidata.ucar.edu/staff/caron/,MyViewer" />
  </metadata>
  <dataset name="test inherited viewer ds 1" ID="tiv/ds1" urlPath="tiv/ds1.nc">
  <dataset name="test inherited viewer ds 2" ID="tiv/ds2" urlPath="tiv/ds2.nc">
</dataset>
~~~

When added to a datasetScan elements, the `viewer` property results in a viewer link being added to the HTML dataset pages for each generated dataset:

~~~xml
<datasetScan name="Test inherited viewer dsScan" ID="tivScan" path="tivScan" location="C:/some/good/data/">
  <metadata
  inherited="true">
    <serviceName>all</serviceName>
    <property name="viewer" value="https://www.unidata.ucar.edu/staff/caron/,MyViewer" />
  </metadata>
</datasetScan>
~~~

### Adding The Dataset URL To The Viewer Link

Adding the same viewer link to all your dataset pages may not be what you want.
The TDS also supports inserting a dataset access URL into the viewer link URL.
If your dataset has a single service, you can place `{url}` into your viewer link.
The datasets access URL will be substituted in place of the `{url}` string.
For instance, the following:

~~~xml
<dataset name="Test Viewer2" ID="testViewer2" serviceName="dapService" urlPath="test/testData.nc" dataType="Grid"
  <property name="viewer" value="http://some.tds.edu/cdmvalidator/validate?URL={url},Validation Service"/>
</dataset>
~~~

results in the following viewer link:

~~~xml
<a href="http://some.tds.edu/cdmvalidator/validate?URL=http://myhost:8080/thredds/dodsC/test/testData.nc">Validation Service</a>
~~~

### Selecting The Dataset Access URL Used In The Viewer Link

When a Dataset has more than one kind of access, each access will have a separate URL.
Use the service type inside of curly brackets to select which access URL to use.

#### Example

~~~xml
<service name="all" base="" serviceType="compound">
  <service name="odap" serviceType="OPENDAP" base="/thredds/dodsC/"/>
  <service name="http" serviceType="HTTPServer" base="/thredds/fileServer/"/>
  <service name="wcs" serviceType="WCS" base="/thredds/wcs/"/>
  <service name="wms" serviceType="WMS" base="/thredds/wms/"/>
  <service name="ncss" serviceType="NetcdfSubset" base="/thredds/ncss/"/>
  <service name="cdmremote" serviceType="CdmRemote" base="/thredds/cdmremote/"/>
  <service name="iso" serviceType="ISO" base="/thredds/iso/"/>
  <service name="ncml" serviceType="NCML" base="/thredds/ncml/"/>
  <service name="uddc" serviceType="UDDC" base="/thredds/uddc/"/>
</service>

<dataset name="test viewer select service" ID="tvss">
  <metadata inherited="true">
    <serviceName>all</serviceName>
  </metadata>

  <dataset name="test viewer select service ds 1" ID="tvss/ds1" urlPath="tvss/ds1.nc">
    <property name="viewer" value="http://some.tds.edu/cdmvalidator/validate?URL={OPENDAP},Validation Service" />
  </dataset>
  <dataset name="test viewer select service ds 2" ID="tvss/ds2" urlPath="tvss/ds2.nc">
    <property name="viewer" value="http://myhost:8080/wcsView/show?dataset={WCS},Validation Service" />
  </dataset>
</dataset>

~~~

generates a viewer link URL for the first dataset of:

~~~
http://some.tds.edu/cdmvalidator/validate?URL=http://myhost:8080/thredds/dodsC/tvss/ds1.nc
~~~

and for the second dataset, the viewer link is:

~~~
http://myhost:8080/wmsView/show?dataset=http://myhost:8080/thredds/wcs/tvss/ds2.nc
~~~

If your server is publicly accessible, this example calls the `some.tds.edu` validator service for your dataset, using OPeNDAP.
The dataset page now looks something like:

{% include image.html file="tds/reference/viewers/validateViewer.png" alt="Viewer with Data Access" caption="" %}

## Returning A JNLP File

Viewer links can also support on the fly generation of JNLP files.
This can be very useful when using data viewing software that can be started with a JNLP file (i.e., running under Java Webstart).
For instance, the automatically generated "IDV" and "NetCDF-Java Tools" viewer links mentioned above use JNLP files to start.
The JNLP generation can be used in other user configured viewer links as well.

### Adding JNLP Template Files

The TDS will return any JNLP template file under the `${tds.content.root.path}/thredds/views/` directory when requested with a URL that looks like:

~~~
http://localhost:8080/thredds/view/<filename>
~~~

#### Example

The URL:

~~~
http://localhost:8080/thredds/view/my/cool/viewer.jnlp
~~~

will look for and return the file:

~~~bash
${tds.content.root.path}/thredds/views/my/cool/viewer.jnlp
~~~

### Adding Dataset Information To The JNLP Template File

The TDS processes the JNLP template file before sending it to the client as the response to their request.
The processing looks for replacement strings of the form `{name}` and replaces them with the value of the corresponding URL query parameter.
So, if the JNLP template file contains any occurrences of the `{dataset}` string and the request URL looked like:

~~~
http://localhost:8080/thredds/view/my/cool/viewer.jnlp?dataset=http://some.other.server/thredds/dodsC/cool/data.nc
~~~

All occurrences of `{dataset}` would be replaced by `http://some.other.server/thredds/dodsC/cool/data.nc`.

So, looking at an approximation of the IDV JNLP file:

~~~xml
<?xml version="1.0" encoding="utf-8"?>
<!-- JNLP File for Integrated Data Viewer -->
<jnlp spec="1.0+" codebase="https://www.unidata.ucar.edu/software/idv/webstart/">
  <information>
    <title>Integrated Data Viewer</title>
    <vendor>Unidata</vendor>
    <homepage href="https://www.unidata.ucar.edu/software/idv/index.html"/>
    <description>Integrated Data Viewer(IDV)</description>
    <description kind="short">A tool for geoscientific analysis and visualization.
    </description>
    <icon href="IDV/idv.gif"/>
    <offline-allowed/>
  </information>
  <security>
   <all-permissions/>
  </security>
  <resources>
   <j2se version="1.4+" max-heap-size="512m"/>
   <jar href="IDV/idv.jar"/>
   <extension name="IDV Base" href="IDV/idvbase.jnlp"/>
  </resources>
  <application-desc main-class="ucar.unidata.idv.DefaultIdv">
   <argument>-data</argument>
   <argument>type:opendap.grid:{dataset}</argument>
  </application-desc>
</jnlp>
~~~

The third from the last line would be replaced with:

~~~xml
    <argument>type:opendap.grid:http://some.other.server/thredds/dodsC/cool/data.nc</argument>
~~~

Which passes the dataset access URL to the IDV as an argument.

## Create A Viewer Implementation Java class

This method is available in TDS version 3.14+.

This technique gives you full control over whether your viewer link appears, and what the URL looks like.
You must create a Java class which implements the [`thredds.server.viewer.Viewer`](https://docs.unidata.ucar.edu/tds/5.0/javadocAll/thredds/server/viewer/Viewer.html){:target="_blank"} interface:

~~~java
public interface Viewer {
 (1) public boolean isViewable( thredds.catalog.InvDatasetImpl dataset);

 (2) public String getViewerLinkHtml( InvDatasetImpl ds, HttpServletRequest req);
}
~~~

* (1) Your class is passed a `thredds.catalog.InvDatasetImpl` object, and it returns `true` if it is viewable by your viewer.
* (2) Your class is passed a viewable `thredds.catalog.InvDatasetImpl`, and it must return a well-formed HTML string that has an `href` link in it.

#### Example

~~~java
package my.package;
import thredds.catalog.*;

public class IDV implements Viewer {
   public boolean isViewable( InvDatasetImpl ds) {
      InvAccess access = ds.getAccess(ServiceType.DODS);
      if (access == null) access = ds.getAccess(ServiceType.OPENDAP);
(1)   if (access == null) return false;
(2)   return (ds.getDataType() == DataType.GRID);
   }

   public String getViewerLinkHtml( InvDatasetImpl ds, HttpServletRequest req) {
      InvAccess access = ds.getAccess(ServiceType.DODS);
(3)   if (access == null) access = ds.getAccess(ServiceType.OPENDAP);
(4)   URI dataURI = access.getStandardUri();
      try {
         URI base = new URI( req.getRequestURL().toString());
(5)      dataURI = base.resolve( dataURI);
      } catch (URISyntaxException e) {
         log.error("Resolve URL with " + req.getRequestURL(), e);
      }

(6)   return "<a href='/thredds/view/idv.jnlp?url="+dataURI.toString()+"'>Integrated Data Viewer (IDV) (webstart)</a>";
   }
}
~~~

* (1) Requires there to be OPeNDAP access for the dataset.
* (2) Requires the dataset to be of DataType.GRID.
* (3) Get the OPeNDAP access object for the dataset.
* (4) Get the access URI.
* (5) Resolves the access URI against the request, which turns it into an absolute URI
* (6) Forms the HTML string to be placed on the dataset's TDS web page.
  Note that is has an `href` embedded in it, which will be displayed in this example as:

    [Integrated Data Viewer (IDV) (WebStart)]()

### Referencing An External URL

If the viewer you want to reference is not part of the TDS, just make the href absolute, e.g.:

~~~
<a href='http://my.server/viewer?url=http://some.tds.edu/thredds/dodsC/model/data.grib2'>My Server</a>
~~~

In this example, the server would see the OPeNDAP data access URL and remotely read it.

### Loading Your Class At Runtime

You must place your Viewer class into the `${tomcat_home}/webapps/thredds/WEB-INF/lib` or `${tomcat_home}/webapps/thredds/WEB-INF/classes` directory.
(Previous instructions to place it into the ${tomcat_home}/shared directory doesn't work, because of classloader problems).

Then tell the TDS to load it by adding a line to the `${tds.content.root.path}/thredds/threddsConfig.xml` file.

#### Example

~~~xml
<viewer>my.package.MyViewer</viewer>
~~~

### Using A Generated JNLP File

A Viewer implementation can still use the TDS JNLP template service (see above).
It just needs to return the appropriate HTML link referencing an existing JNLP template file and giving the appropriate replacment URL query parameters.
The IDV implementation above does just that.

One reason to write an implementation of Viewer and use is JNLP is if the viewer has requirements for the datasets it can handle.
Looking at the IDV implementation above, we see it enforces two requirements:

1. the dataset must have an `OPeNDAP` (aka DODS) access URL, and
2. the dataset must be gridded data.

### Embedding The ToolsUI Viewers On Your Web Page

To call the [ToolsUI](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/toolsui_ref.html){:target="_blank"} application webstart application from your webpage, return this JNLP file:

~~~xml
<?xml version="1.0" encoding="utf-8"?>
<jnlp spec="1.0+" codebase="https://www.unidata.ucar.edu/software/netcdf-java/current/webstart">
    
   <information>
     <title>NetCDF Tools UI</title>
     <vendor>Unidata</vendor>
     <homepage href="https://www.unidata.ucar.edu/software/netcdf-java/"/>
     <description kind="short">GUI interface to netCDF-Java / Common Data Model</description>
     <icon href="nc.gif"/>
     <offline-allowed/>
   </information>
    
   <security>
     <all-permissions/>
   </security>
   
   <resources>
     <j2se version="1.6+" max-heap-size="1024m"/>
     <jar href="netcdfUI.jar"/>
     <extension name="netcdfUI Extra" href="netCDFtoolsExtraJars.jnlp"/>
   </resources>
   
   <application-desc main-class="ucar.nc2.ui.ToolsUI">
     <argument>{catalog}#{dataset}</argument>
   </application-desc>
</jnlp>
~~~

where you should replace:

* `{catalog}` with the absolute URL of the THREDDS catalog.
* `{dataset}` with the ID of the dataset you want the ToolsUI to view.

#### Example

~~~xml
  <application-desc main-class="ucar.nc2.ui.ToolsUI">
      <argument>http://thredds.ucar.edu/thredds/catalog/grib/NCEP/GFS/Global_0p25deg/latest.html?dataset=grib/NCEP/GFS/Global_0p25deg/GFS_Global_0p25deg_20170118_1200.grib2</argument>
 </application-desc>
~~~

If you don't specify the `<argument>`, ToolsUI will still startup normally, and not jump to the `THREDDS catalog` tab.

## Review Of How ToolsUI Works

When TDS gets this URL:

~~~
http://oos.soest.hawaii.edu/thredds/view/ToolsUI.jnlp?catalog=http://oos.soest.hawaii.edu/thredds/idd/nss_hioos.xml&dataset=NS02agg
~~~

it creates a JNLP file which is sent back to your browser.
If your browser has Java WebStart installed as a helper application (which happens when you install Java on your computer), the JNLP file is handled by the "Java plugin" on your browser, which downloads ToolsUI from wherever the jnlp file specifies.

The JNLP file has been customized to include the command line argument of the form `{catalog}#{dataset}`, and the ToolsUI application looks for this and uses it to open that catalog and display the named dataset in the `Catalog Chooser` tab.
This UI component gives access to all the metadata and access protocols of that dataset.
