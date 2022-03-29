---
title: Digital Library Record Generation and Harvesting Reference
last_updated: 2020-08-31
sidebar: admin_sidebar
toc: false
permalink: digital_libraries.html
---


## Digital Library Record Generation With The `DLwriter` Service

### `DLwriter` Configuration

The `DLwriter` service must be enabled in the `threddsConfig.xml` configuration file before it can be used. 
This is done by adding the following lines:

~~~xml
<CatalogServices>
  <allow>true</allow>
</CatalogServices>
~~~

Further configuration must be done before the `DLwriter` can be used on remote catalogs. 
This requires the addition of an `allowRemote` element:

~~~xml
<CatalogServices>
  <allow>true</allow>
  <allowRemote>true</allowRemote>
</CatalogServices>
~~~

### `DLwriter` Usage

The `DLwriter` service generates digital library records for each dataset in a given catalog. 
The following parameters can be used:

|-------------|-----------------------------------|
| Parameter   | Description                       |
|:------------|:----------------------------------|
| `catalog`   | The URL of the target catalog<br> (relative to the request URL or absolute if enabled with `allowRemote` as above) |
| `type`      | The value must be either `DIF` or `ADN` |

As follows:

`catalog=catalog.xml`

Default catalog is `/thredds/idd/models.xml`

The digital library records are written to: `${tomcat_home}/content/thredds/DLwriter`.
