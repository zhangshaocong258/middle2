package com.alibaba.dubbo.performance.demo.agent.agent.model;/**
 * Created by msi- on 2018/5/13.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: TcpProject
 * @description:
 * @author: XSL
 * @create: 2018-05-13 20:46
 **/

public class Holder {
    private final static ConcurrentHashMap<String,AgentFuture<MessageResponse>> requestMap  = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String,Long> timeMap = new ConcurrentHashMap<>();
    public static AgentFuture<MessageResponse> removeRequest(String key){
        return requestMap.remove(key);
    }
    public static void putRequest(String key, AgentFuture<MessageResponse> future) {
        requestMap.put(key,future);
//        timeMap.put(key,System.nanoTime());
    }
    public static Long removeTime(String key) {
        return timeMap.remove(key);
    }
    public static int getSize() {
        return requestMap.size();
    }

}
