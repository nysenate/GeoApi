#!/bin/bash

source config.properties
java -Dlog4j.configurationFile="${logFile}" -classpath "${pathToGeoApi}/${classes}:${pathToGeoApi}/${lib}" gov.nysenate.sage.scripts.streetfinder.scripts.GetTownCodes $baseUrl $adminKey
echo Town code file created