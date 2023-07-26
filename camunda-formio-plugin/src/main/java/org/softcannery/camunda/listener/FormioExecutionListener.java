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
package org.softcannery.camunda.listener;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.softcannery.camunda.FormioContext;
import org.softcannery.formio.model.FormKey;
import org.softcannery.formio.model.SubmissionValue;
import org.softcannery.formio.service.SubmissionService;
import org.softcannery.formio.utils.FormioUtils;
import org.springframework.context.ApplicationContext;

@Slf4j
@Builder
public class FormioExecutionListener implements ExecutionListener {

    private final ApplicationContext applicationContext;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        if (applicationContext == null) {
            log.warn("ApplicationContext not set.  Something went wrong.");
            return;
        }

        FormKey formKey = getFormKey(
            execution.getProcessEngine(),
            execution.getProcessDefinitionId(),
            execution.getCurrentActivityId()
        );

        SubmissionValue submissionValue = getSubmissionValue(execution, formKey.getVariableName());

        Map<String, Object> metadata = submissionValue.getMetadata();
        metadata.put("processDefinitionId", execution.getProcessDefinitionId());
        metadata.put("taskId", execution.getCurrentActivityId());
        metadata.put("activityInstanceId", execution.getActivityInstanceId());
        metadata.put("formId", formKey.getFormId());
        metadata.put("formFile", formKey.getFormFile());

        Integer loopCounter = FormioContext.getCurrentMultiInstanceContext().map(Map.Entry::getValue).orElse(null);

        SubmissionService submissionService = applicationContext.getBean(SubmissionService.class);
        submissionService.save(
            execution.getProcessInstanceId(),
            execution.getCurrentActivityId(),
            execution.getActivityInstanceId(),
            formKey.getOutputSubmissionName(),
            submissionValue,
            loopCounter
        );

        if (execution.hasVariable(formKey.getVariableName())) {
            execution.removeVariable(formKey.getVariableName());
        }
    }

    private SubmissionValue getSubmissionValue(DelegateExecution execution, String variableName) {
        if (!execution.hasVariable(variableName)) return new SubmissionValue();

        JsonValue submissionValue = execution.getVariableTyped(variableName);
        if (submissionValue == null || Strings.isBlank(submissionValue.toString())) return new SubmissionValue();

        return submissionValue.getValue().mapTo(SubmissionValue.class);
    }

    private FormKey getFormKey(ProcessEngine engine, String processDefinitionId, String taskDefinitionKey) {
        FormService formService = engine.getFormService();
        RepositoryService repositoryService = engine.getRepositoryService();

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(
            processDefinitionId
        );

        if (!processDefinition.getInitial().getActivityId().equals(taskDefinitionKey)) {
            return FormioUtils.parseFormKey(
                formService.getTaskFormKey(processDefinitionId, taskDefinitionKey),
                taskDefinitionKey
            );
        } else {
            return FormioUtils.parseFormKey(
                formService.getStartFormData(processDefinitionId).getFormKey(),
                taskDefinitionKey
            );
        }
    }
}
