---
title: Upgrading to TDS version 5
last_updated: 2020-08-26
sidebar: user_sidebar
toc: false
permalink: upgrade.html
---

## Requirements

* Java 11 is required
* Tomcat 8 (servlet 3.1)
* On the command line when starting up Tomcat/TDS, you must specify `-Dtds.content.root.path=<content root>` where `<content root>` points to the top of the content directory.
  Note, in this exmaple, that this is `/data/content/`, not`/data/content/thredds/`. 
  Don't forget the trailing slash. 
  For example:

  ~~~bash
  -Dtds.content.root.path=/data/content/
  ~~~

## Overview

The configuration catalogs and internal state of the TDS has been extensively re-worked to be able to scale to large numbers of catalogs, datasets, and internal objects without excessive use of memory.
A running TDS can be triggered to reread the configuration catalogs without having to restart.
It can be configured to reread only changed catalogs, for fast incremental updates. Other features have been added to make writing configuration catalogs more maintainable, including the `<catalogScan>` element, and default and standard services.

The other major enhancement is that `GridDataset` is replaced by `FeatureDatasetCoverage`, to better support very large feature collections.
The Coverage API works with _coordinate  values_ (not _array indices_), which solves various intractable problems that arise when using array index subsetting on large collections.

A number of API enhancements have been made to take advantage of evolution in the Java language, for example _try-with-resource_ and _foreach_ constructs.
The use of these make code simpler and more reliable.

Deprecated classes and methods have been removed, and the module structure and third-party jar use has been improved.

## TDS Data Services

### Netcdf Subset Service (NCSS)

NCSS queries and responses have been improved and clarified.
Generally the previous queries are backwards compatible. See [NCSS Reference](netcdf_subset_service_ref.html) for details.

New functionality:
* 2D time can now be handled for gridded datasets, with addition of `runtime` and `timeOffset` parameters.
* Handling of interval coordinates has been clarified.
* Use `ensCoord` to select an ensemble member.

Minor syntax changes:
* Use `time=all` instead of `temporal=all`
* For station datasets, `subset=stns` or `subset=bb` is not needed.
  Just define `stns` or a bounding box.

### `ThreddsConfig.xml`

You no longer turn catalog caching on or off, but you can control how many catalogs are cached (see
[here](tds_config_ref.html) for the new syntax).

The following is no longer used:

~~~xml
<Catalog>
  <cache>false</cache>
</Catalog>
~~~

* By default, most services are enabled, but may still be turned off in `threddsConfig.xml`.

## Java Web Start

Java Web Start has been [deprecated as of Java 9](https://www.oracle.com/technetwork/java/javase/9-deprecated-features-3745636.html#JDK-8184998){:target="_blank"}, and has been removed in [Java 11](https://www.oracle.com/technetwork/java/javase/11-relnote-issues-5012449.html){:target="_blank"}, which is the Long-term Release post-Java 8.
Due to these changes, the netCDF-Java project no longer provide Java Web Start files as of version 5.0.
Following suite, the TDS no longer provide any Web Start based Viewers on Dataset pages out of the box.

### Catalogs

### Catalog Schema Changes

Schema version is now `1.2`.

### Client Catalogs

* `<service>` elements may not be nested inside of `<dataset>` elements, they must be directly contained in the `<catalog>` element.

### Server Configuration Catalogs

* The `<catalogScan>` element is now available, which scans a directory for catalog files (any file ending in xml)
* The `<datasetFmrc>` element is no longer supported
* `<datasetRoot>` elements may not be contained inside of *service* elements, they must be directly contained in the `<catalog>` element
* `<service>` elements may not be nested inside of `<dataset>` elements, they must be directly contained in the `<catalog>` element.
* `<service>` elements no longer need to be explicitly defined in each config catalog, but may reference user defined global services
* If the `datatype/featureType` is defined for a dataset, then the `<service>` element may be omitted, and the default set of services for that `datatype` will be used.
* The `expires` attribute is no longer used.

### Viewers

* `thredds.servlet.Viewer` has `InvDatasetImpl` changed to `Dataset`
* `thredds.servlet.ViewerLinkProvider` has `InvDatasetImpl` changed to `Dataset`
* `thredds.server.viewer.dataservice.ViewerService` has `InvDatasetImpl` changed to `Dataset`

### DatasetScan

* `addID` is no longer needed, ids are always added
* `addDatasetSize` is no longer needed, the dataset size is always added
* With `addLatest`, the `service` name is no longer used, it is always `Resolver`, and the correct service is automatically added.
  Use `addLatest` attribute for simple case.
* `fileSort`: by default, datasets at each collection level are listed in increasing order by filename.
  To change to decreasing order, use the [fileSort](tds_dataset_scan_ref.html#sorting-datasets) element.
* `sort`: deprecated in favor of `filesSort`
* User pluggable classes implementing `UserImplType` (`crawlableDatasetImpl`, `crawlableDatasetFilterImpl`, `crawlableDatasetLabelerImpl`,
`crawlableDatasetSorterImpl`) are no longer supported. (This was never officially released or documented).
* `DatasetScan` details are [here](server_side_catalog_specification.html)

### Standard Services

* The TDS provides standard service elements, which know which services are appropriate for each Feature Type.
* User defined services in the root catalog are global and can be referenced by name in any other config catalog.
* User defined services in non-root catalogs are local to that catalog and override (by name) any global services.
* All services are enabled unless explicitly disabled.
** Except for remote catalog services.
* Standard service details are [here](services_ref.html))

### Feature Collections

* The [`update`](feature_collections_ref.html#update-element) element default is now `startup="never"`, meaning do not update collection on start up, and use existing indices when the collection is accessed.
* The [`fileSort`](tds_dataset_scan_ref.html#sorting-datasets) element is now inside the `featureCollection` itself, so it can be processed uniformly for all types of feature collections.
  When a collection shows a list of files, the files will be sorted by increasing name.
  To use a decreasing sort, use the element `<filesSort increasing="false" />` inside the `featureCollection` element.
  This supersedes the old way of placing that element in the `<gribConfig>` element, or the older verbose `lexigraphicByName` element:

  ~~~xml
    <filesSort>
      <lexigraphicByName increasing="false" />  // deprecated
    </filesSort>
  ~~~

* Feature Collection details are [here](feature_collections_ref.html)

### Recommendations for 5.x catalogs

* Put all `<datasetRoot>` elements in root catalog.
* Put all `<catalogScan>` elements in root catalog.
* Use `StandardServices` when possible.
  Annotate your datasets with `featureType` / `dataType`.
* Put all user-defined `<service>` elements in root catalog.
* Only use user-defined `<service>` elements in non-root catalogs when they are experimental or truly a special case.

### Recommendations for ESGF

You must determine the number of datasets that are contained in all of your catalogs.
To get a report, enable [Remote Management](remote_management_ref.html), and from `https://server/thredds/admin/debug`, select "Make Catalog Report".
This may take 5-20 minutes, depending on the numbers of catalogs.

Add the [`<ConfigCatalog>`](tds_config_ref.html#configuration-catalog) element to `threddsConfig.xml`:

~~~xml
<ConfigCatalog>
  <keepInMemory>100</keepInMemory>
  <reread>check</reread>
  <dir>/tomcat_home/content/thredds/cache/catalog/</dir>
  <maxDatasets>1000000</maxDatasets>
</ConfigCatalog>
~~~

where:

* `keepInMemory`: using the default value of 100 is probably good enough.
* `reread`: use value of _check_ to only read changed catalogs when restarting TDS.
* `dir` is where the catalog cache files are kept.
  Use the default directory (or symlink to another place) unless you have a good reason to change.
* `maxDatasets`: this is the number you found in step 1.
  Typical values for ESGF are 1 - 7 million.
  This is a maximum, so it's ok to make it bigger than you need.

Here are some additional, optional changes you can make to increase maintainability:

1. Place all `datasetRoot` elements in the top catalog
2. Place all `service` elements in the root catalog (_catalog.xml_).
   These can be referenced from any catalog.
3. Remove `<service>` elements from non-root catalogs.
4. Add a [`catalogScan`](server_side_catalog_specification.html#catalogscan-element) element to the root catalog, replacing the list of `catalogRef`s listing all the other catalogs.
* This assumes that other catalogs live in a subdirectory under the root, for example `${tds.content.root.path}/thredds/esgcet/**`.

  For example:

  ~~~xml
  <?xml version='1.0' encoding='UTF-8'?>
  <catalog name="ESGF Master Catalog" version="1.2"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
        xsi:schemaLocation="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0 https://schemas.unidata.ucar.edu/thredds/InvCatalog.1.2.xsd">

    <datasetRoot location="/esg/data" path="esg_testroot"/>
    <datasetRoot location="/esg/arc/data/" path="esg_obs4MIPs"/>
    <datasetRoot location="/esg/cordex/data/" path="esg_cordex"/>
    <datasetRoot location="/esg/specs/data/" path="esg_specs"/>

    <service base="/thredds/dodsC/" desc="OpenDAP" name="gridded" serviceType="OpenDAP">
      <property name="requires_authorization" value="false"/>
      <property name="application" value="Web Browser"/>
    </service>

    <service base="" name="fileservice" serviceType="Compound">
      <service base="/thredds/fileServer/" desc="HTTPServer" name="HTTPServer" serviceType="HTTPServer">
        <property name="requires_authorization" value="true"/>
        <property name="application" value="Web Browser"/>
        <property name="application" value="Web Script"/>
      </service>

      <service base="gsiftp://cmip-bdm1.badc.rl.ac.uk/" desc="GridFTP" name="GridFTPServer" serviceType="GridFTP">
        <property name="requires_authorization" value="true"/>
        <property name="application" value="DataMover-Lite"/>
      </service>

      <service base="/thredds/dodsC/" desc="OpenDAP" name="OpenDAPFiles" serviceType="OpenDAP">
        <property name="requires_authorization" value="false"/>
        <property name="application" value="Web Browser"/>
      </service>
    </service>

    <catalogScan name="ESGF catalogs" path="esgcet" location="esgcet" />

  </catalog>
  ~~~
