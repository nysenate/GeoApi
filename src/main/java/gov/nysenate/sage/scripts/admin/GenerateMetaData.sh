#!/bin/bash

source admin.script.properties
java -classpath "${pathToGeoApi}/GeoApi/target/classes/:${pathToGeoApi}/GeoApi/target/${version}/WEB-INF/lib/*"  gov.nysenate.sage.scripts.admin.GenerateMetaDataCLI $1
echo GenerateMetaData complete with arg $1