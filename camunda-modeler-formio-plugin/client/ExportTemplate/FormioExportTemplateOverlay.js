/* eslint-disable no-unused-vars */
import React, { useState } from 'camunda-modeler-plugin-helpers/react';
import { Overlay, Section } from 'camunda-modeler-plugin-helpers/components';

export default function FormioExportTemplateOverlay({ initValues, onClose, anchor }) {
  const onSubmit = () => onClose({ apiKey, endpoint, tag });

  const [apiKey, setApiKey] = useState(initValues.apiKey);
  const [endpoint, setEndpoint] = useState(initValues.endpoint);
  const [tag, setTag] = useState(initValues.tag);

  return <Overlay onClose={onClose} anchor={anchor}>
    <Overlay.Title>Form.io Import</Overlay.Title>

    <Overlay.Body>
      <form id="downloadForm" onSubmit={onSubmit}>
        <fieldset>
          <div className="fields">
            <div className="form-group">
              <label for="apiKey">API Key</label>
              <input type="password" className="form-control"
                name="apiKey"
                value={apiKey}
                onChange={(event) => setApiKey(event.target.value)} />
            </div>

            <div className="form-group">
              <label for="endpoint">Endpoint</label>
              <input type="text" className="form-control"
                name="endpoint"
                value={endpoint}
                onChange={(event) => setEndpoint(event.target.value)} />
            </div>

            <div className="form-group">
              <label for="tag">Version</label>
              <input type="text" className="form-control"
                name="exportFile"
                value={tag}
                onChange={(event) => setTag(event.target.value)} />
            </div>
          </div>
        </fieldset>
      </form>
    </Overlay.Body>
    <Overlay.Footer>
      <Section>
        <Section.Actions>
          <button type="submit" class="btn btn-primary" form="downloadForm">Download</button>
        </Section.Actions>
      </Section>
    </Overlay.Footer>
  </Overlay>;
}

