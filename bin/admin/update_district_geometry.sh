#!/bin/bash

PROG=$(basename "$0")

function usage() {
  echo "Usage: $PROG ZIPFILE DISTRICT_TYPE" >&2
  echo "  ZIPFILE        Path to a zip archive containing the district geometry." >&2
  echo "  DISTRICT_TYPE  District type (used as the target table name in the districts schema)." >&2
  echo "The script will inspect the dataset and prompt for the code and name columns." >&2
}

if [ $# -ne 2 ]; then
  usage
  exit 1
fi

ZIPFILE="$1"

if [ ! -f "$ZIPFILE" ]; then
  echo "$PROG: ERROR: $ZIPFILE not found." >&2
  exit 1
fi

for cmd in ogr2ogr ogrinfo jq psql; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "$PROG: ERROR: $cmd not found." >&2
    exit 1
  fi
done

ZIPFILE_ABS=$(readlink -f "$ZIPFILE")

# GDAL's /vsizip/ doesn't recurse into subdirectories, so if the geospatial
# file is nested, point at it explicitly. Match common OGR-readable formats.
INNER=$(unzip -Z1 "$ZIPFILE_ABS" \
  | grep -iE '\.(shp|geojson|json|gpkg|kml|gml|tab|gdb)$' \
  | head -n1)
DATA_SOURCE="/vsizip/${ZIPFILE_ABS}"
if [ -n "$INNER" ] && [ "$INNER" != "$(basename "$INNER")" ]; then
  DATA_SOURCE="${DATA_SOURCE}/${INNER}"
fi

echo "Available fields:"
ogrinfo -json -so -al "$DATA_SOURCE" | jq -r '.layers[0].fields[].name'

read -r -p "Code column in file: " FILE_CODE_COLUMN
read -r -p "Name column in file (blank to skip): " FILE_NAME_COLUMN

if [ -z "$FILE_NAME_COLUMN" ]; then
  SELECT_COLS="$FILE_CODE_COLUMN"
else
  SELECT_COLS="$FILE_CODE_COLUMN,$FILE_NAME_COLUMN"
fi

DISTRICT_TYPE="$2"
OGR_ARGS=(
  -f PostgreSQL
  "PG:dbname=geoapi"
  "$DATA_SOURCE"
  -overwrite
  -nln "districts.${DISTRICT_TYPE}"
  # Promote single polygons to multi so mixed-geometry data loads cleanly.
  -nlt PROMOTE_TO_MULTI
  # Reproject from the data's native coordinate reference system to WGS84.
  -t_srs EPSG:4326
  # Simplifies column names.
  -lco GEOMETRY_NAME=geom
  -lco FID=gid
  # Drop every attribute column except the code (and optional name) column.
  -select "$SELECT_COLS"
)
ogr2ogr "${OGR_ARGS[@]}"
if [ $? -ne 0 ]; then
  echo "$PROG: ERROR: ogr2ogr load failed." >&2
  exit 1
fi

read -r -p "Code column in SQL (blank to keep as-is): " CODE_RENAME
if [ -n "$FILE_NAME_COLUMN" ]; then
  read -r -p "Name column in SQL (blank to keep as-is): " NAME_RENAME
fi

TABLE="districts.${DISTRICT_TYPE}"
# ogr2ogr's LAUNDER lowercases column names, so convert this to lowercase too.
SQL_CODE_COLUMN="${FILE_CODE_COLUMN,,}"
if [ -n "$CODE_RENAME" ]; then
  psql -d geoapi -c "ALTER TABLE $TABLE RENAME COLUMN \"$SQL_CODE_COLUMN\" TO $CODE_RENAME;" || exit 1
  SQL_CODE_COLUMN="$CODE_RENAME"
fi
SQL_NAME_COLUMN="${FILE_NAME_COLUMN,,}"
if [ -n "$NAME_RENAME" ]; then
  psql -d geoapi -c "ALTER TABLE $TABLE RENAME COLUMN \"$SQL_NAME_COLUMN\" TO $NAME_RENAME;" || exit 1
  SQL_NAME_COLUMN="$NAME_RENAME"
fi

# Calls an API endpoint to finish setup, pretty-printing the response.
source "$(dirname "$0")/admin.script.properties"
echo "Calling /updateMap to refresh districts.${DISTRICT_TYPE}..."
curl -sS -G "${baseUrl}/admin/api/updateMap" \
  --data-urlencode "key=${adminKey}" \
  --data-urlencode "type=${DISTRICT_TYPE}" \
  --data-urlencode "codeColumn=${SQL_CODE_COLUMN}" \
  --data-urlencode "nameColumn=${SQL_NAME_COLUMN}" | jq .
