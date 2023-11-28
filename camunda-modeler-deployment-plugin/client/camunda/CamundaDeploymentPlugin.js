import React, {PureComponent} from 'react';
import {Fill} from 'camunda-modeler-plugin-helpers/components';
import CamundaDeploymentOverlay from './CamundaDeploymentOverlay';
import AuthTypes from '../shared/AuthTypes';
import generateId from '../shared/generate-id';
import {ConnectionError, default as CamundaAPI, DeploymentError} from '../shared/CamundaAPI';
import {omit} from 'min-dash';
import DeploymentConfigValidator from './validation/DeploymentConfigValidator';


const DEPLOYMENT_DETAILS_CONFIG_KEY = 'camunda-deployment-tool';
const ENGINE_ENDPOINTS_CONFIG_KEY = 'camundaEngineEndpoints';
const PROCESS_DEFINITION_CONFIG_KEY = 'process-definition';
const DEFAULT_ENDPOINT = {
  url: 'http://localhost:8080/engine-rest',
  authType: AuthTypes.basic,
  rememberCredentials: true
};
const ET_EXECUTION_PLATFORM_NAME = 'Camunda Platform';
const FORMIO_TYPE = '.formio';
const BPMN_TYPE = '.bpmn';

export default class CamundaDeploymentPlugin extends PureComponent {
  constructor(props) {
    super(props);
    this.btnRef = React.createRef();
  }

  state = {
    modalState: null,
    activeTab: null
  }

  validator = new DeploymentConfigValidator();

  componentDidMount() {
    this.props.subscribe('app.activeTabChanged', ({ activeTab }) => {
      this.setState({ activeTab });
    });

    this.props.subscribe('app.focus-changed', () => {
      if (this.focusChangeCallback) {
        this.focusChangeCallback();
      }
    });
  }

  subscribeToFocusChange = (callback) => {
    this.focusChangeCallback = callback;
  }

  unsubscribeFromFocusChange = () => {
    delete this.focusChangeCallback;
  }

  saveTab = (tab) => {
    const {
      triggerAction
    } = this.props;

    return triggerAction('save-tab', { tab });
  }

  async getSavedConfiguration(tab) {

    const tabConfig = await this.getTabConfiguration(tab);

    if (!tabConfig) {
      return undefined;
    }

    const {
      deployment,
      endpointId
    } = tabConfig;

    const deploymentWithAttachments = await this.withAttachments(deployment);

    const endpoints = await this.getEndpoints();

    return {
      deployment: deploymentWithAttachments,
      endpoint: endpoints.find(endpoint => endpoint.id === endpointId)
    };
  }

  async saveConfiguration(tab, configuration) {

    const {
      endpoint,
      deployment
    } = configuration;

    await this.saveEndpoint(endpoint);

    const tabConfiguration = {
      deployment: withSerializedAttachments(deployment),
      endpointId: endpoint.id
    };

    await this.setTabConfiguration(tab, tabConfiguration);

    return configuration;
  }

  async getDefaultConfiguration(tab, providedConfiguration = {}) {
    const endpoint = await this.getDefaultEndpoint(tab, providedConfiguration.endpoint);

    const deployment = providedConfiguration.deployment || {};
    const deploymentWithAttachments = await this.withAttachments({
      name: withoutExtension(tab.name),
      attachments: [
        {
          path: this.getTemplateFilePath(tab)
        }
      ],
      ...deployment
    });

    return {
      endpoint,
      deployment: deploymentWithAttachments
    };
  }

  async getDefaultEndpoint(tab, providedEndpoint) {

    let endpoint = {},
        defaultUrl = DEFAULT_ENDPOINT.url;

    if (providedEndpoint) {
      endpoint = providedEndpoint;
    } else {

      const existingEndpoints = await this.getEndpoints();

      if (existingEndpoints.length) {
        endpoint = existingEndpoints[0];
      }
    }

    // since we have deprecated AuthTypes.none, we should correct existing
    // configurations
    if (endpoint.authType !== AuthTypes.basic && endpoint.authType !== AuthTypes.bearer) {
      endpoint.authType = DEFAULT_ENDPOINT.authType;
    }

    return {
      ...DEFAULT_ENDPOINT,
      url: defaultUrl,
      ...endpoint,
      id: endpoint.id || generateId()
    };
  }

  async getConfigurationFromUserInput(tab, providedConfiguration, uiOptions) {
    const configuration = await this.getDefaultConfiguration(tab, providedConfiguration);

    return new Promise(resolve => {
      const handleClose = (action, configuration) => {

        this.setState({
          modalState: null
        });

        // contract: if configuration provided, user closed with O.K.
        // otherwise they canceled it
        return resolve({ action, configuration });
      };

      this.setState({
        modalState: {
          tab,
          configuration,
          handleClose,
          ...uiOptions
        }
      });
    });
  }

  getEndpoints() {
    return this.props.config.get(ENGINE_ENDPOINTS_CONFIG_KEY, []);
  }

  setEndpoints(endpoints) {
    return this.props.config.set(ENGINE_ENDPOINTS_CONFIG_KEY, endpoints);
  }

  async withAttachments(deployment) {
    const fileSystem = this.props._getGlobal('fileSystem');
    const { attachments = [] } = deployment;

    async function readFile(path) {
      try {

        // (1) try to read file from file system
        const file = await fileSystem.readFile(path, { encoding: false });

        // (2a) store contents as a File object
        // @barmac: This is required for the performance reasons. The contents retrieved from FS
        // is a Uint8Array. During the form submission, Formik builds a map of touched fields
        // and it traverses all nested objects for that. The outcome was that the form would freeze
        // for a couple of seconds when one tried to re-deploy a file of size >1MB, because Formik
        // tried to build a map with bits' indexes as keys with all values as `true`. Wrapping the
        // contents in a File object prevents such behavior.
        return {
          ...file,
          contents: new File([ file.contents ], file.name)
        };
      } catch {

        // (2b) if read fails, return an empty file descriptor
        return {
          contents: null,
          path,
          name: basename(path)
        };
      }
    }

    const files = await Promise.all(attachments.map(({ path }) => readFile(path)));

    return {
      ...deployment,
      attachments: files.filter(f => f.contents)
    };
  }

  replaceFileExt(path, srcType, dstType) {
    return (path) ? path.substring(0, path.lastIndexOf(srcType)) + dstType : null;
  }

  getTemplateFileName(tab) {
    return (tab.file && tab.file.path)
      ? this.replaceFileExt(tab.file.name, BPMN_TYPE, FORMIO_TYPE)
      : null;
  }

  getTemplateFilePath(tab) {
    return (tab.file)
      ? this.replaceFileExt(tab.file.path, BPMN_TYPE, FORMIO_TYPE)
      : null;
  }

  getTabConfiguration(tab) {
    return this.props.config.getForFile(tab.file, DEPLOYMENT_DETAILS_CONFIG_KEY);
  }

  setTabConfiguration(tab, configuration) {
    return this.props.config.setForFile(tab.file, DEPLOYMENT_DETAILS_CONFIG_KEY, configuration);
  }

  async saveEndpoint(endpoint) {

    const existingEndpoints = await this.getEndpoints();
    const updatedEndpoints = addOrUpdateById(existingEndpoints, endpoint);

    await this.setEndpoints(updatedEndpoints);
    return endpoint;
  }

  getVersion(configuration) {
    const { endpoint } = configuration;
    const api = new CamundaAPI(endpoint);
    return api.getVersion();
  }

  deploy = (options) => {
    const {
      activeTab
    } = this.state;

    return this.deployTab(activeTab, options);
  };

  async deployTab(tab, options={}) {

    const {
      configure
    } = options;

    // (1) Open save file dialog if dirty
    tab = await this.saveTab(tab);

    // (1.1) Cancel deploy if file save cancelled
    if (!tab) {
      return;
    }

    // (2) Get deployment configuration
    // (2.1) Try to get existing deployment configuration
    let configuration = await this.getSavedConfiguration(tab);

    // (2.3) Open modal to enter deployment configuration
    const {
      action,
      configuration: userConfiguration
    } = await this.getConfigurationFromUserInput(tab, configuration);

    // (2.3.1) Handle user cancelation
    if (action === 'cancel') {
      return;
    }

    configuration = await this.saveConfiguration(tab, userConfiguration);

    if (action === 'save') {
      return;
    }

    // (3) Trigger deployment
    let version;

    try {

      // (3.1) Retrieve version we deploy to via API
      try {
        version = (await this.getVersion(configuration)).version;
      } catch (error) {
        if (!(error instanceof ConnectionError)) {
          throw error;
        }
        version = null;
      }

      // (3.2) Deploy via API
      const deployment = await this.deployWithConfiguration(tab, configuration);

      // (3.3) save deployed process definition
      await this.saveProcessDefinition(tab, deployment);

      // (3.4) Handle deployment success or error
      await this.handleDeploymentSuccess(tab, deployment, version);
    } catch (error) {

      if (!(error instanceof DeploymentError)) {
        throw error;
      }

      await this.handleDeploymentError(tab, error, version);
    }
  }

  handleDeploymentError(tab, error, version) {
    const {
      log,
      displayNotification,
      triggerAction
    } = this.props;

    displayNotification({
      type: 'error',
      title: 'Deployment failed',
      content: 'See the log for further details.',
      duration: 10000
    });

    log({
      category: 'deploy-error',
      message: error.problems || error.details || error.message
    });

    // If we retrieved the executionPlatformVersion, include it in event
    const deployedTo = (version &&
      { executionPlatformVersion: version, executionPlatform: ET_EXECUTION_PLATFORM_NAME }) || undefined;

    // notify interested parties
    triggerAction('emit-event', {
      type: 'deployment.error',
      payload: {
        error,
        context: 'deploymentTool',
        ...(deployedTo && { deployedTo: deployedTo })
      }
    });
  }

  deployWithConfiguration(tab, configuration) {

    const {
      endpoint,
      deployment
    } = configuration;

    const api = new CamundaAPI(endpoint);

    return api.deployDiagram(tab.file, deployment);
  }

  async saveProcessDefinition(tab, deployment) {

    if (!deployment || !deployment.deployedProcessDefinition) {
      return;
    }

    const {
      deployedProcessDefinition: processDefinition
    } = deployment;

    const {
      config
    } = this.props;

    return await config.setForFile(tab.file, PROCESS_DEFINITION_CONFIG_KEY, processDefinition);
  }

  handleDeploymentSuccess(tab, deployment, version) {
    const {
      displayNotification,
      triggerAction
    } = this.props;

    displayNotification({
      type: 'success',
      title: 'Deployment succeeded',
      duration: 4000
    });

    // notify interested parties
    triggerAction('emit-event', {
      type: 'deployment.done',
      payload: {
        deployment,
        deployedTo: {
          executionPlatformVersion: version,
          executionPlatform: ET_EXECUTION_PLATFORM_NAME
        },
        context: 'deploymentTool'
      }
    });
  }

  render() {

    const {
      activeTab,
      modalState
    } = this.state;

    const deploy = () => this.deploy({ configure: true });
    // const deployService = new DeployService(this.deployRef);

    const handleClick = () => {
      if(modalState) {
        modalState.handleClose('cancel', null, false);
        return;
      }

      deploy();
    }

    return <React.Fragment>
      { isCamundaTab(activeTab) && (<Fill slot="status-bar__file" group="9_deploy">
        <button className='btn' type="button" onClick={ handleClick } ref={ this.btnRef }>Deploy to Camunda</button>
      </Fill>)}

      {/*<DeploymentTool*/}
      {/*  ref={ this.deployRef }*/}
      {/*  { ...this.props } />*/}

      {/*<StartInstanceTool*/}
      {/*  ref={ this.startInstanceRef }*/}
      {/*  deployService={ deployService }*/}
      {/*  { ...this.props } />*/}
      {modalState &&
        <CamundaDeploymentOverlay
          anchor={ this.btnRef.current }
          configuration={ modalState.configuration }
          activeTab={ modalState.tab }
          title={ modalState.title }
          intro={ modalState.intro }
          // primaryAction={ /* modalState.primaryAction */ }
          onClose={ modalState.handleClose }
          validator={ this.validator }
          // saveCredentials={ /* this.saveCredentials */ }
          // removeCredentials={ /* this.removeCredentials */ }
          subscribeToFocusChange={ this.subscribeToFocusChange }
          unsubscribeFromFocusChange={ this.unsubscribeFromFocusChange }
        />
      }
    </React.Fragment>;
  }
}


// helpers //////////

function withoutExtension(name) {
  return name.replace(/\.[^.]+$/, '');
}

function withoutCredentials(endpoint) {
  return omit(endpoint, ['username', 'password', 'token']);
}

function addOrUpdateById(collection, element) {

  const index = collection.findIndex(el => el.id === element.id);

  if (index !== -1) {
    return [
      ...collection.slice(0, index),
      element,
      ...collection.slice(index + 1)
    ];
  }

  return [
    ...collection,
    element
  ];
}

function isCamundaTab(tab) {
  return tab && tab.type === 'bpmn';
}

function withSerializedAttachments(deployment) {
  const { attachments: fileList = [] } = deployment;
  const attachments = fileList.map(file => ({ path: file.path }));

  return { ...deployment, attachments };
}

function basename(filePath) {
  return filePath.split('\\').pop().split('/').pop();
}
