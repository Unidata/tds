---
title: Deploying The TDS 
last_updated: 2020-08-24
sidebar: dev_sidebar
toc: false
permalink: deploying_the_tds.html
---

This section demonstrates how to obtain and deploy the TDS in the Tomcat Servlet Container.

{%include note.html content="
This section assumes you have successfully installed the JDK and Tomcat Servlet Container as outlined in the [Installation of Java and Tomcat](install_java_tomcat.html) section.
" %}

## About WAR Files

* WAR is short for Web ARchive.
* By default, Tomcat will automatically unpack the WAR distribution into a directory of the same name upon deployment.
* _The unpacked directory is overwritten each time a new WAR file is deployed._

## Downloading And Deploying `thredds.war`

#### Downloading And Renaming The TDS WAR File

1. [Download](https://downloads.unidata.ucar.edu/tds/){:target="_blank"} the TDS WAR file from Unidata's web site (`tds-5.0.0-beta5.war` for this example).

2. Rename the WAR file.
   
   Tomcat automatically *maps* the name of the WAR file to the address in which it is accessed.  
   E.g., a web application with WAR file `foo.war` will be accessible via this URL structure: `http://localhost:8080/foo` 
   
   Unless you want the URL to your TDS to look like `http://localhost:8080/tds-5.0.0-beta5` (_ugly!_) you need to rename the WAR file to match what is in the TDS `META-INF/context.xml` file.  
   By default, that is `thredds`:

   ~~~bash
   <?xml version="1.0" encoding="UTF-8"?>
   <Context path="/thredds">
   </Context>
   ~~~
   
   {%include note.html content="
   Consult the Tomcat documentation about [web application context](http://tomcat.apache.org/tomcat-8.5-doc/config/context.html){:target='_blank'} for more information about the `META-INF/context.xml` file.
   " %}
   
   The down-side of renaming the WAR file to merely `thredds.war` is that a quick glance at the WAR file will not tell you (the server administrator) which version of the TDS is deployed.
  
   
   To solve this, we can make use of a feature in the Tomcat Servlet Container that ignores anything after **double** hashtag symbols in the name of the WAR file.  
   
   If we rename the WAR file to `thredds##5.0.0-beta5.war`, Tomcat will see this matching the context information in the `META-INF/context.xml` file and make the TDS accessible via this URL structure: `http://localhost:8080/thredds` 
   (And we have the added benefit of seeing which version of the TDS is deployed when viewing the raw WAR file). 

   ~~~bash
   # cd /tmp
   # ls -l
   total 274828
   -rw-r--r-- 1 root root  80027070 Oct 24 14:42 tds-5.0.0-beta5.war
   
   # mv tds-5.0.0-beta5.war thredds##5.0.0-beta5.war
   # ls -l  
   total 274828
   -rw-r--r-- 1 root root  80027070 Oct 24 14:42 thredds##5.0.0-beta5.war
   ~~~

#### Deploying The TDS WAR File

1. To deploy the TDS in Tomcat, place the _renamed_ TDS WAR file in the `${tomcat_home}/webapps/` directory (`${tomcat_home}` is `/usr/local` in this example):

   ~~~bash
   # cd /usr/local/tomcat/webapps/
   
   # ls -l
   total 20
   drwxr-x--- 14 root root 4096 Oct 24 13:29 docs
   drwxr-x---  6 root root 4096 Oct 24 13:29 examples
   drwxr-x---  5 root root 4096 Oct 24 13:29 host-manager
   drwxr-x---  5 root root 4096 Oct 24 13:29 manager
   drwxr-x---  3 root root 4096 Oct 24 13:29 ROOT
    
   # cp /tmp/thredds##5.0.0-beta5.war .
   # ls -l
   total 78172
   drwxr-x--- 14 root root     4096 Oct 24 13:29 docs
   drwxr-x---  6 root root     4096 Oct 24 13:29 examples
   drwxr-x---  5 root root     4096 Oct 24 13:29 host-manager
   drwxr-x---  5 root root     4096 Oct 24 13:29 manager
   drwxr-x---  3 root root     4096 Oct 24 13:29 ROOT
   -rw-r--r--  1 root root 80027070 Oct 24 14:51 thredds##5.0.0-beta5.war
   ~~~

2. Confirm the TDS has been deployed.

   If Tomcat is already running, wait a couple of seconds after placing the WAR file in `${tomcat_home}/webapps` and then verify the `thredds##5.0.0-beta5.war` file was unpacked:

   ~~~bash
   # ls -l
   total 78176
   drwxr-x--- 14 root root     4096 Oct 24 13:29 docs
   drwxr-x---  6 root root     4096 Oct 24 13:29 examples
   drwxr-x---  5 root root     4096 Oct 24 13:29 host-manager
   drwxr-x---  5 root root     4096 Oct 24 13:29 manager
   drwxr-x---  3 root root     4096 Oct 24 13:29 ROOT
   drwxr-x---  8 root root     4096 Oct 24 14:51 thredds##5.0.0-beta5
   -rw-r--r--  1 root root 80027070 Oct 24 14:51 thredds##5.0.0-beta5.war
   ~~~

   Go to [http://localhost:8080/thredds/](http://localhost:8080/thredds/){:target="_blank"} in your browser to verify the TDS has been deployed:

   {% include image.html file="tds/tutorial/getting_started/default_cat.png" alt="THREDDS Distribution Catalog" caption="THREDDS Distribution Catalog" %}


    {%include warning.html content="
    Be aware that the contents of the expanded web application directory (`thredds##5.0.0-beta5` in this example) is overwritten whenever a new WAR file of the _same name_ is deployed.  
    
    In other words, the contents of a new `thredds.war` file placed into `${tomcat_home}/webapps` would completely clobber any information in an existing expanded `${tomcat_home}/webapps/thredds` directory.
    
    This is another good reason to rename the TDS WAR files by version as demonstrated prior.  
    " %}

## Creation Of TDS `$CONTENT_ROOT`

Remember the `$CONTENT_ROOT` directory we specified in the JVM options in the custom [`${tomcat_home}/bin/setenv.sh`](running_tomcat.html#setting-java_home-java_opts-catalina_home-catalina_base-and-content_root) file?

~~~bash
# TDS specific ENVARS
#
# Define where the TDS content directory will live
#   THIS IS CRITICAL and there is NO DEFAULT - the
#   TDS will not start without this.
#
CONTENT_ROOT=-Dtds.content.root.path=/data/content
~~~

The TDS `$CONTENT_ROOT` is a directory created by the TDS the first time it is deployed in the location specified by the aforementioned `${tomcat_home}/bin/setenv.sh` file JVM settings.

The TDS uses this directory to store TDS-related configuration files __that will persist between TDS WAR and Tomcat upgrades__.
   
{%include important.html content="
We recommend locating the TDS `$CONTENT_ROOT` directory somewhere separate from `${tomcat_home}` on your file system that will persist.
" %}

#### Confirm The Creation Of `$CONTENT_ROOT`
   
1. Move to the location you've specified for `$CONTENT_ROOT` and do a long listing (`/data` in this example):

   ~~~bash
   # cd /data
   # ls -l
    total 148
    drwxr-x--- 3 root root  4096 Oct 24 14:43 content
   ~~~

   You should see a directory created by the TDS as specified in the JVM settings.

   {%include troubleshooting.html content="
   Any error in the TDS deployment will be reported in the `catalina.out` file of the `${tomcat_home}/logs` directory.
   " %}

   {%include troubleshooting.html content="
   Be sure you have downloaded and deployed the correct version of the TDS.
   The TDS version number will appear in the blue bar at the bottom of TDS catalog pages.
   " %}
   

## Next Step

Next, we'll examine the [Tomcat Manager Application](tomcat_manager_app.html) and grant ourselves access to it in preparation for accessing restricted parts of the TDS.
