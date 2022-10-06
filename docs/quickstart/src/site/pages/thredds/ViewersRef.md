---
title: TDS Dataset Viewer Links
last_updated: 2022-10-06
sidebar: user_sidebar
toc: false
permalink: viewers_ref.html
---

Currently, the TDS automatically adds, when appropriate, the following _standard viewer links_ at the bottom of a datasets HTML web page:

* a [Godiva3](https://reading-escience-centre.gitbooks.io/ncwms-user-guide/content/04-usage.html#godiva3){:target="_blank"}  web-based WMS client link to any dataset that has a WMS service.
* a Jupyter Notebook viewer which provides the boilerplate python code to access remote data for a direct-access dataset.

#### Example

{% include image.html file="tds/reference/viewers/StandardViewers.png" alt="Viewers" caption="" %}

{%include note.html content="
Java WebStart [has been deprecated as of version 9](https://www.oracle.com/technetwork/java/javase/9-deprecated-features-3745636.html#JDK-8184998){:target='_blank'}, and we have removed such viewers from the TDS.
"%}

The TDS also supports two ways to configure other custom viewer links.

*  Add a viewer link by [adding a `viewer` property element to dataset](#adding-viewer-links-with-viewer-property-elements), explicitly listing the URL of the viewer.
*  Add a viewer link by [creating a Java class](#create-a-viewer-implementation-java-class) that tells the TDS what datasets are viewable, and what HTML fragment to include.

## Adding Viewer Links With `viewer` Property Elements

Dataset viewer links can be added to dataset HTML pages using `viewer` property elements.
To add a dataset viewer link to a specific dataset, add a `property` element that has a name starting with `viewer`.
When the TDS generates a dataset HTML page, it looks for all `viewer` property elements and uses the value of each `property` element to generate a viewer link.
The value of the `viewer` property element must be a string containing a URL and, optionally, a name, description, and `ViewerType` separated by commas.

Note that the parameters passed to the `viewer` property element are interpreted by order, so if you wish to include a `ViewerType` but no description or name, you will need to place empty strings as fillers.

The options for `ViewerType` are as follows:
* Application
* Browser
* Jupyter Notebook
* Unknown
  If no `ViewerType` or an unrecognized type is provided, the type will default to `Unknown`.

An HTML link is built using the `viewer` property element.

#### Example

~~~xml
<dataset name="Test Single Dataset" ID="testDataset" serviceName="odap" urlPath="test/testData.nc" dataType="Grid">
  <property name="viewer" value="https://www.unidata.ucar.edu/staff/caron/,MyViewer,A viewer for my data,Application"/>
  <property name="viewer2" value="https://www.unidata.ucar.edu/,,,Browser"/>
</dataset>
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
http://myhost:8080/wcsView/show?dataset=http://myhost:8080/thredds/wcs/tvss/ds2.nc
~~~

## Create A Viewer Implementation Java class

This technique gives you full control over whether your viewer link appears, and what the URL looks like.
You must create a Java class which implements the `thredds.server.viewer.Viewer` interface:

~~~java
public interface Viewer {
 /* Your class is passed a `thredds.catalog.InvDatasetImpl` object, and it returns `true` if it is viewable by your viewer. */   
 boolean isViewable( thredds.catalog.InvDatasetImpl dataset); 

 /* Your class is passed a viewable `thredds.catalog.InvDatasetImpl`, and it must return a well-formed HTML string that has an `href` link in it. */
 String getViewerLinkHtml( InvDatasetImpl ds, HttpServletRequest req);

 /* Your class is passed a viewable `thredds.catalog.InvDatasetImpl`, and it must return a `ViewerLink` object*/
 ViewerLinkProvider.ViewerLink getViewerLink(Dataset ds, HttpServletRequest req);
}
~~~

A `ViewerLink` is an abstraction of the HTML link to a dataset viewer, containing the following properties:

~~~java
  class ViewerLink {
  private String title;
  private String url;
  private String description;
  private ViewerType type;
}
~~~

#### Example

~~~java
package my.package;
import thredds.catalog.*;

public class IDV implements Viewer {
   public boolean isViewable( InvDatasetImpl ds) {
      // 1) Requires there to be OPeNDAP access for the dataset.
      InvAccess access = ds.getAccess(ServiceType.DODS);
      if (access == null) access = ds.getAccess(ServiceType.OPENDAP);
      if (access == null) return false;
      // 2) Requires the dataset to be of DataType.GRID.
      return (ds.getDataType() == DataType.GRID);
   }

   public String getViewerLinkHtml( InvDatasetImpl ds, HttpServletRequest req) {
      InvAccess access = ds.getAccess(ServiceType.DODS);
      // 3) Get the OPeNDAP access object for the dataset.
      if (access == null) access = ds.getAccess(ServiceType.OPENDAP);
      // 4) Get the access URI.
      URI dataURI = access.getStandardUri();
      // 5) Resolves the access URI against the request, which turns it into an absolute URI
      try {
         URI base = new URI( req.getRequestURL().toString());
         dataURI = base.resolve( dataURI);
      } catch (URISyntaxException e) {
         log.error("Resolve URL with " + req.getRequestURL(), e);
      }

      // 6) Forms the HTML string, with an embedded href, to be placed on the dataset's TDS web page.
      return "<a href='/thredds/view/idv.jnlp?url="+dataURI.toString()+
              "'>Integrated Data Viewer (IDV) (webstart)</a>";
   }
   
   public ViewerLinkProvider.ViewerLink getViewerLink(Dataset ds, HttpServletRequest req) {
       // 7) Create a ViewerLink object for the viewer
       return new ViewerLinkProvider.ViewerLink("viewerIDV", getViewerLinkHtml(ds, req), 
               "IDV view of the dataset", ViewerType.Application));
   }
}
~~~

### Referencing An External URL

If the viewer you want to reference is not part of the TDS, just make the href absolute, e.g.:

~~~xml
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

