package com.alibaba.dubbo.performance.demo.agent.agent;


import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.agent.util.ExeService;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClientInitializer;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.*;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.IpHelper;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;


public class ProviderServerHandler extends SimpleChannelInboundHandler<AgentRequest> {
    private Logger logger = LoggerFactory.getLogger(ProviderServerHandler.class);
    private static final String HOST = "127.0.0.1";
    private static final int PORT = Integer.valueOf(System.getProperty("dubbo.protocol.port"));
    private static ConcurrentHashMap<EventLoop,Channel> concurrentHashMap = new ConcurrentHashMap<>();
    private static Endpoint endpoint;
    static {
        try {
            endpoint = new Endpoint(IpHelper.getHostIp(),Integer.valueOf(System.getProperty("server.port")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AgentRequest agentRequest) throws Exception {
        RpcFuture future = invoke(channelHandlerContext, agentRequest);
        Runnable callback = new Runnable() {
            @Override
            public void run() {
                try {
                    Integer result = JSON.parseObject((byte[]) future.get(),Integer.class);
                    AgentResponse response = new AgentResponse(agentRequest.getMessageId(),result,endpoint,RpcRequestHolder.getSize());
                    channelHandlerContext.writeAndFlush(response,channelHandlerContext.voidPromise());
                } catch (Exception e) {
                    channelHandlerContext.writeAndFlush(new AgentResponse(agentRequest.getMessageId(),"-1",endpoint,RpcRequestHolder.getSize()));
                    e.printStackTrace();
                }
            }
        };
//        ExeService.execute(callback);
        future.addListener(callback, channelHandlerContext.channel().eventLoop());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private RpcFuture invoke(ChannelHandlerContext channelHandlerContext,AgentRequest agentRequest) throws IOException {
        Channel channel = channelHandlerContext.channel();
        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(agentRequest.getMethod());
        invocation.setAttachment("path", agentRequest.getInterfaceName());
        invocation.setParameterTypes(agentRequest.getParameterTypesString());    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        JsonUtils.writeObject(agentRequest.getParameter(), writer);
        invocation.setArguments(out.toByteArray());

        Request request = new Request();
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);
        RpcFuture future = new RpcFuture();
        RpcRequestHolder.put(String.valueOf(request.getId()),future);
        Channel nextChannel = concurrentHashMap.get(channel.eventLoop());
        if (nextChannel == null) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(channel.eventLoop())
                    .channel(EpollSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new RpcClientInitializer());
            ChannelFuture channelFuture = bootstrap.connect(HOST,PORT);
            channelFuture.addListener(new ListenerImpl(request));
        } else {
            nextChannel.writeAndFlush(request,nextChannel.voidPromise());
        }
        return future;
    }

    private static final class ListenerImpl implements ChannelFutureListener {
        private final  Request request;
        public ListenerImpl(Request request) {
            this.request = request;
        }
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isSuccess()) {
                Channel channel = channelFuture.channel();
                concurrentHashMap.put(channel.eventLoop(),channel);
                channel.writeAndFlush(request, channel.voidPromise());
            }
            else {
                channelFuture.channel().close();
            }
        }
    }
}
