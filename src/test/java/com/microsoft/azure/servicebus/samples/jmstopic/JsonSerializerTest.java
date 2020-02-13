package com.microsoft.azure.servicebus.samples.jmstopic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;

import java.time.LocalDateTime;

public class JsonSerializerTest {

    private static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @org.junit.Test
    public void runApp() throws Exception {

        final LocalDateTime now = LocalDateTime.now();
        Message message = new Message();

        message.id = "aaaa";
        message.type = "bbbb";
        message.timestamp = now;

        String json = mapper.writeValueAsString(message);
        System.out.println(json);

        Message deserialized = mapper.readValue(json, Message.class);

        Assert.assertEquals(0, message.timestamp.compareTo(deserialized.timestamp));
    }

}