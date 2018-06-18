package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.NettyHttpServer;
import com.alibaba.dubbo.performance.demo.agent.agent.NettyTcpServer;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class AgentApp {
    // agent会作为sidecar，部署在每一个Provider和Consumer机器上
    // 在Provider端启动agent时，添加JVM参数-Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20889
    // 在Consumer端启动agent时，添加JVM参数-Dtype=consumer -Dserver.port=20000
    // 添加日志保存目录: -Dlogs.dir=/path/to/your/logs/dir。请安装自己的环境来设置日志目录。
    public static void main(String[] args) throws Exception {
        String type = System.getProperty("type");
        int port = Integer.parseInt(System.getProperty("server.port"));
        if ("provider".equals(type)) {
            new NettyTcpServer().bind(port);
        } else if ("consumer".equals(type)) {
//            SpringApplication.run(AgentApp.class, args);
            new NettyHttpServer().bind(port);
        }
    }
}
