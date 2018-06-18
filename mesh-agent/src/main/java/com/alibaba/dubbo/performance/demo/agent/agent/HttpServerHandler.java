package com.alibaba.dubbo.performance.demo.agent.agent;/**
 * Created by msi- on 2018/5/18.
 */

import com.alibaba.dubbo.performance.demo.agent.agent.model.Holder;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageResponse;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageFuture;
import com.alibaba.dubbo.performance.demo.agent.agent.util.IdGenerator;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.LoadBalanceChoice;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * @program: TcpProject
 * @description:
 * @author: XSL
 * @create: 2018-05-18 20:33
 **/
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
    private static ConcurrentHashMap<String,Channel> channelMap = new ConcurrentHashMap<>();
    private Map<String,String> paramMap = new HashMap<>();
    private static Map<String,String> map = new HashMap<>();
    static {
        map.put("10.10.10.3","provider-small");
        map.put("10.10.10.4","provider-medium");
        map.put("10.10.10.5","provider-large");
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullHttpRequest);
        decoder.offer(fullHttpRequest);
        for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
            Attribute attribute = (Attribute) data;
            paramMap.put(attribute.getName(),attribute.getValue());
        }
        MessageRequest messageRequest = new MessageRequest(
                IdGenerator.getIdByIncrement(),paramMap.get("interface"),paramMap.get("method"),paramMap.get("parameterTypesString"),paramMap.get("parameter")
                );
        MessageFuture<MessageResponse> future = sendRequest("com.alibaba.dubbo.performance.demo.provider.IHelloService",messageRequest,channelHandlerContext);
        Runnable runnable = () -> {
            try {
                MessageResponse response = future.get();
//                long time = System.nanoTime();
//                long interval = time - Holder.removeTime(response.getMessageId());
//                logger.info(map.get(response.getEndpoint().getHost()) + " : " + " cost = " + interval/1000000 + "ms executing task = " + response.getExecutingTask() + " now WaitTask = " + Holder.getSize());
//                LoadBalanceChoice.addTime("com.alibaba.dubbo.performance.demo.provider.IHelloService", interval/1000 ,response.getEndpoint());
//                LoadBalanceChoice.addExecutingTaskCount(response.getEndpoint().getHost(),response.getExecutingTask());
                if (!writeResponse(fullHttpRequest,fullHttpRequest,channelHandlerContext, (Integer) response.getResultDesc())) {
                }
            } catch (Exception e) {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST
                );
                channelHandlerContext.writeAndFlush(response);
                e.printStackTrace();
            }
        };
        // executor为null 将交给channel的绑定的eventLoop执行
        future.addListener(runnable,channelHandlerContext.channel().eventLoop());
    }

    private boolean writeResponse(HttpRequest request,HttpObject httpObject, ChannelHandlerContext ctx, int data) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                httpObject.decoderResult().isSuccess()? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
                Unpooled.copiedBuffer(String.valueOf(data).getBytes()));
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        response.headers().add(CONTENT_TYPE,"application/json;charset=utf-8");
        response.headers().add(CONTENT_LENGTH,response.content().readableBytes());
        ctx.writeAndFlush(response,ctx.voidPromise());
        return keepAlive;
    }
    private MessageFuture<MessageResponse> sendRequest(String serviceName, MessageRequest request, ChannelHandlerContext channelHandlerContext) throws Exception {
        final Channel channel = channelHandlerContext.channel();
        MessageFuture<MessageResponse> future = new MessageFuture<>();
        Holder.putRequest(request.getMessageId(), future);
        Endpoint endpoint = LoadBalanceChoice.weightedrandomChoice(serviceName);
        request.setEndpoint(endpoint);
//        logger.info("now choose " + map.get(endpoint.getHost()));
        String key = channel.eventLoop().toString() + endpoint.toString();
        Channel nextChannel = channelMap.get(key);
        if (nextChannel == null) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(channel.eventLoop())
                    .channel(EpollSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new NettyClientInitializer());
            ChannelFuture channelFuture = bootstrap.connect(endpoint.getHost(),endpoint.getPort());
            channelFuture.addListener(new ListenerImpl(request,endpoint));
        } else {
            nextChannel.writeAndFlush(request, nextChannel.voidPromise());
        }
        return future;
    }

    private static final class ListenerImpl implements ChannelFutureListener {
        private final Object objects;
        private final Endpoint endpoint;
        public ListenerImpl(Object object,Endpoint endpoint) {
            objects = object;
            this.endpoint = endpoint;
        }
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isSuccess()) {
                Channel channel = channelFuture.channel();
                channelMap.put(channel.eventLoop().toString() + endpoint.toString(),channel);
                channel.writeAndFlush(objects, channel.voidPromise());
            }
            else {
                channelFuture.channel().close();
            }
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}