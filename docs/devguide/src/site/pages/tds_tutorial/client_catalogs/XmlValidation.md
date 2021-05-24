---
title: XML Validation
last_updated: 2020-08-24
sidebar: dev_sidebar
toc: false
permalink: client_catalog_xml_validation.html
---

As catalogs get more complicated, you should check that you haven't made any errors in your XML syntax.
There are three components to checking for catalog:

1. Is the XML well-formed?
2. Is it valid against the catalog schema?
3. Does it have everything it needs to be read by a THREDDS client?

You can check _well-formedness_ using [online tools](http://www.xmlvalidation.com/){:target="_blank"}.
If you also want to check _validity_ in those tools, you will need to declare the catalog schema location like so:

~~~xml
<catalog name="Validation" xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0
    http://www.unidata.ucar.edu/schemas/thredds/InvCatalog.1.0.6.xsd">
  ...
</catalog>
~~~

* The `xmlns:xsi` attribute  declares the schema-instance namespace.
  Just copy it exactly as you see it here.
* The `xsi:schemaLocation` attribute tells your XML validation tool where to find the THREDDS XML schema document.
  Just copy it exactly as you see them here.

Or, you can simply use the [THREDDS Catalog Validation service](https://thredds.ucar.edu/thredds/remoteCatalogValidation.html){:target="_blank"} to check all three components at once.
This service already knows where the schemas are located, so it's not necessary to add that information to the catalog; you only need it if you want to do your own validation.

{%include note.html content="
For more information, you can look at the [schema](https://www.unidata.ucar.edu/schemas/thredds/InvCatalog.1.0.7.xsd){:target='_blank'} referenced in the above example.
However, you'll probably want to study the [catalog specification](client_side_catalog_specification.html) instead, as it is much more digestible.
" %}
