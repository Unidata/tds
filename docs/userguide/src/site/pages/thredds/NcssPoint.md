---
title: NetCDF Subset Service For Point Data
last_updated: 2020-08-31
sidebar: user_sidebar
toc: false
permalink: ncss_point.html
---

## Subsetting Parameters For NCSS Point

|----------------|----------|-------------------------------|-------------|---------|
| Parameter Name | Required | Description & Possible Values | Constraints | Default |
|----------------|----------|-------------------------------|-------------|---------|
| `var` | yes |  Name(s) of variables (comma separated, no blanks), or `all`. |  Variables must be in the dataset description. ||
| `stns` | no | Specify a list of stations (comma separated, no blanks). |  Only on station datasets. ||
| `latitude`, `longitude` | no | Point location; units of `degrees_east`, `degrees_north`. |  Must be within the dataset bounding box. ||
| `north`, `south`, `east`, `west` | no | lat/lon bounding box; units of `degrees_east`, `degrees_north`. | `south` < `north`, `west` < `east`; if crossing 180 meridian, use east boundary > 180 |
| `time` | no | Time as a [W3C Date](ncss_grid.html#w3c-date) or `present` or `all`. | | `present` |
| `time_window` | no | Use with time: how close a match; [UDUNIT time unit string](https://cfconventions.org/Data/cf-conventions/cf-conventions-1.7/build/ch04s04.html){:target="_blank"} or [W3C time duration](#ncss_grid.html#w3c-time-duration) | `1 hour` |
| `time_start`, `time_end`, `time_duration` | no | Used to specify a time range (two of these must be present). <br>Times may be a [W3C Date](ncss_grid.html#w3c-date) or present. <br>Duration is a [UDUNIT time](https://cfconventions.org/Data/cf-conventions/cf-conventions-1.7/build/ch04s04.html){:target="_blank"} or [W3C time duration](ncss_grid.html#w3c-time-duration). | The requested time range must intersect the dataset time range. ||
| `accept` | no | The returned data format. | `netCDF`, `netCDF4`, `XML`, `CSV`, `WaterML2` (station only). | `CSV` |

#### Variable Subsetting

* You must specify a list of valid variable names, or `all`.
     
#### Horizontal Spatial Subsetting

* `stations`: you may specify (in order of precedence): 
   1. a list of stations, 
   2. a point location (find station closest to the point), or 
   3.  a lat/lon bounding box (all stations within the box). 
If none, use `all`.
     
* `points`: you may specify a lat/lon bounding box. 
If none, use `all`.
     
#### Temporal Subsetting

* For all types, you may specify a time range or a specific time. 
If none, return the time closest to the present.
* A time range will request all features that intersect the range.
* A time point will request the feature that is closest to that time, within the time window. 
* The time window defaults to `one hour`.
*  If you include `time=all`, then return `all` times.
     
#### Output Format (`accept` Parameter)

* `csv`: Comma-separated values, one feature per line
* `xml`: Collection of feature elements 
* `netCDF` or `netCDF3`: CF/NetCDF-3
* `netCDF4` or `netCDF-classic`: CF/NetCDF-4 classic model   
* `netCDF4ext`: NetCDF-4 extended model
* `WaterML2`: OGC WaterML 2.0 Timeseries (station only)

## Spatial Subsetting

### Station List

Station datasets only. 
A comma separated list of stations for this dataset. 
You can get the list of stations from the `stations.xml`. 

Example:
    
~~~    
&stns=LECO,LEST,LEVX
~~~
### Single-Point Requests

Station datasets only. 
Find the station closest to the given latitude, longitude. 

Example:

~~~
&latitude=40.2&longitude=61.8
~~~

### Lat/lon Bounding Box

Specified with the params `north`, `south`, `east` and `west`. 
The `north` and `south` parameters are latitude values, and must be in units of `degrees_north` and lie between +/- 90. 
The `east` and `west` parameters are longitude values with units of `degrees_east`, and may be positive or negative, and will be interpreted modulo 360. 
The requested subset starts at the `west` longitude and goes eastward until the `east` longitude. 
Therefore, when crossing the dateline, the west edge may be greater than the east edge. 

Example request:

~~~    
&north=17.3&south=12.088&west=140.2&east=160.0
~~~

## Temporal Subsetting


There are several ways to do temporal subsetting requests:

* `Default`: If no temporal subseting is specified, the closest time to the current time is returned.
* `All time range`: A shorthand to request all the time range in a dataset is setting the parameter `time=all`. 
This can also be done by providing a valid temporal range containing the entire dataset time range.
* `One single time`: Passing the parameter time will get the time slice closest to the requested time if it is within the time range of the dataset.
* `Valid time range`: A valid time range is defined with two of the three parameters: `time_start`, `time_end`, and `time_duration`.

Times (`time`, `time_start`, and `time_end`) must be specified as [W3C Date](#w3c-date) or `present` and `time_duration` as a W3C time duration.

Examples of time query strings with valid temporal ranges:

* `time_start=2007-03-29T12:00:00Z&time_end=2007-03-29T13:00:00Z` (between 12 and 1 pm Greenwich time)
* `time_start=present&time_duration=P3D` (get 3 day forecast starting from the present)
* `time_end=present&time_duration=PT3H` (get last 3 hours)
* `time=2007-03-29T12:00:00Z`
* `time=present`
* `time=all`



|----|-------------------|--------------------------------|
| # |  Request           | Query String                   |
|:---|:------------------|:-------------------------------|
| 1 | All of the data for the variable `Temperature_pressure` for the closest time to the current time. | `?var=Temperature_pressure&temporal=all` |
| 2 | All of the data for the variable `Temperature_pressure` available in a given time range. |`?var=Temperature_pressure&time_start=2015-08-19Z&time_end=2015-08-20T12:00:00Z` |
| 3 | All of the data for the variable T`emperature_pressure` for a specific time. |`?var=Temperature_pressure&time=2015-09-06T00:00:00Z` |
| 4 | Subset the data for the variable `Temperature_pressure` over a given lat/lon bounding box for a specific time. | `?var=Temperature_pressure&time=2015-09-06T00:00:00Z&north=41&west=-109.05&east=-102.05&south=37` |
| 5 | `Temperature_pressure` for every 5th point on the grid (`deltax=deltay=5`) | `?var=Temperature_pressure&horizStride=5` |
| 6 | `Temperature_pressure` for every 5th point on the grid over a given lat/lon bounding box. |`?var=Temperature_pressure&north=41&west=-109.5&east=-102.5&south=37&horizStride=5` |
| 7 | `Temperature_pressure` at a particular vertical level: `1000 mb` (see note below). |`?var=Temperature_pressure&vertCoord=1000` |
| 8 | `Air_temperature` for stations named `LECO`, `LEST`, and `LEVX`. | `?var=air_temperature&subset=stns&stns=LECO,LEST,LEVX` |

{% include note.html content="
In example 7 above, the vertical level value must be in the same units used in the dataset - in this example we assume millibars but you will need to check the dataset description to be sure.
" %}
