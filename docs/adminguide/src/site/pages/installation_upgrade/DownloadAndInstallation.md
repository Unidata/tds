---
title: Download And Installation
last_updated: 2020-09-20
sidebar: admin_sidebar
toc: true
permalink: download.html
---

{% capture quick_guide_tip%}
Need help?
Use the [TDS Quick Start Guide](https://docs.unidata.ucar.edu/tds/{{site.docset_version}}/quickstart/){:target='_blank'} to get the TDS up and running quickly.
{% endcapture %}

{% include tip.html content=quick_guide_tip%}

## Download

### THREDDS Downloads

|------------|------------------|
| File Type | Link For Download |
|-------------|-----------------|
| TDS WAR File | [https://downloads.unidata.ucar.edu/tds/](https://downloads.unidata.ucar.edu/tds/){:target="_blank"} |
| TDS Docker Image` | [https://hub.docker.com/r/unidata/thredds-docker/](https://hub.docker.com/r/unidata/thredds-docker/){:target="_blank"} |
| TDM JAR File | [https://downloads.unidata.ucar.edu/tds/](https://downloads.unidata.ucar.edu/tds/){:target="_blank"} |
| TDM Docker Image | [https://hub.docker.com/r/unidata/tdm-docker](https://hub.docker.com/r/unidata/tdm-docker/){:target="_blank"} |

{% include important.html content="
Please visit the [Support](support.html) page for information on which version of the TDS and TDM are currently supported.
"%}

### Non-Unidata Package Downloads

|------------|------------------|
| File Type | Link For Download |
|-------------|-----------------|
| OpenJDK Java | [https://adoptopenjdk.net/](https://adoptopenjdk.net/){:target="_blank"} |
| Apache Tomcat Servlet Container` | [http://tomcat.apache.org/](http://tomcat.apache.org/){:target="_blank"} |
| Tomcat Docker Image | [https://hub.docker.com/r/unidata/tomcat-docker](https://hub.docker.com/r/unidata/tomcat-docker){:target="_blank"} |

## Installation

### TDS WAR Installation

The TDS can be installed one of two ways:

1. Copying the TDS WAR file into the Tomcat `webapps` directory (`$tomcat_home/webapps/`).
2. The [Tomcat Manager](tomcat_manager_app.html) application.

{% include note.html content="
The TDS WAR file will have the version number in the file name: `thredds##version_number.war`   
   
Tomcat will see this matching the context information in the TDS WAR `/META-INF/context.xml` file, strip out the hash tags and version information, and make the TDS accessible via this URL structure: `http://servername:port/thredds`     
    
This has the benefit of seeing which version of the TDS is deployed when viewing the raw WAR file.
"%}


### TDM Installation

Install and use of the THREDDS Data Manager is covered in the [THREDDS Data Manager Reference](tdm_ref.html) section.

{% include info.html content="
The THREDDS Data Manager (TDM) creates indexes for [GRIB `featureCollections`](grib_feature_collections_ref.html), in a process separate from the TDS.
Installation and use of the TDM requires further understand of [THREDDS catalogs](catalog_primer.html) and [TDS configuration](basic_tds_configuration.html) covered in later sections of this guide.
"%}
 

