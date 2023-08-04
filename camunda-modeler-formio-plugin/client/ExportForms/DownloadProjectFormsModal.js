/* eslint-disable no-unused-vars */
import React, { useState } from "camunda-modeler-plugin-helpers/react";
import { Modal } from "camunda-modeler-plugin-helpers/components";

// we can even use hooks to render into the application
export default function DownloadProjectFormsModal({ initValues, onClose }) {
  const onSubmit = () =>
    onClose({ loginUrl, userName, password, projectUrl, tags });

  const [loginUrl, setLoginUrl] = useState(initValues.loginUrl);
  const [userName, setUserName] = useState(initValues.userName);
  const [password, setPassword] = useState(initValues.password);
  const [projectUrl, setProjectUrl] = useState(initValues.projectUrl);
  const [tags, setTags] = useState(initValues.tags);

  // we can use the built-in styles, e.g. by adding "btn btn-primary" class names
  return (
    <Modal onClose={onClose}>
      <Modal.Title>Form.io Catalog</Modal.Title>

      <Modal.Body>
        <form id="downloadForm" onSubmit={onSubmit}>
          <fieldset>
            <div className="fields">
              <div className="form-group">
                <label for="loginUrl">Form.io Login Url</label>
                <input
                  type="text"
                  className="form-control"
                  name="loginUrl"
                  value={loginUrl}
                  onChange={(event) => setLoginUrl(event.target.value)}
                />
              </div>

              <div className="form-group">
                <label for="userName">Email</label>
                <input
                  type="text"
                  className="form-control"
                  name="userName"
                  value={userName}
                  onChange={(event) => setUserName(event.target.value)}
                />
              </div>

              <div className="form-group">
                <label for="password">Password</label>
                <input
                  type="password"
                  className="form-control"
                  name="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                />
              </div>

              <div className="form-group">
                <label for="projectUrl">Form.io project Url</label>
                <input
                  type="text"
                  className="form-control"
                  name="projectUrl"
                  value={projectUrl}
                  onChange={(event) => setProjectUrl(event.target.value)}
                />
              </div>

              <div className="form-group">
                <label for="tags">Tags</label>
                <input
                  type="text"
                  className="form-control"
                  name="tags"
                  value={tags}
                  onChange={(event) => setTags(event.target.value)}
                />
              </div>
            </div>
          </fieldset>
        </form>
      </Modal.Body>

      <Modal.Footer>
        <div id="downloadButtons" className="form-submit">
          <button type="submit" class="btn btn-primary" form="downloadForm">
            Download
          </button>
          <button
            type="button"
            class="btn btn-secondary"
            onClick={() => onClose()}
          >
            Cancel
          </button>
        </div>
      </Modal.Footer>
    </Modal>
  );
}
