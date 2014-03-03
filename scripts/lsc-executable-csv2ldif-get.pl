#!/usr/bin/perl -w

#===================================================
# Script for LSC executable plugin
#
# Read a CSV and generate LDIF
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
# Get input
#===================================================

my $input = <STDIN>;
my ( $attribute, $value ) = ( $input =~ /^(\w*): (.*)$/ );

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
                  "ERROR Pivot field $csv_pivot_field not present in CSV file";
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

        if ( $columns[$pivotIndex] eq $value ) {
            my $entry = Net::LDAP::Entry->new("cn=line $.");

            my $i = 0;
            while ( $i <= $#columns ) {
                $entry->add( $attributes[$i] => $columns[$i] );
                $i++;
            }

            print $entry->ldif();
        }

    }

    else {

        # Error in parsing
        my $err = $csv->error_input;
        print STDERR "WARN Failed to parse line: $err\n";
    }

    next;
}

close CSV;

exit 0;
