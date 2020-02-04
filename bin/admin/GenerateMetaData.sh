#!/bin/bash

source admin.script.properties
java -Dlog4j.configurationFile="${logFile}" -classpath "${pathToGeoApi}/${classes}:${pathToGeoApi}/${lib}"  gov.nysenate.sage.scripts.admin.GenerateMetaDataCLI $1
echo Connecting to Sage to rebuild the Senator cache
curl "${baseUrl}/admin/datagen/rebuild/sencache?key=${adminKey}"
echo  GenerateMetaData complete with arg $1