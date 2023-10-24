import React, { Component } from "react";
import { createDeployment } from "../actions";
import { connect } from "react-redux";
import { useParams, BrowserRouter } from "react-router-dom";
import {
  completeTask,
  fetchFormDefinition,
  loadTaskVariables,
  startProcessInstance,
} from "../actions";
import FormioForm from "./FormioForm";

class FormioGenericForm extends Component {
  constructor(props) {
    super(props);
    this.state = { submission: "", loaded: false, formValid: false };
    this.handleStartInstance = this.handleStartInstance.bind(this);
    this.handleComplete = this.handleComplete.bind(this);
    this.handleSubmissionChange = this.handleSubmissionChange.bind(this);
  }

  componentDidMount() {
    this.loadFormData();
  }

  componentDidUpdate(prevProps, prevState) {
    if (
        !this.state.loaded ||
        this.props.taskId !== prevProps.taskId ||
        this.props.processDefinitionId !== prevProps.processDefinitionId
    ) {
      this.loadFormData();
    }
  }

  renderStartForm(formSchema) {
    return (
        <div className="generic-form">
          <FormioForm
              formSchema={formSchema}
              formValid={this.state.formValid}
              onSubmit={this.handleStartInstance}
              onSubmissionChange={this.handleSubmissionChange}
          />
        </div>
    );
  }

  renderTaskForm(formSchema, formSubmission) {
    return (
        <div className="generic-form">
          <FormioForm
              formSchema={formSchema}
              formSubmission={formSubmission}
              formValid={this.state.formValid}
              onSubmit={this.handleComplete}
              onSubmissionChange={this.handleSubmissionChange}
          />
        </div>
    );
  }

  render() {
    const { taskId } = useParams();

    if (!this.state.formSchema) {
      return <div>Loading Form Schema...</div>;
    }

    if (taskId == null) {
      return this.renderStartForm(this.state.formSchema);
    } else {
      return this.renderTaskForm(
          this.state.formSchema,
          this.state.formSubmission,
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

export default connect(mapStateToProps, {
  createDeployment,
})(FormioGenericForm);

// // Wrap your root component with BrowserRouter
// ReactDOM.render(
//     <BrowserRouter>
//       <App />
//     </BrowserRouter>,
//     document.getElementById("root")
// );
