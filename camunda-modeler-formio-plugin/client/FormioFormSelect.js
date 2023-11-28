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
  const [forms, setForms] = useState(config.forms);
  const [fileName, setFileName] = useState(config.fileName);
  const [selectedForm, setSelectedForm] = useState(config.forms[formId]);
  var formSchema = config.schema;
  const fileSystem = config.fileSystem;


  const options = {
    dialogAttr: {
      class: "bootstrap-iso",
    },
    readOnly: false,
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
    if (!("template" in formSchema)) {
      alert("Invalid <form.io> file. Use form project file.");
      return;
    }
    setFileName(file.name);

    let items = [];
    const forms = formSchema.template.forms;
    for (let property in forms) {
      items.push(
          <option key={property} value={property}>
            {forms[property].name}
          </option>,
      );
    }

    setFormItems(items);
    setForms(forms);

    const formId = Object.keys(forms).length > 0 ? Object.keys(forms)[0] : "";

    if (formId) {
      setFormId(formId);
      setSelectedForm(forms[formId]);
    }
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

  const [formItems, setFormItems] = useState(items);

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
                    <td valign={"bottom"}>
                      <label className="btn btn-secondary" style={{ margin: "0px" }} htmlFor="formFileInput">
                        Browse Formio Template...
                      </label>
                      <div style={selectStyle}>
                        <input
                            type="file"
                            className="form-control"
                            name="formFileInput"
                            id="formFileInput"
                            onChange={handleFileInput}
                            accept=".formio"
                        />
                      </div>
                    </td>
                    <td>
                      <label htmlFor="formSelectControl">Pick your form</label>
                      <div style={selectStyle}>
                        <select
                          className="form-control"
                          name="formSelectControl"
                          value={formId}
                          onChange={(event) => {setFormId(event.target.value); setSelectedForm(forms[event.target.value]);}}
                        >
                          {formItems}
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
          <Form form={selectedForm} options={options} />
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
