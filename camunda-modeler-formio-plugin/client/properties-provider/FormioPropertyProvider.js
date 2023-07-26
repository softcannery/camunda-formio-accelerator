'use strict';
import React from 'react'
import { is } from 'bpmn-js/lib/util/ModelUtil';
import { find, findIndex } from 'lodash';

var jsxRuntime = require('../../node_modules/@bpmn-io/properties-panel/preact/jsx-runtime');

let LOW_PRIORITY = 500;

export default function FormioPropertyProvider(propertiesPanel, eventBus) {
  this.getGroups = function (element) {
    return function (groups) {

      if (is(element, 'bpmn:StartEvent') || is(element, 'bpmn:UserTask')) {
        const formsGroup = find(groups, { id: "CamundaPlatform__Form" });

        console.log(formsGroup)

        if (formsGroup) {
          formsGroup.entries.push({
            id: "formio-designer-button-edit",
            component: () => jsxRuntime.jsxs("button", {
              class: 'btn btn-secondary',
              "data-entry-id": "formio-designer-button-edit",
              children: ["Edit"],
              onClick: () => { eventBus.fire('formio.form.edit', { element }); },
              style: { margin: "10px 15px 15px" }
            }),
            isEdited: () => false
          })

        }
      }

      return groups;
    }
  };

  propertiesPanel.registerProvider(LOW_PRIORITY, this);
}

FormioPropertyProvider.$inject = ['propertiesPanel', 'eventBus',];
