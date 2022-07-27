---
title: NetCDF Subset Service For Grid Data
last_updated: 2020-08-31
sidebar: user_sidebar
toc: false
permalink: ncss_grid.html
---

## Subsetting Parameters For NCSS Grid

|----------------|----------|-------------------------------|-------------|---------|
| Parameter Name | Required | Description & Possible Values | Constraints | Default |
|----------------|----------|-------------------------------|-------------|---------|
| `var` | yes | Name(s) of variables (comma separated, no blanks), or `all`. | Variables must be in the dataset description. | |
| `north`, `south`, `east`, `west` | no | lat/lon bounding box, units of `degrees_east`, `degrees_north` | `south` < `north`, `west` < `east`; if crossing 180 meridian, use east boundary > 180 | |
| `minx`, `miny`, `maxx`, `maxy` | no | Projection bounding box, in projection coordinate units (`km`) | min < max; use only for gridded data on a projection | |
| `horizStride` | no | Take only every nth point (both x and y) | | `1` |
| `time` | no | Datetime as a W3C Date or present (the time slice closest to the requested time is returned) or `all` (all times are returned). | Must be a time within the dataset time range. | `present` |
| `time_start`, `time_end`, `time_duration` | no | Used to specify a time range (two of these must be present).<br> Datetime may be a W3C Date or present.<br> Duration is a [UDUNIT time](https://cfconventions.org/Data/cf-conventions/cf-conventions-1.7/build/ch04s04.html){:target="_blank"} or [W3C time duration](#w3c-time-duration). | The requested time range must intersect the dataset time range. ||
| `timeStride` | no | Take every nth time in the available series. | Used with `time_range` or `all` | `1` |
| `runtime` | no | Datetime as a W3C Date or `latest` (restrict to that runtime) or `all`. | Used only if there is an independent runtime coordinate. | `latest` |
| `timeOffset` | no | Time as an offset from the runtime coordinate, or `first` or `all`. |  Used only for 2D time coordinate, units same as the dataset. | `all` |
| `vertCoord` | no |Restrict to specified vertical level coordinate value. | Used only if there is a vertical coordinate. | `all` |
| `ensCoord` | no| Restrict to specified ensemble coordinate. | Used only if there is an ensemble coordinate. | `all` |

#### Variable Subsetting
* You must specify a list of valid variable names, or `all`.
* Variable names are case-sensitive and must be URL encoded. 
Be careful not to URL encode twice.

#### Horizontal Spatial Subsetting
* You may specify a lat/lon bounding box or, if the dataset is on a projection, a projection bounding box. 
If none, return the entire horizontal grid.
* Lat/lon bounds start at the `west` value and includes the area going east, until the `east` value.
* Best practice is to make `east` > `west` longitude, by adding `360` to it, if needed.

#### Vertical Spatial Subsetting
* You may specify a vertical coordinate. 
The closest level is returned. 
If not specified, all vertical levels are returned.

#### Temporal sSbsetting
* These rules apply to non-2D time datasets.
* You may specify a time range, or a time point. 
If both, only the time point is used. 
If neither, use the present time.
* A time range (`time_start`, `time_end`, `time_duration`) will request all times that intersect the range (`timeStride` is used if present).
* Two of `time_start`, `time_end`, `time_duration` must be present to define a valid time range. 
(`time_start`, `time_end` may equal present.)
  1. `time_start`, `time_end` : specified range
  2. `time_start`, `time_duration` : starting from time_start until `time_start + time_duration`
  3. `time_end`, `time_duration` : starting from `time_end - time_duration` until `time_end`

* A time point will request the time closest to that value.
* If you set `time="all"`, then all times are returned (`timeStride` is used if present). 
All other time parameters are ignored.
* `time duration`: 
   1. [UDUNIT time unit string](https://cfconventions.org/Data/cf-conventions/cf-conventions-1.7/build/ch04s04.html){:target="_blank"}; or 
   2. [W3C time duration](#w3c-time-duration)
* `datetime`: [W3C Date](#w3c-date) or `present`
* The dataset determines what Calendar is used, so you must use that calendar. 
There can only be one calendar per dataset.
* If no time range or time point, a `timeOffset` can be used to specify the time point.

#### 2D Time Subsetting

* A 2D time dataset will have `CoverageType` set to `FMRC`.
* You may specify a `runtime` with a date, `latest` or `all`. 
Default is `latest`.
* You may specify a `timeOffset` with a numeric value, `first`, or `all`. 
A `timeOffset` value is a duration of time, it is added to the runtime to give the requested time. 
Its units must be the same as the dataset. 
Default is `all`.
* `Time` parameters may be used only used if `timeOffset` is not. 
There are 2 cases:
   1. Runtime is set to a specific value or `latest` (not `all`). 
     Time parameters (`point` or `range`) can then be used.
   2. Runtime to set to `all`. 
     Time point (date, or `present`) only can then be used.

* If no `runtime`, `timeOffset`, or `time` parameters are set, then return all times for latest runtime.
* Special cases:
   1. Set specific `runtime = constant runtime dataset`
   2. Set specific `timeOffset`, set `runTime` to `all = constant offset dataset`
   3. Set specific time, set `runTime` to `all = constant forecast dataset`

#### Interval coordinate

If the coordinate is an interval coordinate (common for vertical or time coordinates), it has a lower and upper bound, e.g., (`2.0-10.0 m`) or (`12-24 hours`).

The request is still made with a single value. 
The interval that contains the requested value is used.

If the requested value is contained in more than one interval (happens with mixed interval time coordinates), the midpoint of the interval is calculated, and the closest midpoint to the requested value is used.

In addition, NCSS supports these parameters for Grids:


|----------------|----------|-------------------------------|-------------|---------|
| Parameter Name | Required | Description & Possible Values | Constraints | Default |
|----------------|----------|-------------------------------|-------------|---------|
| `addLatLon` | no | Make output strictly CF compliant by adding lat/lon coordinates if needed. | `true` or `false` | `false` |
| `accept` | no | The returned data format. | `netCDF` or `netCDF4` | `netCDF` |

#### Adding Lat/Lon Coordinate

To be strictly CF compliant, lat/lon coordinates must be present in the netCDF file. 
For datasets that use a projection, this means that the lat/lon coordinate will be 2D (`lat(y,x), lon(y,x)`). 
Set this parameter to `true` to add 2D lat/lon coordinates for projected data.

#### Output Format (`accept` Parameter)

* `netCDF`: CF/NetCDF-3
* `netCDF4`: CF/NetCDF-4 classic model

## NCSS Grid As Point

If you specify a point location with the latitude, longitude parameters, the request becomes a *grid-as-point* request. 
Request parameters are mostly identical to regular NCSS Grid request, but what gets returned is different. 
If a netCDF file is requested, it will be written in CF / netCDF Discrete Sample Geometry format. 
Other possible return types are the same as NCSS Point requests.

|----------------|----------|-------------------------------|-------------|---------|
| Parameter Name | Required | Description & Possible Values | Constraints | Default |
|----------------|----------|-------------------------------|-------------|---------|
| `latitude`, `longitude` | no | Point location; units of `degrees_east`, `degrees_north` | Must be within the dataset bounding box | |
| `accept` | no | The returned data format. | `netCDF`, `netCDF4`, `XML`, `CSV` | `CSV` |

Note: 
* If you specify a point location with the `latitude`, `longitude` parameters, the request becomes a *grid-as-point* request. 
Other horizontal subsetting parameters (like lat/lon or projection bounding boxes) are ignored.
* Variable with vertical levels will be returned as profiles, unless a specific level is chosen.
* The returned files are written as `CF/DSG` files, either `timeseries` (no vertical coordinate) or `timeSeriesProfile `(if there is a vertical coordinate).

## Use Cases

### Spatial And Coordinate Subsetting

NCSS provides two types of bounding boxes to subset the data:

1. **Lat/lon bounding box** is specified with the parameters `north`, `south`, `east` and `west`. 
The `north` and `south` parameters are latitude values, and must be in units of `degrees_north` and lie between +/- 90. 
The `east` and `west` parameters are longitude values with units of `degrees_east`, and may be positive or negative, and will be interpreted modulo 360. 
The requested subset starts at the west longitude and goes eastward until the east longitude. 
Therefore, when crossing the dateline, the west edge may be greater than the east edge. 
For grids, if the underlying dataset is on a projection, the minimum enclosing projection bounding box will be calculated and used. 
The data contained in the intersection of this rectangle with the data is returned. 
To use, inspect `dataset.xml` for the `<LatLonBox>` elements, which indicate the min and max extensions of the grid.

   For example:

   ~~~xml
   <LatLonBox>
     <west>-153.5889</west>
     <east>-48.5984</east>  
     <south>11.7476</south>
     <north>57.4843</north>
   </LatLonBox>
   ~~~

   Example request:
   
   ~~~
   &north=17.3&south=12.088&west=140.2&east=160.0
   ~~~
   
2. **Projection bounding box** (only on grid datasets with projections) is specified with the parameters `minx`, `miny`, `maxx` and `maxy`. 
These are projection coordinates in `kilometers` on the projection plane; the data contained in the intersection of this rectangle with the data is returned. 
To use, inspect the `dataset.xml` for the `<projectionBox>` elements, which indicate the min and max extensions of the grid. 

   For example:
  
   ~~~xml 
   <gridSet name="time layer_between_two_pressure_difference_from_ground_layer y x">
     <projectionBox>
       <minx>-4264.248291015625</minx>
       <maxx>3293.955078125</maxx>
       <miny>-872.8428344726562</miny>
       <maxy>4409.772216796875</maxy>
     </projectionBox>
   ...
    ~~~
   
    Example request:
   
    ~~~
    &minx=-500&miny=-1600&maxx=500&maxy=0
    ~~~

    By default, if no spatial subsetting is specified, the service returns all the features in the dataset. 
   
### Single-Point Requests   

The NetCDF Subset Service allows the user to extract data for a point of interest by specifying its latitude and longitude. 
The result differs depending on the underlying dataset.

If it's a grid dataset, that means we are using the grid-as-point service. 
NCSS will find the grid cell in which the lat/lon falls and return its data as if it were a point feature. 
The supported output formats are `netCDF`, `netCDF4`, `XML`, and `CSV`.

If it's a station dataset, NCSS will return data for the station nearest the specified lat/lon. 
The supported output formats are `netCDF`, `netCDF4`, `XML`, `CSV` and `WaterML2`.

Point datasets do not support single-point requests.

For example:

~~~
?req=station&var=temp&latitude=40.2&longitude=61.8
~~~

This finds the station nearest to (`lat=40.2, lon=61.8`) and returns its temperature data.  

### Temporal Subsetting And Valid Time Ranges

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

### Vertical Coordinate Subsetting

Subsetting on the vertical axis of a variable or variables with the same vertical levels may be done with the `vertCoord` parameter.

By default, all vertical levels are returned.

### Single Variable Requests

Note that these single variable requests can be easily extended to multi-variable request by simply passing a comma separated list of variables in the `var= parameter`. 
Please note that for grid datasets, each variable in the request must have the same vertical levels.

#### Examples:

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





## Glossary 

### ISO Date

See [W3C Date](#w3c-date)

### W3C Date

For our purposes, a W3C Date, which is a profile of an ISO Date, can be a `dateTime` or a `date`:

A `dateTime` has the form: `'-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?`

where:

* `'-'? yyyy` is a four-or-more digit, optionally negative-signed numeral that represents the year; if more than four digits, leading zeros are prohibited, and `'0000'` is prohibited; also, note that a plus sign is not permitted.
*  the remaining `'-'` are separators between parts of the `date` portion;
* the first `mm` is a two-digit numeral representing the month;
* `dd` is a two-digit numeral representing the day;
* `'T'` is a separator indicating time-of-day follows;
* `hh` is a two-digit numeral representing the hour; `'24'` is permitted if the minutes and seconds represented are zero, and the `dateTime` value so represented is the first instant of the following day (the hour property of a `dateTime` object in the value space cannot have a value greater than `23`);
* `':'` is a separator between parts of the time-of-day portion;
* the second `mm` is a two-digit numeral representing the minute;
* `ss` is a two-integer-digit numeral representing the whole seconds;
* `'.' s+` (if present) represents the fractional seconds;
* `zzzzzz` (if present) represents the time zone (as described below).

For example, `2002-10-10T12:00:00-05:00` (noon on 10 October 2002, Central Daylight Savings Time as well as Eastern Standard Time in the U.S.) is `2002-10-10T17:00:00Z`, five hours later than `2002-10-10T12:00:00Z`.

A `date` is the same as a `dateTime` without the time part `: '-'? yyyy '-' mm '-' dd zzzzzz?`

{% include note.html content="
See the XML Schemas for [`dateTime`](https://www.w3.org/TR/xmlschema-2/#dateTime){:target='_blank'} and [`date`](https://www.w3.org/TR/xmlschema-2/#date){:target='_blank'} for full details.
"%}

### W3C Time Duration

The lexical representation for duration is the [ISO 8601](https://www.w3.org/TR/xmlschema-2/#ISO8601){:target="_blank"} extended format `PnYn MnDTnH nMnS`, where `nY` represents the number of years, `nM` the number of months, `nD` the number of days, `'T'` is the date/time separator, `nH` the number of hours, `nM` the number of minutes and `nS` the number of seconds. 
The number of seconds can include decimal digits to arbitrary precision.

The values of the `Year`, `Month`, `Day`, `Hour` and `Minutes` components are not restricted but allow an arbitrary unsigned integer, i.e., an integer that conforms to the pattern `[0-9]+`. 
Similarly, the value of the `Seconds` component allows an arbitrary unsigned decimal. 
According to [ISO 8601](https://www.w3.org/TR/xmlschema-2/#ISO8601){:target="_blank"}, at least one digit must follow the decimal point if it appears. 
That is, the value of the `Seconds` component must conform to the pattern `(\.[0-9])?`. 
Thus, the lexical representation of duration does not follow the alternative format of 5.5.3.2.1 in [ISO 8601](https://www.w3.org/TR/xmlschema-2/#ISO8601){:target=_blank"}.

An optional preceding minus sign (`-`) is allowed, to indicate a negative duration. 
If the sign is omitted, a positive duration is indicated. 

{% include note.html content="
See also [ISO 8601 Date and Time Formats (D)](https://www.w3.org/TR/xmlschema-2/#isoformats){:target='_blank'} for full details.
"%}

For example, to indicate a duration of 1 year, 2 months, 3 days, 10 hours, and 30 minutes, one would write: `P1Y2M3DT10H30M`. 
One could also indicate a duration of minus 120 days as: `-P120D`.

Reduced precision and truncated representations of this format are allowed provided they conform to the following:

* If the number of years, months, days, hours, minutes, or seconds in any expression equals zero, the number and its corresponding designator may be omitted. 
However, at least one number and its designator must be present.
* The seconds part may have a decimal fraction.
( The designator `'T'` must be absent if and only if all of the time items are absent. 
The designator `'P'` must always be present.

For example, `P1347Y`, `P1347M`, and `P1Y2MT2H` are all allowed, as are `P0Y1347M` and `P0Y1347M0D`.

`P-1347M` is not allowed although `-P1347M` is. 
`P1Y2MT` is not.

{% include note.html content="
See the XML Schema for [duration](https://www.w3.org/TR/xmlschema-2/#duration){:target='_blank'} for full details.
"%}

