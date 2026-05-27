#!/bin/bash

source admin.script.properties
echo Connecting to Sage to rebuild the Senator cache
curl "${baseUrl}/admin/api/datagen/genmetadata/all?key=${adminKey}"
