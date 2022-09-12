---
title: GRIB FAQ
last_updated: 2020-08-26
sidebar: admin_sidebar
toc: true
permalink: tds_grib_faq.html
---

## How do I control where the GRIB index files are written?

In `${tomcat}/content/thredds/threddsConfig.xml`, add or change the `<GribIndex>` element:

~~~xml
<GribIndex>
  <alwaysUse>true</alwaysUse>
  <dir>/my/directory/</dir>
</GribIndex>
~~~

This will make all indices written to `/my/directory/`. 
For more detailed, please see the [GRIB Index redirection](/tds_config_ref.html#grib-index-redirection) documentation.

To do it from your Java program:

~~~
 // place cdm indices somewhere other than default
 GribIndexCache.setDiskCache2(DiskCache2 dc);

 // optional object caching for performance
 ucar.unidata.io.RandomAccessFile.enableDefaultGlobalFileCache();
 GribCdmIndex.initDefaultCollectionCache(100, 200, -1);
~~~

## How do I control the way the collection is divided up?

By default, the collection will be time partitioned by directory. 
To change this, add a `timePartition` attribute on the `collection` element:

~~~xml
<featureCollection name="NCEP GFS model" featureType="GRIB1" path="test/all">
  <metadata inherited="true">
    <serviceName>all</serviceName>
    <dataFormat>GRIB-1</dataFormat>
  </metadata>
  <collection name="gfs" spec="/data/GFS/**/.*\.grb$" timePartition="file"/>
</featureCollection>
~~~

The possible values are:
*  `none`: all files are scanned in and a single collection level index is built.
*  `directory`: each directory is a time partition.
* `file`: each file is a time partition.
* `<time unit>`: the files are divided into partitions based on the given time unit.

There are a number of requirements that you must understand for each type. 
For more detailed, please see the [Time Partition](/partitions_ref.html) documentation.

## Can I use NcML inside a GRIB featureCollection?

Not currently, please send us your use cases so we can evaluate feasibility.

## How do I get both the reference and the forecast time of the data?

This information is available for versions 4.6.0+. Each variable has a `coordinates` attribute that names both the *reference time*, and the *forecast time* coordinate. 
If the variable needs only one time dimension, this will look like:

~~~
   float Ground_heat_flux_surface_3_Hour_Average(time=166552, lat=320, lon=640);
      :long_name = "Ground heat flux (3_Hour Average) @ Ground or water surface";
      :units = "W m^-2";
      :coordinates = "reftime time lat lon";

   double time(time=166552);
     :units = "Hour since 1958-01-01T00:00:00Z";
     :standard_name = "time";
     :long_name = "GRIB forecast or observation time";

   double reftime(time=166552);
     :standard_name = "forecast_reference_time";
     :long_name = "GRIB reference time";
     :units = "Hour since 1958-01-01T00:00:00Z";
~~~

Note that in this case, both reference time and forecast time use the same dimension. 
These variables are called one-dimensional time variables.

If the variable needs two time dimensions, this will look like:

~~~
float Geopotential_height_isobaric(reftime=742, time=18, isobaric=37, y=337, x=451);
      :long_name = "Geopotential height @ Isobaric surface";
      :units = "gpm";
      :coordinates = "reftime time1 isobaric y x";

double reftime(reftime=742);
   :units = "Hour since 2015-03-03T00:00:00Z";
   :standard_name = "forecast_reference_time";
   :long_name = "GRIB reference time";

double time(reftime=742, time=18);
   :units = "Hour since 2015-03-03T00:00:00Z";
   :standard_name = "time";
   :long_name = "GRIB forecast or observation time";
~~~

In this case, the form is always `reftime(reftime)` and `time(reftime, time)`. 
These are called *two-dimensional* time variables. 
What we are seeing here are 742 different model runs, with 18 forecast times for each run. 
You can look at these in the [ToolsUI](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/toolsui_ref.html){:target="_blank"} `CoordSys` tab, select a time coordinate from the lowest table, then right clieck and choose `Show values`, and you will see, for example:

~~~
time =
{
 {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0},
 {2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0},
 {3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0},
 {4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0},
 {5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0},
 {6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0},
 ...
~~~

There is one other variant, namely when there is a single runtime. 
Then you get a *scalar runtime coordinate*, e.g.:

~~~
float Dewpoint_temperature_height_above_ground(time=14, height_above_ground1=1, y=1059, x=1799);
  :long_name = "Dewpoint temperature @ Specified height level above ground";
  :units = "K";
  :coordinates = "reftime time height_above_ground1 y x";

double reftime;
  :units = "Minute since 2015-03-30T06:00:00Z";
  :standard_name = "forecast_reference_time";
  :long_name = "GRIB reference time";

double time(time=14);
  :units = "Minute since 2015-03-30T06:00:00Z";
  :standard_name = "time";
  :long_name = "GRIB forecast or observation time";
~~~

This is a *one-dimensional* time variable, and we use a scalar to avoid introducing an unneeded dimension of length 1.

## Why isn't the coordinate for a time interval strictly monotonic?

An example of an interval variable is "Total_precipitation_surface_Mixed_intervals_Accumulation" in NCEP GFS GRIB data.
We call such variable a "mixed interval" variable because it is measured over time intervals that do not have a fixed length.
For example, the `time_bounds` associated with the `time` coordinate for this variable has values:
~~~
time_bounds[254][2]
    [0], 0.0, 3.0
    [1], 0.0, 6.0
    [2], 0.0, 9.0
    [3], 6.0, 9.0
    [4], 0.0, 12.0
    [5], 6.0, 12.0
    [6], 0.0, 15.0
    [7], 12.0, 15.0
    [8], 0.0, 18.0
    [9], 12.0, 18.0
    ...
~~~

Although the CF convention would allow any value in the interval to be the coordinate value, we currently choose the endpoint of the interval.
This leads to a `time` coordinate with values:

~~~
time2[254]
    3.0, 6.0, 9.0, 9.0, 12.0, 12.0, 15.0, 15.0, 18.0, 18.0,...
~~~

So currently, as shown in this example, a mixed interval time coordinate can have repeated values.
In the future we plan to fix this so that the time coordinate is strictly monotonic in this situation, in order to be compliant with the NUG convention.

See also the documentation about the [`intvFilter` option in the GRIB config](grib_collection_config_ref.html#intvfilter-filter-on-time-interval).
