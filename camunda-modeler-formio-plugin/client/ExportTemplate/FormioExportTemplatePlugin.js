/**
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership.
 *
 * Camunda licenses this file to you under the MIT; you may not use this file
 * except in compliance with the MIT License.
 */

/* eslint-disable no-unused-vars*/
import React, {
  Fragment,
  PureComponent,
} from "camunda-modeler-plugin-helpers/react";
import { Fill } from "camunda-modeler-plugin-helpers/components";

import FormioExportTemplateOverlay from "./FormioExportTemplateOverlay";

export default class FormioExportTemplatePlugin extends PureComponent {
  constructor(props) {
    super(props);

    this.state = { fileName: null };
    this.activeTab = {};
    this.catalog = {};

    this.handleConfigClosed = this.handleConfigClosed.bind(this);
    this.btnRef = React.createRef();
  }

  componentDidMount() {
    const { config, subscribe } = this.props;

    this.setState({ configOpen: false });

    // subscribe to the event when the active tab changed in the application
    subscribe("app.activeTabChanged", ({ activeTab }) => {
      console.info("subscribe:app.activeTabChanged: ", activeTab);
      this.activeTab = activeTab;
    });
  }

  componentDidUpdate() {}

  async handleConfigClosed(config) {
    if (!config) {
      this.setState({ configOpen: false });
      return;
    }

    try {
      const data = await this.formioExportTemplate(
        config.apiKey,
        config.endpoint,
        config.tag,
      );
      if (data === undefined) {
        this.showMessageDialog(
          "Error",
          `Unable download file from URL: ${config.endpoint}`,
        );
        this.setState({ configOpen: false });
        return;
      }

      let formsList = await this.getFormsList(config.apiKey, config.endpoint);

      const fileName = this.state.fileName;
      let formData = this.addProjectIdAndUrlToFormsData(
        data.data,
        config.endpoint,
        formsList,
      );
      this.saveToFile(formData, fileName);
      console.info(`Project stage exported: ${fileName}`);

      await this.setTabConfiguration(this.activeTab, config);

      this.showMessageDialog("Info", `Project exported to\n${fileName}`);
      this.setState({ configOpen: false });
    } catch (error) {
      this.showMessageDialog(
        "Error",
        `Project export failed with error message:\n${error.message}`,
      );
      console.error(error);
    }
  }

  addProjectIdAndUrlToFormsData(formsData, projectUrl, formsList) {
    let projectId = formsData.project;
    let newFormsData = { ...formsData };
    let formIds = {};

    for (const form of formsList) {
      formIds[form.name] = form._id;
    }

    if (newFormsData.template && newFormsData.template.forms) {
      for (const key in newFormsData.template.forms) {
        const form = newFormsData.template.forms[key];
        const settings = form.settings;
        form.settings = {
          project: {
            projectId,
            projectUrl,
            formId: formIds[key],
          },
          ...settings,
        };
      }
    }

    return this.replaceResourceNamewithResourceIdInFormComponents(
      newFormsData,
      formsList,
    );
  }

  replaceResourceNamewithResourceIdInFormComponents(formsData, formsList) {
    for (const form of formsList) {
      let resources;

      if (form.components) {
        resources = this.findResourceIdsInFormComponents(form.components);
      }

      if (resources && formsData.template && formsData.template.forms) {
        let templateFormsEntries = Object.entries(formsData.template.forms);

        let formIndex = templateFormsEntries.findIndex(
          (element) => element[1].name === form.name,
        );

        if (formIndex !== -1) {
          templateFormsEntries[formIndex][1].components =
            this.setResourceIdsInFormComponents(
              templateFormsEntries[formIndex][1].components,
              resources,
            );
        }

        formsData.template.forms = Object.fromEntries(templateFormsEntries);
      }
    }

    return formsData;
  }

  findResourceIdsInFormComponents(components) {
    let resourceIds = {};
    for (const component of components) {
      if (component.components) {
        let innerResources = this.findResourceIdsInFormComponents(
          component.components,
        );
        if (innerResources) {
          resourceIds[component.key] = innerResources;
        }
      } else if (component.data && component.data.resource) {
        resourceIds[component.key] = component.data.resource;
      }
    }

    return Object.values(resourceIds).length > 0 ? resourceIds : null;
  }

  setResourceIdsInFormComponents(components, formResourceIds) {
    let newComponents = [...components];

    for (const component of newComponents) {
      if (component.components && formResourceIds[component.key]) {
        component.components = this.setResourceIdsInFormComponents(
          component.components,
          formResourceIds[component.key],
        );
      } else if (formResourceIds[component.key]) {
        component.data.resource = formResourceIds[component.key];
      }
    }
    return newComponents;
  }

  async formioExportTemplate(apiKey, endpoint, tag) {
    const axios = require("axios");
    const options = {
      headers: { "x-token": apiKey },
    };

    const project = await axios.get(endpoint, options);
    const endpointURL = new URL(endpoint);

    const _tag = await axios.get(
      `${endpointURL.origin}/project/${project.data._id}/tag?tag=${tag}`,
      options,
    );
    if (_tag.data.length === 0) return undefined;

    const response = await axios.get(
      `${endpointURL.origin}/project/${project.data._id}/tag/${_tag.data[0]._id}`,
      options,
    );
    return {
      name: project.data.name,
      tag: _tag.data[0].tag,
      data: response.data,
    };
  }

  async getFormsList(apiKey, endpoint) {
    const axios = require("axios");
    const options = {
      headers: { "x-token": apiKey },
    };

    const project = await axios.get(endpoint, options);
    const endpointURL = new URL(endpoint);
    const _formsList = await axios.get(
      `${endpointURL.origin}/project/${project.data._id}/form?type=form`,
      options,
    );
    if (_formsList.data.length === 0) return undefined;

    return _formsList.data;
  }

  async getTabConfiguration(tab) {
    return await this.props.config.getForFile(
      tab.file,
      "formio-designer-import",
    );
  }

  async setTabConfiguration(tab, configuration) {
    await this.props.config.setForFile(
      tab.file,
      "formio-designer-import",
      configuration,
    );
  }

  saveToFile(json, filePath) {
    console.info("formio > " + filePath);
    const fileSystem = this.props._getGlobal("fileSystem");
    const data = JSON.stringify(json, null, 2);

    fileSystem.writeFile(filePath, { contents: data }, { encoding: "utf8" });
  }

  async handleToggleDownloadOverlay(event) {
    if (this.state.configOpen) {
      this.handleConfigClosed();
      return;
    }

    const self = this;

    let fileName = this.getTargetFileName();

    if (fileName == null) {
      this.showMessageDialog(
        "Warning",
        "Save process to file first before download forms",
      );
      return;
    }

    this.catalog = await this.getTabConfiguration(this.activeTab);
    if (!this.catalog) {
      this.catalog = {
        apiKey: "",
        endpoint: "",
        tag: "",
      };
    }

    self.setState({ fileName: fileName, configOpen: true });
  }

  getTargetFileName() {
    if (this.activeTab.file) {
      const path = this.activeTab.file.path;
      if (path != null) {
        return path.substring(0, path.lastIndexOf(".bpmn")) + ".formio";
      }
    }
    return null;
  }

  showMessageDialog(title, message) {
    const dialog = this.props._getGlobal("dialog");

    dialog.show({
      buttons: [{ id: "close", label: "Close" }],
      message: message,
      type: "info",
      title: title,
    });
  }

  render() {
    return (
      <Fragment>
        <Fill slot="status-bar__file" group="9_formioDesigner">
          <button
            type="button"
            className="btn"
            onClick={(event) => this.handleToggleDownloadOverlay(event)}
            ref={this.btnRef}
          >
            Form.io Import
          </button>
        </Fill>
        {this.state.configOpen && (
          <FormioExportTemplateOverlay
            initValues={this.catalog}
            onClose={this.handleConfigClosed}
            anchor={this.btnRef.current}
          />
        )}
      </Fragment>
    );
  }
}
