---
title: System Requirements
last_updated: 2020-09-20
sidebar: admin_sidebar
toc: false
permalink: system_requirements.html
---


The THREDDS Data Server (TDS) is Java-based web application contained in a single [WAR](https://fileinfo.com/extension/war){:target="_blank"} file, requiring the use of a servlet container such as the open-source Apache [Tomcat](http://tomcat.apache.org/){:target="_blank"} server. 

## Minimum Requirements

* Java Development Kit (JDK), version 8 (64 bit).
* Tomcat 8.5 or above
Use a high-performance file system

## Tested Versions

The TDS is tested using:
* 

## Recommendations

### Hardware
Throw more $$$ at this problem, hardware is cheap, compared to people.

It would be highly unusual for the TDS to not be I/O bound, so buying a high-performance disk subsystem is much better than buying fast CPUs. 
Slower, more energy-efficient multicore processors are optimized for web server loads.

Typically, disk access is much faster on a local drive than on an NFS mounted drive. 
High performance disk subsystems like RAID or SANs will significantly improve TDS throughput.



### Operating System

We highly recommend you use a 64-bit OS if you're planning to run the THREDDS Data Server.

### File System

If you have system admin resources, examine the possible file systems available for your OS. 
We are using the [ZFS](https://zfsonlinux.org/){:target="_blank"} file system on our Linux systems. 


That said, it's been awhile since we did good ol' fashion filesystem bake-off, so if you have found a better solution, please [let us know](mailto:{{site.feedback_email}})!

{% include image.html file="installation_upgrade/bakeoff.png" alt="THIS IS BAKE OFF" caption="" %}


## Supported Versions


## Adequate File Handle Limits

## Dedicated Operating System User Account

## TDM

## LDM

