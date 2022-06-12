# TDS-UI

ToolsUI for the THREDDS Data Server (TDS) is a version of ToolsUI that corresponds to the version of netCDF-Java in use by the TDS, plus extras that are not included with toolsUI, like the `cdm-s3` subproject from netCDF-Java.
This exists simply for developer convenience.
To run toolsUI from the TDS project, execute the following gradle command from the top of the repository:

~~~shell
./gradlew :tds-ui:run
~~~
