#!/bin/bash

source config
java -classpath "${pathToGeoApi}/GeoApi/target/classes/:${pathToGeoApi}/GeoApi/target/geoapi##2.5.1/WEB-INF/lib/*" gov.nysenate.sage.scripts.streetfinder.scripts.InsertTownCode $1
echo Town Codes inserted into the tsv file $1