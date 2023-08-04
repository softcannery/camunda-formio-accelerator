import * as AT from "../../constants/ActionTypes";
import { CALL_API, Schemas } from "../../middleware/api";

export const fetchTasks = () => ({
  [CALL_API]: {
    types: [AT.TASKS_REQUEST, AT.TASKS_SUCCESS, AT.TASKS_FAILURE],
    endpoint: "task?sortBy=created&sortOrder=desc",
    schema: Schemas.TASK_ARRAY,
    settings: {
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    },
  },
});

export const fetchTaskFormKey = (taskId) => ({
  [CALL_API]: {
    types: [
      AT.TASK_FORM_KEY_REQUEST,
      AT.TASK_FORM_KEY_SUCCESS,
      AT.TASK_FORM_KEY_FAILURE,
    ],
    endpoint: `task/${taskId}/form`,
    schema: Schemas.FORM_KEY,
    settings: {
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    },
  },
});

export const postCompleTask = (taskId, values) => ({
  [CALL_API]: {
    types: [
      AT.TASK_SUBMITTED_REQUEST,
      AT.TASK_SUBMITTED_SUCCESS,
      AT.TASK_SUBMITTED_FAILURE,
    ],
    endpoint: `task/${taskId}/complete`,
    schema: Schemas.PROCESS_INSTANCE_STARTED,
    settings: {
      method: "post",
      body: JSON.stringify(values),
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    },
  },
});

export const fetchTaskVariables = (taskId, variableNames) => {
  const variableName = Object.keys(variableNames).join(",");
  return {
    [CALL_API]: {
      types: [
        AT.TASK_VARIABLES_REQUEST,
        AT.TASK_VARIABLES_SUCCESS,
        AT.TASK_VARIABLES_FAILURE,
      ],
      endpoint: `task/${taskId}/form-variables?deserializeValues=false&variableNames=${variableName}`,
      schema: Schemas.TASK_VARIABLES,
      skipCamelize: true,
      settings: {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
    },
  };
};

export const fetchTaskAllVariables = (taskId) => {
  return {
    [CALL_API]: {
      types: [
        AT.TASK_VARIABLES_REQUEST,
        AT.TASK_VARIABLES_SUCCESS,
        AT.TASK_VARIABLES_FAILURE,
      ],
      endpoint: `task/${taskId}/form-variables`,
      schema: Schemas.TASK_VARIABLES,
      settings: {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
    },
  };
};
