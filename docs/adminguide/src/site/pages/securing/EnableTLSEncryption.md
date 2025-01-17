---
title: Enable TLS/SSL Encryption
last_updated: 2020-10-10
sidebar: admin_sidebar
toc: true
permalink: enable_tls_encryption.html
---

## Steps Needed To Enable Transport Layer Security (TLS)

The following must be performed to create a secure connection for a web application (such as the TDS):

1. Modify the Tomcat [server-level](#configuring-tls-in-tomcat) configurations to enable TLS; 
2. Set up a _security constraint_ the web application [deployment descriptor](#configuring-tls-in-web-applications) file. 

## Configuring TLS In Tomcat

The following example demonstrates enabling Transport Layer Security in the Tomcat Servlet Container on a linux system as the `root` user. 

1. [Import](https://tomcat.apache.org/tomcat-{{ site.tomcat_version }}-doc/ssl-howto.html#Prepare_the_Certificate_Keystore){:target="_blank"} your CA-signed certificate into the keystore file as per the [Tomcat documentation](https://tomcat.apache.org/tomcat-{{ site.tomcat_version }}-doc/ssl-howto.html#Prepare_the_Certificate_Keystore){:target="_blank"}.

   
   {% include important.html content="
   **Do NOT use a self-signed certificate!**
   Unidata _highly_ recommends the use of a certificate signed by a Certificate Authority (CA).
   
   
   There are a lot of [compelling arguments](https://www.sslshopper.com/article-when-are-self-signed-certificates-acceptable.html){:target='_blank'} as to why self-signed certificates should **not** be used in a production environment.
   "%}


2. Modify the Tomcat configuration to enable TLS.

   Open `${tomcat_home}/conf/server.xml` with your favorite text editor:

   ~~~bash
   # vi server.xml
   ~~~

   Locate the `Java HTTP/1.1 Connector` listening on port `8080` and verify it is redirecting TLS traffic to port `8443`:
   ~~~xml
   <Connector port="8080" 
              protocol="HTTP/1.1"
              connectionTimeout="20000"
              redirectPort="8443" />
   ~~~

   Find and uncomment the `NIO implementation SSL HTTP/1.1 Connector` listening on port `8443` to activate this connector:

   ~~~xml
   <Connector port="8443" 
              protocol="org.apache.coyote.http11.Http11NioProtocol" 
              maxThreads="150" 
              SSLEnabled="true">
       <SSLHostConfig>
           <Certificate certificateKeystoreFile="conf/localhost-rsa.jks" 
                        type="RSA" />
       </SSLHostConfig>
   </Connector>
   ~~~
   
   {% capture connector %}
   Tomcat also offers a `SSL/TLS HTTP/1.1 Connector` which utilizes `APR/native implementation`. 
   Consult the [Documentation](http://tomcat.apache.org/tomcat-{{ site.tomcat_version }}-doc/config/http.html){:target='_blank'} to see if you should use this connector in lieu of the `NIO implementation SSL HTTP/1.1` connector.
   {% endcapture %}
   {% include info.html content=connector %}
   
   Specify the keystore file in the `certificateKeystoreFile` attribute of the `Certificate` element to tell Tomcat where to find your keystore (the path will be relative to `${tomcat_home}` directory).  
   
   In this example, the keystore file is `${tomcat_home}/conf/tds-keystore`:

   ~~~xml
   <Connector port="8443" 
              protocol="org.apache.coyote.http11.Http11NioProtocol" 
              maxThreads="150" 
              SSLEnabled="true">
       <SSLHostConfig>
           <Certificate certificateKeystoreFile="conf/tds_keystore" 
                        type="RSA"/>
       </SSLHostConfig>
   </Connector>
   ~~~

   If you opted to not use the default keystore password (`changeit`), you'll need to specify the new password so Tomcat can open the file.  Add the `certificateKeystorePassword` attribute of the `Certificate` element for your keystore password.
   
   ~~~xml
   <Connector port="8443" 
              protocol="org.apache.coyote.http11.Http11NioProtocol" 
              maxThreads="150" 
              SSLEnabled="true">
       <SSLHostConfig>
           <Certificate certificateKeystoreFile="conf/tds_keystore" 
                        certificateKeystorePassword="foobar"
                        type="RSA"/>
       </SSLHostConfig>
   </Connector>
   ~~~
 
3. Verify TLS has been enabled.

   Restart Tomcat:

   ~~~bash
   # /usr/local/tomcat/bin/shutdown.sh
   # /usr/local/tomcat/bin/startup.sh
   ~~~

   Verify Tomcat is listening on port 8443:

   ~~~bash
   # netstat -an | grep tcp | grep 8443

   tcp        0      0 0.0.0.0:8443                0.0.0.0:*                LISTEN 
   ~~~

   {%include info.html content="
     Run `man netstat` in your terminal window to learn more about this command.
   " %}

### Troubleshooting
* Check the XML syntax in `${tomcat_home}/conf/server.xml` to make sure it is well-formed and without error.
* Did you restart Tomcat after you made your changes to `server.xml`?
* Did you specify the full path to the keystore file in `server.xml`?


## Configuring TLS In Web Applications

The web application deployment descriptor, a.k.a. `web.xml` specifies if all or parts of the web application need to be accessed via TLS.  
The deployment descriptor file is located in the `WEB-INF` directory of the web application:

~~~bash
${tomcat_home}/webapps/application_name/WEB-INF/web.xml
~~~

By convention, Tomcat and other servlet containers will read the web application deployment descriptors _upon application deployment_ to look for:
 
 1. initialization  parameters; and 
 2. container-managed security constraints.

### Example Deployment Descriptor Entry

Here is a _container-managed security constraint_ entry (item #2 above) you might find in a deployment descriptor:

~~~xml
<security-constraint>
  <web-resource-collection>
    <url-pattern>/secret_stuff/*</url-pattern> 
  </web-resource-collection>
  <user-data-constraint>
    <transport-guarantee>CONFIDENTIAL</transport-guarantee>
  </user-data-constraint>
</security-constraint>
~~~

Simple descriptions of the elements in the entry above:

|--------------------------|--------------|
| Configuration            |  Description |
|:-------------------------|:-------------|
| `<security-constraint>`  | Defines the access privileges to a collection of resources using their URL mapping. |
| `<web-resource-collection>` | A list of URL patterns that describe a set of resources to be protected. |
| `<url-pattern>` | The request URI to be protected. |
| `<user-data-constraint>` | Establishes the data will be transported between client and server will take place over a protected transport layer connection. |
| `<transport-guarantee>` | Choices for type of transport guarantee include `NONE`, `INTEGRAL`, and `CONFIDENTIAL`:<br/><br/> Specify `CONFIDENTIAL` when the application requires that data be transmitted so as to prevent other entities from observing the contents of the transmission. (E.g., via TLS.)<br/><br/> Specify `INTEGRAL` when the application requires that the data be sent between client and server in such a way that it cannot be changed in transit.<br/><br/> Specify `NONE` to indicate that the container must accept the constrained requests on any connection, including an unprotected one. |

{%include note.html content=" 
  For more information on how to configure security requirements for a web application in a deployment descriptor, see: [Defining Security Requirements for Web Applications](https://javaee.github.io/tutorial/security-webtier.html#BNCAS){:target='_blank'}.
" %}

### TDS `web.xml` Is Preconfigured

**The TDS has been pre-configured to require TLS encryption to access its administration tools, such as the  the [TDS Remote Management Tool](remote_management_ref.html), and the [TdsMonitor Tool](using_the_tdsmonitor_tool.html).**

You, the administrator, do NOT have to modify the TDS's `web.xml` file.

However, if you choose to use these administration tools (and we encourage you to do so), you will need to [configure the required access roles](accessing_tds_admin_tools.html) in the Tomcat server.

#### Security Constraint For The TDS Remote Management Tool 

The following is the _security constraint_ entry in the TDS `web.xml` for the [TDS Remote Management Tool](remote_management_ref.html):

~~~xml
<!-- tdsConfig with HTTPS needed for /admin access  -->
<security-constraint>
  <web-resource-collection>
    <web-resource-name>sensitive read access</web-resource-name>
    <url-pattern>/admin/*</url-pattern>  <!-- 1 -->
  </web-resource-collection>
  <auth-constraint>                      <!-- 2 -->
    <role-name>tdsConfig</role-name>     <!-- 3 -->
  </auth-constraint>
  <user-data-constraint>
    <transport-guarantee>CONFIDENTIAL</transport-guarantee>  <!-- 4 -->
  </user-data-constraint>
</security-constraint>
~~~

Using the [security constraint definitions](#example-deployment-descriptor-entry) above, you can see this  entry shows:

|------|-------------|
| Comment # | Description |
|:-----|:------------| 
| 1 | The URI pattern `https://hostname:port/admin/*` of the THREDDS Data Server is considered a protected resource.<br/>Any content therein will be governed by the rest of the security constraint. |
| 2 & 3 | The `<auth-constraint>` and `role-name` elements do not deal with TLS connections per se, but rather access control.<br/>  These configurations are restricting access to users in the role `tdsConfig`. |
| 4 | We are requiring a TLS connection to access this content. |   


#### Security Constraint For The TDSMonitor Tool 


The following is the _security constraint_ entry in the TDS `web.xml` for the [TdsMonitor Tool](using_the_tdsmonitor_tool.html):

~~~xml
<!-- tdsMonitor with HTTPS needed for access to logs  -->
<security-constraint>
  <web-resource-collection>
    <web-resource-name>sensitive read access</web-resource-name>
    <url-pattern>/admin/log/*</url-pattern>    <!-- 1 -->
  </web-resource-collection>
  <auth-constraint>                            <!-- 2 -->
    <role-name>tdsMonitor</role-name>          <!-- 3 -->
  </auth-constraint>
  <user-data-constraint>
    <transport-guarantee>CONFIDENTIAL</transport-guarantee>   <!-- 4 --?
  </user-data-constraint>
</security-constraint>
~~~

Using the [security constraint definitions](#example-deployment-descriptor-entry) above, you can see this  entry shows:

|------|-------------|
| Comment # | Description |
|:-----|:------------| 
| 1 | The URI pattern `https://hostname:port/admin/logs/*` of the THREDDS Data Server is considered a protected resource.<br/>Any content therein will be governed by the rest of the security constraint. <br/><br/>Note these configurations will override the configurations declared for the [TDS Remote Management Tool](#security-constraint-for-the-tds-remote-management-tool) for the `/admin/logs/*` directory. |
| 2 & 3 | The `<auth-constraint>` and `role-name` elements do not deal with TLS connections per se, but rather access control.<br/>  These configurations are restricting access to users in the role `tdsMonitor`. |
| 4 | We are requiring a TLS connection to access this content. |   
