#!/bin/bash

source config
java -classpath "${pathToGeoApi}/GeoApi/target/classes/:${pathToGeoApi}/GeoApi/target/geoapi##2.5/WEB-INF/lib/*" gov.nysenate.sage.scripts.StreetFinder.Scripts.InsertTownCode $1
echo Town Codes inserted into the tsv file $1