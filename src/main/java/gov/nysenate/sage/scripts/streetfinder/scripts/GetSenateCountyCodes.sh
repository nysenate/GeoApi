#!/bin/bash

source config.properties
java -classpath "${pathToGeoApi}/GeoApi/target/classes/:${pathToGeoApi}/GeoApi/target/${version}/WEB-INF/lib/*" gov.nysenate.sage.scripts.streetfinder.scripts.GetSenateCountyCodes $baseUrl
echo Senate county code file created