#!/bin/bash

USER="$(whoami)"
POSTGRES_USER="postgres"
GEOAPI_DB="geoapi"
GEOCODER_DB="geocoder"
GEOAPI_SCHEMA="geoapi_schema.sql"
GEOCACHE_SCHEMA="geocache_schema.sql"
DISTRICT_SHAPE_DATA="district_shapefiles.sql"
COUNTY_CITY_DATA="county_city_data.sql"
STREETFILE_DATA="streetfile_data.sql"
MEMBER_DATA="member_data.sql"
GEOCACHE_DATA="geocache_data.sql"
TIGER_DATA="tigerline_data.sql"
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function usage(){
  echo "Usage:  ./db_setup DATA_DIRECTORY_PATH"
  echo "The DATA_DIRECTORY_PATH is the directory that contains the .sql components used to populate the databases."
}

if [[ $# -eq 0 ]]; then
	usage
	exit
fi

DATA_DIR="$1"
if [[ ! -d "$DATA_DIR" ]]; then
  echo "The data directory you have specified is not a valid directory!" >&2
  exit
fi 

function check_if_db_exists(){
	if [[ $(psql -l | grep -w "$1" | wc -l) == 1 ]]; then
	  read -p "WARNING: $1 database already exists. Would you like to drop it and re-initialize? [y/n] " -n 1 -r
	  echo
	  if [[ $REPLY =~ ^[Yy]$ ]]; then
	    echo "Dropping $1 database..."
	    drop_status=$(psql -c "DROP DATABASE $1;")
	    if [[ $drop_status != 'DROP DATABASE' ]]; then
	      echo "ERROR: Failed to drop $1 database." >&2
	      return 2
	    fi
	  else 
	    echo "Skipping $1."
	  	return 1
	  fi
  fi
  return 0
}

function create_db(){
	echo 'Creating geoapi database...';
	create_status="$(psql -c "CREATE DATABASE $1")"
	if [[ $create_status == 'CREATE DATABASE' ]]; then
	  echo "Created $1 database."
	  return 0
	else 
	  echo "Failed to create $1 database. Terminating.."
	  return 1
	fi
}

function import_sql(){
	read -p "Importing $3 to database $1 using $2. Proceed? [y/n] " -n 1 -r
	echo
	if [[ $REPLY =~ ^[Yy]$ ]]; then
		if [[ ! -e "$DATA_DIR/$2" ]]; then 
      echo "ERROR: The $2 file needs to exist in this directory in order to proceed." >&2
      return 1
    else 
      psql -X --set "ON_ERROR_STOP=1" -f "$DATA_DIR/$2" -d "$1"
		  if [[ $? != 0 ]]; then
			  echo "Failed to load $3 due to the above error(s)."
			  return 1
		  else
		    echo "Loaded $3 successfully. "
		    return 0
		  fi
    fi  	  
	fi
	echo "Skipping import."
	return 1	
}

function analyze(){
	echo "Performing analyze on database $1."
	psql -c "ANALYZE;" -d "$1"
}

if [[ $USER != $POSTGRES_USER ]]; then
  echo "ERROR: Need to run script as postgres user." >&2
  echo "Switching to postgres user. Run script again."
  sudo su postgres
  exit
fi

if check_if_db_exists $GEOAPI_DB; then
  if create_db $GEOAPI_DB; then
    if import_sql $GEOAPI_DB $GEOAPI_SCHEMA "Geoapi Database Schema"; then
      import_sql $GEOAPI_DB $COUNTY_CITY_DATA "County/City Data"
      import_sql $GEOAPI_DB $DISTRICT_SHAPE_DATA "District Shapefile Data"
      import_sql $GEOAPI_DB $STREETFILE_DATA "BOE Street File Data"
      import_sql $GEOAPI_DB $MEMBER_DATA "Legislative Member Data"
      analyze $GEOAPI_DB
    fi
  fi
fi

if check_if_db_exists $GEOCODER_DB; then
  if create_db $GEOCODER_DB; then
    import_sql $GEOCODER_DB $GEOCACHE_SCHEMA "Geocode Cache Schema"
    import_sql $GEOCODER_DB $GEOCACHE_DATA "Cached Geocode Data"
    if import_sql $GEOCODER_DB $TIGER_DATA "NY Road Census/TIGER Line Data"; then
      echo "Correcting search path..."
      psql -X -c "ALTER DATABASE $GEOCODER_DB SET search_path=public, tiger, tiger_data"
    fi
    analyze $GEOCODER_DB
  fi
fi