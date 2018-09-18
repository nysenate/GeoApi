#!/bin/bash

source config
java -classpath "${pathToGeoApi}/GeoApi/target/classes/:${pathToGeoApi}/GeoApi/target/geoapi##2.5/WEB-INF/lib/*" gov.nysenate.sage.scripts.StreetFinder.Scripts.GetTownCodes
echo Town code file created