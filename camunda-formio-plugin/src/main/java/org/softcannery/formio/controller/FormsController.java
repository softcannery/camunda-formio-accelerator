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
package org.softcannery.formio.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.task.Task;
import org.softcannery.formio.model.FormKey;
import org.softcannery.formio.utils.FormioUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(path = "/forms", produces = "application/json")
public class FormsController {

    public static final String SUBMISSION_IN_VARIABLE = "submissionInVariable";
    public static final String SUBMISSION_OUT_VARIABLE = "submissionOutVariable";
    private final ConfigurableEnvironment env;

    @GetMapping(path = "{processDefinitionId}")
    public String getForm(
        @PathVariable String processDefinitionId,
        @RequestParam(name = "taskId", required = false) String taskId
    ) throws IOException {
        log.debug("getForm: processDefinitionId={}, taskId={}", processDefinitionId, taskId);

        ProcessEngine engine = BpmPlatform.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);

        // by default use processDefinitionKey as variable name in other case taskDefinitionKey
        String[] value = getFormKey(engine, processDefinition, taskId);
        FormKey formKey = FormioUtils.parseFormKey(value[0], value[1]);
        log.debug("getForm: formKey={}, defaultValue={}", value[0], value[1]);

        if (formKey == null) {
            return null;
        }

        String content = getResourceByName(repositoryService, processDefinition, formKey);
        if (Strings.isBlank(content)) return null;

        JsonNode formNode;
        ObjectMapper mapper = new ObjectMapper();
        if (Strings.isNotBlank(formKey.getFormId())) {
            JsonNode jsonNode = mapper.readTree(content);
            JsonNode templateNode = jsonNode.get("template");
            if (templateNode == null || templateNode.isMissingNode()) return null;

            JsonNode formsNode = templateNode.get("forms");
            if (formsNode == null || formsNode.isMissingNode()) return null;

            formNode = formsNode.get(formKey.getFormId());
        } else {
            formNode = mapper.readTree(content);
        }

        if (formNode == null || formNode.isMissingNode()) return null;

        ((ObjectNode) formNode).put(SUBMISSION_IN_VARIABLE, formKey.getVariableName());
        ((ObjectNode) formNode).put(SUBMISSION_OUT_VARIABLE, formKey.getVariableName());
        return mapper.writeValueAsString(formNode);
    }

    private String[] getFormKey(ProcessEngine engine, ProcessDefinition processDefinition, String taskId) {
        FormService formService = engine.getFormService();

        String formKey = formService.getStartFormData(processDefinition.getId()).getFormKey();

        String defaultValue = Optional
            .ofNullable(ProcessDefinitionEntity.class.cast(processDefinition).getInitial())
            .map(ActivityImpl::getActivityId)
            .orElse(null);

        if (Strings.isNotBlank(taskId)) {
            TaskService taskService = engine.getTaskService();

            Optional<Task> task = taskService
                .createTaskQuery()
                .initializeFormKeys()
                .taskId(taskId)
                .list()
                .stream()
                .findFirst();

            if (task.isPresent()) {
                Task currentTask = task.get();

                formKey = formService.getTaskFormKey(processDefinition.getId(), currentTask.getTaskDefinitionKey());
                defaultValue = currentTask.getTaskDefinitionKey();
            }
        }

        return new String[] { formKey, defaultValue };
    }

    private String getResourceByName(
        RepositoryService repositoryService,
        ProcessDefinition processDefinition,
        FormKey formKey
    ) {
        String resourceName = formKey.getFormFile();
        List<Resource> resources = repositoryService.getDeploymentResources(processDefinition.getDeploymentId());
        for (Resource resource : resources) {
            String fileName = Paths.get(resource.getName()).getFileName().toString();
            if (!fileName.equalsIgnoreCase(URLDecoder.decode(resourceName, StandardCharsets.UTF_8))) continue;

            String content = new String(resource.getBytes(), StandardCharsets.UTF_8);

            Map<String, String> metadata = Map.of(
                "processDefinitionKey",
                processDefinition.getKey(),
                "processDefinitionName",
                Optional.ofNullable(processDefinition.getName()).orElse(""),
                "taskFormId",
                Optional.ofNullable(formKey.getFormId()).orElse(formKey.getFormFile())
            );
            return Optional
                .of(content)
                .map(this::resolveEnvPlaceholders)
                .map(text -> resolveMetadataPlaceholders(text, metadata))
                .get();
        }

        return null;
    }

    protected String resolveMetadataPlaceholders(String text, Map<String, String> metadata) {
        MutablePropertySources sources = new MutablePropertySources();

        Properties properties = new Properties();

        properties.putAll(metadata);

        PropertiesPropertySource propertySource = new PropertiesPropertySource("metadata", properties);
        sources.addFirst(propertySource);

        PropertySourcesPropertyResolver propertyResolver = new PropertySourcesPropertyResolver(sources);

        return propertyResolver.resolvePlaceholders(text);
    }

    protected String resolveEnvPlaceholders(String text) {
        return env.resolvePlaceholders(text);
    }
}
