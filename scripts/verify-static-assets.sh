#!/usr/bin/env bash
set -euo pipefail

STATIC_ASSET_PATHS=(
    'src/main/resources/static'
    'src/main/resources/templates'
)

FORBIDDEN_PATTERN='console\.log|debugger|TODO|FIXME|onclick=|onerror=|onload=|innerHTML'

if git grep -n -I -E "${FORBIDDEN_PATTERN}" -- "${STATIC_ASSET_PATHS[@]}"; then
    echo "Static assets or templates contain debug hooks, TODO markers, inline event handlers, or raw innerHTML usage." >&2
    exit 1
fi

echo "Static asset hygiene check passed."
