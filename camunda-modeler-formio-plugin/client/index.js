/**
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership.
 *
 * Camunda licenses this file to you under the MIT; you may not use this file
 * except in compliance with the MIT License.
 */

import { registerClientExtension, registerBpmnJSPlugin } from 'camunda-modeler-plugin-helpers';

import FormioDesignerPlugin from './FormioDesignerPlugin';
//import FormioDownloadProjectFormsPlugin from './ExportForms/FormioDownloadProjectFormsPlugin';
import FormioExportTemplatePlugin from './ExportTemplate/FormioExportTemplatePlugin';
import FormioPropertyProvider from './properties-provider';
import { require } from 'ace-builds';

import * as ace from 'ace-builds';
import 'ace-builds/src-noconflict/mode-html';
import 'ace-builds/src-noconflict/mode-json';
import 'ace-builds/src-noconflict/theme-xcode';

registerClientExtension(FormioDesignerPlugin);
// registerClientExtension(FormioDownloadProjectFormsPlugin);
registerClientExtension(FormioExportTemplatePlugin);

registerBpmnJSPlugin(FormioPropertyProvider);
