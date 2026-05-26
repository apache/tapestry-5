#!/usr/bin/env bash

# ##############################################################################
#
# Vendors ASM source from gitlab.ow2.org into Apache Tapestry's internal plastic package.
# Combines asm, asm-commons, asm-tree, and asm-util into a single source tree
# and rewrites the package from: org.objectweb.asm -> org.apache.tapestry5.internal.plastic.asm
#
# Usage: ./vendor-asm.sh <TAG>
#   TAG : ASM git tag, e.g. ASM_9_10
#
# Run without arguments to list available tags.
#
# ##############################################################################

set -euo pipefail

ASM_REPO="https://gitlab.ow2.org/asm/asm.git"
SRC_PKG="org.objectweb.asm"
DST_PKG="org.apache.tapestry5.internal.plastic.asm"
SRC_PATH="org/objectweb/asm"

DST_PATH="./src/external/java/org/apache/tapestry5/internal/plastic/asm"

MODULES=(
    asm
    asm-analysis
    asm-commons
    asm-tree
    asm-util
)

TAG="${1:-}"

if [[ -z "$TAG" ]]; then
    echo "Usage: $0 <TAG>"
    echo "  Example: $0 ASM_9_10"
    echo ""
    echo "Available tags (fetched from remote):"
    git ls-remote --tags "$ASM_REPO" | grep -o 'ASM_[0-9_]*$' | sort -V
    exit 0
fi

# ##############################################################################

echo "=> Cleaning '${DST_PATH}' and preparing clone desitnation"

# Make sure we are in the correct directory
cd "$(dirname "${BASH_SOURCE[0]}")"

# Wipe the destination so removed or renamed files from a previous run don't linger and break things
rm -rf -- "${DST_PATH}"
mkdir -p -- "${DST_PATH}"

# Using temp directory so we never touch the working tree
# and trap a cleanup step if the script fails partway through.
WORK_DIR="$(mktemp -d)"
trap 'rm -rf -- "${WORK_DIR}"' EXIT

# ##############################################################################

echo "=> Cloning ASM '${TAG}' ..."

git clone --depth 1 --branch "${TAG}" "${ASM_REPO}" "${WORK_DIR}/asm-src" --quiet

# ##############################################################################

echo "=> Copying sources from modules: ${MODULES[*]} ..."

for MODULE in "${MODULES[@]}"; do
    SRC_ROOT="${WORK_DIR}/asm-src/${MODULE}/src/main/java/${SRC_PATH}"

    if [[ ! -d "${SRC_ROOT}" ]]; then
        echo "    WARNING: ${MODULE} has no sources at expected path, skipping."
        continue
    fi

    # SRC_ROOT ends at org/objectweb/asm, so the path stripped from each file
    # naturally includes any subpackage directory (commons/, tree/, util/).
    # This means all modules land under the same $DST_PATH without extra nesting.
    # Both .java and .html (package.html Javadoc) files are included.
    find "${SRC_ROOT}" \( -name "*.java" -o -name "*.html" \) | while read -r FILE; do
        REL="${FILE#"${SRC_ROOT}"/}"
        DEST_FILE="${DST_PATH}/${REL}"
        mkdir -p "$(dirname "${DEST_FILE}")"
        cp "${FILE}" "${DEST_FILE}"
    done

    COUNT=$(find "${SRC_ROOT}" -name "*.java" | wc -l | tr -d ' ')
    echo "    ${MODULE}: ${COUNT} files -> ${DST_PATH}"
done

# ##############################################################################

echo "=> Rewriting package references ..."

# Rewrite all occurrences of the old package to the new one
#
# Two forms must be handled:
#
#   Dot                     Used in package/import declarations and Javadoc. The
#   (org.objectweb.asm)     dots in the sed pattern are escaped so they match
#                           literally, not as regex wildcards, which would
#                           otherwise also match org/objectweb/asm.
#
#   Slash                   Used in runtime class-name strings, like ASM's
#   (org/objectweb/asm)     Constants#checkAsmExperimental converts class names
#                           via getName().replace('.','/') and then checks them
#                           against the original location. Since the vendored
#                           classes live in the new package at runtime, the
#                           slash-form strings must match it too.

SRC_PKG_ESCAPED="${SRC_PKG//./\\.}"   # org\.objectweb\.asm         (literal dots for sed)
SRC_PATH_SLASHED="${SRC_PKG//./\/}"   # org/objectweb/asm           (slash-form to find)
DST_PATH_SLASHED="${DST_PKG//./\/}"   # org/apache/tapestry5/...    (slash-form replacement)

find "${DST_PATH}" \( -name "*.java" -o -name "*.html" \) | while read -r FILE; do
    sed -i '' "s|${SRC_PKG_ESCAPED}|${DST_PKG}|g" "${FILE}"
    sed -i '' "s|${SRC_PATH_SLASHED}|${DST_PATH_SLASHED}|g" "${FILE}"
done

# ##############################################################################

echo "=> Copying LICENSE.txt as LICENSE-${TAG}.txt ..."

cp -- "${WORK_DIR}/asm-src/LICENSE.txt" "./LICENSE-${TAG}.txt"

# ##############################################################################

echo "=> DONE! Please update the NOTICE.txt to mention '${TAG}'"
