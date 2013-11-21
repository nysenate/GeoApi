#!/bin/bash

PROG=`basename $0`
USER="$(whoami)"
POSTGRES_USER=postgres
GEOAPI_DB=geoapi
GEOCODER_DB=geocoder
GEOAPI_LIVE_DATA="geoapi_live_data.sql"
GEOCACHE_LIVE_DATA="geocache_live_data.sql"

if [ "$USER" == "$POSTGRES_USER" ]; then
  echo "$PROG: ERROR: Run this script using your own user (not postres)." >&2
  exit 1
fi

if [ -z "$2" ]; then
  DATA_DIR="."
else
  DATA_DIR="$2"
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

function prompt() {
  read -r -p "Would you like to proceed? [y/N] " response
  if [[ $response =~ ^([yY][eE][sS]|[yY])$ ]]; then
    return 0;
  else 
    return 1;
  fi
}

function createEmptyFile() {
  FILE_NAME="$1"
  if [ -n "$2" ]; then
    PERM_USER="$2"
  else
    PERM_USER="$USER"
  fi
  touch "$FILE_NAME"
  sudo chown $PERM_USER "$FILE_NAME"
}

function backup() {
  echo "The live data in geoapi will be backed up."
  if prompt; then
    GEOAPI_LIVE_DATA=$DATA_DIR/$GEOAPI_LIVE_DATA
    createEmptyFile $GEOAPI_LIVE_DATA $POSTGRES_USER
    echo "Backing up live GeoApi data into $GEOAPI_LIVE_DATA"
    sudo su $POSTGRES_USER -c "pg_dump -a -t admin -t apiuser $GEOAPI_DB > $GEOAPI_LIVE_DATA"
    sudo su $POSTGRES_USER -c "pg_dump -a -n job -n log $GEOAPI_DB >> $GEOAPI_LIVE_DATA"
    echo "Resetting search_path"
    sudo su $POSTGRES_USER -c "psql -c 'SET search_path=public' $GEOAPI_DB" 
    echo "Backed up "
  else 
    echo "Skipped."
  fi

  echo "The live data in geocoder will be backed up."
  if prompt; then
    GEOCACHE_LIVE_DATA=$DATA_DIR/$GEOCACHE_LIVE_DATA
    createEmptyFile $GEOCACHE_LIVE_DATA $POSTGRES_USER
    echo "Backing up live Geocache data into $GEOCACHE_LIVE_DATA"
    sudo su $POSTGRES_USER -c "pg_dump -a -t cache.geocache $GEOCODER_DB > $GEOCACHE_LIVE_DATA"
    echo "Resetting search_path"
    sudo su $POSTGRES_USER -c "psql -c 'SET search_path=public,tiger,tiger_data' $GEOCODER_DB"    
    echo "Backed up Geocache data."
  else
    echo "Skipped"
  fi
}

function restore() {
  GEOAPI_LIVE_DATA="$DATA_DIR/$GEOAPI_LIVE_DATA"
  if [ -e "$GEOAPI_LIVE_DATA" ]; then
    echo "Found GeoApi Live data backup created on $(date -r $GEOAPI_LIVE_DATA)"
    echo "WARNING: Restoring from this backup will overwrite the following data:"
    
    echo "
    Database: geoapi
    ----------------
    - Admin account, Api Users
    - Senator, Congressional, Assembly member meta data
    - Job processing data
    - Logging data
    "

    if prompt; then
      # The tables are truncated with cascade so any foreign key tables will be truncated too.
      # Make sure to account for this when modifying this portion during database schema changes.
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE log.exception CASCADE' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE log.deployment' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE log.apiRequest CASCADE' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE log.address CASCADE' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE log.services CASCADE' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE log.requestTypes CASCADE' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE log.point CASCADE' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE public.admin CASCADE' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE public.apiUser CASCADE' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE public.assembly' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE public.congressional' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE public.senator CASCADE' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE public.senate CASCADE' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE job.process CASCADE' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE job.status CASCADE' $GEOAPI_DB"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE job.user CASCADE' $GEOAPI_DB"

      sudo su $POSTGRES_USER -c "psql -f $GEOAPI_LIVE_DATA $GEOAPI_DB"
      echo "Resetting search_path"
      sudo su $POSTGRES_USER -c "psql -c 'SET search_path=public' $GEOAPI_DB"
    else 
      echo "Skipped"
    fi
  fi
  if [ -e "$GEOCACHE_LIVE_DATA" ]; then
    echo "Found GeoCache Live data backup created on $(date -r $GEOCACHE_LIVE_DATA)"
    echo "WARNING: Restoring from this backup will overwrite the following data:"
    
    echo "
    Database: geocoder
    -------------------
    - Geocode Cache
    "
    
    if prompt; then
      echo "Truncating and restoring GeoCache data"
      sudo su $POSTGRES_USER -c "psql -c 'TRUNCATE TABLE cache.geocache' $GEOCODER_DB"

      sudo su $POSTGRES_USER -c "psql -f $GEOCACHE_LIVE_DATA $GEOCODER_DB"
      echo "Resetting search_path"
      sudo su $POSTGRES_USER -c "psql -c 'SET search_path=public,tiger,tiger_data' $GEOCODER_DB"
    else
      echo "Skipped"
    fi
  fi 
}

case "$1" in 
-b) backup && echo "Finished";;
-r) restore && echo "Finished";;
*) usage;;
esac
