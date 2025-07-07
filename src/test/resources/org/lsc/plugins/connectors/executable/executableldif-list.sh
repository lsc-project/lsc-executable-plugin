#!/bin/sh

echo "Scanning repository content ..." 1>&2

ldapsearch -x -LLL -H "$LDAP_URL" -D "$LDAP_BIND_DN" -w "$LDAP_BIND_PW" -b "$LDAP_BASE" -s "$LDAP_SCOPE" '(objectClass=person)' 1.1

exit $?
