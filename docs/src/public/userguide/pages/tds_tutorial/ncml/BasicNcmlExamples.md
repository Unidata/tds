---
title: Basic NcML Examples
last_updated: 2020-08-25
sidebar: tdsTutorial_sidebar
toc: false
permalink: basic_ncml_examples.html
---
Our goal in this section is to enhance two sample datasets using NcML. 
The first dataset is an unknown gridded (we think) netCDF file. 
The second dataset is gridded output from the [Weather Research and Forecasting (WRF)](https://www.mmm.ucar.edu/weather-research-and-forecasting-model){:target="_blank"} model, a very popular atmospheric model among educators and researchers. 
Our goal will be to make this file [CF compliant](https://cfconventions.org/){:target="_blank"}.

## Example 1: Remote Grid File (Unknown)

In this example, we will use NcML to modify a 'remote' dataset to fix it enough to work with viewers that can read "Grids" FeatureTypes (e.g., [IDV](https://www.unidata.ucar.edu/software/idv/){:target="_blank"}).

1. Add the following to your `catalog.xml` file and restart Tomcat:

    ~~~xml
    <datasetRoot path="workshop_ncml" location="/machine/tds/data/ncmlExamples/simpleNcmlTwo/" />

    <dataset name="ncml examples" ID="testNcmlDataset" serviceName="odap"
             urlPath="workshop_ncml/hwave_4D.nc"/>
    ~~~

2.  In the [ToolsUI](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/toolsui_ref.html){:target="_blank"} `Viewer` tab, open [http://localhost:8080/thredds/dodsC/workshop_ncml/hwave_4D.nc](http://localhost:8080/thredds/dodsC/workshop_ncml/hwave_4D.nc]){:target="_blank"}. 
 Do you notice anything missing?
    
3.  In the ToolsUI `CoordSys` tab, open [http://localhost:8080/thredds/dodsC/workshop_ncml/hwave_4D.nc](http://localhost:8080/thredds/dodsC/workshop_ncml/hwave_4D.nc]){:target="_blank"}. 
  Now do you notice anything missing? 
  (*Hint*: what coordinate variables do you expect to see?) 
    
4. Just for fun, let's go to the ToolsUI `FeatureTypes → Grids` tab, and try to open [http://localhost:8080/thredds/dodsC/workshop_ncml/hwave_4D.nc](http://localhost:8080/thredds/dodsC/workshop_ncml/hwave_4D.nc]){:target="_blank"}. 
   Obviously, something isn't right. 
   Let's try to fix things with NcML!  
    
5. In ToolsUI `NcML` tab, open [http://localhost:8080/thredds/dodsC/workshop_ncml/hwave_4D.nc](http://localhost:8080/thredds/dodsC/workshop_ncml/hwave_4D.nc){:target="_blank"} and save the resulting NcML file.
Notice the `Conventions` global attribute? 
Hmmm...
    
6.  It appears that the file is missing `time` and `level` coordinate variable. 
  First, let's add a time variable using NcML. 
  Let's assume that we were told that the file consists of four time steps going from `2011-10-03 0000 UTC - 0900 UTC`. 
  Add the following to the modified NcML file:   

    ~~~xml
    <variable name="time" shape="time" type="int">
      <attribute name="units" value="hours since 2011-10-03" />
      <attribute name="standard_name" value="time" />
      <values>0 3 6 9</values>
    </variable>
    ~~~
7. Next, let's add a `level` variable, which in this case is actually a depth. 
We don't know what the value should be, so we will put in a value of `0` for now and contact the user to get more information. 
Add the following to your NcML file:
    
    ~~~xml
    <variable name="level" shape="level" type="int">
      <attribute name="units" value="m" />
      <attribute name="standard_name" value="depth" />
      <attribute name="positive" value="down" />
      <values>0</values>
    </variable>
   ~~~

8.  Add standard_name attributes to the `latitude` and `longitude` variables, like so:

    ~~~xml
    <variable name="longitude">
      <attribute name="standard_name" value="longitude" />
    </variable>
  
    <variable name="latitude">
      <attribute name="standard_name" value="latitude" />
    </variable>
    ~~~
    
9.  The variable `wave_height` needs an attribute called `coordinates` that has a value `time level latitude longitude`:
    
    ~~~xml
    <variable name="wave_height">
      <attribute name="coordinates" value="time level latitude longitude"/>
    </variable>
    ~~~
    
10. Finally, remove any of the unmodified information, like the dimensions and global attributes.
    
11.  Now, open the NcML file [ToolsUI](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/toolsui_ref.html){:target="_blank"} `FeatureTypes → Grids` tab and visualize the variable `wave_height`.    

## Example 2: Local WRF output

1.  In the [ToolsUI](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/toolsui_ref.html){:target="_blank"}  `Viewer` tab, open `/machine/tds/data/ncmlExamples/simpleNcmlOne/wrfout_d01_2005-08-27_00_00_00`.  
Note that the model output are on an [Arakawa C grid](http://mitgcm.org/sealion/online_documents/node45.html){:target="_blank"}, so we have `regular` and `stag` dimensions.

2.  Open the following link in your browser: [WRF goes CF](https://www.unidata.ucar.edu/blogs/developer/en/entry/wrf_goes_cf){:target="_blank"}

3. In ToolsUI `NcML` tab, open `/machine/tds/data/ncmlExamples/simpleNcmlOne/wrfout_d01_2005-08-27_00_00_00`, and save a copy of the resulting NcML.

4. We will not be changing any of the dimensions or global attributes, so remove them from the NcML. 
Also, many of the variable attributes will not be changed, so they can be removed as well (e.g., `FieldType`, `MemoryOrder`,  `description`).

5. Some variables (notably 2D) have an attribute called `coordinates`, while others do not. [WRF goes CF](https://www.unidata.ucar.edu/blogs/developer/en/entry/wrf_goes_cf_two){:target="_blank"} on which variables need them and go ahead and add a few.

6.  Pay attention to units (for example, see the variable `XTIME`).

7.  To see a partial WRF NcML file, open `/machine/tds/data/ncmlExamples/simpleNcmlOne/wrf-cf.ncml`, which was provided by Rich Signell. Note the addition of the `Conventions` attribute.