#!/bin/bash

line=""
read line
text="$line"

while test "$line" != ""
do
 read line
 text="$text
$line"
done

echo "Failing getting user information for id=$1" 1>&2

exit 1
