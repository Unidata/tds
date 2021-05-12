---
title: Block Non-Essential Port Access At The Firewall
last_updated: 2020-08-24
sidebar: admin_sidebar
toc: true
permalink: block_nonessential_ports.html
---

## Best Practice

{%include important.html content="
We recommend working with your systems/network administrator to block access to all non-essential ports at the firewall.
" %}

* It is easy to issue commands to Tomcat if you know:
  1. the correct port number; and
  2. the command expected on that port.
* Unless you are on a private network, you need a firewall to restrict who is allowed to access network ports.

## Keep In Mind
* Port `8080` should have unrestricted access unless you plan to [proxy requests to the Tomcat Servlet Container from an HTTP server](tds_behind_proxy.html){:target="_blank"}.
* If you are using any of the TDS monitoring and debugging tools, or the Tomcat Manager application, you must also open up port `8443`.
