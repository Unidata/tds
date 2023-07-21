---
title: Customizing the TDS Look and Feel
last_updated: 2020-08-24
sidebar: user_sidebar
toc: false
permalink: customizing_tds_look_and_feel.html
---

TDS provides a extensible and customizable user interface using [Thymeleaf](https://www.thymeleaf.org/){:target="_blank"} Java template engine.
The HTML pages which currently support customization are:
 * Catalog
 * Dataset access

UI customization can be implemented through the contribution of [CSS stylesheets](customizing_tds_look_and_feel.html#css-stylesheets) 
and [Thymeleaf HTML templates](customizing_tds_look_and_feel.html#thymeleaf-templates).
To promote the accessibility and usability of dataset, TDS administrators may register Jupyter Notebooks as dataset viewers 
using the [Jupyter Notebook service](customizing_tds_look_and_feel.html#jupyter-notebooks).

## CSS Stylesheets

To customize the TDS using CSS, contributed CSS documents should be placed inside the `${tds.content.root.path}/thredds/public` directory.

By default, the TDS is configured to use several CSS documents supplied in the `public` directory in `threddsConfig.xml`.
There are four properties within the `htmlSetup` element used to define stylesheets:

~~~xml
<htmlSetup>
   <standardCssUrl>standard.css</standardCssUrl>
   <catalogCssUrl>catalog.css</catalogCssUrl>
   <datasetCssUrl>dataset.css</datasetCssUrl>
   <openDapCssUrl>tdsDap.css</openDapCssUrl>
</htmlSetup>
~~~

To override HTML style default the TDS, replace any or all of the standard stylsheets with your own.
Each property is responsible for style on a set of HTML pages:
* `standardCssUrl`: styles are applied on all generated HTML pages (except the OPeNDAP HTML form)
* `catalogCssUrl`: styles are applied only on Catalog HTML pages
* `datasetCssUrl`: styles are applied only on Dataset access HTML pages
* `openDapCssUrl`: styles are applied only on the OPeNDAP HTML form; unlike other pages, this is the only CSS document applied on the page

## Thymeleaf Templates

When the TDS is deployed, a `templates` directory is created within the main `content` directory (`tds.content.root.path`).
Each time a customizable HTML page is requested (Catalog or Dataset), the Thymeleaf template resolver will search this directory
for user-supplied template fragments.

Pages are customizable at plug-in points defined by the tag `ext:`, which instructs the template resolver to look for
externally supplied template fragments.  
Some of the plug-in points provide defaults when no user-supplied template is available (such as the main TDS header and footer, 
whereas other plug-in allow for additional content. 
See the [Thymeleaf documentation](https://www.thymeleaf.org/doc/articles/layouts.html){:target="_blank"} for an overview of natural templating using Thymeleaf and fragments. 
A full list of currently supported plug-in points for user-supplied fragments can be found in the following sections.

### Overwriting a default

To contribute a template fragment, place the `fragment` element in `templates/tdsTemplateFragments.html`.

#### Example: overwriting the default header

Add the following to the 'template/tdsTemplateFragments.html' file:

~~~html
<div th:fragment="header">Your header content here</div>
~~~

The templating system will automatically attach default TDS CSS properties to custom headers and footers.
To avoid this behavior, users must provide their own overrides through [custom stylesheets](customizing_tds_look_and_feel.html#css-stylesheets).

Current default fragments which allow overrides are:
  * `header`
  * `footer`

### Contributing additional content sections

Users may contribute additional content sections the same way as overridable defaults; unlike sections with default content, i.e. headers and footers), 
additional content sections are optional and will only render as HTML elements if a user-contributed template fragment exists.

#### Example: adding content to the bottom of the Catalog

Add the following to the 'template/tdsTemplateFragments.html' file:

~~~html
<div th:fragment="datasetCustomContentBottom">
    <div>Your bottom content goes here.</div>
</div>
~~~

#### Example: contributing multiple fragments

To add multiple fragments to a customizable section, add the following to the 'template/tdsTemplateFragments.html' file:

~~~html
<div th:fragment="datasetCustomContentBottom">
    <div th:replace="~{ext:additionalFragments/myFragments :: mySectionHeader}"/>
    <div th:replace="~{ext:additionalFragments/myFragments :: mySectionContent}"/>
</div>
~~~

And, in the `templates/additionalFragments/myFragments.html` file:

~~~html
    <div th:fragment="mySectionHeader" class="section-header">My Section Name</div>
    <div th:fragment="mySectionContent" class="section-content">Your contributed content here.</div>
~~~

In the example above, we have defined our own fragments in a separate file, `myFragments.html`. 
Fragments which correspond to a plug-in point, such as `catalogCustomContentTop` must be within the file `tdsTemplateFragments`, 
however main fragments may reference paths to unlimited other template files by using the `ext:` tag.

*Note:* The classes `section-header` and `section-content` apply the default TDS style for content panes.

Currently supported contributable sections are:

  * `catalogCustomContentTop` - additional content placed at the top of catalog pages.
  * `catalogCustomContentBottom` - additional content placed at the bottom of catalog pages.
  * `datasetCustomContentBottom` - additional content placed at the bottom of dataset access pages.

### Contributing additional content tabs
Contributing tabbed content requires two fragments, one for the *tab button* and another for the *content*.
Each tab button must implement the click event handler `switchTab(buttonElement, contentElementId, groupId)`.

#### Example

Add the following to the 'template/tdsTemplateFragments.html' file:

~~~html
<div th:fragment="customInfoTabButtons">
   <div class="tab-button info" onclick="switchTab(this, 'custom1', 'info')">Custom1</div>
   <div class="tab-button info" onclick="switchTab(this, 'custom2', 'info')">Custom2</div>
</div>

<div th:fragment="customInfoTabContent">
   <div class="tab-content info" id="custom1">This is one contributed tab pane...</div>
   <div class="tab-content info" id="custom2">..and this is a second!</div>
</div>
~~~

In the above example, the `tab-button` and `tab-content` classes apply the same style to the contributed tabs as the
default tabs. 
The `info` class groups the contributed tabs with the other tabs in the information tab pane.
To group a contributed tab with the access tab pane, use the `access` class.
*Note:* Multiple custom tabs may be contributed by grouping them within the fragment tags.

Current contributable tabs are:

  * `customAccessTabButtons/customAccessTabContent` - adds tabs to the tab pane holding the "Access" and "Preview" views.
  * `customInfoTabButtons/customInfoTabContent` - adds tabs to the tab pane holding view with information about the dataset.

### Accessing TDS properties in custom templates
Information from the server is passed to the templated pages through a data model. 
The properties made available to the template parser are:

~~~java
{
  String googleTracking,
  String serverName,
  String logoUrl,
  String logoAlt,
  String installName,
  String installUrl,
  String webappName,
  String wabappUrl,
  String webappVersion,
  String webappBuildTimestamp,
  String webappDocsUrl,
  String contextPath,
  String hostInst,
  String hostInstUrl
}
~~~

Additionally, the catalog page is passed the properties `boolean rootCatalog`, which is set to `true` only on the top-level catalog page, 
and `List<CatalogItemContext> items`, a set of items in the Catalog defined as `CatalogItemContext` data contracts:

~~~java
class CatalogItemContext {

  String getDisplayName();

  int getLevel();

  String getDataSize();

  String getLastModified();

  String getIconSrc();

  String getHref();
}
~~~

Similarly, the dataset page is passed the property `DatasetContext dataset`, a data contract defining the properties of the dataset:

~~~java
class DatasetContext {

  String getName();

  String getCatUrl();

  String getCatName();

  List<Map<String, String>> get Documentation();

  List<Map<String, String>> getAccess();

  List<Map<String, String>> getContributors();

  List<Map<String, String>> getKeywords();

  List<Map<String, String>> getDates();

  List<Map<String, String>> getProjects();

  List<Map<String, String>> getCreators();

  List<Map<String, String>> getPublishers();

  List<Map<String, String>> getVariables();

  String getVariableMapLink();

  Map<String, Object> getGeospatialCoverage();

  Map<String, Object> getTimeCoverage();

  List<Map<String, String>> getMetadata();

  List<Map<String, String>> getProperties();

  Map<String, Object> getAllContext();

  Object getContextItem(String key);

  List<Map<String, String>> getViewerLinks();
}
~~~

#### Example: Dataset view

Add a section to a dataset view which links to the host institution site and displays a table of all properties returned by `getAllContext()`.

Add the following to the 'template/tdsTemplateFragments.html' file:

~~~html
<div th:fragment="datasetCustomContentBottom">
    <h3>Properties of
      <th:block th:text="${dataset.getName()}"
       - hosted by <a th:href="${hostInstUrl}" th:text="${hostInst}"></a>
    </h3>
    <table class="property-table">
        <tr th:each="prop : ${dataset.getAllContext()}">
            <td><em th:text="${prop.key}"/><td th:text="${prop.value}"/>
        </tr>
    </table>
</div>
~~~

### Contributing to the TDS: adding accessible properties

Don't see what you're looking for?
If the properties exposed to the template parser do not meet your needs, you are encouraged to update the above data models by submitting a pull request to
the [Unidata TDS GitHub repository](https://github.com/Unidata/tds){:target="_blank"}. 
The data models are defined and populated in
[`CatalogViewContextParser.java`](https://github.com/Unidata/tds/blob/master/tds/src/main/java/thredds/server/catalogservice/CatalogViewContextParser.java){:target="_blank"}.

## Jupyter Notebooks

### About
The goal of the *Jupyter Notebook service* is to provide a method of interacting with and visualizing TDS datasets without
large data transfers. 
When the Notebook service is enabled, the service provides a list of available Notebooks the demo access to requested dataset via [Siphon](https://unidata.github.io/siphon/latest/api/){:target="_blank"}. 
The service returns requested Notebooks as `ipynb` files, which may be viewed in Jupyter Notebook or JupyterLab and edited by the end user to 
explore capabilities of the dataset and Siphon.

Read more about Jupyter Notebooks [here](https://jupyter-notebook.readthedocs.io/en/stable/).

### Enable/Disable Notebook Service
By default, the Jupyter Notebook service is enabled. To disable the Notebook service, add the following property to `threddsConfig.xml`:

~~~xml
  <JupyterNotebookService>
    <allow>false</allow>
  </JupyterNotebookService>
~~~

To configure the Notebook service, add the following properties to `threddsConfig.xml`:

~~~xml
  <JupyterNotebookService>
    <allow>true</allow>
    <maxAge>60</scour>
    <maxFiles>100</maxFiles>
  </JupyterNotebookService>
~~~

Where `<maxAge>` defines how long a mapping between a dataset and a Notebook should be stored after the last access, and `<maxFiles>` defines the maximum number of mappings which can be stored at one time.
The TDS provides some default Notebook viewers, which can be 

### Using the Notebooks Service

#### Accessing Notebooks through a browser

All Notebooks viewers that are valid for a given dataset can be accessed though the Dataset HTML page under the "Preview" tab.

#### Accessing Notebooks via code

Two public endpoints are available in the Notebook service:
* Get all valid viewers for a dataset: {hostURL}/thredds/notebook/{datasetID}?catalog={catalogURL}
    * e.g. https://mysite.edu/thredds/notebook/mydataset?catalog=catalog.xml
* Download a selected viewer: {hostURL}/thredds/notebook/{datasetID}?catalog={catalogURL}&filename={filename}
    * e.g. https://mysite.edu/thredds/notebook/mydataset?catalog=catalog.xml&filename=jupyter_viewer.ipynb

### Custom Notebooks
To add a Notebook viewers to the TDS Notebook service, place `ipynb` files in the `notebooks` folder within the content directory. 
(*Note*: To register new Notebook viewers, the server must be restarted with the new files in the notebook directory, 
TDS will not process new Notebooks while active.)

Notebook viewer properties are set by adding a `viewer_info` property to the Notebooks metadata block:

~~~
  "metadata": {
  ...
    "viewer_info": {
    ...
    }
  }
~~~

The Notebook services checks for two viewer properties: `description` and `accepts`. The `description` property defines a plain-text
description of the viewer, and defaults to an empty string if not present. The `accepts` property defines the set of datasets for which
the viewer is valid and may include any or all of the following sub-properties:

* `accept_datasetIDs`: Accepts a list of dataset IDs for which the Notebook is valid.
* `accept_catalogs`: Accepts a list of catalog names or URLs which contain datasets for which the Notebook is valid.
* `accept_dataset_types`: Accepts a list of  feature types for which the Notebook is valid (e.g. Grid, Point, Station).
* `accept_all`: If true, indicates the Notebook is valid for all datasets.

If no `accepts` properties are included in the Notebook metadata, the Notebook will default to `"accept_all": true`.

#### Examples

A Notebook configured for all datasets in the catalog `testCatalog`:
~~~
  "metadata": {
  ...
    "viewer_info": {
        "description": "This Notebook displays all datasets in the test catalog.",
        "accepts": {
          "accept_catalogs": ["testCatalog"],
        }
    }
  }
~~~

A Notebook configured for all gridded datasets and a dataset called `almostGridded`
~~~
  "metadata": {
  ...
    "viewer_info": {
        "description": "This Notebook displays gridded data.",
        "accepts": {
          "accept_datasetIDs": ["almostGridded"],
          "accept_dataset_types": ["Grid"]
        }
    }
  }
~~~

The `accept_datasetIDs` can also include regular expressions. This can be useful, for instance, when configuring a
notebook for all datasets in a `datasetScan`:
~~~
  "metadata": {
  ...
    "viewer_info": {
        "description": "Notebook that displays all datasets in a dataset scan.",
        "accepts": {
          "accept_datasetIDs": ["myDatasetScanID/.*"],
        }
    }
  }
~~~

#### Suppressing default Notebooks

To suppress default Notebooks, you can override them with a custom Notebook or a dummy Notebook, configured to not accept any datasets.

For example, to suppress `default_viewer.ipynb`, place a file of the same name in the content directory with the following `viewer_info`:
~~~
  "metadata": {
  ...
    "viewer_info": {
        "accepts": {
          "accept_all": false
        }
    }
  }
~~~

### Contributing default Notebooks

You can contribute default Notebooks viewers to the TDS repository to highlight various types of datasets by submitting a pull request to
the [Unidata TDS GitHub repository](https://github.com/Unidata/tds){:target="_blank"}.
The default Notebooks live in the [`jupyter_notebooks`](https://github.com/Unidata/tds/tree/master/tds/src/main/webapp/WEB-INF/altContent/startup/jupyter_notebooks) directory.
*NOTE:* Be sure to map your contributed Notebooks to the appropriate datasets by editing the Notebook's metadata, as described above.
