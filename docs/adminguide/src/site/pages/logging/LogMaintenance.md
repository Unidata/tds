---
title: Log Maintenance
last_updated: 2020-10-03
sidebar: admin_sidebar
toc: true
permalink: log_maintenance.html
---


        If you have not implemented [log rotation](log_maintenance.html), you should compress and archive the log files located in the following directories to another place on your file server:
       * `${tomcat.home}/logs/*`
       * `${tds.content.root.path}/thredds/logs/*`
       * `${tds.content.root.path}/tdm/*.log`

   {%include note.html content="
    You will need to stop the Tomcat servlet container while performing the maintenance aforementioned steps. 
   " %}
