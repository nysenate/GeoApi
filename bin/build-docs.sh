#!/bin/bash
# Builds the Sphinx documentation (developer docs + admin docs) and syncs the
# generated HTML into src/main/webapp so the webapp can serve it. The HTML is
# committed to the repo, so run this whenever docs/index.rst or
# admin-docs/index.rst change, then review and commit the result.
#
# This is intentionally a developer-run step, NOT part of the Maven build: it
# needs a Python toolchain (Python 3.11+) and PyPI access to create the venv,
# which we don't want to require on the prod build/deploy box.
set -eo pipefail
source "$(dirname "$0")/utils.sh"

VENV="$ROOTDIR/docs/.venv"
REQS="$ROOTDIR/docs/requirements.txt"

# Ensure the build virtualenv exists with the pinned dependencies.
if [ ! -x "$VENV/bin/sphinx-build" ]; then
    echo "Creating documentation virtualenv at $VENV ..."
    python3 -m venv "$VENV"
fi
"$VENV/bin/pip" install --quiet --disable-pip-version-check -r "$REQS"

# Build a doc set and replace its served copy. $1 is the source directory name
# under the repo root, which is also its subdirectory under src/main/webapp.
# If $2 is "true", the _static theme assets are dropped after sync and served
# from docs/html/_static instead (both doc sets use the same nature theme, so
# the assets are identical). See the resource handler in WebApplicationConfig.
build_docs() {
    local name="$1"
    local share_static="$2"
    local srcdir="$ROOTDIR/$name"
    local builddir="$srcdir/_build/html"
    local doctreedir="$srcdir/_build/doctrees"
    local servedir="$ROOTDIR/src/main/webapp/$name/html"

    # -d keeps the doctree cache out of the html output so it isn't synced/committed.
    echo "Building $name ..."
    "$VENV/bin/sphinx-build" -b html -q -d "$doctreedir" "$srcdir" "$builddir"

    # Replace, don't merge, so stale files from older Sphinx versions are dropped.
    echo "Syncing $name -> ${servedir#$ROOTDIR/}"
    rm -rf "$servedir"
    mkdir -p "$servedir"
    cp -a "$builddir/." "$servedir/"

    if [ "$share_static" = "true" ]; then
        echo "Dropping duplicate _static (served from docs/html/_static)"
        rm -rf "$servedir/_static"
    fi
}

build_docs "docs" false
build_docs "admin-docs" true

echo "Done. Review and commit src/main/webapp/docs/html and src/main/webapp/admin-docs/html."
