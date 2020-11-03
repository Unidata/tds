---
title: Tomcat Configuration Files
last_updated: 2020-08-24
sidebar: admin_sidebar
toc: false
permalink: tomcat_configuration_files.html
---

This section examines two of the Tomcat Configuration Files (found in the `${tomcat_home}/conf` directory) and how to modify them for use with the TDS.

{%include note.html content="
This section assumes you have successfully installed the JDK and Tomcat Servlet Container as outlined in the [Installation of Java and Tomcat](install_java_tomcat.html) section.
" %}

## `${tomcat_home}/conf/server.xml`

* XML file (well-formed syntax is important).
* Tomcat's main configuration file.
* Changes to `server.xml` do not take effect until Tomcat is restarted.
* Where we make changes to enhance TDS security.

#### Important elements in `server.xml`

Examine the elements in `server.xml`.
Move into the `${tomcat_home}/conf` directory and examine the `server.xml` file (`${tomcat_home}` is `/usr/local` in this example):

~~~bash
# cd /usr/local/tomcat/conf
# less server.xml
~~~


|-----------------|-----------|-------------------------------|
| Tag Name  | Instances | How it relates to the TDS     |
|:----------------|:---------:|:------------------------------|
| [`<Server>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/config/server.html "The Server element represents the entire Catalina servlet container as a whole. It is the single outermost element in server.xml"){:target="_blank"} | 1...1 | Not modified unless you want to change the port number Tomcat listens for a `SHUTDOWN` command. (Enabled by default.) |
| &nbsp;&nbsp; [`<GlobalNamingResources>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/config/globalresources.html "The GlobalNamingResources element defines the global Java Naming and Directory Interface (JNDI) resources for the Server."){:target="_blank"} | 0...\* | Needed to contain the `UserDatabase` that corresponds to the `UserDatabaseRealm` used to authenticate users. (Enabled by default.) |
| &nbsp; &nbsp; &nbsp; &nbsp; [`<Resource>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/config/resources.html "The Resource element represents a static resource from which classes will be loaded and static files will be served."){:target="_blank"} | 0...\* | Editable user database (`tomcat-users.xml`) used by `UserDatabaseRealm` to authenticate users. (`UserDatabaseRealm` Resource enabled by default.) |
| &nbsp; &nbsp; [`<Service>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/config/service.html "The Service element represents the combination of one or more Connector components that share a single Engine component for processing incoming requests. The top Tomcat service is named Catalina (hence the log file name of catalina.out)."){:target="_blank"} | 1...\* | Not modified unless `you` wish to establish more than one service. (Catalina Service enabled by default.) |
| &nbsp; &nbsp; &nbsp; &nbsp; [`<Connector>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/connectors.html "The Connector element forward requests to the Engine using a specific protocol and returns the results to the requesting client."){:target="_blank"} | 1...\* | Used to establish HTTP and SSL connections. Also will communicate with an web server for proxying requests. (HTTP connector enabled by default on port 8080.) |
| &nbsp; &nbsp; &nbsp; &nbsp; [`<Engine>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/config/engine.html "The Engine element represents the entire request processing machinery associated with a particular Catlina Service."){:target="_blank"} | 1...1 | Not modified unless you specify a Host other than `localhost`. (Enabled by default.) |
| &nbsp; &nbsp; &nbsp; &nbsp; [`<Realm>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/config/realm.html "The Realm element represents a database of usernames, passwords, and roles (groups) assigned to those users."){:target="_blank"} | 0...\* | The `UserDatabaseRealm` uses the `UserDatabase` configured in the global JNDI Resource. (`UserDatabaseRealm` enabled by default.) |
| &nbsp; &nbsp; &nbsp; &nbsp; [`<Valve>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/config/valve.html "The Valve element represents a component that will be inserted into the request processing pipeline for the associated containing element."){:target="_blank"} | 0...\* | The `RemoteAddrValve` is used to filter access to the TDS based on IP address. (NOT enabled by default. You will need to add this if you want to use IP Filtering.) |
| &nbsp; &nbsp; &nbsp; &nbsp; [`<Host>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/config/host.html "The Host element represents a virtual host."){:target="_blank"} | 1...\* | Not modified unless you specify a `Host` other than `localhost`. (`localhost` enabled by default.) |
| &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;[`<Valve>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/config/valve.html "The Valve element represents a component that will be inserted into the request processing pipeline for the associated containing element."){:target="_blank"} | 0...\* | We modify the `AccessLogValve` to customize the access logs generated by Tomcat. (NOT enabled by default. You will need to add this if you want to enable access logging. ) |

## `${tomcat_home}/conf/tomcat-users.xml`

* XML file (well-formed syntax is important).
* Stores user names, passwords and roles.
* Changes to `tomcat-users.xml` do not take effect until Tomcat is restarted.
* What the TDS uses for user authentication and access control.


#### Important elements in `tomcat-users.xml`

Examine the elements in `tomcat-users.xml`:

~~~bash
# pwd
/usr/local/tomcat/conf

# less tomcat-users.xml
~~~

Reference the table below to see how the tomcat-users.xml elements relate to configuring TDS (mouse-over the element for a description):

|----------|-----------|---------------------------|
| Tag Name | Instances | How it relates to the TDS |
|:---------|:---------:|:--------------------------|
| [`<tomcat-users>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/realm-howto.html#UserDatabaseRealm "The tomcat-users element represents the single outermost element in tomcat-users.xml"){:target="_blank"} | 1...1 | Not modified. (The only tag you get by default.) |
| &nbsp; &nbsp; [`<role>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/realm-howto.html#UserDatabaseRealm "The role element defines one role or group a user can belong to."){:target="_blank"} | 1...\* | You will have at least two of these: one for the Tomcat manager application and one for the TDS. (You will need to add if you want to enable role-based authentication.) |
| &nbsp; &nbsp; [`<user>`](http://tomcat.apache.org/tomcat-{{site.tomcat_version}}-doc/realm-howto.html#UserDatabaseRealm "The user element represents one valid user."){:target="_blank"} | 1...\* | You will need to create an entry for each user who needs access to the Tomcat manager application and/or the restricted areas of the TDS. (You will need to add if you want to enable user authentication.) |


## Next Step

Next, we'll see how to obtain and [deploy the TDS](deploying_the_tds.html) in the Tomcat Servlet Container.