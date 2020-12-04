---
title: Restrict Tomcat Permissions
last_updated: 2020-10-02
sidebar: admin_sidebar
toc: true
permalink: tomcat_permissions.html
---


## Restrict Permissions In `${tomcat_home}`

We also recommend restricting the permissions of the Tomcat `user/group` within `${tomcat_home}`.

1. Change the user/group ownership `${tomcat_home}` to the `tomcat` user and `tomcat` group:

   ~~~bash
   # cd /usr/local
   # chown -R tomcat:tomcat apache-tomcat-8.5.34
   # ls -l tomcat
   total 148
   drwxr-x--- 2 tomcat tomcat  4096 Oct 24 14:22 bin
   -rw-r----- 1 tomcat tomcat 19539 Sep  4 16:30 BUILDING.txt
   drwx------ 2 tomcat tomcat  4096 Oct 24 15:14 conf
   -rw-r----- 1 tomcat tomcat  6090 Sep  4 16:30 CONTRIBUTING.md
   drwxr-x--- 2 tomcat tomcat  4096 Oct 24 13:29 lib
   -rw-r----- 1 tomcat tomcat 57092 Sep  4 16:30 LICENSE
   drwxr-x--- 2 tomcat tomcat  4096 Oct 24 13:41 logs
   -rw-r----- 1 tomcat tomcat  1726 Sep  4 16:30 NOTICE
   -rw-r----- 1 tomcat tomcat  3255 Sep  4 16:30 README.md
   -rw-r----- 1 tomcat tomcat  7142 Sep  4 16:30 RELEASE-NOTES
   -rw-r----- 1 tomcat tomcat 16262 Sep  4 16:30 RUNNING.txt
   drwxr-x--- 3 tomcat tomcat  4096 Oct 24 14:43 temp
   drwxr-x--- 8 tomcat tomcat  4096 Oct 24 15:36 webapps
   drwxr-x--- 3 tomcat tomcat  4096 Oct 24 13:41 work
   ~~~
   
2. Change the user/ownership of the `${tomcat_home}/conf` directory to be owned by the `root` user, have a group of `tomcat` and have a permission of user/group read only:

    ~~~bash
    # cd /usr/local/tomcat
    # chown -R root conf
    # ls -l 
    total 148
    drwxr-x--- 2 tomcat tomcat  4096 Oct 24 14:22 bin
    -rw-r----- 1 tomcat tomcat 19539 Sep  4 16:30 BUILDING.txt
    drwx------ 2 root   tomcat  4096 Sep  4 16:30 conf
    -rw-r----- 1 tomcat tomcat  6090 Sep  4 16:30 CONTRIBUTING.md
    drwxr-x--- 2 tomcat tomcat  4096 Oct 24 13:29 lib
    -rw-r----- 1 tomcat tomcat 57092 Sep  4 16:30 LICENSE
    drwxr-x--- 2 tomcat tomcat  4096 Oct 24 13:41 logs
    -rw-r----- 1 tomcat tomcat  1726 Sep  4 16:30 NOTICE
    -rw-r----- 1 tomcat tomcat  3255 Sep  4 16:30 README.md
    -rw-r----- 1 tomcat tomcat  7142 Sep  4 16:30 RELEASE-NOTES
    -rw-r----- 1 tomcat tomcat 16262 Sep  4 16:30 RUNNING.txt
    drwxr-x--- 3 tomcat tomcat  4096 Oct 24 14:43 temp
    drwxr-x--- 8 tomcat tomcat  4096 Oct 24 15:36 webapps
    drwxr-x--- 3 tomcat tomcat  4096 Oct 24 13:41 work
    ~~~
   
3. Give the `tomcat` group write/execute permissions for the `${tomcat_home}/conf` directory.

    ~~~bash
    # chmod 750 conf
    # ls -l 
    total 148
    drwxr-x--- 2 tomcat tomcat  4096 Oct 24 14:22 bin
    -rw-r----- 1 tomcat tomcat 19539 Sep  4 16:30 BUILDING.txt
    drwxr-x--- 2 root   tomcat  4096 Sep  4 16:30 conf
    -rw-r----- 1 tomcat tomcat  6090 Sep  4 16:30 CONTRIBUTING.md
    drwxr-x--- 2 tomcat tomcat  4096 Oct 24 13:29 lib
    -rw-r----- 1 tomcat tomcat 57092 Sep  4 16:30 LICENSE
    drwxr-x--- 2 tomcat tomcat  4096 Oct 24 13:41 logs
    -rw-r----- 1 tomcat tomcat  1726 Sep  4 16:30 NOTICE
    -rw-r----- 1 tomcat tomcat  3255 Sep  4 16:30 README.md
    -rw-r----- 1 tomcat tomcat  7142 Sep  4 16:30 RELEASE-NOTES
    -rw-r----- 1 tomcat tomcat 16262 Sep  4 16:30 RUNNING.txt
    drwxr-x--- 3 tomcat tomcat  4096 Oct 24 14:43 temp
    drwxr-x--- 8 tomcat tomcat  4096 Oct 24 15:36 webapps
    drwxr-x--- 3 tomcat tomcat  4096 Oct 24 13:41 work
    ~~~
   
   
4. Change the user/group permissions of the files and subdirectories in `${tomcat_home}/conf` directory. 
   
   (Depending on the web applications you are running and/or your virtual host configurations, Tomcat may create a `${tomcat_home}/conf/Catalina` directory with corresponding subdirectories and files for [context](https://tomcat.apache.org/tomcat-8.5-doc/virtual-hosting-howto.html#Configuring_Your_Contexts){:target="_blank"} information.)  
                                                                                                                                   
    ~~~bash
    # cd /usr/local/tomcat/conf
    # find . -type f -print -exec chmod 440 {} \;
    # find . -type d -print -exec chmod 750 {} \;
    # ls -l 
    total 228
    drwxr-x--- 3 root tomcat   4096 Oct 24 13:41 Catalina
    -r--r----- 1 root tomcat  13548 Sep  4 16:30 catalina.policy
    -r--r----- 1 root tomcat   7746 Sep  4 16:30 catalina.properties
    -r--r----- 1 root tomcat   1338 Sep  4 16:30 context.xml
    -r--r----- 1 root tomcat   1149 Sep  4 16:30 jaspic-providers.xml
    -r--r----- 1 root tomcat   2313 Sep  4 16:30 jaspic-providers.xsd
    -r--r----- 1 root tomcat   3622 Sep  4 16:30 logging.properties
    -r--r----- 1 root tomcat   7511 Sep  4 16:30 server.xml
    -r--r----- 1 root tomcat   1993 Oct 24 15:14 tomcat-users.xml
    -r--r----- 1 root tomcat   2633 Sep  4 16:30 tomcat-users.xsd
    -r--r----- 1 root tomcat 169322 Sep  4 16:30 web.xml
    ~~~
 
5. Change the user/ownership of the `${tomcat_home}/bin` and `${tomcat_home}/lib` directories to be owned by the `root` user and have a group of `tomcat`:
    ~~~bash
    # cd /usr/local/tomcat
    # chown -R root lib
    # chown -R root bin
    # ls -l
    total 144
    drwxr-x--- 2 root   tomcat  4096 Oct 24 17:39 bin
    -rw-r----- 1 tomcat tomcat 19539 Sep  4 16:30 BUILDING.txt
    drwxr-x--- 2 root   tomcat  4096 Sep  4 16:30 conf
    -rw-r----- 1 tomcat tomcat  6090 Sep  4 16:30 CONTRIBUTING.md
    drwxr-x--- 2 root   tomcat  4096 Oct 24 17:38 lib
    -rw-r----- 1 tomcat tomcat 57092 Sep  4 16:30 LICENSE
    drwxr-x--- 2 tomcat tomcat  4096 Sep  4 16:28 logs
    -rw-r----- 1 tomcat tomcat  1726 Sep  4 16:30 NOTICE
    -rw-r----- 1 tomcat tomcat  3255 Sep  4 16:30 README.md
    -rw-r----- 1 tomcat tomcat  7142 Sep  4 16:30 RELEASE-NOTES
    -rw-r----- 1 tomcat tomcat 16262 Sep  4 16:30 RUNNING.txt
    drwxr-x--- 2 tomcat tomcat  4096 Oct 24 17:38 temp
    drwxr-x--- 7 tomcat tomcat  4096 Oct 24 17:39 webapps
    drwxr-x--- 2 tomcat tomcat  4096 Sep  4 16:28 work
    ~~~

    {%include important.html content="
    If you are not planning to use the Tomcat Manager application, you may consider changing the ownership of the files in the `webapps` directory to belong to another under-privileged user.
    " %}

6. Change the user/group permissions of the TDS `$CONTENT_ROOT` directory to give the `tomcat` user and `tomcat` group permissions to write content (`$CONTENT_ROOT` is `/data/content` in this example):
    ~~~bash
    # cd /data
    # chown -R tomcat:tomcat content
    # ls -l
    total 144
    drwxr-x--- 3 tomcat tomcat  4096 Oct 24 17:43 content
    ~~~
