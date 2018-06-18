package com.alibaba.dubbo.performance.demo.agent.agent;/**
 * Created by msi- on 2018/5/16.
 */



import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageResponse;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageFuture;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClientInitializer;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.IpHelper;
import com.alibaba.fastjson.JSON;
import com.sun.org.apache.regexp.internal.RE;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: TcpProject
 * @description:
 * @author: XSL
 * @create: 2018-05-16 20:29
 **/

public class NettyServerHandler extends SimpleChannelInboundHandler<MessageRequest> {
//    private Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageRequest messageRequest) throws Exception {
//        concurrentHashMap.put(messageRequest.getMessageId(),Thread.currentThread().getName());
        MessageFuture future = invoke(channelHandlerContext,messageRequest);
        Runnable callable = () -> {
            try {
                Integer result = JSON.parseObject((byte[]) future.get(),Integer.class);
                MessageResponse response = new MessageResponse(messageRequest.getMessageId(),result,endpoint,RpcRequestHolder.getSize());
                channelHandlerContext.writeAndFlush(response,channelHandlerContext.voidPromise());
            } catch (Exception e) {
                channelHandlerContext.writeAndFlush(new MessageResponse(messageRequest.getMessageId(),"-1",endpoint,RpcRequestHolder.getSize()));
                e.printStackTrace();
            }
        };
        future.addListener(callable,channelHandlerContext.channel().eventLoop());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private MessageFuture invoke(ChannelHandlerContext channelHandlerContext,MessageRequest messageRequest) throws IOException {
        final Channel channel = channelHandlerContext.channel();
        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(messageRequest.getMethod());
        invocation.setAttachment("path", messageRequest.getInterfaceName());
        invocation.setParameterTypes(messageRequest.getParameterTypesString());    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        JsonUtils.writeObject(messageRequest.getParameter(), writer);
        invocation.setArguments(out.toByteArray());

        Request request = new Request();
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);
        MessageFuture future = new MessageFuture();
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
        private final Object objects;
        public ListenerImpl(Object object) {
            objects = object;
        }
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isSuccess()) {
                Channel channel = channelFuture.channel();
                concurrentHashMap.put(channel.eventLoop(),channel);
                channel.writeAndFlush(objects, channel.voidPromise());
            }
            else {
                channelFuture.channel().close();
            }
        }
    }
}
