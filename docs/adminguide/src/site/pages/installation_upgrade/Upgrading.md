---
title: Upgrading
last_updated: 2020-11-01
sidebar: admin_sidebar
toc: true
permalink: upgrading.html
---

## Before Upgrading

Prior to upgrading to a new version of the TDS, please do the following:

### Read The Release Notes For The New Version

There have been some [significant changes](release_notes.html) between TDS version 4x and 5.0.
Please have a look at the [release notes](release_notes.html) correspomnding to the new TDS version to understand if the nature of the changes will influence the TDS operation.

### Un-Deploy The Old TDS Version 

Before upgrading to a newer version of the THREDDS Data Server, it is important to fully un-deploy the old TDS from the servlet container.

1. Tomcat and other servlet containers have _working_ directories into which any files needed during run time, such as the generated servlet code and class files for JSPs are written:

   ~~~
   ${tomcat.home}/work/Catalina/localhost/thredds
   ~~~

Un-deploying the older TDS version will _flush_ the contents of this working directory, ensuring there is no conflict between the old and new replacement files created by the new TDS deployment.


2. To undeploy the TDS, remove the TDS WAR file _and_ the matching exploded (i.e., uncompressed) directory of the same name from `${tomcat_home}/webapps`.  
You can perform this task manually or by using the [Tomcat Manager](tomcat_manager_app.html) application.

### Perform Server Maintenance Tasks
 
This is a good time to perform maintenance of your server environment. 
Such tasks may include, but are not limited to:
   
1. Upgrading any outdated packages, such as thr JDK and Tomcat as per the [system requirements](system_requirements.html).
2. Archiving old [log files](log_maintenance.html) to a secure and stable disk space.
    
## Recommended Upgrade Process

This is the process we at Unidata follow when we upgrade the TDS on our systems.
(We use the Tomcat, but the upgrade process should be similar for other servlet containers).

1. [Download](download.html) the most recent versions of the TDS and the [THREDDS Data Manager (TDM)](tdm_ref.html) (if you use the later).
2. [Install](download.html#installation) the TDS and/or the TDM as outlined in the [Installation](download.html#installation) process.
2. Stop and restart the TDM process if you upgraded either the JDK or the TDM.

## Upgrading From TDS Version 4.x To 5

In addition to the step outlined in the [recommended upgrade process](#recommended-upgrade-process), you will need to perform the following necessary changes:

1. Modify your [JVM settings](jvm_settings.html) to be compatible with TDS 5, particularly the setting for specifying the location of the [content directory](tds_content_directory.html).

2. Review and update your catalogs to reflect the changes made to:

   * The TDS data services if you use the [Netcdf Subset Service (NCSS)](release_notes.html#netcdf-subset-service-ncss), or if you use [catalog caching](release_notes.html#catalog-caching).
   * The default behavior of [standard services](release_notes.html#standard-services) in TDS 5.
   * The THREDDS [catalog schema](release_notes.html#catalog-schema-changes).
   * [Client](release_notes.html#client-catalogs) and [server configuration](release_notes.html#server-configuration-catalogs) catalog syntax.
   * The [general recommendations](release_notes.html#recommendations-for-50-catalogs) for catalog structure.
   * [DatasetScan](release_notes.html#datasetscan) and [Feature Collections](release_notes.html#feature-collections) syntax.
   * [Earth System Grid Federation (ESGF)](release_notes.html#recommendations-for-esgf) data.


