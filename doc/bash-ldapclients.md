# Shell wrappers to LDAP clients

Presentation
============

These scripts are Shell (BASH) wrappers that use LDAP clients (ldapsearch, ldapmodify, ...) to communicate with LSC Executable plugin. There is a script for each operation: LIST, GET, ADD, REMOVE, UPDATE and RENAME.

> **_NOTE:_** These scripts are used in Executable plugin unit tests.

The goal is to be able to hack easily these scripts to implement a wanted behaviour in an Executable connector.

Installation
============

Scripts are available in `src/test/resources/org/lsc/plugins/connectors/executable/` directory of Executable plugin source.

You need to have LDAP clients installed, and available in the PATH.

Configuration
=============

Scripts
-------

Declare each needed script in Executable service section:

```xml
  <exec:listScript>/var/lib/lsc/list.sh</exec:listScript>
  <exec:getScript>/var/lib/lsc/get.sh</exec:getScript>
  <exec:addScript>/var/lib/lsc/add.sh</exec:addScript>
  <exec:updateScript>/var/lib/lsc/update.sh</exec:updateScript>
  <exec:removeScript>/var/lib/lsc/delete.sh</exec:removeScript>
  <exec:renameScript>/var/lib/lsc/rename.sh</exec:renameScript>
```

Variables
---------

Use plugin service variables to declare the LDAP parameters:

```xml
  <exec:variables>
    <entry><key>LDAP_BIND_DN</key><value>cn=Directory Manager</value></entry>
    <entry><key>LDAP_BIND_PW</key><value>secret</value></entry>
    <entry><key>LDAP_URL</key><value>ldap://localhost:33389</value></entry>
    <entry><key>LDAP_BASE</key><value>dc=lsc-project,dc=org</value></entry>
    <entry><key>LDAP_SCOPE</key><value>sub</value></entry>
  </exec:variables>
```

