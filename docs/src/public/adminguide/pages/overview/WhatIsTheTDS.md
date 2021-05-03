---
title: What Is The THREDDS Data Server?
last_updated: 2020-11-13
sidebar: admin_sidebar
toc: true
permalink: what_is_the_tds.html
---


## Purpose
The THREDDS Data Server (TDS) is a component of the Unidata's [Thematic Real-time Environmental Distributed Data Services (THREDDS)](https://journals.tdl.org/jodi/index.php/jodi/article/view/51){:target="_blank"} project.

The goal of the THREDDS project is to provide students, educators, and researchers with coherent access to a large collection of real-time and archived datasets from a variety of environmental data sources at a number of distributed TDS server sites.

## TDS Overview

{% include image.html file="overview/overview.png" alt="TDS" caption="" %}

1. The TDS is an open-source Java web application which is run in a special type of application server  called a _servlet container_.

   The TDS web application comes bundled in a single [WAR](https://fileinfo.com/extension/war){:target="_blank"} file,  which allows for easy installation in a servlet container such as [Apache Tomcat](http://tomcat.apache.org/){:target="_blank"} application server. 

   While there are a variety of servlet containers, we at Unidata use Tomcat as it is open-source, widely used, and highly vetted.

   {%include info.html content="
    When a Java-based web application such as the TDS is first deployed in a servlet container, some of its contents is compiled into [servlets](https://docs.oracle.com/javaee/5/tutorial/doc/bnafe.html){:target='_blank'}, a special type Java class which facilities the request-response programming model between a client and server.  
    " %}

2. The TDS is built upon the [NetCDF-Java library](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/){:target="_blank"}; a framework for reading netCDF and other file formats into the [Common Data Model (CDM)](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/common_data_model_overview.html){:target="_blank"}.

   The CDM is an abstract [data model](https://en.wikipedia.org/wiki/Data_model){:target="_blank"} for scientific datasets. 
   It merges the [netCDF](https://www.unidata.ucar.edu/software/netcdf/){:target="_blank"}, [OPeNDAP](https://www.opendap.org/){:target="_blank"}, and [HDF5](https://portal.hdfgroup.org/display/support){:target="_blank"} data models to create a common API for many types of scientific data.
   Thus, facilitating multiple access methods to TDS data and the ability to create virtual datasets through aggregation.
   
  3. The TDS makes its data available via multiple _remote data access protocols_ include OPeNDAP, [OGC Web Coverage Service (WCS)](wcs_ref.html), [OGC Web Map Service (WMS)](wms_ref.html), and HTTP.
   Remote clients, such as Unidata's [Siphon](https://www.unidata.ucar.edu/software/siphon/){:target="_blank"}, can leverage these remote data protocols to access scientific data.
  
  4. Every TDS publishes THREDDS _client catalogs_ that advertise the datasets and services it makes available. 
     
     THREDDS catalogs are XML documents that list datasets, and the data access services available for the datasets. 
     Catalogs may contain metadata to document details about the datasets. 
     
  5. TDS configuration files provide the TDS with information about which datasets and data collections are available and what services are provided for the datasets.
  These configuration files, known as server-side catalogs, are also XML files and are persisted in a directory called the [TDS Content Directory](tds_content_directory.html).







