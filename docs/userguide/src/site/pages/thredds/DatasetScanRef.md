---
title: Configuring TDS With DatasetScan
last_updated: 2020-08-21
sidebar: user_sidebar
toc: false
permalink: tds_dataset_scan_ref.html
---

## Overview

The `datasetScan` element allows you to serve all files in a directory tree. The files must be homogenous so that the same metadata can be applied to all of them.

{% include note.html content="
See the [Server-Side Catalog Specification](server_side_catalog_specification.html#datasetscan-element) for the formal definition of the `datasetScan` element XML.
" %}

Here is a minimal catalog containing a `datasetScan` element:

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<catalog name="Unidata Workshop 2006 - NCEP Model Data" version="1.0.1"
    xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
    xmlns:xlink="http://www.w3.org/1999/xlink">
  <service name="myserver" serviceType="OpenDAP" base="/thredds/dodsC/" />

  <datasetScan name="NCEP Data" path="ncep" location="/data/ldm/pub/native/grid/NCEP/" >
    <serviceName>myserver</serviceName>
  </datasetScan>
</catalog>
~~~

The main points are:

* The `path` attribute on the `datasetScan` element is the part of the URL that identifies this `datasetScan` and is used to map URLs to a file on disk.
* The `location` attribute on the `datasetScan` element gives the location of the dataset collection on a local file system or in an Object Store. 
If the location is an Object Store, then a delimiter is required so that the location can be treated as a "directory",
for instance, `location="cdms3:my-bucket?super/long/key/#delimiter=/"`.
For more information on Object Store files and URL syntax see [Dataset URLs](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/dataset_urls.html#object-stores).
* The (`path`, `location`) of every `datasetScan` defines an implicit [datasetRoot](server_side_catalog_specification.html#datasetroot-element). 
   **The `datasetRoot` path therefore must be unique across all `datasetRoots` in the server.**


In the catalog that the TDS server sends to a client, the `datasetScan` element is shown as a catalog reference:

~~~xml
<catalog name="Unidata Workshop 2006 - NCEP Model Data" version="1.0.1"
    xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
    xmlns:xlink="http://www.w3.org/1999/xlink">

  <service name="myserver" serviceType="OpenDAP" base="/thredds/dodsC/" />
  <catalogRef xlink:href="/thredds/catalog/ncep/catalog.xml" xlink:title="NCEP Data" name="" />
</catalog>
~~~

The catalog will be generated dynamically on the server when requested, by scanning the server's directory `/data/ldm/pub/native/grid/NCEP/`. 
For example, if the directory looked like:

~~~
/data/ldm/pub/native/grid/NCEP/
  GFS/
    CONUS_191km/
      GFS_CONUS_191km_20061107_0000.grib1
      GFS_CONUS_191km_20061107_0000.grib1.gbx9
      GFS_CONUS_191km_20061107_0600.grib1
      GFS_CONUS_191km_20061107_1200.grib1
    CONUS_80km/
      ...
    ...
  NAM/
    ...
  NDFD/
    ...
~~~

The result of a request for `/thredds/catalog/ncep/catalog.xml` might look like:

~~~xml
<catalog ...>
  <service name="myserver" serviceType="OpenDAP" base="/thredds/dodsC/" />
  <dataset name="NCEP Data">
    <metadata inherited="true">
      <serviceName>myserver</serviceName>
    </metadata>
    <catalogRef xlink:title="GFS" xlink:href="GFS/catalog.xml" name="" />
    <catalogRef xlink:title="NAM" xlink:href="NAM/catalog.xml" name="" />
    <catalogRef xlink:title="NDFD" xlink:href="NDFD/catalog.xml" name="" />
  </dataset>
</catalog>
~~~

And for a `/thredds/catalog/ncep/GFS/CONUS_191km/catalog.xml` request:

~~~xml
<catalog ...>
  <service name="myserver" serviceType="OpenDAP" base="/thredds/dodsC/" />
  <dataset name="ncep/GFS/CONUS_191km">
    <metadata inherited="true">
      <serviceName>myserver</serviceName>
    </metadata>
    <dataset name="GFS_CONUS_191km_20061107_0000.grib1"
             urlPath="ncep/GFS/CONUS_191km/GFS_CONUS_191km_20061107_0000.grib1" />
    <dataset name="GFS_CONUS_191km_20061107_0000.grib1.gbx"
             urlPath="ncep/GFS/CONUS_191km/GFS_CONUS_191km_20061107_0000.grib1.gbx" />
    <dataset name="GFS_CONUS_191km_20061107_0000.grib1"
             urlPath="ncep/GFS/CONUS_191km/GFS_CONUS_191km_20061107_0600.grib1" />
    <dataset name="GFS_CONUS_191km_20061107_0000.grib1"
             urlPath="ncep/GFS/CONUS_191km/GFS_CONUS_191km_20061107_1200.grib1" />
  </dataset>
</catalog>
~~~

Note that:

* Files are turned into `dataset` elements, subdirectories are turned into nested `catalogRef` elements.
* All the catalog URLs are relative. 
   If the original catalog URL is `http://server:8080/thredds/catalog.xml` then the first `catalogRef /thredds/catalog/ncep/catalog.xml` resolves to `http://server:8080/thredds/catalog/ncep/catalog.xml`. 
   From that catalog, the `catalogRef GFS/catalog.xml` resolves to `http://server:8080/thredds/catalog/ncep/GFS/catalog.xml`.
* The dataset access URLs are built from the service base and the dataset `urlPath` (see [THREDDS URL construction](client_side_catalog_specification.html#constructingURLs)). 
   So the dataset URLs from the above catalog would be `http://server:8080/thredds/dodsC/ncep/GFS/CONUS_191km/GFS_CONUS_191km_20061107_0000.grib1`. 
   (You don't have to worry about these URLs, as they are all generated automatically).
* Each `datasetScan` element must reference a `service` element (whether directly, as above, or inherited).
* Because the TDS uses the set of all given path values to map URLs to datasets, **each `datasetScan` path MUST be unique across all config catalogs on a given TDS installation**.

## Inherited Metadata

The `datasetScan` element is an extension of a `dataset` element, and it can contain any of the `metadata` elements that a dataset can. 
Typically, you want all of its contained datasets to inherit the `metadata`, so add an inherited `metadata` element contained in the `datasetScan` element, for example:

~~~xml
<catalog name="Unidata Workshop 2006 - NCEP Model Data" version="1.0.1"
    xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0">

  <service name="myserver" serviceType="OpenDAP" base="/thredds/dodsC/" />
  <datasetScan name="NCEP Data" path="ncep" location="/data/ldm/pub/native/grid/NCEP/">
    <metadata inherited="true">
      <serviceName>myserver</serviceName>
      <authority>unidata.ucar.edu:</authority>
      <dataType>Grid</dataType>
    </metadata>
  </datasetScan>
</catalog>
~~~

## Including Only Desired Files

A `datasetScan` element can specify which files and directories it will include with a `filter` element.

{% include note.html content="
See the [Server-Side Catalog Specification](server_side_catalog_specification.html#filter-element) for the formal definition of the `filter` element XML.
" %}

When no filter element is given, all files and directories are included in the generated catalog(s). 
Adding a `filter` element to your `datasetScan` element allows you to include (and/or exclude) the files and directories as you desire. 
For instance, the following `filter` and `selector` elements will only include files that end in `.grib1` and exclude any file that ends with `*_0000.grib1`:

~~~xml
<filter>
  <include wildcard="*.grib1"/>
  <exclude wildcard="*_0000.grib1"/>
</filter>
~~~

You can specify which files to include or exclude using either wildcard patterns (with the `wildcard` attribute) or [regular expressions](http://www.regular-expressions.info/){:target="_blank"} (with the `regExp` attribute). 
If the wildcard or regular expression matches the dataset name, the dataset is included or excluded as specified. 
By default, includes and excludes apply only to regular files (atomic datasets). 
You can specify that they apply to directories (collection datasets) as well by using the *atomic* and *collection* attributes.

For instance, the additional selector in this `filter` element means that only directories that don't start with `CONUS` will be cataloged (since the default value of `atomic` is `true`, we have to explicitly set it to `false` if we only want to filter directories):

~~~xml
<filter>
  <include wildcard="*.grib1"/>
  <exclude wildcard="*_0000.grib1"/>
  <exclude wildcard="CONUS*" atomic="false" collection="true"/>
</filter>
~~~

Its a good idea to always use a filter element with explicit includes, so if stray files accidentally get into your data directories, they won't generate erroneous catalog entries. This is known as **whitelisting**.

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<catalog name="Unidata Workshop 2006 - NCEP Model Data" version="1.0.1"
    xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
    xmlns:xlink="http://www.w3.org/1999/xlink">

  <service name="myserver" serviceType="OpenDAP" base="/thredds/dodsC/" />
  <datasetScan name="NCEP Data" path="ncep" location="/data/ldm/pub/native/grid/NCEP/" >
    <serviceName>myserver</serviceName>
    <filter>
      <include wildcard="*.grib1"/>
      <include wildcard="*.grib2"/>
      <exclude wildcard="*.gbx"/>
    </filter>
  </datasetScan>
</catalog>
~~~

Complicated matching can be done with regular expressions, e.g.:

~~~xml
<filter>
  <include regExp="PROFILER_.*_2013110[67]_[0-9]{4}\.nc"/>
</filter>
~~~

A few gotchas to remember:

* to match any number of characters, use `.*`, not `*`
* in the above, we use a `\` to escape the `.` character, to require a literal `.` character. 
  Note that only one backslash is needed. 
   (Inside a Java String, one needs to use `\\`, but not here in the catalog).

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<catalog name="Unidata Workshop 2006 - NCEP Model Data" version="1.0.1"
    xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
    xmlns:xlink="http://www.w3.org/1999/xlink">

  <service name="myserver" serviceType="OpenDAP" base="/thredds/dodsC/" />
  <datasetScan name="NCEP Data" path="ncep" location="/data/ldm/pub/native/grid/NCEP/" >
    <serviceName>myserver</serviceName>
    <filter>
      <include regExp="PROFILER_wind_06min_2013110[67]_[0-9]{4}\.nc"/>
    </filter>
    <addTimeCoverage
          datasetNameMatchPattern="PROFILER_wind_06min_([0-9]{4})([0-9]{2})([0-9]{2})_([0-9]{2})([0-9]{2}).nc$"
          startTimeSubstitutionPattern="$1-$2-$3T$4:$5:00" duration="1 hour"/>
  </datasetScan>
</catalog>
~~~

## Sorting Datasets

Datasets at each collection level are listed in ascending order by name. 
To specify that they are to be sorted in reverse order:

~~~xml
<filesSort increasing="false" />
~~~

Note that the sort is done before renaming.

## Adding A Link To The "Latest" Dataset

You may want to have a special link that points to the *"latest"* data in the collection, especially for data that is constantly being updated, e.g., real-time data. 
Here, *latest* means the last filename in a list sorted by name (so it's only the latest if the time stamp is in the filename and the name sorts correctly by time).

The simplest way to enable this is to add the attribute `addLatest="true"` to the `datasetScan` element. The latest resolver service will be automatically added to the catalog.

~~~xml
<datasetScan name="GRIB2 Data" path="grib2" location="c:/data/grib2/" serviceName="myserver"
    addLatest="true" >
 ...
</datasetScan>
~~~

The `<addLatest>` child element allows more options in configuring the latest service:

~~~xml
<datasetScan name="GRIB2 Data" path="grib2" location="c:/data/grib2/" serviceName="myserver" >
  <addLatest name="Latest Run" top="false" lastModifiedLimit="60000" />
</datasetScan>
~~~

where the `addLatest` attributes mean:
* `name`: the name of the dataset in the catalog (default *latest*)
* `top`: place link on top (try) or bottom (`false`) of the `catalogScan` (default `true`)
* `lastModifiedLimit`: files whose last modified date is less than this amount (in minutes, may be fractional) are excluded (default `0`).

{% include note.html content="
See the [Server-Side Catalog Specification](server_side_catalog_specification.html#addlatest-element) for the formal definition of the `addLatest` element XML.
" %}

## Adding `timeCoverage` Elements

A `datasetScan` element may contain an `addTimeCoverage` element. 
The `addTimeCoverage` element indicates that a `timeCoverage` metadata element should be added to each dataset in the collection and describes how to determine the time coverage for each datasets in the collection.

Currently, the `addTimeCoverage` element can only construct start/duration `timeCoverage` elements and uses the dataset name to determine the start time. 
As described in the [Naming Datasets](tds_dataset_scan_ref.html#naming-datasets) section of this document, the `addTimeCoverage` element applies a [regular expressions](http://www.regular-expressions.info/){:target="_blank"} match to the dataset name. 
If the match succeeds, any regular expression [capturing groups](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#cg){:target="_blank"} are used in the start time replacement string to build the start time string.
These attributes values are used to determine the time coverage:

1. The `datasetNameMatchPattern` attribute value is used for a regular expression match on the dataset name. 
   If a match is found, a `timeCoverage` element is added to the dataset. 
   The match pattern should include capturing groups which allow the match to save substrings from the dataset name.
2. The `startTimeSubstitutionPattern` attribute value has all capture group references (`"$n"`) replaced by the corresponding substring that was captured during the match. 
   The resulting string is used as the start value of the resulting `timeCoverage` element.
3. The `duration` attribute value is used as the duration value of the resulting `timeCoverage` element.

For example, the `addTimeCoverage` element:
~~~xml
<datasetScan name="GRIB2 Data" path="grib2" location="c:/data/grib2/" serviceName="myserver">
  <addTimeCoverage
    datasetNameMatchPattern="([0-9]{4})([0-9]{2})([0-9]{2})_([0-9]{2})([0-9]{2}).grib1$"
    startTimeSubstitutionPattern="$1-$2-$3T$4:00:00"
 duration="60 hours" />
</datasetScan>
~~~

results in the following `timeCoverage` element:

~~~xml
<timeCoverage>
   <start>2005-07-18T12:00:00</start>
   <duration>60 hours</duration>
</timeCoverage>
~~~

A variation is the addition of the `datasetPathMatchPattern` attribute. 
It can be used instead of the `datasetNameMatchPattern` attribute and changes the target of the match from the dataset name to the dataset path. 
If both attributes are used, the `datasetNameMatchPattern` attribute takes precedence.

## Naming Datasets

By default, datasets are named with the corresponding file name. 
By adding a `namer` element, you can specify a more human readable dataset names. 
The following `namer` looks for datasets named `GFS` or `NCEP` and renames them with the corresponding replace string:

~~~xml
<namer>
  <regExpOnName regExp="GFS" replaceString="NCEP GFS model data" />
  <regExpOnName regExp="NCEP" replaceString="NCEP model data"/>
</namer>
~~~

More complex renaming is possible as well. The `namer` uses a [regular expressions](http://www.regular-expressions.info/){:target="_blank"}  match on the dataset name. 
If the match succeeds, any regular expression [capturing groups](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#cg){:target="_blank"} are used in the replacement string.

A capturing group is a part of a regular expression enclosed in parenthesis. 
When a regular expression with a capturing group is applied to a string, the substring that matches the capturing group is saved for later use. 
The captured strings can then be substituted into another string in place of capturing group references,`$n`, where `n` is an integer indicating a particular capturing group. 
(The capturing groups are numbered according to the order in which they appear in the match string.) 

For example, the regular expression `Hi (.), how are (.)?` when applied to the string `Hi Fred, how are you?` would capture the strings `Fred` and `you`. 
Following with a capturing group replacement in the string `$2 are $1` would result in the string "you are Fred."

Here's an example `namer`:

~~~xml
<namer>
  <regExpOnName regExp="([0-9]{4})([0-9]{2})([0-9]{2})_([0-9]{2})([0-9]{2})"
                replaceString="NCEP GFS 191km Alaska $1-$2-$3 $4:$5:00 GMT"/>
</namer>
~~~

The regular expression has five capturing groups:

1. The first capturing group, `([0-9]{4})`,  captures four digits, in this case the year.
2. The second capturing group, `([0-9]{2})`, captures two digits, in this case the month.
3. The third capturing group, `([0-9]{2})`, captures two digits, in this case the day of the month.
4. The fourth capturing group, `([0-9]{2})`, captures two digits, in this case the hour of the day.
5. The fifth capturing group, `([0-9]{2})`, captures two digits, in this case the minutes of the hour.

When applied to the dataset name `GFS_Alaska_191km_20051011_0000.grib1`,  the strings `2005`, `10`, `11`, `00`, and `00` are captured.
After replacing the capturing group references in the `replaceString` attribute value, we get the name `NCEP GFS 191km Alaska 2005-10-11 00:00:00 GMT`. 
So, when cataloged, this dataset would end up as something like this:

~~~xml
<dataset name="NCEP GFS 191km Alaska 2005-10-11 00:00:00 GMT"
         urlPath="models/NCEP/GFS/Alaska_191km/GFS_Alaska_191km_20051011_0000.grib1"/>
~~~
