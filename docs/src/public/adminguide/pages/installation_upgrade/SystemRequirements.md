---
title: System Requirements
last_updated: 2020-09-20
sidebar: admin_sidebar
toc: false
permalink: system_requirements.html
---

## Minimum Requirements

* OpenJDK Java 8 or above
* Apache Tomcat 8.5 or above (or a servlet contain that supports servlet specification 3.1)
* 64-bit operating system
* High-performance file system


## What We Use At Unidata

Unidata develops, tests, and runs/deploys the THREDDS Data Server using:
 
* OpenJDK Java 
* Apache Tomcat servlet container
* Linux OS
* [ZFS](https://zfsonlinux.org/){:target="_blank"} for Linux systems


## A Note About Hardware

Throw more $$$ at this problem, hardware is cheap, compared to people.

It would be highly unusual for the TDS to not be I/O bound, so buying a high-performance disk subsystem is much better than buying fast CPUs. 
Slower, more energy-efficient multicore processors are optimized for web server loads.

Typically, disk access is much faster on a local drive than on an NFS mounted drive. 
High performance disk subsystems like RAID or SANs will significantly improve TDS throughput.

## Recommendations

Please review the [performance tips](performance_tips.html) for more information regarding OS and file system tuning.

