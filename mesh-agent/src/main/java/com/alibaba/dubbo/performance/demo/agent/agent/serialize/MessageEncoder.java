package com.alibaba.dubbo.performance.demo.agent.agent.serialize;

import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentInvocation;
import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.util.CodeUtil;
import com.esotericsoftware.kryo.pool.KryoPool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;

public class MessageEncoder extends MessageToByteEncoder<Object> {
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
    private static final int REQUEST_FLAG = 0x00;
    private static final int RESPONSE_FLAG= 0x01;
    public static final int HEADER_LENGTH = 18;
    private KryoSerialize kryoSerialize;
    public MessageEncoder(KryoPool pool) {
        this.kryoSerialize = new KryoSerialize(pool);
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        int startIndex = byteBuf.writerIndex();
        if (object instanceof AgentRequest) {
            encodeRequest(byteBuf, (AgentRequest) object);
        } else {
            encodeResponse(byteBuf, (AgentResponse) object);
        }
        int endIndex = byteBuf.writerIndex();
        byteBuf.setInt(startIndex + 14 ,endIndex-startIndex-HEADER_LENGTH);
    }

    private void encodeRequest(ByteBuf out, AgentRequest request) throws IOException {
        ByteBufOutputStream bufOutputStream = new ByteBufOutputStream(out);
        try {
            bufOutputStream.writeByte(REQUEST_FLAG);
            bufOutputStream.writeByte(0);
            bufOutputStream.writeInt(Integer.valueOf(request.getMessageId()));
            bufOutputStream.write(CodeUtil.endpoint2bytes(request.getEndpoint()));
            bufOutputStream.write(LENGTH_PLACEHOLDER);
            AgentInvocation agentInvocation = new AgentInvocation(
                    request.getInterfaceName(),
                    request.getMethod(),
                    request.getParameterTypesString(),
                    request.getParameter()
            );
            kryoSerialize.serialize(bufOutputStream, agentInvocation);
        } finally {
            bufOutputStream.close();
        }
    }

    private void encodeResponse(ByteBuf out , AgentResponse response) throws IOException {
        ByteBufOutputStream bufOutputStream = new ByteBufOutputStream(out);
        try {
            bufOutputStream.writeByte(RESPONSE_FLAG);
            bufOutputStream.writeByte(response.getExecutingTask());
            bufOutputStream.writeInt(Integer.valueOf(response.getMessageId()));
            bufOutputStream.write(CodeUtil.endpoint2bytes(response.getEndpoint()));
            bufOutputStream.write(LENGTH_PLACEHOLDER);
            bufOutputStream.writeInt((Integer) response.getResultDesc());
        } finally {
            bufOutputStream.close();
        }
    }
}
