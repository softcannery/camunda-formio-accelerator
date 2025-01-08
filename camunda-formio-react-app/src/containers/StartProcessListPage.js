import React, { useEffect } from "react";
import { connect } from "react-redux";
import { Link, useParams } from "react-router-dom";
import { Container, Header } from "semantic-ui-react";
import BPMNDiagram from "../components/BPMNDiagram";
import List from "../components/List";
import { loadProcessDefinitionsWithXML } from "../actions";

const StartProcessListPage = ({ processDefinition, processDefinitionXML, loadProcessDefinitionsWithXML }) => {
    const { key } = useParams();

    useEffect(() => {
        loadProcessDefinitionsWithXML();
    }, [loadProcessDefinitionsWithXML]);

    const renderProcess = (process) => (
        <li key={process.id}>
            <Link to={`/startProcess/key/${process.id}`}>
                {process.name} - Version {process.version}
            </Link>
            <BPMNDiagram xml={process.xml} />
        </li>
    );

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
                    renderItem={renderProcess}
                    items={processDefinition}
                    loadingLabel={`Loading process definitions...`}
                />
            </Container>
        );
    }
};

const mapStateToProps = (state) => ({
    ...state.entities,
});

export default connect(mapStateToProps, { loadProcessDefinitionsWithXML })(StartProcessListPage);
