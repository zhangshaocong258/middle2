package com.alibaba.dubbo.performance.demo.agent.agent;/**
 * Created by msi- on 2018/5/27.
 */

import com.alibaba.dubbo.performance.demo.agent.agent.serialize.*;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @program: dubbo-mesh
 * @description:
 * @author: XSL
 * @create: 2018-05-27 21:49
 **/

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new MessageEncoder(KryoPoolFactory.getKryoPoolInstance()))
                .addLast(new MessageDecoder(KryoPoolFactory.getKryoPoolInstance()));
//                ProtostuffCodeUtil util = ProtostuffCodeUtil.getClientCodeUtil();
//                socketChannel.pipeline().addLast(new ProtostuffEncoder(util))
//                        .addLast(new ProtostuffDecoder(util))
//                        .addLast(new NettyClientHandler());
    }
}