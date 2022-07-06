---
title: TDS Web Map Service (WMS)
last_updated: 2021-08-06
sidebar: admin_sidebar
toc: false
permalink: adding_wms.html
---

The TDS WMS implementation uses the [edal-java](https://reading-escience-centre.github.io/edal-java/){:target="_blank"} software developed by Jon Blower ([Reading E-Science Center](http://www.met.reading.ac.uk/resc/home/){:target="_blank"} at the University of Reading).
It supports [OGC Web Map Service (WMS)](https://www.ogc.org/standards/wms){:target="_blank"} versions 1.3.0 and 1.1.1.
Interacting with the WMS service included with the TDS should be the same as ncWMS (for more information, see the [Usage](https://reading-escience-centre.gitbooks.io/ncwms-user-guide/content/04-usage.html){:target="_blank"} section of the [ncWMS User Guide](https://reading-escience-centre.gitbooks.io/ncwms-user-guide/content/){:target="_blank"}.

## Which Files Can Be Served Through The WMS server?

Data files must contain gridded data.
The [NetCDF-Java Common Data Model](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/){:target="_blank"} must be able to identify the coordinate system used. Check this by opening in the `Grid Panel` of the [ToolsUI](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/toolsui_ref.html){:target="_blank"} application.
There should be one or more variables shown as a `GeoGrid`.

## Enabling And Using WMS

The WMS service **is enabled by default** in the TDS.
Additional `WMS` configuration options can be set in the `threddsConfig.xml` file.
More details are available in the `WCS` section of the [threddsConfig.xml file](http://127.0.0.1:4005/tds_config_ref.html#wms-service) documentation.
If you do not wish to use this service, it must be explicitly disabled in the `threddsConfig.xml` configuration file before it can be used.
This is done by adding an `allow` element to the `WMS` element as follows:

~~~xml
<WMS>
    <allow>false</allow>
</WMS>
~~~

As long as the WMS service is enabled, datasets can be configured to have a WMS access method in the TDS catalog configuration files similar to how other services are configured.
The service element's serviceType and base attribute values must be as follows:

~~~
<service name="wms" serviceType="WMS" base="/thredds/wms/" />
~~~

The dataset to be served must reference this service (or a containing compound service) by the service name:

~~~
<dataset ID="sample" name="Sample Data" urlPath="sample.nc">
  <serviceName>wms</serviceName>
</dataset>
~~~

WMS clients may not be able to directly use the THREDDS catalogs to find the WMS services, but the catalogs are useful for users to browse and for separate search services (e.g., [OGC catalog services](https://www.ogc.org/standards/cat){:target="_blank"}).

## WMS Configuration

Additional server level WMS configuration options can be set in the `threddsConfig.xml` file (see the [WMS Service](../adminguide/tds_config_ref.html) section of the TDS Configuration File Reference documentation). 
Further WMS configuration properties are set in the wmsConfig.xml file.
These properties are mainly related with styling of WMS images.
Similar to the `threddsConfig.xml file`, the WMS configuration file (wmsConfig.xml) is found in the `${tds.content.root.path}/thredds` directory.
A detailed description of the wmsConfig.xml file can be on the [Customizing WMS](customizing_wms.html) reference page.

If you are installing a new TDS, you should find a default `wmsConfig.xml` file (along with other configuration files) in the `${tds.content.root.path}/thredds` directory after you first deploy the TDS.
If a copy of the file is missing in the expected location, you can obtain a fresh copy from `${tomcat_home}/webapps/thredds/WEB-INF/altContent/startup/wmsConfig.xml`.

## Change to CRS List in WMS GetCapabilities Documents

The number of CRS listed in the WMS GetCapabilities documents has been reduced between TDS 4.1 and 4.2. 

{%include note.html content="
For more information, view the FAQ entry [What happened to the long list of CRSs in my WMS GetCapabilities documents?](../adminguide/troubleshooting_faqs.html#what-happened-to-the-long-list-of-crss-in-my-wms-getcapabilities-documents).
" %}

## Add a JVM Option to Avoid an X Server Bug

WMS uses a number of graphics packages.
In some situations, WMS can run into an X Server bug that can cause Tomcat to crash.
This can be avoided by telling the code there is no display device. 
You may see error messages like the following:

~~~java
java.lang.NoClassDefFoundError: Could not initialize class sun.awt.X11GraphicsEnvironment
~~~

To avoid this situation, the graphics code needs to be told that there is no graphics console available.
This can be done by setting the `java.awt.headless` system property to `true` which can be done using `JAVA_OPTS`:

~~~bash
JAVA_OPTS="-Xmx1024m -Xms256m -server -Djava.awt.headless=true"
export JAVA_OPT
~~~

What the option means:

`-Djava.awt.headless=true` sets the value of the `java.awt.headless` system property to `true`.
Setting this system property to true prevent graphics rendering code from assuming that a graphics console exists.
More information about using the headless mode in Java SE can be found on the Oracle [website](https://www.oracle.com/technical-resources/articles/javase/headless.html){:target="_blank"}.

## Add a JVM Option to Avoid `java.util.prefs` Problem Storing System Preferences

Some libraries that WMS depends on use the `java.util.prefs` package and there are some known issues that can crop up with storing system preferences.
This problem can be avoided by setting the `java.util.prefs.systemRoot` system property to point to a directory in which the TDS can write.
The given directory must exist and must contain a directory named ".systemPrefs" which must be writable by the user under which Tomcat is run.

~~~bash
JAVA_OPTS="-Xmx1024m -Xms256m -server -Djava.util.prefs.systemRoot=${tds.content.root.path}/thredds/javaUtilPrefs"
export JAVA_OPT
~~~

What the option means:

`-Djava.util.prefs.systemRoot=<directory>` sets the value of the `java.util.prefs.systemRoot` system property to the given directory path.
The `java.util.prefs` code will use the given directory to persist the system (as opposed to user) preferences.
More information on the issue can be found on the [TDS FAQ page](../adminguide/troubleshooting_faqs.html#im-seeing-the-error-inconsistent-array-length-read-538976288--1668244581-when-i-open-the-dataset-in-the-idv-why).

## Serving Remote Datasets

The TDS can also serve remote datasets with the WMS protocol if configured to do so.
It must be explicitly configured in the threddsConfig.xml configuration file. This is done by adding an allowRemote element to the WMS element as follows:

~~~xml
<WMS>
  <allow>true</allow>
  <allowRemote>true</allowRemote>
  ...
</WMS>
~~~

A slight extension of the WMS Dataset URL format allows the TDS to serve remote datasets.
The dataset is identified by adding the parameter dataset whose value is a URL:

~~~
https://servername:8080/thredds/wms?dataset=datasetURL
~~~

The URL must be a dataset readable by the NetCDF-Java library, typically an OPeNDAP dataset on another server.
It must have gridded data with identifiable coordinate systems (see above).
For example, an OPeNDAP URL might be

~~~
https://las.pfeg.noaa.gov/cgi-bin/nph-dods/data/oceanwatch/nrt/gac/AG14day.nc
~~~

This can be served remotely as a WMS dataset with this URL:

~~~
https://servername:8080/thredds/wms?dataset=https://las.pfeg.noaa.gov/cgi-bin/nph-dods/data/oceanwatch/nrt/gac/AG14day.nc
~~~

## Various `WMS` Clients

* [GoogleEarth](https://www.google.com/earth/){:target="_blank"} (WMS) [free]
* Godiva3 (WMS) [free - distributed with TDS]
* [NASA WorldWind](https://worldwind.arc.nasa.gov){:target="_blank"} (WMS) [free]
* [IDV](https://www.unidata.ucar.edu/software/idv/){:target="_blank"} (WMS) [free]
* [ToolsUI](https://downloads.unidata.ucar.edu/netcdf-java/){:target="_blank"} (WMS) [free]
* [OWSlib](http://geopython.github.io/OWSLib/){:target="_blank"} (WMS and WCS) [free]
* [Map Express](https://www.cadcorp.com/products/desktop/cadcorp-sis-desktop-express/){:target="_blank"} (`WMS` and `WCS`) [commercial / free]
* [IDL](https://www.harrisgeospatial.com/Software-Technology/IDL){:target="_blank"} (WMS) [commercial]
* [gvSIG](http://www.gvsig.org/web/){:target="_blank"} (WMS and WCS) [free]

#### Godiva3 `WMS` Client

The Godiva3 `WMS` client is part of the `ncWMS` code base and as such is included in the TDS distribution.
It is a web application written in JavaScript using the OpenLayers library.

In the TDS, you can access the Godiva2 client from the `Viewers` section of all `WMS` accessible datasets.
The Godiva3 User Guide is available from the [ncWMS documentation](https://reading-escience-centre.gitbooks.io/ncwms-user-guide/content/04-usage.html#godiva3){:target="_blank"}.

{% include image.html file="tds/tutorial/tds_configuration/Godiva2_screenshot.png" alt="Godiva2" caption="" %}

<!-- 
### OWSLib (python client) example:

[tds-python-workshop `WMS` notebook](http://nbviewer.jupyter.org/github/Unidata/unidata-python-workshop/blob/master/notebooks/wms_sample.ipynb){:target="_blank"}
-->
