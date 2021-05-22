---
title: Client Catalogs In ToolsUI
last_updated: 2020-08-25
sidebar: tdsTutorial_sidebar
toc: false
permalink: client_catalog_via_toolsUI.html
---

The NetCDF Tools User Interface (a.k.a. ToolsUI) can read and display THREDDS catalogs.
You can start it [from the command line](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/toolsui_ref.html){:target="_blank"}.

Use the THREDDS Tab, and click on the ![fileOpen](images/tds/tutorial/client_catalogs/fileIcon.jpg){:height="12px" width="12px"} button to navigate to a local catalog file, or enter in the URL of a remote catalog, as below (_note that this is an XML document, not an HTML page!_).
The catalog will be displayed in a tree widget on the left, and the selected dataset will be shown on the right:

{% include image.html file="tds/tutorial/client_catalogs/TUIthreddsTab.png" alt="ToolsUI" caption="" %}

Once you get your catalog working in a TDS, you can enter the TDS URL directly, and view the datasets with the `Open` buttons.
