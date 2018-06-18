package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.agent.InvokeService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.*;

public class ConnecManager {
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private Bootstrap bootstrap;
    private Channel channel;
    private Object lock = new Object();
    private List<Channel> channelList;
    private Random random = new Random();

    public ConnecManager() {
    }

    public Channel getChannel() throws Exception {
        if (null != channelList) {
            return channelList.get(random.nextInt(channelList.size()));
        }

        if (null == bootstrap) {
            synchronized (lock) {
                if (null == bootstrap) {
                    initBootstrap();
                }
            }
        }

        if (null == channel) {
            synchronized (lock) {
                if (null == channel) {
                    int port = Integer.valueOf(System.getProperty("dubbo.protocol.port"));
                    channel = bootstrap.connect("127.0.0.1", port).sync().channel();
                }
            }
        }
        return channel;
    }

    public void initBootstrap() {
        bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class)
                .handler(new RpcClientInitializer());
    }
}
