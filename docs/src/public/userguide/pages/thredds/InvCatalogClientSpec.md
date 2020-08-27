---
title: Client-Side Catalog Specification
last_updated: 2020-08-25
sidebar: tdsTutorial_sidebar
toc: false
permalink: client_side_catalog_specification.html
---

## Overview

A THREDDS catalog is a way to describe an inventory of available datasets. 
These catalogs provide a simple hierarchical structure for organizing a collection of datasets, an access method for each dataset, a human understandable name for each dataset, and a structure on which further descriptive information can be placed.

This document specifies the semantics of a THREDDS catalog, as well as its representation as an XML document. 
It is written to help clients read and understand THREDDS catalogs received from a server.

Related resources:

* [Client Catalog Primer](basic_client_catalog.html) 
* [Client Catalog Example](https://thredds.ucar.edu/thredds/idd/forecastModels.xml){:target="_blank"} 
* [Catalog XML Schema](https://www.unidata.ucar.edu/schemas/thredds/InvCatalog.1.2.xsd){:target="_blank"}
* [Server-Side Catalog Specification](server_side_catalog_specification.html)

## Base Catalog Elements

* [`catalog`](client_side_catalog_specification.html#catalog-element)
* [`service`](client_side_catalog_specification.html#service-element)
* [`dataset type`](client_side_catalog_specification.html#dataset-type-element)
* [`access`](client_side_catalog_specification.html#access-element)
* [`XLink`](client_side_catalog_specification.html#xlink-attribute-group) Attribute Group


### `catalog` Element

~~~xml
<xsd:element name="catalog"> <!-- 1 -->
  <xsd:complexType>
    <xsd:sequence>
      <xsd:element ref="service" minOccurs="0" maxOccurs="unbounded"/>   <!-- 2 -->
      <xsd:element ref="property" minOccurs="0" maxOccurs="unbounded" /> <!-- 2 -->
      <xsd:element ref="dataset" minOccurs="1" maxOccurs="unbounded" />  <!-- 2 -->
    </xsd:sequence>

    <xsd:attribute name="base" type="xsd:anyURI"/>  <!-- 3 -->
    <xsd:attribute name="name" type="xsd:string" /> <!-- 4 -->
    <xsd:attribute name="expires" type="dateType"/>  <!-- 5 -->
    <xsd:attribute name="version" type="xsd:token" default="1.2" />  <!-- 6 -->
  </xsd:complexType>
</xsd:element>
~~~

1. The `catalog` element is the top-level element. 
2. It may contain zero or more [`service`](client_side_catalog_specification.html#service-element) elements, followed by zero or more [`property`](client_side_catalog_specification.html#property-element) elements, followed by one or more [`dataset type`](client_side_catalog_specification.html#dataset-element) elements. 
3. The `base` is used to resolve any relative URLs in the catalog such as `catalogRefs`, `services`, etc. 
It is usually the URL of the catalog document itself. 
4. Optionally, the catalog may have a display `name`. 
5. The option `expires` attribute indicates when this catalog should be re-read. 
If not present, assume you must re-read each time. 
6. The value of the `version` attribute indicates the version of the `InvCatalog` specification to which the catalog conforms.


#### Example

Here is an example of very simple, useful catalog:

~~~xml
<catalog xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0">  <!-- 1 -->
  <service name="aggServer" serviceType="DODS"  base="http://acd.ucar.edu/thredds/dodsC/" />
  <dataset name="SAGE III Ozone Loss" urlPath="sage.nc">
    <serviceName>aggServer</serviceName>
  </dataset>
</catalog>
~~~

1. Note the necessary presence of the [`xml namespace`](http://en.wikipedia.org/wiki/XML_namespace){:target="_blank"} attribute `xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"` on the `catalog` element.

### `service` Element

~~~xml
<xsd:element name="service">  <!-- 1 -->
 <xsd:complexType>
  <xsd:sequence>
    <xsd:element ref="property" minOccurs="0" maxOccurs="unbounded" />  <!-- 2 -->
    <xsd:element ref="service" minOccurs="0" maxOccurs="unbounded" />  <!-- 3 -->
  </xsd:sequence>

  <xsd:attribute name="name" type="xsd:string" use="required" />  <!-- 4 -->
  <xsd:attribute name="base" type="xsd:string" use="required" />  <!-- 5 -->
  <xsd:attribute name="serviceType" type="serviceTypes" use="required" />  <!-- 6 -->
  <xsd:attribute name="desc" type="xsd:string"/>   <!-- 7 -->
  <xsd:attribute name="suffix" type="xsd:string" />  <!-- 5 -->
 </xsd:complexType>
</xsd:element>
~~~

1. A `service` element represents a data access service and allows basic data access information to be factored out of `dataset` and `access` elements.
2. A `service` element may contain `0` or more [`property`]((client_side_catalog_specification.html#property_element)) elements to allow for the encoding of additional, service-specific information.
3. Only `service` element with `serviceType="Compound"` may have nested `service` elements. 
Compound services are used when there is more than one way to access a dataset (e.g., `OpenDAP` and `FTP`), **and** the access URLs are the same except for the service base. 
Nested `service` elements may also be used directly by `dataset` or `access` elements, and so must have unique names.
4. The `name` attribute is required and its value must be unique for all `service` elements within the catalog. 
These unique names are used in the definition of a [dataset access method](client_side_catalog_specification.html#dataset_access_methods) to refer to a specific `service` element. 
5. The mandatory `base` attribute and the optional `suffix` attribute are both used in the construction of the dataset URL (see [constructing URLS](client_side_catalog_specification.html#constructing_urls)). 
The `base` may be an absolute URL or it may be relative to the catalog's base URL. 
6. The `service` element has a `serviceType` attribute whose value is typically one of the [`serviceType`](client_side_catalog_specification.html#service_type) values. 
7. The optional `desc` attribute allows you to give a human-readable description of the `service`.

#### Examples

Simple examples of where the `base` is an absolute URL and a relative to catalog URL:

~~~xml
<service name="mcidasServer" serviceType="ADDE" 
         base="http://thredds.ucar.edu/thredds/adde/" />  <!-- absolute URL -->

<service name="this" serviceType="OPENDAP" base="/thredds/dodsC/" />  <!-- relative URL -->
~~~

{% include note.html content="
See the [constructing URLS](client_side_catalog_specification.html#constructing_urls) section of this document for more information on how the *resolved URL* is created.
"%}

### `dataset type` Element

~~~xml
<xsd:element name="dataset" type="DatasetType" />  <!-- 1 -->
<xsd:complexType name="DatasetType">
  <xsd:sequence>
    <xsd:group ref="threddsMetadataGroup" minOccurs="0" maxOccurs="unbounded" />  <!-- 2 -->
    <xsd:element ref="access" minOccurs="0" maxOccurs="unbounded"/>   <!-- 3 -->
    <xsd:element ref="dataset" minOccurs="0" maxOccurs="unbounded"/>  <!-- 4 -->
  </xsd:sequence>

  <xsd:attribute name="name" type="xsd:string" use="required"/>  <!-- 5 -->
  <xsd:attribute name="alias" type="xsd:token"/>                 <!-- 6 -->
  <xsd:attribute name="authority" type="xsd:string"/>            <!-- 7 -->
  <xsd:attribute name="collectionType" type="collectionTypes"/>  <!-- deprecated -->
  <xsd:attribute name="dataType" type="dataTypes"/>              <!-- 8 -->
  <xsd:attribute name="harvest" type="xsd:boolean"/>             <!-- 9 -->
  <xsd:attribute name="ID" type="xsd:token"/>                    <!-- 10  -->
  <xsd:attribute name="restrictAccess" type="xsd:string"/>  

  <xsd:attribute name="serviceName" type="xsd:string" />         <!-- 11 -->
  <xsd:attribute name="urlPath" type="xsd:token" />              <!-- 12 -->
</xsd:complexType>
~~~

1. A `dataset` element represents a named, logical set of data at a level of granularity appropriate for presentation to a user. 
A `dataset` is a [`directDataset`](client_side_catalog_specification.html#directDataset) if it contains at least one [dataset access method](client_side_catalog_specification.html#dataset_access_methods), otherwise it is a container for nested datasets, called a [`collectionDataset`](client_side_catalog_specification.html#collectionDataset).
2. A `dataset` element contains any number of elements from the [`threddsMetadataGroup`](client_side_catalog_specification.html#threddsMetadataGroup) in any order. 
3. These are followed by `0` or more [`access`](client_side_catalog_specification.html#access_element) elements, followed by `0` or more nested `dataset` elements (actually you can use any element in the dataset substitution group: `dataset` or `catalogRef`). 
4. The data represented by a nested `dataset` element should be a subset, a specialization or in some other sense "contained" within the data represented by its parent `dataset` element.
5. A `dataset` must have a `name` attribute, and may have other attributes. 
The `name` of the dataset should be a human readable name that will be displayed to users.
6. If you want the same dataset to appear in multiple places in the same catalog, use an `alias` attribute. 
Define it in one place (with all appropriate metadata), then wherever else it should appear, make a dataset with an alias to it, whose value is the `ID` of the defined dataset. 
(Note it may not refer to a dataset in another catalog referred to by a `catalogRef` element.) 
In this case, any other properties of the dataset are ignored, and the dataset to which the `alias` refers is used in its place.
7. A dataset may have a naming `authority` specified within itself or in a parent dataset. 
   (You may also use an `authority` element rather than an attribute.) 
8. As of 5.0, it is optional as long as you specify the `dataType` or `featureType` of the dataset. 
9. If the `harvest` attribute is `true`, then this dataset is available to be placed into digital libraries or other discovery services. 
Note that the `harvest` attribute should be carefully placed to get the right level of granularity for digital library entries, and is typically placed on collection datasets.
10. If an `ID` attribute is given, its value must be unique within the catalog. 
We highly recommend that all datasets be given a unique ID. 
This allows for a number of capabilities including `XPath ID` reference. 
If a `dataset` has an `ID` and an `authority` attribute, then the combination of the two should be globally unique for all time. 
If the same `dataset` is specified in multiple catalogs, then the combination of its `authority` and `ID` should be identical if possible.
11. The `dataset` element's `serviceName` (which can also be specified as a `serviceName` element) specifies which `service` to use for this dataset. 
12. The `urlPath` attribute, in combination with the applicable service, is used to specify data access methods. 
When you have more than one way to access a dataset, either explicitly define them using more than one nested [`access`]((client_side_catalog_specification.html#access_element)) elements, or use a [`compoundService`](client_side_catalog_specification.html#compoundService).

#### Examples

~~~xml
<dataset name="DC8 flight 1999-11-19" urlPath="SOLVE_DC8_19991119.nc">
  <serviceName>agg</serviceName>
</dataset>

<dataset ID="SOLVE_DC8_19991119" name="DC8 flight 1999-11-19, 1 min merge">
  <metadata xlink:href="http://dataportal.ucar.edu/metadata/tracep_dc8_1min_05"/>
  <access serviceName="disk" urlPath="SOLVE_DC8_19991119.nc"/>
</dataset>
~~~

An example using an `alias`. 
In this case the `dataset` referred to *logically* replaces the `alias` dataset.

~~~xml
<dataset name="Station Data">
  <dataset name="Metar data" urlPath="cgi-bin/MetarServer.pl?format=qc" />
  <dataset name="Level 3 Radar data" urlPath="cgi-bin/RadarServer.pl?format=qc" />
  <dataset name="Alias to SOLVE dataset" alias="SOLVE_DC8_19991119"/>
</dataset>
~~~

### `access` Element

~~~xml
<xsd:element name="access">  <!-- 1 -->
  <xsd:complexType>
    <xsd:sequence>
      <xsd:element ref="dataSize" minOccurs="0"/>   <!-- 2 -->
    </xsd:sequence>
    <xsd:attribute name="urlPath" type="xsd:token" use="required"/>   <!-- 3 -->
    <xsd:attribute name="serviceName" type="xsd:string"/>             <!-- 4 -->
    <xsd:attribute name="dataFormat" type="dataFormatTypes"/>         <!-- 5 -->
  </xsd:complexType>
</xsd:element >
~~~

1. An `access` element specifies how a dataset can be accessed through a data [`service`]((client_side_catalog_specification.html#service_element)) element. 
It always refers to the dataset that it is immediately contained within.
2. An `access` element may contain an optional `dataSize` element to specify how large the dataset would be if it were to be copied to the client.
3. The `urlPath` is appended to the service's base to get the dataset URL (see [constructing URLS](client_side_catalog_specification.html#constructing_urls)). 
4. The `serviceName` refers to the unique name of a `service` element. 
5. The [`dataFormat`](client_side_catalog_specification.html#dataFormat) is important when the [`serviceType`](client_side_catalog_specification.html#serviceType) is a bulk transport like `FTP` or `HTTP`, as it specifies the format of the transferred file. 
It is not needed for client/server protocols like `OpenDAP` or `ADDE`.

#### Example

~~~xml
<catalogRef xlink:title="NCEP Model Data" xlink:href="http://yerserv/uniModels.xml"/>
~~~

### `XLink` Attribute Group

~~~xml
<xsd:attributeGroup name="XLink">     <!-- 1 -->
  <xsd:attribute ref="xlink:href" />  <!-- 2 -->
  <xsd:attribute ref="xlink:title" /> <!-- 3 -->
  <xsd:attribute ref="xlink:show"/>   <!-- 4 -->
  <xsd:attribute ref="xlink:type" />  <!-- 4 -->
</xsd:attributeGroup>
~~~

1. These are attributes from the `XLink` specification that are used to point to another web resource. 
2. The `xlink:href` attribute is used for the URL of the resource itself. 
3. The `xlink:title` attribute is a human-readable description of the linked resource. 
THREDDS clients can display the title to the user as appropriate. 
These are the only two attributes currently used in the THREDDS software.
4. You can also add the `xlink:type` or `xlink:show` attributes.

#### Example

~~~xml
<documentation xlink:href="http://cloud1.arc.nasa.gov/solve/" xlink:title="SOLVE home page"/>
~~~

## THREDDS Metadata Elements

*THREDDS metadata elements* are catalog elements that are used in Digital Libraries entries, discovery centers, and for annotation and documentation of datasets.

### `threddsMetadataGroup` Element Group

~~~xml
<xsd:group name="threddsMetadataGroup">   <!-- 1 -->
  <xsd:choice minOccurs="0" maxOccurs="unbounded">
    <xsd:element name="documentation" type="documentationType"/>   <!-- 2 -->
    <xsd:element ref="metadata"  />   <!-- 3 -->                             
    <xsd:element ref="property"  />   <!-- 4 -->

    <!-- The next group of elements are used primarily for use in Digital Libraries.  -->
    <xsd:element ref="contributor"/>    <!-- 5 -->
    <xsd:element name="creator" type="sourceType"/>             <!-- 6 -->
    <xsd:element name="date" type="dateTypeFormatted"/>         <!-- 7 -->
    <xsd:element name="keyword" type="controlledVocabulary" />  <!-- 8 -->
    <xsd:element name="project" type="controlledVocabulary" />  <!-- 9 -->
    <xsd:element name="publisher" type="sourceType"/>           <!-- 10 -->

    <!-- The next group of elements are used in search services.  -->
    <xsd:element ref="geospatialCoverage"/>    <!-- 11 -->
    <xsd:element name="timeCoverage" type="timeCoverageType"/>   <!-- 12 -->
    <xsd:element ref="variables"/>     <!-- 13 -->

    <xsd:element name="dataType" type="dataTypes"/>            <!-- 14 -->
    <xsd:element name="dataFormat" type="dataFormatTypes"/>    <!-- 15 -->
    <xsd:element name="serviceName" type="xsd:string" />       <!-- 16 -->
    <xsd:element name="authority" type="xsd:string" />         <!-- 17 -->
    <xsd:element ref="dataSize"/>                              <!-- 18 -->
  </xsd:choice>
</xsd:group>
~~~

1. The elements in the `threddsMetadataGroup` may be used as nested elements of both [`dataset`](client_side_catalog_specification.html#dataset-element) and [`metadata`](client_side_catalog_specification.html#metadata-element) elements. 
There may be any number of them in any order, but more than one `geospatialCoverage`, `timeCoverage`, `dataType`, `dataFormat`, `serviceName`, or `authority` elements will be ignored.
2. A [`documentation`](client_side_catalog_specification.html#documentation-element) element contains (or points to) human-readable content. 
Documentation content may be displayed to users by THREDDS clients as appropriate for the situation. 
3. A [`metadata`](client_side_catalog_specification.html#metadata-element) element is a container for machine-readable information structured in XML. 
4. A [`property`](client_side_catalog_specification.html#property-element) element is an arbitrary name/value pair.
5. A [`contributor`](client_side_catalog_specification.html#contributor-element) element is typically a person's name with an optional role attribute, documenting some person's contribution to the dataset. 
6. A [`creator`](client_side_catalog_specification.html#creator-element) element indicates who created the dataset. 
7. A [`date`](client_side_catalog_specification.html#date-element) element is used to document various dates associated with the dataset, using one of the date type enumerations. 
8. A [`keyword`](client_side_catalog_specification.html#keyword-element) element is used for library searches, while a project element specifies what scientific project the dataset belongs to. 
9. Both the [`project`](client_side_catalog_specification.html#project-element) and `keyword` elements have type `controlledVocabulary`, which allows an optional vocabulary attribute to specify if you are using words from a restricted list, for example DIF. 
10. A [`publisher`](client_side_catalog_specification.html#publisher-element) element indicates who is responsible for serving the dataset. 
Both a `contibutor` and `publisher` element use the `sourceType` definition.
11. The [`geospatialCoverage`](client_side_catalog_specification.html#geospatialcoverage-element) element specifies a `lat/lon` bounding box for the data. 
12. The [`timeCoverage`](client_side_catalog_specification.html#timecoverge-element) element specifies the range of dates that the dataset covers. 
13. The [`variables`](client_side_catalog_specification.html#variables-element) element specifies the names of variables contained in the datasets, and ways to map the names to standard vocabularies.
14. The [`dataType`](client_side_catalog_specification.html#datatype-element) element is used to indicate the high-level semantic type of the dataset (e.g., `grid`, `point`, `trajectory`) and can be used by clients to decide how to display the data. 
The values come from the [`dataType`](client_side_catalog_specification.html#datatype-enumeration) enumeration which are intended to map to the scientific data types from the [Common Data Model (CDM)](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/common_data_model_overview.html){:target='_blank'}. 
15. The [`dataFormat`](client_side_catalog_specification.html#dataformat-element) element indicates the format of the data and is mainly used so clients can determine how to read data that is accessed using a bulk access method. 
The data format values come from the [dataFormat](client_side_catalog_specification.html#dataformat-enumeration) enumeration. 
16. The [`serviceName`](client_side_catalog_specification.html#servicename-element) element is a reference to a service element; its content must match the name of a `service` element in the catalog. 
The service referenced by a dataset is used in the [construction of access method URLs](client_side_catalog_specification.html#constructing_urls) for that dataset. 
(This element and the `serviceName` attribute of an `access` element are both used in the same way.) 
17. The [`authority`](client_side_catalog_specification.html#authority-element) element is used to further refine dataset `ID`s with the goal of allowing for globally unique `ID`s. 
18. The [`dataSize`](client_side_catalog_specification.html#datasize-element) element can be used to specify how large the dataset would be if it were to be copied to a client.

Including any of these elements in a `metadata` element with its inherit attribute set to `true` means that they apply to the containing dataset and any nested datasets.

If your intention is to enable THREDDS to write entries into a Digital Library, you should to be aware of how [elements are mapped to Digital Libraries](digital_libraries.html). 
For example, you will probably want to add a `documentation` element with type summary as its content will be the description of the dataset in the Digital Library entry. 
Another `documentation` element you may need has type rights which specifies what restrictions there are on the dataset usage.

#### Examples

~~~xml
<documentation type="summary"> The SAGE III Ozone Loss and Validation Experiment (SOLVE)
 was a measurement campaign designed to examine the processes controlling ozone levels
 at mid- to high latitudes. Measurements were made in the Arctic high-latitude
 region in winter using the NASA DC-8 and ER-2 aircraft,
 as well as balloon platforms and ground-based instruments. </documentation>
~~~

~~~xml
<documentation type="rights"> Users of these data files are expected  to follow the NASA
  ESPO Archive guidelines for use of the SOLVE data, including consulting with the PIs
  of the individual measurements  for interpretation and credit.
</documentation>

<keyword>Ocean Biomass</keyword>

<project vocabulary="DIF">NASA Earth Science Project Office, Ames Research Center</project>
~~~

### `documentation type` Element

~~~xml
<xsd:complexType name="documentationType" mixed="true">
  <xsd:sequence>
    <xsd:any namespace="http://www.w3.org/1999/xhtml" minOccurs="0" maxOccurs="unbounded"
         processContents="strict"/>
  </xsd:sequence>

  <xsd:attribute name="type" type="documentationEnumTypes"/>
  <xsd:attributeGroup ref="XLink" />
</xsd:complexType>
~~~

