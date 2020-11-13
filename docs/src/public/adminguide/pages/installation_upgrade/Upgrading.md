---
title: Upgrading
last_updated: 2020-11-01
sidebar: admin_sidebar
toc: true
permalink: upgrading.html
---

## Before Upgrading

Prior to upgrading to a new version of the TDS, please do the following:

1. **Read the [release notes](#read-the-release-notes) for the new version.**

   There have been some [significant changes](release_notes.html) between TDS version 4x and 5.0.

    Please have a look at the [release notes](release_notes.html) correspomnding to the new TDS version to understand if the nature of the changes will influence the TDS operation.

2. **[Un-deploy](#un-deploying-the-old-tds) the old TDS version.**

   Before upgrading to a newer version of the THREDDS Data Server, it is important to fully un-deploy the old TDS from the servlet container.

   Tomcat and other servlet containers have _working_ directories `${tomcat.home}/work/Catalina/localhost/thredds` into which any files needed during run time, such as the generated servlet code and class files for JSPs are written.  
   Un-deploying the older TDS version will _flush_ the contents of this working directory, ensuring there is no conflict between the old and new replacement files created by the new TDS deployment.

   To undeploy the TDS, remove the TDS WAR file _and_ the matching exploded (i.e., uncompressed) directory of the same name from `${tomcat_home}/webapps`.  You can perform this task manually or by using the [Tomcat Manager](tomcat_manager_app.html) application.

3. **Perform [server maintenance](#perform-server-environment-maintenance) tasks.**
 
   This is a good time to perform maintenance of your server environment. 
   Such tasks may include, but are not limited to:
   
    * **Upgrading Outdated Packages**
    
       If needed, upgrade to the most recent versions of Java and Tomcat as per the [system requirements](system_requirements.html).

    * **Archiving Log Files**
    
        If you have not implemented [log rotation](log_maintenance.html), you should compress and archive the log files located in the following directories to another place on your file server:
       * `${tomcat.home}/logs/*`
       * `${tds.content.root.path}/thredds/logs/*`
       * `${tds.content.root.path}/tdm/*.log`

   {%include note.html content="
    You will need to stop the Tomcat servlet container while performing the maintenance aforementioned steps. 
   " %}

  
## Recommended Upgrade Process

This is the process we at Unidata follow when we upgrade the TDS on our systems.
(We use the Tomcat, but the upgrade process should be similar for other servlet containers).

1. [Download](download.html) the most recent versions of the TDS and the [THREDDS Data Manager (TDM)](tdm_ref.html) (if you use the later).
2. [Install](download.html#installation) the TDS and/or the TDM as outlined in the [Installation](download.html#installation) 
2. Stop and restart the TDM process if you upgraded either java or the TDM.

## Upgrading From TDS Version 4.x To 5

In addition to the step outlined in the [recommended upgrade process](#recommended-upgrade-process), you will need to perform the following necessary changes:

1. Modify your [JVM settings](jvm_settings.html) to be compatible with TDS 5, particularly the setting for specifying the location of the [content directory](#tds-content-directory).

2. Review and update your catalogs to reflect the changes made to:
   
   * The TDS [data services](#changes-to-tds-data-services) if you use the [Netcdf Subset Service (NCSS)](release_notes.html#netcdf-subset-service-ncss), 
[`CdmrFeature` Service](release_notes.html#cdmrfeature-service), or if you use [catalog caching](release_notes.html#catalog-caching).
   * The default behavior of [standard services](release_notes.html#standard-services) in TDS 5.
   * The THREDDS [catalog schema](release_notes.html#catalog-schema-changes).
   * [Client](release_notes.html#client-catalogs) and [server configuration](release_notes.html#server-configuration-catalogs) catalog syntax.
   * The [general recommendations](release_notes.html#recommendations-for-50-catalogs) for catalog structure.
   * [DatasetScan](release_notes.html#datasetscan) and [Feature Collections](release_notes.html#feature-collections) syntax.
   * [Earth System Grid Federation (ESGF)](release_notes.html#recommendations-for-esgf) data.


