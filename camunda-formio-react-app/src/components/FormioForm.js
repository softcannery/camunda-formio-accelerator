import React from 'react';
import { reduxForm } from 'redux-form'
import { Form, Button } from 'semantic-ui-react'
import { Form as Formio } from '@formio/react';
import FileService from './FileService';

let FormioForm = props => {
    const { formSchema, formSubmission, handleSubmit, onSubmissionChange, formValid } = props
    let formRef = null;

    const handleFormReady = (instance) => {
        formRef = instance;
    }

    const handleChange = (submission) => {
        if (formRef)
            formRef.checkValidity(undefined, true, undefined, false);
        onSubmissionChange(submission);
    }

    return (
        <form onSubmit={handleSubmit}>
            <Formio form={formSchema}
                    submission={formSubmission}
                    onChange={handleChange}
                    formReady={handleFormReady}
            />
            <Form.Field disabled={!formValid} control={Button} primary type='submit'>Submit</Form.Field>
        </form>
    )
}

export default reduxForm({ form: 'formio' })(FormioForm)
