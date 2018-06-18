package com.alibaba.dubbo.performance.demo.agent.agent.util;/**
 * Created by msi- on 2018/5/17.
 */

import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageRequest;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @program: dubbo-mesh
 * @description:
 * @author: XSL
 * @create: 2018-05-17 15:59
 **/

public class WaitService {
    private static Executor executor = Executors.newFixedThreadPool(512,Executors.defaultThreadFactory());
    private static EtcdRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
//    private static RpcClient rpcClient = new RpcClient(registry);

    private WaitService() {
    }
    public static void init() {}
    public static void execute(Runnable callable) {
        executor.execute(callable);
    }
//    public static RpcFuture executeInvoke(MessageRequest messageRequest) throws Exception {
//        return (RpcFuture) rpcClient.invoke(messageRequest.getInterfaceName(),messageRequest.getMethod(),messageRequest.getParameterTypesString(),messageRequest.getParameter());
//    }
}
