#!/bin/bash

PROG=$(basename "$0")

function usage() {
  echo "Usage: $PROG SOURCE DISTRICT_TYPE" >&2
  echo "  SOURCE         Either a path to a zip archive containing the district geometry," >&2
  echo "                 or the URL of an ArcGIS REST feature service layer, e.g." >&2
  echo "                 https://<host>/arcgis/rest/services/<service>/FeatureServer/6" >&2
  echo "  DISTRICT_TYPE  District type (used as the target table name in the districts schema)." >&2
  echo "The script will inspect the dataset and prompt for the code and name columns." >&2
}

if [ $# -ne 2 ]; then
  usage
  exit 1
fi

SOURCE="$1"
DISTRICT_TYPE="$2"

REQUIRED_CMDS=(ogr2ogr ogrinfo jq psql curl)
case "$SOURCE" in
  http://*|https://*) IS_URL=true ;;
  *) IS_URL=false; REQUIRED_CMDS+=(unzip) ;;
esac

for cmd in "${REQUIRED_CMDS[@]}"; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "$PROG: ERROR: $cmd not found." >&2
    exit 1
  fi
done

source "$(dirname "$0")/admin.script.properties"
if ! curl -fsS "${baseUrl}/ping" >/dev/null; then
  echo "$PROG: ERROR: server at ${baseUrl} is not responding." >&2
  exit 1
fi

if $IS_URL; then
  # GDAL's ESRIJSON driver reads a feature service's /query endpoint directly and pages
  # through it automatically, so there's nothing to download by hand. Services cap each
  # response at maxRecordCount (typically 1000) regardless of what's requested.
  case "$SOURCE" in
    # Already a full query, so a hand-tuned where clause is passed through untouched.
    *"/query?"*) QUERY_URL="$SOURCE" ;;
    *) QUERY_URL="${SOURCE%/}/query?where=1=1&outFields=*&outSR=4326&f=json" ;;
  esac
  DATA_SOURCE="ESRIJSON:${QUERY_URL}"
else
  if [ ! -f "$SOURCE" ]; then
    echo "$PROG: ERROR: $SOURCE not found." >&2
    exit 1
  fi
  ZIPFILE_ABS=$(readlink -f "$SOURCE")
  # GDAL's /vsizip/ doesn't recurse into subdirectories, so if the geospatial
  # file is nested, point at it explicitly. Match common OGR-readable formats.
  INNER=$(unzip -Z1 "$ZIPFILE_ABS" \
    | grep -iE '\.(shp|geojson|json|gpkg|kml|gml|tab|gdb)$' \
    | head -n1)
  DATA_SOURCE="/vsizip/${ZIPFILE_ABS}"
  if [ -n "$INNER" ] && [ "$INNER" != "$(basename "$INNER")" ]; then
    DATA_SOURCE="${DATA_SOURCE}/${INNER}"
  fi
fi

TABLE="districts.${DISTRICT_TYPE,,}"
HOOK="$(dirname "$0")/post_load/${DISTRICT_TYPE,,}.sql"

echo "Available fields:"
ogrinfo -json -so -al "$DATA_SOURCE" | jq -r '.layers[0].fields[].name'
echo

# Some code columns are SAGE-specific and appear in
# no upstream dataset, so a post-load hook adds and populates them instead.
read -r -p "Code column in file (blank if supplied by a post-load hook): " FILE_CODE_COLUMN
if [ -z "$FILE_CODE_COLUMN" ] && [ ! -f "$HOOK" ]; then
  echo "$PROG: ERROR: no code column given and no post-load hook at $HOOK to supply one." >&2
  exit 1
fi
read -r -p "Name column in file (blank to skip): " FILE_NAME_COLUMN
read -r -p "Additional columns to keep (comma-separated, blank to skip): " FILE_EXTRA_COLUMNS

SELECT_COLS="$FILE_CODE_COLUMN,$FILE_NAME_COLUMN,$FILE_EXTRA_COLUMNS"
# Fixes string in case of skipped fields.
SELECT_COLS="${SELECT_COLS//,,/,}"
SELECT_COLS="${SELECT_COLS#,}"
SELECT_COLS="${SELECT_COLS%,}"

OGR_ARGS=(
  -f PostgreSQL
  "PG:dbname=${database} user=${db_user}"
  "$DATA_SOURCE"
  -overwrite
  -nln "$TABLE"
  # Promote single polygons to multi so mixed-geometry data loads cleanly.
  # Note that this is required in NYS for consistency: Ellis Island and the Statue of Liberty
  # are separate polygons from the rest of the state.
  -nlt PROMOTE_TO_MULTI
  # Reproject from the data's native coordinate reference system to WGS84.
  -t_srs EPSG:4326
  # Simplifies column names.
  -lco GEOMETRY_NAME=geom
  -lco FID=gid
  # Drop every attribute column except these.
  -select "$SELECT_COLS"
)
ogr2ogr "${OGR_ARGS[@]}"
if [ $? -ne 0 ]; then
  echo "$PROG: ERROR: ogr2ogr load failed." >&2
  exit 1
fi

# ogr2ogr's LAUNDER lowercases column names, so convert these to lowercase too.
if [ -n "$FILE_CODE_COLUMN" ]; then
  read -r -p "Code column in SQL (blank to keep as-is): " CODE_RENAME
  SQL_CODE_COLUMN="${FILE_CODE_COLUMN,,}"
  if [ -n "$CODE_RENAME" ]; then
    psql -d "$database" -U "$db_user" -c "ALTER TABLE $TABLE RENAME COLUMN \"$SQL_CODE_COLUMN\" TO $CODE_RENAME;" || exit 1
    SQL_CODE_COLUMN="$CODE_RENAME"
  fi
else
  read -r -p "Code column in SQL (created by the post-load hook): " SQL_CODE_COLUMN
fi
if [ -n "$FILE_NAME_COLUMN" ]; then
  read -r -p "Name column in SQL (blank to keep as-is): " NAME_RENAME
fi
SQL_NAME_COLUMN="${FILE_NAME_COLUMN,,}"
if [ -n "$NAME_RENAME" ]; then
  psql -d "$database" -U "$db_user" -c "ALTER TABLE $TABLE RENAME COLUMN \"$SQL_NAME_COLUMN\" TO $NAME_RENAME;" || exit 1
  SQL_NAME_COLUMN="$NAME_RENAME"
fi

# Adds any SAGE-specific columns the source doesn't carry. This has to run before the NOT
# NULL enforcement below, since for some types the hook is what supplies the code column.
if [ -f "$HOOK" ]; then
  echo "Running post-load hook $HOOK..."
  psql -d "$database" -U "$db_user" -v ON_ERROR_STOP=1 -f "$HOOK" || exit 1
fi

# Some NOT NULL enforcement to prevent problems in Java. This doubles as the gate on the
# hook's work: a district the hook had no code for fails here rather than reaching Java.
psql -d "$database" -U "$db_user" -c "ALTER TABLE $TABLE ALTER COLUMN $SQL_CODE_COLUMN SET NOT NULL;" || exit 1
if [ -n "$SQL_NAME_COLUMN" ]; then
  psql -d "$database" -U "$db_user" -c "ALTER TABLE $TABLE ALTER COLUMN $SQL_NAME_COLUMN SET NOT NULL;" || exit 1
fi

# The name column is nullable, which requires some extra care.
if [ -n "$SQL_NAME_COLUMN" ]; then
  NAME_VALUE="'$SQL_NAME_COLUMN'"
else
  NAME_VALUE="NULL"
fi
psql -d "$database" -U "$db_user" -c "INSERT INTO districts.type_info (type_name, code_column, name_column)
VALUES ('${DISTRICT_TYPE,,}', '$SQL_CODE_COLUMN', $NAME_VALUE)
ON CONFLICT (type_name) DO UPDATE SET
    code_column = EXCLUDED.code_column,
    name_column = EXCLUDED.name_column;" || exit 1

# Calls an API endpoint to finish setup, pretty-printing the response.
echo "Calling /cleanMaps to clean ${TABLE}..."
curl -sS -G "${baseUrl}/admin/api/cleanMaps" \
  --data-urlencode "key=${adminKey}" \
  --data-urlencode "type=${DISTRICT_TYPE}" | jq .
