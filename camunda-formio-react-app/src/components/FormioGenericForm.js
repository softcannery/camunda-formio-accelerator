import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { completeTask, fetchFormDefinition, loadTaskVariables, startProcessInstance } from "../actions";
import { connect } from "react-redux";
import FormioForm from "./FormioForm";

const FormioGenericForm = ({ processDeployment, createDeployment, dispatch }) => {
  const [state, setState] = useState({ submission: "", loaded: false, formValid: false });
  const { taskId, processDefinitionId } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    loadFormData();
  }, [taskId, processDefinitionId]);

  const loadFormData = () => {
    setState((prevState) => ({ ...prevState, loaded: true }));

    dispatch(fetchFormDefinition(processDefinitionId, taskId)).then((formDefinition) => {
      const formSchema = formDefinition.response;

      if (taskId == null) {
        setState((prevState) => ({ ...prevState, formSchema: formSchema }));
        return;
      }

      let variables = {};
      let submission = { data: {} };
      variables[formSchema.submissionInVariable] = null;
      for (let c of formSchema.components) {
        variables[c.key] = null;
      }
      dispatch(loadTaskVariables(taskId, variables)).then((formVariables) => {
        variables = formVariables.response.entities.taskVariables.variables;
        submission = JSON.parse(variables[formSchema.submissionInVariable] || "{}");
        submission = updateSubmission(formSchema, submission, variables);
        setState((prevState) => ({ ...prevState, formSchema: formSchema, formSubmission: submission }));
      });
    });
  };

  const updateSubmission = (formSchema, submission, variables) => {
    let sub = submission;
    for (let c of formSchema.components) {
      if (c.key in variables) {
        const value = variables[c.key];
        if (value !== undefined && value != null) sub.data[c.key] = value;
      }
    }
    return sub;
  };

  const handleSubmissionChange = (e) => {
    setState((prevState) => ({ ...prevState, submission: e, formValid: e.isValid }));
  };

  const handleComplete = (values) => {
    const form = state.formSchema;
    const body = getBody(form, { data: state.submission.data });
    return dispatch(completeTask(taskId, body));
  };

  const handleStartInstance = (values) => {
    const form = state.formSchema;
    const body = getBody(form, { data: state.submission.data });
    return dispatch(startProcessInstance(processDefinitionId, body));
  };

  const getBody = (formSchema, submission) => {
    let variables = {};
    variables[formSchema.submissionOutVariable] = {
      value: JSON.stringify(submission),
      type: "json",
    };
    return {
      variables: variables,
    };
  };

  const renderStartForm = (formSchema) => (
      <div className="generic-form">
        <FormioForm
            formSchema={formSchema}
            formValid={state.formValid}
            onSubmit={handleStartInstance}
            onSubmissionChange={handleSubmissionChange}
        />
      </div>
  );

  const renderTaskForm = (formSchema, formSubmission) => (
      <div className="generic-form">
        <FormioForm
            formSchema={formSchema}
            formSubmission={formSubmission}
            formValid={state.formValid}
            onSubmit={handleComplete}
            onSubmissionChange={handleSubmissionChange}
        />
      </div>
  );

  if (!state.formSchema) {
    return <div>Loading Form Schema...</div>;
  }

  return taskId == null ? renderStartForm(state.formSchema) : renderTaskForm(state.formSchema, state.formSubmission);
};

const mapStateToProps = (state, ownProps) => ({
  ...state.entities,
});

export default connect(mapStateToProps)(FormioGenericForm);
