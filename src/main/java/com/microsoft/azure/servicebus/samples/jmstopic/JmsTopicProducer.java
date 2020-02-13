// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus.samples.jmstopic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;
import java.util.Random;
import java.util.function.Function;

/**
 * This sample demonstrates how to send messages from a JMS Topic producer into
 * an Azure Service Bus Topic, and receive the message from a Service Bus topic
 * subscription using a message consumer that treats the subscription as a
 * JMS Queue.
 */
public class JmsTopicProducer {

    static final String SB_SAMPLES_CONNECTIONSTRING = "SB_SAMPLES_CONNECTIONSTRING";
    private static ObjectMapper mapper = new ObjectMapper();
    // number of messages to send
    private static int totalSend = 10;
    // log4j logger
    private static Logger logger = Logger.getRootLogger();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public static void main(String[] args) {

        System.exit(runApp(args, (connectionString) -> {
            JmsTopicProducer app = new JmsTopicProducer();
            try {
                app.run(connectionString);
                return 0;
            } catch (Exception e) {
                System.out.printf("%s", e.toString());
                return 1;
            }
        }));
    }

    public static int runApp(String[] args, Function<String, Integer> run) {
        try {

            String connectionString = null;

            // parse connection string from command line
            Options options = new Options();
            options.addOption(new Option("c", true, "Connection string"));
            CommandLineParser clp = new DefaultParser();
            CommandLine cl = clp.parse(options, args);
            if (cl.getOptionValue("c") != null) {
                connectionString = cl.getOptionValue("c");
            }

            // get overrides from the environment
            String env = System.getenv(SB_SAMPLES_CONNECTIONSTRING);
            if (env != null) {
                connectionString = env;
            }

            if (connectionString == null) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("run jar with", "", options, "", true);
                return 2;
            }
            return run.apply(connectionString);
        } catch (Exception e) {
            System.out.printf("%s", e.toString());
            return 3;
        }
    }

    public void run(String connectionString) throws Exception {


        // The connection string builder is the only part of the azure-servicebus SDK library
        // we use in this JMS sample and for the purpose of robustly parsing the Service Bus
        // connection string.
        ConnectionStringBuilder csb = new ConnectionStringBuilder(connectionString);

        // set up the JNDI context
        Hashtable<String, String> hashtable = new Hashtable<>();
        hashtable.put("connectionfactory.SBCF", "amqps://" + csb.getEndpoint()
                                                                .getHost() + "?amqp.idleTimeout=120000&amqp.traceFrames=true");
        hashtable.put("topic.TOPIC", "BasicTopic");
        hashtable.put("queue.SUBSCRIPTION1", "BasicTopic/Subscriptions/Subscription1");
        hashtable.put("queue.SUBSCRIPTION2", "BasicTopic/Subscriptions/Subscription2");
        hashtable.put("queue.SUBSCRIPTION3", "BasicTopic/Subscriptions/Subscription3");
        hashtable.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        Context context = new InitialContext(hashtable);

        ConnectionFactory cf = (ConnectionFactory) context.lookup("SBCF");

        // Look up the topic
        Destination topic = (Destination) context.lookup("TOPIC");

        // we create a scope here so we can use the same set of local variables cleanly
        // again to show the receive side seperately with minimal clutter
        {
            // Create Connection
            Connection connection = cf.createConnection(csb.getSasKeyName(), csb.getSasKey());
            connection.start();
            // Create Session, no transaction, client ack
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            // Create producer
            MessageProducer producer = session.createProducer(topic);

            final Random random = new Random();

            // Send messages
            for (int i = 0; i < totalSend; i++) {
                Message message1 = new Message();
                message1.id = "aaaa " + random.nextInt();
                message1.type = "bbbb" + random.nextInt();
                System.out.println(message1);
                final ObjectMessage objectMessage = session.createObjectMessage(message1);
                producer.send(objectMessage);
                System.out.printf("Sent message %d.\n", i + 1);
            }

            producer.close();
            session.close();
            connection.stop();
            connection.close();
        }

        System.out.printf("Closing queue client.\n");
    }
}
