import React, { Component } from "react";
import { connect } from "react-redux";
import { Link, Routes, Route, useParams, BrowserRouter } from "react-router-dom";
import { Container, Header } from "semantic-ui-react";
import BPMNDiagram from "../components/BPMNDiagram";
import List from "../components/List";
import { loadProcessDefinitionsWithXML } from "../actions";

class StartProcessListPage extends Component {
    componentWillMount() {
        this.props.loadProcessDefinitionsWithXML();
    }

    renderProcess(process) {
        return (
            <li key={process.id}>
                <Link
                    to={`/startProcess/key/${process.id}`}
                    activeClassName="active"
                >
                    {process.name} - Version {process.version}
                </Link>
                <BPMNDiagram xml={process.xml}></BPMNDiagram>
            </li>
        );
    }

    render() {
        console.log(this.props);
        const { processDefinition, processDefinitionXML } = this.props;

        if (!processDefinition) {
            return (
                <div>
                    <p>Loading process definitions...</p>
                </div>
            );
        } else {
            Object.keys(processDefinition).forEach((id) => {
                if (processDefinitionXML && processDefinitionXML[id]) {
                    processDefinition[id].xml = processDefinitionXML[id].bpmn20Xml;
                }
            });

            return (
                <Container text>
                    <Header as="h2">Choose process to start</Header>
                    <List
                        renderItem={this.renderProcess}
                        items={processDefinition}
                        loadingLabel={`Loading process definitions...`}
                    />
                </Container>
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
    loadProcessDefinitionsWithXML,
})(StartProcessListPage);

// // Wrap your root component with BrowserRouter
// ReactDOM.render(
//     <BrowserRouter>
//         <App />
//     </BrowserRouter>,
//     document.getElementById("root")
// );