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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.softcannery.formio.model.SubmissionEntity;
import org.softcannery.formio.model.SubmissionHistoryEntity;
import org.softcannery.formio.model.SubmissionHistoryValue;
import org.softcannery.formio.model.SubmissionValue;
import org.softcannery.formio.repository.SubmissionHistoryRepository;
import org.softcannery.formio.repository.SubmissionRepository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class SubmissionService {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private final SubmissionRepository submissionRepository;
    private final SubmissionHistoryRepository historyRepository;
    private final IdentityService identityService;

    @Transactional
    public SubmissionEntity save(
        String processInstanceId,
        String currentActivityId,
        String activityInstanceId,
        String submissionName,
        SubmissionValue submissionValue,
        Integer loopCounter
    ) {
        String userName = Optional
            .ofNullable(identityService.getCurrentAuthentication())
            .map(Authentication::getUserId)
            .orElse("anonymous");

        String remoteAddress = "0.0.0.0";

        String entityId = processInstanceId + ":" + submissionName + (loopCounter != null ? ":" + loopCounter : "");

        SubmissionEntity entity = submissionRepository
            .findById(entityId)
            .orElseGet(() ->
                SubmissionEntity
                    .builder()
                    .id(entityId)
                    .instanceId(processInstanceId)
                    .taskId(currentActivityId)
                    .submissionName(submissionName)
                    .loopCounter(loopCounter)
                    .createdOn(new Date())
                    .createdBy(userName)
                    .createdFrom(remoteAddress)
                    .build()
            );

        entity.setUpdatedOn(new Date());
        entity.setUpdatedBy(userName);
        entity.setUpdatedFrom(remoteAddress);

        Map<String, Object> metadata = submissionValue.getMetadata();

        metadata.put("submissionId", entity.getId());
        metadata.put("createdOn", sdf.format(entity.getCreatedOn()));
        metadata.put("createdBy", entity.getCreatedBy());
        metadata.put("createdFrom", entity.getCreatedFrom());

        metadata.put("updatedOn", sdf.format(entity.getUpdatedOn()));
        metadata.put("updatedBy", entity.getUpdatedBy());
        metadata.put("updatedFrom", entity.getUpdatedFrom());

        entity.setValue(submissionValue);

        SubmissionHistoryEntity historyEntity = SubmissionHistoryEntity
            .builder()
            .id(UUID.randomUUID().toString())
            .submissionId(entity.getId())
            .instanceId(processInstanceId)
            .taskId(currentActivityId)
            .loopCounter(loopCounter)
            .submissionName(submissionName)
            .activityInstanceId(activityInstanceId)
            .value(new SubmissionHistoryValue(submissionValue))
            .createdOn(entity.getCreatedOn())
            .createdBy(userName)
            .createdFrom(remoteAddress)
            .build();

        historyRepository.save(historyEntity);

        return submissionRepository.save(entity);
    }
}
