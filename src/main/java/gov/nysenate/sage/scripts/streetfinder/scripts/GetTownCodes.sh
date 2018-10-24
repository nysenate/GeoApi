#!/bin/bash

source config
java -classpath "${pathToGeoApi}/GeoApi/target/classes/:${pathToGeoApi}/GeoApi/target/${version}/WEB-INF/lib/*" gov.nysenate.sage.scripts.streetfinder.scripts.GetTownCodes
echo Town code file created