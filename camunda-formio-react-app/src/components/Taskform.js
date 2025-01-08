import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { connect } from "react-redux";
import FormioGenericForm from "../components/FormioGenericForm";

const TaskForm = ({ redirect, ...props }) => {
  const navigate = useNavigate();
  const params = useParams();
  const { taskId, processDefinitionId } = params;

  if (redirect) {
    navigate(redirect);
    return null;
  }

  return (
      <div>
        <FormioGenericForm
            taskId={taskId}
            processDefinitionId={processDefinitionId}
            {...props}
        />
      </div>
  );
};

const mapStateToProps = (state) => ({
  ...state.entities,
  ...state.form,
});

export default connect(mapStateToProps, {})(TaskForm);
