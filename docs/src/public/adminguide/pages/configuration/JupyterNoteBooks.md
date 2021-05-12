---
title: Jupyter Notebook Service
last_updated: 2020-10-03
sidebar: admin_sidebar
toc: true
permalink: jupyter_notebooks.html
---

## About Jupyter Notebooks

The goal of the Jupyter Notebook service is to provide an method of interacting with and visualizing TDS datasets without
large data transfers. 
When the Notebook service is enabled, requests to the service will return a Notebook (`ipynb` file) which demos accessing the requested dataset via [Siphon](https://unidata.github.io/siphon/latest/api/){:target="_blank"}. 
Notebook files may be viewed in Jupyter Notebook or JupyterLab and edited by the end user to explore capabilities of the dataset and Siphon.

### Enable/Disable Notebook Service
By default, the Jupyter Notebook service is enabled. 
If no contributed Notebook viewers are found, the TDS will supply a default viewer for accessing all datasets in the system. 
This default can be found in `notebooks/jupyter_viewer.ipynb` in the content directory.

To disable the Notebook service, add the following property to `threddsConfig.xml`:

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

Where `<maxAge>` defines how long a mapping between a dataset and a Notebook should be stored after the last access, and `<maxFile>` defines the maximum number of mapping which can be stored at one time.

### Contribute Notebooks

  * To add a Notebook viewers to the TDS Notebook service, place `ipynb` files in the `notebooks` folder within the
  content directory. 
  (Note: To register new Notebook viewers, the server must be restarted with the new files in the
 notebook directory, TDS will not process new Notebooks while active.)
  * To map a Notebook viewer to a subset of datasets, include the following in the Notebook's metadata:

~~~
  "metadata": {
  ...
    "viewer_info": {
      "accept_datasetIDs": [],
      "accept_catalogs": [],
      "accept_dataset_types": [],
      "accept_all" : false,
      "order": 1
    }
  }
~~~

All `viewer_info` properties are optional. 
Multiple properties may be used to register a single Notebook.

* `accept_datasetIDs` - Accepts a list of dataset IDs for which the Notebook is valid.
* `accept_catalogs` - Accepts a list of catalog names or URLs which contain datasets for which the Notebook is valid.
* `accept_dataset_types`: Accepts a list of  feature types for which the Notebook is valid (e.g. Grid, Point).
* `accept_all` - If true, indicates the Notebook is valid for all datasets.
* `order` - In the case that more than one Notebook is valid for a given dataset, `order` will be used to determine
which Notebook is returned.

If no `viewer_info` is included in the Notebook metadata, the following default will be supplied:

~~~
  "metadata": {
  ...
    "viewer_info": {
      "accept_all" : true,
      "order": INT_MAX
    }
  }
~~~
