import React, { Component } from "react";
import { connect } from "react-redux";
import { useParams, BrowserRouter } from "react-router-dom";
import FormioGenericForm from "../components/FormioGenericForm";

class StartProcessPage extends Component {
  render() {
    const { processInstanceStarted } = this.props;
    const { processDefinitionId } = useParams();

    if (processInstanceStarted) {
      return <div>Process instance has been started.</div>;
    } else {
      return (
          <div>
            <FormioGenericForm processDefinitionId={processDefinitionId} />
          </div>
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

export default connect(mapStateToProps, {})(StartProcessPage);

// // Wrap your root component with BrowserRouter
// ReactDOM.render(
//     <BrowserRouter>
//       <App />
//     </BrowserRouter>,
//     document.getElementById("root")
// );
