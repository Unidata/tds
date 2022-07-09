# UGRID extensions for netCDF-Java

This is an experimental package for [UGRID](https://ugrid-conventions.github.io/ugrid-conventions/) support for netCDF-Java.
It is meant to be used by the TDS to support Forecast Model Run Collections for files that follow the UGRID conventions.
While this is experimental and intended for use by the TDS, it may become part of the core netCDF-Java project and supported for wider use.
THe initial codebase in the package was incorporated from https://github.com/asascience-open/NetCDF-Java-UGRID (commit: [908827ea](https://github.com/asascience-open/NetCDF-Java-UGRID/commit/908827ea96b38cee4d022c5b8498c48d6fd3adf1).
Updates have been made to support the new API in netCDF-Java 5, and basic tests have been added.
No license was included with the initial code, and source file headers were imported as-is.
Much of the code is under copyright by Applied Science Associates, although some code has an older version of the UCAR/Unidata header.
Code that included the older version of the UCAR/Unidata header has been updated to reflect the new version of the header.
The NetCDF-Java-UGRID codebase was originally intended to be incorporated into the netCDF-Java project, and so we will assume the license is intended to be no more restrictive than the netCDF-Java license.
At the time, netCDF-Java and the THREDDS Data Server (TDS) utilized a [homegrown license](https://github.com/Unidata/thredds/blob/4.6.x/LICENSE.txt), but both have since moved to a standard [BSD-3 Clause license](https://github.com/Unidata/tds/blob/main/LICENSE).
Therefore, we will be releasing this code under the BSD-3 Clause license as well.
