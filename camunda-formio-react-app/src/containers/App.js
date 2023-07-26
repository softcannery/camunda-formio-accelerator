import React from 'react'
import { connect } from 'react-redux'
import { Container, Header } from 'semantic-ui-react'
import DeployProcess from '../components/DeployProcess'

const App = ({actions, children}) => (
  <div>
    <Container text>
      <Header as='h2'>Camunda Process Form.io Application</Header>
      <p>If this is the first time you are running this application, you might want to upload an example BPMN process with Forms.
      All you need to do is to upload the example BPMN file by clicking on the following button and choose the BPMN and Formio files in ./hr-onboarding-bpmn folder</p>
      <p>After uploading the process you should go to "Start Process" and choose the "HR Onboarding".</p>
      <DeployProcess/>
    </Container>
  </div>
)

const mapStateToProps = state => ({
})

export default connect(
  mapStateToProps,
)(App)
