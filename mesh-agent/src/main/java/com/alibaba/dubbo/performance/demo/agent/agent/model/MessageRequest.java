package com.alibaba.dubbo.performance.demo.agent.agent.model;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;

import java.io.Serializable;


public class MessageRequest implements Serializable{
    private String messageId;
    private String interfaceName;
    private String method;
    private String parameterTypesString;
    private String parameter;
    private Endpoint endpoint;
    private int executingTask;
    public MessageRequest() {
    }

    public MessageRequest(String messageId, String interfaceName, String method, String parameterTypesString, String parameter,Endpoint endpoint) {
        this.messageId = messageId;
        this.interfaceName = interfaceName;
        this.method = method;
        this.parameterTypesString = parameterTypesString;
        this.parameter = parameter;
        this.endpoint = endpoint;
        this.executingTask = 0;
    }

    public MessageRequest(String messageId, String interfaceName, String method, String parameterTypesString, String parameter) {
        this.messageId = messageId;
        this.interfaceName = interfaceName;
        this.method = method;
        this.parameterTypesString = parameterTypesString;
        this.parameter = parameter;
        this.endpoint = null;
        this.executingTask = 0;
}

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public int getExecutingTask() {
        return executingTask;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMethod() {
        return method;
    }

    public String getParameterTypesString() {
        return parameterTypesString;
    }

    public String getParameter() {
        return parameter;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public String toString() {
        return "MessageRequest{" +
                "messageId='" + messageId + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", method='" + method + '\'' +
                ", parameterTypesString='" + parameterTypesString + '\'' +
                ", parameter='" + parameter + '\'' +
                '}';
    }
}
