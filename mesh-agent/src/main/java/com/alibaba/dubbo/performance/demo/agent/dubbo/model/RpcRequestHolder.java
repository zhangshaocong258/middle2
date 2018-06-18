package com.alibaba.dubbo.performance.demo.agent.dubbo.model;


import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class RpcRequestHolder {

    // key: requestId     value: RpcFuture
    private static ConcurrentHashMap<String,RpcFuture> processingRpc = new ConcurrentHashMap<>();

    public static void put(String requestId,RpcFuture rpcFuture){
        processingRpc.put(requestId,rpcFuture);
    }

    public static RpcFuture get(String requestId){
        return processingRpc.get(requestId);
    }

    public static RpcFuture remove(String requestId){
        return processingRpc.remove(requestId);
    }

    public static int getSize() {
        return processingRpc.size();
    }
}
