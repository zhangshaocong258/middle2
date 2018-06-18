package com.alibaba.dubbo.performance.demo.agent.agent.serialize;

import com.alibaba.dubbo.performance.demo.agent.agent.model.*;
import com.alibaba.dubbo.performance.demo.agent.agent.util.CodeUtil;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.esotericsoftware.kryo.pool.KryoPool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.IOException;

public class MessageDecoder extends LengthFieldBasedFrameDecoder {
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

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf byteBuf = (ByteBuf) super.decode(ctx,in);
        if (byteBuf == null) {
            return null;
        } else {
            try {
                Object response = decodeData(byteBuf);
                return response;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    private Object decodeData(ByteBuf in) throws IOException {
        status = in.readByte();
        executingTasks = ((int) in.readByte() & 0xff);
        id = String.valueOf(in.readInt());
        in.readBytes(endpointBytes);
        endpoint = CodeUtil.bytes2endpoint(endpointBytes);
        in.skipBytes(4);
        if ((status & 0x01) == 0x00) {
            ByteBufInputStream bufInputStream = new ByteBufInputStream(in,true);
            AgentInvocation agentInvocation = null;
            try {
                agentInvocation = (AgentInvocation) kryoSerialize.deserialize(bufInputStream);
            } finally {
                bufInputStream.close();
            }
            if (agentInvocation != null) {
                AgentRequest request = new AgentRequest(
                        id, agentInvocation.getInterfaceName(), agentInvocation.getMethod(),
                        agentInvocation.getParameterTypesString(), agentInvocation.getParameter(), endpoint
                );
                return request;
            } else {
                return null;
            }
        } else {
            int data = in.readInt();
            AgentResponse response = new AgentResponse(
                    id,data,endpoint,executingTasks
            );
            in.release();
            return response;
        }
    }
}
