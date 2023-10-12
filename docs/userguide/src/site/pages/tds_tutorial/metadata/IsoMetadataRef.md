---
title: TDS And ncISO - Metadata Services
last_updated: 2020-08-26
sidebar: user_sidebar
toc: false
permalink: iso_metadata.html
---

The TDS can include ISO metadata services by using the [ncISO plugin](https://github.com/Unidata/threddsIso/){:target="_blank"} package from NOAA/Environmental Data Management (many thanks to Dave Neufeld and Ted Habermann).
See [enabling ncISO services](adding_ogc_iso_services.html) for more information on how to add these services to your TDS.

## ncISO Services
The ncISO plugin provides three new services for datasets:
* `NCML`: an NcML representation of the dataset's structure and metadata;
* `ISO`: an ISO 19115 metadata representation of the dataset; and
* `UDDC`: an evaluation of how well the metadata contained in the dataset conforms to the NetCDF Attribute Convention for Data Discovery (NACDD) (see the [NOAA/EDM page on NACDD](http://wiki.esipfed.org/index.php/Category:Attribute_Conventions_Dataset_Discovery){:target="_blank"}).

## Enabling ncISO Services

The ncISO services are disabled by default.
Provided that you have added the [ncISO plugin](adding_ogc_iso_services.html#nciso-configuration), these services can be enabled for locally served datasets by including the following in the `threddsConfig.xml` file:

~~~xml
<NCISO>
  <ncmlAllow>true</ncmlAllow>
  <uddcAllow>true</uddcAllow>
  <isoAllow>true</isoAllow>
</NCISO>
~~~

## Providing ncISO Services For Datasets

Once ncISO is enabled, datasets can be configured to have the three ncISO services in the TDS catalog configuration files similar to the way other services are configured.
The `service` element's `serviceType` and `base` attribute values must be as follows: 

~~~xml
<service name="ncml" serviceType="NCML" base="/thredds/ncml/"/>
<service name="uddc" serviceType="UDDC" base="/thredds/uddc/"/>
<service name="iso" serviceType="ISO" base="/thredds/iso/"/>
~~~

The dataset to be served must reference a containing compound service by the service `name`.
For instance, if a compound service named `all` contained all three services listed above: 

~~~xml
<dataset ID="sample" name="Sample Data" urlPath="sample.nc">
  <serviceName>all</serviceName>
</dataset>
~~~
