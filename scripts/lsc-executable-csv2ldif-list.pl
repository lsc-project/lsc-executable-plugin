#!/usr/bin/perl -w

#===================================================
# Script for LSC executable plugin
#
# Read a CSV and generate LDIF
#
#                  ==LICENSE NOTICE==
#
# Copyright (c) 2014, LSC Project
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
#    * Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
# notice, this list of conditions and the following disclaimer in the
# documentation and/or other materials provided with the distribution.
#     * Neither the name of the LSC Project nor the names of its
# contributors may be used to endorse or promote products derived from
# this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
# IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
# TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
# PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
# OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
# PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
# LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
#                  ==LICENSE NOTICE==
#
# Author: Clement Oudot
#===================================================

my $VERSION = 1.0;

use strict;
use Text::CSV;
use Net::LDAP::LDIF;

#===================================================
# Get variables from lsc.xml
#===================================================
# CSV File
my $csv_file = $ENV{CSV_FILE};

# CSV delimiter
my $csv_delimiter = $ENV{CSV_DELIMITER} || ",";

# CSV pivot field
my $csv_pivot_field = $ENV{CSV_PIVOT_FIELD} || "id";

#===================================================
# Open CSV
#===================================================
my $csv = Text::CSV->new(
    {
        sep_char => $csv_delimiter,
        binary   => 1,
    }
);
open( CSV, "<:encoding(utf8)", $csv_file ) or die $!;

my @attributes;
my $pivotIndex;

while (<CSV>) {

    if ( $csv->parse($_) ) {

        # Get attribute names
        if ( $. == 1 ) {
            @attributes = $csv->fields();

            # Check if pivot field exists
            unless ( grep ( /^$csv_pivot_field$/, @attributes ) ) {
                print STDERR
                  "ERROR: Pivot field $csv_pivot_field not present in CSV file";
                exit 1;
            }

            my $i = 0;
            while ( $i <= $#attributes ) {
                if ( $attributes[$i] eq $csv_pivot_field ) {
                    $pivotIndex = $i;
                }
                $i++;
            }
            next;
        }

        my @columns = $csv->fields();

        my $entry =
          Net::LDAP::Entry->new( "cn=line $.",
            $csv_pivot_field => $columns[$pivotIndex], );

        print $entry->ldif();

    }

    else {

        # Error in parsing
        my $err = $csv->error_input;
        print STDERR "WARN: Failed to parse line: $err\n";
    }

    next;
}

close CSV;

exit 0;
