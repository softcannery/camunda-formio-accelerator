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
package org.softcannery.formio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.cibseven.bpm.BpmPlatform;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.RepositoryService;
import org.cibseven.bpm.engine.TaskService;
import org.cibseven.bpm.engine.repository.ProcessDefinition;
import org.cibseven.bpm.engine.repository.Resource;
import org.cibseven.bpm.engine.task.Task;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class FormsController {

    public static final String SUBMISSION_VARIABLE = "submissionVariable";
    private final ConfigurableEnvironment env;

    @GetMapping(path = "/forms/{processDefinitionId}")
    public String getForm(
        @PathVariable String processDefinitionId,
        @RequestParam(name = "id", required = false) String id,
        @RequestParam(name = "taskId", required = false) String taskId,
        @RequestParam(name = "file") String file
    ) throws IOException {
        log.debug("getForm.deploymentId: {}, id={}, file={}", processDefinitionId, id, file);

        ProcessEngine engine = BpmPlatform.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();

        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);

        // by default use processDefinitionKey as variable name in other case taskDefinitionKey
        String submissionVariable = processDefinition.getKey();
        if (taskId != null) {
            TaskService taskService = engine.getTaskService();
            Optional<Task> task = taskService.createTaskQuery().taskId(taskId).list().stream().findFirst();
            if (task.isPresent()) {
                submissionVariable = task.get().getTaskDefinitionKey();
            }
        }

        String content = getResourceByName(repositoryService, processDefinition, file);
        if (Strings.isBlank(content)) return null;

        JsonNode formNode;
        ObjectMapper mapper = new ObjectMapper();
        if (Strings.isNotBlank(id)) {
            JsonNode jsonNode = mapper.readTree(content);
            JsonNode templateNode = jsonNode.get("template");
            if (templateNode == null || templateNode.isMissingNode()) return null;

            JsonNode formsNode = templateNode.get("forms");
            if (formsNode == null || formsNode.isMissingNode()) return null;

            formNode = formsNode.get(id);
        } else {
            formNode = mapper.readTree(content);
        }

        if (formNode == null || formNode.isMissingNode()) return null;
        ((ObjectNode) formNode).put(SUBMISSION_VARIABLE, submissionVariable);
        return mapper.writeValueAsString(formNode);
    }

    private String getResourceByName(
        RepositoryService repositoryService,
        ProcessDefinition processDefinition,
        String resourceName
    ) {
        List<Resource> resources = repositoryService.getDeploymentResources(processDefinition.getDeploymentId());
        for (Resource resource : resources) {
            String fileName = Paths.get(resource.getName()).getFileName().toString();
            if (!fileName.equalsIgnoreCase(resourceName)) continue;

            String content = new String(resource.getBytes(), StandardCharsets.UTF_8);
            return env.resolvePlaceholders(content);
        }
        return null;
    }
}
