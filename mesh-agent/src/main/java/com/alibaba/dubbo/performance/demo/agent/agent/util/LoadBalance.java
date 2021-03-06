package com.alibaba.dubbo.performance.demo.agent.agent.util;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LoadBalance {
    private static Logger logger = LoggerFactory.getLogger(LoggerFactory.class);
    private static EtcdRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private static List<Endpoint> endpoints;
    private static Object lock = new Object();
    private static Map<String, Integer> weight = new HashMap<>();
    private static ConcurrentLinkedQueue<Endpoint> queue = new ConcurrentLinkedQueue<Endpoint>();

    static {
        weight.put("10.10.10.3", 1);
        weight.put("10.10.10.4", 2);
        weight.put("10.10.10.5", 3);
        try {
            checkEndpoint("com.alibaba.dubbo.performance.demo.provider.IHelloService");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LoadBalance() {
    }

    public static Endpoint weightedrandomChoice() throws Exception {
        Endpoint endpoint = queue.poll();
        if (endpoint == null) {
            ArrayList<Endpoint> arrayList = new ArrayList<>();
            for (Endpoint e : endpoints) {
                int len = weight.get(e.getHost());
                for (int i = 0; i < len; i++) {
                    arrayList.add(e);
                }
            }
            Collections.sort(arrayList, COMPARATOR);
            if (queue.isEmpty()) {
                queue = new ConcurrentLinkedQueue(arrayList);
            }
            return queue.poll();
        }
        return endpoint;
    }

    private static final Comparator<Endpoint> COMPARATOR = new Comparator<Endpoint>() {
        public int compare(Endpoint o1, Endpoint o2) {
            if (o1.getWeight() < (o2.getWeight())) {
                return 1;
            } else {
                return -1;
            }
        }
    };


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
