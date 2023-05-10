---
title: Forecast Model Run Collection (FMRC)
last_updated: 2020-08-26
sidebar: user_sidebar
toc: false
permalink: fmrc_ref.html
---

## Overview

A **Forecast Model Run Collection (FMRC)** is a **collection of forecast model runs** which can be uniquely identified by the start of the model run, called the model **run time**, (also called the **analysis time** or **generating time** or **reference time**). 
Each model run has a series of forecast times. 
A collection of these runs therefore has two time coordinates, the run time and the forecast time. 
An FMRC creates a 2D time collection dataset, and then creates various 1D time subsets out of it. 
See this [poster](https://www.unidata.ucar.edu/software/tds/current/tutorial/files/FmrcPoster.pdf){:target="_blank"} for a detailed example.

Previously this functionality was provided using [FMRC Aggregation](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/fmrc_ref.html){:target="_blank"} through NcML and the `<fmrcDataset>` element in the TDS configuration catalog. 
As of TDS 4.2, that implementation is now deprecated and `<featureCollection>` elements are the correct way to provide this functionality. 
As of 4.3, one should only serve GRIB files with `featureCollection=GRIB`, e.g., not with FMRC.
Typically, FMRC is used for collections of model runs stored in netCDF/CF files.

## Constraints On FMRC

* The component files of the collection must all be recognized as *Grid* Feature datasets by the CDM software.
* Each component file must have a single reference time.
* The times and variables for a model run can be in a single file or spread out among multiple files.
* The model runs are assumed to be homogenous, that is, they contain the same collection of variables and attributes, and they must be on the same horizontal and vertical grid. 
The model runs can differ only in their time and runtime coordinates and the actual data values.

#### Notes
* It's best if the reference time is part of the filename, in a way that can be extracted with a [DateExtractor](feature_collections_ref.html#date-extractor).
* If there is a `serviceType=HTTPServer` for the Feature Collection, it is removed from the virtual datasets (all except the Files datasets).
* If an `ID` attribute is not specified on the `featureCollection`, the `path` attribute is used as the `ID`. 
This is a preferred idiom.
* Note that for the case when a model run dataset is in a single file, it *may be different than the same file as seen through the corresponding `_Files` dataset*, if `regularize` is enabled. 
In that case, the time coordinates will be regularized across all model run datasets in the collection.
* The FMRC virtual dataset is assembled by examining the Grid Coordinate Systems of the component files. 
One can use NcML to fix some problems in the component files.

## `fmrcConfig` Element

Defines options on feature collections with `featureType=FMRC`.

~~~xml
<fmrcConfig regularize="false" datasetTypes="TwoD Best Files Runs" />
<fmrcConfig regularize="false" datasetTypes="Files">
  <bestDataset name="Best_Exclude_Spinup" offsetsGreaterEqual="0"/>
</fmrcConfig>
~~~

where:

1.  `regularize`: If `true`, then the runs for a given hour (from `0Z`) are assumed to have the same forecast time coordinates. 
For example, if you have 4 model runs per day (e.g.: `0`, `6`, `12`, `18Z`) and many days of model runs, then all the `6Z` runs for all days will have the same time coordinates, etc. 
This "regularizes" time coordinates, and is useful when there may be missing forecast times, which may result in creating a new time coordinate. 
**Leave this to `false` unless you really have a series of runs with uniform offsets**.

2. `datasetTypes`: list the dataset types that are exposed in the TDS catalog. 
The possible values are:

    * `TwoD`: dataset with two time dimensions (run time and forecast time), which contains all the data in the collection.
    * `Best`: dataset using the latest model data available for each possible forecast hour.
    * `Files`: each component file of the collection is available separately, as in a `datasetScan`. 
    A "latest" file will be added if there is a "latest" Resolver service in the catalog.
    * `Runs`: A *model run dataset* contains all the data for one run time.
    * `ConstantForecasts`: A *constant forecast dataset* is created from all the data that have the same forecast time. 
    This kind of dataset has successively shorter forecasts of the same endpoint.
    * `ConstantOffsets`: A *constant offset dataset* is created from all the data that have the same offset from the beginning of the run.

3.  `bestDataset`: you can define your own "best dataset". 
This uses the same algorithm as the `Best` dataset above, but excludes data based on its offset hour. 
In the above example, a `Best` dataset is created with offset hours less than `0` excluded.

    * `name`: the human visible name of the defined `Best` dataset, must be unique within the `fmrcConfig` element. 
    Do not use `best.ncd`, `fmrc.ncd`, `runs`, `files`, `forecast`, or `offset`.
    * `offsetsGreaterEqual`: forecast offset hours (forecast time - run time) less than this value are excluded.

#### Notes:

* If an `fmrcConfig` element is not present, the default is `regularize=false`, and `datasetTypes="TwoD Best Files Runs"`. 
Specifying your own `fmrcConfig` completely overrides the datasetTypes default.
* When using FMRC for gridded data that doesn't have `2D` times, be sure to put `regularize=false` (or leave it off).

## Working With FMRC In ToolsUI

The [ToolsUI](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/toolsui_ref.html){:target="_blank"} `FMRC` tab allows you to view internal structures of an FMRC. 
You can pass it a [collection specification string](/collection_spec_string_ref.html) or a file with a `featureCollection` element in it.

## Working With FMRC In Client Software

#### Opening An FMRC

Use static method on `ucar.nc2.ft.fmrc.Fmrc`:

~~~
public static Fmrc open(String collection, Formatter errlog, Formatter debug);
~~~

The collection may be one of:
* [collection specification string](collection_spec_string_ref.html)
* `catalog:catalogURL`
* `filename.ncml`

#### Run Date

If a `dateFormatMark` is given, a [DateExtractor](feature_collections_ref.html#date-extractor) extracts the run-date from the filename or URL. 
Otherwise, there must be global attributes `_CoordinateModelBaseDate` or `_CoordinateModelRunDate` inside each dataset. 
The GRIB IOSP reader automatically adds this global attribute.

#### Forecast Date

Each file is opened as a GridDataset:

~~~
gds = GridDataset.open( mfile.getPath());
~~~

and the forecast time coordinates are extracted from the grid coordinate system.

There is no need to specify `forecastModelRunCollection` vs `forecastModelRunSingleCollectionc`, nor `timeUnitsChange`. 
This is detected automatically.

#### Regular

If true, then all runs for a given offset hour (from `0Z`) are assumed to have the same forecast time coordinates. 
This obviates the need for the FMRC definition files which previously were used on the Unidata data server motherlode. 
This evens out time coordinates, and compensates for missing forecast times in the IDD feed.

## Persistent Caching

The FMRC cache, currently implemented with [Chronicle Map](https://chronicle.software/open-hft/map/){:target="_blank"},
records the essential grid information from each file in a key/value store.
This cache is persisted to disk, and so also persists between reboots.
When a collection is scanned, any filenames already in the database are reused. 
Any new ones are read and added to the database. 
Any entries in the database that no longer have a filename associated with them are deleted.
The cache database file is located in `${tds.content.root.path}/thredds/cache/collection/GridDatasetInv.dat`.
See also [FMRC cache settings documentation](https://docs.unidata.ucar.edu/tds/current/userguide/tds_config_ref.html#featurecollection-cache).

## Conversion of `<datasetFmrc>` to `<featureCollection>`

There is no need to specify `forecastModelRunCollection` versus `forecastModelRunSingleCollection`, nor `timeUnitsChange`. 
This is detected automatically.

#### Old Way #1

~~~xml
<datasetFmrc name="NCEP-GFS-CONUS_80km" collectionType="ForecastModelRuns" harvest="true" 
             path="fmrc/NCEP/GFS/CONUS_80km">   <!-- 1 -->
   <metadata inherited="true">
   <documentation type="summary">good stuff</documentation>
  </metadata>
   <netcdf xmlns="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2" enhance="true"> <!-- 2 -->
     <aggregation dimName="run" type="forecastModelRunCollection" 
                  fmrcDefinition="NCEP-GFS-CONUS_80km.fmrcDefinition.xml" recheckEvery="15 min">
       <scan location="/data/ldm/pub/native/grid/NCEP/GFS/CONUS_80km/" suffix=".grib1"
             dateFormatMark="GFS_CONUS_80km_#yyyyMMdd_HHmm" subdirs="true" olderThan="5 min"/>
     </aggregation>
   </netcdf>
   <fmrcInventory location="/data/ldm/pub/native/grid/NCEP/GFS/CONUS_80km/" 
                  suffix=".grib1" 
                  fmrcDefinition="NCEP-GFS-CONUS_80km.fmrcDefinition.xml" />  <!-- 3 -->
   <addTimeCoverage datasetNameMatchPattern="GFS_CONUS_80km_([0-9]{4})([0-9]{2})([0-9]{2})_([0-9]{2})00.grib1$"
                    startTimeSubstitutionPattern="$1-$2-$3T$4:00:00"
                    duration="240 hours"/>
</datasetFmrc>
~~~

where:
1. `datasetFmrc` replaced by `featureCollection`
    * optional `collectionType=ForecastModelRuns` → mandatory `featureType=FMRC`

2. NcML `netcdf` element describing the aggregation is now done by `collection` element
    * `aggregation` `dimName`, `type`, and `fmrcDefinition` are no longer needed
    * `netcdf scan` `location`, `suffix`, `subdirs`, and `dateFormatMark` are replaced by `collection spec`

3. `fmrcInventory` and `addTimeCoverage` elements are no longer needed.


#### Old Way #2

~~~xml
<datasetFmrc name="RTOFS Forecast Model Run Collection" path="fmrc/rtofs">
  <netcdf xmlns="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2">
    <variable name="mixed_layer_depth">  <!-- 1 -->
      <attribute name="long_name" value="mixed_layer_depth @ surface"/>
      <attribute name="units" value="m"/>
    </variable>
    <aggregation dimName="runtime" type="forecastModelRunSingleCollection" timeUnitsChange="true" recheckEvery="10 min">
      <variable name="time">   <!-- 2 -->
        <attribute name="units" value="hours since "/>
      </variable>
      <scanFmrc location="c:/rps/cf/rtofs" regExp=".*ofs_atl.*\.grib2$"
                runDateMatcher="#ofs.#yyyyMMdd" forecastOffsetMatcher="HHH#.grb.grib2#" subdirs="true"
                olderThan="10 min"/>    <!-- 3 -->
    </aggregation>
  </netcdf>
</datasetFmrc>
~~~

where:
1. On the outside of the aggregation, attributes are being added/modified for the existing variable `mixed_layer_depth` in the *resulting FMRC dataset*.
2. On the inside of the aggregation, an attribute is being added/modified for the existing variable `time` *for each dataset in the collection*. 
Typically, you need to do this in order to make the component files into a gridded dataset.
3. The collection is defined by a `scanFmrc` element, creating a `forecastModelRunSingleCollection` with one forecast time per file.

#### New Way

~~~xml
<featureCollection name="NCEP-GFS-CONUS_80km" featureType="FMRC" harvest="true" 
                   path="fmrc/NCEP/GFS/CONUS_80km">
  <metadata inherited="true">
    <documentation type="summary">good stuff</documentation>
  </metadata>
  <collection spec="/data/ldm/pub/native/grid/NCEP/GFS/CONUS_80km/GFS_CONUS_80km_#yyyyMMdd_HHmm#.grib1"
               recheckAfter="15 min"
               olderThan="5 min"/>  <!-- 1 -->
  <update startup="true" rescan="0 5 3 * * ? *" />  <!-- 2 -->
  <protoDataset choice="Penultimate" change="0 2 3 * * ? *" />  <!-- 3 -->
  <fmrcConfig regularize="true" 
              datasetTypes="TwoD Best Files Runs ConstantForecasts ConstantOffsets" />  <!-- 4 -->
</featureCollection>
~~~

1. `collection` `spec` element
   * `collection` `recheckAfter` is the same as `aggregation` `recheckEvery`
   * `collection` `olderThan` is the same as `scan` `olderThan`
2. `update` (optional) allows control over when the `featureCollection` is updated.
3. `protoDataset` (optional) allows control over the selection of the `prototypical` dataset.
4. `fmrcConfig` (optional) allows control over which FMRC virtual datasets are made available.
