---
title: Server-Side Catalog Specification
last_updated: 2020-08-21
sidebar: dev_sidebar
toc: false
permalink: server_side_catalog_specification.html
---

## Overview

The THREDDS Data Server (TDS) uses specialized catalogs to configure the server, called server-side catalogs or configuration catalogs. 
This document specifies the semantics and XML representation of the server-side specializations allowed in THREDDS catalogs. 
It should be used in conjunction with the [client-side catalog specification](client_side_catalog_specification.html).

Also, see:

* [DatasetScan](tds_dataset_scan_ref.html) reference
* [FeatureCollection](feature_collections_ref.html) reference
* [Catalog XML Schema](https://www.unidata.ucar.edu/schemas/thredds/InvCatalog.1.2.xsd){:target="_blank"}
* [Client-Side Catalog Specification](client_side_catalog_specification.html)

## Base Catalog Elements


### `catalog` Element

In addition to all the elements of a [client catalog](client_side_catalog_specification.html), a server catalog may have `datasetRoot` and `catalogScan` elements, and `dataset` elements may be `datasetScan` or `featureCollection` elements.

~~~xml
<xsd:element name="catalog">
  <xsd:complexType>
    <xsd:sequence>
      <xsd:element ref="service" minOccurs="0" maxOccurs="unbounded"/>
      <xsd:element ref="datasetRoot" minOccurs="0" maxOccurs="unbounded" />
      <xsd:element ref="property" minOccurs="0" maxOccurs="unbounded" />
      <xsd:element ref="dataset" minOccurs="1" maxOccurs="unbounded" />
      <xsd:element ref="catalogScan" minOccurs="0" maxOccurs="unbounded"/>

    </xsd:sequence>

    <xsd:attribute name="base" type="xsd:anyURI"/>
    <xsd:attribute name="name" type="xsd:string" />
    <xsd:attribute name="expires" type="dateType"/>
    <xsd:attribute name="version" type="xsd:token" default="1.2" />
  </xsd:complexType>
</xsd:element>
~~~
### `datasetRoot` Element

~~~xml
<xsd:element name="datasetRoot">
  <xsd:complexType>
    <xsd:attribute name="path" type="xsd:string" use="required"/>
    <xsd:attribute name="location" type="xsd:string" use="required"/>
  </xsd:complexType>
</xsd:element>
~~~

* The `datasetRoot` element associates a portion of the URL path with a directory on disk where the data files are stored. 
* It must be contained directly in the `catalog` element. 
* The `datasetRoot`s are *global* to a TDS, they only need to be declared once; put them in a catalog that is processed before they are used (by convention, put them in the top catalog).

#### Example

~~~xml
<catalog xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0" 
         name="TDS configuration file" >
 <datasetRoot path="dsR1" location="C:/data/mydata/" />
 <datasetRoot path="dsR1/sub2" location="Q:/smaug/floober/" />
 ...
 <dataset name="dataset1" urlPath="dsR1/dataset1.nc" />
 <dataset name="dataset2" urlPath="dsR1/sub/dataset2.nc" />
 <dataset name="dataset3" urlPath="dsR1/sub2/dataset3.nc" />
</catalog>
~~~

In the client catalog, `dataset1` will have URL `dsR1/dataset1.nc` (relative to the catalog URL), and it will be mapped by the server to the file `C:/data/mydata/dataset1.nc`. 

The `dataset2` has URL `dsR1/sub/dataset2.nc`, and refers to the file `C:/data/mydata/sub/dataset2.nc`. 

The dataset roots are searched for the longest match, so `dataset3` with URL `dsR1/sub2/dataset3.nc`, will be matched to the second datasetRoot, and so refers to the file `Q:/smaug/floober/dataset3.nc`.

### `catalogScan` Element

~~~xml
<xsd:element name="catalogScan">
  <xsd:complexType>
    <xsd:attribute name="name" type="xsd:string"/>
    <xsd:attribute name="path" type="xsd:string"/>
    <xsd:attribute name="location" type="xsd:string"/>
  </xsd:complexType>
</xsd:element>
~~~

A `catalogScan` element indicates a top directory to scan for catalogs (files ending in xml). 
Subdirectories will also be scanned. 
Optionally, the directory can be watched, and changes will be detected while the TDS is running. 
The `catalogScan` must be at the top level of the catalog (not nested in a `dataset`). 
If using this element, do not name any of your catalogs `*catalogScan.xml*`.

* `name`: the name to display in the catalog.
* `path`: the logical URL path.
* `location`: the top directory to scan (and all subdirectories). Must be a path relative to `$\{tds.content.root.path}`.

### `dataset` Element

The [`ncml:netcdf`](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/annotated_ncml_schema.html){:target="_blank"} element is specific to server side catalogs, along with the [`restrictAccess`](restict_access_to_tds.html#restrict-access-by-dataset-in-tds-catalogs) attribute:

~~~xml
<xsd:element name="dataset" type="DatasetType" />
<xsd:complexType name="DatasetType">
  <xsd:sequence>
    <xsd:group ref="threddsMetadataGroup" minOccurs="0" maxOccurs="unbounded" />
    <xsd:element ref="access" minOccurs="0" maxOccurs="unbounded"/>
    <xsd:element ref="ncml:netcdf" minOccurs="0"/>                        <!-- (1) ncml:netcdf -->
    <xsd:element ref="dataset" minOccurs="0" maxOccurs="unbounded"/>
  </xsd:sequence>

  <xsd:attribute name="name" type="xsd:string" use="required"/>
  <xsd:attribute name="alias" type="xsd:token"/>
  <xsd:attribute name="authority" type="xsd:string"/>
  <xsd:attribute name="collectionType" type="collectionTypes"/>
  <xsd:attribute name="dataType" type="dataTypes"/>
  <xsd:attribute name="harvest" type="xsd:boolean"/>
  <xsd:attribute name="ID" type="xsd:token"/>
  <xsd:attribute name="restrictAccess" type="xsd:string"/>               <!-- (2) restrictAccess -->
  <xsd:attribute name="serviceName" type="xsd:string" />
  <xsd:attribute name="urlPath" type="xsd:token" />
</xsd:complexType>
~~~

* The `ncml:netcdf` element [modifies the dataset with NcML](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/basic_ncml_tutorial.html){:target="_blank"}. For the [`datasetScan` element](#datasetscan-element), it modifies all contained datasets.
* The `restrictAccess` attribute tells the TDS to [restrict access](restict_access_to_tds.html#restrict-access-by-dataset-in-tds-catalogs) to this dataset. 
It is always inherited by all contained datasets.

### `datasetScan` Element 

A `datasetScan` can be used wherever a `dataset` element is allowed.

~~~xml
<xsd:element name="datasetScan" substitutionGroup="dataset">
  <xsd:complexType>
    <xsd:complexContent>
      <xsd:extension base="DatasetType">
        <xsd:sequence>
          <xsd:element ref="filter" minOccurs="0" maxOccurs="1"/>
          <xsd:element ref="namer" minOccurs="0" maxOccurs="1"/>
          <xsd:element ref="sort" minOccurs="0" maxOccurs="1"/>           <!-- use filesSort -->
          <xsd:element name="addLatest" type="addLatestType" minOccurs="0"/>
          <xsd:element ref="addProxies" minOccurs="0" maxOccurs="1"/>     <!-- use addLatest -->
          <xsd:element ref="addTimeCoverage" minOccurs="0" maxOccurs="1"/>
        </xsd:sequence>

        <xsd:attribute name="path" type="xsd:string" use="required"/>
        <xsd:attribute name="location" type="xsd:string"/>
        <xsd:attribute name="addLatest" type="xsd:boolean"/>
      </xsd:extension>
    </xsd:complexContent>
  </xsd:complexType>
</xsd:element>
~~~

* The `datasetScan` element generates nested THREDDS catalogs by scanning the directory named in the `location` attribute, and creating a `dataset` for each file found, and a `catalogRef` for each subdirectory. 
* The location must be an absolute path. 
* The `path` attribute is used to create the URL for these files and catalogs. 
* The path must be globally unique over all paths for the TDS. 
* Do not put leading or trailing slashes on the path.
* The `addLatest` attribute adds a *latest resolver service* to the `datasetScan`.

A `datasetScan` element is in the `dataset substitutionGroup`, so it can be used wherever a `dataset` element can be used. 
It is an extension of a `DatasetType`, so any of the `dataset` element nested elements and attributes can be used in it. 
This allows you to add enhanced metadata to a `datasetScan`. 
However, you should not add nested datasets, as these will be ignored.

Each generated catalog will include all datasets at the requested level of the given dataset collection location. 
Each collection (directory) dataset will be included as a `catalogRef` element and each atomic (file) dataset will be included as a dataset element. 
The name of the resulting dataset or `catalogRef` will be the name of the corresponding dataset. 
Any inherited metadata from the `datasetScan` will be added to all nested datasets.

Here is a very simple example:

~~~xml
<datasetScan name="GRIB2 Data" path="grib2" location="C:/data/grib2/" >
  <dataFormat>GRIB-2</dataFormat>
</datasetScan >
~~~

The client view of the given `datasetScan` element would look like:

~~~xml
<catalogRef name="" xlink:href="/thredds/catalog/grib2/catalog.xml" xlink:title="GRIB2 Data" />
~~~

If the `C:/data/grib2/` directory contained three files (`data1.wmo`, `data2.wmo`, and `readme.txt`) and one directory (`test`), the catalog at the URL given in the above `xlink:href` attribute would look something like:

~~~xml
<catalog xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0" name="WMO catalogs">
  <service name="myserv" ... />
  <dataset name="GRIB2 Data">
    <metadata inherited="true">
      <serviceName>myserv</serviceName>
    </metadata>
    <dataset name="data1.wmo" urlPath="grib2/data1.wmo" />
    <dataset name="data2.wmo" urlPath="grib2/data2.wmo" />
    <dataset name="readme.txt" urlPath="grib2/readme.txt" />
    <catalogRef xlink:title="test" xlink:href="test/catalog.xml" name="" />
  </dataset>
</catalog>
~~~

### `filter` Element

~~~xml
<xsd:element name="filter">
  <xsd:complexType>
    <xsd:choice>
      <xsd:sequence minOccurs="0" maxOccurs="unbounded">
        <xsd:element name="include" type="FilterSelectorType" minOccurs="0"/>
        <xsd:element name="exclude" type="FilterSelectorType" minOccurs="0"/>
      </xsd:sequence>
    </xsd:choice>
  </xsd:complexType>
</xsd:element>

<xsd:complexType name="FilterSelectorType">
  <xsd:attribute name="regExp" type="xsd:string"/>
  <xsd:attribute name="wildcard" type="xsd:string"/>
  <xsd:attribute name="atomic" type="xsd:boolean"/>
  <xsd:attribute name="collection" type="xsd:boolean"/>
</xsd:complexType>
~~~

* The `filter` element allows users to specify which datasets are to be included in the generated catalogs. 
* A `filter` element can contain any number of `include` and `exclude` elements. 
* Each `include` or `exclude` element may contain either a `wildcard` or a `regExp` attribute. 
* If the given wildcard pattern or [regular expression](http://www.regular-expressions.info/){:target="_blank"} matches a dataset name, that dataset is included or excluded as specified. 

By default, includes and excludes apply only to atomic datasets (regular files). 
You can specify that they apply to atomic and/or collection datasets (directories) by using the `atomic` and `collection` attributes.

Expanding on the above example:

~~~xml
<datasetScan name="GRIB2 Data" path="grib2" location="C:/data/grib2/" >
  <dataFormat>GRIB-2</dataFormat>
  <filter>
    <include wildcard="*.wmo" />
  </filter>
</datasetScan>
~~~

results in:

~~~xml
<catalog xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0">
  <service name="myserv" ... />
  <dataset name="GRIB2 Data">
    <metadata inherited="true"><serviceName>myserv</serviceName></metadata>
    <dataset name="data1.wmo" urlPath="data1.wmo" />
    <dataset name="data2.wmo" urlPath="data2.wmo" />
  </dataset>
</catalog>
~~~

* When there are one or more `include` filters, datasets must pass *at least one* of them. 
* When there are one or more `exclude` filters, datasets must pass *all* of them. 

The logic can be summarized as

`dataset is included if (include1 OR include2 OR ...) AND (!exclude1 AND !exclude2 AND ...)`

More examples are available in the [datasetScan](tds_dataset_scan_ref.html) documentation.

### `namer` Element

~~~xml
<xsd:element name="namer">
  <xsd:complexType>
    <xsd:choice maxOccurs="unbounded">
      <xsd:element name="regExpOnName" type="NamerSelectorType"/>
      <xsd:element name="regExpOnPath" type="NamerSelectorType"/>
    </xsd:choice>
  </xsd:complexType>
</xsd:element>
~~~

~~~xml
<xsd:complexType name="NamerSelectorType">
  <xsd:attribute name="regExp" type="xsd:string"/>
  <xsd:attribute name="replaceString" type="xsd:string"/>
</xsd:complexType>
~~~

The `namer` element specifies one or more ways of creating names for the files in the scan. 
If multiple renamers are specified, the first one that matches the filename is used.

Currently, two types of renaming are available. 
Both methods use [regular expression](http://www.regular-expressions.info/){:target="_blank"}  matching and [capturing group](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html){:target="_blank"} replacement to determine the new name. 
The first type, specified by the `regExpOnName` element, does regular expression matching on the dataset name. 
The second type, specified by the `regExpOnPath` element, does regular expression matching on the entire dataset path. 
In either type, the `regExp` attribute contains the regular expression used in matching on the name or path and the `replaceString` attribute contains the replacement string upon which capturing group replacement is performed.

A capturing group is a part of a regular expression enclosed in parentheses. 
When a regular expression with a capturing group is applied to a string, the substring that matches the capturing group is saved for later use. 
The captured strings can then be substituted into another string in place of capturing group references, "`$n`", where "`n`" is an integer indicating a particular capturing group. 
(The capturing groups are numbered according to the order in which they appear in the match string.) 
For example, the regular expression.

`Hi (.*), how are (.*)?`

when applied to the string "`Hi Fred, how are you?`" would capture the strings "`Fred`" and "`you`". 
Following with a capturing group replacement in the string "`$2 are $1`" would result in the string "`you are Fred`".

Here's an example `namer`:

~~~xml
<namer>
  <regExpOnName regExp="([0-9]{4})([0-9]{2})([0-9]{2})_([0-9]{2})([0-9]{2})"
                replaceString="NCEP GFS 191km Alaska $1-$2-$3 $4:$5:00 GMT"/>
</namer>
~~~

the regular expression has five capturing groups

1 The first capturing group, "`([0-9]{4})`",  captures four digits, in this case the year.
2 The second capturing group, "`([0-9]{2})`", captures two digits, in this case the month.
3 The third capturing group, "`([0-9]{2})`", captures two digits, in this case the day of the month.
4 The fourth capturing group, "`([0-9]{2})`", captures two digits, in this case the hour of the day.
5 The fifth capturing group, "`([0-9]{2})`", captures two digits, in this case the minutes of the hour.

When applied to the dataset name "`GFS_Alaska_191km_20051011_0000.grib1`", the strings "`2005`", "`10`", "`11`", "`00`", and "`00`" are captured. 
After replacing the capturing group references in the `replaceString` attribute value, we get the name "`NCEP GFS 191km Alaska 2005-10-11 00:00:00 GMT`". 
So, when cataloged, this dataset would end up like this (note that only the `name` is affected, not the `urlPath` or `ID`) :

~~~xml
<dataset name="NCEP GFS 191km Alaska 2005-10-11 00:00:00 GMT"
         ID="models/NCEP/GFS/Alaska_191km/GFS_Alaska_191km_20051011_0000.grib1"
         urlPath="models/NCEP/GFS/Alaska_191km/GFS_Alaska_191km_20051011_0000.grib1"/>
~~~

### `sort` Element

~~~xml
<xsd:element name="sort">
  <xsd:complexType>
    <xsd:choice>
      <xsd:element name="lexigraphicByName">
        <xsd:complexType>
          <xsd:attribute name="increasing" type="xsd:boolean"/>
        </xsd:complexType>
      </xsd:element>
    </xsd:choice>
  </xsd:complexType>
</xsd:element>
~~~

By default, datasets at each collection level are listed in ascending order by filename. To do a reverse sort:

~~~xml
<datasetScan>
  <sort>
    <lexigraphicByName increasing="false"/>
  </sort>
</datasetScan>
~~~

### `addLatest` Element

~~~xml
<xsd:complexType name="addLatestType">
  <xsd:attribute name="name" type="xsd:string"/>
  <xsd:attribute name="top" type="xsd:boolean"/>
  <xsd:attribute name="serviceName" type="xsd:string"/>
  <xsd:attribute name="lastModifiedLimit" type="xsd:float"/>  <!-- minutes -->
</xsd:complexType>
~~~

This adds a latest proxy dataset (name is lexicographically greatest in the scan). 
The `name` attribute will set the name of the proxy dataset, and the `top` attribute indicates if the proxy dataset should appear at the top or bottom of the list of dataset in this collection.
Default behavior in the TDS if any these attributes are missing is to name the dataset "`latest.xml`", and place the dataset at the top of the collection. 
If `lastModifedLimit` attribute is set, the TDS will exclude any dataset that was last modified within the number of minutes specified by the `lastModifedLimit` attribute.

An example is available in the [datasetScan](tds_dataset_scan_ref.html) documentation.

### `addTimeCoverage` Element

~~~xml
<xsd:element name="addTimeCoverage">
  <xsd:complexType>
    <xsd:attribute name="datasetNameMatchPattern" type="xsd:string"/>
    <xsd:attribute name="datasetPathMatchPattern" type="xsd:string"/>
    <xsd:attribute name="startTimeSubstitutionPattern" type="xsd:string"/>
    <xsd:attribute name="duration" type="xsd:string"/>
  </xsd:complexType>
</xsd:element>
~~~

The `addTimeCoverage` element indicates that a THREDDS `timeCoverage` element should be added to each atomic dataset cataloged by the containing `datasetScan` element and describes how to determine the time coverage for each dataset in the collection.

Currently, the `addTimeCoverage` element can only describe one method for determining the time coverage of a dataset. 
The  `datasetNameMatchPattern` attribute is used in a [regular expression](http://www.regular-expressions.info/){:target="_blank"} match on the dataset name. If the match succeeds, a [capturing group](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html){:target="_blank"}  replacement is performed on the `startTimeSubstitutionPattern` attribute, and the result is the start time string (see the [`namer` element](#namer-element) description, above, for more on regular expressions and capturing groups). 
The time coverage duration is given by the `duration` attribute.

The `datasetPathMatchPattern` attribute was added (2009-06-05, TDS 4.0) to allow matching on the entire dataset path instead of just the dataset name. 
The two match pattern attributes should not be used together; if they are both given the `datasetNameMatchPattern` will be used.

Example:

~~~xml
<datasetScan name="My Data" path="myData" location="c:/my/data/">
  <serviceName>myserver</serviceName>
  <addTimeCoverage datasetNameMatchPattern="([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})_gfs_211.nc$"
                   startTimeSubstitutionPattern="$1-$2-$3T$4:00:00"
                   duration="60 hours" />
</datasetScan>
~~~

for the dataset named "`2005071812_gfs_211.nc`", results in the following `timeCoverage` element:

~~~xml
<timeCoverage>
  <start>2005-07-18T12:00:00</start>
  <duration>60 hours</duration>
</timeCoverage>
~~~
