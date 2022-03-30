---
title: THREDDS Metadata
last_updated: 2020-08-26
sidebar: quickstart_sidebar
toc: false
permalink: thredds_metadata.html
---

## What Is Metadata?

The term *metadata* refers to "data about data". 
The term is ambiguous, as it is used for two fundamentally different concepts. 
*Structural metadata* is about the design and specification of *data structures* and is more properly called "data about the containers of data"; *descriptive metadata*, on the other hand, is about individual instances of application data, the *data content*.
- [Wikipedia Metadata entry](https://en.wikipedia.org/wiki/Metadata){:target="_blank"}


## Metadata Tour On Several TDS Sites

* [NOAA Pacific Fisheries Environmental Lab](http://oceanwatch.pfeg.noaa.gov/thredds/catalog.xml){:target="_blank"}
* [Unidata IDD Real-time Archive](https://thredds.ucar.edu:8080/thredds/catalog.xml){:target="_blank"}

## Introduction

A *simple catalog* may contain very minimal information about its datasets, at minimum just a name, a service and a URL for each dataset. 
An *enhanced catalog* is one in which you have added enough metadata that its possible to create a Digital Library record for import into one of the Data Discovery Centers like [IDN](http://gcmd.gsfc.nasa.gov/){:target="_blank"}.

The THREDDS catalog specification allows you to add all kinds of metadata, in fact, you can put any information you want into metadata elements by using separate XML namespaces. 
The TDS comes with an example enhanced catalog that contains a recommended set of metadata that you can use as a template. 
We recommend that you aim for this level of metadata in all the datasets you want to publish.

## Annotated Example

The example enhanced catalog lives at `${tomcat_home}/content/thredds/enhancedCatalog.xml`:

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<catalog xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    name="Unidata THREDDS/IDD Data Server" version="1.0.1">  <!-- 1 -->

  <service name="latest" serviceType="Resolver" base="" />   <!-- 2 -->
  <service name="both" serviceType="Compound" base="">       <!-- 3 -->
    <service name="ncdods" serviceType="OPENDAP" base="/thredds/dodsC/" />
    <service name="HTTPServer" serviceType="HTTPServer" base="/thredds/fileServer/" />
  </service>

  <dataset name="NCEP Model Data">     <!-- 4 -->
    <metadata inherited="true">         <!-- 5 -->
      <serviceName>both</serviceName>
      <authority>edu.ucar.unidata</authority>
      <dataType>Grid</dataType>
      <dataFormat>NetCDF</dataFormat>
      <documentation type="rights">Freely available</documentation>
      <documentation xlink:href="http://www.emc.ncep.noaa.gov/modelinfo/index.html" 
                     xlink:title="NCEP Model documentation" />

      <creator>
        <name vocabulary="DIF">DOC/NOAA/NWS/NCEP</name>
        <contact url="http://www.ncep.noaa.gov/" email="http://www.ncep.noaa.gov/mail_liaison.shtml" />
      </creator>
      <publisher>
        <name vocabulary="DIF">UCAR/UNIDATA</name>
        <contact url="http://www.unidata.ucar.edu/" email="support@unidata.ucar.edu" />
      </publisher>
      <timeCoverage>
        <end>present</end>
        <duration>14 days</duration>
      </timeCoverage>
    </metadata>

    <datasetScan name="ETA Model/CONUS 80 km" ID="NCEP-ETA"
                       path="testEnhanced" location="content/dodsC/data/">   <!-- 6 -->

      <metadata inherited="true">   <!-- 7 -->
        <documentation
  type="summary">NCEP North American Model : AWIPS 211 (Q) Regional - CONUS (Lambert Conformal).
 Model runs are made at 12Z and 00Z, with analysis and forecasts every 6 hours out to 60 hours.
 Horizontal = 93 by 65 points, resolution 81.27 km, LambertConformal projection.
 Vertical = 1000 to 100 hPa pressure levels.</documentation>   <!-- 8 -->

        <geospatialCoverage>            <!-- 9 -->
          <northsouth>
            <start>26.92475</start>
            <size>15.9778</size>
            <units>degrees_north</units>
          </northsouth>
          <eastwest>
            <start>-135.33123</start>
            <size>103.78772</size>
            <units>degrees_east</units>
          </eastwest>
        </geospatialCoverage>

        <variables vocabulary="CF-1">    <!-- 10 -->
          <variable name="Z_sfc" vocabulary_name="geopotential_height" 
                    units="gp m">Geopotential height, gpm</variable>
        </variables>
      </metadata>

      <filter>    <!-- 11 -->
        <include wildcard="*eta_211.nc" />
      </filter>
      <addDatasetSize/>
      <addProxies/>
        <simpleLatest />
      </addProxies>
      <addTimeCoverage datasetNameMatchPattern="([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})_eta_211.nc$" 
                       startTimeSubstitutionPattern="$1-$2-$3T$4:00:00" duration="60 hours" />
    </datasetScan>
  </dataset>
</catalog>
~~~

### Annotations

1. This is the standard `catalog` element for version 1.0.1. The only thing you should change is the name.
2. You need this service in order to use the `addProxies` child element of the `datasetScan` element.
3. This is a compound service gives access to the datasets both through OpenDAP and through HTTP file transfer.
4. This is a collection level dataset that we added in order to demonstrate factoring out information. It's not particularly needed in this example, which only contains one nested dataset (the datasetScan at (6)), but for more complicated situations its very useful.
5. The metadata element that's part of the collection dataset at (4). Because it has `inherited=true`, everything in it will apply to the collection's nested datasets. The specific fields are ones that often can be factored out in this way, but your catalog may be different.
   1. `serviceName`: indicates that all the nested datasets will use the compound service named *both*.
   2. `authority`: used to create globally unique dataset IDs. Note the use of *reverse DNS naming*, which guarantees global uniqueness.
   3. `dataType`: all datasets are of type *Grid*.
   4. `dataFormat`: all datasets have file type *NetCDF*.
   5. `rights`: a documentation element indicating who is allowed to use the data.
   6. `documentation`: an example of how to embed links to web pages.
   7. `creator`: who created the dataset. Note that we used standard names from [IDN DIF vocabulary](https://idn.ceos.org/){:target="_blank"}.
   8. `publisher`: who is serving the dataset
   9. `timeCoverage`: the time range that the collection of data covers. In this example, there are 14 days of data available in the collection, ending with the present time.
6. The `datasetScan` element dynamically creates a subcatalog by scanning the directory named by `location`. 
   The `name` attribute is used as the title of DL records, so try to make it concise yet descriptive. 
   The `ID` is also very important. See [here](/tds_dataset_scan_ref.html) for a full description of the `datasetScan` element.
7. This metadata also applies to all the dynamically created datasets in the `datasetScan` element.
8. The summary `documentation` is used as a paragraph-length summary of the dataset in Digital Libraries. 
   Anyone searching for your data will use this to decide if it's the data they are looking for.
9. The `geospatialCoverage` is a lat/lon (and optionally elevation) bounding box for the dataset.
10. The `variables` element list the data variables available in the dataset.
11. There are a number of special instructions for datasetScan (see here for the gory details). 
    The `filter` element specifies which files and directories to include or exclude from the catalog. 
    The `addDatasetSize` element indicates that a `dataSize` element should be added to each atomic dataset. 
    The `addProxies` element causes resolver datasets to be added at each collection level when accessed resolve to the latest dataset at that collection level. 
    This is useful for real-time collections. 
    The `addTimeCoverage` dynamically adds a `timeCoverage` element to the individual datasets in the collection, which will override the timeCoverage inherited from the collection dataset metadata at (5). 
    This is useful for the common case that all the datasets in a collection differ only in their time coverage.

## Resources

* [THREDDS Catalog Client Specification](/InvCatalogSpec.html#dlElements)

## Metadata Standards

There are a number of existing metadata standards available for describing datasets. 
These include:
  
* [Dublin Core](https://dublincore.org/){:target="_blank"} - general library discovery metadata standard
* [International Directory Network (IDN)](https://idn.ceos.org/){:target="_blank"}  - standard for geophysical data
* [ISO 19115](https://www.isotc211.org/){:target="_blank"} - standard for geophysical data (FGDC is merging/synchronizing with this ISO standard)

### Including Metadata Records in THREDDS Catalogs

Any metadata records can be included directly in or referenced from a THREDDS `metadata` element. 
Here is an example of how to include a Dublin Core record directly in a THREDDS `metadata` element:

~~~xml
<metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
  <dc:title>NCEP GFS Model - Alaska 191km </dc:title>
  <dc:creator>NOAA/NCEP</dc:creator>
  ...
</metadata>
~~~

Here is an example of how to reference a `metadata` record (`xlink` attributes are used):

~~~xml
<metadata xlink:title="NCEP GFS Model - Alaska 191km"
          xlink:href="http://server/dc/ncep.gfs.alaska_191km.xml" />
~~~

### What's The Difference Between Metadata And Documentation?

When the material is an XML file meant for software to read, use a `metadata` element. 
When it's an HTML page meant for a human to read, use a `documentation` element:

~~~xml
<documentation xlink:title="My Data" xlink:href="http://my.server/md/data1.html" />
~~~
