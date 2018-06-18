package com.alibaba.dubbo.performance.demo.agent.registry;/**
 * Created by msi- on 2018/5/6.
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: dubbo-mesh
 * @description: 负载均衡实现
 * @author: XSL
 * @create: 2018-05-06 11:07
 **/

public class LoadBalanceChoice {
    private static Logger logger = LoggerFactory.getLogger(LoggerFactory.class);
    private static EtcdRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private static ConcurrentLinkedQueue<Endpoint> chooseQueue = new ConcurrentLinkedQueue();
    private static List<Endpoint> endpoints;
    private static Object lock = new Object();
    private static Map<String, Integer> localWeight = new HashMap<>();
    private static ConcurrentLinkedQueue<Endpoint> queue = new ConcurrentLinkedQueue<Endpoint>();

    static {
        localWeight.put("10.10.10.3", 1);
        localWeight.put("10.10.10.4", 2);
        localWeight.put("10.10.10.5", 3);
    }

    private LoadBalanceChoice() {
    }

    public static Endpoint weightedrandomChoice(String serviceName) throws Exception {
        checkEndpoint(serviceName);
        Endpoint endpoint = queue.poll();
        if (endpoint == null) {
            ArrayList<Endpoint> arrayList = new ArrayList<>();
            for (Endpoint e : endpoints) {
                int len = localWeight.get(e.getHost());
                for (int i = 0; i < len; i++) {
                    arrayList.add(e);
                }
            }
            Collections.shuffle(arrayList);
            if (queue.isEmpty()) {
                queue = new ConcurrentLinkedQueue(arrayList);
            }
            return queue.poll();
        }
        return endpoint;
    }


    private static void checkEndpoint(String serviceName) throws Exception {
        if (null == endpoints) {
            synchronized (lock) {
                if (null == endpoints) {
                    endpoints = registry.find(serviceName);
                }
            }
        }
    }

}
