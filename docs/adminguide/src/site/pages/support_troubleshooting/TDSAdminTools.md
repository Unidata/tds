---
title: Accessing TDS Monitoring and Debugging Tools
last_updated: 2020-10-10
sidebar: admin_sidebar
toc: true
permalink: accessing_tds_admin_tools.html
---


## Accessing TDS Monitoring and Debugging Tools
Other than the compelling security reasons, you will want to enable TLS to take advantage of the [TDS Remote Management Tool](remote_management_ref.html) and the [TdsMonitor Tool](using_the_tdsmonitor_tool.html) monitoring and debugging tools.  



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

{%include note.html content="
For more information on how TLS works, Wikipedia details the [steps involved](https://en.wikipedia.org/wiki/Transport_Layer_Security){:target='_blank'} during an TLS transaction.
" %}
