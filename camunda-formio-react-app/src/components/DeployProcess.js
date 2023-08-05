import React, { Component } from "react";
import { createDeployment } from "../actions";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { Form, Button } from "semantic-ui-react";
import FileReaderInput from "react-file-reader-input";

class DeployProcess extends Component {
  handleFileReaderInput = (e, results) => {
    const files = results.map((result) => this.readFile(result));

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

      this.uploadDeployment(deployment);
    } else {
      console.error("Bpmn process diagram is required for deployment.");
    }
  };

  readFile(result) {
    const [e, file] = result;

    return {
      name: file.name,
      contents: new File([e.target.result], file.name),
    };
  }

  uploadDeployment(deployment) {
    this.props.createDeployment(deployment);
  }

  render() {
    if (!this.props.processDeployment) {
      return (
        <Form>
          <FileReaderInput
            as="binary"
            id="my-file-input"
            onChange={this.handleFileReaderInput}
            multiple
          >
            <Button primary>
              Browse Files to Deploy BPMN and Formio forms
            </Button>
          </FileReaderInput>
        </Form>
      );
    } else {
      return (
        <p>Successfully created deployment in Camunda BPMN Runtime Engine.</p>
      );
    }
  }
}

const mapStateToProps = (state, ownProps) => {
  const params = ownProps.match.params;
  return {
    ...params,
    ...state.entities,
  };
};

export default withRouter(
  connect(mapStateToProps, {
    createDeployment,
  })(DeployProcess),
);
