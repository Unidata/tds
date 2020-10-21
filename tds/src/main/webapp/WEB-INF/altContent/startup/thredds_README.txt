NAME:
${tds.content.root.path}/thredds/

PURPOSE:
Contains THREDDS catalogs and TDS configuration files. All TDS configuration, modifications, and additions should be
made to the pertinent files in this directory.

CONTENTS:
This directory will contain the following subdirectories and files:

  - cache/                Contains directories for temporary files and on-disk caches.
  - catalog.xml           Main TDS client configuration file (a.k.a, the root catalog) used to serve data.
  - enhancedCatalog.xml   Example configuration catalog, demonstrating advanced features (referenced from catalog.xml).
  - logs/                 TDS-generated log files are located within this directory.
  - notebooks/            Contains public endpoint .ipynb files for the TDS Jupyter Notebook service.
  - public/               Certain files in this directory are automatically mapped and served from the TDS context root.
  - state/                Contains state information about the TDS configuration catalogs.
  - templates/            User-supplied Thymeleaf HTML templates to customize the look and feel of your TDS server.
  - threddsConfig.xml     Main TDS configuration file for allowing non-default services, configuring caching, etc.
  - wmsConfig.xml         A configuration file for the THREDDS Web Mapping Service (WMS).

MORE INFORMATION:
https://docs.unidata.ucar.edu/tds/current/userguide/tds_content_directory.html





