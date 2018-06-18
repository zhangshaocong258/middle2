package com.alibaba.dubbo.performance.demo.agent.agent.serialize;/**
 * Created by msi- on 2018/5/18.
 */

import com.alibaba.dubbo.performance.demo.agent.agent.model.*;
import com.alibaba.dubbo.performance.demo.agent.agent.util.Common;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.sun.org.apache.regexp.internal.RE;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.serialization.ObjectDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


/**
 * @program: dubbo-mesh
 * @description: 消息解码接口
 * @author: XSL
 * @create: 2018-05-18 16:11
 **/

public class MessageDecoder extends LengthFieldBasedFrameDecoder {
//    private Logger logger = LoggerFactory.getLogger(MessageDecoder.class);
    private static final int MAX_OBJECT_SIZE = 16384;
    private byte[] endpointBytes = new byte[8];
    private String id;
    private int executingTasks;
    private int status;
    private Endpoint endpoint;
    private KryoSerialize kryoSerialize;

    public MessageDecoder(KryoPool pool) {
        super(MAX_OBJECT_SIZE,14,4);
        this.kryoSerialize = new KryoSerialize(pool);
    }
    public MessageDecoder(int maxObjectSize, KryoPool pool) {
        super(MAX_OBJECT_SIZE,14,4);
        this.kryoSerialize = new KryoSerialize(pool);
    }
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf byteBuf = (ByteBuf) super.decode(ctx,in);
        if (byteBuf == null) {
            return null;
        } else {
            try {
                Object response = decodeData(byteBuf);
                if (response instanceof MessageResponse) {
                  MessageFuture future = Holder.removeRequest(((MessageResponse) response).getMessageId());
                  if (future!=null) {
                      future.done(response);
                  }
                }
                return response;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    private Object decodeData(ByteBuf in) throws IOException {
        //根据状态标识判断是request还是response
        status = in.readByte();
        executingTasks = ((int) in.readByte() & 0xff);
        id = String.valueOf(in.readInt());
        in.readBytes(endpointBytes);
        endpoint = Common.bytes2endpoint(endpointBytes);
        in.skipBytes(4);
        if ((status & 0x01) == 0x00) {
            ByteBufInputStream bufInputStream = new ByteBufInputStream(in,true);
            Invocation invocation = null;
            try {
                invocation = (Invocation) kryoSerialize.deserialize(bufInputStream);
            } finally {
                bufInputStream.close();
            }
            if (invocation != null) {
                MessageRequest request = new MessageRequest(
                        id, invocation.getInterfaceName(), invocation.getMethod(),
                        invocation.getParameterTypesString(), invocation.getParameter(), endpoint
                );
                return request;
            } else {
                return null;
            }
        } else {
            int data = in.readInt();
            MessageResponse response = new MessageResponse(
                    id,data,endpoint,executingTasks
            );
            in.release();
            return response;
        }
    }
}
