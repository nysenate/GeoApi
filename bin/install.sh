#!/bin/bash
source $(dirname "$0")/utils.sh

mvn install:install-file -DgroupId=org.json -DartifactId=json -Dversion=20121202 -Dfile=$ROOTDIR/lib/json-20121202.jar -Dpackaging=jar -DgeneratePom=true

