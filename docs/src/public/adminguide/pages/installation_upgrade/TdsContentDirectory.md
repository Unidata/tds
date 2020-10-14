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

By default, a `thredds/` subdirectory is created in of the TDS content directory.
E.g.: `${tds.content.root.path}/thredds/`

It is within this `thredds/` subdirectory the aforementioned configuration files (called [_catalogs_](basic_catalog.html)) and other information needed to run the TDS are stored.  

### Contents Of `${tds.content.root.path}/thredds/`

Below is a brief explanation of the files and subdirectories of `${tds.content.root.path}/thredds/` directory that come with the TDS "out-of-the-box". 
Links to more information about the various files and subdirectories have been included for your convenience.

#### `cache/`
This directory contains cashed TDS configuration information. 
[Caching](caching.html) is discussed is more detail in the [performance tuning](caching.html) section of this guide.

#### `catalog.xml`
This is the main TDS configuration file (a.k.a the _root catalog_) used to serve data.
[more information](basic_catalog.html)

#### `enhancedCatalog.xml`
This is an _example client catalog_ file that comes with the TDS, and is referenced from [`catalog.xml`](basic_catalog.html) file.
This file and client catalogs are discussed in the [TDS configuration](basic_catalog.html) section of this guide.

#### `logs/`
_TDS-generated log files_ are located within this directory.
These logs are _different_ from the servlet container (Tomcat) log files.
[TDS logging](tds_logging.html), and the contents of the `logs/` directory is covered in the [more information](tds_logging.html)

#### `notebooks/`

#### `public/`

#### `state/`

#### `templates/`
User-supplied Thymeleaf HTML templates (see [Customizing TDS](customizing_tds_look_and_feel.html#thymeleaf-templates) for details).

#### `threddsConfig.xml` 
A [configuration file](tds_config_ref.html) for allowing non-default services, configuring caching, etc.

#### `wmsConfig.xml`


## Other Subdirectories In `${tds.content.root.path}`

Supplemental features and programs used by the TDS, such as the [THREDDS Data Manager (TDM)](tdm_ref.html), will create and store relevant  information other subdirectories in `${tds.content.root.path}`.  
E.g.: `${tds.content.root.path}/tdm/`

     
## A Note About Security

Files containing passwords or any other security-related content should **NOT** be placed in the TDS content directory, as this is not the proper location for such files.  

The TDS is designed to serve file system data using the information stored in the TDS content directory.
A misconfiguration on the part of the administrator could result in the inadvertent sharing of this sensitive information.

Please see the [Securing The TDS](restict_access_to_tds.html) section of this guide for instructions on how to properly store passwords and sensitive information.

