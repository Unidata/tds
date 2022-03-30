---
title: Remove Unused Web Applications
last_updated: 2020-10-02
sidebar: admin_sidebar
toc: true
permalink: remove_unused_webapps.html
---

## Best Practice
Generally, it is good practice to remove any unused web applications out of `${tomcat_home}/webapps`.

## Tomcat Web Applications That Come With Tomcat
Tomcat "ships" with several default web applications you may want to consider removing if they are not being utilized:

* The `ROOT` application is Tomcat's `DocumentRoot` and contains the server's main web page.
  Give thought to the content that is placed in `ROOT/`, as it will be readily available. 
  Note: if you want to utilize a `robots.txt` file to restrict crawler activity, `ROOT/` is the place it will go.
* The `manager` application is used for remote management of web applications. 
  To use this application, you must add a user with role of `manager-gui` in `tomcat-users.xml`.
  Obviously, if you are not planning to use the Manager application, it should be removed.
* The `host-manager` application is used for management of virtual hosts. 
  To use this application, you must add a user with role of `admin-gui` in `tomcat-users.xml`. 
  If you are not planning to do a lot of virtual hosting in Tomcat this application should be removed.
* The `examples` application should probably be removed from a production server to minimize security exposure.
* The docs are a copy of the [Online tomcat documentation](https://tomcat.apache.org/tomcat-8.5-doc/){:target="_blank"}. 
  Unless you have need for a local copy, removing docs would help to tidy-up `${tomcat_home}/webapps`.
 
