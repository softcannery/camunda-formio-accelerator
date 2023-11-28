/* eslint-disable no-unused-vars */
import React, { useState } from "camunda-modeler-plugin-helpers/react";
import { Modal } from "camunda-modeler-plugin-helpers/components";
import { Form } from "react-formio";

// polyfill upcoming structural components
const Title = Modal.Title || (({ children }) => <div>{children}</div>);
const Body = Modal.Body || (({ children }) => <div>{children}</div>);
const Footer = Modal.Footer || (({ children }) => <div>{children}</div>);

// we can even use hooks to render into the application
export default function FormioFormSelect({ config, onClose }) {
  const onSubmit = () =>
    onClose({ formId, fileName, varName, inputDS, outputDS });

  const [varName, setVarName] = useState(config.variableName);
  const [inputDS, setInputDS] = useState(config.inputDS);
  const [outputDS, setOutputDS] = useState(config.outputDS);
  const [showInput] = useState(config.showInput);

  const [formId, setFormId] = useState(config.formId);
  const [forms] = useState(config.forms);
  const [fileName] = useState(config.fileName);

  const options = {
    dialogAttr: {
      class: "bootstrap-iso",
    },
    readOnly: false,
    noDefaultSubmitButton: true,
  };

  const selectStyle = {
    width: "300px",
  };

  let items = [];
  for (let property in forms) {
    items.push(
      <option key={property} value={property}>
        {forms[property].name}
      </option>,
    );
  }

  let sources = config.dataSources.map((ds) => (
    <option key={ds.id} value={ds.id}>
      {ds.name}
    </option>
  ));

  return (
    <Modal id="modalSelect" className="form-builder" onClose={onClose}>
      <Title>Form select</Title>

      <Body>
        <form id="formSelect" onSubmit={onSubmit}>
          <fieldset>
            <div className="fields">
              <div className="form-group">
                <table>
                  <tr>
                    <td>
                      <label htmlFor="formSelectControl">Pick your form</label>
                      <div style={selectStyle}>
                        <select
                          className="form-control"
                          name="formSelectControl"
                          value={formId}
                          onChange={(event) => setFormId(event.target.value)}
                        >
                          {items}
                        </select>
                      </div>
                    </td>
                    {showInput && (
                      <div>
                        {/* <td>
                      <label htmlFor="inputVarControl">Variable name</label>
                      <input type="text"
                        className="form-control"
                        name="inputVarControl"
                        value={ varName }
                        onChange={ (event) => setVarName(event.target.value) } />
                    </td> */}
                        <td>
                          <label htmlFor="inputSubmissionSelectControl">
                            Input data source
                          </label>
                          <div style={selectStyle}>
                            <select
                              className="form-control"
                              name="inputSubmissionSelectControl"
                              value={inputDS}
                              onChange={(event) =>
                                setInputDS(event.target.value)
                              }
                            >
                              {sources}
                            </select>
                          </div>
                        </td>
                      </div>
                    )}
                    <td>
                      <label htmlFor="outputSubmissionSelectControl">
                        Output data source
                      </label>
                      <div style={selectStyle}>
                        <select
                          className="form-control"
                          name="outputSubmissionSelectControl"
                          value={outputDS}
                          onChange={(event) => setOutputDS(event.target.value)}
                        >
                          {sources}
                        </select>
                      </div>
                    </td>
                  </tr>
                </table>
              </div>
            </div>
          </fieldset>
        </form>

        <h3>Preview</h3>
        <div className="bootstrap-iso">
          <Form form={forms[formId]} options={options} />
        </div>
      </Body>

      <Footer>
        <div id="dialogButtons" className="form-submit">
          <button type="submit" class="btn btn-primary" form="formSelect">
            Select
          </button>
          <button
            type="button"
            class="btn btn-secondary"
            onClick={() => onClose()}
          >
            Cancel
          </button>
        </div>
      </Footer>
    </Modal>
  );
}
