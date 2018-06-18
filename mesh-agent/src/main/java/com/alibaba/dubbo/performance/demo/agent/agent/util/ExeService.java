package com.alibaba.dubbo.performance.demo.agent.agent.util;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExeService {
    private static Executor executor = Executors.newFixedThreadPool(1024,Executors.defaultThreadFactory());
    private static EtcdRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private ExeService() {
    }
    public static void init() {}
    public static void execute(Runnable callable) {
        executor.execute(callable);
    }
}
