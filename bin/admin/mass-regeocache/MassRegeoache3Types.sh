#!/bin/bash

#offset, limit, type, subtype

#offset - typically this should start at 0. but if the process fails you can start at another point
#limit - for no limit pass in 0. Otherwise pass in whatever number max limit of records
#useFallback - fall back to other providers if the default provider cant geocode the address
#types - all, method, town, quality, zipcode

#subtypes for all is the provider (All sets the limit to 0, specifying the 5th argument will not do anything)
#subtypes for method is the provider (specifying the 5th argument will not do anything)
#subtypes for town is the town name & provider is optional
#subtypes for qualtiy is the qualty rating found in GeocodeQuality.java & provider is optional
#subtypes for zipcode is the zipcode itself & provider is optional

source ../admin.script.properties
java -Dlog4j.configurationFile="${logFile}" -classpath "${pathToGeoApi}/${classes}:${pathToGeoApi}/${lib}"  gov.nysenate.sage.scripts.admin.MassRegeocacheCLI $1 $2 $3 $4 $5 $6 $7 $8 $9
echo Method regeocache complete with args $1 $2 $3 $4 $5 $6 $7 $8 $9