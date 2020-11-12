---
title: Upgrading The TDS
last_updated: 2020-08-21
sidebar: admin_sidebar
toc: true
permalink: upgrading.html
---

## Upgrading Between TDS 5.0.x Versions

This is the process we at Unidata follow when we upgrade the TDS on our systems.
(We use the Tomcat, but the upgrade process should be similar for other servlet containers).

### Undeploy Old TDS Instance

When upgrading a THREDDS Data Server (TDS), it is important to fully undeploy the running TDS before deploying the new TDS.

Tomcat and other servlet containers have _working_ directories `${tomcat.home}/work/Catalina/localhost/thredds` into which any files needed during run time, such as the generated servlet code and class files for JSPs are written.  
Un-deploying the older TDS version will _flush_ the contents of this working directory, ensuring there is no conflict between the old and new replacement files created by the new TDS deployment.

#### Example Undeployment Of The TDS

To undeploy the TDS, remove the TDS WAR file _and_ the matching exploded directory of the same name from `${tomcat_home}/webapps`.

~~~ bash
# cd /opt/tomcat/webapps
# ls -l 

drwxr-x---  26 tomcat  tomcat       832 Nov  2 19:22 tds-5.0
-rw-r--r--   1 tomcat  tomcat  87431013 Oct 18 15:21 tds-5.0.war

# rm -rf tds-5.0*
# ls -l 

~~~

Or, if you prefer, you can use the [Tomcat Manager](tomcat_manager_app.html) application to undeploy the TDS.

### Perform System Maintenance
 
This is a good time to perform maintenance of your server environment. 

1. Shutdown Tomcat by using the calling the appropriate shutdown script in `${tomcat_home}/bin` or invoking the [server-level scripts/command](server_scripts.html) to trigger the shutdown of the Tomcat process.

   ~~~bash 
   # /opt/tomcat/bin/shutdown.sh
   ~~~
2. If needed, upgrade to the most recent versions of Java and Tomcat as per the [system requirements](system_requirements.html).
3. If you have not implemented [log rotation](log_maintenance.html), you should compress and archive the log files located in the following directories to another place on your file server:
  * `${tomcat.home}/logs/*`
  * `${tds.content.root.path}/thredds/logs/*`
  * `${tds.content.root.path}/tdm/*.log`
  
4. Restart Tomcat by using the calling the appropriate startup script in `${tomcat_home}/bin` or invoking the [server-level scripts/command](server_scripts.html) to trigger the startup of the Tomcat process.
   
   ~~~bash 
   # /opt/tomcat/bin/startup.sh
   ~~~

###   
5. Stop and restart the [THREDDS Data Manager](tdm_ref.html) process if you upgraded java.
6. Use the Tomcat manager to deploy the new TDS

## Upgrading From TDS 4.x to TDS 5.0

There have been some [significant changes](release_notes.html) between TDS version 4x and 5.0.

If you are upgrading your TDS server from, we recommend you follow the 
In addition to following the steps outlined [above](#recommended-upgrade-process), you will need to perform the following steps:

1. 