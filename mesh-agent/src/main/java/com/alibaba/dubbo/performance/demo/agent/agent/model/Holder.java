package com.alibaba.dubbo.performance.demo.agent.agent.model;

import java.util.concurrent.ConcurrentHashMap;


public class Holder {
    private final static ConcurrentHashMap<String, AgentFuture<MessageResponse>> requestMap = new ConcurrentHashMap<>();

    public static AgentFuture<MessageResponse> removeRequest(String key) {
        return requestMap.remove(key);
    }

    public static void putRequest(String key, AgentFuture<MessageResponse> future) {
        requestMap.put(key, future);
    }


}
