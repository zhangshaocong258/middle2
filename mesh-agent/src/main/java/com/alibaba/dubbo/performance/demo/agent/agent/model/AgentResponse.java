package com.alibaba.dubbo.performance.demo.agent.agent.model;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;

import java.io.Serializable;


public class AgentResponse implements Serializable {
    private String messageId;
    private Object resultDesc;
    private Endpoint endpoint;
    private int executingTask;

    public AgentResponse(String messageId, Object resultDesc, Endpoint endpoint, int executingTask) {
        this.messageId = messageId;
        this.resultDesc = resultDesc;
        this.endpoint = endpoint;
        this.executingTask = executingTask;
    }

    public int getExecutingTask() {
        return executingTask;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setResultDesc(Object resultDesc) {
        this.resultDesc = resultDesc;
    }

    public String getMessageId() {
        return messageId;
    }

    public Object getResultDesc() {
        return resultDesc;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public String toString() {
        return "AgentResponse{" +
                "messageId='" + messageId + '\'' +
                ", resultDesc=" + resultDesc +
                '}';
    }
}
