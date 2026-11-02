#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

REMOTE_USER="davynm"
REMOTE_HOST="softala.haaga-helia.fi"
REMOTE_PATH="/home/davynm"
REMOTE_PORT="22"
JAR_FILE="target/projectbackend-0.0.1-SNAPSHOT.jar"
MAVEN_ARGS="clean package -DskipTests"

if [[ ! -f "./mvnw" ]]; then
    printf 'Error: Maven wrapper ./mvnw was not found.\n' >&2
    exit 1
fi

printf 'Building application...\n'
read -r -a MAVEN_ARGS_ARRAY <<< "$MAVEN_ARGS"
bash ./mvnw "${MAVEN_ARGS_ARRAY[@]}"

if [[ ! -f "$JAR_FILE" ]]; then
    printf 'Error: expected JAR was not created: %s\n' "$JAR_FILE" >&2
    exit 1
fi

printf 'Uploading %s to %s@%s:%s...\n' "$JAR_FILE" "$REMOTE_USER" "$REMOTE_HOST" "$REMOTE_PATH"
scp -P "$REMOTE_PORT" "$JAR_FILE" "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/"

printf 'Deployment upload completed.\n'
