import FormioPropertyProvider from "./FormioPropertyProvider";

/**
 * A bpmn-js module, defining all extension services and their dependencies.
 *
 *
 */
export default {
  __init__: ["FormioPropertyProvider"],
  FormioPropertyProvider: ["type", FormioPropertyProvider],
};
