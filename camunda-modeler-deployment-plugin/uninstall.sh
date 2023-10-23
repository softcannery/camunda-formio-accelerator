#!/bin/bash

MODELER_DIR=${MODELER_DIR:-~/Library/Application Support/camunda-modeler}

PLUGIN="${MODELER_DIR}/resources/plugins/deploy-plugin"

rm -rf "${PLUGIN}"
