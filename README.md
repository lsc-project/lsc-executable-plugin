# Executable LSC Plugin

[![Build Status](https://travis-ci.org/lsc-project/lsc-executable-plugin.svg?branch=master)](https://travis-ci.org/lsc-project/lsc-executable-plugin)

This plugin enables any sysadmin to use LSC on a custom source or destination without writing Java code, only by
wrapping each method through a script command.

Scripts must return on the standard output the attended content. Error stream is reserved for messages that must be handle like error
message. A non null return code indicates that the script encountered an error.

The design of a source connector only requires two scripts :
- the LIST script which provides the entries DN list
- the GET script which returns an full entry based on its DN

The design of a destination connector requires 4 more scripts :
- the ADD script which add a new entry (add operation)
- the UPDATE script which update an existing entry (modify operation)
- the REMOVE script which delete an existing entry (delete operation)
- the RENAME script which change the DN of an existing entry (modrdn operation)

A final combination of LDAP to get data and scripts to update is also available (see executableLdapDestinationService)

Full documentation: http://lsc-project.org/wiki/documentation/plugins/executable
