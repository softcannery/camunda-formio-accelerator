import React, { Component } from "react";
import { connect } from "react-redux";
import { withRouter, Redirect } from "react-router-dom";
import FormioGenericForm from "../components/FormioGenericForm";

class TaskForm extends Component {
  render() {
    const { taskId, processDefinitionId, redirect } = this.props;
    if (redirect) {
      return <Redirect to={redirect} />;
    }
    return (
      <div>
        <FormioGenericForm
          taskId={taskId}
          processDefinitionId={processDefinitionId}
        />
      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const params = ownProps.match.params;
  return {
    ...params,
    ...state.entities,
    ...state.form,
  };
};

export default withRouter(connect(mapStateToProps, {})(TaskForm));
