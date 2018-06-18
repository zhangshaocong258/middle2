package com.alibaba.dubbo.performance.demo.agent.agent.model;

import java.util.concurrent.ConcurrentHashMap;


public class AgentHolder {
    private final static ConcurrentHashMap<String, AgentFuture<AgentResponse>> requestMap = new ConcurrentHashMap<>();

    public static AgentFuture<AgentResponse> removeRequest(String key) {
        return requestMap.remove(key);
    }

    public static void putRequest(String key, AgentFuture<AgentResponse> future) {
        requestMap.put(key, future);
    }


}
