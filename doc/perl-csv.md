# Perl wrappers to read CSV files

Presentation
============

These scripts can be used for LIST and GET operations with the Executable plugin. They read a CSV file and provide to LSC Executable plugin the wanted entries.

Installation
============

Scripts are available in the `scripts/` directory of Executable plugin source, and shipped with packages.

You need those prerequisites on the system running the scripts:

* Perl
* Perl Text::CSV module
* Perl Net::LDAP module

Scripts must be executable.

Configuration
=============

List script
-----------

Declare list script in the plugin service:

```
  <exec:listScript>/var/lib/lsc/lsc-executable-csv2ldif-list.pl</exec:listScript>
```

Get script
----------

Declare get script in the plugin service:

```
  <exec:getScript>/var/lib/lsc/lsc-executable-csv2ldif-get.pl</exec:getScript>
```

Variables
---------

Use plugin services variables to declare the CSV parameters:

```
  <exec:variables>
    <entry><key>CSV_FILE</key><value>/tmp/sample.csv</value></entry>
    <entry><key>CSV_DELIMITER</key><value>;</value></entry>
    <entry><key>CSV_PIVOT_FIELD</key><value>uid</value></entry>
  </exec:variables>
```

Parameters are:

* `CSV_FILE`: Path to CSV file
* `CSV_DELIMITER`: CSV fields delimiter. By default: `,`
* `CSV_PIVOT_FIELD`: Which field is the pivot. By default: `id`

> **_NOTE:_** Only one pivot is managed for the moment
