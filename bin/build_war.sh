#!/bin/sh
#
# build_war.sh - Simple script to build the SAGE WAR file
#
# Project: GeoApi / SAGE
# Author: Ken Zalewski
# Organization: New York State Senate
# Date: 2012-01-08
#

script_dir=`dirname $0`
base_dir=`cd $script_dir/..; echo $PWD`
temp_dir=/tmp/$USER-build_war
class_dir="$temp_dir/WEB-INF/classes"

cleanup() {
  rm -rf "$temp_dir"
}

mkdir -p "$temp_dir"

cd "$base_dir"

echo "Copying web application skeleton"
cp -a src/main/webapp/* "$temp_dir" || exit 1

echo "Compiling Java source"
mkdir -p "$class_dir"
make TARGET_DIR="$class_dir" || exit 1

echo "Copying resources"
cp -a src/main/resources/* "$class_dir" || exit 1

echo "Copying local JAR libraries"
cp -a $HOME/lib "$temp_dir/WEB-INF/"

echo "Creating WAR file"
pushd "$temp_dir"
zip -r $HOME/GeoApi.war . || exit 1
popd

cleanup
exit 0
