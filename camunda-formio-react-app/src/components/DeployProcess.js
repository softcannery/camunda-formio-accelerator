import React, { useState } from "react";
import { createDeployment } from "../actions";
import { connect } from "react-redux";
import { useParams, useNavigate } from "react-router-dom";
import { Form, Button } from "semantic-ui-react";
import FileReaderInput from "react-file-reader-input";

const DeployProcess = ({ processDeployment, createDeployment }) => {
  const navigate = useNavigate();
  const params = useParams();

  const handleFileReaderInput = (e, results) => {
    const files = results.map((result) => readFile(result));

    console.info("Selected files", files);

    const bpmn = files.filter(({ name }) => name.endsWith(".bpmn"));

    if (bpmn.length > 0) {
      const diagram = bpmn[0];

      var deployment = {
        tenantId: null,
        name: diagram.name,
        source: "camunda-formio-react-app",
        files: files,
      };

      uploadDeployment(deployment);
    } else {
      console.error("Bpmn process diagram is required for deployment.");
    }
  };

  const readFile = (result) => {
    const [e, file] = result;

    return {
      name: file.name,
      contents: new File([e.target.result], file.name),
    };
  };

  const uploadDeployment = (deployment) => {
    createDeployment(deployment);
  };

  return (
      !processDeployment ? (
          <Form>
            <FileReaderInput
                as="binary"
                id="my-file-input"
                onChange={handleFileReaderInput}
                multiple
            >
              <Button primary>
                Browse Files to Deploy BPMN and Formio forms
              </Button>
            </FileReaderInput>
          </Form>
      ) : (
          <p>Successfully created deployment in Camunda BPMN Runtime Engine.</p>
      )
  );
};

const mapStateToProps = (state, ownProps) => {
  return {
    ...useParams(),
    ...state.entities,
  };
};

export default connect(mapStateToProps, { createDeployment })(DeployProcess);
