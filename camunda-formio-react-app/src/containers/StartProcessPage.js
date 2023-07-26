import React, { Component } from 'react'
import { connect } from 'react-redux'
import { withRouter } from 'react-router-dom'
import FormioGenericForm from '../components/FormioGenericForm'

class StartProcessPage extends Component {

    render() {
        const { processDefinitionId, processInstanceStarted } = this.props

        if (processInstanceStarted) {
            return (
                <div>Process instance has been started.</div>
            )
        } else {
            return (
                <div>
                    <FormioGenericForm processDefinitionId={processDefinitionId} />
                </div>
            )
        }
    }

}
const mapStateToProps = (state, ownProps) => {
    const params = ownProps.match.params
    return {
        ...params,
        ...state.entities
    }
}

export default withRouter(connect(mapStateToProps, {})(StartProcessPage))
