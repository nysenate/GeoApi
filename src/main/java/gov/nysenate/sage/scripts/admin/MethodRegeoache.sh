#!/bin/bash

source admin.script.properties
java -classpath "${pathToGeoApi}/GeoApi/target/classes/:${pathToGeoApi}/GeoApi/target/${version}/WEB-INF/lib/*"  gov.nysenate.sage.scripts.admin.MethodRegeocacheCLI $1 $2
echo Method regeocache complete with args $1 $2