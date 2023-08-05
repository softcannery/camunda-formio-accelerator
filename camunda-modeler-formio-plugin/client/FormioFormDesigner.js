/* eslint-disable no-unused-vars */
import React, { useState, useRef } from "camunda-modeler-plugin-helpers/react";

import { Modal } from "camunda-modeler-plugin-helpers/components";
import { FormBuilder } from "react-formio";

// polyfill upcoming structural components
const Title = Modal.Title || (({ children }) => <div>{children}</div>);
const Body = Modal.Body || (({ children }) => <div>{children}</div>);
const Footer = Modal.Footer || (({ children }) => <div>{children}</div>);

// we can even use hooks to render into the application
export default function FormioFormDesigner({ config, onClose }) {
  const [varName, setVarName] = useState(config.variableName);
  const [inputDS, setInputDS] = useState(config.inputDS);
  const [outputDS, setOutputDS] = useState(config.outputDS);
  const [showInput] = useState(config.showInput);

  const [formName, setFormName] = useState(config.formName);
  const [originSchema, setFormSchema] = useState(config.schema);

  var formSchema = config.schema;
  const fileSystem = config.fileSystem;

  const FORMIO_TYPE = ".formio";

  const options = {
    dialogAttr: {
      class: "bootstrap-iso",
    },
    noDefaultSubmitButton: true,
  };

  const loadFileContent = async (filePath) => {
    try {
      return await fileSystem.readFile(filePath, { encoding: "utf8" });
    } catch (error) {
      if (error.code === "EISDIR") {
        console.log(new Error(`Cannot open directory: ${filePath}`));
      }
      return undefined;
    }
  };

  const replaceFileExt = (path, srcType, dstType) => {
    return path ? path.substring(0, path.lastIndexOf(srcType)) + dstType : null;
  };

  const handleFileInput = async (e) => {
    const file = e.target.files[0];
    const fileContent = await loadFileContent(file.path);
    formSchema = JSON.parse(fileContent.contents);
    if ("template" in formSchema) {
      alert("Invalid <form.io> file. Use single form file.");
      return;
    }
    setFormSchema(formSchema);
    setFormName(replaceFileExt(file.name, FORMIO_TYPE, ""));
  };

  let sources = config.dataSources.map((ds) => (
    <option key={ds.id} value={ds.id}>
      {ds.name}
    </option>
  ));

  return (
    <Modal id="formBuilder" className="form-builder">
      <Title>Form builder</Title>

      <Body>
        <form id="formSelect">
          <fieldset>
            <div className="fields">
              <div className="form-group">
                <div>
                  <input
                    type="file"
                    className="form-control"
                    name="formFileInput"
                    id="formFileInput"
                    onChange={handleFileInput}
                    accept=".formio"
                  />
                  <label className="btn btn-secondary" htmlFor="formFileInput">
                    Browse...
                  </label>
                </div>
                <div>
                  <label htmlFor="formNameControl">Form name</label>
                  <input
                    type="text"
                    className="form-control"
                    name="formNameControl"
                    value={formName}
                    onChange={(event) => setFormName(event.target.value)}
                  />
                </div>
                {showInput && (
                  <div>
                    <label htmlFor="inputSubmissionSelectControl">
                      Input data source
                    </label>
                    <select
                      className="form-control"
                      name="inputSubmissionSelectControl"
                      value={inputDS}
                      onChange={(event) => setInputDS(event.target.value)}
                    >
                      {sources}
                    </select>
                  </div>
                )}
                <div>
                  <label htmlFor="outputSubmissionSelectControl">
                    Output data source
                  </label>
                  <select
                    className="form-control"
                    name="outputSubmissionSelectControl"
                    value={outputDS}
                    onChange={(event) => setOutputDS(event.target.value)}
                  >
                    {sources}
                  </select>
                </div>
              </div>
            </div>
          </fieldset>
        </form>
        <h3>Design</h3>
        <div class="bootstrap-iso">
          <FormBuilder
            form={originSchema}
            onChange={(schema) => (formSchema = schema)}
            options={options}
          />
        </div>
      </Body>

      <Footer>
        <div id="dialogButtons">
          <button
            type="submit"
            class="btn btn-primary"
            onClick={() =>
              onClose({ formSchema, formName, varName, inputDS, outputDS })
            }
          >
            Save
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
