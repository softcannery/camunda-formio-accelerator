import React, {Component} from 'react'
import {withRouter} from 'react-router-dom'
import {completeTask, fetchFormDefinition, loadTaskVariables, startProcessInstance} from '../actions'
import {connect} from 'react-redux'
import FormioForm from './FormioForm'

class FormioGenericForm extends Component {
    constructor(props) {
        super(props)
        this.state = { submission: '', loaded: false, formValid: false };
        this.handleStartInstance = this.handleStartInstance.bind(this);
        this.handleComplete = this.handleComplete.bind(this);
        this.handleSubmissionChange = this.handleSubmissionChange.bind(this);
    }

    componentDidMount() {
        this.loadFormData();
    }

    componentDidUpdate(prevProps, prevState) {
        if (!this.state.loaded ||
            this.props.taskId !== prevProps.taskId ||
            this.props.processDefinitionId !== prevProps.processDefinitionId)
        {
            this.loadFormData()
        }
    }

    renderStartForm(formSchema) {
        return (
            <div className="generic-form">
                <FormioForm formSchema={formSchema}
                            formValid={this.state.formValid}
                            onSubmit={this.handleStartInstance}
                            onSubmissionChange={this.handleSubmissionChange} />
            </div>
        )
    }

    renderTaskForm(formSchema, formSubmission) {
        return (
            <div className="generic-form">
                <FormioForm formSchema={formSchema}
                            formSubmission={formSubmission}
                            formValid={this.state.formValid}
                            onSubmit={this.handleComplete}
                            onSubmissionChange={this.handleSubmissionChange} />
            </div>
        )
    }

    render() {
        const { taskId } = this.props

        if (!this.state.formSchema) {
            return ( <div>Loading Form Schema...</div> )
        }

        if (taskId == null) {
            return this.renderStartForm(this.state.formSchema)
        } else {
            return this.renderTaskForm(this.state.formSchema, this.state.formSubmission)
        }
    }

    loadFormData() {
        const { taskId, processDefinitionId, dispatch } = this.props;
        const self = this;
        this.setState({ loaded: true });

        dispatch(fetchFormDefinition(processDefinitionId, taskId)).then((formDefinition) => {
            const formSchema = formDefinition.response;

            if (taskId == null) {
                self.setState({ formSchema: formSchema });
                return
            }

            let variables = {};
            let submission = { data: {} };
            variables[formSchema.submissionInVariable] = null
            for (let c of formSchema.components) {
                variables[c.key] = null;
            }
            dispatch(loadTaskVariables(taskId, variables)).then((formVariables) => {
                variables = formVariables.response.entities.taskVariables.variables;
                submission = JSON.parse(variables[formSchema.submissionInVariable] || "{}");
                submission = this.updateSubmission(formSchema, submission, variables);
                self.setState({ formSchema: formSchema, formSubmission: submission });
            });
        });
    }

    updateSubmission(formSchema, submission, variables) {
        let sub = submission
        for (let c of formSchema.components) {
            if (c.key in variables) {
                const value = variables[c.key]
                if (value !== undefined && value != null)
                    sub.data[c.key] = value
            }
        }
        return sub
    }

    handleSubmissionChange(e) {
        this.setState({ submission: e, formValid: e.isValid });
    };

    handleComplete(values, dispatch) {
        const { taskId } = this.props
        const form = this.state.formSchema
        const body = this.getBody(form, { data: this.state.submission.data } )
        return dispatch(completeTask(taskId, body))
    }

    handleStartInstance(values, dispatch) {
        const { processDefinitionId } = this.props
        const form = this.state.formSchema
        const body = this.getBody(form, { data: this.state.submission.data } )
        return dispatch(startProcessInstance(processDefinitionId, body))
    }

    getBody(formSchema, submission) {
        let variables = { }
        variables[formSchema.submissionOutVariable] = { 'value': JSON.stringify(submission), 'type': 'json' };
        return {
            'variables': variables
        }
    }
}

const mapStateToProps = (state, ownProps) => {
    const params = ownProps.match.params
    return {
        ...params,
        ...state.entities,
        ...ownProps
    }
}

FormioGenericForm = withRouter(connect(mapStateToProps)(FormioGenericForm))

export default FormioGenericForm
