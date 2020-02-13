package com.microsoft.azure.servicebus.samples.jmstopic;

import java.io.Serializable;
import java.util.Map;

public class Payload implements Serializable {
    public String action;
    public String type;
    public Map<String, Object> params;

    @Override
    public String toString() {
        return "Payload{" +
                "action='" + action + '\'' +
                ", type='" + type + '\'' +
                ", params=" + params +
                '}';
    }
}
