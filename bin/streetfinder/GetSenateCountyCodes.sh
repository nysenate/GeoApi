#!/bin/bash

source config.properties
java -Dlog4j.configurationFile="${logFile}" -classpath "${pathToGeoApi}/${classes}:${pathToGeoApi}/${lib}" gov.nysenate.sage.scripts.streetfinder.scripts.GetSenateCountyCodes $baseUrl $adminKey
echo Senate county code file created