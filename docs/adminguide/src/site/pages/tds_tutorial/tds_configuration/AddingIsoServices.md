---
title: ISO Services
last_updated: 2020-08-25
sidebar: admin_sidebar
toc: false
permalink: adding_iso_services.html
---

## Configure TDS To Allow `ncISO` Access

Out of the box, the TDS distribution will have `ncISO` enabled.
If you do not wish to use these services, they must be explicitly disabled in the `threddsConfig.xml` file.
Please see the  [threddsConfig.xml file](tds_config_ref.html) documentation for information on how to disable these services.
The default `threddsConfig.xml` file (which should now be in your `${tds.content.root.path}/content/thredds` directory) contains commented out sections for each of these services.

### `ncISO` Configuration

The following section in the `threddsConfig.xml` file controls the `ncIso` services:

~~~xml
<NCISO>
  <ncmlAllow>true</ncmlAllow>
  <uddcAllow>true</uddcAllow>
  <isoAllow>true</isoAllow>
</NCISO>
~~~

Each `*Allow` element allows one of the three `ncISO` services.

### Adding `ncISO` Services

As long as the `ncISO` services are enabled, all that is required for the TDS to provide `ncISO` services on datasets is for those datasets to reference the `ncISO` service elements.
For example:

~~~xml
<service name="ncIsoServices" serviceType="Compound" base="" >
    <service name="ncml" serviceType="NCML" base="/thredds/ncml/" />
    <service name="uddc" serviceType="UDDC" base="/thredds/uddc/" />
    <service name="iso" serviceType="ISO" base="/thredds/iso/" />
</service>
~~~
