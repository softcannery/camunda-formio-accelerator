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

package test;

import static org.assertj.core.api.Assertions.*;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.extension.mail.notification.MailNotificationService;
import org.camunda.bpm.extension.mail.service.MailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.softcannery.example.EmbeddedTaskFormio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = { EmbeddedTaskFormio.class, MailIntegrationTest.TestConsumer.class })
public class MailIntegrationTest {

    public static final List<Mail> RECEIVED_MAILS = new ArrayList<>();

    @TestConfiguration
    static class TestConsumer {

        @Bean
        public Consumer<Mail> testConsumer() {
            return RECEIVED_MAILS::add;
        }
    }

    @RegisterExtension
    static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP_IMAP)
        .withConfiguration(GreenMailConfiguration.aConfig().withUser("test@camunda.com", "bpmn"))
        .withPerMethodLifecycle(false);

    @Autowired
    MailConfiguration mailConfiguration;

    @Autowired
    MailService mailService;

    @Autowired
    MailNotificationService mailNotificationService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void shouldMapConfigValue() {
        assertThat(mailConfiguration.getSender()).isEqualTo("from@camunda.com");
    }

    @Test
    public void shouldNotifyForReceivedMail() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        assertThat(mailNotificationService.isRunning()).isTrue();
        GreenMailUtil.sendTextEmail("test@camunda.com", "from@camunda.com", "mail-1", "body", ServerSetupTest.SMTP);
        countDownLatch.await(10, TimeUnit.SECONDS);
        assertThat(RECEIVED_MAILS.size()).isEqualTo(1);
    }

    @Test
    public void testPizzaOrderProcess() throws Exception {
        GreenMailUtil.sendTextEmail("test@camunda.com", "from@camunda.com", "Pizza Order 1", "1 x Pepperoni", ServerSetupTest.SMTP);

        runtimeService.startProcessInstanceByKey("pizzaOrderProcess");

        waitForAsyncJobs();

        List<Task> tasks = taskService.createTaskQuery().taskName("make the pizza").list();
        assertThat(tasks).isNotEmpty();

        for (Task task : tasks) {
            taskService.complete(task.getId());
        }

        tasks = taskService.createTaskQuery().taskName("deliver the pizza").list();
        assertThat(tasks).isNotEmpty();

        for (Task task : tasks) {
            taskService.complete(task.getId());
        }

        waitForAsyncJobs();

        assertThat(runtimeService.createProcessInstanceQuery().list()).isEmpty();
    }

    private void waitForAsyncJobs() throws InterruptedException {
        JobQuery jobQuery = managementService.createJobQuery().executable();

        while (jobQuery.count() > 0) {
            Thread.sleep(500);
        }
    }

}
