/**
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership.
 *
 * Camunda licenses this file to you under the MIT; you may not use this file
 * except in compliance with the MIT License.
 */

import React from 'react';

import {omit} from 'min-dash';

import css from './CamundaDeploymentModal.less';

import AuthTypes from '../shared/AuthTypes';

import {FileInput, Radio, TextInput} from '../shared/ui';

import {Overlay, Section} from 'camunda-modeler-plugin-helpers/components';

import {Field, Formik} from 'formik';


export default class CamundaDeploymentOverlay extends React.PureComponent {

  constructor(props) {
    super(props);

    this.state = {
      isAuthNeeded: true
    };

    this.valuesCache = { ...props.configuration };
  }

  componentDidMount = () => {
    const {
      subscribeToFocusChange,
      validator
    } = this.props;

    const {
      onAppFocusChange
    } = this;

    subscribeToFocusChange(onAppFocusChange);
    // validator.resetCancel();
  }

  componentWillUnmount = () => {
    this.props.unsubscribeFromFocusChange();
  }

  isConnectionError(code) {
    return code === 'NOT_FOUND' || code === 'CONNECTION_FAILED' || code === 'NO_INTERNET_CONNECTION';
  }

  checkEndpointURLConnectivity = async (skipError) => {
    // const {
    //   valuesCache,
    //   setFieldErrorCache,
    //   externalErrorCodeCache,
    //   isConnectionError
    // } = this;

    // if (isConnectionError(externalErrorCodeCache) || skipError) {

    //   const validationResult = await this.props.validator.validateConnection(valuesCache.endpoint);

    //   if (!hasKeys(validationResult)) {

    //     this.externalErrorCodeCache = null;
    //     this.props.validator.clearEndpointURLError(setFieldErrorCache);
    //   } else {

    //     const { code } = validationResult;

    //     if (isConnectionError(code) && code !== this.externalErrorCodeCache) {
    //       this.props.validator.updateEndpointURLError(code, setFieldErrorCache);
    //     }

    //     this.externalErrorCodeCache = code;
    //   }
    // }
  }

  onSetFieldValueReceived = () => {

    // Initial endpoint URL validation. Note that this is not a form validation
    // and should affect only the Endpoint URL field.
    return this.checkEndpointURLConnectivity(true);
  }

  onAppFocusChange = () => {

    // User may fix connection related errors by focusing out from app (turn on wifi, start server etc.)
    // In that case we want to check if errors are fixed when the users focuses back on to the app.
    return this.checkEndpointURLConnectivity();
  }

  onClose = (action = 'cancel', data = null, shouldOverrideCredentials = false) => {

    if (shouldOverrideCredentials) {

      const { valuesCache } = this;
      const { endpoint } = valuesCache;
      const {
        username, password, token, rememberCredentials
      } = endpoint;

      if (rememberCredentials) {
        this.props.saveCredentials({ username, password, token });
      } else {
        this.props.removeCredentials();
      }
    }

    this.props.onClose(action, data);
  }

  onSubmit = async (values, { setFieldError }) => {
    const {
      endpoint
    } = values;

    const connectionValidation = await this.props.validator.validateConnection(endpoint);

    if (!hasKeys(connectionValidation)) {
      this.externalErrorCodeCache = null;
      this.onClose('deploy', values);
    } else {

      const {
        details,
        code
      } = connectionValidation;

      if (code === 'UNAUTHORIZED') {
        this.setState({
          isAuthNeeded: true
        });
      }

      this.externalErrorCodeCache = code;
      this.props.validator.onExternalError(values.endpoint.authType, details, code, setFieldError);
    }
  }

  fieldError = (meta) => {
    return meta.error;
  }

  setAuthType = (form) => {

    return event => {

      const authType = event.target.value;

      const {
        values,
        setValues
      } = form;

      let {
        endpoint
      } = values;

      if (authType !== AuthTypes.basic) {
        endpoint = omit(endpoint, ['username', 'password']);
      }

      if (authType !== AuthTypes.bearer) {
        endpoint = omit(endpoint, ['token']);
      }

      setValues({
        ...values,
        endpoint: {
          ...endpoint,
          authType
        }
      });
    };

  }

  onAuthDetection = (isAuthNeeded) => {
    this.setState({
      isAuthNeeded
    });
  }

  render() {

    const {
      fieldError,
      onSubmit,
      onClose,
      onSetFieldValueReceived,
      onAuthDetection
    } = this;

    const {
      configuration: values,
      validator,
      title,
      intro,
      primaryAction
    } = this.props;

    return (
      <Overlay
        className={css.DeploymentConfigModal}
        onClose={() => onClose('cancel', null, false)}
        anchor={this.props.anchor}
        minWidth={500}
      >
        <Formik
          initialValues={values}
          onSubmit={onSubmit}
          validateOnBlur={false}
          validateOnMount
        >
          {form => {

            this.valuesCache = { ...form.values };
            if (!this.setFieldErrorCache) {
              this.setFieldErrorCache = form.setFieldError;
              onSetFieldValueReceived();
            }

            return (
              <form onSubmit={form.handleSubmit}>

                <Overlay.Title>
                  {
                    title || 'Deploy Diagram to Camunda Platform'
                  }
                </Overlay.Title>
                <Section>
                  <Section.Body>

                    {
                      intro && (
                        <p className="intro">
                          {intro}
                        </p>
                      )
                    }
                    <fieldset>
                      <div className="fields">

                        <Field
                          name="deployment.name"
                          component={TextInput}
                          label="Deployment Name"
                          fieldError={fieldError}
                          validate={(value) => {
                            return validator.validateDeploymentName(value, this.isOnBeforeSubmit);
                          }}
                          autoFocus
                        />

                        <Field
                          name="deployment.tenantId"
                          component={TextInput}
                          fieldError={fieldError}
                          hint="Optional"
                          label="Tenant ID"
                        />
                      </div>
                    </fieldset>
                  </Section.Body>
                </Section>
                <Section>
                  <Section.Header>
                    Endpoint Configuration
                  </Section.Header>
                  <Section.Body>
                    <fieldset>
                      <div className="fields">

                        <Field
                          name="endpoint.url"
                          component={TextInput}
                          fieldError={fieldError}
                          label="REST Endpoint"
                          hint="Should point to a running Camunda Platform REST API endpoint."
                        // validate={ (value) => {
                        //   this.externalErrorCodeCache = null;
                        //   return validator.validateEndpointURL(
                        //     value,
                        //     form.setFieldError,
                        //     this.isOnBeforeSubmit,
                        //     onAuthDetection,
                        //     (code) => { this.externalErrorCodeCache = code; }
                        //   );
                        // } }
                        />

                        <Field
                          name="endpoint.authType"
                          label="Authentication"
                          component={Radio}
                          onChange={(event) => {
                            form.handleChange(event);
                            this.setAuthType(form);
                          }}
                          values={
                            [
                              { value: AuthTypes.basic, label: 'HTTP Basic' },
                              { value: AuthTypes.bearer, label: 'Bearer token' }
                            ]
                          }
                        />

                        {form.values.endpoint.authType === AuthTypes.basic && (<React.Fragment>
                          <Field
                            name="endpoint.username"
                            component={TextInput}
                            fieldError={fieldError}
                            validate={(value) => {
                              return validator.validateUsername(value || '', this.isOnBeforeSubmit);
                            }}
                            label="Username"
                          />
                          <Field
                            name="endpoint.password"
                            component={TextInput}
                            fieldError={fieldError}
                            validate={(value) => {
                              return validator.validatePassword(value || '', this.isOnBeforeSubmit);
                            }}
                            label="Password"
                            type="password"
                          />
                        </React.Fragment>)}

                        {form.values.endpoint.authType === AuthTypes.bearer && (<Field
                          name="endpoint.token"
                          component={TextInput}
                          fieldError={fieldError}
                          validate={(value) => {
                            return validator.validateToken(value || '', this.isOnBeforeSubmit);
                          }}
                          label="Token"
                        />)}

                        {/* {
                        isAuthNeeded && (
                          <Field
                            name="endpoint.rememberCredentials"
                            component={ CheckBox }
                            type="checkbox"
                            label="Remember credentials"
                          />
                        )
                      } */}
                      </div>
                    </fieldset>

                  </Section.Body>
                </Section>
                <Section
                >
                  <Section.Header>
                    Include additional files
                  </Section.Header>
                  <Section.Body>
                    <fieldset>
                      <Field
                        name="deployment.attachments"
                        component={FileInput}
                        label="Select files"
                        validate={validator.validateAttachments}
                      />
                    </fieldset>
                    <Section.Actions>
                      <button
                        type="submit"
                        className="btn btn-primary"
                        disabled={form.isSubmitting}
                        onClick={() => {

                          // @oguz:
                          // this is a hack as FormIK does not seem to
                          // set isSubmitting when this button is clicked.
                          // if you come up with a better solution, please
                          // do a PR.
                          this.isOnBeforeSubmit = true;
                          setTimeout(() => {
                            this.isOnBeforeSubmit = false;
                          });
                        }}
                      >
                        {primaryAction || 'Deploy'}
                      </button>
                    </Section.Actions>
                  </Section.Body>

                </Section>
              </form>
            );
          }}
        </Formik>
      </Overlay>
    );
  }
}

function hasKeys(obj) {
  return obj && Object.keys(obj).length > 0;
}
