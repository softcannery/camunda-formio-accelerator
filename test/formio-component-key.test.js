const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const html = fs.readFileSync(
  path.join(
    __dirname,
    "../camunda-formio-plugin/src/main/resources/static/formio.html",
  ),
  "utf8",
);
const source = html.match(/<script cam-script type="text\/javascript">([\s\S]*?)<\/script>/)[1];

async function restoreForm(components, values) {
  let fetched;
  const variableManager = {
    variables: {},
    createVariable(variable) {
      this.variables[variable.name] = variable;
    },
    variableValue(name) {
      return values[name];
    },
  };
  const form = { components, on() {} };
  const context = {
    URLSearchParams,
    console: { log() {}, debug() {} },
    document: { getElementById() { return {}; } },
    $scope: {},
    camForm: {
      options: { formUrl: "formio.html?id=test", processDefinitionId: "test" },
      client: { config: { apiUri: "/camunda/api/engine" } },
      variableManager,
      on() {},
      fetchVariables(callback) { fetched = callback(null, {}); },
      mergeVariables() {},
      storeOriginalValues() {},
      restore() {},
    },
    Formio: { createForm() { return Promise.resolve(form); } },
    FormioUtils: {
      eachComponent(items, callback) { items.forEach(callback); },
    },
    inject(dependencies) {
      dependencies.at(-1)(
        { get() { return Promise.resolve({ data: { submissionInVariable: "input" } }); } },
        { appUri(value) { return "/camunda/api/engine/" + value; } },
      );
    },
  };
  new vm.Script(source, { filename: "formio.html" }).runInNewContext(context);
  await new Promise((resolve) => setImmediate(resolve));
  assert.ok(fetched, "the embedded script must restore fetched variables");
  await fetched;
  return {
    data: JSON.parse(JSON.stringify(form.submission.data)),
    variables: Object.keys(variableManager.variables),
  };
}

test("the complete embedded form script parses", () => {
  assert.doesNotThrow(() => new vm.Script(source, { filename: "formio.html" }));
});

for (const [shape, component] of [
  ["nested component", { component: { key: "answer" } }],
  ["direct component", { key: "answer" }],
]) {
  test(`creates and restores variables for a ${shape}`, async () => {
    const result = await restoreForm([component], { answer: "restored" });
    assert.deepEqual(result.variables, ["input", "answer"]);
    assert.deepEqual(result.data, { answer: "restored" });
  });
}

test("keeps null and missing values out while restoring false, zero and empty text", async () => {
  const keys = ["nullValue", "missing", "falseValue", "zero", "empty"];
  const result = await restoreForm(keys.map((key) => ({ key })), {
    nullValue: null,
    falseValue: false,
    zero: 0,
    empty: "",
  });
  assert.deepEqual(result.data, { falseValue: false, zero: 0, empty: "" });
});
