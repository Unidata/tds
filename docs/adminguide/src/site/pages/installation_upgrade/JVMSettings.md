---
title: Java Virtual Machine (JVM) Settings
last_updated: 2020-10-02
sidebar: admin_sidebar
toc: true
permalink: jvm_settings.html
---

## Setting `$JAVA_OPTS`

You will need to define and pass the following options and parameters to the Java Virtual Machine (JVM) in order to:

1. tell the TDS where to store configuration files; and
2. increase the amount of memory allocated to the JVM to enhance performance; and
3. add additional settings to the JVM to enable more advanced services in the TDS (e.g, [WMS](adding_wms.html), etc).

### Utilize Tomcat `setenv.sh`

The main control script for the Tomcat Servlet Container (`${tomcat_home}/bin/catalina.sh`) is executed on server startup and shutdown.
When executed, the `catalina.sh` script will look for a `setenv.sh` file (or `setenv.bat` on Windows systems) in the `${tomcat_home}/bin` directory.
If it finds `setenv.sh`, it will apply the custom environment and JVM configurations specified within the file.  

You can use the `setenv.sh` file to configure the JVM by setting by defining and populating the `$JAVA_OPTS` variable.

#### Example `setenv.sh` File

~~~bash
#!/bin/sh
#
# ENVARS for Tomcat
#
export CATALINA_HOME="/usr/local/tomcat"

export CATALINA_BASE="/usr/local/tomcat"

export JAVA_HOME="/usr/local/jdk"

# TDS specific ENVARS
#
# Define where the TDS content directory will live
# THIS IS CRITICAL and there is NO DEFAULT - 
# the TDS will not start without this.
#
CONTENT_ROOT=-Dtds.content.root.path=/data/content

# Set java prefs related variables (used by the wms service)
JAVA_PREFS_ROOTS="-Djava.util.prefs.systemRoot=$CONTENT_ROOT/thredds/javaUtilPrefs \
                  -Djava.util.prefs.userRoot=$CONTENT_ROOT/thredds/javaUtilPrefs"

#
# Some commonly used JAVA_OPTS settings:
#
NORMAL="-d64 -Xmx4096m -Xms512m -server"
HEAP_DUMP="-XX:+HeapDumpOnOutOfMemoryError"
HEADLESS="-Djava.awt.headless=true"

#
# Standard setup.
#
JAVA_OPTS="$CONTENT_ROOT $NORMAL $HEAP_DUMP $HEADLESS $JAVA_PREFS_ROOTS"

export JAVA_OPTS
~~~

### TDS `$JAVA_OPTS` Options 

You will need to set the following JVM options for the TDS.

|--------------------|-----------|
| JVM Option         |  Purpose  |
|--------------------|-----------|
| `tds.content.root.path`| _Required by TDS_<br/> A TDS-specific variable which defines the location of where TDS-related configuration files are stored. <br/>[[more information](#tds-content-directory)] |
| `Xms` | _Recommended for performance_<br/> The initial and minimum allocated memory of the JVM. <br/>[[more information](#jvm-performance-options)] |
| `Xmx` | _Recommended for performance_<br/> The maximum allocated memory of the JVM. <br/>[[more information](#jvm-performance-options)] |
| `server` | _Recommended for performance_<br/> Tells the Hotspot compiler to run the JVM in "server" mode. <br/>[[more information](#jvm-performance-options)] |
| `java.awt.headless` | _Required for WMS usage_<br/> Needed to prevent graphics rendering code from assuming a graphics console exists. <br/>[[more information](#jvm-options-needed-for-wms-usage)] |
| `java.util.prefs.systemRoot` | _Required for WMS usage_<br/> Allows the `java.util.prefs` of the TDS WMS to write system preferences to a location that is writable by the Tomcat user. <br/>[[more information](#jvm-options-needed-for-wms-usage)]
| `java.util.prefs.userRoot` | _Required for WMS usage_<br/> Allows the `java.util.prefs` of the TDS WMS to write system preferences to a location that is writable by the Tomcat user. <br/>[[more information](#jvm-options-needed-for-wms-usage)]

{%include note.html content="
For more information about the possible options/arguments available for `$JAVA_OPTS`, please consult the [Oracle Documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/java.html#BABDJJFI){:target='_blank'}.
" %}

### TDS Content Directory

All THREDDS Data Server configuration information is stored in the [TDS content directory](tds_content_directory.html). 
This directory is created the first time the TDS is deployed to your server.

The location of this directory is chosen by you, the administrator.
It is a good idea to locate this directory somewhere separate from `${tomcat_home}` on your file system.
It needs to be persisted between Tomcat upgrades or TDS re-deployments.

The location of TDS content directory is controlled by setting the `tds.content.root.path` Java system property using this syntax:
`-Dtds.content.root.path=path_to_TDS_content_root`

#### Example Setting `tds.content.root.path`

~~~bash
-Dtds.content.root.path=/data/content/
~~~

There is no default location for this directory in the TDS; **`tds.content.root.path` must be set, or the TDS will not start**. 

{% include info.html content="
The [TDS content directory](tds_content_directory.html) is explained in greater detail in [subsequent sections](tds_content_directory.html) of this guide.
"%} 

### JVM Performance Options

Unidata recommends setting the following JVM options to promote better performance for the TDS.

* **JVM initial allocated memory**

    This is a number value (in bytes) for the initial size of the JVM heap. 
    This value must be a multiple of 1024 and greater than 1 MB. 
    Append the letter `k` or `K` to indicate kilobytes, `m` or `M` to indicate megabytes, `g` or `G` to indicate gigabytes.

    Enable the _initial allocated memory_ with the `Xms` Java system property using this syntax: `-Xms<size>`


* **JVM maximum allocated memory**

    This is a number value (in bytes) for maximum size of the JVM memory allocation pool. 
    Like the initial allocated memory (`Xms`), this value must be a multiple of 1024 and greater than 2 MB. 
    Append the letter `k` or `K` to indicate kilobytes, `m` or `M` to indicate megabytes, `g` or `G` to indicate gigabytes.
    
    Enable the _maximum allocated memory_ with the `Xmx` Java system property using this syntax: `-Xmx<size>`
    
    {% include important.html content="
    Whenever possible, Unidata recommends _at least_ `-Xmx4096m` (or more) for 64-bit operating systems.
    "%}
    
* **Java HotSpot server VM**

    To improve performance, the JVM can be configured to run is _server_ mode (for "server-class" machines) or in _client_ mode (for Java clients). 
    
    Configure the Hotspot compiler to run the JVM in _server_ mode with the `server` Java system property using this syntax: `-server`
 
#### Example Setting JVM Performance Options

~~~bash
-Xmx4096m -Xms512m -server
~~~

### JVM Options Needed For WMS Usage

The [Web Map Service (WMS)](adding_wms.html) is one of the [data access services](services_ref.html#data-access-services) available in the TDS. 
If you intend to use this service, you'll need to set the following options in the JVM.

* **JVM headless mode**  

   The JVM needs to be run in [`headless mode`](https://blog.idrsolutions.com/2013/08/what-is-headless-mode-in-java/){:target="_blank"} to prevent graphics rendering code used in the WMS from assuming a graphics console exists in the server environment.  
   (Application servers typically do not need peripherals, such as display devices).
   Without this, the WMS code will crash the server in some circumstances.
   
   Enabled _headless mode_ with the `java.awt.headless` Java system property using this syntax: `-Djava.awt.headless=true`

* **Java preferences**

   The [Java Preferences API](https://www.vogella.com/tutorials/JavaPreferences/article.html){:target="_blank"} facilities the systematic handling of Java program preference configurations, such as user settings.
   The TDS WMS code leverages this API to write system preferences to a location that is writable by the Tomcat user.
   
   This _location_ is one of the uses for the aforementioned [TDS content directory](#tds-content-directory). 
   
   Specify the `java.util.prefs.systemRoot` and `java.util.prefs.userRoot` Java system properties using this syntax: `-Djava.util.prefs.systemRoot=path_to_TDS_content_root/thredds/javaUtilPrefs` and `-Djava.util.prefs.userRoot=path_to_TDS_content_root/thredds/javaUtilPrefs`
   
   {% include note.html content="
   The TDS stashes its configurations in a `/thredds` subdirectory of the [TDS content directory](tds_content_directory.html).
   "%} 
   
#### Example Setting WMS JVM Options
   
~~~bash
-Djava.awt.headless=true \
-Djava.util.prefs.systemRoot=/data/content/thredds/javaUtilPrefs \
-Djava.util.prefs.userRoot=/data/content/thredds/javaUtilPrefs
~~~
   
{% include info.html content="
The [WMS Reference](adding_wms.html) contains more information about the TDS Web Map Service.
"%} 



