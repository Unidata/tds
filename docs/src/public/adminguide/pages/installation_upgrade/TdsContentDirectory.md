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

## Layout

All TDS configuration, modifications, and additions should be made in this directory.
Do not place files containing passwords or anything else with security issues in this directory.
Typically, you will only be adding and modifying catalogs and configuration files.

### `thredds` Subdirectory

The TDS stashes its configurations in a `/thredds` subdirectory of the TDS content directory, e.g.: `${tds.content.root.path}/thredds/`

Other types of information used by the TDS, such as the [THREDDS Data Manager (TDM)](tdm_ref.html), will utilize other subdirectories in `${tds.content.root.path}`.

For now, we will focus on the following subset of the content directory:

 * `${tds.content.root.path}/thredds/`
   * `catalog.xml` - the main TDS configuration catalog (root catalog for TDS configuration)
   * `enhancedCatalog.xml` - an example catalog [Note: It is referenced from catalog.xml.]
   * `threddsConfig.xml` - [configuration file](tds_config_ref.html) for allowing non-default services, configuring caching, etc.
   * `logs/`
     * `catalogInit.log` - log file for messages generated while reading TDS configuration catalogs during TDS initialization and reinitialization.
     * `threddsServlet.log` - log messages about individual TDS requests, including any error messages. 
             Useful for debugging problems.
   * `cache/` - various cache directories
     * `agg/`
     * `cdm/`
     * `collection/`
     * `ehcache/`
     * `ncss/`
     * `wcs/`
   * `templates/`
     * `tdsTemplateFragments.html` - user-supplied Thymeleaf HTML templates (see [Customizing TDS](customizing_tds_look_and_feel.html#thymeleaf-templates) for details).
