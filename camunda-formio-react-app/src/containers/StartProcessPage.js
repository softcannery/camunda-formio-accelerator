import React from "react";
import { useParams } from "react-router-dom";
import { connect } from "react-redux";
import FormioGenericForm from "../components/FormioGenericForm";

const StartProcessPage = ({ processInstanceStarted, ...props }) => {
  const { processDefinitionId } = useParams();

  if (processInstanceStarted) {
    return <div>Process instance has been started.</div>;
  } else {
    return (
        <div>
          <FormioGenericForm processDefinitionId={processDefinitionId} {...props} />
        </div>
    );
  }
};

const mapStateToProps = (state) => ({
  ...state.entities,
});

export default connect(mapStateToProps, {})(StartProcessPage);
