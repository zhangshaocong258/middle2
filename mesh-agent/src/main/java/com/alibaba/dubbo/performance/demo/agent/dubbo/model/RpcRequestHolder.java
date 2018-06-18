package com.alibaba.dubbo.performance.demo.agent.dubbo.model;

import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageFuture;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class RpcRequestHolder {

    // key: requestId     value: RpcFuture
    private static ConcurrentHashMap<String,MessageFuture> processingRpc = new ConcurrentHashMap<>();

    public static void put(String requestId,MessageFuture rpcFuture){
        processingRpc.put(requestId,rpcFuture);
    }

    public static MessageFuture get(String requestId){
        return processingRpc.get(requestId);
    }

    public static MessageFuture remove(String requestId){
        return processingRpc.remove(requestId);
    }

    public static int getSize() {
        return processingRpc.size();
    }
}
