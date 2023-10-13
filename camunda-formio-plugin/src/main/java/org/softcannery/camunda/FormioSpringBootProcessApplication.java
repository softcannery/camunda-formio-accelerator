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

import static org.camunda.bpm.application.ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH;
import static org.camunda.bpm.spring.boot.starter.util.GetProcessApplicationNameFromAnnotation.processApplicationNameFromAnnotation;

import jakarta.servlet.ServletContext;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.application.impl.ProcessApplicationReferenceImpl;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.engine.spring.application.SpringProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.camunda.bpm.spring.boot.starter.event.PreUndeployEvent;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.softcannery.camunda.listener.ProcessDeployedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ServletContextAware;

@Slf4j
@AutoConfiguration
public class FormioSpringBootProcessApplication extends SpringProcessApplication {

    @Value("${spring.application.name:null}")
    protected Optional<String> springApplicationName;

    protected String contextPath = "/";

    @Autowired
    protected CamundaBpmProperties camundaBpmProperties;

    @Autowired
    protected ProcessEngine processEngine;

    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    @Configuration
    static class ProcessApplicationDeploymentConfiguration {

        @Autowired
        private SpringProcessApplication springProcessApplication;

        @Autowired
        private SpringProcessEngineConfiguration processEngineConfiguration;

        @EventListener
        public void onProcessDeployedEvent(ProcessDeployedEvent event) {
            log.info("onProcessDeployedEvent: {}", event);

            String deploymentId = event.getProcessDefinition().getDeploymentId();

            processEngineConfiguration
                .getManagementService()
                .registerProcessApplication(
                    deploymentId,
                    new ProcessApplicationReferenceImpl(springProcessApplication)
                );
        }

        @EventListener
        public void onPostDeploy(PostDeployEvent event) {
            Set<String> deploymentIds = processEngineConfiguration
                .getRepositoryService()
                .createDeploymentQuery()
                .list()
                .stream()
                .map(Deployment::getId)
                .collect(Collectors.toSet());

            deploymentIds.forEach(deploymentId ->
                processEngineConfiguration
                    .getManagementService()
                    .registerProcessApplication(
                        deploymentId,
                        new ProcessApplicationReferenceImpl(springProcessApplication)
                    )
            );
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        processApplicationNameFromAnnotation(applicationContext)
            .apply(springApplicationName)
            .ifPresent(this::setBeanName);

        if (camundaBpmProperties.getGenerateUniqueProcessApplicationName()) {
            setBeanName(CamundaBpmProperties.getUniqueName(CamundaBpmProperties.UNIQUE_APPLICATION_NAME_PREFIX));
        }

        String processEngineName = processEngine.getName();
        setDefaultDeployToEngineName(processEngineName);

        RuntimeContainerDelegate.INSTANCE.get().registerProcessEngine(processEngine);

        properties.put(PROP_SERVLET_CONTEXT_PATH, contextPath);
        super.afterPropertiesSet();
    }

    @Override
    public void destroy() throws Exception {
        super.destroy();
        RuntimeContainerDelegate.INSTANCE.get().unregisterProcessEngine(processEngine);
    }

    @PostDeploy
    public void onPostDeploy(ProcessEngine processEngine) {
        eventPublisher.publishEvent(new PostDeployEvent(processEngine));
    }

    @PreUndeploy
    public void onPreUndeploy(ProcessEngine processEngine) {
        eventPublisher.publishEvent(new PreUndeployEvent(processEngine));
    }

    @ConditionalOnWebApplication
    @Configuration
    class WebApplicationConfiguration implements ServletContextAware {

        @Override
        public void setServletContext(ServletContext servletContext) {
            if (!ObjectUtils.isEmpty(servletContext.getContextPath())) {
                contextPath = servletContext.getContextPath();
            }
        }
    }
}
