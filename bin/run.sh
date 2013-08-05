#!/bin/bash
source $(dirname "$0")/utils.sh

BASE="$ROOTDIR/target/GeoApi-$VERSION/WEB-INF"

case $1 in
    --help | -h | help) echo "USAGE: `basename $0` SCRIPT_CLASS_NAME|help <args>"; exit;;
esac

# TODO: This memory size should be an adjustable parameter
java -Xmx1024m -Xms16m -cp $BASE/classes/:$BASE/lib/* gov.nysenate.sage.scripts.$@

