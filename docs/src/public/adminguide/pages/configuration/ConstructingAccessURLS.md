---
title: Constructing Access URLS
last_updated: 2020-10-03
sidebar: admin_sidebar
toc: true
permalink: constucting_access_urls.html
---


## Example 
Here's an example of a very simple catalog:

~~~xml
<?xml version="1.0" ?>  <!-- 1 -->
<catalog xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0" > <!-- 2 -->
  <service name="odap" serviceType="OpenDAP" base="/thredds/dodsC/" /> <!-- 3 -->
  <dataset name="SAGE III Ozone 2006-10-31" serviceName="odap" urlPath="sage/20061031.nc" ID="20061031.nc"/> <!-- 4 -->
</catalog> <!-- 5 -->
~~~

with this line-by-line explanation:

1. The first line indicates that this is an XML document.
2. This is the `root` element of the XML, the `catalog` element.
   It must declare the THREDDS catalog namespace with the `xmlns` attribute exactly as shown.
3. This declares a `service` named `odap` that will serve data via the OpenDAP protocol.
   Many other data access services come bundled with THREDDS.
4. This declares a `dataset` named `SAGE III Ozone 2006-10-31`. 
   It references the `odap` `service` on line 3, meaning it will be served via OpenDAP.
   The `URLPath` of `sage/20061031.nc` will provided a needed compontent to build the access URL 
5. This closes the `catalog` element.

Using the catalog directly above, here are the steps for client software to construct a dataset access URL:

1. Find the `service` referenced by the dataset:

   ~~~xml
   <service name="odap" serviceType="OpenDAP" base="/thredds/dodsC/" />
   <dataset name="SAGE III Ozone 2006-10-31" serviceName="odap" urlPath="sage/20061031.nc" ID="20061031.nc"/>
   ~~~

2. Append the `service base` path to the server root to construct the service base URL:
   * serverRoot = `http://hostname:port`
   * serviceBasePath = `/thredds/dodsC/`
   * serviceBaseUrl = `serverRoot + serviceBasePath = `http://hostname:port/thredds/dodsC/`
3. Find the `urlPath` referenced by the `dataset`:
   ~~~xml
   <dataset name="SAGE III Ozone 2006-10-31" serviceName="odap" urlPath="sage/20061031.nc" ID="20061031.nc"/>
   ~~~
4. Append the `dataset` `urlPath` to the `service base` URL to get the dataset access URL:
   * serviceBaseUrl = `http://hostname:port/thredds/dodsC/`
   * datasetUrlPath = `sage/20061031.nc`
   * datasetAccessUrl = serviceBaseUrl + datasetUrlPath = `http://hostname:port/thredds/dodsC/sage/20061031.nc`

In summary, construct a URL from a client catalog with these 3 pieces:

~~~
http://hostname:port/thredds/dodsC/sage/20061031.nc
<------------------><------------><--------------->
     server            service         dataset
~~~
