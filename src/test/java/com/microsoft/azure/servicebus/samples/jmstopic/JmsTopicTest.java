package com.microsoft.azure.servicebus.samples.jmstopic;

import org.junit.Assert;

public class JmsTopicTest {
    @org.junit.Test
    public void runApp() throws Exception {
        Assert.assertEquals(0,
                JmsTopicProducer.runApp(new String[0], (connectionString) -> {
                    JmsTopicProducer app = new JmsTopicProducer();
                    try {
                        app.run(connectionString);
                        return 0;
                    } catch (Exception e) {
                        System.out.printf("%s", e.toString());
                        return 1;
                    }
                }));
        Assert.assertEquals(0,
                JmsTopicConsumer.runApp(new String[0], (connectionString) -> {
                    JmsTopicConsumer app = new JmsTopicConsumer();
                    try {
                        app.run(connectionString);
                        return 0;
                    } catch (Exception e) {
                        System.out.printf("%s", e.toString());
                        return 1;
                    }
                }));
    }

}