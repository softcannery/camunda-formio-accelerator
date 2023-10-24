import React, { Component } from "react";
import { connect } from "react-redux";
import { Navigate, useParams } from "react-router-dom";
import FormioGenericForm from "../components/FormioGenericForm";

class TaskForm extends Component {
  render() {
    const { redirect } = this.props;
    const { taskId, processDefinitionId } = useParams();
    if (redirect) {
      return <Navigate to={redirect} />;
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

export default connect(mapStateToProps, {})(TaskForm);