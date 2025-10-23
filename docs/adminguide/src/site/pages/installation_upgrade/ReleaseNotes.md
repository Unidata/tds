---
title: Release Notes
last_updated: 2025-09-17
sidebar: admin_sidebar
toc: true
permalink: release_notes.html
---

## Requirements

* Java and tomcat versions listed [here](install_java_tomcat.html#system-requirements)
* On the command line when starting up Tomcat/TDS, you must specify `-Dtds.content.root.path=<content root>` where `<content root>` points to the top of the content directory.
  Note, in this example, that this is `/data/content/`, not`/data/content/thredds/`.
  Don't forget the trailing slash.
  For example:

  ~~~bash
  -Dtds.content.root.path=/data/content/
  ~~~

## Quick Navigation
* [Upgrade from v5.7 to v5.8](#58-upgrade)
* [Upgrade from v5.6 to v5.7](#57-upgrade)
* [Summary of changes from v4.x through v5.6](#upgrading-from-4x)

## 5.8 Upgrade

The TDS v5.8 release is a bug-fix-only release, and, if upgrading from 5.7, does not require any special changes to the TDS configuration.
If upgrading from version 5.6 or prior, please see the upgrade notes detailed in the rest of this page.
The major bug addressed in this release is related to caching of certain types of datasets on startup.
It is strongly recommended that administrators upgrade to this release.
More information about the release can be found on the [GitHub release page](https://github.com/Unidata/tds/releases/tag/v5.8){:target="_blank"}.

## 5.7 Upgrade

The following changes will impact the upgrade process from v5.6 to v5.7.
If you are upgrading to v5.7 from a version prior to v5.6, please see the [summary of changes from v4.x through v5.6](#upgrading-from-4x)
Going forward, we will document significant changes relative to the previous release and not as a grand summary of changes since 4.x.

### Caching

Chronicle-Map has been replaced with a two-level cache--an entry-limited in-memory [Guava cache](https://github.com/google/guava/wiki/cachesexplained){:target="_blank"} (L1) and a persisted [EclipseStore](https://docs.eclipsestore.io/manual/storage/index.html){:target="_blank"} disk cache (L2).
Since Chronicle-Map has been removed, the need to add special java options to export specific JVM packages is no longer necessary.
We used to document these in `setEnvh.sh` as the `CHRONICLE_CACHE` variable:

```bash
CHRONICLE_CACHE="--add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-exports java.base/sun.nio.ch=ALL-UNNAMED --add-exports jdk.unsupported/sun.misc=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"
```

You should remove these from the java options used to start the TDS.
For more details, please see the [Running Tomcat](running_tomcat.html#setenv.sh) documentation.

Additionally, because of the different caching stack used, a few of the cache related parameters defined in `threddsConfig.xml` have been removed.
Specifically, the `maxBloatFactor` and `averageValueSize` configuration options have been removed from the `FeatureCollection` cache.
For more information, please see the [TDS Configuration File Reference](tds_config_ref.html#featurecollection-cache) documentation.

Finally, you will want to remove any of the old chronicle-map based caches:
* `${tds.content.root.path}/thredds/cache/catalog/*`
* `${tds.content.root.path}/thredds/cache/collection/*`

### threddsIso

The threddsIso service is once again bundled with the TDS war file, eliminating the need to manually obtain and install the plug-in.

### wms

#### New (old) style

The version of the WMS service shipped with TDS 4.x contained a default style called `colored_fat_arrows`.
This style is not part of the new `edal-java` library used in TDS 5.x, and as such, has not been usable out of the box.
While it was possible to manually add this style, it is once again distributed as part of the TDS.

#### Mandatory config changes

If you have not modified the default `wmsConfig.xml` file (`${tds.content.root.path}/thredds/wmsConfig.xml`), you will need to delete it and allow the TDS to create a new one on startup.

If you have modified the `wmsConfig.xml` file, you will need to make some changes before starting TDS v5.7.
First, open the file and ensure that any references to the wmsConfig dtd are updated to reflect the new 2.0 dtd:

```
<!DOCTYPE wmsConfig SYSTEM "https://schemas.unidata.ucar.edu/thredds/dtd/ncwms/wmsConfig_2_0.dtd">
```

Note that older versions may reference the dtd using the hostname `www.unidata.ucar.edu` - these should be updated to use the schemas hostname exactly as shown above.

Additionally, four new default WMS configuration options have been added, and these will need to be added to your existing configuration:

```xml
<wmsConfig>
    <global>
        <defaults>
          ...
          <defaultAboveMaxColor>#000000</defaultAboveMaxColor>
          <defaultBelowMinColor>#000000</defaultBelowMinColor>
          <defaultNoDataColor>extend</defaultNoDataColor>
          <defaultOpacity>100</defaultOpacity>
        </defaults>
    </global>
    ...
</wmsConfig>
```

A description of these parameters can be found in the [wms customization documentation](https://docs.unidata.ucar.edu/tds/current/userguide/customizing_wms.html).


### TDM / TDS Local API

A new, local API has been added to the TDS to simplify the process of running the TDM in situations where the TDS and TDM are running on the same system.
Previously, you needed to configure a tdm tomcat user with authentication to send triggers to the TDS to initiate GRIB related updates.
With the introduction of the new local API, the TDM can send triggers to a TDS located on the same host without the need for authentication.
See the [TDM documentation](tdm_ref.html#local-triggers) for more details.

### Zarr compatible datasetScan

Starting with TDS 5.7, version 2 zarr datasets (represented by a directory or S3 key with appropriate delimiter) will be recognized by `datasetScan`s.
These zarr datasets will have two entries in a catalog: a `catalogRef` that points to a catalog that exposes the underlying directory content (previous behavior), as well as a direct access `dataset` entry (new).
Component files or objects within a zarr dataset (e.g. `.zgroup`, `.zarrts`, `.zarray`) will only be served using the `HTTPServer`.
A zarr dataset can be served by any service other than `HTTPServer`.
Note that many zarr stores utilize `blosc` for compression, which is not supported by netCDF-Java (but is scheduled to be part of the next release), so data access services may fail, but metadata services are expected to work.
Information on how to describe object storage based datasets can be found in the [netCDF-Java DatasetUrl documentation](https://docs.unidata.ucar.edu/netcdf-java/current/userguide/dataset_urls.html#object-stores).
Examples of exposing object store hosted data can be seen [here](https://github.com/Unidata/tds/blob/main/tds/src/test/content/thredds/tds-s3.xml){:target="_blank"} and [here](https://github.com/Unidata/tds/blob/main/tds/src/test/content/thredds/tds-zarr.xml){:target="_blank"} (note S3 GRIB collections are a work in progress and not currently supported).
More documentation to come in the future.

## Upgrading from 4.x

The configuration catalogs and internal state of the TDS has been extensively re-worked to be able to scale to large numbers of catalogs, datasets, and internal objects without excessive use of memory.
A running TDS can be triggered to reread the configuration catalogs without having to restart.
It can be configured to reread only changed catalogs, for fast incremental updates. Other features have been added to make writing configuration catalogs more maintainable, including the `<catalogScan>` element, and default and standard services.

The other major enhancement is that `GridDataset` is replaced by `FeatureDatasetCoverage`, to better support very large feature collections.
The Coverage API works with _coordinate  values_ (not _array indices_), which solves various intractable problems that arise when using array index subsetting on large collections.

A number of API enhancements have been made to take advantage of evolution in the Java language, for example _try-with-resource_ and _foreach_ constructs.
The use of these make code simpler and more reliable.

Deprecated classes and methods have been removed, and the module structure and third-party jar use has been improved.

## New System Requirements

As of TDS 5, the following are considered to be minimum system requirements:

* Java {{ site.java_version }} or above
* Apache Tomcat {{ site.tomcat_version }} or above (or a servlet container that supports servlet specification {{ site.servlet_spec }})

## JVM Setting Requirement Changes

New to TDS 5, you **must** specify the location of the [TDS content directory](tds_content_directory.html) in the [JVM settings](jvm_settings.html#tds-content-directory) documentation.

There is no default location for this directory in the TDS; **`tds.content.root.path` must be set, or the TDS will not start**. 

{% include info.html content="
Consult the [JVM Settings](jvm_settings.html#tds-content-directory) documentation for instructions on how to set tds.content.root.path`.
"%} 

## Changes To TDS Data Services

### cdmrFeature

The `CdmrFeature` services has been removed.
This was an experimental service whose functionality will be replaced.

### Netcdf Subset Service (NCSS)

NCSS queries and responses have been improved and clarified.
Generally the previous queries are backwards compatible. See [NCSS Reference](netcdf_subset_service_ref.html) for details.

#### New Functionality
* 2D time can now be handled for gridded datasets, with addition of `runtime` and `timeOffset` parameters.
* Handling of interval coordinates has been clarified.
* Use `ensCoord` to select an ensemble member.

#### Minor Syntax Changes
* Use `time=all` instead of `temporal=all`
* For station datasets, `subset=stns` or `subset=bb` is not needed.
  Just define `stns` or a bounding box.

### ncWMS

The WMS service bundled with the TDS has been upgraded from ncWMS 1.x to edal-java 2.x.
This also includes an upgrade of the built-in browser based viewer Godiva, from version 2 to 3.
As a result of the move to edal-java 2.x, the default color palletes have changed.
All palette names have either been changed or removed.
The table below summarizes the changes:

| 1.x             | 2.x          |
| --------------- | ------------ |
| alg             | N/A          |
| alg2            | N/A          |
| ferret          | N/A          |
| greyscale       | seq-GreysRev |
| ncview          | x-Ncview     |
| occam           | x-Occam      |
| occam_pastel-30 | N/A          |
| rainbow         | x-Rainbow    |
| redblue         | div-BuRd     |
| sst_36          | x-Sst        |

This change will most likely impact any custom settings in `wmsConfig.xml`, so adjust accordingly.
Please visit the [ncWMS User Guide](https://reading-escience-centre.gitbooks.io/ncwms-user-guide/content/04-usage.html#getmap){:target="_blank"} for a visual listing of all color palette options available with the TDS out of the box.

### Catalog Caching

You no longer turn catalog caching on or off, but you can control how many catalogs are cached (see the
[catalog specification](tds_config_ref.html#configuration-catalog) for the new syntax).

The following is no longer used:

~~~xml
<Catalog>
  <cache>false</cache>
</Catalog>
~~~

## TDS Services

* By default, most services are enabled, but [can be disabled in threddsConfig.xml](tds_config_ref.html).

## Java Web Start Deprecated

Java Web Start has been [deprecated as of Java 9](https://www.oracle.com/technetwork/java/javase/9-deprecated-features-3745636.html#JDK-8184998){:target="_blank"}, and has been removed in [Java 11](https://www.oracle.com/java/technologies/javase/11-relnotes.html){:target="_blank"}, which is the Long-term Release post-Java 8.
Due to these changes, the netCDF-Java project no longer provide Java Web Start files as of version 5.0.0.
Following suite, the TDS no longer provide any Web Start based Viewers on Dataset pages out of the box.

## Changes To THREDDS Catalogs

### Catalog Schema Changes

Schema version is now `1.2`.

### Client Catalogs

* `<service>` elements may not be nested inside of `<dataset>` elements, they must be directly contained in the `<catalog>` element.

### Server Configuration Catalogs

* The `<catalogScan>` element is now available, which scans a directory for catalog files (any file ending in xml).
* The `<datasetFmrc>` element is no longer supported.
* `<datasetRoot>` elements may not be contained inside of *service* elements, they must be directly contained in the `<catalog>` element.
* `<service>` elements may not be nested inside of `<dataset>` elements, they must be directly contained in the `<catalog>` element.
* `<service>` elements no longer need to be explicitly defined in each config catalog, but may reference user defined global services.
* If the `datatype/featureType` is defined for a dataset, then the `<service>` element may be omitted, and the default set of services for that `datatype` will be used.
* The `expires` attribute is no longer used.

### Viewers

* `thredds.servlet.Viewer` has `InvDatasetImpl` changed to `Dataset`
* `thredds.servlet.ViewerLinkProvider` has `InvDatasetImpl` changed to `Dataset`
* `thredds.server.viewer.dataservice.ViewerService` has `InvDatasetImpl` changed to `Dataset`

### DatasetScan

* `addID` is no longer needed; `id`s are always added.
* `addDatasetSize` is no longer needed, the dataset size is always added
* With `addLatest`, the `service` name is no longer used, it is always `Resolver`, and the correct service is automatically added.
  Use `addLatest` attribute for simple case.
* `fileSort`: by default, datasets at each collection level are listed in increasing order by filename.
  To change to decreasing order, use the [`fileSort`](tds_dataset_scan_ref.html#sorting-datasets) element.
* `sort`: deprecated in favor of `filesSort`
* User pluggable classes implementing `UserImplType` (`crawlableDatasetImpl`, `crawlableDatasetFilterImpl`, `crawlableDatasetLabelerImpl`,
`crawlableDatasetSorterImpl`) are no longer supported. 
  (This was never officially released or documented).

{% include info.html content="
Consult the [`DatasetScan`](server_side_catalog_specification.html) specification for more information.
"%} 

### Standard Services

* The TDS provides standard service elements, which know which services are appropriate for each Feature Type.
* User defined services in the root catalog are global and can be referenced by name in any other config catalog.
* User defined services in non-root catalogs are local to that catalog and override (by name) any global services.
* Except for remote catalog services, all services are enabled by default unless explicitly disabled.


{% include info.html content="
Consult the [TDS Services](services_ref.html) references for more information.
"%} 

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

{% include info.html content="
Consult the [Feature Collection](feature_collections_ref.html) references for more information.
"%} 


### Recommendations For 5.0 Catalogs

* Put all `<datasetRoot>` elements in the root catalog.
* Put all `<catalogScan>` elements in the root catalog.
* Use `StandardServices` when possible.
  Annotate your datasets with `featureType` / `dataType`.
* Put all user-defined `<service>` elements in the root catalog.
* Only use user-defined `<service>` elements in non-root catalogs when they are experimental or truly a special case.

### Recommendations For ESGF

For Earth System Grid Federation (ESGF) data, you must determine the number of datasets that are contained in all of your catalogs.
To get a report, enable [Remote Management](remote_management_ref.html), and from `https://server/thredds/admin/debug`, select "Make Catalog Report".

{% include note.html content="
This may take 5-20 minutes, depending on the numbers of catalogs.
"%} 

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
* `reread`: use value of `check` to only read changed catalogs when restarting TDS.
* `dir` is location of the catalog cache files.
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
  This assumes that other catalogs live in a subdirectory under the root, for example `${tds.content.root.path}/thredds/esgcet/**`.

#### ESGF Example

~~~xml
<?xml version='1.0' encoding='UTF-8'?>
<catalog name="ESGF Master Catalog" version="1.2"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
        xsi:schemaLocation="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0 
                            https://schemas.unidata.ucar.edu/thredds/InvCatalog.1.2.xsd">

  <datasetRoot location="/esg/data" path="esg_testroot"/>
  <datasetRoot location="/esg/arc/data/" path="esg_obs4MIPs"/>
  <datasetRoot location="/esg/cordex/data/" path="esg_cordex"/>
  <datasetRoot location="/esg/specs/data/" path="esg_specs"/>

  <service base="/thredds/dodsC/" desc="OpenDAP" name="gridded" serviceType="OpenDAP">
    <property name="requires_authorization" value="false"/>
    <property name="application" value="Web Browser"/>
  </service>

  <service base="" name="fileservice" serviceType="Compound">
    <service base="/thredds/fileServer/" desc="HTTPServer" name="HTTPServer" 
             serviceType="HTTPServer">
      <property name="requires_authorization" value="true"/>
      <property name="application" value="Web Browser"/>
      <property name="application" value="Web Script"/>
    </service>

    <service base="gsiftp://cmip-bdm1.badc.rl.ac.uk/" desc="GridFTP" name="GridFTPServer" 
             serviceType="GridFTP">
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
