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
package org.softcannery.camunda;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

import jakarta.annotation.PostConstruct;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.softcannery.formio.model.SubmissionEntity;
import org.softcannery.formio.model.SubmissionValue;
import org.softcannery.formio.repository.SubmissionHistoryRepository;
import org.softcannery.formio.repository.SubmissionRepository;
import org.softcannery.formio.service.SubmissionService;
import org.softcannery.formio.utils.FormioUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class FormioContext extends AbstractMap<String, Object> implements TransactionSynchronization {

    public static final String RESOURCES = "resources";
    public static final String SUBMISSIONS = "submissions";
    public static final String HISTORY = "history";

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private SubmissionHistoryRepository submissionHistoryRepository;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private ProcessEngine processEngine;

    private final Map<String, Object> submissions = new LinkedHashMap<>();

    @Override
    public void beforeCommit(boolean readOnly) {
        // deal with stuff
    }

    @Override
    public void afterCommit() {
        // deal with stuff
    }

    @PostConstruct
    public void init() {
        TransactionSynchronizationManager.registerSynchronization(this);

        String instanceId = getCurrentProcessInstanceId();

        List<SubmissionEntity> entities = submissionRepository.findByInstanceId(instanceId);

        entities
            .stream()
            .filter(entity -> Objects.isNull(entity.getLoopCounter()))
            .forEach(entity ->
                submissions.put(
                    entity.getSubmissionName(),
                    entity.getValue().history(() -> getHistory(instanceId, entity))
                )
            );

        entities
            .stream()
            .filter(entity -> Objects.nonNull(entity.getLoopCounter()))
            .collect(
                groupingBy(
                    SubmissionEntity::getSubmissionName,
                    mapping(
                        entity ->
                            new SimpleEntry<>(
                                entity.getLoopCounter(),
                                entity.getValue().history(() -> getHistory(instanceId, entity))
                            ),
                        Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue, (v1, v2) -> v2, LinkedHashMap::new)
                    )
                )
            )
            .forEach((name, values) -> submissions.put(name, values));
    }

    public Map<String, String> getResources() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        String deploymentId = Context.getBpmnExecutionContext().getProcessDefinition().getDeploymentId();

        return Context
            .getCommandContext()
            .runWithoutAuthorization(() -> FormioUtils.getResources(repositoryService, deploymentId, environment));
    }

    private Map<String, Object> getHistory(String instanceId, SubmissionEntity submissionEntity) {
        return new SubmissionHistoryLazyMap(
            submissionHistoryRepository,
            instanceId,
            submissionEntity.getSubmissionName()
        );
    }

    public Map<String, Object> getSubmissions() {
        return this.submissions;
    }

    @Override
    public Object get(Object key) {
        final String name = Optional
            .of(key)
            .map(Object::toString)
            .orElseThrow(() -> new IllegalArgumentException("Undefined submission: " + key));

        if (RESOURCES.equals(name)) {
            return getResources();
        } else if (SUBMISSIONS.equals(name)) {
            return getSubmissions();
        }

        return submissions.computeIfAbsent(name, this::computeSubmissionValue);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return submissions.entrySet();
    }

    public static String getCurrentProcessInstanceId() {
        return Context.getBpmnExecutionContext().getExecution().getProcessInstanceId();
    }

    private SubmissionValue computeSubmissionValue(String submissionName) {
        final SubmissionValue submissionValue = new SubmissionValue();

        ExecutionEntity execution = Context.getBpmnExecutionContext().getExecution();

        Map<String, Object> metadata = submissionValue.getMetadata();
        metadata.put("processDefinitionId", execution.getProcessDefinitionId());
        metadata.put("activityId", execution.getCurrentActivityId());
        metadata.put("activityInstanceId", execution.getActivityInstanceId());

        Integer loopCounter = FormioContext.getCurrentMultiInstanceContext().map(Map.Entry::getValue).orElse(null);

        SubmissionEntity submissionEntity = submissionService.save(
            execution.getProcessInstanceId(),
            execution.getCurrentActivityId(),
            execution.getActivityInstanceId(),
            submissionName,
            submissionValue,
            loopCounter
        );

        return submissionEntity.getValue();
    }

    public static Optional<Map.Entry<String, Integer>> getCurrentMultiInstanceContext() {
        final ExecutionEntity currentExecution = Context.getBpmnExecutionContext().getExecution();
        ActivityImpl activity = currentExecution.getActivity();
        ExecutionEntity execution = currentExecution;

        while (activity != null) {
            if (activity.isMultiInstance()) {
                final IntegerValue loopCounter = currentExecution.getVariableTyped("loopCounter");

                return Optional.of(Map.entry(activity.getActivityId(), loopCounter.getValue()));
            } else {
                activity = activity.getParentFlowScopeActivity();
                execution = execution.getParent();
            }
        }

        return Optional.empty();
    }
}
