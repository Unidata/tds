---
title: Installation Of Java And Tomcat
last_updated: 2020-08-24
sidebar: user_sidebar
toc: false
permalink: install_java_tomcat.html
---

This section demonstrates the installation of the Java JDK and the Tomcat Servlet Container binaries.  

{% include note.html content="
Users of OS-provided packages via package management systems for Java and/or Tomcat may want to reference the [THREDDS mailing list](https://www.unidata.ucar.edu/mailing_lists/archives/thredds/){:target='_blank'} for installation help."
%}

## System Requirements

* OpenJDK Java 17
* Apache Tomcat 10.x

While there are different distributors of Java and servlet containers, Unidata develops, uses and tests the THREDDS Data Server using _OpenJDK Java_ and the _Apache Tomcat_ servlet container.


## Installing OpenJDK Java JDK

The following example shows the JDK installation on a linux system.  
The installation is being performed as the `root` user.

{% include note.html content="
For installation of the JDK on Windows or Mac OS, see the [JDK Installation Guide](https://adoptium.net/installation.html){:target='_blank'}.
" %}

1.  [Download](https://adoptium.net/){:target="_blank"} the required OpenJDK version from the AdoptOpenJDK site. 

2.  Install the JDK.

    Copy the binary `tar.gz` file into the installation directory (`/usr/local` in this example):

    ~~~bash
    # pwd
    /usr/local
    
    # cp /tmp/jdk-xx.tar.gz .

    # ls -l
    total 187268
    -rw-r--r-- 1 root root 191757099 Oct 24 13:19 jdk-xx.tar.gz
    ~~~

    Unpack the archive file:

    ~~~bash
    # tar xvfz jdk-xx.tar.gz 
    ~~~

    This will extract the JDK in the installation directory:

    ~~~bash
    # ls -l
    total 187272
    drwxr-xr-x 7 root root      4096 Oct  6 07:58 jdk-xx
    -rw-r--r-- 1 root root 191757099 Oct 24 13:19 jdk-xx.tar.gz
    ~~~

    Remove the remaining binary `tar.gz` file when the installation is complete.
   
    ~~~bash
    # rm jdk-xx.tar.gz
    # ls -l
    total 187279
    drwxr-xr-x 7 root root      4096 Oct  6 07:58 jdk-xx
    ~~~

    {% include important.html content="
    Depending on your OS you may need install either the 32-bit or 64-bit version of the JDK.
    But, we *really, really, really recommend* you use a 64-bit OS if you're planning to run the THREDDS Data Server.
    " %}

<a name="tomcat"></a>
## Installing The Tomcat Servlet Container

The following example shows Tomcat installation on a linux system. 
(This type of installation will work on Mac OS systems as well.) 
The installation is performed as the `root` user.

{% include note.html content="
For installation of Tomcat on Windows, see the [Tomcat Setup Guide](http://tomcat.apache.org/tomcat-10.1-doc/setup.html#Windows){:target='_blank'}.
" %}

1.  [Download](https://tomcat.apache.org/download-10.cgi){:target="_blank"} current version of the Tomcat servlet container.

2.  Install Tomcat as per the Apache Tomcat [installation instructions](http://tomcat.apache.org/tomcat-10.1-doc/setup.html){:target="_blank"}.

    Copy the binary tar.gz file into the installation directory (`/usr/local` in this example):

    ~~~bash
    # pwd
    /usr/local
    
    # cp /tmp/apache-tomcat-xx.tar.gz .

    # ls -l
    total 196676
    -rw-r--r-- 1 root root   9625824 Oct 24 13:27 apache-tomcat-xx.tar.gz
    drwxr-xr-x 7 root root      4096 Oct  6 07:58 jdk-xx
    ~~~

    Unpack the archive file:

    ~~~bash
    # tar xvfz apache-tomcat-xx.tar.gz
    ~~~

    This will create a Tomcat directory:

    ~~~bash
    # ls -l
    total 196680
    drwxr-xr-x 9 root root      4096 Oct 24 13:29 apache-tomcat-xx
    -rw-r--r-- 1 root root   9625824 Oct 24 13:27 apache-tomcat-xx.tar.gz
    drwxr-xr-x 7 root root      4096 Oct  6 07:58 jdk-xx
    ~~~

    Remove the remaining binary `tar.gz` file when the installation is complete.
   
    ~~~bash
    # rm apache-tomcat-xx.tar.gz
    # ls -l
    total 187282
    drwxr-xr-x 9 root root      4096 Oct 24 13:29 apache-tomcat-xx
    drwxr-xr-x 7 root root      4096 Oct  6 07:58 jdk-xx
    ~~~

## Create Symbolic Links

Adding symbolic links for both the Tomcat and the JDK installations will allow for upgrades of both packages without having to change to configuration files and server startup/shutdown scripts.

The following example shows creating symbolic links for the Tomcat and JDK installation on a linux system. 
(This type of installation will work on Mac OS systems as well.) 
The installation is performed as the `root` user.

{%include note.html content="
Windows users can consult the [Microsoft Documentation](https://docs.microsoft.com/en-us/windows/win32/fileio/symbolic-links){:target='_blank'} for creating symbolic links on Windows systems.
" %}

1. Create symbolic links for the Tomcat and the JDK installations:

    ~~~ bash
    # pwd
    /usr/local
    
    # ln -s apache-tomcat-xx tomcat 
    # ln -s jdk-xx jdk
    # ls -l 
    total 196684
    drwxr-xr-x 9 root root      4096 Oct 24 13:29 tomcat -> apache-tomcat-xx
    drwxr-xr-x 9 root root      4096 Oct 24 13:29 apache-tomcat-xx
    lrwxrwxrwx 1 root root        12 Oct 24 13:59 jdk -> jdk-xx
    drwxr-xr-x 7 root root      4096 Oct  6 07:58 jdk-xx
    ~~~

## Next Step

Next, we'll do a quick tour of the relevant elements of the [Tomcat Directory Structure](tomcat_dir_structure_qt.html) and how these elements relate to the TDS.
