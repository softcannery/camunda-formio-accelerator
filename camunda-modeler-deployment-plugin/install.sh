#!/bin/bash

echo Updating plugin

MODELER_DIR=${MODELER_DIR:-~/Library/Application Support/camunda-modeler}

PLUGIN="${MODELER_DIR}/resources/plugins/deploy-plugin"

rm -rf "${PLUGIN}"

mkdir "${PLUGIN}"

cp -v ./index.js "${PLUGIN}"
cp -vR ./css "${PLUGIN}"
cp -vR ./dist "${PLUGIN}"
