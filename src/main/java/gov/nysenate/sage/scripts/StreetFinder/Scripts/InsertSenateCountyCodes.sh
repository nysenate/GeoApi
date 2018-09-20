#!/bin/bash

source config
java -classpath "${pathToGeoApi}/GeoApi/target/classes/:${pathToGeoApi}/GeoApi/target/geoapi##2.5.1/WEB-INF/lib/*" gov.nysenate.sage.scripts.StreetFinder.Scripts.InsertSenateCountyCode $1
echo Senate County Codes inserted into the tsv file $1