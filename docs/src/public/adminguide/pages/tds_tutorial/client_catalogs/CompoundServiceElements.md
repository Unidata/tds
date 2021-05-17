---
title: Compound Service Elements
last_updated: 2020-08-24
sidebar: admin_sidebar
toc: false
permalink: compound_service_elements.html
---

Datasets can be made available through more than one access method by defining and then referencing a _compound_ `service` element. 
The following:

~~~xml
<service name="all" serviceType="Compound" base="" >
  <service name="odap" serviceType="OpenDAP" base="/thredds/dodsC/" />
  <service name="wcs" serviceType="WCS" base="/thredds/wcs/" />
</service>
~~~

defines a compound service named `all` which contains two nested services.
Any dataset that reference the compound service will have two access methods. 
For instance:

~~~xml
<dataset name="SAGE III Ozone 2006-10-31" urlPath="sage/20061031.nc" ID="20061031.nc">
  <serviceName>all</serviceName>
</dataset>
~~~

would result in these two access URLs, one for OpenDAP access 

~~~xml
/thredds/dodsC/sage/20061031.nc
~~~

and, one for WCS access:

~~~xml
/thredds/wcs/sage/20061031.nc
~~~

The contained services can still be referenced independently.
For instance:

~~~xml
<dataset name="Global Averages" urlPath="sage/global.nc" ID="global.nc">
  <serviceName>odap</serviceName>
</dataset>
~~~

results in a single access URL:

~~~xml
/thredds/dodsC/sage/global.nc
~~~

{%include note.html content="
A complete listing of [recognized service types](client_side_catalog_specification.html#service-element) can be found in the [Client Catalog Specification](client_side_catalog_specification.html).
" %}
