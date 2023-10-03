# Executable LSC Plugin

[![Build Status](https://travis-ci.org/lsc-project/lsc-executable-plugin.svg?branch=master)](https://travis-ci.org/lsc-project/lsc-executable-plugin)

> **_NOTE:_** Plugin version: 1.0, Minimal required LSC version: 2.1

Presentation
============

This plugin enables any sysadmin to use LSC on a custom source or destination without writing Java code, only by wrapping each method through a script command.

Scripts must return on the standard output the attended content. Error stream is reserved for messages that must be handle like error message. A non null return code indicates that the script encountered an error.

The design of a source connector only requires two scripts :

* the LIST script which provides the entries DN list 
* the GET script which returns an full entry based on its DN

The design of a destination connector requires 4 more scripts :

* the ADD script which adds a new entry (add operation)
* the UPDATE script which updates an existing entry (modify operation)
* the REMOVE script which deletes an existing entry (delete operation)
* the RENAME script which changes the DN of an existing entry (modrdn operation)

There are two different destination connectors:

* Executable LDAP destination service: LIST and GET operations are done with LDAP destination service (see LSC main documentation about *LDAP destination service*)
* Executable LDIF destination service: LIST and GET operations are done by scripts, like the one used in Executable LDIF source service

Installation
============

Get the Executable plugin. Then copy the plugin (.jar file) inside LSC lib directory. (for example `/usr/lib/lsc`)

Configuration
=============

XML namespace
-------------

You need to adapt the namespace of the main markup to import `exec` namespace:

```
<?xml version="1.0" ?>
<lsc xmlns="http://lsc-project.org/XSD/lsc-core-2.1.xsd" xmlns:exec="http://lsc-project.org/XSD/lsc-executable-plugin-1.0.xsd" revision="0">
...
</lsc>
```

Connection
----------

You need to define a fake connection for the plugin:

```
    <pluginConnection>
      <name>executable</name>
      <url>fake</url>
      <username>fake</username>
      <password>fake</password>
    </pluginConnection>
```

Executable LDIF Source Service
------------------------------

The configuration part of the source service is:

```
      <pluginSourceService implementationClass="org.lsc.plugins.connectors.executable.ExecutableLdifSourceService">
        <name>user-src-service</name>
        <connection reference="executable" />
        <exec:executableLdifSourceServiceSettings>
          <name>user-src-service-exec</name>
          <connection reference="executable" />
          <exec:listScript>path/to/listscript</exec:listScript>
          <exec:getScript>path/to/getscript</exec:getScript>
          <exec:variables>
            <entry><key>key</key><value>value</value></entry>
            <entry><key>key2</key><value>value2</value></entry>
          </exec:variables>
        </exec:executableLdifSourceServiceSettings>
      </pluginSourceService>
```

Parameters are:

* `listScript`: Path to the script used to list all entries. The script must be executable.
* `getScript`: Path to the script used to list a specific entry. The script must be executable.
* `variables`: allow to define variables that will be passed as environment variables to scripts.

Executable LDAP Destination Service
-----------------------------------

The configuration part of the destination service is:

```
<pluginDestinationService implementationClass="org.lsc.plugins.connectors.executable.ExecutableLdapDestinationService">
  <name>user-dst-service</name>
  <connection reference="openldap" />
  <exec:executableLdapDestinationServiceSettings>
    <name>user-dst-service-exec</name>
    <connection reference="openldap" />
    <baseDn>ou=users,dc=example,dc=com</baseDn>
    <pivotAttributes>
      <string>uid</string>
    </pivotAttributes>
    <fetchedAttributes>
      <string>cn</string>
      <string>givenName</string>
      <string>objectclass</string>
      <string>sn</string>
      <string>uid</string>
    </fetchedAttributes>
    <getAllFilter><![CDATA[(objectClass=inetOrgPerson)]]></getAllFilter>
    <getOneFilter><![CDATA[(&(objectClass=inetOrgPerson)(uid={uid}))]]></getOneFilter>
    <exec:addScript>path/to/addscript</exec:addScript>
    <exec:updateScript>path/to/updatescript</exec:updateScript>
    <exec:removeScript>path/to/removescript</exec:removeScript>
    <exec:renameScript>path/to/renamescript</exec:renameScript>
    <exec:variables>
      <entry><key>key</key><value>value</value></entry>
      <entry><key>key2</key><value>value2</value></entry>
    </exec:variables>
  </exec:executableLdapDestinationServiceSettings>
</pluginDestinationService>
```

> **_NOTE:_** Here the connection do not refer to fake plugin connection, but to a LDAP connection. (see LSC main documentation about *LDAP connection*)

Parameters are:

* `addScript`: Path to the script used to add an entry. The script must be executable.
* `updateScript`: Path to the script used to update an entry. The script must be executable.
* `removeScript`: Path to the script used to remove an entry. The script must be executable.
* `renameScript`: Path to the script used to rename an entry. The script must be executable.
* `variables`: allow to define variables that will be passed as environment variables to scripts.
* `Other parameters`: see LSC main documentation about *LDAP destination service*.

Executable LDIF Destination Service
-----------------------------------

The configuration part of the destination service is:

```
<pluginDestinationService implementationClass="org.lsc.plugins.connectors.executable.ExecutableLdifDestinationService">
  <name>user-dst-service</name>
  <connection reference="executable" />
  <exec:executableLdifDestinationServiceSettings>
    <name>user-dst-service-exec</name>
    <connection reference="executable" />
    <exec:listScript>path/to/listscript</exec:listScript>
    <exec:getScript>path/to/getscript</exec:getScript>
    <exec:addScript>path/to/addscript</exec:addScript>
    <exec:updateScript>path/to/updatescript</exec:updateScript>
    <exec:removeScript>path/to/removescript</exec:removeScript>
    <exec:renameScript>path/to/renamescript</exec:renameScript>
    <exec:variables>
      <entry><key>key</key><value>value</value></entry>
      <entry><key>key2</key><value>value2</value></entry>
    </exec:variables>
    <exec:fetchedAttributes>
      <string>uid</string>
      <string>nom</string>
      <string>prenom</string>
    </exec:fetchedAttributes>
  </exec:executableLdifDestinationServiceSettings>
</pluginDestinationService>
```

Parameters are:

* `listScript`: Path to the script used to list all entries. The script must be executable.
* `getScript`: Path to the script used to list a specific entry. The script must be executable.
* `addScript`: Path to the script used to add an entry. The script must be executable.
* `updateScript`: Path to the script used to update an entry. The script must be executable.
* `removeScript`: Path to the script used to remove an entry. The script must be executable.
* `renameScript`: Path to the script used to rename an entry. The script must be executable.
* `variables`: allow to define variables that will be passed as environment variables to scripts.
* `fetchedAttributes`: list of destination attributes that should be taken into account for the synchronization.

Plugin loading
==============

To load the plugin into LSC, you need to modify `JAVA_OPTS`:

```
JAVA_OPTS="-DLSC.PLUGINS.PACKAGEPATH=org.lsc.plugins.connectors.executable.generated"
```

For example, to run a user synchronization:
```
JAVA_OPTS="-DLSC.PLUGINS.PACKAGEPATH=org.lsc.plugins.connectors.executable.generated" /usr/bin/lsc -f /etc/lsc/executable/ -s user -t 1
```

> **_NOTE:_** The use of -t 1 limits LSC to one thread.

Scripts
=======

* [How to write your own scripts](./doc/howto-scripts.md)
* Sample scripts:
    * [Shell wrappers to LDAP clients](./doc/bash-ldapclients.md) (GET, LIST, ADD, REMOVE, UPDATE, RENAME)
    * [Perl wrappers to CSV file](./doc/perl-csv.md) (GET, LIST)
    * [Perl wrappers for LDIF inputs](./doc/perl-ldif.md) (ADD, REMOVE, UPDATE, RENAME)

