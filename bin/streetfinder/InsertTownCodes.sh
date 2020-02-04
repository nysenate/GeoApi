#!/bin/bash

source config.properties
java -Dlog4j.configurationFile="${logFile}" -classpath "${pathToGeoApi}/${classes}:${pathToGeoApi}/${lib}" gov.nysenate.sage.scripts.streetfinder.scripts.InsertTownCode $1
echo Town Codes inserted into the tsv file $1