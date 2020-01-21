#!/bin/bash

source admin.script.properties
java -Dlog4j.configurationFile="${logFile}" -classpath "${pathToGeoApi}/${classes}:${pathToGeoApi}/${lib}"  gov.nysenate.sage.scripts.admin.GenerateMetaDataCLI $1
echo GenerateMetaData complete with arg $1