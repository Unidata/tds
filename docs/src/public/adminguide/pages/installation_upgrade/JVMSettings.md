---
title: JVM Settings
last_updated: 2020-09-20
sidebar: admin_sidebar
toc: false
permalink: jvm_settings.html
---

## Setting `$JAVA_OPTS`

You will need to define and pass the following the options and parameters to the JVM in order to:

1. Increase the amount of memory allocated to the JVM to enhance performance by setting `$JAVA_OPTS`; and
2. Add additional settings to the JVM to enable more advanced services in the TDS (e.g, [WMS](wms_ref.html), etc).

Create a `setenv.sh` file (or `setenv.bat` on Windows systems) in the `${tomcat_home}/bin` directory, and specify the following options and parameters

### TDS Content Directory

All THREDDS Data Server configuration information is stored in the [TDS content directory](tds_content_directory.html). This directory is created the first time the TDS is deployed.

The location of this directory is chosen by you, the administrator.
It is a good idea to locate this directory somewhere separate from `${tomcat_home}` on your file system, as it needs to be persisted between Tomcat upgrades.

The location of TDS content directory is controlled by the `tds.content.root.path` Java system property.
There is no default location for this directory in the TDS; **`tds.content.root.path` must be set or the TDS will not start**. 

#### Example

~~~
-Dtds.content.root.path=/data/content
~~~


### `java.util.prefs` Variables

This option is needed to prevent graphics rendering code from assuming a graphics console exists. <br> Without this, WMS code will crash the server in some circumstances.

### `$JAVA_OPTS` Options 



|--------------------|-----------|
| JVM Option         |  Purpose  |
|--------------------|-----------|
| `Dtds.content.root.path=path_to_TDS_content_root` |  **required** <br>This is a TDS-specific variable. It defines the location of where TDS-related configuration files will be stored. |

| `Xms`              |  The initial and minimum allocated memory of the JVM (for performance). |
| `Xmx`               |  The maximum allocated memory of the JVM (for performance). |
| `server`      |  Tells the Hotspot compiler to run the JVM in "server" mode (for performance). |
| `Djava.awt.headless=true`  required if you intend to enable WMS <br> This option is needed to prevent graphics rendering code from assuming a graphics console exists. <br> Without this, WMS code will crash the server in some circumstances. |
| `Djava.util.prefs.systemRoot` | allows the java.util.prefs of the TDS WMS to write system preferences to a location that is writable by the Tomcat user. |
| `Djava.util.prefs.userRoot` | allows the java.util.prefs of the TDS WMS to write system preferences to a location that is writable by the Tomcat user. |

* allow Tomcat to reference/find the location of `$JAVA_HOME` and `$CATALINA_BASE`) during startup and shutdown;

* increase the amount of memory allocated to the JVM to enhance performance by setting `$JAVA_OPTS`; and

* add additional settings to the JVM via `$JAVA_OPTS` to enable more advanced services in the TDS (e.g, WMS, etc).

Tomcat's `${tomcat_home}/bin/startup.sh` script executes the `catalina.sh` script found in the same directory.  
`catalina.sh` is the main control script for the Tomcat Servlet Container which is executed on server startup and shutdown (also called from the `${tomcat_home}/bin/shutdown.sh` script).
 
When executed, the `catalina.sh` script will look for a `setenv.sh` in the `${tomcat_home}/bin` directory.  
If it finds `setenv.sh`, it will apply the custom environment and JVM configurations specified within the file.  
(Thus, saving you the trouble of directly modifying and potentially introducing errors in the important `catalina.sh` script).


### Example

~~~
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

# Set java prefs related variables (used by the wms service, for example)
JAVA_PREFS_ROOTS="-Djava.util.prefs.systemRoot=$CONTENT_ROOT/thredds/javaUtilPrefs \
                  -Djava.util.prefs.userRoot=$CONTENT_ROOT/thredds/javaUtilPrefs"

#
# Some commonly used JAVA_OPTS settings:
#
NORMAL="-d64 -Xmx4096m -Xms512m -server -ea"
HEAP_DUMP="-XX:+HeapDumpOnOutOfMemoryError"
HEADLESS="-Djava.awt.headless=true"

#
# Standard setup.
#
JAVA_OPTS="$CONTENT_ROOT $NORMAL $HEAP_DUMP $HEADLESS $JAVA_PREFS_ROOTS"

export JAVA_OPTS
~~~

   {% include important.html content="
   Whenever possible, Unidata recommends `-Xmx4096m` (or more) for 64-bit systems.
   " %}



    {%include note.html content="
    For more information about the possible options/arguments available for `$JAVA_OPTS`, please consult the [Oracle Documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/java.html#BABDJJFI){:target='_blank'}.
    " %}
