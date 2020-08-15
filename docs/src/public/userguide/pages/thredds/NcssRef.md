---
title: Netcdf Subset Service Reference
last_updated: 2020-08-14
sidebar: tdsTutorial_sidebar
toc: false
permalink: netcdf_subset_service_ref.html
---

## Overview

The NetCDF Subset Service (NCSS) is a web service for subsetting [CDM scientific datasets](https://docs.unidata.ucar.edu/netcdf-java/5.4/userguide/reading_cdm.html){:target="_blank"}. 
The subsetting is specified using earth coordinates, such as lat/lon or projection coordinates bounding boxes and date ranges, rather than index ranges that refer to the underlying data arrays. 
The data arrays are subsetted but not re-sampled or reprojected, and preserve the resolution and accuracy of the original dataset.

NCSS supports spatial and temporal subsetting on collections of grids, stations and points. A Dataset is described by a **Dataset Description** XML document, which describes the dataset in enough detail to enable a programmatic client to form valid data requests.

NCSS may return netCDF binary files (using [CF Conventions](http://cfconventions.org/latest.html){:target="_blank"}), XML, ASCII, or [WaterML2](https://www.ogc.org/standards/waterml){:target="_blank"} depending on the request and the dataset.

NCSS uses HTTP GET with key-value pairs (KVP) which may appear in any order. The service interface follows [REST design](https://en.wikipedia.org/wiki/Representational_state_transfer), as well as [Google/KML](https://developers.google.com/kml/){:target="_blank"}  and [W3C XML Schema Datatypes](https://www.w3.org/TR/xmlschema-2/){:target="_blank"}  when applicable.

#### Dataset Descriptions

Each dataset has an XML document called the **Dataset Description Document**. 
These are intended to perform the same function as [OGC GetCapabilities](https://github.com/7o9/implementer-friendly-standards/blob/master/introduction.rst#getcapabilities){:target="_blank"}  or [Atom Introspection](https://movabletype.org/documentation/developer/api/atom-legacy/atom-introspection.html){:target="_blank"} , that is, provide clients with the necessary information to formulate a valid request and send it to the server. 
**The content of these documents is still evolving**.

* **Grid Dataset**

   A Grid Dataset is a collection of Grids which have horizontal (x,y) coordinates, and optional vertical, time, and ensemble coordinates. Grid data points next to each other in index space are next to each other in coordinate space.

   Example of a Grid Dataset document:
   * [DGEX CONUS 12km Data](https://www.unidata.ucar.edu/staff/sarms/tdsDox/temp/tds/reference/files/gridsDataset.xml){:target="_blank"}


* **Station Dataset**

   Station datasets contain a collection of point features representing time-series of data points at named locations.

   Example a Station Dataset document:
   * [Metar Station Data](https://www.unidata.ucar.edu/software/tds/current/reference/files/stationsDataset.xml){:target="_blank"}

   Station datasets also support station list requests. 
   This request will return all the stations within a specified bounding box. 

   {% include note.html content="
   The station dataset feature was broken in TDS version 4.5. and was fixed in 4.5.4
   " %}

*  **Point Dataset**

   Point datasets contain a collection of point features which can be subset by space and time.

   Example Point Dataset document:
   * [Surface Buoy Point Data](https://www.unidata.ucar.edu/software/tds/current/reference/files/pointDataset.xml){:target="_blank"}
   
## Subsetting Parameters For NCSS

* [Grid datasets](https://docs.unidata.ucar.edu/netcdf-java/5.4/userguide/subsetting_parameters_ncss_grid.html){:target="_blank"}
* [Point and Station (Discrete Sampling Geometry) datasets](https://docs.unidata.ucar.edu/netcdf-java/5.4/userguide/subsetting_parameters_ncss_point){:target="_blank"}
