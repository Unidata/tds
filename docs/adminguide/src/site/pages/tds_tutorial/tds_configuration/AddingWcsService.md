---
title: Adding OGC/ISO Services
last_updated: 2020-08-25
sidebar: admin_sidebar
toc: false
permalink: adding_wcs_service.html
---

## Configure TDS To Allow WCS Access

Out of the box, the TDS distribution will have `WCS` enabled.
If you do not wish to use this service, it must be explicitly disabled in the `threddsConfig.xml` file.
Please see the  [threddsConfig.xml file](tds_config_ref.html) documentation for information on how to disable these services.
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

### Adding `WCS` And `WMS` Services

As long as the `WCS` service is enabled, all that is required for the TDS to provide `WCS` access to datasets is for those datasets to reference the `WCS` service element.
Adding them to an existing compound service would look something like this:

~~~xml
<service name="wcs" serviceType="WCS" base="/thredds/wcs/" />
~~~

### Example: Setup `WCS` Access For NAM Data

1. Edit the TDS configuration file and allow `WCS` services:

   ~~~bash
   $ cd ${tds.content.root.path}/thredds
   $ vim threddsConfig.xml
   ~~~

   and add/replace the `WCS` element:

   ~~~xml
   <WCS>
     <allow>true </allow>
   </WCS>
   ~~~
   
   (this is optional, as the service is on by default)

2. Edit the `catalog.xml` file and add `WCS` service to the NAM dataset:

   ~~~xml
   <service name="wcs" serviceType="WCS" base="/thredds/wcs/" />
   ~~~

3. Restart Tomcat so the TDS is reinitialized:

   ~~~bash
   $ cd ${tomcat_home}/bin
   $ ./shutdown.sh
   $ ./startup.sh
   ~~~

4. Test that `WCS` is working:
   1. Bring the catalog up in a browser: <http://localhost:8080/thredds/catalog.html>{:target="_blank"}
   2. Click down to one of the NAM dataset pages.
   3. Select the `WCS` Access link
