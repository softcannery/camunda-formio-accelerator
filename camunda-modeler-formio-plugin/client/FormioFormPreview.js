/* eslint-disable no-unused-vars */
import React, { useState } from 'camunda-modeler-plugin-helpers/react';

import { Modal } from 'camunda-modeler-plugin-helpers/components';

import ReactDOM from 'react-dom';

import { Form } from 'react-formio';

// polyfill upcoming structural components
const Title = Modal.Title || (({ children }) => <div>{ children }</div>);
const Body = Modal.Body || (({ children }) => <div>{ children }</div>);
const Footer = Modal.Footer || (({ children }) => <div>{ children }</div>);

// we can even use hooks to render into the application
export default function FormioFormPreview({ config, onClose }) {

  const [ schema ] = useState(config.schema);
  const [ file ] = useState(config.file);

  const options = {
    dialogAttr: {
      class: 'bootstrap-iso'
    },
    readOnly: true,
    noDefaultSubmitButton: true
  };

  return <Modal id="formPreview" className="form-builder">
    <Title>Form preview - { schema.name }</Title>

    <Body>
      <div class="bootstrap-iso">
        <Form form={ schema } options={ options } />
      </div>
    </Body>

    <Footer>
      <div id="dialogButtons">
        <button type="button" class="btn btn-primary" onClick={ () => onClose() }>Close</button>
      </div>
    </Footer>
  </Modal>;
}
