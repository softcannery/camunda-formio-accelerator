import * as AT from '../../constants/ActionTypes'
import { CALL_API, Schemas } from '../../middleware/api'

export const postCreateDeployment = (deployment) => {
  const {
    name,
    tenantId,
    source,
    enableDuplicateFiltering = 'true',
    files = []

  } = deployment;

  let form = new FormData()
  form.append('deployment-name', name);

  if (source) {
    form.append('deployment-source', source);
  }

  if (tenantId) {
    form.append('tenant-id', tenantId);
  }

  // make sure that we do not re-depl\oy already existing deployment
  form.append('enable-duplicate-filtering', enableDuplicateFiltering);

  files.forEach(file => {
    const type = file.name.endsWith('.bpmn') ? 'text/xml' : '';

    form.append(file.name, new Blob([ file.contents ], { type: type }), file.name);
  });

  return {
    [CALL_API]: {
      types: [ AT.PROCESS_DEPLOYMENT_REQUEST, AT.PROCESS_DEPLOYMENT_SUCCESS, AT.PROCESS_DEPLOYMENT_FAILURE ],
      endpoint: `deployment/create`,
      schema: Schemas.PROCESS_DEPLOYMENT,
      settings: {
        method: 'post',
        body: form,
        headers: {
          'Accept': 'application/json'
        }
      }
    }
  }
}

