# How to write scripts for Executable plugin

Script operations
=================

The Executable plugin runs a script for the following operations:

* LIST: get all entries with their pivot attribute
* GET: get one entry, identified by the pivot attribute, with all attributes
* ADD: add one entry
* UPDATE: modify one entry
* REMOVE: delete one entry
* RENAME: rename one entry

File handles
============

* STDIN: receive messages from LSC, LDIF formatted
* STDOUT: send messages to LSC, LDIF formatted
* STDERR: send log messages to LSC. If log messages are prefixed with `DEBUG: `, `INFO: `, `WARN: ` or `ERROR: `, they will be written to the corresponding log level. If not specified, messages will be written to WARN level.

Return code
===========

* If 0 is returned, this means that the script has succeeded
* If a non zero exit code, this means that the script has failed.

Variables
=========

It is possible to pass variables to script by setting `<exec:variables>` parameters in `lsc.xml`.

They can then be read in the script as environment variables.

Detailed API for each operation
===============================

LIST
----

Nothing is given on STDIN by LSC. The script must send to STDOUT the list of entries under this format:

```ldif
dn: entry1 identifier
pivot1: aaa

dn: entry2 identifier
pivot1: bbb
```

> **_NOTE:_** You can define more than one pivot attributes.

GET
---

For each entry found, LSC call the GET script with all the pivot attributes and their values on STDIN, under this format:

```ldif
pivot1: aaa
pivot2: xxx
```

The script must find the corresponding entry and return the full entry on STDOUT, under this format:

```ldif
dn: entry identifier
attribute1: aaa
attribute2: abc
attribute3: def
```

The script is called with the destination main identifier as argument.

ADD
---

LSC will send on STDIN the LDIF of an entry to add, with changetype `add`:

```ldif
dn: DN
changetype: add
attribute1: aaa
attribute2: abc
attribute3: def
```

Nothing is expected from the script on STDOUT.

The script is called with the destination main identifier as argument.

UPDATE
------

LSC will send on STDIN the LDIF of an entry to modify, with changetype `modify`:

```ldif
dn: DN
changetype: modify
replace: attribute1
attribute1: aaa
-
add: attribute2
attribute2: abc
```

Nothing is expected from the script on STDOUT.

The script is called with the destination main identifier as argument.

REMOVE
------

LSC will send on STDIN the LDIF of an entry to delete, with changetype `delete`:

```ldif
dn: DN
changetype: delete
```

Nothing is expected from the script on STDOUT.

The script is called with the destination main identifier as argument.

RENAME
------

LSC will send on STDIN the LDIF of an entry to rename, with changetype `modrdn`:

```ldif
dn: DN
changetype: modrdn
newrdn: attribute1=aaa
deleteoldrdn: 1
newsuperior: BRANCH
```

Nothing is expected from the script on STDOUT.

The script is called with the destination main identifier as argument.
