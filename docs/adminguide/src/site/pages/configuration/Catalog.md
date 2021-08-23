---
title: catalog.xml
last_updated: 2020-10-03
sidebar: admin_sidebar
toc: true
permalink: catalog.html
---


##  Default TDS Configuration Catalog 
The main TDS configuration catalog is at `${tds.content.root.path}/thredds/catalog.xml`.
It is also referred to as the **root catalog**.

### Default Root Catalog
Here is the default root `catalog.xml` file that is shopped with the TDS: 

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<catalog name="THREDDS Server Default Catalog : You must change this to fit your server!"
         xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
         xmlns:xlink="http://www.w3.org/1999/xlink"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0
           http://www.unidata.ucar.edu/schemas/thredds/InvCatalog.1.0.6.xsd">
  
  <service name="all" base="" serviceType="compound">
    <service name="odap" serviceType="OpenDAP" base="/thredds/dodsC/"/>
    <service name="dap4" serviceType="DAP4" base="/thredds/dap4/"/>
    <service name="http" serviceType="HTTPServer" base="/thredds/fileServer/"/>
    <service name="wcs" serviceType="WCS" base="/thredds/wcs/"/>
    <service name="wms" serviceType="WMS" base="/thredds/wms/"/>
    <service name="ncssGrid" serviceType="NetcdfSubset" base="/thredds/ncss/grid/"/>
    <service name="ncssPoint" serviceType="NetcdfSubset" base="/thredds/ncss/point/"/>
    <service name="cdmremote" serviceType="CdmRemote" base="/thredds/cdmremote/"/>
    <service name="iso" serviceType="ISO" base="/thredds/iso/"/>
    <service name="ncml" serviceType="NCML" base="/thredds/ncml/"/>
    <service name="uddc" serviceType="UDDC" base="/thredds/uddc/"/>
  </service>

  <datasetRoot path="test" location="content/testdata/" />

  <dataset name="Test Grid Dataset" ID="testGrid"
           serviceName="all"  urlPath="test/crossSeamProjection.nc" dataType="Grid"/>

  <dataset name="Test Point Dataset" ID="testPoint"
           serviceName="all" urlPath="test/H.1.1.nc" dataType="Point"/>
  
  <dataset name="Test Station Dataset" ID="testStation"
           serviceName="all" urlPath="test/H.2.1.1.nc" dataType="Point"/>

  <datasetScan name="Test all files in a directory" ID="testDatasetScan"
               path="testAll" location="content/testdata">
    <metadata inherited="true">
      <serviceName>all</serviceName>
    </metadata>
  </datasetScan>

  <catalogRef xlink:title="Test Enhanced Catalog" xlink:href="enhancedCatalog.xml" name=""/>
</catalog>
~~~

### How The TDS Loads Configuration Catalog Data

When the TDS starts, the root configuration catalog (`catalog.xml`) is read.  


You can include references to other configuration catalogs in `catalog.xml`, using the `catalogRef` elements.
On the second to the last line in the [above `catalog.xml`](#default-root-catalog), the `catalogRef` element loads another configuration catalog called `enhancedCatalog.xml`.
~~~xml
 <catalogRef xlink:title="Test Enhanced Catalog" xlink:href="enhancedCatalog.xml" name=""/>
~~~

`enhancedCatalog.xml` is another test configuration catalog that comes with the TDS.
It serves as an example of how to include/use other catalog files.

Upon startup, the TDS reads the configurations in `catalog.xml` and all of the other _included_ catalogs (e.g., `enhancedCatalog.xml`) referenced by the `catalogRef` element.
 
 The result is the creation of a "tree of catalogs" are served by the TDS as the top-level client catalogs.


### Accessing The Root Catalog As A Client Catalog

The [relationship](configuration_overview.html#knowing-the-difference-between-the-two) between the configuration catalogs and the client catalogs is described in the [Configuration Overview](configuration_overview.html#knowing-the-difference-between-the-two) section.  
As a reminder, the TDS configuration catalogs represent the top-level client catalogs served by the TDS. 
Specifically, the TDS takes the information in the configuration catalogs and uses it to generate the client catalogs. 

That said, the main root configuration catalog (`${tds.content.root.path}/thredds/catalog.xml`),  can be accessed as a client catalog at:

~~~
www.server.com/thredds/catalog/catalog.xml
~~~

In the case of our `enhancedCatalog.xml` test catalog, the tree looks like:

~~~
catalog.xml
    |
    |-- enhancedCatalog.xml
~~~

The nested catalog, `enhancedCatalog.xml`, is exposed to end users in a `<catalogRef>` element in the client catalog:

~~~xml
<catalogRef xlink:title="Test Enhanced Catalog" name="Test Enhanced Catalog" xlink:href="enhancedCatalog.xml"/>
~~~

and, can be accessed at the provided by the `xlink:href`, which in this case is:

~~~
www.server.com/thredds/catalog/enhancedCatalog.xml
~~~

The tree of configuration catalogs can be as deeply nested as desired.

## Additional Root Catalogs

Additional root configuration catalogs can be defined in the `${tds.content.root.path}/thredds/threddsConfig.xml` file. 
For instance, to add a test catalog add the following line to `threddsConfig.xml`:

~~~xml
<catalogRoot>myTestCatalog.xml</catalogRoot>
~~~

Each additional root configuration catalog can be the root of another tree of configuration catalogs.
To access the new root as an end user, you would visit: 

~~~
www.server.com/thredds/catalog/myTestCatalog.xml
~~~