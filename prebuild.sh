#!/bin/bash

# Ugly workaround to https://github.com/gradle/gradle/issues/18519
# Gradle dependency resolution for capability conflict does not work for 
# transitive Maven dependencies with a classifier
# So we resolve these dependencies ourselves. :D

# TODO: This seems no longer to be needed on the Apache Jenkins with Gradle 8.14.4,
#       although the ticket wasn't closed.

set -euo pipefail

NETTY_VERSION=4.1.96.Final

M2_BASE="${HOME}/.m2/repository/io/netty"
MAVEN_CENTRAL="https://repo1.maven.org/maven2/io/netty"

download_netty_native() {
    local artifact=$1
    local classifier=$2
    local ext=$3
    
    local source_url="${MAVEN_CENTRAL}/${artifact}/${NETTY_VERSION}/${artifact}-${NETTY_VERSION}.${ext}"
    local target_file="${M2_BASE}/${artifact}/${NETTY_VERSION}/${artifact}-${NETTY_VERSION}-${classifier}.${ext}"

    if [[ -f "${target_file}" ]]; then
        echo "Already cached: ${artifact}-${NETTY_VERSION}-${classifier}.${ext}"
        return 0
    fi

    echo "Downloading: ${artifact}-${NETTY_VERSION}-${classifier}.${ext}"
    
    # -f  : Fail silently on HTTP errors (prevents saving a 404 HTML page as a .jar)
    # -sS : Silent mode (no progress bar to clutter Jenkins logs), but Show errors if they occur
    # --create-dirs : Automatically creates the target directory structure
    curl -f -sS --create-dirs "${source_url}" -o "${target_file}"
}

# Detect Architecture and normalize to Netty's classifier naming
RAW_ARCH=$(uname -m)
case "$RAW_ARCH" in
    x86_64 | amd64)
        NETTY_ARCH="x86_64"
        ;;
    arm64 | aarch64)
        # Netty uses "aarch_64" for both Apple Silicon and Linux ARM
        NETTY_ARCH="aarch_64" 
        ;;
    *)
        echo "Skipping Netty pre-resolution: Unsupported architecture (${RAW_ARCH})"
        exit 0
        ;;
esac

# Detect OS and map to Netty artifact/classifier prefix
RAW_OS=$(uname -s | tr '[:upper:]' '[:lower:]')
case "$RAW_OS" in
    linux)
        ARTIFACT="netty-transport-native-epoll"
        CLASSIFIER="linux-$NETTY_ARCH"
        ;;
    darwin)
        ARTIFACT="netty-transport-native-kqueue"
        CLASSIFIER="osx-${NETTY_ARCH}"
        ;;
    *)
        echo "Skipping Netty pre-resolution: No native transport needed for OS (${RAW_OS})"
        exit 0
        ;;
esac

echo "Detected OS: $RAW_OS, Arch: $RAW_ARCH"
echo "Pre-resolving Netty native dependencies for ${CLASSIFIER}..."


download_netty_native "pom"
download_netty_native "jar"

echo "Pre-resolution complete."
