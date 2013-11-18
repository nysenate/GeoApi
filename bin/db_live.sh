#!/bin/bash

PROG=`basename $0`
USER="$(whoami)"
POSTGRES_USER=postgres
GEOAPI_DB=geoapi
GEOCODER_DB=geocoder
GEOAPI_LIVE_DATA="geoapi_live_data.sql"
GEOCACHE_LIVE_DATA="geocache_live_data.sql"

if [ "$USER" == "$POSTGRES_USER" ]; then
  echo "$PROG: ERROR: Run this script using your own user." >&2
  exit 1
fi

function usage() {
  echo "  Usage: 
  
  $PROG can be used to backup and restore all live database tables in SAGE. For
  example the geocode cache, job tables, and log are all written to regularly 
  by the application and therefore should be dumped periodically.  

  Note: This script is intended to be used on a database that has already been 
        initialized through db_setup.sh.

  Backup: $PROG -b [BACKUP_DIR] 
          where BACKUP_PATH is the directory to store the sql data dumps. 
          Defaults to current directory.
  
  Restore: $PROG -r [RESTORE_DIR]
           where RESTORE_DIR is the same as BACKUP_DIR
           Defaults to current directory.
" >&2
}

if [ $# -lt 1 ]; then
  usage
  exit
fi

function backup() {
  if [ -z "$1" ]; then
    DATA_DIR="."
  else
    DATA_DIR="$1"
  fi

  echo "This script will backup the following data:
    ----------------
    Database: geoapi
    ----------------
    Admin account, Api Users
    Job processing data
    Log tables data

  "
  read -r -p "Would you like to proceed? [y/N] " response
  if [[ $response =~ ^([yY][eE][sS]|[yY])$ ]]; then

    GEOAPI_LIVE_DATA="$DATA_DIR/$GEOAPI_LIVE_DATA"
    touch "$GEOAPI_LIVE_DATA"
    sudo chown $POSTGRES_USER "$GEOAPI_LIVE_DATA"

    echo "Backing up live GeoApi data into $GEOAPI_LIVE_DATA"

    sudo su $POSTGRES_USER -c "pg_dump -a -t admin -t apiuser $GEOAPI_DB > $GEOAPI_LIVE_DATA"
    sudo su $POSTGRES_USER -c "pg_dump -a -n job -n log $GEOAPI_DB >> $GEOAPI_LIVE_DATA"
    sudo su $POSTGRES_USER -c "psql -c 'SET search_path=public' $GEOAPI_DB" 

    echo "Completed the live backup. To restore using this data refer to the db_live_restore.sh script."

  else 

    echo "No data has been backed up."

  fi
}

function restore() {
  echo "moo"
}

case "$1" in 
-b) backup "$2";;
-r) restore "$2";;
*) usage;;
esac

