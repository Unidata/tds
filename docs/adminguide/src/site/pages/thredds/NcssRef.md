---
title: NetCDF Subset Service Reference
last_updated: 2020-08-29
sidebar: admin_sidebar
toc: false
permalink: netcdf_subset_service_ref.html
---

## Overview

The NetCDF Subset Service (NCSS) is a web service for subsetting [CDM scientific datasets](https://docs.unidata.ucar.edu/netcdf-java/5.4/userguide/reading_cdm.html){:target="_blank"}. 
The subsetting is specified using earth coordinates, such as lat/lon or projection coordinates bounding boxes and date ranges, rather than index ranges that refer to the underlying data arrays. 
The data arrays are subsetted but not re-sampled or reprojected, and preserve the resolution and accuracy of the original dataset.

NCSS supports spatial and temporal subsetting on collections of grids, stations and points. 
A Dataset is described by a **Dataset Description** XML document, which describes the dataset in enough detail to enable a programmatic client to form valid data requests.

NCSS may return netCDF binary files (using [CF Conventions](http://cfconventions.org/latest.html){:target="_blank"}), `XML`, `ASCII`, or [WaterML2](https://www.ogc.org/standards/waterml){:target="_blank"} depending on the request and the dataset.

NCSS uses `HTTP GET` with key-value pairs (KVP) which may appear in any order. 
The service interface follows [REST design](https://en.wikipedia.org/wiki/Representational_state_transfer), as well as [Google/KML](https://developers.google.com/kml/){:target="_blank"},  and [W3C XML Schema Datatypes](https://www.w3.org/TR/xmlschema-2/){:target="_blank"}  when applicable.

### Dataset Descriptions

Each dataset has an XML document called the **Dataset Description Document**. 
These are intended to perform the same function as [OGC GetCapabilities](https://github.com/7o9/implementer-friendly-standards/blob/master/introduction.rst#getcapabilities){:target="_blank"}  or [Atom Introspection](https://movabletype.org/documentation/developer/api/atom-legacy/atom-introspection.html){:target="_blank"} , that is, provide clients with the necessary information to formulate a valid request and send it to the server. 
**The content of these documents is still evolving**.

#### Grid Dataset

A Grid Dataset is a collection of Grids which have horizontal (x,y) coordinates, and optional vertical, time, and ensemble coordinates. 
Grid data points next to each other in index space are next to each other in coordinate space.

Example of a Grid Dataset document:
* [DGEX CONUS 12km Data](https://www.unidata.ucar.edu/staff/sarms/tdsDox/temp/tds/reference/files/gridsDataset.xml){:target="_blank"}


#### Station Dataset

Station datasets contain a collection of point features representing time-series of data points at named locations.

Example a Station Dataset document:
* [Metar Station Data](https://www.unidata.ucar.edu/software/tds/current/reference/files/stationsDataset.xml){:target="_blank"}

Station datasets also support station list requests. 
This request will return all the stations within a specified bounding box. 

{% include note.html content="
The station dataset feature was broken in TDS version 4.5. and was fixed in 4.5.4
" %}

#### Point Dataset

Point datasets contain a collection of point features which can be subset by space and time.

Example Point Dataset document:
* [Surface Buoy Point Data](https://www.unidata.ucar.edu/software/tds/current/reference/files/pointDataset.xml){:target="_blank"}
   
## Subsetting Parameters For NCSS

* [Grid datasets](ncss_grid.html)
* [Point and Station (Discrete Sampling Geometry) datasets](ncss_point.html)

### Variable Subsetting

For all types, you must specify a list of valid variable names.

#### Horizontal Spatial Subsetting

* `stations`: you may specify a lat/lon bounding box, a point location, or a list of stations. 
If none, do not filter.

* `points`: you may specify a lat/lon bounding box. 
If none, do not filter.

* The lat/lon bounding box declared in the dataset description is an approximated rectangle to the actual lat/lon boundaries so there may be valid points within the data but outside of the declared bounding box in the dataset description

#### Vertical Spatial Subsetting

* Variable with vertical levels will be returnd as profiles, unless a specific level is chosen.

#### Temporal Subsetting

* For all types, you may specify a time range or a specific time. 
If none, return the time closest to the present.

* A time range will request all features that intersect the range.

* A time point will request the feature that is closest to that time.

* If you include temporal=all, then return all times.

#### Output Format (`accept` Parameter)
* `csv`: Comma-separated values, one feature per line
* `xml`: Collection of feature elements
* `netCDF`: CF/NetCDF-3
* `netCDF4`: CF/NetCDF-4 classic model
* `netCDF4ext`: NetCDF-4 extended model
* `WaterML2`: OGC WaterML 2.0 Timeseries (station only)

## Use Cases

### Spatial And Coordinate Subsetting

NCSS provides two types of bounding boxes to subset the data:

1. **Lat/lon bounding box** is specified with the parameters `north`, `south`, `east` and `west`. 
The `north` and `south` parameters are latitude values, and must be in units of `degrees_north` and lie between +/- 90. 
The `east` and `west` parameters are longitude values with units of `degrees_east`, and may be positive or negative, and will be interpreted modulo 360. 
The requested subset starts at the west longitude and goes eastward until the east longitude. 
Therefore, when crossing the dateline, the west edge may be greater than the east edge. 
For grids, if the underlying dataset is on a projection, the minimum enclosing projection bounding box will be calculated and used. 
The data contained in the intersection of this rectangle with the data is returned. 
To use, inspect `dataset.xml` for the `<LatLonBox>` elements, which indicate the min and max extensions of the grid.

   For example:

   ~~~xml
   <LatLonBox>
     <west>-153.5889</west>
     <east>-48.5984</east>  
     <south>11.7476</south>
     <north>57.4843</north>
   </LatLonBox>
   ~~~

   Example request:
   
   ~~~
   &north=17.3&south=12.088&west=140.2&east=160.0
   ~~~
   
2. **Projection bounding box** (only on grid datasets with projections) is specified with the parameters `minx`, `miny`, `maxx` and `maxy`. 
These are projection coordinates in `kilometers` on the projection plane; the data contained in the intersection of this rectangle with the data is returned. 
To use, inspect the `dataset.xml` for the `<projectionBox>` elements, which indicate the min and max extensions of the grid. 

   For example:
  
   ~~~xml 
   <gridSet name="time layer_between_two_pressure_difference_from_ground_layer y x">
     <projectionBox>
       <minx>-4264.248291015625</minx>
       <maxx>3293.955078125</maxx>
       <miny>-872.8428344726562</miny>
       <maxy>4409.772216796875</maxy>
     </projectionBox>
   ...
    ~~~
   
    Example request:
   
    ~~~
    &minx=-500&miny=-1600&maxx=500&maxy=0
    ~~~

    By default, if no spatial subsetting is specified, the service returns all the features in the dataset. 
   
### Single-Point Requests   

The NetCDF Subset Service allows the user to extract data for a point of interest by specifying its latitude and longitude. 
The result differs depending on the underlying dataset.

If it's a grid dataset, that means we are using the grid-as-point service. 
NCSS will find the grid cell in which the lat/lon falls and return its data as if it were a point feature. 
The supported output formats are `netCDF`, `netCDF4`, `XML`, and `CSV`.

If it's a station dataset, NCSS will return data for the station nearest the specified lat/lon. 
The supported output formats are `netCDF`, `netCDF4`, `XML`, `CSV` and `WaterML2`.

Point datasets do not support single-point requests.

For example:

~~~
?req=station&var=temp&latitude=40.2&longitude=61.8
~~~

This finds the station nearest to (`lat=40.2, lon=61.8`) and returns its temperature data.  

### Temporal Subsetting And Valid Time Ranges

There are several ways to do temporal subsetting requests:

* `Default`: If no temporal subseting is specified, the closest time to the current time is returned.
* `All time range`: A shorthand to request all the time range in a dataset is setting the parameter `time=all`. 
This can also be done by providing a valid temporal range containing the entire dataset time range.
* `One single time`: Passing the parameter time will get the time slice closest to the requested time if it is within the time range of the dataset.
* `Valid time range`: A valid time range is defined with two of the three parameters: `time_start`, `time_end`, and `time_duration`.

Times (`time`, `time_start`, and `time_end`) must be specified as [W3C Date](ncss_grid.html#w3c-date) or `present` and `time_duration` as a W3C time duration.

Examples of time query strings with valid temporal ranges:

* `time_start=2007-03-29T12:00:00Z&time_end=2007-03-29T13:00:00Z` (between 12 and 1 pm Greenwich time)
* `time_start=present&time_duration=P3D` (get 3 day forecast starting from the present)
* `time_end=present&time_duration=PT3H` (get last 3 hours)
* `time=2007-03-29T12:00:00Z`
* `time=present`
* `time=all`

### Vertical Coordinate Subsetting

Subsetting on the vertical axis of a variable or variables with the same vertical levels may be done with the `vertCoord` parameter.

By default, all vertical levels are returned.

### Single Variable Requests

Note that these single variable requests can be easily extended to multi-variable request by simply passing a comma separated list of variables in the `var= parameter`. 
Please note that for grid datasets, each variable in the request must have the same vertical levels.

#### Examples:

|----|-------------------|--------------------------------|
| # |  Request           | Query String                   |
|:---|:------------------|:-------------------------------|
| 1 | All of the data for the variable `Temperature_pressure` for the closest time to the current time. | `?var=Temperature_pressure&temporal=all` |
| 2 | All of the data for the variable `Temperature_pressure` available in a given time range. |`?var=Temperature_pressure&time_start=2015-08-19Z&time_end=2015-08-20T12:00:00Z` |
| 3 | All of the data for the variable T`emperature_pressure` for a specific time. |`?var=Temperature_pressure&time=2015-09-06T00:00:00Z` |
| 4 | Subset the data for the variable `Temperature_pressure` over a given lat/lon bounding box for a specific time. | `?var=Temperature_pressure&time=2015-09-06T00:00:00Z&north=41&west=-109.05&east=-102.05&south=37` |
| 5 | `Temperature_pressure` for every 5th point on the grid (`deltax=deltay=5`) | `?var=Temperature_pressure&horizStride=5` |
| 6 | `Temperature_pressure` for every 5th point on the grid over a given lat/lon bounding box. |`?var=Temperature_pressure&north=41&west=-109.5&east=-102.5&south=37&horizStride=5` |
| 7 | `Temperature_pressure` at a particular vertical level: `1000 mb` (see note below). |`?var=Temperature_pressure&vertCoord=1000` |
| 8 | `Air_temperature` for stations named `LECO`, `LEST`, and `LEVX`. | `?var=air_temperature&subset=stns&stns=LECO,LEST,LEVX` |

{% include note.html content="
In example 7 above, the vertical level value must be in the same units used in the dataset - in this example we assume millibars but you will need to check the dataset description to be sure.
" %}

## URL Construction

~~~
http://{host}/{context}/{service}/{dataset}[/{description} | ?{query}]
~~~

Where:


|  `{host}`                | = | server name, e.g., `thredds.ucar.edu` |
|  `{context}`             | = | `thredds` (usually) |
|  `{service}`             | = | `ncss/grid` or `ncss/point`, depending on if the dataset has feature type Grid or one of the Point types. |
|  `{dataset}`             | = | logical path for the dataset, obtained from the catalog. |
|  `{description}`         | = | `dataset.[xml\|html]` or  `pointDataset.[xml\|html]`  or `datasetBoundaries.xml`  or `stations.xml`. |
|  `dataset.[xml\|html]`    | = | the dataset description in `XML` or as a web form (Point or Grid). |
|  `pointDataset.[xml\|html]` | = | the grid-as-point or the point dataset description in `XML` or as a web form. |
|  `datasetBoundaries.xml`  | = | the description of the bounding boxes for Grid datasets. |
|  `station.xml`            | = | the list of valid stations for a station dataset. |
|  `{query}`                | = | the `KVP`s to describe the subset that you want (see below for valid combinations). |

