#!/bin/bash

source config.properties
java -classpath "${pathToGeoApi}/GeoApi/target/classes/:${pathToGeoApi}/GeoApi/target/${version}/WEB-INF/lib/*" gov.nysenate.sage.scripts.streetfinder.scripts.InsertTownCode $1
echo Town Codes inserted into the tsv file $1