#!/bin/bash

source admin.script.properties
java -Dlog4j.configurationFile="${logFile}" -classpath "${pathToGeoApi}/${classes}:${pathToGeoApi}/${lib}"  gov.nysenate.sage.scripts.admin.MethodRegeocacheCLI $1 $2
echo Method regeocache complete with args $1 $2