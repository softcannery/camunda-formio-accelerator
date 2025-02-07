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

import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.cibseven.bpm.engine.delegate.ExecutionListener;
import org.cibseven.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.cibseven.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.cibseven.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.cibseven.bpm.engine.impl.el.Expression;
import org.cibseven.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.cibseven.bpm.engine.impl.pvm.process.ActivityImpl;
import org.cibseven.bpm.engine.impl.pvm.process.ScopeImpl;
import org.cibseven.bpm.engine.impl.util.xml.Element;
import org.softcannery.formio.utils.FormioUtils;
import org.springframework.context.ApplicationContext;

@Slf4j
@Builder
public class FormioParseListener extends AbstractBpmnParseListener {

    private final ApplicationContext applicationContext;

    @Override
    public void parseStartEvent(Element element, ScopeImpl scope, ActivityImpl activity) {
        log.debug("parseStartEvent: {}", activity.getActivityId());

        registerExecutionListener(element, activity);
    }

    @Override
    public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
        log.debug(
            "parse process key {} for deployment {}",
            processDefinition.getKey(),
            processDefinition.getDeploymentId()
        );

        Set<String> dataStores = processElement
            .elements("dataStoreReference")
            .stream()
            .map(element -> element.attribute("name"))
            .collect(Collectors.toUnmodifiableSet());

        if (applicationContext != null) {
            applicationContext.publishEvent(new ProcessDeployedEvent(processDefinition));
        }
    }

    @Override
    public void parseUserTask(Element element, ScopeImpl scope, ActivityImpl activity) {
        log.debug("parseStartEvent: {}", activity.getActivityId());

        String formKey = FormioUtils.buildFormioFormKey(element);
        if (formKey != null) {
            Expression exp = FormioUtils.buildExpression(formKey);
            ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition().setFormKey(exp);
        }

        registerExecutionListener(element, activity);
    }

    private void registerExecutionListener(Element element, ActivityImpl activity) {
        log.debug("registerExecutionListener: {}", activity.getActivityId());

        String formKeyAttribute = element.attributeNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, "formKey");
        if (Strings.isBlank(formKeyAttribute)) return;

        if (!formKeyAttribute.startsWith("embedded:app:formio.html")) return;

        FormioExecutionListener listener = FormioExecutionListener
            .builder()
            .applicationContext(applicationContext)
            .build();
        activity.addListener(ExecutionListener.EVENTNAME_END, listener);
    }
}
