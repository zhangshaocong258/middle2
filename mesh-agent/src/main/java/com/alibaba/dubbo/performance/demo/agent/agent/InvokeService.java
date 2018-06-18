package com.alibaba.dubbo.performance.demo.agent.agent;
/**
 * Created by msi- on 2018/5/17.
 */

import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageFuture;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @program: dubbo-mesh
 * @description:
 * @author: XSL
 * @create: 2018-05-17 15:59
 **/

public class InvokeService {
//    private static Executor executor = Executors.newFixedThreadPool(32,Executors.defaultThreadFactory());
    private static EtcdRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private static RpcClient rpcClient = new RpcClient(registry);
    private InvokeService() {
    }
    public static void init() {}
//    public static void execute(Runnable callable) {
//        executor.execute(callable);
//    }

}
