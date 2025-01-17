---
title: Restrict Access To The TDS
last_updated: 2020-10-15
sidebar: admin_sidebar
toc: true
permalink: restrict_access_to_tds.html
---

## Methods Of Restricting Access

There are three options to limit access to specific datasets or the TDS application as a whole:

* [Limit Access To Entire TDS By IP/Host](#limit-access-to-entire-tds-by-iphost)
* [Limit Access To Parts Or Entire TDS By User/Role](#limit-access-to-parts-or-entire-tds-by-userrole)
* [Limit Access To Specific Dataset By User/Role](#limit-access-to-specific-dataset-by-userrole)

They can be used individually or in combination with one another.
All of these options rely upon the Tomcat servlet container functionality to a certain degree.

## Limit Access To Entire TDS By IP/Host

The Tomcat `RemoteHostValve` or `RemoteAddrValve` allows you to either allow or deny access to the TDS (or other web applications) by IP address or host name.  This is an _application-wide_ approach to access control.

Configure the valves in the Tomcat `${tomcat_home}/conf/server.xml` or web application `META-INF/context.xml` files.

{%include note.html content="
These valves rely on incoming IP addresses or hostnames which are vulnerable to spoofing. 
Also, this approach is not much help when dealing with [DHCP](https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol){:target='_blank'} clients.
" %} 

### `RemoteAddrValve`

The `RemoteAddrValve` compares the client IP address against one or more regular expressions to either allow or refuse the request from this client.

#### Example `RemoteAddrValve` Use

1. Using the `RemoteAddrValve` to allow access only for the clients connecting from localhost:

   ~~~xml
   <!-- This example only allows access from localhost -->
   <Valve className="org.apache.catalina.valves.RemoteAddrValve"
          allow="127\.\d+\.\d+\.\d+|::1|0:0:0:0:0:0:0:1"/>
   ~~~
   
2. Using the `RemoteAddrValve` to allow unrestricted access for the clients connecting from localhost but for all other clients only to port 8443:
   ~~~xml
   <!-- This example allows 8080 access from localhost but all other connections must use 8443  -->
   <Valve className="org.apache.catalina.valves.RemoteAddrValve"
          addConnectorPort="true"
          allow="127\.\d+\.\d+\.\d+;\d*|::1;\d*|0:0:0:0:0:0:0:1;\d*|.*;8443"/>
   ~~~

### `RemoteHostValve`

The `RemoteHostValve` compares the client hostname against one or more regular expressions to either allow or refuse the request from this client.

#### Example `RemoteHostValve` Use

1. Using the `RemoteHostValve` to restrict access based on resolved host names:

   ~~~xml
   <!-- This example denies access based on host names -->
   <Valve className="org.apache.catalina.valves.RemoteHostValve"
              deny=".*\.bandwidthhogs\.com" />
   ~~~  

   {% capture remote_host_valve %}
      Consult the Tomcat [Remote Host Valve](https://tomcat.apache.org/tomcat-{{ site.tomcat_version }}-doc/config/valve.html#Remote_Host_Valve){:target='_blank'}  documentation for more information about valve syntax and options.
   {% endcapture %}
   {% include info.html content=remote_host_valve%}


## Limit Access To Parts Or Entire TDS By User/Role

Use a web application's [deployment descriptor](https://docs.oracle.com/cd/E19316-01/819-3669/6n5sg7bhc){:target="_blank"} (`web.xml`) file to limit access to parts or all of the application.

Servlet containers, including Tomcat, can implement access control configurations found in the deployment descriptor file by enforcing user authentication to the restricted resources.
This type of access control works well when you want to restrict your entire site to a single set of users, or when you want to give access to different datasets to different users. 

{%include note.html content="
You will need to configure the [user authentication credentials](/digested_passwords.html) in Tomcat to and should enable [TLS/SSL encryption](/enable_tls_encryption.html) to utilize this type of access control.
" %}

### Creating A `security-constraint` In `web.xml`

Restrict a pattern of URLs, by adding `<security-constraint>` elements into the deployment descriptor (`web.xml`) file. 
The following fragment will force **all** URL accesses that have the `urlPattern` to authorized users with the role `roleName`. 
The `<transport-guarantee>` elements forces a switch to communication over the TLS/SSL socket:

~~~xml
<security-constraint>
  <web-resource-collection>
    <web-resource-name>restrict by URL</web-resource-name>
    <url-pattern>urlPattern</url-pattern>
    <http-method>GET</http-method>
  </web-resource-collection>
  <auth-constraint>
    <role-name>roleName</role-name>
  </auth-constraint>
  <user-data-constraint>
    <transport-guarantee>CONFIDENTIAL</transport-guarantee>
  </user-data-constraint>
</security-constraint>
~~~

#### Example `<security-constraint>` Use

~~~xml
<security-constraint>
  <web-resource-collection>
    <web-resource-name>restrict by URL</web-resource-name>
    <url-pattern>/dodsC/dataRoot/*</url-pattern>
    <http-method>GET</http-method>
  </web-resource-collection>
  <auth-constraint>
    <role-name>tiggeRole</role-name>
  </auth-constraint>
  <user-data-constraint>
    <transport-guarantee>CONFIDENTIAL</transport-guarantee>
  </user-data-constraint>
</security-constraint>
~~~

You do **not** include `/thredds` in the `<url-pattern>` element. 
Also, if you are using multiple data services, you must include each service's URL pattern.

#### Example `<url-pattern>` Use

~~~xml
<web-resource-collection>
  <web-resource-name>restrict by URL</web-resource-name>
  <url-pattern>/dodsC/testEnhanced/*</url-pattern>
  <url-pattern>/fileServer/testEnhanced/*</url-pattern>
  <http-method>GET</http-method>
</web-resource-collection>
~~~

{%include info.html content="
Consult the [Using Deployment Descriptors to Secure Web Applications](https://docs.oracle.com/cd/E19226-01/820-7627/6nisfjn8c/){:target='_blank'} by Oracle for the documentation outlining the role of the deployment descriptor in web application security.
" %}

## Limit Access To Specific Dataset By User/Role

THREDDS catalogs offer a more fine-grained approach to access control of datasets.

### How It Works

A more fine-grained approach is to modify the `dataset` elements in the [TDS configuration catalog](server_side_catalog_specification.html). 
To do this, you add an attribute on a `dataset` or `datasetScan` element in the TDS catalog: `restrictAccess="roleName"`. 
All services that use that dataset will be restricted to users with the named role.
    
When a client tries to access a restricted dataset, it is redirected to a URL that triggers a security challenge. 
If the challenge is successful, the client is redirected back to the original dataset URL, except now it has an authenticated session, represented by a session cookie passed to the client. 
For subsequent requests by the same client, no authentication is needed as long as the session remains valid.

The default TDS configuration uses BASIC authentication [digested passwords](/digested_passwords.html). 
By modifying the TDS deployment descriptor (`web.xml`) file, the server administrator can require that authentication be done differently, (e.g., require TLS/SSL). 
You can also plug in your own Authentication.

{%include warning.html content="
Changes to the TDS `web.xml` must be **manually** propagated to new versions of the TDS when upgrading.
" %} 

### Client Communication With The TDS

To access any restricted dataset that a TDS might serve, a client such as a browser, OPeNDAP enabled application, or WCS client, must be able to:

1 Follow redirects, including circular redirects;
2 Switch to TLS/SSL and back;
3 Perform Basic and Digest authentication;
4 Answer the security challenges with the appropriate user name and password; and
5 Return session cookies.

#### How To Configure Restricted Datasets

1.  Decide on distinct sets of datasets that need to be restricted. 
For each set, choose a name called a `security role`. 
Avoid special characters in the role names, especially `/"><'` and space. 

    For example, suppose you have three sets of restricted data that you call `ccsmData`, `fieldProject`, `tiggeData`.

2.  Add each role to the `${tomcat_home}/conf/tomcat-users.xml` file, along with the `restrictedDatasetUser` role:

    ~~~xml
    <role rolename="restrictedDatasetUser"/>
    <role rolename="ccsmData"/>
    <role rolename="fieldProject"/>
    <role rolename="tiggeData"/>
    ~~~

    If you only have one set of datasets that you want to restrict, you can use just the `restrictedDatasetUser`, i.e., you don't need to have multiple roles. 
    However, you **must always** use the name `restrictedDatasetUser`.

3.  [Add each user](/digested_passwords.html) who should have authorization to the `tomcat-users.xml` file. 
A user may have multiple roles, and must always have the `restrictedDatasetUser` role:

    ~~~xml
    <user username="john" 
          password="df693bv78sdcf8da45980f340923789093fc1585808170b2027677195e0ad121f87b27320ae$1$55003acb56e907b1wo95jflw8459475035gj0s8304g66fhs7fnns" 
          roles="ccsmData,restrictedDatasetUser"/>
    <user username="tiggeUser" 
          password="bb7a2b6cf8da7122125c663fc1585808170b2027677195e0ad121f87b27320ae$1$55003acb56e907b19d29d3b4211dc98c837354690bc90579742d6747efeec4ea" 
          roles="tiggeData,restrictedDatasetUser"/>
    <user username="luci" 
          password="fjs7eglls8976497044f575fh6870884830f6980362535e5fg789046254867hj$1$88906khl56e980b19dfh749607388gh64807dc98c783636hj8654859hgkgj8fnd" 
          roles="fieldProject,tiggeData,restrictedDatasetUser"/>
    ~~~
    {%include warning.html content="
    Make sure *none* of these `restrictedDatasetUsers` have any of the \"secure\" roles such as `tdsConfig`, `manager`, or `admin`.
    
     
    The `tdsConfig`, `manager` and `admin` roles allow access to secure parts of Tomcat and TDS. 
    These can only be accessed using HTTPS (TLS), and thus are considered secure. 
    
    
    If you are restricting access to datasets, you will also add other users who will have the `restrictedDatasetUser` role. 
    The `restrictedDatasetUser` usage can also use non-HTTPS URLs, and so is vulnerable to [session hijacking](enable_tls_encryption.html). 
    
    
    By keeping the roles separate, you make sure the worst that can happen is that someone can download some scientific data they shouldn't have access to.
    " %} 
    
4.  In the [TDS configuration catalogs](/server_side_catalog_specification.html), add `restrictAccess={security role}` attributes to the `dataset` or `datasetScan` elements. 
This will also restrict access to all children of those datasets. 

    #### Example `restrictAccess={security role}` Use

    ~~~xml
    <?xml version="1.0" encoding="UTF-8"?>
    <catalog name="TDS Catalog" xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0">
      <service name="thisDODS" serviceType="OpenDAP" base="/thredds/dodsC/" />
      <datasetRoot path="test" location="/data/testdata/"/>
      <dataset name="Test Single Dataset" ID="testDataset" serviceName="thisDODS"
          urlPath="test/testData.nc" restrictAccess="tiggeData">
        <dataset name="Nested" ID="nested" serviceName="thisDODS" urlPath="test/nested/testData.nc" />
      </dataset>
      <datasetScan name="Test all files in a directory" ID="testDatasetScan"
          path="testAll" location="/data/testdata" restrictAccess="ccsmData">
        <metadata inherited="true">
          <serviceName>thisDODS</serviceName>
        </metadata>
      </datasetScan>
    </catalog>
    ~~~

    The `dataset` with `ID` `testDataset`, as well as its child dataset nested are restricted, as are all the datasets generated by the `datasetScan`. 
    Users can see these datasets in the catalogs, but when they try to access the data, they will be challenged to authenticate.

5.  After restarting Tomcat, use a browser to navigate to a restricted dataset. 
You should be prompted for a username and password. This must match a user that has a `role` matching the `restrictAccess` attribute on the dataset.

    {%include troubleshooting.html content="
     In the BASIC and DIGEST authentication processes, your browser will send _cached_ credentials, if they exist, to the server without giving you a chance to re-enter. 
     
     
     You will need to clear your browser cache of incorrect or out-of-date credentials in order to authenticate successfully. 
     Depending on your browser, this may require exiting all instances of the browser and restarting it.
     
     
     Furthermore, make sure your user has the `restrictedDatasetUser` security role in addition to the role needed to access the desired dataset.
     " %} 
