NAME:
${tds.content.root.path}   (a.k.a., the TDS content directory)

PURPOSE:
All THREDDS Data Server configuration information is stored here.

Due to the importance of this directory, it is a good idea to locate it somewhere separate from ${tomcat_home} on your
file system. It needs to be persisted between Tomcat upgrades or TDS re-deployments.

CONTENTS:
This directory will contain the following subdirectories:

  - thredds/    Contains THREDDS catalogs and TDS configuration files.
  - tdm/        Will be present if the THREDDS Data Manager (TDM) is installed/used with the TDS.

MORE INFORMATION:
https://docs.unidata.ucar.edu/tds/current/userguide/tds_content_directory.html





