---
title: System Requirements
last_updated: 2020-09-20
sidebar: admin_sidebar
toc: true
permalink: system_requirements.html
---

## Minimum Requirements

* OpenJDK Java {{ site.java_version }} or above
* Apache Tomcat {{ site.tomcat_version }} or above (or a servlet container that supports servlet specification {{ site.servlet_spec }})
* 64-bit operating system
* High-performance file system


## What We Use At Unidata

Unidata develops, tests, and runs/deploys the THREDDS Data Server using:
 
* Eclipse Temurin 
* Apache Tomcat servlet container
* Linux OS
* [ZFS](https://zfsonlinux.org/){:target="_blank"} for Linux systems

{% include note.html content="
Reference the TDS [GitHub Actions](https://github.com/Unidata/tds/blob/master/.github/workflows/tds.yml#L21-L32){:target='_blank'} for the current TDS testing environment.
"%}

## A Note About Hardware

Throw more $$$ at this problem, hardware is cheap, compared to people.

It would be highly unusual for the TDS to not be I/O bound, so buying a high-performance disk subsystem is much better than buying fast CPUs. 
In specific terms, if a given system is frequently serving the same data over and over (because everyone wants the current data), more memory is better and cheaper than storage which is very expensive.  
Caveat: It's a challenge to make a default statement without understanding the specific use case, so please feel free to [consult with Unidata](mailto:{{site.feedback_email}}) if you have any questions.

Typically, disk access is much faster on a local drive than on an NFS mounted drive. 
High performance disk subsystems like RAID or SANs will significantly improve TDS throughput.

{% include info.html content="
Please review the [performance tips](performance_tips.html) for more information regarding OS and file system tuning.
"%}
