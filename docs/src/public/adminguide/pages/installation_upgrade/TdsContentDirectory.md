---
title: TDS Content Directory
last_updated: 2020-10-03
sidebar: admin_sidebar
toc: true
permalink: tds_content_directory.html
---

## Purpose

All THREDDS Data Server configuration information is stored under the TDS content directory.
The content directory is created and populated with default files the first time the TDS is deployed or any time the directory is empty.

## Location

The location of this directory is chosen by you, the administrator. 
Due to the importance of this directory, it is a good idea to locate it somewhere separate from `${tomcat_home}` on your file system.
It needs to be persisted between Tomcat upgrades or TDS re-deployments.

The location of the directory is controlled by [setting the `tds.content.root.path` Java system property](jvm_settings.html#tds-content-directory).

{% include warning.html content="
There is no default location for this directory in the TDS; **`tds.content.root.path` must be set or the TDS will not start**. 
"%}

All TDS configuration, modifications, and additions should be made in this directory.
Typically, you will only be adding and modifying catalogs and configuration files.

## `thredds/` Subdirectory

By default, a `thredds/` subdirectory is created in the TDS content directory.
E.g.: `${tds.content.root.path}/thredds/`

The aforementioned configuration files (called [_catalogs_](basic_catalog.html)) and other information needed to run the TDS are stored in the `thredds/` subdirectory . 

### Contents

The `thredds/` directory includes a number of files and subdirectories:

~~~bash
$ ll
total 48
drwxr-x---   8 tomcat  tomcat   256 Oct 14 12:30 cache
-rw-r-----   1 tomcat  tomcat  2259 Oct 14 12:30 catalog.xml
-rw-r-----   1 tomcat  tomcat  2609 Oct 14 12:30 enhancedCatalog.xml
drwxr-x---   9 tomcat  tomcat   288 Oct 14 12:30 logs
drwxr-x---   3 tomcat  tomcat    96 Oct 14 12:30 notebooks
drwxr-x---   3 tomcat  tomcat    96 Oct 14 12:30 public
drwxr-x---   3 tomcat  tomcat    96 Oct 14 12:31 state
drwxr-x---   3 tomcat  tomcat    96 Oct 14 12:30 templates
-rw-r-----   1 tomcat  tomcat  8655 Oct 14 12:30 threddsConfig.xml
-rw-r-----   1 tomcat  tomcat  2797 Oct 14 12:30 wmsConfig.xml
~~~

Below is a brief explanation of these files and subdirectories, with links to further information.

|---------|--------------|
| Name | Description |
|:--------|:-------------|
| `cache/` | Contains directories for temporary files and on-disk caches. <br/>[[more information]](caching.html)  |
| `catalog.xml` | This is the main TDS _client_ configuration file (a.k.a, the _root catalog_) used to serve data. <br/>[[more information]](basic_catalog.html) |
| `enhancedCatalog.xml` | Example configuration catalog, demonstrating advanced features (referenced from `catalog.xml`).<br/>[[more information]](basic_catalog.html)  |
| `logs/` | _TDS-generated log files_ are located within this directory, and are _different_ from the servlet container (Tomcat) log files.<br/>[[more information]]() |
| `notebooks/` | This directory contains public endpoint `.ipynb` files for the TDS Jupyter Notebook service.<br/>[[more information]]() | 
| `public/` | Certain files in this directory are automatically mapped and served from the TDS context root.<br/>[[more information]]()| 
| `state/` | Contains state information about the TDS configuration catalogs.<br/>[[more information]]() | 
| `templates/` | User-supplied Thymeleaf HTML templates to customize the look and feel of your TDS server.<br/>[[more information]](customizing_tds_look_and_feel.html#thymeleaf-templates) |
|`threddsConfig.xml` | Main TDS configuration file for allowing non-default services, configuring caching, etc.<br/>[[more information]](tds_config_ref.html) |
| `wmsConfig.xml` | A configuration file for the THREDDS Web Mapping Service (WMS).<br>[[more information]](wms_ref.html) |


## Other Subdirectories In `${tds.content.root.path}`

Supplemental features and programs used by the TDS, such as the [THREDDS Data Manager (TDM)](tdm_ref.html), will create and store relevant  information other subdirectories in `${tds.content.root.path}`.  
E.g.: `${tds.content.root.path}/tdm/`

     
## A Note About Security

Files containing passwords or any other security-related content should **NOT** be placed in the TDS content directory, as this is not the proper location for such files.  

The TDS is designed to serve file system data using the information stored in the TDS content directory.
A misconfiguration on the part of the administrator could result in the inadvertent sharing of this sensitive information.

Please see the [Securing The TDS](restict_access_to_tds.html) section of this guide for instructions on how to properly store passwords and sensitive information.

