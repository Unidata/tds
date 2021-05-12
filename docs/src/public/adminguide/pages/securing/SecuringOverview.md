---
title: Securing The TDS Overview
last_updated: 2020-08-26
sidebar: admin_sidebar
toc: true
permalink: securing_tds_overview.html
---

##  What This Section Covers

This section contains industry standard best practices and recommendations to secure the THREDDS Data Server for use in a production environment.

## Why Is Security Important?

Misconfiguration of Tomcat or the TDS can introduce security vulnerabilities in your production server environment. 
The following recommendations should be considered "layers" of security: not completely effective by themselves, but more potent when combined.

**This is not a complete laundry list of security fixes!**
 Please use it as a starting point when securing your server.

### Reporting A Security Issue
To report a potential security-related issue or bug, contact: <security@unidata.ucar.edu>

## Recommendations
Unidata strongly recommends performing the following tasks if you intent to run the TDS in a production environment:

1. [Enable TLS/SSL Encryption](enable_tls_encryption.html)
2. [Use Digested Passwords](digested_passwords.html)
3. [Secure Tomcat Manager Application](secure_manager_app.html)
4. [Remove Unused Web Applications](remove_unused_webapps.html)
5. [Block Non-Essential Ports](block_nonessential_ports.html)
6. [Keep Software Up-To-Date](keep_software_uptodate.html)

## Restricting Access To The TDS

You can [restrict access](restrict_access_to_tds.html) to specific datasets or the TDS application as a whole:
* [Limit Access To Entire TDS By IP/Host](restrict_access_to_tds.html#limit-access-to-entire-tds-by-iphost)
* [Limit Access To Parts Or Entire TDS By User/Role](restrict_access_to_tds.html#limit-access-to-parts-or-entire-tds-by-userrole)
* [Limit Access To Specific Dataset By User/Role](restrict_access_to_tds.html#limit-access-to-specific-dataset-by-userrole)

## Modifying The Security Manager

The JVM Security Manager that comes with Tomcat imposes a fine-grained security restrictions to all Java applications running the JVM.
It confines the Java applications in a sandbox, and restricts them from utilizing certain features of the Java language Tomcat normally is able to access.


If you are hosting untrusted servlets or JSP on your server, then implementing the Security Manager may be a good idea _if you know what you are doing_.
Be aware the Security Manager may prevent trusted web applications (like the TDS) from performing certain functions if configured too restrictively.

**Most likely, you will have not any need to perform these adjustments.**

{%include info.html content="
Please reference the Oracle [Security Manager](https://docs.oracle.com/javase/tutorial/essential/environment/security.html){:target='_blank'} documentation if you choose to go down this dark path.
" %}





