---
title: Enable TLS Encryption
last_updated: 2020-10-10
sidebar: quickstart_sidebar
toc: true
permalink: enable_tls_encryption.html
---

## The Threat

Communication between two servers can be intercepted (i.e., an HTTP transaction between the client and server).
An attacker can eavesdrop on the conversation and control the relay of messages between the victims, making them believe that they are talking directly to each other over a private connection.

A potential goal of an attack is to steal personal information, such as login credentials.


{% include image.html file="installation_upgrade/securing/man_in_the_middle.png" alt="Man In The Middle Attack" caption="" %}

## Transport Layer Security (TLS)

Transport Layer Security (TLS), and formerly Secure Sockets Layer (SSL), is a cryptographic protocol that provides security and data integrity for communications over TCP/IP networks.
TLS allows applications to communicate across a network in a way designed to prevent eavesdropping, tampering, and message forgery.

TLS uses a cryptographic system that uses two keys to encrypt data: a public key known to everyone and a private or secret key known only to the recipient of the message.

By convention, URLs that require an TLS connection start with `https` instead of `http`.

{%include note.html content="
For more information on how TLS works, Wikipedia details the [steps involved](https://en.wikipedia.org/wiki/Transport_Layer_Security){:target='_blank'} during an TLS transaction.
" %}

### TLS Certificates
A public key certificate is an electronic document which incorporates a digital signature to bind together a public key with identity information of the certificate user.
The certificate can be used to verify that a public key belongs to an individual.
The digital signature can be signed by a Certificate Authority (CA) or the certificate user (a self-signed certificate).

### Do Not Use Self-Signed Certificates

Unidata _highly_ recommends the use of a certificate signed by a Certificate Authority (CA) for the following reasons:

* Browser warnings for self-signed certificates can be very confusing and make users question the legitimacy of your website.
* More and more browsers are no longer supporting/displaying [mixed http/https content](https://blog.chromium.org/2019/10/no-more-mixed-messages-about-https.html){:target="_blank"}.
* It's about trust: CA-signed certificates verify your identify to your users.
  If an attacker intercepts the traffic between your server and the client they can inject their own self-signed certificate in the place of your own.
   In such a case, the visitor likely will not notice.
  
* Self-signed certificates _cannot_ (by nature) be revoked, which may allow an attacker who has already gained access to monitor and inject data into a connection to spoof an identity if a private key has been compromised. 
  Certificate Authorities, on the other hand, have the ability to revoke a compromised certificate.

### Certificate `keystore` File
A keystore file stores the details of the Transport Layer Security certificate necessary to make the protocol secured.
The Tomcat documentation includes a section on [importing your certificate](https://tomcat.apache.org/tomcat-{{ site.tomcat_version }}-doc/ssl-howto.html#Prepare_the_Certificate_Keystore){:target="_blank"} into a keystore file.
Tomcat uses the keystore file for TLS transactions. 

## Enabling Transport Layer Security

The following steps are needed to create a secure connection for a web application (such as the TDS):

1. Modify the Tomcat [server-level](#configuring-tls-in-tomcat) configurations to enable; 
2. Define the users and roles who are perm
2. In the web application [deployment descriptor](#configuring-tls-in-web-applications). 

### Configuring TLS In Tomcat

The following example demonstrates enabling Transport Layer Security in the Tomcat Servlet Container on a linux system as the `root` user. 

1. [Imported](https://tomcat.apache.org/tomcat-{{ site.tomcat_version }}-doc/ssl-howto.html#Prepare_the_Certificate_Keystore){:target="_blank"} your CA-signed certificate into the keystore file as per the [Tomcat documentation](https://tomcat.apache.org/tomcat-{{ site.tomcat_version }}-doc/ssl-howto.html#Prepare_the_Certificate_Keystore){:target="_blank"}.


1. Modify the Tomcat configuration to enable TLS:

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
   
      
   {%include tip.html content="
   Changes to `${tomcat_home}/conf/server.xml` do not take effect until Tomcat is restarted.
   " %}

2. Verify TLS has been enabled.

   Restart Tomcat:

   ~~~bash
   # /usr/local/tomcat/bin/shutdown.sh
   # /usr/local/tomcat/bin/startup.sh
   ~~~

   Verify Tomcat is listening on port 8443 by running the `netstat` command:

   ~~~bash
   # netstat -an | grep tcp | grep 8443

   tcp        0      0 0.0.0.0:8443                0.0.0.0:*                LISTEN 
   ~~~

   {%include info.html content="
     Run `man netstat` in your terminal window to learn more about this command.
   " %}

#### Troubleshooting Tips
* Check the XML syntax in `${tomcat_home}/conf/server.xml` to make sure it is well-formed and without error.
* Did you restart Tomcat after you made your changes to `server.xml`?
* Did you specify the full path to the keystore file in `server.xml`?


### Configuring TLS In Web Applications

The web application deployment descriptor, a.k.a. `web.xml` specifies if all or parts of the web application need to be accessed via TLS.  

The deployment descriptor file is located in the `WEB-INF` directory of the web application:

~~~bash
${tomcat_home}/webapps/application_name/WEB-INF/web.xml
~~~

By convention, Tomcat and other servlet containers will read the web application deployment descriptors for several reasons, including:
 
 1. initialization parameters
 2. container-managed security constraints upon application deployment.

#### Example Deployment Descriptor Entry

This is the entry in the TDS `web.xml` for the [TDS Remote Management Tool](remote_management_ref.html):

~~~xml
<security-constraint>
  <web-resource-collection>
    <url-pattern>/secret_stuff/*</url-pattern> 
  </web-resource-collection>
  <auth-constraint>
    <role-name>vipUsersRole</role-name>
  </auth-constraint>
  <user-data-constraint>
    <transport-guarantee>CONFIDENTIAL</transport-guarantee>
  </user-data-constraint>
</security-constraint>
~~~

Simple descriptions of the relevant elements in the entry above:

|--------------------------|--------------|
| Configuration            |  Description |
|:-------------------------|:-------------|
| `<security-constraint>`  | Defines the access privileges to a collection of resources using their URL mapping. |
| `<web-resource-collection>` | A list of URL patterns that describe a set of resources to be protected. |
| `<url-pattern>` | The request URI to be protected. |
| `<auth-constraint>` | Specifies whether authentication is to be used and names the roles authorized to perform the constrained requests. |
| `<role-name>` | The role name of one of the security-role elements defined for this web application at the server-level (`${tomcat_home/conf/tomcat-users.xml`) |
| `<user-data-constraint>` | Establishes the data will be transported between client and server will take place over a protected transport layer connection. |
| `<transport-guarantee>` | Choices for type of transport guarantee include `NONE`, `INTEGRAL`, and `CONFIDENTIAL`:<br/><br/> Specify `CONFIDENTIAL` when the application requires that data be transmitted so as to prevent other entities from observing the contents of the transmission. (E.g., via TLS.)<br/><br/> Specify `INTEGRAL` when the application requires that the data be sent between client and server in such a way that it cannot be changed in transit.<br/><br/> Specify `NONE` to indicate that the container must accept the constrained requests on any connection, including an unprotected one. |

{%include note.html content=" 
  For more information on how to configure security requirements for a web application in a deployment descriptor, see: [Defining Security Requirements for Web Applications](https://javaee.github.io/tutorial/security-webtier.html#BNCAS){:target='_blank'}.
" %}


**The TDS has been pre-configured to require TLS encryption to access the both the [TDS Remote Management Tool](remote_management_ref.html), and the [TdsMonitor Tool](using_the_tdsmonitor_tool.html).**
You, the administrator, do not have to do anything.

This is the entry in the TDS `web.xml` for the [TDS Remote Management Tool](remote_management_ref.html):

~~~xml
<!-- tdsConfig with HTTPS needed for /admin access  -->
<security-constraint>
  <web-resource-collection>
    <web-resource-name>sensitive read access</web-resource-name>
    <url-pattern>/admin/*</url-pattern>
  </web-resource-collection>
  <auth-constraint>
    <role-name>tdsConfig</role-name>
  </auth-constraint>
  <user-data-constraint>
    <transport-guarantee>CONFIDENTIAL</transport-guarantee>
  </user-data-constraint>
</security-constraint>
~~~

Simple descriptions of the relevant elements in the entry above:

|--------------------------|--------------|
| Configuration            |  Description |
|:-------------------------|:-------------|
| `<security-constraint>`  | Defines the access privileges to a collection of resources using their URL mapping. |
| `<web-resource-collection>` | A list of URL patterns that describe a set of resources to be protected. |
| `<url-pattern>` | The request URI to be protected. |
| `<auth-constraint>` | Specifies whether authentication is to be used and names the roles authorized to perform the constrained requests. |
| `<role-name>` | The role name of one of the security-role elements defined for this web application at the server-level (`${tomcat_home/conf/tomcat-users.xml`) |
| `<user-data-constraint>` | Establishes the data will be transported between client and server will take place over a protected transport layer connection. |
| `<transport-guarantee>` | Choices for type of transport guarantee include `NONE`, `INTEGRAL`, and `CONFIDENTIAL`:<br/><br/> Specify `CONFIDENTIAL` when the application requires that data be transmitted so as to prevent other entities from observing the contents of the transmission. (E.g., via TLS.)<br/><br/> Specify `INTEGRAL` when the application requires that the data be sent between client and server in such a way that it cannot be changed in transit.<br/><br/> Specify `NONE` to indicate that the container must accept the constrained requests on any connection, including an unprotected one. |

{%include note.html content=" 
  For more information on how to configure security requirements for a web application in a deployment descriptor, see: [Defining Security Requirements for Web Applications](https://javaee.github.io/tutorial/security-webtier.html#BNCAS){:target='_blank'}.
" %}

## Accessing TDS Monitoring and Debugging Tools
Other than the compelling security reasons, you will want to enable TLS to take advantage of the [TDS Remote Management Tool](remote_management_ref.html)and the [TdsMonitor Tool](using_the_tdsmonitor_tool.html) monitoring and debugging tools.  

1. Enable TLS in Tomcat
   If Tomcat has not already been configured to run via TLS, follow the tutorial in the previous section to Enable TLS in Tomcat.
2. Modify `${tomcat_home}/conf/tomcat-users.xml` to add the new `tdsConfig` and `tdsMonitor` roles.
   Add these roles to your list of roles:
   
   ~~~xml
   <tomcat-users xmlns="http://tomcat.apache.org/xml"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://tomcat.apache.org/xml tomcat-users.xsd"
                 version="1.0">
   <!--
     NOTE:  By default, no user is included in the "manager-gui" role required
     to operate the "/manager/html" web application.  If you wish to use this app,
     you must define such a user - the username and password are arbitrary. It is
     strongly recommended that you do NOT use one of the users in the commented out
     section below since they are intended for use with the examples web
     application.
   -->
   <!--
     NOTE:  The sample user and role entries below are intended for use with the
     examples web application. They are wrapped in a comment and thus are ignored
     when reading this file. If you wish to configure these users for use with the
     examples web application, do not forget to remove the <!.. ..> that surrounds
     them. You will also need to set the passwords to something appropriate.
   -->
     <role rolename="manager-gui"/>
     <role rolename="tdsConfig"/>
     <role rolename="tdsMonitor"/>
     <user username="admin" 
           password="bb7a2b6cf8da7122125c663fc1585808170b2027677195e0ad121f87b27320ae$1$55003acb56e907b19d29d3b4211dc98c837354690bc90579742d6747efeec4ea" 
           roles="manager-gui, tdsConfig, tdsMonitor"/>
   </tomcat-users>
   ~~~
      
   {%include important.html content="
   Keep in mind: Changes to `${tomcat_home}/conf/tomcat-users.xml` do not take effect until Tomcat is restarted.
   " %}
   
3. Restart Tomcat and access the [TDS Remote Management Tool](http://localhost:8080/thredds/admin/debug){:target="_blank"} in your browser (authenticate with the login/password specified in `${tomcat_home}/conf/tomcat-users.xml`).

   {% include image.html file="tds/tutorial/production_servers/remotemanagementtool.png" alt="TDS Remote Management Tool" caption="" %}


## Resources
* [Qualys SSL Server Test](https://www.ssllabs.com/ssltest/){:target="_blank"}
  is a free online service that analyzes the configuration of any public TLS web server. 
  Note: be sure to check the Do not show the results on the boards box if you do not want your results to be public.
* [TLS/SSL Configuration HOW-TO](https://tomcat.apache.org/tomcat-{{ site.tomcat_version }}-doc/ssl-howto.html){:target="_blank"}
  The Apache Tomcat document detailing how to enable TLS.
* [Tomcat Migration Guide](https://tomcat.apache.org/migration.html){:target="_blank"}
  A document detailing the various changes between Tomcat versions.
* [When are self-signed certificates acceptable?](https://www.sslshopper.com/article-when-are-self-signed-certificates-acceptable.html){:target="_blank"}
  A compelling argument as to why self-signed certificates should not be used in a production environment
