package com.alibaba.dubbo.performance.demo.agent.agent.util;


import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Limiter {
    public static Map<Endpoint,AtomicInteger> limitMap = new HashMap<>(3);
}
