/*
 * Camunda Platform Accelerator for Form.io Community License v1.0
 *
 * This Camunda Platform Accelerator for Form.io Community License v1.0 (“this Agreement”) sets
 * forth the terms and conditions on which Soft Cannery LTD. (“the Licensor”) makes available
 * this software (“the Software”). BY INSTALLING, DOWNLOADING, ACCESSING, USING OR DISTRIBUTING
 * THE SOFTWARE YOU INDICATE YOUR ACCEPTANCE TO, AND ARE ENTERING INTO A CONTRACT WITH,
 * THE LICENSOR ON THE TERMS SET OUT IN THIS AGREEMENT. IF YOU DO NOT AGREE TO THESE TERMS,
 * YOU MUST NOT USE THE SOFTWARE. IF YOU ARE RECEIVING THE SOFTWARE ON BEHALF OF A LEGAL ENTITY,
 * YOU REPRESENT AND WARRANT THAT YOU HAVE THE ACTUAL AUTHORITY TO AGREE TO THE TERMS AND
 * CONDITIONS OF THIS AGREEMENT ON BEHALF OF SUCH ENTITY. “Licensee” means you, an individual,
 * or the entity on whose behalf you are receiving the Software.
 *
 * Permission is hereby granted, free of charge, to the Licensee obtaining a copy of this
 * Software and associated documentation files, to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject in each case to the following conditions:
 *
 * Condition 1: If the Licensee distributes the Software or any derivative works of the Software,
 * the Licensee must attach this Agreement.
 *
 * Condition 2: Without limiting other conditions in this Agreement, the grant of rights under
 * this Agreement does not include the right to provide Commercial Product or Service. Written
 * permission from the Licensor is required to provide Commercial Product or Service.
 *
 * A “Commercial Product or Service” is software or service intended for or directed towards
 * commercial advantage or monetary compensation for the provider of the product or service
 * enabling parties to deploy and/or execute Commercial Product or Service.
 *
 * If the Licensee is in breach of the Conditions, this Agreement, including the rights granted
 * under it, will automatically terminate with immediate effect.
 *
 * SUBJECT AS SET OUT BELOW, THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * NOTHING IN THIS AGREEMENT EXCLUDES OR RESTRICTS A PARTY’S LIABILITY FOR (A) DEATH OR PERSONAL
 * INJURY CAUSED BY THAT PARTY’S NEGLIGENCE, (B) FRAUD, OR (C) ANY OTHER LIABILITY TO THE EXTENT
 * THAT IT CANNOT BE LAWFULLY EXCLUDED OR RESTRICTED.
 */
package org.softcannery.formio.utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import org.cibseven.bpm.engine.RepositoryService;
import org.cibseven.bpm.engine.impl.context.Context;
import org.cibseven.bpm.engine.impl.el.Expression;
import org.cibseven.bpm.engine.impl.el.ExpressionManager;
import org.cibseven.bpm.engine.impl.util.xml.Element;
import org.cibseven.bpm.engine.repository.Resource;
import org.softcannery.formio.model.FormKey;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public class FormioUtils {

    private static final String FORM = "form";
    private static final String FILE = "file";
    private static final String VARIABLE = "var";
    private static final String INPUT_SUBMISSION = "in";
    private static final String OUTPUT_SUBMISSION = "out";

    private static final String FORMKEY_TEMPLATE = "embedded:app:formio.html?";
    private static final String FORMKEY_URL = "/embedded/app/formio.html?";

    public static String buildFormioFormKey(Element element) {
        Element extensionElementsElement = element.element("extensionElements");
        if (extensionElementsElement == null) return null;

        Element propertiesElement = extensionElementsElement.element("properties");
        if (propertiesElement == null) return null;

        List<Element> properties = propertiesElement.elements("property");
        if (properties.size() == 0) return null;

        Map<String, String> parameters = properties
            .stream()
            .filter(p -> p.attribute("name").startsWith("formio."))
            .collect(
                Collectors.toMap(p -> p.attribute("name"), p -> Optional.ofNullable(p.attribute("value")).orElse(""))
            );

        List<String> params = new ArrayList<>();
        if (parameters.containsKey("formio.file")) params.add(FILE + "=" + parameters.get("formio.file"));
        if (parameters.containsKey("formio.form")) params.add(FORM + "=" + parameters.get("formio.form"));
        if (parameters.containsKey("formio.variable")) params.add(VARIABLE + "=" + parameters.get("formio.variable"));
        if (parameters.containsKey("formio.inputDataSource")) params.add(
            INPUT_SUBMISSION + "=" + parameters.get("formio.inputDataSource")
        );
        if (parameters.containsKey("formio.outputDataSource")) params.add(
            OUTPUT_SUBMISSION + "=" + parameters.get("formio.outputDataSource")
        );

        return FORMKEY_TEMPLATE + String.join("&", params);
    }

    public static Expression buildExpression(String expression) {
        ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
        return expressionManager.createExpression(expression);
    }

    public static FormKey parseFormKey(String url, String defaultValue) {
        if (Strings.isBlank(url) || !url.startsWith(FORMKEY_TEMPLATE)) return null;

        MultiValueMap<String, String> parameters = UriComponentsBuilder
            .fromUriString(url.replace(FORMKEY_TEMPLATE, FORMKEY_URL))
            .build()
            .getQueryParams();

        if (!parameters.containsKey(FILE)) return null;

        return FormKey
            .builder()
            .formFile(parameters.getFirst(FILE))
            .formId(firstOrDefault(parameters, FORM, null))
            .variableName(firstOrDefault(parameters, VARIABLE, defaultValue))
            .inputSubmissionName(firstOrDefault(parameters, INPUT_SUBMISSION, defaultValue))
            .outputSubmissionName(firstOrDefault(parameters, OUTPUT_SUBMISSION, defaultValue))
            .build();
    }

    private static String firstOrDefault(MultiValueMap<String, String> parameters, String name, String defaultValue) {
        if (!parameters.containsKey(name)) return defaultValue;

        String value = parameters.getFirst(name);
        return Strings.isNotBlank(value) ? value : defaultValue;
    }

    public static Map<String, String> getResources(
        RepositoryService repositoryService,
        String deploymentId,
        ConfigurableEnvironment env
    ) {
        Map<String, String> map = new LinkedHashMap<>();
        List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);
        for (Resource resource : resources) {
            String fileName = Paths.get(resource.getName()).getFileName().toString();

            if (fileName.endsWith(".formio")) {
                String content = new String(resource.getBytes(), StandardCharsets.UTF_8);
                String data = env.resolvePlaceholders(content);
                map.put(fileName, data);
            }
        }
        return map;
    }
}
