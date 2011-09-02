Executable LSC Plugin
---------------------

This plugin enables any sysadmin to use LSC on a custom source or destination without writing Java code, only by
wrapping each method through a script command.

Scripts must return on the standard output the attended content. Error stream is reserved for messages that must be handle like error
message. A non null return code indicates that the script encountered an error.

The design of a source connector only requires two scripts :
- the LIST script which will