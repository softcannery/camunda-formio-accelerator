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
import React, { Fragment, PureComponent } from 'camunda-modeler-plugin-helpers/react';
import { Fill } from 'camunda-modeler-plugin-helpers/components';
import { omit, pick } from 'min-dash';

import DownloadProjectFormsModal from './DownloadProjectFormsModal';

/**
 * An example client extension plugin to enable auto saving functionality
 * into the Camunda Modeler
 */
export default class FormioDownloadProjectFormsPlugin extends PureComponent {

  constructor(props) {
    super(props);

    this.state = {};
    this.activeTab = {};
    this.catalog = {};

    this.handleConfigClosed = this.handleConfigClosed.bind(this);
  }

  componentDidMount() {
    const {
      config,
      subscribe
    } = this.props;

    this.setState({ configOpen: false });

    // subscribe to the event when the active tab changed in the application
    subscribe('app.activeTabChanged', ({ activeTab }) => {
      console.log('subscribe:app.activeTabChanged: ', activeTab);
      this.activeTab = activeTab;
    });
  }

  componentDidUpdate() {
  }

  async handleConfigClosed(config) {

    if (config) {
      const token = await this.formioLogin(config.loginUrl, config.userName, config.password);
      const forms = await this.formioFormsByTag(token, config.projectUrl, config.tags);
      for (const form of forms) {
        if (form.type === 'form') {
          const fileName = this.state.filePath + form.name + '.form';
          this.saveToFile(form, fileName);
          console.log(form.name);
        }
      }

      const catalog = {
        loginUrl: config.loginUrl,
        userName: config.userName,
        password: config.password,
        projectUrl: config.projectUrl,
        tags: config.tags
      };

      await this.setTabConfiguration(this.activeTab, catalog);

      this.showMessageDialog('Info', 'Forms downloaded from catalog');
      this.setState({ configOpen: false });
    } else {
      this.setState({ configOpen: false });
    }
  }

  async formioLogin(loginUrl, userName, password) {

    const axios = require('axios');
    let response = await axios.post(loginUrl, {
      data: {
        email: userName,
        password: password,
      } });

    return response.headers['x-jwt-token'];
  }

  async formioFormsByTag(token, projectUrl, tags) {
    const axios = require('axios');
    const options = {
      headers: { 'x-jwt-token': token }
    };
    const response = await axios.get(`${projectUrl}/form?tags__in=${tags}`, options);
    return response.data;
  }

  async getTabConfiguration(tab) {
    const credentials = await this.props.config.getForFile(tab.file, 'formio-designer-credentials');
    const data = await this.props.config.getForFile(tab.file, 'formio-designer-download-form');
    return {
      ...credentials,
      ...data
    };
  }

  async setTabConfiguration(tab, configuration) {
    await this.props.config.setForFile(tab.file, 'formio-designer-credentials',
      pick(configuration, ['loginUrl', 'userName', 'password']));

    await this.props.config.setForFile(tab.file, 'formio-designer-download-form',
      omit(configuration, ['loginUrl', 'userName', 'password']));
  }

  saveToFile(schema, filePath) {
    console.log('formio > ' + filePath);
    const fileSystem = this.props._getGlobal('fileSystem');
    const data = JSON.stringify(schema, null, 2);

    fileSystem.writeFile(filePath, { contents: data }, { encoding: 'utf8' });
  }

  async handleOpenDownloadDialog(event) {
    const self = this;

    let filePath = this.getFormFilePath();

    if (filePath == null) {
      this.showMessageDialog('Warning', 'Save process to file first before download forms');
      return;
    }

    this.catalog = await this.getTabConfiguration(this.activeTab);
    if (!this.catalog) {
      this.catalog = {
        loginUrl: 'https://formio.form.io/user/login',
        userName: '',
        password: '',
        projectUrl: '',
        tags: ''
      };
    }

    self.setState({ filePath: filePath, configOpen: true });
  }

  getFormFilePath() {
    if (this.activeTab.file) {
      const path = this.activeTab.file.path;
      if (path != null) {
        return path.substring(0, path.lastIndexOf(this.activeTab.file.name));
      }
    }
    return null;
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
      <Fill slot="toolbar" group="9_formioDesigner">
        <button type="button" onClick={ (event) => this.handleOpenDownloadDialog(event) }>Form.io Forms</button>
      </Fill>
      { this.state.configOpen && (
        <DownloadProjectFormsModal
          initValues={ this.catalog }
          onClose={ this.handleConfigClosed }
        />
      )}
    </Fragment>;
  }
}
