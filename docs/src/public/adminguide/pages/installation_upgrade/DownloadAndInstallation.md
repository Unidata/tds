---
title: Download And Installation
last_updated: 2020-09-20
sidebar: admin_sidebar
toc: false
permalink: download.html
---


## Download

### THREDDS Downloads

|------------|------------------|
| File Type | Link For Download |
|-------------|-----------------|
| TDS WAR File | [https://www.unidata.ucar.edu/downloads/tds/](https://www.unidata.ucar.edu/downloads/tds/){:target="_blank"} |
| TDS Docker Image` | [https://hub.docker.com/r/unidata/thredds-docker/](https://hub.docker.com/r/unidata/thredds-docker/){:target="_blank"} |
| TDM JAR File | [https://www.unidata.ucar.edu/downloads/tds/](https://www.unidata.ucar.edu/downloads/tds/){:target="_blank"} |
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

Install the TDS as per the deployment instructions of [Tomcat](http://tomcat.apache.org/){:target="_blank"} servlet container. 

{% capture quick_guide_info %}
See the [TDS Quick Start Guide](https://docs.unidata.ucar.edu/tds/{{site.docset_version}}/quickstart/){:target='_blank'} for detailed step-by-step instructions on how to install the TDS.
{% endcapture %}

{% include info.html content=quick_guide_info%}


### TDM Installation


The THREDDS Data Manager (TDM) creates indexes for [GRIB `featureCollections`](grib_feature_collections_ref.html), in a process separate from the TDS. 

Installation and use of the TDM requires further understand of [THREDDS catalogs](catalog_primer.html) and [TDS configuration](basic_tds_configuration.html) covered in later sections of this guide.
 
Install and use of the THREDDS Data Manager is covered in the [THREDDS Data Manager Reference](tdm_ref.html) section.
