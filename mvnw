#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.3.2
#
# Optional ENV vars:
# ------------------
#   JAVA_HOME - location of a JDK home dir, required when type is not auto
#   MVNW_REPOURL - repo url base for downloading maven distribution
#   MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
#   MVNW_VERBOSE - true: enable verbose log; others: silence the output
# ----------------------------------------------------------------------------

set -euf
[ "${MVNW_VERBOSE-}" != true ] || set -x

# OS specific support.
native_path() { printf '%s\n' "$1"; }
case "$(uname)" in
CYGWIN* | MINGW*)
  [ -z "${JAVA_HOME-}" ] || JAVA_HOME="$(cygpath --unix "$JAVA_HOME")"
  native_path() { cygpath --path --windows "$1"; }
  ;;
esac

# set JAVACMD and JAVACCMD
set_java_home() {
  # For Cygwin and MinGW, ensure paths are in Unix format before anything is touched
  if [ -n "${JAVA_HOME-}" ]; then
    if [ -x "$JAVA_HOME/jre/sh/java" ]; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
      JAVACCMD="$JAVA_HOME/jre/sh/javac"
    else
      JAVACMD="$JAVA_HOME/bin/java"
      JAVACCMD="$JAVA_HOME/bin/javac"

      if [ ! -x "$JAVACMD" ] || [ ! -x "$JAVACCMD" ]; then
        echo "The JAVA_HOME environment variable is not defined correctly, so mvnw cannot run." >&2
        echo "JAVA_HOME is set to \"$JAVA_HOME\", but \"\$JAVA_HOME/bin/java\" or \"\$JAVA_HOME/bin/javac\" does not exist." >&2
        return 1
      fi
    fi
  else
    JAVACMD="$(
      'set' +e
      'unset' -f command 2>/dev/null
      'command' -v java
    )" || :
    JAVACCMD="$(
      'set' +e
      'unset' -f command 2>/dev/null
      'command' -v javac
    )" || :

    if [ ! -x "${JAVACMD-}" ]; then
      echo "The java executable is not found. Please ensure the JAVA_HOME environment variable is defined correctly." >&2
      return 1
    fi
    if [ ! -x "${JAVACCMD-}" ]; then
      echo "The javac executable is not found. Please ensure the JAVA_HOME environment variable is defined correctly." >&2
      return 1
    fi
  fi
}

# hash string like Java String::hashCode
hash_string() {
  str="${1:-}" h=0
  while [ -n "$str" ]; do
    char="${str%"${str#?}"}"
    h=$(((h * 31 + $(LC_CTYPE=C printf '%d' "'$char")) % 4294967296))
    str="${str#?}"
  done
  printf '%d' $h
}

verbose() { :; }
[ "${MVNW_VERBOSE-}" != true ] || verbose() { printf '%s\n' "${1-}"; }

die() {
  printf '%s\n' "$1" >&2
  exit 1
}

trim() {
  # MWRAPPER-139:
  #   Trims trailing and leading whitespace, carriage returns, tabs, and linefeeds.
  #   Needed for removing poorly-formatted key/value pairs from maven-wrapper.properties file.
  printf "%s" "${1}" | tr -d '[:space:]'
}

# parse distributionUrl and optional distributionSha256Sum, wrapperUrl from ...
while IFS="=" read -r key value; do
  case "$(trim "${key}")" in
  distributionUrl) distributionUrl=$(trim "${value}") ;;
  distributionSha256Sum) distributionSha256Sum=$(trim "${value}") ;;
  wrapperUrl) wrapperUrl=$(trim "${value}") ;;
  esac
done <"${0%/*}/.mvn/wrapper/maven-wrapper.properties"
[ -n "${distributionUrl-}" ] || die "cannot read distributionUrl property in ${0%/*}/.mvn/wrapper/maven-wrapper.properties"

case "${distributionUrl##*/}" in
apache-maven-*-bin.tar.gz | apache-maven-*-bin.zip) : ;;
*) die "distributionUrl is not valid, must contain maven-*.*.*/apache-maven-*-bin.tar.gz or apache-maven-*-bin.zip: ${distributionUrl}" ;;
esac

# find maven home
# .m2/wrapper/dists/{apache-maven-<version>}/[hash]/apache-maven-<version>/
distributionUrlName="${distributionUrl##*/}"
distributionUrlNameMain="${distributionUrlName%.*}"
distributionUrlNameMain="${distributionUrlNameMain%-bin}"
MAVEN_USER_HOME="${MAVEN_USER_HOME:-${HOME}/.m2}"
MAVEN_HOME="${MAVEN_USER_HOME}/wrapper/dists/${distributionUrlNameMain-}/$(hash_string "${distributionUrl}")"

exec_maven() {
  unset MVNW_VERBOSE MVNW_USERNAME MVNW_PASSWORD MVNW_REPOURL || :
  exec "$MAVEN_HOME/bin/mvn" "$@" || die "cannot exec $MAVEN_HOME/bin/mvn"
}

if [ -d "$MAVEN_HOME" ]; then
  verbose "found existing MAVEN_HOME at $MAVEN_HOME"
  exec_maven "$@"
fi

case "${distributionUrl-}" in
*?://*) : ;;
file://*)
  # local file - ignore checksum validation
  tmp_download_dir="$MAVEN_HOME"
  distributionUrl="${distributionUrl#file://}"
  distributionUrlName="${distributionUrl##*/}"
  mkdir -p "$tmp_download_dir"
  if ! cp -f "$distributionUrl" "$tmp_download_dir/$distributionUrlName"; then
    die "failed to copy $distributionUrl to $tmp_download_dir/$distributionUrlName"
  fi
  ;;
*)
  die "distributionUrl must be an absolute URI, but was: ${distributionUrl}"
  ;;
esac

# prepare tmp dir
if TMP_DOWNLOAD_DIR="$(mktemp -d)" && [ -d "$TMP_DOWNLOAD_DIR" ]; then
  clean() { rm -rf -- "$TMP_DOWNLOAD_DIR"; }
  trap clean HUP INT TERM EXIT
else
  die "cannot create temp dir"
fi

mkdir -p -- "${MAVEN_HOME%/*}"

verbose "Downloading from: $distributionUrl"

# perform download with optional authentication
if [ -n "${MVNW_USERNAME-}" ] && [ -n "${MVNW_PASSWORD-}" ]; then
  # use curl with authentication
  if command -v curl >/dev/null 2>&1; then
    if ! curl -fL --user "${MVNW_USERNAME}:${MVNW_PASSWORD}" -o "$TMP_DOWNLOAD_DIR/$distributionUrlName" "$distributionUrl"; then
      die "could not download $distributionUrl"
    fi
  elif command -v wget >/dev/null 2>&1; then
    if ! wget --http-user="${MVNW_USERNAME}" --http-password="${MVNW_PASSWORD}" -O "$TMP_DOWNLOAD_DIR/$distributionUrlName" "$distributionUrl"; then
      die "could not download $distributionUrl"
    fi
  else
    die "no http client found (tried curl, wget); please install one"
  fi
else
  if command -v curl >/dev/null 2>&1; then
    if ! curl -fL -o "$TMP_DOWNLOAD_DIR/$distributionUrlName" "$distributionUrl"; then
      die "could not download $distributionUrl"
    fi
  elif command -v wget >/dev/null 2>&1; then
    if ! wget -O "$TMP_DOWNLOAD_DIR/$distributionUrlName" "$distributionUrl"; then
      die "could not download $distributionUrl"
    fi
  else
    die "no http client found (tried curl, wget); please install one"
  fi
fi

# verify checksum
if [ -n "${distributionSha256Sum-}" ]; then
  distributionSha256Result=false
  if command -v sha256sum >/dev/null 2>&1; then
    if echo "${distributionSha256Sum}  ${TMP_DOWNLOAD_DIR}/${distributionUrlName}" | sha256sum -c >/dev/null 2>&1; then
      distributionSha256Result=true
    fi
  elif command -v shasum >/dev/null 2>&1; then
    if echo "${distributionSha256Sum}  ${TMP_DOWNLOAD_DIR}/${distributionUrlName}" | shasum -a 256 -c >/dev/null 2>&1; then
      distributionSha256Result=true
    fi
  else
    echo "Checksum validation was requested but neither 'sha256sum' or 'shasum' are available." >&2
    echo "Please install either command, or disable validation by removing 'distributionSha256Sum' from your maven-wrapper.properties." >&2
  fi
  if [ $distributionSha256Result = false ]; then
    die "Error: Failed to validate Maven distribution SHA-256, your Maven distribution might be compromised. If you updated your Maven version, you need to update the specified distributionSha256Sum property."
  fi
fi

# unzip and move
if ! command -v unzip >/dev/null 2>&1; then
  die "no unzip found; please install unzip"
fi
unzip -q "$TMP_DOWNLOAD_DIR/$distributionUrlName" -d "$TMP_DOWNLOAD_DIR" || die "failed to unzip"
printf '%s' "$distributionUrl" >"$TMP_DOWNLOAD_DIR/${distributionUrlNameMain}.url"

# Restart using the downloaded Maven
MAVEN_HOME_PARENT="${MAVEN_HOME%/*}"
MAVEN_HOME_LAST="${MAVEN_HOME##*/}"
mkdir -p "$MAVEN_HOME_PARENT"
mv -- "$TMP_DOWNLOAD_DIR/$distributionUrlNameMain" "$MAVEN_HOME" || mv -- "$TMP_DOWNLOAD_DIR" "$MAVEN_HOME"

verbose "Installed Maven to $MAVEN_HOME"
exec_maven "$@"
