package org.mokai.impl.camel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Date;
import net.greghaines.jesque.client.Client;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mokai.Message;
import org.mokai.persist.MessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
public class JesqueRedeliveryProcessor implements Processor {

    private final Logger log = LoggerFactory.getLogger(JesqueRedeliveryProcessor.class);

    private ResourceRegistry resourceRegistry;

    private final Client jesqueClient;

    private static final String ReDeliverMessageJob = "reDeliverMessageJob";

    public JesqueRedeliveryProcessor(ResourceRegistry resourceRegistry, Client jesqueClient) {
        this.resourceRegistry = resourceRegistry;
        this.jesqueClient = jesqueClient;
    }

    public void triggerJob(String jobName, Object[] args) {
        net.greghaines.jesque.Job job = new net.greghaines.jesque.Job(jobName, args);
        jesqueClient.enqueue(jobName, job);
        log.info("Succesfully enqueued job  " + jobName);
    }

    public void triggerDelayedJob(String jobName, long delay, Object[] args) {
        net.greghaines.jesque.Job job = new net.greghaines.jesque.Job(jobName, args);
        long future = System.currentTimeMillis() + delay;
        jesqueClient.delayedEnqueue(jobName+"-DELAYED", job, future);
        log.info("Succesfully enqueued job {} to be executed in {}", new Object[]{jobName, new Date(future)});
    }

    @Override
    public void process(Exchange exchange) {
        Message message = (Message) exchange.getIn().getBody(Message.class);
        String body = (String) message.getProperty("body");

        JsonObject jsonMessage = new JsonParser().parse(body).getAsJsonObject();
        String deliveryToken = jsonMessage.get("deliveryToken").getAsString();
        int deliverySequence = jsonMessage.get("deliverySequence").getAsInt();
        long retryDelay = jsonMessage.get("retryDelay").getAsLong();
        triggerDelayedJob(ReDeliverMessageJob, retryDelay, new Object[]{deliveryToken, deliverySequence});

        log.info("Redeliver within {} seconds message with deliveryToken:{} and deliverySequence:{}", new Object[]{retryDelay, deliveryToken, deliverySequence});
        MessageStore messageStore = resourceRegistry.getResource(MessageStore.class);
        message.setStatus(Message.STATUS_REDELIVERED);
        messageStore.saveOrUpdate(message);
    }

}
