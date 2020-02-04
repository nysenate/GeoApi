#!/bin/bash

source config.properties
java -Dlog4j.configurationFile="${logFile}" -classpath "${pathToGeoApi}/${classes}:${pathToGeoApi}/${lib}" gov.nysenate.sage.scripts.streetfinder.scripts.InsertSenateCountyCode $1
echo Senate County Codes inserted into the tsv file $1