---
title: Client-Side Catalog Specification
last_updated: 2020-08-25
sidebar: user_sidebar
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


### `catalog` Element

~~~xml
<xsd:element name="catalog"> 
  <xsd:complexType>
    <xsd:sequence>
      <xsd:element ref="service" minOccurs="0" maxOccurs="unbounded"/>   
      <xsd:element ref="property" minOccurs="0" maxOccurs="unbounded" /> 
      <xsd:element ref="dataset" minOccurs="1" maxOccurs="unbounded" />  
    </xsd:sequence>

    <xsd:attribute name="base" type="xsd:anyURI"/>  
    <xsd:attribute name="name" type="xsd:string" /> 
    <xsd:attribute name="expires" type="dateType"/>  
    <xsd:attribute name="version" type="xsd:token" default="1.2" />  
  </xsd:complexType>
</xsd:element>
~~~

The `catalog` element is the top-level element.
It may contain zero or more [`service`](#service-element) elements, followed by zero or more [`property`](#property-element) elements, followed by one or more [`dataset type`](#dataset-type) elements. 
The `base` is used to resolve any relative URLs in the catalog such as `catalogRefs`, `services`, etc. 
It is usually the URL of the catalog document itself. 
Optionally, the catalog may have a display `name`. 
The option `expires` attribute indicates when this catalog should be re-read. 
If not present, assume you must re-read each time. 
The value of the `version` attribute indicates the version of the `InvCatalog` specification to which the catalog conforms.


#### Example

Here is an example of very simple, useful catalog:

~~~xml
<catalog xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0">  
  <service name="aggServer" serviceType="DODS"  base="http://acd.ucar.edu/thredds/dodsC/" />
  <dataset name="SAGE III Ozone Loss" urlPath="sage.nc">
    <serviceName>aggServer</serviceName>
  </dataset>
</catalog>
~~~

Note the necessary presence of the [`xml namespace`](http://en.wikipedia.org/wiki/XML_namespace){:target="_blank"} attribute `xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"` on the `catalog` element.

### `service` Element

~~~xml
<xsd:element name="service">  
 <xsd:complexType>
  <xsd:sequence>
    <xsd:element ref="property" minOccurs="0" maxOccurs="unbounded" />  
    <xsd:element ref="service" minOccurs="0" maxOccurs="unbounded" />  
  </xsd:sequence>

  <xsd:attribute name="name" type="xsd:string" use="required" />  
  <xsd:attribute name="base" type="xsd:string" use="required" />  
  <xsd:attribute name="serviceType" type="serviceTypes" use="required" />  
  <xsd:attribute name="desc" type="xsd:string"/>   
  <xsd:attribute name="suffix" type="xsd:string" />  
 </xsd:complexType>
</xsd:element>
~~~

A `service` element represents a data access service and allows basic data access information to be factored out of `dataset` and `access` elements.

The `name` attribute is required and its value must be unique for all `service` elements within the catalog. 
These unique names are used in the definition of a [dataset access method](#dataset-access-methods) to refer to a specific `service` element. 
The mandatory `base` attribute and the optional `suffix` attribute are both used in the construction of the dataset URL (see [constructing URLS](#constructing-urls)). 
The `base` may be an absolute URL or it may be relative to the catalog's base URL. 
The `service` element has a `serviceType` attribute whose value is typically one of the [`serviceType`](#service-element) values. 
The optional `desc` attribute allows you to give a human-readable description of the `service`.

A `service` element may contain `0` or more [`property`](#property-element) elements to allow for the encoding of additional, service-specific information.

Only `service` element with `serviceType="Compound"` may have nested `service` elements. 
Compound services are used when there is more than one way to access a dataset (e.g., `OpenDAP` and `FTP`), **and** the access URLs are the same except for the service base. 
Nested `service` elements may also be used directly by `dataset` or `access` elements, and so must have unique names.

#### Examples

Simple examples of where the `base` is an absolute URL and a relative to catalog URL:

~~~xml
<service name="mcidasServer" serviceType="ADDE" 
         base="http://thredds.ucar.edu/thredds/adde/" />  <!-- absolute URL -->

<service name="this" serviceType="OPENDAP" base="/thredds/dodsC/" />  <!-- relative URL -->
~~~

{% include note.html content="
See the [constructing URLS](#constructing-urls) section of this document for more information on how the *resolved URL* is created.
"%}

### `dataset` Type

~~~xml
<xsd:element name="dataset" type="DatasetType" />  
<xsd:complexType name="DatasetType">
  <xsd:sequence>
    <xsd:group ref="threddsMetadataGroup" minOccurs="0" maxOccurs="unbounded" />  
    <xsd:element ref="access" minOccurs="0" maxOccurs="unbounded"/>   
    <xsd:element ref="dataset" minOccurs="0" maxOccurs="unbounded"/>  
  </xsd:sequence>

  <xsd:attribute name="name" type="xsd:string" use="required"/>  
  <xsd:attribute name="alias" type="xsd:token"/>                 
  <xsd:attribute name="authority" type="xsd:string"/>            
  <xsd:attribute name="collectionType" type="collectionTypes"/>  <!-- deprecated -->
  <xsd:attribute name="dataType" type="dataTypes"/>              
  <xsd:attribute name="harvest" type="xsd:boolean"/>             
  <xsd:attribute name="ID" type="xsd:token"/>                    
  <xsd:attribute name="restrictAccess" type="xsd:string"/>  

  <xsd:attribute name="serviceName" type="xsd:string" />         
  <xsd:attribute name="urlPath" type="xsd:token" />              
</xsd:complexType>
~~~

A `dataset` element represents a named, logical set of data at a level of granularity appropriate for presentation to a user. 
A `dataset` is a [`directdataset`](#direct-datasets) if it contains at least one [dataset access method](#dataset-access-methods), otherwise it is a container for nested datasets, called a [`collectionDataset`](#collection-datasets).

A `dataset` must have a `name` attribute, and may have other attributes. 
The `name` of the dataset should be a human readable name that will be displayed to users.

If an `ID` attribute is given, its value must be unique within the catalog. 
We highly recommend that all datasets be given a unique ID. 
This allows for a number of capabilities including `XPath ID` reference. 
A dataset may have a naming `authority` specified within itself or in a parent dataset. 
(You may also use an `authority` element rather than an attribute.) 
If a `dataset` has an `ID` and an `authority` attribute, then the combination of the two should be globally unique for all time. 
If the same `dataset` is specified in multiple catalogs, then the combination of its `authority` and `ID` should be identical if possible.

A `dataset` element contains any number of elements from the [`threddsMetadataGroup`](#threddsmetadatagroup-element-group) in any order. 
These are followed by `0` or more [`access`](#access-element) elements, followed by `0` or more nested `dataset` elements (actually you can use any element in the dataset substitution group: `dataset` or `catalogRef`). 
The data represented by a nested `dataset` element should be a subset, a specialization or in some other sense "contained" within the data represented by its parent `dataset` element.

If the `harvest` attribute is `true`, then this dataset is available to be placed into digital libraries or other discovery services. 
Note that the `harvest` attribute should be carefully placed to get the right level of granularity for digital library entries, and is typically placed on collection datasets.

If you want the same dataset to appear in multiple places in the same catalog, use an `alias` attribute. 
Define it in one place (with all appropriate metadata), then wherever else it should appear, make a dataset with an alias to it, whose value is the `ID` of the defined dataset. 
(Note, it may not refer to a dataset in another catalog referred to by a `catalogRef` element.) 
In this case, any other properties of the dataset are ignored, and the dataset to which the `alias` refers is used in its place.

The `dataset` element's `serviceName` (which can also be specified as a `serviceName` element) specifies which `service` to use for this dataset. 
As of 5.0, it is optional as long as you specify the `dataType` or `featureType` of the dataset. 
The `urlPath` attribute, in combination with the applicable service, is used to specify data access methods. 
When you have more than one way to access a dataset, either explicitly define them using more than one nested [`access`](#access-element) elements, or use a [`compoundService`](#access-methods-and-compound-services).

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

An example using an `alias` attribute. 
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
<xsd:element name="access">  
  <xsd:complexType>
    <xsd:sequence>
      <xsd:element ref="dataSize" minOccurs="0"/>   
    </xsd:sequence>
    <xsd:attribute name="urlPath" type="xsd:token" use="required"/>   
    <xsd:attribute name="serviceName" type="xsd:string"/>             
    <xsd:attribute name="dataFormat" type="dataFormatTypes"/>         
  </xsd:complexType>
</xsd:element >
~~~

An `access` element specifies how a dataset can be accessed through a data [`service`](#service-element) element. 
It always refers to the dataset that it is immediately contained within.

The `urlPath` is appended to the service's base to get the dataset URL (see [constructing URLS](#constructing-urls)). 
The `serviceName` refers to the unique name of a `service` element. 
The [`dataFormat`](#dataformat-enumeration) is important when the [`serviceType`](#servicetypes-enumeration) is a bulk-transport protocol, like `FTP` or `HTTP`, as it specifies the format of the transferred file. 
It is not needed for client/server protocols like `OpenDAP` or `ADDE`.

An `access` element may contain an optional `dataSize` element to specify how large the dataset would be if it were to be copied to the client.

#### Example

~~~xml
<access serviceName="ftpServer" urlPath="SOLVE_DC8_19991119.nc" dataFormat="NetCDF" />
~~~


### `catalogRef` Element 

~~~xml
<xsd:element name="catalogRef" substitutionGroup="dataset">
  <xsd:complexType>
    <xsd:complexContent>
      <xsd:extension base="DatasetType">
        <xsd:attributeGroup ref="XLink"/>
      </xsd:extension>
    </xsd:complexContent>
  </xsd:complexType>
</xsd:element>
~~~

A `catalogRef` element refers to another THREDDS catalog that logically is a nested dataset inside this parent catalog. 
This is used to separately maintain catalogs and to break up large catalogs. 
THREDDS clients should not read the referenced catalog until the user explicitly requests it, so that very large dataset collections can be represented with `catalogRef` elements without large delays in presenting them to the user. 
The referenced catalog is not textually substituted into the containing catalog, but remains a self-contained object. 
The referenced catalog must be a valid THREDDS catalog, but it does not have to match versions with the containing catalog.

The [`XLink` `attributeGroup`](#xlink-attribute-group) allows you to add `Xlink` attributes, a generalization of HTTP `hrefs`. 
The value of `xlink:href` is the URL of the referenced catalog.
It may be absolute or relative to the parent catalog URL. 
The value of `xlink:title` is displayed as the name of the dataset that the user can click on to follow the `XLink`.

A `catalogRef` element is in the dataset `substitutionGroup`, so it can be used wherever a `dataset` element can be used. 
It is an extension of a `DatasetType`, so any of dataset's nested elements and attributes can be used in it. 
This allows you to add enhanced metadata to a `catalogRef`. 
However, you should not add nested datasets, as these will be ignored. 
Furthermore, metadata elements are NOT copied to the referenced catalog, so they are used only to display information to the user before the user downloads the referenced catalog.

#### Example

~~~xml
<catalogRef xlink:title="NCEP Model Data" xlink:href="http://yerserv/uniModels.xml"/>
~~~

### `XLink` Attribute Group

~~~xml
<xsd:attributeGroup name="XLink">     
  <xsd:attribute ref="xlink:href" />  
  <xsd:attribute ref="xlink:title" /> 
  <xsd:attribute ref="xlink:show"/>   
  <xsd:attribute ref="xlink:type" />  
</xsd:attributeGroup>
~~~

These are attributes from the `XLink` specification that are used to point to another web resource. 
The `xlink:href` attribute is used for the URL of the resource itself. 
The `xlink:title` attribute is a human-readable description of the linked resource. 
THREDDS clients can display the title to the user as appropriate. 
These are the only two attributes currently used in the THREDDS software.
You can also add the `xlink:type` or `xlink:show` attributes.

#### Example

~~~xml
<documentation xlink:href="http://cloud1.arc.nasa.gov/solve/" xlink:title="SOLVE home page"/>
~~~



## THREDDS Metadata Elements

*THREDDS metadata elements* are catalog elements that are used in Digital Libraries entries, discovery centers, and for annotation and documentation of datasets.

### `threddsMetadataGroup` Element Group

~~~xml
<xsd:group name="threddsMetadataGroup">   
  <xsd:choice minOccurs="0" maxOccurs="unbounded">
    <xsd:element name="documentation" type="documentationType"/>   
    <xsd:element ref="metadata"  />                                
    <xsd:element ref="property"  />   

    <!-- The next group of elements are used primarily for use in Digital Libraries.  -->
    <xsd:element ref="contributor"/>    
    <xsd:element name="creator" type="sourceType"/>             
    <xsd:element name="date" type="dateTypeFormatted"/>         
    <xsd:element name="keyword" type="controlledVocabulary" />  
    <xsd:element name="project" type="controlledVocabulary" />  
    <xsd:element name="publisher" type="sourceType"/>           

    <!-- The next group of elements are used in search services.  -->
    <xsd:element ref="geospatialCoverage"/>    
    <xsd:element name="timeCoverage" type="timeCoverageType"/>   
    <xsd:element ref="variables"/>    

    <xsd:element name="dataType" type="dataTypes"/>            
    <xsd:element name="dataFormat" type="dataFormatTypes"/>    
    <xsd:element name="serviceName" type="xsd:string" />       
    <xsd:element name="authority" type="xsd:string" />         
    <xsd:element ref="dataSize"/>                              
  </xsd:choice>
</xsd:group>
~~~

The elements in the `threddsMetadataGroup` may be used as nested elements of both [`dataset`](#dataset-type) and [`metadata`](#metadata-element) elements. 
There may be any number of them in any order, but more than one `geospatialCoverage`, `timeCoverage`, `dataType`, `dataFormat`, `serviceName`, or `authority` elements will be ignored.
A [`documentation`](#documentation-element) element contains (or points to) human-readable content. 
Documentation content may be displayed to users by THREDDS clients as appropriate for the situation. 
A [`metadata`](#metadata-element) element is a container for machine-readable information structured in XML. 
A [`property`](#property-element) element is an arbitrary name/value pair.

The next group of elements are used primarily for use in Digital Libraries.

A [`contributor`](#contributor-element) element is typically a person's name with an optional role attribute, documenting some person's contribution to the dataset. 
A `creator` element indicates who created the dataset. 
A [`date`](#date-type) element is used to document various dates associated with the dataset, using one of the date type enumerations. 
A `keyword` element is used for library searches, while a project element specifies what scientific project the dataset belongs to. 
Both the `project` and `keyword` elements have type `controlledVocabulary`, which allows an optional vocabulary attribute to specify if you are using words from a restricted list, for example DIF. 
A `publisher` element indicates who is responsible for serving the dataset. 
Both a `contibutor` and `publisher` element use the `sourceType` definition.

The next group of elements are used in search services.

The [`geospatialCoverage`](#geospatialcoverage-element) element specifies a lat/lon bounding box for the data. 
The [`timeCoverage`](#timecoverage-element) element specifies the range of dates that the dataset covers. 
The [`variables`](#variables-element) element specifies the names of variables contained in the datasets, and ways to map the names to standard vocabularies.
The [`dataType`](#datatype-enumeration) element is used to indicate the high-level semantic type of the dataset (e.g., `grid`, `point`, `trajectory`) and can be used by clients to decide how to display the data. 
The values come from the [`dataType`](#datatype-enumeration) enumeration which are intended to map to the scientific data types from the [Common Data Model (CDM)](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/common_data_model_overview.html){:target='_blank'}. 
The [`dataFormat`](#dataformat-enumeration) element indicates the format of the data and is mainly used so clients can determine how to read data that is accessed using a bulk access method. 
The data format values come from the [dataFormat](#dataformat-enumeration) enumeration. 
The `serviceName` element is a reference to a service element; its content must match the name of a `service` element in the catalog. 
The service referenced by a dataset is used in the [construction of access method URLs](#constructing-urls) for that dataset. 
(This element and the `serviceName` attribute of an `access` element are both used in the same way.) 
The `authority` element is used to further refine dataset `ID`s with the goal of allowing for globally unique `ID`s. 
The [`dataSize`](#datasize-element) element can be used to specify how large the dataset would be if it were to be copied to a client.

Including any of these elements in a `metadata` element with its inherit attribute set to `true` means that they apply to the containing dataset and any nested datasets.

### `documentation` Element

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



The `documentation` element may contain arbitrary plain text content, or XHTML.
We call this kind of content "human readable" information. 
It has an optional `documentation type` attribute, such as `summary`, `funding`, `history`, etc.

The `documentation` element may also contain an `XLink` to an HTML or plain text web page. 
This allows you to point to external web references, and allows you to factor out common documentation which can be referenced from multiple places. 
Note, it should not link to an XML page (unless its XHTML); instead use the `metadata` element in these instances. 

#### Examples

~~~xml
<documentation xlink:href="http://espoarchive.nasa.gov/archive/index.html"
    xlink:title="Earth Science Project Office Archives"/>

<documentation>Used in doubled CO2 scenario</documentation>
~~~


### `documentation` Type

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


### `metadata` Element

~~~xml
<xsd:element name="metadata">
  <xsd:complexType>
    <xsd:choice>
      <xsd:group ref="threddsMetadataGroup" minOccurs="0" maxOccurs="unbounded" />
      <xsd:any namespace="##other" minOccurs="0" maxOccurs="unbounded" processContents="strict"/>
    </xsd:choice>

    <xsd:attribute name="inherited" type="xsd:boolean" default="false" />
    <xsd:attribute name="metadataType" type="metadataTypeEnum"  />
    <xsd:attributeGroup ref="XLink" />
  </xsd:complexType>
</xsd:element>
~~~

A `metadata` element contains or refers to structured information (in XML) about datasets, which is used by client programs to display, describe, or search for the dataset.  
We call this kind of content "machine readable" information.
   
A `metadata` element contains any number of elements from the `threddsMetadataGroup` in any order, OR it contains any other well-formed XML elements, as long as they are in a `namespace` other than the THREDDS `namespace`. 
It may also contain an `XLink` to another XML document, whose top-level element should be a valid `metadata` element (see example below).
Note, it should not link to an HTML page, use the `documentation` element instead.
   
The `inherited` attribute indicates whether the metadata is inherited by nested datasets. 
If `true`, the `metadata` element becomes logically part of each nested dataset. 
(The metadata always applies to the containing dataset whether inherited is `true` or not.)
   
The `metadataType` attribute may have any value, but the "well known" values are listed in the `metadataType` enumeration. 
To use `metadata` elements from the `threddsMetadataGroup`, do **not** include the `metadata type` attribute (or set it to "THREDDS"). 
To use your own elements, give it a `metadata type`, and add a `namespace` declaration (see example below).

#### Examples

~~~xml
<!-- contains THREDDS metadata -->
<metadata inherited="true">
  <contributor role="data manager">John Smith</contributor>
  <keyword>Atmospheric Science</keyword>
  <keyword>Aircraft Measurements</keyword>
  <keyword>Upper Tropospheric Chemistry</keyword>
</metadata>


<!-- link to external file containing THREDDS metadata -->
<metadata xlink:href="http://dataportal.ucar.edu/metadata/solveMetadata.xml"
   xlink:title="Solve metadata" />
~~~

If you use an `XLink`, it should point to a document whose top element is a `metadata` element, which declares the THREDDS `namespace`:

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata  xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0">
  <contributor role="Investigator">Mashor Mashnor</contributor>

  <abstract>
   This project aims to determine the physiological adaptations of algae to the
   extreme conditions of Antarctica.
  </abstract>

  <publisher>
     <name vocabulary="DIF">AU/AADC</name>
     <long_name vocabulary="DIF">Australian Antarctic Data Centre, Australia</long_name>
     <contact url="http://www.aad.gov.au/default.asp?casid=3786" email="metadata@aad.gov.au"/>
  </publisher>

</metadata>
~~~

When using elements from another `namespace`, all the sub-elements should be in the same `namespace`, which should be declared in the `metadata` element:

~~~xml
<metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
  <dc:title>Goto considered harmful</dc:title >
  <dc:description>The unbridled use of the go to statement has an immediate consequence
      that it becomes terribly
        hard to find a meaningful set of coordinates in which to describe the process progress.
  </dc:description>
  <dc:author>Edsger W. Dijkstra</dc:author>
</metadata>
~~~

If you use an `XLink` to point to elements from another `namespace`, add a `metadataType` attribute:

~~~xml
<metadata xlink:href="http://www.unidata.ucar.edu/metadata/ncep/dif.xml"
    xlink:title="NCEP DIF metadata" metadataType="DublinCore"/>
~~~

whose `xlink:href` should point to a document whose top element is a `metadata` element, which declares a different `namespace`.
Note, you also still need to declare the THREDDS `namespace`:

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata  xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
           xmlns:dc="http://purl.org/dc/elements/1.1/">
  <dc:title>Goto considered harmful</dc:title >
  <dc:description>The unbridled use of the go to statement has an immediate consequence
      that it becomes terribly
        hard to find a meaningful set of coordinates in which to describe the process progress.
  </dc:description>
  <dc:author>Edsger W. Dijkstra</dc:author>
</metadata>
~~~

This equivalent declaration makes the other `namespace` the default `namespace`:

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<cat:metadata  xmlns:cat="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
               xmlns="http://purl.org/dc/elements/1.1/">
  <title>Goto considered harmful</title >
  <description>The unbridled use of the go to statement has an immediate consequence
      that it becomes terribly
        hard to find a meaningful set of coordinates in which to describe the process progress.
  </description>
  <author>Edsger W. Dijkstra</author>
</cat:metadata>
~~~

### `property` Element

~~~xml
<xsd:element name="property">
  <xsd:complexType>
    <xsd:attribute name="name" type="xsd:string"/>
    <xsd:attribute name="value" type="xsd:string"/>
  </xsd:complexType>
</xsd:element>
~~~

`property` elements are arbitrary name/value pairs to associate with a `catalog`, `dataset` or `service` element. 
Properties on datasets are added as global attributes to the THREDDS data model objects. 
Generally there may be multiple properties having the same name.

#### Example

~~~xml
<property name="Conventions" value="WRF" />
~~~

### `source` Type

~~~xml
<xsd:complexType name="sourceType">
  <xsd:sequence>
    <xsd:element name="name" type="controlledVocabulary"/>  
    <xsd:element name="contact">                       
      <xsd:complexType>
        <xsd:attribute name="email" type="xsd:string" use="required"/>   
        <xsd:attribute name="url" type="xsd:anyURI"/>                    
      </xsd:complexType>
    </xsd:element>
  </xsd:sequence>
</xsd:complexType>
~~~

This is used by the `creator` and `publisher` elements to specify roles of responsibility for the dataset. 
1. The `sourceType` must have a `name` element. 
The `name` element has an optional `vocabulary` attribute if it originates from a controlled vocabulary. 
2. A `contact` element is also required.
3. The `contact` element has attributes to specify a web `url` and an `email` address.

#### Example

Here is an example of the `sourceType` used in a `publisher` element:
~~~xml
<publisher>
  <name vocabulary="DIF">UCAR/NCAR/CDP > Community Data Portal, National Center for Atmospheric
    Research, University Corporation for Atmospheric Research</name>
  <contact url="http://dataportal.ucar.edu" email="cdp@ucar.edu"/>
</publisher>
~~~

### `contributor` Element

~~~xml
<xsd:element name="contributor">    
  <xsd:complexType>
    <xsd:simpleContent>
      <xsd:extension base="xsd:string">
        <xsd:attribute name="role" type="xsd:string" use="required"/>    
      </xsd:extension>
    </xsd:simpleContent>
  </xsd:complexType>
</xsd:element>
~~~

1. A `contributor` element represents a person's name.
2. It has an optional `role` attribute that specifies the role the person plays with regard to this dataset. 
The `role`s can be any string (i.e., they are not from a controlled vocabulary).

#### Example

~~~xml
<contributor role="PI">Jane Doe</contributor>
~~~

### `geospatialCoverage` Element

~~~xml
<xsd:element name="geospatialCoverage">  
  <xsd:complexType>
   <xsd:sequence>
     <xsd:element name="northsouth" type="spatialRange" minOccurs="0" />    
     <xsd:element name="eastwest" type="spatialRange" minOccurs="0" />      
     <xsd:element name="updown" type="spatialRange" minOccurs="0" />        
     <xsd:element name="name" type="controlledVocabulary" 
                  minOccurs="0" maxOccurs="unbounded"/>   
   </xsd:sequence>

   <xsd:attribute name="zpositive" type="upOrDown" default="up"/>     
  </xsd:complexType>
</xsd:element>

<xsd:complexType name="spatialRange">     
  <xsd:sequence>
    <xsd:element name="start" type="xsd:double"  />
    <xsd:element name="size" type="xsd:double" />
    <xsd:element name="resolution" type="xsd:double" minOccurs="0" />   
    <xsd:element name="units" type="xsd:string" minOccurs="0" />
  </xsd:sequence>
</xsd:complexType>

<xsd:simpleType name="upOrDown">
  <xsd:restriction base="xsd:token">
    <xsd:enumeration value="up"/>
    <xsd:enumeration value="down"/>
  </xsd:restriction>
</xsd:simpleType>
~~~

1. A `geospatialCoverage` element specifies the lat/lon bounding box, and an altitude range covered by the data.
2. The `northsouth` and `eastwest` elements should both be set to specify a lat/lon bounding box. 
The default units are *degrees_north* and *degrees_east*, respectively. 
3. The `updown` element specifies the altitude range, with default units in *meters*. 
4. You can optionally add any number of `name`s to describe the covered region. 
An important special case is global coverage, in which case you should use the name `global` (see example below):
5. A `zpositive` value of up means that `z` increases up, like units of height, while a value of down means that `z` increases downward, like units of pressure or depth. 
6. The `spatialRange` elements indicate that the range goes from `start` to `start + size`. 
7. Use the `resolution` attribute to indicate the data resolution.

#### Example

~~~xml
<geospatialCoverage zpositive="down">
  <northsouth>
    <start>10</start>
    <size>80</size>
    <resolution>2</resolution>
    <units>degrees_north</units>
  </northsouth>
  <eastwest>
    <start>-130</start>
    <size>260</size>
    <resolution>2</resolution>
    <units>degrees_east</units>
  </eastwest>
  <updown>
    <start>0</start>
    <size>22</size>
    <resolution>0.5</resolution>
    <units>km</units>
  </updown>
</geospatialCoverage>

<geospatialCoverage>
  <name vocabulary="Thredds">global</name>
</geospatialCoverage>
~~~

### `timeCoverage` Element 

~~~xml
<xsd:complexType name="timeCoverageType">   
  <xsd:sequence>
    <xsd:choice minOccurs="2" maxOccurs="3" >
      <xsd:element name="start" type="dateTypeFormatted"/>   
      <xsd:element name="end" type="dateTypeFormatted"/>     
      <xsd:element name="duration" type="duration"/>         
    </xsd:choice>
    <xsd:element name="resolution" type="duration" minOccurs="0"/>     
  </xsd:sequence>
</xsd:complexType>
~~~
The `timeCoverage` element specifies a date range and is defined by the `timeCoverageType`.
The `timeCoverageType`'s `start` element represents the start of the date range.
The `end` element represents the end of the date range.
The `duration` element represents the date range duration.
The optional `resolution` element should be used to indicate the data resolution for time series data.

When using the `timeCoverage` element, the date range can be specified in one of the following ways: 
1. By giving both a `start` and an `end` date element;  **or** 
2. By specifying a `start` element and a `duration` element; **or** 
3. By specifying an `end` element and a `duration` element. 

#### Examples

~~~xml
<timeCoverage>
  <start>1999-11-16T12:00:00</start>
  <end>present</end>
</timeCoverage>

<timeCoverage>
  <start>1999-11-16T12:00:00</start>
  <duration>P3M</duration>  <!-- 3 months  -->
</timeCoverage>

<timeCoverage>   <!-- 10 days before the present up to the present -->
  <end>present</end>
  <duration>10 days</duration>
  <resolution>15 minutes</resolution>
</timeCoverage>
~~~

## `date` Type

~~~xml
<xsd:simpleType name="dateType">
  <xsd:union memberTypes="xsd:date xsd:dateTime udunitDate">
    <xsd:simpleType>
      <xsd:restriction base="xsd:token">
        <xsd:enumeration value="present"/>
      </xsd:restriction>
    </xsd:simpleType>
  </xsd:union>
</xsd:simpleType>

<xsd:simpleType name="udunitDate">
  <xsd:restriction base="xsd:string">
    <xsd:annotation>
      <xsd:documentation>Must conform to complete udunits date string, eg
          "20 days since 1991-01-01"</xsd:documentation>
    </xsd:annotation>
  </xsd:restriction>
</xsd:simpleType>
~~~

A `dateType` follows the [W3C Date profile of ISO 8601 for date/time formats](https://www.w3.org/TR/NOTE-datetime){:target="_blank"}. 
Note that it is a simple type, so that it can be used as the *type of an attribute*. 
It can be one of the following:

* an `xsd:date`, with form `CCYY-MM-DD`
* an `xsd:dateTime` with form `CCYY-MM-DDThh:mm:ss`, `CCYY-MM-DDThh:mm:ssZ` or `CCYY-MM-DDThh:mm:ss-hh:ss`
* a valid [UDUNITS](https://www.unidata.ucar.edu/software/udunits/udunits-current/doc/udunits/udunits2.html){:target="_blank"} date string
* the string `present`

#### Examples

~~~xml
<start>1999-11-16</start>
<start>1999-11-16T12:00:00</start> <!-- implied UTC -->
<start>1999-11-16T12:00:00Z</start> <!-- explicit UTC  -->
<start>1999-11-16T12:00:00-05:00</start> <!-- EST time zone specified -->
<start>20 days since 1991-01-01</start>
<start>present</start>
~~~

### `dateTypeFormatted` Type

~~~xml
<xsd:complexType name="dateTypeFormatted">
  <xsd:simpleContent>
    <xsd:extension base="dateType">
      <xsd:attribute name="format" type="xsd:string" /> // from java.text.SimpleDateFormat
      <xsd:attribute name="type" type="dateEnumTypes" />
    </xsd:extension>
  </xsd:simpleContent>
</xsd:complexType>
~~~

A `dateTypeFormatted` extends `dateType` by allowing an optional, user-defined `format` attribute and an optional `type` attribute. 
The format string follows the specification in [java.text.SimpleDateFormat](https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html){:target="_blank"}. 
The values of the `type` attribute are taken from the Dublin Core date types.

#### Example

~~~xml
<start format="yyyy DDD" type="created">1999 189</start> <!-- year, day of year -->
~~~

|-----------------|-------------------------------|
| Example_Format_String | Example_Text |
|:----------------|:------------------------------|
| `yyyy.MM.dd G 'at' HH:mm:ss z` |  `2001.07.04 AD at 12:08:56 PDT` |
| `EEE, MMM d, "yy"` |  `Wed, Jul 4, '01`
| `K:mm a, z` | `0:08 PM, PDT` |
| `yyyyy.MMMMM.dd GGG hh:mm aaa` | `02001.July.04 AD 12:08 PM` |
| `EEE, d MMM yyyy HH:mm:ss Z` |   `Wed, 4 Jul 2001 12:08:56 -0700` |
| `yyMMddHHmmssZ` | `010704120856-0700` |

### `duration` Type

~~~xml
<xsd:simpleType name="duration">
  <xsd:union memberTypes="xsd:duration udunitDuration" />
</xsd:simpleType>

<xsd:simpleType name="udunitDuration">
  <xsd:restriction base="xsd:string">
    <xsd:annotation>
      <xsd:documentation>Must conform to udunits time duration, eg "20.1 hours"
      </xsd:documentation>
    </xsd:annotation>
  </xsd:restriction>
</xsd:simpleType>
~~~

A `duration` type can be one of the following:

* an `xsd:duration` type specified in the following form `PnYnMnDTnHnMnS` where:
  * `P` indicates the period (required)
  * `nY` indicates the number of years
  * `nM` indicates the number of months
  * `nD` indicates the number of days
  * `T` indicates the start of a time section (required if you are going to specify hours, minutes, or seconds)
  * `nH` indicates the number of hours
  * `nM` indicates the number of minutes
  * `nS` indicates the number of seconds
* a valid [UDUNITS](https://www.unidata.ucar.edu/software/udunits/udunits-current/doc/udunits/udunits2.html){:target="_blank"} time duration string.

#### Example

~~~xml
<duration>P5Y2M10DT15H</duration>
<duration>5 days</duration>
~~~

### `dataSize` Element

~~~xml
<xsd:element name="dataSize">
  <xsd:complexType>
    <xsd:simpleContent>
    <xsd:extension base="xsd:string">
      <xsd:attribute name="units" type="xsd:string" use="required"/>
    </xsd:extension>
    </xsd:simpleContent>
  </xsd:complexType>
</xsd:element>
~~~

A `dataSize` element is just a number with a `units` attribute, which should one of the following:
* `bytes`
* `Kbytes`
* `Mbytes`
* `Gbytes`
* `Tbytes`

#### Example

~~~xml
<dataSize units="Kbytes">123</dataSize>
~~~

### `controlledVocabulary` Type

~~~xml
<xsd:complexType name="controlledVocabulary">
 <xsd:simpleContent>
  <xsd:extension base="xsd:string">
   <xsd:attribute name="vocabulary" type="xsd:string" />
  </xsd:extension>
 </xsd:simpleContent>
</xsd:complexType>
~~~

A `controlledVocabulary` simply adds an optional `vocabulary` attribute to the string-valued element, indicating that the value comes from a restricted list.

#### Example

~~~xml
<name vocabulary="DIF">UCAR/NCAR/CDP</name>
~~~

### `variables` Element

~~~xml
<xsd:element name="variables">
  <xsd:complexType>
    <xsd:choice>
      <xsd:element ref="variable" minOccurs="0" maxOccurs="unbounded"/>
      <xsd:element ref="variableMap" minOccurs="0"/>
    </xsd:choice>
    <xsd:attribute name="vocabulary" type="variableNameVocabulary" use="optional"/>
    <xsd:attributeGroup ref="XLink"/>
  </xsd:complexType>
</xsd:element>

<xsd:element name="variable">
  <xsd:complexType mixed="true">
    <xsd:attribute name="name" type="xsd:string" use="required"/>
    <xsd:attribute name="vocabulary_name" type="xsd:string" use="optional"/>
    <xsd:attribute name="units" type="xsd:string"/>
  </xsd:complexType>
</xsd:element>

<xsd:element name="variableMap">
  <xsd:complexType>
    <xsd:attributeGroup ref="XLink"/>
  </xsd:complexType>
</xsd:element>
~~~

A `variables` element contains a list of `variables` OR a `variableMap` element that refers to another document that contains a list of `variables`. 
This element specifies the `variables` (aka fields or parameters) that are available in the dataset, and associates them with a standard vocabulary of names, through the `vocabulary` attribute. 
The optional `XLink` is a reference to an online resource describing the standard vocabulary.

Each `variable` element must have a `name` attribute which contains the name of variable in the dataset. 
The optional `vocabulary_name` attribute contains the variables name from a standard vocabulary (specified by the `variables` element). 
The `units` attribute contains the units of the variable in the dataset. 
The content of the `variable` element can contain text describing the variable. 
A `variableMap` element contains an `XLink` to `variable` elements, so that you can factor these out and refer to them from multiple places.

The main purpose of the `variables` element is to describe a dataset for a search service or digital library.  
For example GCMD requires a list of dataset "Parameter Valids" from their controlled vocabulary. 
A client might want to show those "standard variable names" to a user, since the names may be more meaningful than the actual variable names.

#### Examples
     
~~~xml
<variables vocabulary="CF-1.0">
  <variable name="wv" vocabulary_name="Wind Speed" units="m/s">Wind Speed @ surface</variable>
  <variable name="wdir" vocabulary_name="Wind Direction" units= "degrees">
    Wind Direction @ surface
  </variable>
  <variable name="o3c" vocabulary_name="Ozone Concentration" units="g/g">
    Ozone Concentration @ surface
  </variable>
</variables>

<variables vocabulary="GRIB-NCEP" xlink:href="http://www.unidata.ucar.edu//GRIB-NCEPtable2.xml">
  <variableMap xlink:href="../standardQ/Eta.xml" />
</variables>
~~~

A `variableMap` should point to an XML document with a top-level `variables` element with the THREDDS `namespace` declared:

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<variables xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0" >
  <variable name="wv" vocabulary_name="Wind Speed" units="m/s"/>
  <variable name="wdir" vocabulary_name="Wind Direction" units= "degrees"/>
  <variable name="o3c" vocabulary_name="Ozone Concentration" units="g/g"/>
  ...
</variables>
~~~



## Other Enumerations

The remaining definitions are all enumerations of "well-known" values. 
Note that for all of these, any token is a legal value. 
However, standard software is likely to understand only the values that are explicitly listed. 
We encourage you to use these well-known values if possible, and to submit new values to the [THREDDS mailing list](https://www.unidata.ucar.edu/mailing_lists/archives/thredds/){:target="_blank"} for inclusion in future versions of this schema.

### `dataFormat` Enumeration

~~~xml
<!-- DataFormat Types -->
<xsd:simpleType name="dataFormatTypes">
  <xsd:union memberTypes="xsd:token mimeType">
    <xsd:simpleType>
      <xsd:restriction base="xsd:token">
        <xsd:enumeration value="BUFR"/>
        <xsd:enumeration value="ESML"/>
        <xsd:enumeration value="GEMPAK"/>
        <xsd:enumeration value="GINI"/>
        <xsd:enumeration value="GRIB-1"/>
        <xsd:enumeration value="GRIB-2"/>
        <xsd:enumeration value="HDF4"/>
        <xsd:enumeration value="HDF5"/>
        <xsd:enumeration value="McIDAS-AREA"/>
        <xsd:enumeration value="NcML"/>
        <xsd:enumeration value="NetCDF"/>
        <xsd:enumeration value="NetCDF-4"/>
        <xsd:enumeration value="NEXRAD2"/>
        <xsd:enumeration value="NIDS"/>

        <xsd:enumeration value="image/gif"/>
        <xsd:enumeration value="image/jpeg"/>
        <xsd:enumeration value="image/tiff"/>
        <xsd:enumeration value="text/csv"/>
        <xsd:enumeration value="text/html"/>
       <xsd:enumeration value="text/plain"/>
         <xsd:enumeration value="text/tab-separated-values"/>
        <xsd:enumeration value="text/xml"/>
        <xsd:enumeration value="video/mpeg"/>
        <xsd:enumeration value="video/quicktime"/>
        <xsd:enumeration value="video/realtime"/>
      </xsd:restriction>
    </xsd:simpleType>
  </xsd:union>
</xsd:simpleType>

<xsd:simpleType name="mimeType">
  <xsd:restriction base="xsd:token">
    <xsd:annotation>
      <xsd:documentation>any valid mime type
        (see http://www.iana.org/assignments/media-types/)
      </xsd:documentation>
    </xsd:annotation>
  </xsd:restriction>
</xsd:simpleType>
~~~

These describe the data formats, used in an `access` attribute or [`dataset`](#dataset-type) element, when the service is a bulk-transport protocol (like FTP), and the client has to know how to read the downloaded dataset file.

In addition to the file formats explicitly listed, you can use a mime type. 
We have listed relevant file formats above.

You can also use your own scientific file format.
[Send them to us](https://www.unidata.ucar.edu/mailing_lists/archives/thredds/){:target="_blank"}, and we will add it to this list (check to see if it's a mime type first).

#### Examples

~~~xml
<dataFormat>NcML</dataFormat>
<dataFormat>image/gif</dataFormat>
<dataFormat>image/jpeg</dataFormat>
<dataFormat>image/png</dataFormat>
<dataFormat>video/mpeg</dataFormat>
<dataFormat>video/quicktime</dataFormat>
~~~

### `dataType` Enumeration

~~~xml
<xsd:simpleType name="dataTypes">
  <xsd:union memberTypes="xsd:token">
    <xsd:simpleType>
      <xsd:restriction base="xsd:token">
        <xsd:enumeration value="Grid"/>
        <xsd:enumeration value="Image"/>
        <xsd:enumeration value="Point"/>
        <xsd:enumeration value="Radial"/>
        <xsd:enumeration value="Station"/>
        <xsd:enumeration value="Swath"/>
        <xsd:enumeration value="Trajectory"/>
      </xsd:restriction>
    </xsd:simpleType>
  </xsd:union>
</xsd:simpleType>
~~~

These are the [Feature Types](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/feature_datasets.html){:target="_blank"} of the datasets, which are used by clients to know how to display the data.

### `date` Enumeration

~~~xml
<xsd:simpleType name="dateEnumTypes">
  <xsd:union memberTypes="xsd:token">
    <xsd:simpleType>
      <xsd:restriction base="xsd:token">
        <xsd:enumeration value="created"/>
        <xsd:enumeration value="modified"/>
        <xsd:enumeration value="valid"/>
        <xsd:enumeration value="issued"/>
        <xsd:enumeration value="available"/>
        <xsd:enumeration value="metadataCreated"/>
      </xsd:restriction>
    </xsd:simpleType>
  </xsd:union>
</xsd:simpleType>
~~~

The `date` type enumeration defines a basic set of types for a `date` element. 
These values were taken from the [Dublin Core](https://www.dublincore.org/){:target="_blank"} metadata set.

This set of values is not exclusive so other values are allowed. 
Alternate values must be strings that do not contain end-of-line characters or tabs (they must be of the [`xsd:token`](https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#token){:target="_blank"}  data type).

### `documentation` Enumeration

~~~xml
<xsd:simpleType name="documentationEnumTypes">
 <xsd:union memberTypes="xsd:token">
  <xsd:simpleType>
   <xsd:restriction base="xsd:token">
     <xsd:enumeration value="funding"/>
     <xsd:enumeration value="history"/>
     <xsd:enumeration value="processing_level"/>
     <xsd:enumeration value="rights"/>
     <xsd:enumeration value="summary"/>
   </xsd:restriction>
  </xsd:simpleType>
 </xsd:union>
</xsd:simpleType>
~~~

The `documentation` type enumeration defines a basic set of types used by the [`documentation`](#documentation-element) element.

This set of values is not exclusive so other values are allowed. 
Alternate values must be strings that do not contain end-of-line characters or tabs (they must be of the [`xsd:token`](https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#token){:target="_blank"}  data type).

### `metadata` Enumeration

~~~xml
<xsd:simpleType name="metadataTypeEnum">
    <xsd:union memberTypes="xsd:token">
      <xsd:simpleType>
        <xsd:restriction base="xsd:token">
          <xsd:enumeration value="THREDDS"/>
          <xsd:enumeration value="ADN"/>
          <xsd:enumeration value="Aggregation"/>
          <xsd:enumeration value="CatalogGenConfig"/>
          <xsd:enumeration value="DublinCore"/>
          <xsd:enumeration value="DIF"/>
          <xsd:enumeration value="FGDC"/>
          <xsd:enumeration value="LAS"/>
          <xsd:enumeration value="ESG"/>
        <xsd:enumeration value="Other"/>
      </xsd:restriction>
     </xsd:simpleType>
   </xsd:union>
  </xsd:simpleType>
~~~

The `metadata` type enumeration defines a basic set of types used by the [`metadata`](#metadata-element) element.

This set of values is not exclusive so other values are allowed. 
Alternate values must be strings that do not contain end-of-line characters or tabs (they must be of the [`xsd:token`](https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#token){:target="_blank"} data type).

### `serviceTypes` Enumeration

~~~xml
<xsd:simpleType name="serviceTypes">
 <xsd:union memberTypes="xsd:token">
  <xsd:simpleType>
   <xsd:restriction base="xsd:token">

    <!-- client/server -->
    <xsd:enumeration value="ADDE"/>
    <xsd:enumeration value="DAP4"/>
    <xsd:enumeration value="DODS"/> <!-- same as OpenDAP -->
    <xsd:enumeration value="OpenDAP"/>
    <xsd:enumeration value="OpenDAPG"/>
    <xsd:enumeration value="NetcdfSubset"/>
    <xsd:enumeration value="CdmRemote"/>
    <xsd:enumeration value="CdmFeature"/>
    <xsd:enumeration value="ncJSON"/>
    <xsd:enumeration value="H5Service"/>

    <!-- bulk transport -->
    <xsd:enumeration value="HTTPServer"/>
    <xsd:enumeration value="FTP"/>
    <xsd:enumeration value="GridFTP"/>
    <xsd:enumeration value="File"/>

    <!-- web services -->
    <xsd:enumeration value="ISO"/>
    <xsd:enumeration value="LAS"/>
    <xsd:enumeration value="LAS"/>
    <xsd:enumeration value="NcML"/>
    <xsd:enumeration value="UDDC"/>
    <xsd:enumeration value="WCS"/>
    <xsd:enumeration value="WMS"/>
    <xsd:enumeration value="WSDL"/>

    <!--offline -->
    <xsd:enumeration value="WebForm"/>

    <!-- THREDDS -->
    <xsd:enumeration value="Catalog"/>
    <xsd:enumeration value="Compound"/>
    <xsd:enumeration value="Resolver"/>
    <xsd:enumeration value="THREDDS"/>
   </xsd:restriction>
  </xsd:simpleType>
 </xsd:union>
</xsd:simpleType>
~~~

These are the known service types, used in a [`service`](#service-element) element, that indicate how to access a dataset. 
A `serviceType` is similar, but not generally the same as the [scheme](https://www.iana.org/assignments/uri-schemes/uri-schemes.xhtml){:target="_blank"} of a URI, like `http:`, `ftp:`, `file:`, etc. 
In general, the combination of the `serviceType` and the [`dataFormat`](#dataformat-enumeration) is intended to be sufficient for a client to access and read the dataset. 
Additional information can be encoded in service properties.

The `OPeNDAP` and `ADDE` service types correspond to the [OPeNDAP](https://www.opendap.org/){:target="_blank"} and [ADDE](https://www.ssec.wisc.edu/mcidas/doc/learn_guide/current/adde.html){:target="_blank"} data access protocols. 
These are client/server protocols that specify both the access (or transport) protocol, and the data model; so no separate `dataFormat` attribute needed. 
`DODS` is a synonym for `OPeNDAP`; `OpenDAP-G` corresponds to `OPeNDAP` over [`GridFTP`](https://en.wikipedia.org/wiki/GridFTP){:target="_blank"}.

The next set of service types are all bulk-transfer protocols, and you need to also specify the `dataFormat` for datasets that use these.
FTP is the well-known File Transfer Protocol, and GridFTP is a variant of that used by the Globus Data Grid. 
The File service is for local files, used for local catalogs or in situations like DODS Aggregation Server configuration. 
A `File` dataset is not readable by remote clients. 
`HTTPServer` should be used when your file is being served by an HTTP (Web) Server. 
This is used for bulk-transfer just like FTP, and also can be used by the [NetCDF-Java](https://docs.unidata.ucar.edu/netcdf-java/{{site.netcdf-java_docset_version}}/userguide/){:target="_blank"} library to access NetCDF files remotely (in that case just make sure the dataset has `dataFormatType` of `NetCDF` or `NcML`).

The `LAS` service type is for connection to Live Access Servers. 
`WMS`, `WFS` and `WCS` are for the Web Map, Feature, and Coverage Servers, respectively, from the [OpenGIS Consortium](https://www.ogc.org/){:target="_blank"}. 
These are still experimental servers, at least for THREDDS. 
`WSDL` corresponds to a server using the [Web Services Description Language](https://www.w3.org/TR/wsdl/){:target="_blank"} to specify its data services. 
We do not yet have an example of that within THREDDS.

The `WebForm` service indicate that the dataset URL will take you to an HTML page where you can presumably order the data in some way, to be delivered later. 
It's still a good idea to specify the dataset `dataFormatType`.

The last set of service types are THREDDS defined types. 
The `Catalog` and `Resolver` types return XML documents over HTTP. 
These are generally handled internally by THREDDS. 
A [compoundService](#service-element) indicates the service is composed of other services.

### `variableNameVocabulary` Enumerations

~~~xml
<xsd:simpleType name="variableNameVocabulary">
  <xsd:union memberTypes="xsd:token">
    <xsd:simpleType>
      <xsd:restriction base="xsd:token">
        <xsd:enumeration value="CF-1.0"/>
        <xsd:enumeration value="DIF"/>
        <xsd:enumeration value="GRIB-1"/>
        <xsd:enumeration value="GRIB-2"/>
      </xsd:restriction>
    </xsd:simpleType>
  </xsd:union>
</xsd:simpleType>
~~~

These are the known vocabularies for standard variable names, used in the [`variables`](#variables-element) element. 
`CF` refers to the [Climate and Forecast Conventions](https://cfconventions.org/){:target="_blank"} metadata conventions for netCDF; they have a [list of standard variable names](https://cfconventions.org/standard-names.html){:target="_blank"}. 
`DIF` is [Directory Interchange Format](https://idn.ceos.org/){:target="_blank"} from NASA's Global Change Master Directory, which has a controlled variable classification scheme. 
The World Meteorological Organization's [GRIB (version 1)](https://dss.ucar.edu/docs/formats/grib/gribdoc/){:target="_blank"} data file format defines a set of standard parameters.

You can also use another vocabulary name; [send it to us](https://www.unidata.ucar.edu/mailing_lists/archives/thredds/){:target="_blank"}, and we will add it to this list.

## Dataset Access Methods

There are two ways a dataset's access methods can be specified:

1. A dataset element may include a `urlPath` attribute. 
The value of the `urlPath` attribute along with the dataset default service specify one or more* access methods.

2. A `dataset` element may include child `access` elements. 
Each `access` element defines one or more access methods. 
The values of the access element's `urlPath` and `serviceName` attributes specify one or more access methods. 
If the `access` element does not include a `serviceName` attribute, the dataset default service is used.

(*) Multiple access methods are defined whenever the `service` element is a [`compoundService`](#access-methods-and-compound-services).

### Dataset Default Service

There are a number of ways a `service` element can be referenced by a dataset. 
When multiple references come into play for a given dataset, the following is the precedence for deciding on the default service to use with access methods:

1. A dataset may have a `serviceName` attribute or element directly in it.
2. A dataset may inherit a `serviceName` from a parent dataset.
3. A dataset may contain an `access` element that defines a `serviceName` and `urlPath`.
4. A dataset may have a `featureType` or `dataType` defined and a `urlPath`, in which case it will use the default services for that type.

#### Examples
    
1. A `dataset` element has a `urlPath` attribute and inherits a `serviceName` from a parent/ancestor dataset. 
  This is a common case as many catalogs will contain datasets that all refer to the same service.
   
   ~~~xml
   <dataset name="collection of data">
     <metadata inherited="true">
       <serviceName>myservice</serviceName>
     </metadata>
     <dataset name="my dataset" urlPath="myData.nc" />
     <dataset name="our dataset" urlPath="ourData.nc" />
     <dataset name="their dataset" urlPath="theirData.nc" />
     ...
   </dataset>
   ~~~
   
   2. A `dataset` element has a `urlPath` attribute and directly contains a `serviceName` element or attribute.
   
   ~~~xml
   <dataset name="my dataset" urlPath="myData.nc">
     <serviceName>myservice</serviceName>
   </dataset>
   
   <dataset name="my dataset" urlPath="myData.nc" serviceName="myservice">
   ~~~
   
   3. A `dataset` element contains a child access element. 
   
   ~~~xml
   <dataset name="my dataset">
     <access serviceName="myservice" urlPath="myData.nc" />
   </dataset>
   ~~~
   
   4. A dataset may have a `featureType` or `dataType` enumeration defined and a `urlPath`. 
   
   ~~~xml
   <dataset name="my dataset"  dataType="Grid" urlPath="myData.nc" />  
   ~~~  

The [standard services](services_ref.html#standard-data-services) for Grids will be used.    

### Access Methods And Compound Services

Any `service` element of type `Compound` used in the construction of access methods results in one access method for each nested service.

#### Example

~~~xml
<service name="all" serviceType="Compound" base="" >
  <service name="odap" serviceType="OPENDAP" base="/thredds/dodsC/" />
  <service name="wcs" serviceType="WCS" base="/thredds/wcs/" />
</service>
<dataset name="cool data" urlPath="cool/data.nc">
  <serviceName>all</serviceName>
</dataset>
~~~

The above results in two access methods for `cool data` using the `urlPath` attribute value `cool/data.nc`:
1. one using the `odap` `service` element and;
2. the other using the `wcs` `service` element

### Constructing URLs

A dataset access URL is constructed by concatenating the service `base` URL with the access `urlPath`. 
If the service has a `suffix` attribute, that is then appended:

~~~
URL = service.base + access.urlPath + service.suffix
~~~

These operations are straight string concatenations -- a slash (`/`) is not automatically added. 
If a slash is needed between the `base` and `urlPath`, remember to include a trailing slash on the value of the `service@base` attribute.

Clients have access to each of these elements and may make use of the URL in protocol-specific ways. 
For example the OPeNDAP protocol appends `dds`, `das`, and `dods` to make the actual calls to the OPeNDAP server.

When a service `base` is a relative URL, it is resolved against the catalog base URL. 

#### Example

If the catalog base URL is `https://thredds.ucar.edu/thredds/dodsC/catalog.xml` and a service `base` is `airtemp/`, then the resolved `base` is `https://thredds.ucar.edu/thredds/dodsC/airtemp/`. 

If the service base is `/airtemp/`, the resolved URL is `https://thredds.ucar.edu/thredds/airtemp/`.

## Dataset Classification

THREDDS *Dataset Inventory Catalogs* organize and describe collections of data. 
A catalog can be thought of as a logical directory of data resources available via the Internet. 
A dataset may be a:

1. **direct dataset** (describes how to directly access data over the Internet);
2. **collection dataset** (contains other datasets); or
3. **dynamic dataset** (content is generated by a call to a server).

### Direct Datasets

A direct access dataset has an access URL and a service type (like `FTP`, `DODS`, `WMS`, etc.) that allows a THREDDS-enabled application to directly access its data, using the specified service's protocol. 
It is represented simply by a dataset `type` element.

### Collection Datasets

A collection dataset has nested `<dataset> `elements. We distinguish two types:

1. A *heterogeneous collection* dataset may have arbitrarily-deep nested datasets, and there are no constraints on how the datasets are related.
2. A *coherent collection* dataset contains nested datasets which must be direct and coherently related. 
A coherent dataset should have a `collectionType` attribute that describes the relationship of its nested datasets.

### Dynamic Datasets

A dynamic dataset has an access URL and a service `type` Catalog, or `Resolver`. 
Its contents are generated dynamically by making a call to a server, and describe datasets that are constantly changing, and/or are too large to list exhaustively.

A *resolver dataset* is a kind of query dataset, with service type Resolver. 
It returns a catalog which must contain either a direct dataset, or a coherent collection dataset. 
It is typically used to implement a *virtual* dataset like "latest model run" or "latest measurement" on a real time dataset, where the actual URL must be generated when the user requests it.

A query dataset looks a lot like a [`catalogRef`](#catalogref-element), since you de-reference a URL and get a catalog back. 
However, a `catalogRef` is cacheable, but a query dataset is inherently dynamic, so is not cacheable.

## Datasets As Web Resources

Its important to distinguish a THREDDS dataset from its access URL. 
A dataset can have multiple ways of being accessed, and so have multiple access URLs. 
But, even in the simple case that a dataset has one access URL, the dataset potentially contains metadata that is not stored with the data pointed to by its access URL. 
In order to use the full power of THREDDS, you must work with the full dataset object, not just with its access URL.

One way to reference the dataset as a web resource is to use `catalog.xml#datasetId`, where `catalogURL` is the URL of a THREDDS catalog, and `datasetId` is the ID of a dataset inside of that catalog. 

#### Example

~~~
http://server:8080/thredds/catalog/grib.v5/gfs_2p5deg/catalog.xml#grib.v5/gfs_2p5deg/TwoD
~~~

The reference implementation of THREDDS datasets is the netCDF-Java library, which accepts dataset URLS of the form `thredds:catalog.xml#dataset_id`, where the `thredds:` prefix ensures that the URL is understood as a THREDDS catalog and dataset.

In the context of a web browser, the dataset URL is `catalog.html?dataset=datasetId`, for example

#### Example

~~~
http://localhost:8081/thredds/catalog/GFS_CONUS_80km/catalog.html?dataset=GFS_CONUS_80km/Best
~~~

This URL, when sent to a THREDDS Data Server, shows the metadata for the dataset with ID `GFS_CONUS_80km/Best` in the catalog `http://localhost:8081/thredds/catalog/GFS_CONUS_80km/catalog.html`.