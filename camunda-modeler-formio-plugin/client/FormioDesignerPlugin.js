/* eslint-disable no-unused-vars*/
import React, { Fragment, PureComponent } from 'camunda-modeler-plugin-helpers/react';
import { is, getBusinessObject } from 'bpmn-js/lib/util/ModelUtil';
import InputOutputHelper from 'bpmn-js-properties-panel/lib/helper/InputOutputHelper';
import ElementHelper from 'bpmn-js-properties-panel/lib/helper/ElementHelper';
import ExtensionElementsHelper from 'bpmn-js-properties-panel/lib/helper/ExtensionElementsHelper';
import { camelCase } from 'lodash';

import FormioFormDesigner from './FormioFormDesigner';
import FormioFormSelect from './FormioFormSelect';

const defaultState = {
  form: {},
  designerOpen: false,
};

const FORMIO_TYPE = '.formio';
const BPMN_TYPE = '.bpmn';
const FORM_KEY = 'embedded:app:formio.html';
const FORM_HANDLER_CLASS = 'org.softcannery.camunda.FormioStartFormHandler';

export default class FormioDesignerPlugin extends PureComponent {
  constructor(props) {
    super(props);

    this.state = defaultState;
    this.activeTab = {};
    this.formContent = {};
    this.selectedElement = undefined;
    this.handleFormSelectClosed = this.handleFormSelectClosed.bind(this);
    this.handleFormDesignerClosed = this.handleFormDesignerClosed.bind(this);
  }

  componentDidMount() {

    /**
    * The component props include everything the Application offers plugins,
    * which includes:
    * - config: save and retrieve information to the local configuration
    * - subscribe: hook into application events, like <tab.saved>, <app.activeTabChanged> ...
    * - triggerAction: execute editor actions, like <save>, <open-diagram> ...
    * - log: log information into the Log panel
    * - displayNotification: show notifications inside the application
    */
    const {
      config,
      subscribe
    } = this.props;

    const self = this;
    this.tabs = {};

    FormioUtils.Evaluator.noeval = true;

    // subscribe to the event when the active tab changed in the application
    subscribe('app.activeTabChanged', ({ activeTab }) => {
      console.debug('subscribe:app.activeTabChanged: ', activeTab);
      self.activeTab = activeTab;
    });

    subscribe('bpmn.modeler.created', (event) => {
      const {
        tab,
        modeler,
      } = event;

      self.modeler = modeler;
      self.tabs[tab.id] = { tab, modeler };

      const eventBus = modeler.get('eventBus');

      eventBus.on('formio.form.edit', ({ element }) => {
        console.debug('eventBus:formio.form.edit:', element);
        self.selectedElement = element;
        self.openFormDesigner(element, self.tabs[self.activeTab.id].modeler);
      });
    });
  }

  componentDidUpdate() {
  }

  saveActiveProcessDiagram() {
    const {
      displayNotification,
      triggerAction
    } = this.props;

    // trigger a tab save operation
    triggerAction('save')
      .then(tab => {
        if (!tab) {
          return displayNotification({ title: 'Failed to save' });
        }
      });
  }

  async saveToFile(schema, filePath) {
    console.debug('save > ' + filePath);
    const fileSystem = this.props._getGlobal('fileSystem');
    const data = JSON.stringify(schema, null, 2);

    await fileSystem.writeFile(filePath, { contents: data }, { encoding: 'utf8' });
  }

  async loadFromFile(filePath) {
    console.debug('load < ' + filePath);
    const fileSystem = this.props._getGlobal('fileSystem');

    try {
      return await fileSystem.readFile(filePath, { encoding: 'utf8' });
    } catch (error) {
      if (error.code === 'EISDIR') {
        return this.handleError(new Error(`Cannot open directory: ${filePath}`));
      }
      return undefined;
    }
  }

  async handleFormDesignerClosed(options) {
    this.setState({ designerOpen: false });

    console.debug(options);
    if (options) {
      let fileName = options.formName + FORMIO_TYPE;
      if (fileName === null || fileName === '') {
        fileName = this.selectedElement.businessObject.id + FORMIO_TYPE;
      }

      this.setFormName(this.state.modeler, this.selectedElement,
        fileName, null, options.varName, options.inputDS, options.outputDS);
      this.saveToFile(options.formSchema, this.getFormFilePath(fileName));
      await this.saveTab(this.activeTab);
    }
  }

  async handleFormSelectClosed(config) {
    this.setState({ selectOpen: false });

    console.debug(config);
    if (config) {
      this.setFormName(this.state.modeler, this.selectedElement,
        config.fileName, config.formId, config.varName, config.inputDS, config.outputDS);
      await this.saveTab(this.activeTab);
    }
  }

  getFormFilePath(fileName) {
    if (this.activeTab.file) {
      const path = this.activeTab.file.path;
      if (path) {
        return path.substring(0, path.lastIndexOf(this.activeTab.file.name)) + fileName;
      }
    }
    return null;
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

  setFormName(modeler, element, fileName, formId, variableName, inputDS, outputDS) {
    var bo = getBusinessObject(element);

    let inputDataSource = inputDS;
    let outputDataSource = outputDS;
    let taskDataSource = undefined;
    const currentValues = this.readFormioExtensionProperties(bo);

    const dataSources = this.getDataSources(undefined, modeler);

    if (is(element, 'bpmn:UserTask')) {
      const inDS = dataSources.find(ds => ds.id === inputDataSource);
      if (!inputDataSource || !inDS) {
        taskDataSource = this.createDataSource(element, modeler, taskDataSource, bo.name || bo.id);
        inputDataSource = getBusinessObject(taskDataSource).name;
      }
      const inputConnection = this.createDataInputAssociation(modeler, element, (inDS) ? inDS.shape : taskDataSource, currentValues);
      console.log(inputConnection);
    }

    const outDS = dataSources.find(ds => ds.id === outputDataSource);
    if (!outputDataSource || !outDS) {
      taskDataSource = this.createDataSource(element, modeler, taskDataSource, bo.name || bo.id);
      outputDataSource = getBusinessObject(taskDataSource).name;
    }
    const outputConnection = this.createDataOutputAssociation(modeler, element, (outDS) ? outDS.shape : taskDataSource, currentValues);
    console.log(outputConnection);

    bo.extensionElements = this.createExtensionElements(modeler, element,
      fileName, formId, variableName || bo.id, inputDataSource, outputDataSource);

    const modeling = modeler.get('modeling');
    if (is(element, 'bpmn:StartEvent')) {
      modeling.updateProperties(element, {
        'camunda:formKey': FORM_KEY,
        'camunda:formHandlerClass': FORM_HANDLER_CLASS,
        'extensionElements': bo.extensionElements
      });
    } else {
      modeling.updateProperties(element, {
        'camunda:formKey': FORM_KEY,
        'extensionElements': bo.extensionElements
      });
    }
  }

  readFormioExtensionProperties(businessObject) {
    let camundaProperties = ExtensionElementsHelper.getExtensionElements(businessObject, 'camunda:Properties')[0];
    if (!camundaProperties) {
      return {
        variable: businessObject.id,
        submission: businessObject.id,
        file: undefined,
        form: undefined
      };
    }

    return {
      file: this.readExtensionParameter(camundaProperties, 'formio.file'),
      form: this.readExtensionParameter(camundaProperties, 'formio.form'),
      variable: this.readExtensionParameter(camundaProperties, 'formio.variable') || businessObject.id,
      inputDS: this.readExtensionParameter(camundaProperties, 'formio.inputDataSource') || businessObject.id,
      outputDS: this.readExtensionParameter(camundaProperties, 'formio.outputDataSource') || businessObject.id
    };
  }

  createExtensionElements(modeler, element, fileName, formId, variableName, inputDS, outputDS) {
    const bpmnFactory = modeler.get('bpmnFactory');

    const bo = getBusinessObject(element);
    const formParams = this.readFormioExtensionProperties(bo);
    const parameterNameOld = formParams.variable || bo.id;
    const submissionIdOld = formParams.submission || formParams.inputDS || bo.id;

    let extensionElements = bo.get('extensionElements');

    if (!extensionElements) {
      extensionElements = ElementHelper.createElement('bpmn:ExtensionElements', { values: [] }, bo, bpmnFactory);
    }

    let camundaProperties = ExtensionElementsHelper.getExtensionElements(bo, 'camunda:Properties')[0];
    if (!camundaProperties) {
      camundaProperties = ElementHelper.createElement('camunda:Properties', { values: [] }, extensionElements, bpmnFactory);
      extensionElements.values.push(camundaProperties);
    }
    this.createOrUpdateParameter(bpmnFactory, camundaProperties, 'values', 'camunda:Property',
      'formio.variable', variableName);
    if (is(element, 'bpmn:UserTask')) {
      this.createOrUpdateParameter(bpmnFactory, camundaProperties, 'values', 'camunda:Property',
        'formio.inputDataSource', inputDS);
    }
    this.createOrUpdateParameter(bpmnFactory, camundaProperties, 'values', 'camunda:Property',
      'formio.outputDataSource', outputDS);
    this.createOrUpdateParameter(bpmnFactory, camundaProperties, 'values', 'camunda:Property',
      'formio.file', fileName || formParams.file);
    this.createOrUpdateParameter(bpmnFactory, camundaProperties, 'values', 'camunda:Property',
      'formio.form', formId || formParams.id);

    if (!InputOutputHelper.isInputOutputSupported(element)) {
      return extensionElements;
    }

    let io = InputOutputHelper.getInputOutput(bo);
    if (!io) {
      io = ElementHelper.createElement('camunda:InputOutput', { inputParameters: [] }, extensionElements, bpmnFactory);
      extensionElements.values.push(io);
    }

    // cleanup existing if name changed
    let parameter = io.inputParameters.find(p => p.name === parameterNameOld);
    if (parameter && parameter.name !== variableName) {
      parameter.name = variableName;
    }

    const parameterValue = this.defaultParameterValue(inputDS);
    parameter = io.inputParameters.find(p => p.name === variableName);
    if (parameter) {
      if (parameter.value === this.defaultParameterValue(submissionIdOld))
        parameter.value = parameterValue;
    } else {
      parameter = ElementHelper.createElement('camunda:InputParameter', {
        name: variableName,
        value: parameterValue
      }, io, bpmnFactory);
      io.inputParameters.push(parameter);
    }

    return extensionElements;
  }

  createOrUpdateParameter(bpmnFactory, parent, property, type, name, value) {
    let list = parent[property];
    if (!Array.isArray(list)) {
      return parent;
    }

    let parameter = list.find(p => p.name === name);
    if (parameter) {
      parameter.value = value;
    } else {
      parameter = ElementHelper.createElement(type, { name: name, value: value }, parent, bpmnFactory);
      list.push(parameter);
    }
    return parent;
  }

  readExtensionParameter(camundaProperties, name) {
    let list = camundaProperties['values'];
    if (!Array.isArray(list)) {
      return undefined;
    }

    let parameter = list.find(p => p.name === name);
    return (parameter) ? parameter.value : undefined;
  }

  defaultParameterValue(submissionName) {
    return `$\{formio.${submissionName}.json}`;
  }

  canAssignForm(element) {
    return is(element, 'bpmn:StartEvent') || is(element, 'bpmn:UserTask');
  }

  async saveTab(tab) {
    const {
      triggerAction
    } = this.props;

    return triggerAction('save-tab', { tab });
  }

  getDataSources(element, modeler) {
    let dataSources = [ ];
    const elementRegistry = modeler.get('elementRegistry');
    var elements = elementRegistry._elements;
    Object.keys(elements).forEach(function(key) {
      if (elements[key].element.type === 'bpmn:DataStoreReference') {
        const bo = getBusinessObject(elements[key].element);
        dataSources.push({
          id: bo.name || bo.id,
          name: bo.name || bo.id,
          businessObject: bo,
          shape: elements[key].element
        });
      }
    });

    const bo = getBusinessObject(element);
    if (bo) {
      const name = camelCase(bo.name || bo.id);
      const ds = dataSources.find(d => d.name === name);
      if (!ds) {
        dataSources.push({ id: bo.id, name: `[${name}]` });
      }
    }
    return dataSources;
  }

  createDataInputAssociation(modeler, element, dataSource, currentValues) {
    const modeling = modeler.get('modeling');
    const bo = getBusinessObject(element);

    if (bo.dataInputAssociations) {
      const association = bo.dataInputAssociations.find(a => a.sourceRef[0].name === currentValues.inputDS);
      if (association) {
        if (association.sourceRef[0] === dataSource.businessObject) {
          return association;
        } else {
          const elementRegistry = modeler.get('elementRegistry');
          const connection = elementRegistry.get(association.id);
          if (connection) {
            modeling.removeConnection(connection);
          }
        }
      }
    }
    return modeling.createConnection(dataSource, element, { type: 'bpmn:DataInputAssociation' }, element.parent);
  }

  createDataOutputAssociation(modeler, element, dataSource, currentValues) {
    const modeling = modeler.get('modeling');
    const bo = getBusinessObject(element);

    if (bo.dataOutputAssociations) {
      const association = bo.dataOutputAssociations.find(a => a.targetRef.name === currentValues.outputDS);
      if (association) {
        if (association.targetRef === dataSource.businessObject) {
          return association;
        } else {
          const elementRegistry = modeler.get('elementRegistry');
          const connection = elementRegistry.get(association.id);
          if (connection) {
            modeling.removeConnection(connection);
          }
        }
      }
    }
    return modeling.createConnection(element, dataSource, { type: 'bpmn:DataOutputAssociation' }, element.parent);
  }

  createDataSource(element, modeler, dataSource, name) {
    if (dataSource) return dataSource;

    const modeling = modeler.get('modeling');
    const shape = modeling.createShape({ type: 'bpmn:DataStoreReference' },
      {
        x: element.x + Math.floor(element.width / 2),
        y: element.y + element.height + 50
      }, element.parent);

    modeling.updateProperties(shape, { 'name': camelCase(name) });
    return shape;
  }

  async openFormDesigner(element, modeler) {
    if (!this.canAssignForm(element)) return;

    if (!this.activeTab.file.path) {
      this.showMessageDialog('Info', 'Save process to file before assign form');
      return;
    }

    const dataSources = this.getDataSources(element, modeler);

    let formSchema = {
      components: [
        {
          label: 'Text Field',
          type: 'textfield',
          inputType: 'text',
          id: 'textField'
        }
      ]
    };
    const bo = getBusinessObject(element);
    const fileName = this.getTemplateFileName(this.activeTab);
    const formReference = this.readFormioExtensionProperties(bo);
    let dataSourceRef = dataSources.find(ds => ds.id === formReference.inputDS);
    if (!dataSourceRef) {
      formReference.inputDS = dataSources[0].id;
    }
    dataSourceRef = dataSources.find(ds => ds.id === formReference.outputDS);
    if (!dataSourceRef) {
      formReference.outputDS = dataSources[0].id;
    }

    let formName = bo.id;
    let filePath = this.getTemplateFilePath(this.activeTab);

    // if form assigned
    if (formReference.file) {
      filePath = this.getFormFilePath(formReference.file);
      formName = this.replaceFileExt(formReference.file, FORMIO_TYPE, '');
    }

    const value = await this.loadFromFile(filePath);
    if (value) {
      formSchema = JSON.parse(value.contents);
    }

    const fileSystem = this.props._getGlobal('fileSystem');
    if ('template' in formSchema) {
      const formId = (Object.keys(formSchema.template.forms).length > 0)
        ? Object.keys(formSchema.template.forms)[0] : '';
      this.setState({
        selectOpen: true,
        modeler,
        formContent: {
          forms: formSchema.template.forms,
          fileName: fileName,
          formId: formReference.form || formId,
          showInput: is(element, 'bpmn:UserTask'),
          variableName: formReference.variable,
          inputDS: formReference.inputDS,
          outputDS: formReference.outputDS,
          fileSystem,
          dataSources
        }
      });
    } else {
      this.setState({
        designerOpen: true,
        modeler,
        formContent: {
          formName: formName,
          schema: formSchema,
          showInput: is(element, 'bpmn:UserTask'),
          variableName: formReference.variable,
          inputDS: formReference.inputDS,
          outputDS: formReference.outputDS,
          fileSystem,
          dataSources
        }
      });
    }
  }

  showMessageDialog(title, message) {
    const dialog = this.props._getGlobal('dialog');

    dialog.show({
      buttons: [ { id: 'close', label: 'Close' } ],
      message: message,
      type: 'info',
      title: title
    });
  }

  render() {
    return <Fragment>
      { this.state.designerOpen && (
        <FormioFormDesigner
          onClose={ this.handleFormDesignerClosed }
          config={ this.state.formContent }
        />
      )}
      { this.state.selectOpen && (
        <FormioFormSelect
          onClose={ (e) => this.handleFormSelectClosed(e) }
          config={ this.state.formContent }
        />
      )}
    </Fragment>;
  }
}
