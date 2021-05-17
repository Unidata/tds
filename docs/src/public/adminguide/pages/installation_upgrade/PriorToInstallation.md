---
title: Prior To Installation
last_updated: 2020-11-30
sidebar: admin_sidebar
toc: true
permalink: prior_to_installation.html
---
Unidata recommends performing the following tasks to prepare your system prior to installing the TDS.

{% capture quick_guide_tip%}
Need help?
Use the [TDS Quick Start Guide](https://docs.unidata.ucar.edu/tds/{{site.docset_version}}/quickstart/){:target='_blank'} to get the TDS up and running quickly.
{% endcapture %}
{% include tip.html content=quick_guide_tip%}


## Review The System Requirements

Consult the [System Requirements](system_requirements.html) before installation to ensure your server environment is compatable for running the TDS.

## Purchase TLS Certificate From A Certificate Authority

The use of HTTPS has been the defacto standard for web traffic since Chrome and other browsers have started denoting HTTP sites as "[Not Secure](https://blog.chromium.org/2018/02/a-secure-web-is-here-to-stay.html){:target='_blank'}"

Unidata **highly recommends** the use of the HTTPS protocol with your TDS, and a certificate signed by a [Certificate Authority (CA)](https://en.wikipedia.org/wiki/Certificate_authority){:target='_blank'}.

{%include important.html content="
A self-signed certificate says to your users \"_Trust me - I am who I say I am._\" While a certificate signed by a CA says, \"_Trust me - the CA agrees I am who I say I am._\"
"%}

## Create Dedicated Tomcat User And Group

### Do Not Run Tomcat As The Super User

The JVM doesn't fork at all, nor does it support `setuid()` calls.
The JVM, and therefore Tomcat, is _one_ process.
The JVM is a virtual machine with many threads under the same process.

Because of OS constraints, all threads in the same JVM process must run under the same user id.
No thread may run as the `root` user unless they are **all** are run as the `root` user.
Hence, any programs run in Tomcat (TDS, manager application, other JSPs and servlets) will run as the `root` user.
  
If you _choose_ to run the Tomcat process as the `root` user, and an attacker manages to exploit a weakness in Tomcat or something running in `${tomcat_home}/webapps/` to run arbitrary commands, those commands will be run as the **superuser**!

We **strongly discourage running Tomcat as the `root` user** and recommend creating an unprivileged, dedicated user and group for running the Tomcat process.

{%include info.html content="
See [Tomcat as root and security issues](https://marc.info/?t=104516038700003&r=1&w=2){:target='_blank'} for a lengthy thread in the tomcat-users mailing list archives dedicated to the perils of running Tomcat as the root user.
"%}
  

### Create A Dedicated User/Group For Running Tomcat

The following example shows creation of a dedicated user/group on a linux system. (Windows and Mac OS users will need to consult their systems administrator regarding user/group creation for those operating systems.)

In this example, both the user and group names will be named `tomcat`, and the user's home directory, a.k.a. `${tomcat_home}`, is `/usr/local/tomcat`.
Both the `groupadd` and `useradd` commands are run as the `root` user:

~~~bash
# groupadd tomcat
# useradd -g tomcat -d /usr/local/tomcat tomcat
~~~
    
You should see and entry for a `tomcat` user in your `/etc/group` file:
    
~~~bash
tomcat:x:2001:
~~~
    
And, something like the following in your `/etc/passwd` file:
    
~~~bash
tomcat:x:25945:2001::/usr/local/tomcat:/bin/bash
~~~

### Create Server Startup/Shutdown Scripts For Tomcat
  
Create the appropriate server startup and shutdown scripts for your server if you intend to run the TDS in a production environment.