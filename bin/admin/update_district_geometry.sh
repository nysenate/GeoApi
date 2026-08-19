#!/bin/bash

PROG=$(basename "$0")

function usage() {
  echo "Usage: $PROG [SOURCE] DISTRICT_TYPE" >&2
  echo "  SOURCE         Either a path to a zip archive containing the district geometry," >&2
  echo "                 or the URL of an ArcGIS REST feature service layer, e.g." >&2
  echo "                 https://<host>/arcgis/rest/services/<service>/FeatureServer/6" >&2
  echo "                 Omitted for a type listed in sources.conf, which supplies it." >&2
  echo "  DISTRICT_TYPE  District type (used as the target table name in the districts schema)." >&2
  echo "A type listed in sources.conf takes its columns from there and is not prompted;" >&2
  echo "otherwise the script inspects the dataset and asks for the id column." >&2
}

# Assigns variables based on argument count.
case $# in
  1) SOURCE=""; DISTRICT_TYPE="$1" ;;
  2) SOURCE="$1"; DISTRICT_TYPE="$2" ;;
  *) usage; exit 1 ;;
esac

# Supplies the defaults for types we load from a known dataset. Sourced with
# DISTRICT_TYPE set, since it keys off the type.
SOURCES_CONF="$(dirname "$0")/sources.conf"
if [ ! -f "$SOURCES_CONF" ]; then
  echo "$PROG: ERROR: $SOURCES_CONF not found." >&2
  exit 1
fi
unset ID_COLUMN EXTRA_COLUMNS
source "$SOURCES_CONF"

# A configured type is loaded from its configured source, since SAGE relies on certain columns.
if [ -n "$DEF_SOURCE" ]; then
  if [ -n "$SOURCE" ]; then
    echo "$PROG: ERROR: $DISTRICT_TYPE is configured in $SOURCES_CONF and takes no SOURCE argument." >&2
    exit 1
  fi
  SOURCE="$DEF_SOURCE"
elif [ -z "$SOURCE" ]; then
  echo "$PROG: ERROR: no source given and no entry for $DISTRICT_TYPE in $SOURCES_CONF." >&2
  usage
  exit 1
fi

for cmd in ogr2ogr ogrinfo jq psql curl unzip; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "$PROG: ERROR: $cmd not found." >&2
    exit 1
  fi
done

source "$(dirname "$0")/admin.script.properties"
SERVER_UP=1
if ! curl -fsS "${baseUrl}/ping" >/dev/null; then
  SERVER_UP=0
  echo "$PROG: WARNING: server at ${baseUrl} is not responding." >&2
fi

LAYER_URL=""
if [[ "$SOURCE" =~ ^https?://.*/(Feature|Map)Server/[0-9]+/?$ ]]; then
  # GDAL's ESRIJSON driver reads a feature service's /query endpoint directly and pages
  # through it automatically, so there's nothing to download by hand.
  # "where=1=1" gets every row, since the endpoint rejects a query with no WHERE clause.
  LAYER_URL="${SOURCE%/}"
  # Coordinates are taken at full precision. Rounding them cuts the response by about a
  # third, but it also folds near-degenerate spikes over on themselves: at six decimal
  # places 23 of the 995 town_city polygons came back self-intersecting, which fails the
  # ST_IsValid check cleanMaps ends with and leaves the layer too invalid to simplify.
  DATA_SOURCE="ESRIJSON:${LAYER_URL}/query?where=1=1&outFields=*&outSR=4326&f=json"
elif [[ "$SOURCE" == http://* || "$SOURCE" == https://* ]]; then
  # Any other URL is handed to GDAL as-is, which covers a plain geospatial file served over HTTP.
  DATA_SOURCE="$SOURCE"
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

# The field list is only used to show the choices and to drop columns the source lacks.
# A feature service layer states its own fields, so ask it rather than have ogrinfo derive
# them: deriving means reading the features, which for a layer small enough to come back in
# one page of the query endpoint downloads the whole dataset a second time.
FIELDS=""
if [ -n "$LAYER_URL" ]; then
  FIELDS=$(curl -fsS "${LAYER_URL}?f=json" | jq -r '.fields[]?.name')
fi
# Anything else, and any service that didn't answer, has to be inspected.
if [ -z "$FIELDS" ]; then
  FIELDS=$(ogrinfo -json -so -al "$DATA_SOURCE" | jq -r '.layers[0].fields[].name')
fi
if [ -z "$FIELDS" ]; then
  echo "$PROG: ERROR: no fields found in $DATA_SOURCE." >&2
  exit 1
fi
echo "Available fields:"
echo "$FIELDS"
echo

# Prompt for the id column, if not provided.
if [[ ! -v DEF_SOURCE ]]; then
  read -rp "ID column in file: " ID_COLUMN
  if [ -z "$ID_COLUMN" ]; then
    echo "$PROG: ERROR: no id column given." >&2
    exit 1
  fi
fi

SELECT_COLS="$ID_COLUMN,$EXTRA_COLUMNS"
# Fixes string in case of skipped fields.
SELECT_COLS="${SELECT_COLS//,,/,}"
SELECT_COLS="${SELECT_COLS#,}"
SELECT_COLS="${SELECT_COLS%,}"

# Drops anything the source doesn't actually carry, since ogr2ogr errors on an unknown column.
KEPT=""
IFS=',' read -ra COLS <<< "$SELECT_COLS"
for col in "${COLS[@]}"; do
  # Builds up a CSV string of existing columns.
  if grep -qxiF "$col" <<< "$FIELDS"; then
    KEPT="${KEPT:+$KEPT,}$col"
  fi
done
SELECT_COLS="$KEPT"
# TODO: include id?
echo "Keeping columns: $SELECT_COLS"

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

# Adds any SAGE-specific columns the source doesn't carry, and standardizes the name column
# on "name". This has to run before the NOT NULL enforcement below, since for some types the
# hook is what supplies the id column.
if [ -f "$HOOK" ]; then
  echo "Running post-load hook $HOOK..."
  psql -d "$database" -U "$db_user" -v ON_ERROR_STOP=1 -f "$HOOK" || exit 1
fi

# Some NOT NULL enforcement to prevent problems in Java. This doubles as the gate on the
# hook's work: a district the hook had no id for fails here rather than reaching Java.
psql -d "$database" -U "$db_user" -c "ALTER TABLE $TABLE ALTER COLUMN $ID_COLUMN SET NOT NULL;" || exit 1
HAS_NAME=$(psql -d "$database" -U "$db_user" -tAc \
  "SELECT 1 FROM information_schema.columns
   WHERE table_schema = 'districts' AND table_name = '${DISTRICT_TYPE,,}' AND column_name = 'name';") || exit 1
if [ -n "$HAS_NAME" ]; then
  psql -d "$database" -U "$db_user" -c "ALTER TABLE $TABLE ALTER COLUMN name SET NOT NULL;" || exit 1
fi

psql -d "$database" -U "$db_user" -c "INSERT INTO districts.type_info (type_name, id_column)
VALUES ('${DISTRICT_TYPE,,}', LOWER('$ID_COLUMN'))
ON CONFLICT (type_name) DO UPDATE SET
    id_column = EXCLUDED.id_column;" || exit 1

# Calls an API endpoint to finish setup, pretty-printing the response.
if [ "$SERVER_UP" -eq 0 ]; then
  echo "$PROG: WARNING: skipping /cleanMaps, since the server was not responding. You'll have to run it manually." >&2
else
  echo "Calling /cleanMaps to clean ${TABLE}..."
  curl -sS -G "${baseUrl}/admin/api/cleanMaps" \
    --data-urlencode "key=${adminKey}" \
    --data-urlencode "type=${DISTRICT_TYPE}" | jq .
fi
