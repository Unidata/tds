---
title: Time Partitions
last_updated: 2020-08-17
sidebar: tdsTutorial_sidebar
toc: false
permalink: partitions_ref.html
---

## Overview

All of the GRIB records in all of the files that you specify constitute a *GRIB collection*. 
The Common Data Model (CDM) creates one or more CDM datasets from this collection. 
There is always an overall *collection dataset*, and for large collections you may want to also present smaller subsets, for example yearly subsets of a multiyear model run. 
To do so, GRIB collections can be divided up into *time partitions*, based on the reference time of the GRIB records.

You may also want to partition your collection due to memory constraints or in order to speed up indexing. 
To build an index, all of the metadata of each GRIB record is read into memory. 
This is somewhere between 200-1000 bytes / record. 
A good estimate is twice the sum of the sizes of all the GRIB index files (`gbx9` files) in the collection. 

This memory is only required when building the index, and after the indexes are built, the memory usage is proportional to the size of the CDM index files (ncx files). 

For example, if you have 1 million records in your collection, you may need ~500 MB of main memory for the indexing. 
The time it takes to index also increases with the number of GRIB records (probably somewhat more than linearly). 
When your collection is changing, partitioned collections will only have to update the partitions that change. 
All of these are reasons you may want to use time partitioning.

{% include note.html content="
See the [THREDDS Data Manager](tdm_ref.html) documentation for options on how to get your collections indexed.
" %}

## Homogeneity Requirements

A feature collection dataset is a homogeneous collection of records. 
That means that the metadata describing the dataset is approximately the same for all of the records. 

Each GRIB record is self-contained, in the sense that it does not reference other records, or an overall schema. 
The point of making a collection of GRIB records into a CDM dataset, is to allow the user to access the entire collection at once, using the netCDF API for multidimensionsal arrays. 
The user cannot, using the netCDF API, access the metadata for individual GRIB records. 
Instead, we assume that the metadata is uniform in the collection, and expose it with variable and global attributes.

## Time Partitions

A time partition is the partitioning of the GRIB records into disjoint sets, based on the reference time of the GRIB records. 
(For model runs the reference time is usually the model run time). 

The partitioning is controlled by the `timePartition` attribute on the `<collection>` element inside the `<featureCollection>`, e.g.:

~~~xml
<featureCollection name="NCEP GFS model" featureType="GRIB1" path="test/all">
  <metadata inherited="true">
    <serviceName>all</serviceName>
    <dataFormat>GRIB-1</dataFormat>
  </metadata>
  <collection name="gfs" spec="/data/GFS/**/.*\.grb$" timePartition="directory"/>
</featureCollection>
~~~

For each partition, a CDM index file is created, and a CDM collection dataset can be acccessed by the user.

The possible values of `timePartition` are:
* [`directory`](partitions_ref.html#directory-partition): each directory is a partition. 
   Nested directories create nested partitions (e.g., year/month/day).
* [`file`](partitions_ref.html#file-partition): each file is a partition.
* [`<time unit>`](partitions_ref.html#time-partition): the files are divided into partitions based on the given time unit. 
    The reference time must be encoded in the filenames. 
    This is the only time you need to use a `dateFormatMark`.
    
* [`none`](partitions_ref.html#none-partition): all files are scanned in, and one collection level index is built.    

#### Directory Partition

In order to use a directory partition, the directory structure must partition the data by reference time. 
That is, all of the data for any given reference time must be completely contained in the directory. 
Directories can be nested to any level. 
To use, add the attribute `timePartition="directory"`, *or simply omit, as this is the default*.

Example:

~~~xml
<featureCollection featureType="GRIB1" name="rdavm partition directory" path="gribCollection/pofp">
  <collection name="ds083.2-directory" 
              spec="Q:/cdmUnitTest/gribCollections/rdavm/ds083.2/PofP/**/.*grib1" 
              timePartition="directory"/> <!-- 1 -->
</featureCollection>
~~~

1. The collection is divided into partitions by directory. 
In order to use this, you cannot have two GRIB records with the same reference time in different directories.

#### File Partition

In order to use a file partition, all of the records for a reference time must be contained in a single file. 
The common case is that each file contains all of the records for a single reference time. 
To use, put `timePartition="file"`.

Example:

~~~xml
<featureCollection featureType="GRIB1" name="rdavm partition directory" path="gribCollection/pofp">
  <collection name="ds083.2-directory" 
              spec="Q:/cdmUnitTest/gribCollections/rdavm/ds083.2/PofP/**/.*grib1" timePartition="file"/> <!-- 1 -->
</featureCollection>
~~~

1. The collection is divided into partitions by files. 
In order to use this, you cannot have two GRIB records with the same reference time in different files.



#### Time Partition

In order to use a time partition, the filenames must contain parsable time information than can be used to partition the data. 
The directory layout, if any, is not used. 
The common case is where all files are in a single directory, and each file has the reference date encoded in the name. 
The split-out of variables does not matter.

If a collection is configured as a time partition, all of the filenames are read into memory at once. 
A date extractor must be specified, and is used to group the files into partitions. 
For example, if `timePartition = "1 year"`, all of the files for each calendar year are made into a collection. 
The overall dataset is the collection of all of those yearly collections.

#### None Partition

If a collection is configured with `timePartition="none"`, all of the records' metadata (excluding the data itself) will be read into memory at once. 
A single top-level collection is written.

This option is good option for small-medium collections (say `< 1M` records) which are not time-partitioned by directory. 
Note that this option takes the longest when indexing, and other strategies are preferred for large collections, especially if the collection is dynamic and must be re-indexed often.

Example:

~~~xml
<featureCollection featureType="GRIB1" 
                   name="gfsConus80_none" path="gribCollection/gfsConus80_none"> <!-- 1 -->
 <metadata inherited="true"> <!-- 2 -->
      <documentation type="summary">This dataset blah blah blah</documentation>
      <documentation xlink:href="http://www.rda.ucar.edu/rda/docs#ds099.9"
            xlink:title="RDA Information"/>
 </metadata>

 <collection name="ds099.9" <!-- 3 -->
    spec="Q:/cdmUnitTest/gribCollections/rdavm/ds099.9/PofP/**/.*grib1" <!-- 4 -->
    timePartition="none"/> <!-- 5 -->

   <update startup="never" trigger="allow"/> <!-- 6 -->
   <tdm rewrite="test" rescan="0 0/15 * * * ? *" /> <!-- 7 -->
   <gribConfig datasetTypes="TwoD Latest Best" /> <!-- 8 -->
</featureCollection>
~~~

1. A `featureCollection` must have a name, a `featureType` and a `path` (do **not** set an `ID` attribute). 
  The name is "human readable" and may change at will. 
  The `featureType` attribute must now equal `GRIB1` or `GRIB2`, not plain `GRIB`.
2. A `featureCollection` is an [`InvDataset`](https://docs.unidata.ucar.edu/netcdf-java/current/javadocAll/thredds/catalog/InvDataset.html){:target="_blank"} object, so it can contain any elements an `InvDataset` can contain, such as metadata. 
  Do **not** set `dataType` or `dataFormat`, as these are set automatically. 
  In this example, we don't set the `serviceName`, so the default service is used.
3. The `collection` `name` should be short but descriptive, it must be unique across all collections on your TDS, and should not change.
4. The collection specification (`spec`) defines the collection of files that are in this dataset.
5. The `partitionType` is `none`.
6. This update element tells the TDS to use the existing indices, and to read them only when an external trigger is sent. 
  This is the default behavior, so it could be omitted.
7. This `tdm` element tells the TDM to test every 15 minutes if the collection has changed, and to rewrite the indices when it has changed.
8. GRIB specific configuration (`gribConfig`), in this case, says to add both the full 2D time collection dataset, the Best, and a resolver link to the latest file.
   In this case, all files are read in by the TDS and a single collection index is made. Two datasets (TwoD and Best)are created for the entire collection. 
   
The simplified catalog looks like this:

{% include image.html file="tds/reference/time_partitions/partition_none.png" alt="timePartition=none Catalog" caption="" %}

Under the hood:

~~~xml
<dataset name="NCEP GFS Puerto_Rico (191km)">
    <metadata inherited="true">
      <serviceName>VirtualServices</serviceName>
      <dataType>GRID</dataType>
      <dataFormat>GRIB-2</dataFormat>
    </metadata>
    <dataset name="Full Collection (Reference / Forecast Time) Dataset" 
             ID="fmrc/NCEP/GFS/Puerto_Rico/TwoD" urlPath="fmrc/NCEP/GFS/Puerto_Rico/TwoD">
      <documentation type="summary">Two time dimensions: reference and forecast; full access to all GRIB records</documentation>
    </dataset>
    <dataset name="Best NCEP GFS Puerto_Rico (191km) Time Series" 
             ID="fmrc/NCEP/GFS/Puerto_Rico/Best" urlPath="fmrc/NCEP/GFS/Puerto_Rico/Best">
      <documentation type="summary">Single time dimension: for each forecast time, use GRIB record with smallest offset from reference time</documentation>
    </dataset>
    <dataset name="Latest Collection for NCEP GFS Puerto_Rico (191km)" urlPath="latest.xml">
      <serviceName>latest</serviceName>
    </dataset>
  </dataset>
~~~

{% include image.html file="tds/reference/time_partitions/none_pizza_with_left_beef.png" alt="None Pizza With Left Beef" caption="None Pizza With Left Beef" %}

## Collection Storage Strategies

Assuming you have control over how GRIB records are stored in the files, here are some best practices to consider.

* Generally, managing many small files has more overhead than managing smaller numbers of large files. 
  For today's disks, file sizes of 100 Mb - 10 Gb seems right. 
  Keep the number of files in a directory small, a few hundred is best, and more than a thousand starts to make things like directory listings hard.

* Partition your files by reference time, which typically is the model run time. 
  Depending on size and number, you might create directories by day, month, year, etc.

* When you have complete control over how the collection is stored on disk, and you want to optimize for fastest THREDDS indexing and retrieval, file partitions are recommended. 
  Placing all of the records for a single reference time in a single file is often optimal. 
  If there are a small number of records for each runtime ( < a few tens of thousands?) you might want to put more than one reference time in each file. 
  It's essential all the records for each runtime be in a single file.

* GRIB files are unordered collections of GRIB records, and the CDM simply scans the files in the collection, looking for GRIB headers. 
  So you can concatenate GRIB files together just using the `cat` command. 
  You can also create tar files; the internal files are ignored and the tar file simply is seen as a collection of GRIB records. 
  Other archives which don't compress are also usable. 
  However, `zip` and `gzip` are not currently usable in this way.

* If you reorganize your file collection, delete any previous THREDDS index files (`.gbx9` and `.ncx4`) and regenerate them with the TDM.
   If you store indexes separate from the data, make sure you track down those directories and delete old index files.


