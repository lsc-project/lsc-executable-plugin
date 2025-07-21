# Perl wrappers for LDIF inputs

Presentation
============

This script catches LDIF inputs from LSC executable plugin, and allows you to easily parse data with Net::LDAP API.

This script can be used for ADD, REMOVE, UPDATE and RENAME operations. 

Installation
============

Script is available in the `scripts/` directory of Executable plugin source, and shipped with packages.

You need those prerequisites on the system running the script:

* Perl
* Perl Net::LDAP module

Script must be executable.


Configuration
=============

Script
------

There is one script for all operations (ADD, REMOVE, UPDATE and RENAME).

```xml
  <exec:addScript>/var/lib/lsc/lsc-executable-add-modify-delete-modrdn.pl</exec:addScript>
  <exec:updateScript>/var/lib/lsc/lsc-executable-add-modify-delete-modrdn.pl</exec:updateScript>
  <exec:removeScript>/var/lib/lsc/lsc-executable-add-modify-delete-modrdn.pl</exec:removeScript>
  <exec:renameScript>/var/lib/lsc/lsc-executable-add-modify-delete-modrdn.pl</exec:renameScript>
```

Variables
---------

No variables needed.

Hacking
=======

By default, the script does nothing:

```perl
    if ( $entry->changetype() eq "add" ) {
    }
    if ( $entry->changetype() eq "modify" ) {
    }
    if ( $entry->changetype() eq "delete" ) {
    }
    if ( $entry->changetype() eq "modrdn" or $entry->changetype() eq "moddn" ) {
    }
```

See [Net::LDAP::Entry](https://metacpan.org/dist/perl-ldap/view/lib/Net/LDAP/Entry.pod) POD to see how to get data from the `$entry` object.

You can then write any Perl code or launch any command trough [Perl exec function](https://perldoc.perl.org/functions/exec).

