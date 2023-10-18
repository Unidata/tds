---
title: Adding OGC/ISO Services
last_updated: 2020-08-25
sidebar: user_sidebar
toc: false
permalink: adding_ogc_iso_services.html
---

## Configure TDS To Allow WCS, WMS, and ncISO Access

Out of the box, the TDS distribution will have `WCS` and `WMS` enabled.
If you do not wish to use these services, they must be explicitly allowed in the `threddsConfig.xml` file.
The ncISO services are disabled by default but can be enabled by adding the plugin and updating the `threddsConfig.xml` file.
Please see the [threddsConfig.xml file](tds_config_ref.html#wcs-service) documentation for information on how to enable/disable these services.
The default `threddsConfig.xml` file (which should now be in your `${tds.content.root.path}/content/thredds` directory) contains commented out sections for each of these services.

### `WCS` Configuration

The following section in the `threddsConfig.xml` file controls the WCS service:

~~~xml
<WCS>
  <allow>true</allow>
  ...
</WCS>
~~~

Additional `WCS` configuration options can be set in the `threddsConfig.xml` file.
More details are available in the `WCS` section of the [threddsConfig.xml file](tds_config_ref.html#wcs-service) documentation.

### `WMS` Configuration

The following section in the `threddsConfig.xml` file controls the WMS service:

~~~xml
<WMS>
  <allow>true</allow>
  ...
</WMS>
~~~

Additional `WMS` configuration options can be set in the `threddsConfig.xml` file,
More details are available in the `WMS` section of the [threddsConfig.xml file](tds_config_ref.html#wms-service) documentation.

### ncISO Configuration

#### Adding the plugin
To use the ncISO services, you must add the `tds-plugin-jar-with-dependencies.jar` artifact to your TDS for TDS versions >= 5.5.
For TDS versions prior to 5.5 this artifact was included in the TDS war file.
To see which versions of the plugin are compatible with your TDS version see the table [here](https://github.com/Unidata/threddsIso).
The plugin can be downloaded on the [TDS downloads page](https://downloads.unidata.ucar.edu/tds/){:target="_blank"}.
The downloaded ncISO plugin jar file should be placed in your `${tomcat_home}/webapps/thredds/WEB-INF/lib/` directory.

#### Updating `threddsConfig.xml`
The following section in the `threddsConfig.xml` file controls the ncISO services:

~~~xml
<NCISO>
  <ncmlAllow>false</ncmlAllow>
  <uddcAllow>false</uddcAllow>
  <isoAllow>false</isoAllow>
</NCISO>
~~~

Each `*Allow` element allows one of the three ncISO services.

After adding the ncISO plugin and updating your `threddsConfig.xml`, the TDS should be restarted.

### Adding `WCS` And `WMS` Services

As long as the `WCS` and `WMS` services are enabled, all that is required for the TDS to provide `WCS` and `WMS` access to datasets is for those datasets to reference `WCS` and `WMS` service elements.
Adding them to an existing compound service would look something like this:

~~~xml
<service name="grid" serviceType="Compound" base="" >
    <service name="odap" serviceType="OpenDAP" base="/thredds/dodsC/" />
    <service name="wcs" serviceType="WCS" base="/thredds/wcs/" />
    <service name="wms" serviceType="WMS" base="/thredds/wms/" />
    <service name="ncss" serviceType="NetcdfSubset" base="/thredds/ncss/" />
    <service name="http" serviceType="HTTPServer" base="/thredds/fileServer/" />
</service>
~~~

### Adding ncISO Services

Similar to above, as long as the ncISO services are enabled, all that is required for the TDS to provide ncISO services on datasets is for those datasets to reference the ncISO service elements.
For instance, adding to the same compound service as above:

~~~xml
<service name="grid" serviceType="Compound" base="" >
    <service name="odap" serviceType="OpenDAP" base="/thredds/dodsC/" />
    <service name="wcs" serviceType="WCS" base="/thredds/wcs/" />
    <service name="wms" serviceType="WMS" base="/thredds/wms/" />
    <service name="ncss" serviceType="NetcdfSubset" base="/thredds/ncss/" />
    <service name="http" serviceType="HTTPServer" base="/thredds/fileServer/" />
    <service name="ncml" serviceType="NCML" base="/thredds/ncml/" />
    <service name="uddc" serviceType="UDDC" base="/thredds/uddc/" />
    <service name="iso" serviceType="ISO" base="/thredds/iso/" />
</service>
~~~

### Example: Setup `WCS` And `WMS` Access For NAM Data

1. Edit the TDS configuration file and allow `WCS` and `WMS` services:

   ~~~bash
   $ cd ${tds.content.root.path}/thredds
   $ vim threddsConfig.xml
   ~~~

   and add/replace the `WCS` and `WMS` elements:

   ~~~xml
   <WCS>
     <allow>true </allow>
   </WCS>
   <WMS>
     <allow>true</allow>
   </WMS>
   ~~~

2. Edit the `catalog.xml` file and add `WCS` and `WMS` services to the NAM dataset:

   ~~~xml
   <service name="wcs" serviceType="WCS" base="/thredds/wcs/" />
   <service name="wms" serviceType="WMS" base="/thredds/wms/" />
   ~~~

3. Restart Tomcat so the TDS is reinitialized:

   ~~~bash
   $ cd ${tomcat_home}/bin
   $ ./shutdown.sh
   $ ./startup.sh
   ~~~

4. Test that `WCS` and `WMS` are working:
   1. Bring the catalog up in a browser: <http://localhost:8080/thredds/catalog.html>{:target="_blank"}
   2. Click down to one of the NAM dataset pages.
   3. Select the `WCS` Access link
   4. Go back, select the `WMS` Access link

5. Check Dataset Viewer Links for Godiva3.

### Adding `Grid` DataType To Datasets

Once datasets are accessible over the `WMS` and `WCS` services, a quick look at the dataset pages shows several `Viewer` links available for each dataset, including `Godiva3`.
This is not whether the dataset is recognized by the `CDM` as gridded but rather if the metadata in the catalog indicates that the dataset is a `Grid`.
This is accomplished with the `dataType` metadata element:

~~~xml
<dataType>Grid</dataType>
~~~

### Example: Add `Grid` DataType To The NAM Data

1. Edit the `catalog.xml` file and add a `Grid` `dataType` element (as above) to the NAM dataset.
2. Restart Tomcat so the TDS is reinitialized:

   ~~~bash
   $ cd ${tomcat_home}/bin
   $ ./shutdown.sh
   $ ./startup.sh
   ~~~

3. Check the dataset pages for the Godiva3 Viewer link.

### More `WMS` Configuration

Besides the basic `WMS` configuration available in the `threddsConfig.xml` file, there are additional configuration settings in the `wmsConfig.xml` file.
These settings can be applied globally, by dataset, by variable in a dataset, or to variables in any dataset by CF standard name.

### Default Image Styling

There are additional configuration settings for default image styling including settings for the default values of color scale range, palette name, and number of color bands as well as whether to use a linear or logarithmic scale.


### Interval Time vs Full Time List in `GetCapabilities` Documents

By default, the `WMS` will list time intervals in a `GetCapabilities` document.
Unfortunately, though time intervals are part of the `WMS` specification, not all `WMS` clients know how to interpret time intervals in the `GetCapabilities` document.
If you need a list of all times instead of the time intervals, you can add `verbose=true` as a URL query parameter to your `GetCapabilities` request.
For long time-series, this list can cause the `GetCapabilities` document to be quite large.

### Example: Modifying The `wmsConfig.xml` File

1. Open a dataset in Godiva3 and plot a parameter.
2. Notice the default color scale range is `[-50,50]`.
   Decide on a better color scale range.
3. Open the "`WMS` Detailed Configuration" page in your browser.
4. Edit the `wmsConfig.xml` file:

   ~~~bash
   $ cd ${tds.content.root.path}/thredds
   $ vi, wmsConfig.xml
   ~~~

   and change the color scale range for the target parameter in the chosen dataset.
5. Reopen Godiva3 on the dataset and plot the target parameter.
   Check the new default color scale range.

### Styling Features And Non-Standard Requests

`ncWMS` provides several styling and displaying capabilities that are also available in TDS:

* The `WMS` tries to identify vector components that it can combine and display as a single vector layer.
   It looks for CF `standard_name` attributes with values of the form `eastward_*` and `northward_*` and combines those that match into a vector layer.
* `ncWMS` provides several vector styles: `barb`, `stumpvec`, `trivec`, `linevec`, and `fancyvec`.
* Some styling properties can be specified through the non-standard optional parameters supported by `ncWMS`:
  1. `GetTransect`
  2. `GetVerticalProfile`
  3. `GetVerticalSection`

### Example: `WMS` Request With Styling Parameters

1. Open the non-standard optional parameters supported by `ncWMS` page.
2. Make several `WMS` request changing the color scale range and the displaying properties for the values out of range.
3. Use this as base request.


### Using `WCS` And WMS

#### Various `WCS` And `WMS` Clients

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

In the TDS, you can access the Godiva3 client from the `Viewers` section of all `WMS` accessible datasets.
The Godiva3 User Guide is available from the ncWMS site.

{% include image.html file="tds/tutorial/tds_configuration/Godiva2_screenshot.png" alt="Godiva2" caption="" %}
