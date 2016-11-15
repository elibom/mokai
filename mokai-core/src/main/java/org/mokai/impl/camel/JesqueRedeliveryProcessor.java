package org.mokai.impl.camel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.greghaines.jesque.Config;
import net.greghaines.jesque.ConfigBuilder;
import net.greghaines.jesque.client.Client;
import net.greghaines.jesque.client.ClientImpl;
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

    public JesqueRedeliveryProcessor(ResourceRegistry resourceRegistry) {
        this("localhost", ConfigBuilder.DEFAULT_PORT, ConfigBuilder.DEFAULT_PASSWORD);
//        this("10.0.0.120", ConfigBuilder.DEFAULT_PORT, "oAtnNRX7Z8S4rTf5GIkslso9LpselwDy7GDCskiduTG59egyaQDlH");
        this.resourceRegistry = resourceRegistry;
    }

    public JesqueRedeliveryProcessor(String redisIP, int redisPort, String redisPassword) {
        Config jesqueConfig = new Config(redisIP, redisPort, ConfigBuilder.DEFAULT_TIMEOUT, redisPassword, ConfigBuilder.DEFAULT_NAMESPACE, ConfigBuilder.DEFAULT_DATABASE);
        jesqueClient = new ClientImpl(jesqueConfig);
    }

    public void triggerJob(String jobName, Object[] args) {
        net.greghaines.jesque.Job job = new net.greghaines.jesque.Job(jobName, args);
        jesqueClient.enqueue(jobName, job);
        System.out.println("Succesfully enqueued job  " + jobName);
    }

    @Override
    public void process(Exchange exchange) {
        Message message = (Message) exchange.getIn().getBody(Message.class);
        String body = (String) message.getProperty("body");
        JsonObject jsonMessage = new JsonParser().parse(body).getAsJsonObject();
        String deliveryToken = jsonMessage.get("deliveryToken").getAsString();
        int deliverySequence = jsonMessage.get("deliverySequence").getAsInt();
        triggerJob(ReDeliverMessageJob, new Object[]{deliveryToken, deliverySequence});
        log.info("Redeliver message with deliveryToken:{} and deliverySequence:{}", new Object[]{deliveryToken, deliverySequence});
        MessageStore messageStore = resourceRegistry.getResource(MessageStore.class);
        message.setStatus(Message.STATUS_REDELIVERED);
        messageStore.saveOrUpdate(message);
    }

}
