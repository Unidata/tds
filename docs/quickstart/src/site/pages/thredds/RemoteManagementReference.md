---
title: Remote Management Reference
last_updated: 2020-08-21
sidebar: quickstart_sidebar
toc: false
permalink: remote_management_ref.html
---

## Introduction

It's very convenient to be able to remotely manage and administer the Tomcat web server, and to remotely configure and debug the THREDDS Data Server, for example from a web browser running on your desktop.
However, remote access to the server introduces potential security problems, so by default these capabilities are not turned on.
You can run Tomcat and TDS quite successfully by editing the configuration files on the server, and restarting when needed.

Managing a server is difficult, and we recommend that you enable remote management.
By following the procedures here, you can do so without opening any big security holes.
However, you must decide this yourself, based on your organization's security policies, and a risk assessment for your server.
In what follows we try to explain what risks the various options have, as well as we understand them.
A good compromise may be to do all the work to enable remote management, then turn it on only while actively configuring the server, and turn it off when in production mode.

In any case, we strongly recommend that you also read and follow the guidelines in the [Putting TDS Into Production](tomcat_permissions.html) section of this tutorial.

Follow the [checklist](installation_checklist.html) for more concise and up-to-date configuration instruction.

## Configuring Tomcat Users

Special permissions (like remote management) are done in Tomcat by creating users with special roles.
As long as you also follow the Tomcat/TDS Security guidelines, using the simplest Tomcat mechanism to do this should be safe.

Edit `${tomcat_home}/conf/tomcat-users.xml`, adding roles `tdsConfig`, `manager`, `admin`, and users who have those roles, e.g.:

~~~xml
<?xml version='1.0' encoding='utf-8'?>
<tomcat-users>
  <role rolename="manager"/>
  <role rolename="admin"/>
  <role rolename="tdsConfig"/>

  <user username="admin" password="adminpassword" roles="admin, manager-gui, manager"/>
  <user username="yername" password="yerpassword" roles="tdsConfig"/>
</tomcat-users>
~~~

The `manager`, `manager-gui`, and `admin` roles are used within Tomcat itself to allow the use of the manager and administrator web interface.
The `tdsConfig` role is used to configure the TDS.
These roles must be specified exactly as shown.
Note that all 3 of these roles are independent - you can add any, all or none of them.
The easiest way to enable or disable remote administration is to change this file and restart Tomcat.

The list of users, their names and passwords, are whatever you want them to be.
After you get this set up, you can manage users remotely through the administrator interface.
Before you go into production mode, you **should** change to using [digest passwords](digested_passwords.html).

{%include note.html content="
Remember that any changes to the `tomcat-users.xml` file won't take effect until you restart Tomcat.
" %}



**Higher Security**:
You can also use an LDAP server or a Database to store users and roles, which may give you higher levels of security.
Use of this feature is beyond the scope of this documentation, however.

## Enable TLS/SSL Encryption

We ensure that no one can intercept and read sensitive information to and from the server (through doing what's called network sniffing) by encrypting the information using [SSL/TLS encryption](enable_tls_encryption.html)

## TDS Remote Debugging

Once TLS/SSL is enabled, you can remotely debug and configure the TDS.
You need to login with a user who has the `tdsConfig` role.

Debugging information is available at [http://localhost:8080/thredds/admin/debug](http://localhost:8080/thredds/admin/debug){:target="_blank"}.

{% include image.html file="tds/reference/remote_management/TdsDebug.png" alt="TDS Remote Management" caption="" %}

Some capabilities of particular interest are:

* `Show Tomcat Logs`: allows you to look at the Tomcat logs in `${tomcat_home}/logs`
* `Show TDS Logs`: allows you to look at the TDS logs in `${tomcat_home}/content/thredds/logs`
* `Show static catalogs`: list all the static (non-dynamic) catalogs read in at startup
* `Show data roots`: list all the dataRoots with links to the directories they are mapped to
* `Show File Object Caches`: Show all files currently in the object caches
* `Clear File Object Caches`: Remove all unlocked files in the object caches
