---
title: NcML Aggregation Examples
last_updated: 2020-08-25
sidebar: dev_sidebar
toc: false
permalink: ncml_aggregation_examples.html
---

Our goal in this section is to aggregate datafiles using NcML.


## Example 1: JoinExisting

1. In the [ToolsUI](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/toolsui_ref.html){:target="_blank"} `Viewer` tab, open `/machine/tds/data/ncmlExamples/aggAdvancedNcmlOne/data/archv.2012_240_00.nc`  
Note that the variable `MT` has a shape of `1` -- there is only one time in the file.

2. In the data file path, change `archv.2012_240_00.nc` to `archv.2012_240_01.nc`. 
Did you notice any changes between the two files?  
The units for `MT` in each file is` `days since 1900-12-31 00:00:00` -- this is an important observation!.

3. Okay, both have an `MT` dimension, which is the dimension of the time variable, so let's aggregate on that. 
Go to the `NcML` tab of ToolsUI and enter the following:

    ~~~xml
    <?xml version="1.0" encoding="UTF-8"?>
    <netcdf xmlns="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2">
      <aggregation dimName="MT" type="joinExisting">
        <scan location="data/" suffix=".nc" subdirs="false"/>
      </aggregation>
    </netcdf>
    ~~~
   
    Save the file as `/machine/tds/data/ncmlExamples/aggAdvancedNcmlOne/joinExisting.ncml`

    Now, switch back to the Viewer tab and open the NcML file you just created. 
    Note that the `MT` variable now has a size of `2` -- Yay! 
    We aggregated the files. 
    All done. 
    Let's close up shop... not so fast.

    Notice anything funky? 
    What is the difference between the variable `MT` vs. `Date`? 
    Which one should be used to obtain the time values? How would someone know?

4.  Open up the NcML file we created in the `CoordSys` tab. 
Notice that there are five coordinate-related variables listed in the bottom pane? 
Notice the two `Coordinate Systems` listed in the middle pane include both `MT` and `Date`? 
Is this correct?

5.  In the bottom pane of the `CoordSys` tab, right click on the `MT` variable and select `Show Values as Date`.  
Do the same for the variable `Date`. 
What do you think we should do?

    {% include question.html content="
    Why is Date even being used as a coordinate variable?
    "%}

6.  Open the `NCDump` tab in ToolsUI and look at the attributes of a variable, say `u`. 
Notice anything?  
The metadata explicitly states that `Date` is a coordinate variable, in addition to the other coordinate variables.

7.  Go back to the `NcML` tab and add the following below the aggregation section of the xml:

    ~~~xml
    <variable name="u">
        <attribute name="coordinates" value="MT Depth Latitude Longitude"/>
    </variable>
    ~~~

    Save the NcML edits and return to the CoordSys tab. What happens? Is this what we want?

8.  Rinse, wash, and repeat for each variable.  

    {% include note.html content="
    Aggregation often involves much more than simply combining files! 
    You really have to know the data you are aggregating.
    "%}

    {% include warning.html content="
    Don't do this with GRIB files! Use the `GribFeatureCollection` instead.
    "%}

## Example 2: JoinNew

In this example, we will use NcML to aggregate data files produced from the same model (same run, actually). 
However, something key is missing, and we will have to add it ourselves. 
Once again, we will see that joining these files is only part of the battle!

1. In the [ToolsUI](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/toolsui_ref.html){:target="_blank"} `Viewer` tab, open `/machine/tds/data/ncmlExamples/aggAdvancedNcmlTwo/data/umwmout_2013-06-04_23-00-00.nc`.

2. In the data file path, change `umwmout_2013-06-04_23-00-00.nc` to `umwmout_2013-06-05_00-00-00.nc`. 
Did you notice any changes between the two files?  
Do you notice anything missing? What dimension will we use to aggregate?

3. Open the file in the `CoordSys` tab. 
Anything important missing?  
Oh, no worries, the TIME IS ENCODED IN THE FILE NAME! 
Good enough, right?  
More common than should be advertised (no need to promote this behavior), so we have an NcML method to grab the date from file names. 
We will need to add a time dimension and variable. 
Open the NcML tab and enter the following:

    ~~~xml
    <?xml version="1.0" encoding="UTF-8"?>
    <netcdf xmlns="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2">
      <aggregation dimName="time" type="joinNew">
        <scan dateFormatMark="umwmout_#yyyy-MM-dd_HH-mm-ss" 
              location="data/" 
              suffix=".nc" subdirs="false"/>
      </aggregation>
    </netcdf>
   ~~~
   
   Save the file as `/machine/tds/data/ncmlExamples/aggAdvancedNcmlTwo/joinNew.ncml`

4. Open your NcML file in the `Viewer` tab. 
Looks good, right? 
Ok, cool. All done. 
Let's close up shop... not so fast. 
Ugh.  
As it is, this will add a `time` dimension to all variables in the file. 
Is that what we want? 
What about the `1D` coordinate variables?

5.  We should explicitly list the variables that we want to aggregate. 
This can be very tedious. 
Go ahead and add the following to your NcML file inside the aggregation tag:

    ~~~xml
    <variableAgg name="u_stokes" />
    <variableAgg name="v_stokes" />
    <variableAgg name="seamask" />
    <variableAgg name="depth" />
    <variableAgg name="wspd" />
    <variableAgg name="wdir" />
    <variableAgg name="uc" />
    <variableAgg name="vc" />
    <variableAgg name="rhoa" />
    <variableAgg name="rhow" />
    <variableAgg name="momx" />
    <variableAgg name="momy" />
    <variableAgg name="cgmxx" />
    <variableAgg name="cgmxy" />
    <variableAgg name="cgmyy" />
    <variableAgg name="taux_form" />
    <variableAgg name="tauy_form" />
    <variableAgg name="taux_form_1" />
    <variableAgg name="tauy_form_1" />
    <variableAgg name="taux_form_2" />
    <variableAgg name="tauy_form_2" />
    <variableAgg name="taux_form_3" />
    <variableAgg name="tauy_form_3" />
    <variableAgg name="taux_skin" />
    <variableAgg name="tauy_skin" />
    <variableAgg name="taux_ocn" />
    <variableAgg name="tauy_ocn" />
    <variableAgg name="taux_bot" />
    <variableAgg name="tauy_bot" />
    <variableAgg name="taux_snl" />
    <variableAgg name="tauy_snl" />
    <variableAgg name="tailatmx" />
    <variableAgg name="tailatmy" />
    <variableAgg name="tailocnx" />
    <variableAgg name="tailocny" />
    <variableAgg name="cd" />
    <variableAgg name="swh" />
    <variableAgg name="mwp" />
    <variableAgg name="mwl" />
    <variableAgg name="mwd" />
    <variableAgg name="dwp" />
    <variableAgg name="dwl" />
    <variableAgg name="dwd" />
    ~~~
    
6.  Open the NcML file in `FeatureTypes → Grids`, click on a variable (say `seamask`), and click the `Red Alien` to visualize the data.  
Again, you really need to know your data to do this! 
Is `seamask` something that should be aggregated? 
Maybe, maybe not.
