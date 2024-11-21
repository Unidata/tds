---
title: TDS Services
last_updated: 2020-08-21
sidebar: quickstart_sidebar
toc: false
permalink: services_ref.html
---

## Data Services

### Standard Data Services

The TDS has a set of **Standard Data Services** that are always available (unless explicitly disabled) and can be referenced from any configuration catalog:

* cdmRemote
* dap4
* httpService
* resolver
* netcdfSubsetGrid
* netcdfSubsetPoint
* opendap
* wcs
* wms
* iso
* ncml
* uddc
 
### Available Services

The TDS configures the appropriate set of standard data services for each dataType/featureType.
You can configure the available data services globally, and they will be applied to all datasets of that dataType/featureType.
If you do not specify a service for a dataset, but you do specify its dataType/featureType, then the datatype-appropriate services will be enabled for it.

### User-Defined Services

You can still define your own services, either globally in the root catalog, or locally in any configuration catalog.
* Services placed in the root catalog are global and can be referenced in any other config catalog.
* Services placed in any other catalog are local, are used only in that catalog, and override (by name) any global services.


## Server Information Services

*Server Information Services* provide human- and machine-readable access to information about the server installation. 
E.g., an abstract and a list of keywords summarizing the services and data available on the server, contact information and other information about the group hosting the server, and the version of the THREDDS Data Server (TDS) running.

#### Basic Configuration:

|-----------------|-------------------|-------------------------------|-------------------------------|
| Service Name         | Default Availability | Access Point   |
|:----------------|:------------------|:------------------------------|:-------------------------------|
| Server Information (HTML) | Enabled | `/thredds/serverInfo.html` |
| Server Information (XML) | Enabled | `/thredds/serverInfo.xml` |
| Server Version Information (Text) | Enabled | `/thredds/serverVersion.txt` |

## Catalog Services

*Catalog Services* provide subsetting and HTML conversion services for THREDDS catalogs. 
Catalogs served by the TDS can be subset and/or viewed as HTML. 
Remote catalogs, if allowed/enabled, can be validated, displayed as HTML, or subset.

{% include note.html content="
Consult the [Configuration Catalogs](config_catalog.html) documentation and [TDS Configuration File Reference](tds_config_ref.html) for more information.
" %}

#### Basic Configuration:

|-----------------|-------------------|-------------------------------|-------------------------------|
| Service Name         | Default Availability | Access Point   |
|:----------------|:------------------|:------------------------------|:-------------------------------|
| THREDDS Catalog Services | Enabled | `/thredds/catalog.{xml|html}`<br/> `/thredds/catalog/*/catalog.{xml|html}`<br/>`/thredds/catalog/*/*.{xml|html}` |
| Remote THREDDS Catalog Service | *Disabled* | `/thredds/remoteCatalogService` |


## `ncISO` Metadata Services

*`ncISO` Metadata Services* facilitate the generation of [ISO 19115 metadata representation](https://en.wikipedia.org/wiki/Geospatial_metadata){:target="_blank"} from data in [NetCDF](https://www.unidata.ucar.edu/software/netcdf/). 

The three `ncISO` Metadata Services are:

* **ISO**

  The ISO Metadata Service provides [ISO 19115 metadata representation](https://www.ngdc.noaa.gov/wiki/index.php/NcISO){:target="_blank"} of a dataset's structure and metadata.
  
   {% include note.html content="
  Learn how to [enable NcISO](adding_ogc_iso_services.html) in the TDS Configuration file.
  " %}
  
* **NCML**

    The *NCML Metadata Service* provides NCML representation of a dataset.
    
    {% include note.html content="
    More details are available about how to [Use NcML in the TDS](using_ncml_in_the_tds.html). 
  " %}


* **UDDC**
    
    Provide an evaluation of how well the metadata contained in a dataset conforms to the [NetCDF Attribute Convention for Data Discovery (NACDD)](https://wiki.esipfed.org/Category:Attribute_Conventions_Dataset_Discovery){:target="_blank"}.

#### Basic Configuration:

|-----------------|-------------------|-------------------------------|
| Service Name         | Default Availability | Access Point   |
|:----------------|:------------------|:------------------------------|
| ISO | Enabled | `/thredds/iso/*`  |
| NCML | Enabled | `/thredds/ncml/*`  |
| UDDC | Enabled | `/thredds/uddc/*`  |

#### Catalog Service Configuration:
(These are the exact [required](services_ref.html#tds-requirements-for-thredds-catalog-service-elements) values to enable these service.)

|-----------------|-------------------|------------------------|
|  Service Name |   Service Type  | Service Base URL |
|:----------------|:------------------|:-----------------------|
| ISO | `ISO` | `/thredds/iso/` |
| NCML | `NCML` | `/thredds/ncml/` |
| UDDC | `UDDC` | `/thredds/uddc/` |



## Data Access Services

The TDS provides a number of different *Data Access Services*, including:

* **OPeNDAP**
   
   The [OPeNDAP DAP2](https://en.wikipedia.org/wiki/OPeNDAP){:target="_blank"} data access protocol.
   
   {% include note.html content="
   View all the available [configuration options](basic_config_catalog.html) for OPeNDAP in the TDS.
   " %}
           
* **NetCDF Subset Service**

   The [NetCDF Subset Service (NCSS)](netcdf_subset_service_ref.html) is a web service for subsetting CDM scientific datasets. 
   
   {% include note.html content="
   Set NCSS configuration options in the [TDS Configuration File](tds_config_ref.html#netcdf-subset-service-ncss) (threddsConfig.xml).
   " %}
   
* **CDM Remote**

   The [`cdmremote/ncstream`](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/cdmremote.html){:target="_blank"} data access service is automatically enabled when an appropriate FeatureCollection is used.

        
* **OGC Web Coverage Service (WCS)**

   [OGC Web Coverage Service (WCS)](https://www.ogc.org/standards/wcs){:target="_blank"} supports access to geospatial data as "coverages".
                                                                                                                                            
   {% include note.html content="
   More setup, configuration, and implementation details for the TDS OGC WCS implementation are available in the [TDS WCS Reference](wcs_ref.html).
   " %}


* **OGC Web Map Service (WMS)**

   The [OGC Web Map Service (WMS)](https://www.opengeospatial.org/standards/wms){:target="_blank"}  supports access to geo-registered map images from geoscience datasets.

   {% include note.html content="
   More setup, configuration, and implementation details for the TDS OGC WMS implementation are available in the [TDS WMS Reference](adding_wms.html).
   " %}
   
        
* **HTTP File Download**

   The HTTP File Download service supports HTTP byte ranges. 
   
   {% include note.html content="
   Set HTTP File Download  configuration options in the [TDS Configuration File](tds_config_ref.html) (threddsConfig.xml).
   " %}

* **OGC Sensor Observation Service (SOS)**

   The [Sensor Observation Service (SOS)](https://www.ogc.org/standards/sos){:target="_blank"} standard is applicable to use cases in which sensor data needs to be managed in an interoperable way. 
   
   {% include note.html content="
   The [NcSOS plugin](https://github.com/asascience-open/ncSOS/wiki){:target='_blank'} adds a OGC SOS service to your existing THREDDS server.
   (This plugin is maintained by the developers at [Applied Science Associates](http://asascience.com/){:target='_blank'}.)
   " %}

#### Basic Configuration:

|-----------------|-------------------|-------------------------------|
| Service Name         | Default Availability | Access Point   |
|:----------------|:------------------|:------------------------------|
| OPeNDAP DAP2 | Enabled | `/thredds/dodsC/*`  |
| NetCDF Subset Service | Enabled | `/thredds/ncss/*`  |
| CDM Remote | Enabled | `/thredds/cdmremote/*`  |
| OGC Web Coverage Service (WCS) | Enabled | `/thredds/wcs/*` |
| OGC Web Map Service (WMS) | Enabled | `/thredds/wms/*` |
| HTTP File Download | Enabled | `/thredds/fileServer/*` |
| OGC Sensor Observation Service (SOS) | *Disabled* | `/thredds/sos/*` | 

#### Catalog Service Configuration:
(These are the exact [required](services_ref.html#tds-requirements-for-thredds-catalog-service-elements) values to enable these service.)

|-----------------|-------------------|------------------------|
|  Service Name |   Service Type  | Service Base URL |
|:----------------|:------------------|:-----------------------|
| OPeNDAP DAP2 | `OPeNDAP` | `/thredds/dodsC/` |
| NetCDF Subset Service | `NetcdfSubset` | `/thredds/ncss/` |
| CDM Remote | `cdmremote` | `/thredds/cdmremote/*` |
| OGC Web Coverage Service (WCS) | `WCS` | `/thredds/wcs/` | 
| OGC Web Map Service (WMS) | `WMS` | `/thredds/wms/` | 
| HTTP File Download | `HTTPServer` | `/thredds/fileServer/` |
| OGC Sensor Observation Service (SOS) | `SOS` | `/thredds/sos/` |
  
   


## TDS Requirements For THREDDS Catalog `service` Elements

Since the TDS provides data access services at predefined URL base paths, services whose access is listed as a THREDDS Catalog `service` element:

1. *must* use the appropriate value for the `serviceType` attribute;
2. *must* use the appropriate value for the service `base` URL attribute; and
3. *may* use any value (unique to the catalog) for the service `name` attribute


### Examples of All Individual Services

The `serviceType` and `base` values are **required** in the following examples:

#### OPeNDAP
  
~~~xml
  <service name="odap" serviceType="OPeNDAP" base="/thredds/dodsC/"/>
~~~

#### NetCDF Subset Service

For Feature Type `GRID`, use:

~~~xml
<service name="ncssGrid" serviceType="NetcdfSubset" base="/thredds/ncss/grid/" />
~~~

For Feature Types `POINT` or `STATION`, use:

~~~xml
<service name="ncssPoint" serviceType="NetcdfSubset" base="/thredds/ncss/point/" />
~~~

#### WCS
  
~~~xml
  <service name="wcs" serviceType="WCS" base="/thredds/wcs/"/>
~~~

#### WMS
  
~~~xml
  <service name="wms" serviceType="WMS" base="/thredds/wms/" />
~~~

#### HTTP Bulk File Service
  
~~~xml
  <service name="fileServer" serviceType="HTTPServer" base="/thredds/fileServer/" />
~~~

#### ncISO
  
~~~xml
  <service name="iso" serviceType="ISO" base="/thredds/iso/" />

  <service name="ncml" serviceType="NCML" base="/thredds/ncml/" />

  <service name="uddc" serviceType="UDDC" base="/thredds/uddc/" />
~~~


#### SOS
  
~~~xml
  <service name="sos" serviceType="SOS" base="/thredds/sos/" />
~~~

### Compound `service` Element Example

~~~xml
<service name="all" serviceType="Compound" base="">
  <service name="HTTPServer" serviceType="HTTPServer" base="/thredds/fileServer/"/>
  <service name="opendap" serviceType="OPENDAP" base="/thredds/dodsC/"/>
  <service name="ncssGrid" serviceType="NetcdfSubset" base="/thredds/ncss/grid/"/>
  <service name="cdmremote" serviceType="CdmRemote" base="/thredds/cdmremote/"/>

  <service name="wcs" serviceType="WCS" base="/thredds/wcs/"/>
  <service name="wms" serviceType="WMS" base="/thredds/wms/"/>

  <service name="iso" serviceType="ISO" base="/thredds/iso/"/>
  <service name="ncml" serviceType="NCML" base="/thredds/ncml/"/>
  <service name="uddc" serviceType="UDDC" base="/thredds/uddc/"/>

  <service name="sos" serviceType="SOS" base="/thredds/sos/"/>
</service>
~~~
