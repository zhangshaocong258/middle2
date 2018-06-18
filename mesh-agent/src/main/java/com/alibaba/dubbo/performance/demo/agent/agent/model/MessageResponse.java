package com.alibaba.dubbo.performance.demo.agent.agent.model;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;

import java.io.Serializable;


public class MessageResponse implements Serializable {
    private String messageId;
    private Object resultDesc;
    private Endpoint endpoint;
    private int executingTask;
    public MessageResponse(String messageId, Object resultDesc) {
        this.messageId = messageId;
        this.resultDesc = resultDesc;
    }

    public MessageResponse(String messageId, Object resultDesc, Endpoint endpoint, int executingTask) {
        this.messageId = messageId;
        this.resultDesc = resultDesc;
        this.endpoint = endpoint;
        this.executingTask = executingTask;
    }

    public MessageResponse() {
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
        return "MessageResponse{" +
                "messageId='" + messageId + '\'' +
                ", resultDesc=" + resultDesc +
                '}';
    }
}
