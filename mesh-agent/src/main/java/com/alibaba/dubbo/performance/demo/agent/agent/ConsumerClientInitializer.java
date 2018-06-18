package com.alibaba.dubbo.performance.demo.agent.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.serialize.*;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;


public class ConsumerClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new MessageEncoder(KryoPoolFactory.getKryoPoolInstance()))
                .addLast(new MessageDecoder(KryoPoolFactory.getKryoPoolInstance()))
                .addLast(new ConsumerClientHandler());
    }
}