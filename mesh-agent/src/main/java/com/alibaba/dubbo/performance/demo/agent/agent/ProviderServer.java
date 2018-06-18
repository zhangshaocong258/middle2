package com.alibaba.dubbo.performance.demo.agent.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.serialize.*;
import com.alibaba.dubbo.performance.demo.agent.agent.util.ExeService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;

public class ProviderServer {
    public void bind(int port) throws Exception {
        ExeService.init();
        EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
        EventLoopGroup workerGroup = new EpollEventLoopGroup(4);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(EpollServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childHandler(new ProviderServerInitializer());
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static class ProviderServerInitializer extends ChannelInitializer<SocketChannel>{
        @Override
        protected void initChannel(SocketChannel SocketChannel) throws Exception {
            SocketChannel.pipeline()
                    .addLast(new MessageEncoder(KryoPoolFactory.getKryoPoolInstance()))
                    .addLast(new MessageDecoder(KryoPoolFactory.getKryoPoolInstance()))
                    .addLast(new ProviderServerHandler());
        }
    }
}
