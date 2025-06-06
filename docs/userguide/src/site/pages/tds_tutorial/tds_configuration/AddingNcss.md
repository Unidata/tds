---
title: Adding The NetCDF Subset Service
last_updated: 2020-08-24
sidebar: user_sidebar
toc: false
permalink: adding_ncss.html
---

The NetCDF Subset Service (NCSS) is one of the ways that the TDS can serve data.
It is an experimental REST protocol for returning subsets of CDM datasets.
We want to eventually serve all CDM-compatible datasets through NCSS, but right now there are some restrictions on the types of datasets that are supported.

This documentation is for TDS administrators.
If you are a client wanting to access data through the NetCDF Subset Service, look at [NetCDF Subset Service Reference](netcdf_subset_service_ref.html).

## Enabling NCSS In the TDS

The NetCDF Subset Service must be enabled in the `threddsConfig.xml` configuration file before it can be used.
This is done by adding an `allow` element to the `NetcdfSubsetService` element as follows:

~~~xml
<NetcdfSubsetService>
    <allow>true</allow>
</NetcdfSubsetService>
~~~

{% include note.html content="
Details on other configuration options for NCSS are available in the [`threddsConfig.xml`](tds_config_ref.html) documentation.
"
%}

## Serving Datasets With NCSS

In your configuration catalogs, you must define the service based on the type of data being served.
For Feature Type `GRID`, use:

~~~xml
<service name="ncssGrid" serviceType="NetcdfSubset" base="/thredds/ncss/grid/" />
~~~

For Feature Types `POINT` or `STATION`, use:

~~~xml
<service name="ncssPoint" serviceType="NetcdfSubset" base="/thredds/ncss/point/" />
~~~

Then as usual, add the service to any datasets that you want served, e.g.:

~~~xml
<dataset name="datasetName" ID="datasetID" urlPath="/my/urlPath"> 
   <serviceName>ncssGrid</serviceName> 
</dataset> 
~~~

Note that the name of the service (`ncssGrid` in this example) is arbitrary, but the `serviceType` and base must be _exactly_ as shown.

## Restrictions On What Files Can Be Served

First, only datasets in the format that the CDM can read are supported. 
Second, the data must represent one of the following Feature Types: `GRID`, `POINT`, `STATION`.

## Verifying A Dataset Is Gridded

Open your file in the [ToolsUI](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/toolsui_ref.html){:target="_blank"} program, using the `FeatureTypes->Grids` tab.
Any fields identified as grids will show up in the top table.
To be sure, go into the viewer (click ![redraw](images/tds/tutorial/tds_configuration/redraw.gif) to bring up the Viewer, then click ![redraw](images/tds/tutorial/tds_configuration/redraw.gif) again to show the data) and make sure the data is displayed correctly.
If all that works, then the data should be served correctly by the TDS.

## Verifying A Dataset Is Pointed

Open your file in the `ToolsUI` program using the `FeatureTypes->PointFeature` tab.
In the top table, verify that `featureType` is either `POINT` or `STATION`.
