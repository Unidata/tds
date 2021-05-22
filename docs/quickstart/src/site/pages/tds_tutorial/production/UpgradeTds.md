---
title: Recommended Process For Upgrading A TDS
last_updated: 2020-08-21
sidebar: quickstart_sidebar
toc: false
permalink: upgrade_tds_ref.html
---

When upgrading a THREDDS Data Server (TDS), it is important to fully un-deploy the running TDS before deploying the new TDS.
Not doing so can cause conflicts between old and new Java classes and/or JSP pages (among other things).
The reason for this is that Tomcat and other webapp containers have working directories, such as `${tomcat.home}/work/Catalina/localhost/thredds`, in which generated files are stored for use (like compiled JSP pages).

So, here is the process we follow when we upgrade the TDS on our systems (we use Tomcat, but the process should be similar for other webapp containers):

1. Use the Tomcat manager app to un-deploy the TDS
  * `https://server:port/manager/html/`
2. Shutdown Tomcat
3. Clean up and archive any log files:
  * `${tomcat.home}/logs/*`
  * `${tds.content.root.path}/thredds/logs/*`
4. Startup Tomcat
5. Use the Tomcat manager to deploy the new TDS