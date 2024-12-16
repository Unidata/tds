---
title: Customizing WMS
last_updated: 2024-12-16
sidebar: user_sidebar
toc: false
permalink: customizing_wms.html
---

Several properties related to the generation of images from the WMS service can be configured using the `wmsConfig.xml` file.
By default, this file is located in the `${tds.content.root.path}/thredds` directory.
An example `wmsConfig.xml` file is shipped with the TDS, which looks like:

{% capture rmd %}{% includefile ../tds/src/main/webapp/WEB-INF/altContent/startup/wmsConfig.xml %}{% endcapture %}

~~~xml
{{ rmd }}
~~~

Note that as of TDS v5.6, the Document Type Definition has been updated from `wmsConfig.dtd` to `wmsConfig_2_0.dtd`.
This file provides a way to set default values for WMS parameters when they are missing from a request.
In general, you can provide default values for the following properties:
 * _allowFeatureInfo_: Allow _GetFeatureInfo_ requests.
 * _defaultColorScaleRange_: Range of values to when generating images.
 * _defaultAboveMaxColor_: The color to plot values above the maximum end of the scale range.
 * _defaultBelowMinColor_: The color to plot values below the minimum end of the scale range.
 * _defaultNoDataColor_: The color to use for values which have no data.
 * _defaultOpacity_: The percentage opacity of the final output image.
 * _defaultPaletteName_: A color palette name.
 * _defaultNumColorBands_: The number of color bands to use.
 * _logScaling_: Use a logarithmic scale when generating images.
 * _intervalTime_: Deprecated, is not supported in the new wms service.

Details regarding these properties can be found in the [ncWMS User Guide](https://reading-escience-centre.gitbooks.io/ncwms-user-guide/content/04-usage.html#getmap){:target="_blank"}
There are two main elements to the `wmsConfig.xml` file - the `<global>`, and the `<overrides>`.
Each controls the level of granularity at which default values are chosen.
Settings in `<overrides>` take precedence over settings in `<global>`.

## Global

The `<global>` element contains one `<defaults>` and one `<standardNames>` child element.
It is within these elements that you can control default settings at a `global` level.

### Default

All options must be configured in this section.
These set the default values for all WMS requests.

### Standard Names

Values set under `<defaults>` can be overridden by matching on the value of a `standard_name` attribute of a variable.
With the exception of _allowFeatureInfo_, all other properties can be set based on `standard_name`.
Because this is global, you must include information about the `units` used to define the `<colorScaleRange>`.
This allows the WMS service to deal with variables that have the same `standard_name` yet have different, but comparable, units.
The units must be defined using a `udunits` compatible string.
The current set of unit strings support can be found in [this xml document](https://docs.unidata.ucar.edu/thredds/udunits2/current/udunits2_combined.xml){:target="_blank"}.
A more user-friendly version can be found at [this very helpful site](https://ncics.org/portfolio/other-resources/udunits2/){:target="_blank"}, which is maintained by the [North Carolina Institute for Climate Studies](https://ncics.org/){:target="_blank"}.

## Overrides

The `<overrides>` element contains a series of `<datasetPath>` children.
The `pathSpec` attribute of a `<datasetPath>` element allows for applying default settings based on the dataset path as seen in the TDS url (i.e. the dataset ID).
Default values can be set for all properties based on the path.
With the exception of _allowFeatureInfo_, these can be overridden on a variable by variable basis based on the name of the variable.

## Default Precedence Summary

Default values for a given property are selected based on matches (lowest to highest precedence):

`global/defaults` < `global/standardName` < `overrides/pathDefaults` < `overrides/variable`
