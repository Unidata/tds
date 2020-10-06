---
title: TDS Administrator's Guide
last_updated: 2020-10-01
sidebar: admin_sidebar 
permalink: index.html
toc: false
---

## Who Should Use This Guide?

This TDS Administrator's Guide is for:

* People who install, secure, and/or maintain the THREDDS Data Server (TDS) in a production environment;
* People who modify TDS configurations; 
* People who modify existing THREDDS catalogs to make data available via the TDS.

Users of this guide do need to have prior knowledge of the THREDDS Data Server or other Unidata technologies.


## Other TDS Documentation

Multiple guides are available for the TDS, depending on the need and audience, including:

* A [TDS Quick Start](https://docs.unidata.ucar.edu/tds/{{site.docset_version}}/quickstart/){:target="_blank"} for those who want to get the TDS up and running quickly in a *development* environment.
* The [TDS User's Guide](https://docs.unidata.ucar.edu/tds/{{site.docset_version}}/userguide/){:target="_blank"} for users who wish to access the TDS via browser and consume data via the TDS web interface.
* The [TDS Developer's Guide](https://docs.unidata.ucar.edu/tds/{{site.docset_version}}/devguide/){:target="_blank"} for developers looking to access the TDS data programmatically.

## What Is The THREDDS Data Server?

The THREDDS Data Server (TDS) is a component of the Unidata's [Thematic Real-time Environmental Distributed Data Services (THREDDS)](https://journals.tdl.org/jodi/index.php/jodi/article/view/51){:target="_blank"} project.
The goal of the THREDDS project is to provide students, educators, and researchers with coherent access to a large collection of real-time and archived datasets from a variety of environmental data sources at a number of distributed TDS server sites.

The TDS is an open-source Java web application contained in a single [WAR](https://fileinfo.com/extension/war){:target="_blank"} file which allows easy installation in a servlet container such as the [Tomcat](http://tomcat.apache.org/){:target="_blank"} application server. 

