---
title: TDS Online Tutorial
last_updated: 2020-04-01
sidebar: user_sidebar
permalink: index.html
toc: false
---

## What is the THREDDS Project?

The overarching goal of Unidata's Thematic Real-time Environmental Distributed Data Services (THREDDS) project is to provide students, educators and researchers with coherent access to a large collection of real-time and archived datasets from a variety of environmental data sources at a number of distributed server sites.

## What is the THREDDS Data Server (TDS)?

The THREDDS Data Server (TDS) is a web server that provides metadata and data access for scientific datasets, using OPeNDAP, OGC WMS and WCS, HTTP, and other remote data access protocols.
The TDS is developed and supported by Unidata, a division of the University Corporation for Atmospheric Research (UCAR), and is sponsored by the National Science Foundation.

Some of the technology in the TDS:

* THREDDS [Dataset Inventory Catalogs](basic_config_catalog.html) are used to provide virtual directories of available data and their associated metadata. 
  These catalogs can be generated dynamically or statically.
* The [Netcdf-Java/CDM library](https://www.unidata.ucar.edu/software/netcdf-java/){:target="_blank"} reads NetCDF, OpenDAP, and HDF5 datasets, as well as other binary formats such as GRIB and NEXRAD into a Common Data Model (CDM), essentially an (extended) netCDF view of the data.
  Datasets that can be read through the Netcdf-Java library are called CDM datasets.
* TDS can use the [NetCDF Markup Language](using_ncml_in_the_tds.html) (NcML) to modify and create virtual aggregations of CDM datasets.
* An integrated server provides [OPeNDAP](http://www.opendap.org/){:target="_blank"} access to any CDM dataset.
  OPeNDAP is a widely used, subsetting data access method extending the HTTP protocol.
* An integrated server provides bulk file access through the HTTP protocol.
* An integrated server provides data access through the [OpenGIS Consortium (OGC) Web Coverage Service (WCS)](https://www.ogc.org/standards/wcs){:target="_blank"} protocol, for any gridded dataset whose coordinate system information is complete.
* An integrated server provides data access through the [OpenGIS Consortium (OGC) Web Map Service (WMS)](http://www.opengeospatial.org/standards/wms){:target="_blank"} protocol, for any gridded dataset whose coordinate system information is complete.
  This software was developed by Jon Blower (University of Reading (UK) E-Science Center) as part of the [ESSC Web Map Service for environmental data](https://github.com/Reading-eScience-Centre/edal-java){:target="_blank"} (aka Godiva3).
* The optional [ncISO server](iso_metadata.html) provides automated metadata analysis and ISO metadata generation.
* The integrated [NetCDF Subset Service](netcdf_subset_service_ref.html) allows subsetting certain CDM datasets in coordinate space, using a REST API.
  Gridded data subsets can be returned in [CF-compliant](http://cfconventions.org/cf-conventions/v1.6.0/cf-conventions.html){:target="_blank"} netCDF-3 or netCDF-4.
  Point data subsets can be returned in CSV, XML, WaterML (with [assistance](https://github.com/Unidata/thredds/tree/5.0.0/waterml#waterml){:target="_blank"}
  from [ERDDAP](https://coastwatch.pfeg.noaa.gov/erddap/index.html){:target="_blank"} \[NOAA / Robert Simons/CoHort Software\], [license information](https://github.com/Unidata/thredds/tree/5.0.0/docs/src/private/licenses/third-party/erddap){:target="_blank"}), 
  or [CF-DSG v1.6](http://cfconventions.org/cf-conventions/v1.6.0/cf-conventions.html#discrete-sampling-geometries){:target="_blank"} netCDF files.

The THREDDS Data Server is implemented in 100% Java\*, and is contained in a single war file, which allows very easy installation into a servlet container such as the open-source Tomcat web server.
Configuration is made as simple and as automatic as possible, and we have made the server as secure as possible.

\* Writing to netCDF-4 files is supported through the netCDF C library only.

Much of the realtime data available over the Unidata Internet Data Distribution (IDD) is available through a demonstration THREDDS Data Server hosted at Unidata at [https://thredds.ucar.edu/](https://thredds.ucar.edu/thredds/catalog.html){:target="_blank"}.
You are welcome to browse and access these meteorological datasets.
If you need regular access to large amounts of data, please contact <support-idd@unidata.ucar.edu>.

As of version 5.0, the TDS is released under the BSD-3 licence, which can be found can be found [here](https://github.com/Unidata/tds/blob/master/LICENSE){:target="_blank"}.

For information on how to cite the TDS, please visit <https://www.unidata.ucar.edu/community/index.html#acknowledge>
