package com.alibaba.dubbo.performance.demo.agent.agent.serialize;/**
 * Created by msi- on 2018/5/18.
 */

import com.alibaba.dubbo.performance.demo.agent.agent.model.Invocation;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageResponse;
import com.alibaba.dubbo.performance.demo.agent.agent.util.Common;
import com.esotericsoftware.kryo.pool.KryoPool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.log4j.or.jms.MessageRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * @program: dubbo-mesh
 * @description:
 * @author: XSL
 * @create: 2018-05-18 16:08
 **/

public class MessageEncoder extends MessageToByteEncoder<Object> {
//    private Logger logger = LoggerFactory.getLogger(MessageEncoder.class);
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
        if (object instanceof MessageRequest) {
            encodeRequest(byteBuf, (MessageRequest) object);
        } else {
            encodeResponse(byteBuf, (MessageResponse) object);
        }
        int endIndex = byteBuf.writerIndex();
        //写入长度
        byteBuf.setInt(startIndex + 14 ,endIndex-startIndex-HEADER_LENGTH);
    }

    private void encodeRequest(ByteBuf out, MessageRequest request) throws IOException {
        ByteBufOutputStream bufOutputStream = new ByteBufOutputStream(out);
        try {
            //为数据长度预留位置
            bufOutputStream.writeByte(REQUEST_FLAG);
            bufOutputStream.writeByte(0);
            // id 头部 0 - 3   4个字节
            bufOutputStream.writeInt(Integer.valueOf(request.getMessageId()));
            // 请求类型 8.1 1个比特  返回状态 8.2 - 8.8 7个比特  待返回的请求数  9 1个字节
            // 发送的网络ip地址 10 - 11 4个字节 网络端口 12 - 13 4个字节
            bufOutputStream.write(Common.endpoint2bytes(request.getEndpoint()));
            bufOutputStream.write(LENGTH_PLACEHOLDER);
            Invocation invocation = new Invocation(
                    request.getInterfaceName(),
                    request.getMethod(),
                    request.getParameterTypesString(),
                    request.getParameter()
            );
            kryoSerialize.serialize(bufOutputStream,invocation);
        } finally {
            bufOutputStream.close();
        }


    }

    private void encodeResponse(ByteBuf out , MessageResponse response) throws IOException {
        ByteBufOutputStream bufOutputStream = new ByteBufOutputStream(out);
        try {
            //为数据长度预留位置
            // id 头部 0 - 3   4个字节
            bufOutputStream.writeByte(RESPONSE_FLAG);
            bufOutputStream.writeByte(response.getExecutingTask());
            bufOutputStream.writeInt(Integer.valueOf(response.getMessageId()));
            // 请求类型 8.1 1个比特  返回状态 8.2 - 8.8 7个比特  待返回的请求数  9 1个字节
            // 发送的网络ip地址 10 - 11 4个字节 网络端口 12 - 13 4个字节
            bufOutputStream.write(Common.endpoint2bytes(response.getEndpoint()));
            // 数据体长度 头部 4 - 7  4个字节
            bufOutputStream.write(LENGTH_PLACEHOLDER);
            bufOutputStream.writeInt((Integer) response.getResultDesc());
        } finally {
            bufOutputStream.close();
        }
    }
}
