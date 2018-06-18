package com.alibaba.dubbo.performance.demo.agent.agent.util;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private static AtomicInteger count = new AtomicInteger(0);
    public static String getIdAndIncrement() {
        return String.valueOf(count.getAndIncrement());
    }
}
