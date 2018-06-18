package com.alibaba.dubbo.performance.demo.agent.agent;/**
 * Created by msi- on 2018/5/18.
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @program: TcpProject
 * @description:
 * @author: XSL
 * @create: 2018-05-18 20:26
 **/

public class NettyHttpServer {
//    private Logger logger = LoggerFactory.getLogger(NettyHttpServer.class);
    public void bind(final int port) throws Exception {
        EventLoopGroup boss = new EpollEventLoopGroup(2);
        EventLoopGroup worker = new EpollEventLoopGroup();
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
                                    .addLast(new HttpServerHandler());
                        }
                        });
            ChannelFuture future = serverBootstrap.bind("0.0.0.0",port).sync();
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
