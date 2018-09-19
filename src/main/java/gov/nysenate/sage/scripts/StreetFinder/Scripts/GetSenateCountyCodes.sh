#!/bin/bash

source config
java -classpath "${pathToGeoApi}/GeoApi/target/classes/:${pathToGeoApi}/GeoApi/target/geoapi##2.5.1/WEB-INF/lib/*" gov.nysenate.sage.scripts.StreetFinder.Scripts.GetSenateCountyCodes
echo Senate county code file created