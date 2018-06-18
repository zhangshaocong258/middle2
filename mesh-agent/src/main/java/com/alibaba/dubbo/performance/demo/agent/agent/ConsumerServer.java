package com.alibaba.dubbo.performance.demo.agent.agent;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;


public class ConsumerServer {
    public void bind(final int port) throws Exception {
        EventLoopGroup boss = new EpollEventLoopGroup(1);
        EventLoopGroup worker = new EpollEventLoopGroup(8);
        ServerBootstrap serverBootstrap = new ServerBootstrap().group(boss,worker);
        try {
            serverBootstrap.channel(EpollServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .option(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_BACKLOG,4096)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(10 * 1024))
                                    .addLast(new ConsumerServerHandler());
                        }
                        });
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
