import * as AT from "../../constants/ActionTypes";
import { CALL_API } from "../../middleware/api";

export const fetchFormDefinition = (processDefinitionId, params) => ({
  [CALL_API]: {
    types: [
      AT.FORM_DEFINITION_REQUEST,
      AT.FORM_DEFINITION_SUCCESS,
      AT.FORM_DEFINITION_FAILURE,
    ],
    endpoint: `/forms/${processDefinitionId}${params}`,
    settings: {
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    },
  },
});

export const fetchFormSubmission = (submissionId, formKey) => ({
  [CALL_API]: {
    types: [
      AT.FORM_SUBMISSION_REQUEST,
      AT.FORM_SUBMISSION_SUCCESS,
      AT.FORM_SUBMISSION_FAILURE,
    ],
    endpoint: `/submission/${submissionId}`,
    settings: {
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    },
  },
});
