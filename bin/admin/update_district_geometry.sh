#!/bin/bash

PROG=$(basename "$0")

function usage() {
  echo "Usage: $PROG ZIPFILE DISTRICT_TYPE CODE_COLUMN [NAME_COLUMN]" >&2
  echo "  ZIPFILE        Path to a zip archive containing the district geometry." >&2
  echo "  DISTRICT_TYPE  District type (used as the target table name in the districts schema)." >&2
  echo "  CODE_COLUMN    Name of the code column for districts.type_info." >&2
  echo "  NAME_COLUMN    Optional name column for districts.type_info." >&2
}

if [ $# -lt 3 ] || [ $# -gt 4 ]; then
  usage
  exit 1
fi

ZIPFILE="$1"

if [ ! -f "$ZIPFILE" ]; then
  echo "$PROG: ERROR: $ZIPFILE not found." >&2
  exit 1
fi

if [ -z "$4" ]; then
  SELECT_COLS="$3"
else
  SELECT_COLS="$3,$4"
fi

if ! command -v ogr2ogr >/dev/null 2>&1; then
  echo "$PROG: ERROR: ogr2ogr not found. Install gdal-bin." >&2
  exit 1
fi

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

OGR_ARGS=(
  -f PostgreSQL
  "PG:dbname=geoapi"
  "$DATA_SOURCE"
  -overwrite
  -nln "districts.${2}"
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

# Calls an API endpoint to finish setup, pretty-printing the response.
source "$(dirname "$0")/admin.script.properties"
echo "Calling /updateMap to refresh districts.${2}..."
curl -sS -G "${baseUrl}/admin/api/updateMap" \
  --data-urlencode "key=${adminKey}" \
  --data-urlencode "type=${2}" \
  --data-urlencode "codeColumn=${3}" \
  --data-urlencode "nameColumn=${4}" | jq .
