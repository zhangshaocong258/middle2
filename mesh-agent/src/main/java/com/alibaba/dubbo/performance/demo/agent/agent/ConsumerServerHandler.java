package com.alibaba.dubbo.performance.demo.agent.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentFuture;
import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentHolder;
import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.util.IdGenerator;
import com.alibaba.dubbo.performance.demo.agent.agent.util.ExeService;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.agent.util.LoadBalance;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

public class ConsumerServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private Logger logger = LoggerFactory.getLogger(ConsumerServerHandler.class);
    private static ConcurrentHashMap<String,Channel> channelMap = new ConcurrentHashMap<>();
    private Map<String,String> paramMap = new HashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullHttpRequest);
        decoder.offer(fullHttpRequest);
        for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
            Attribute attribute = (Attribute) data;
            paramMap.put(attribute.getName(),attribute.getValue());
        }
        AgentRequest agentRequest = new AgentRequest(
                IdGenerator.getIdAndIncrement(),
                paramMap.get("interface"),
                paramMap.get("method"),
                paramMap.get("parameterTypesString"),
                paramMap.get("parameter")
                );
        AgentFuture<AgentResponse> future = sendRequest(agentRequest,channelHandlerContext);
        Runnable callback = new Runnable() {
            @Override
            public void run() {
                try {
                    AgentResponse response = future.get();
                    writeResponse(fullHttpRequest,fullHttpRequest,channelHandlerContext, (Integer) response.getResultDesc());
                } catch (Exception e) {
                    FullHttpResponse response = new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST
                    );
                    channelHandlerContext.writeAndFlush(response);
                    e.printStackTrace();
                }
            }
        };
        ExeService.execute(callback);
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
    private AgentFuture<AgentResponse> sendRequest(AgentRequest request, ChannelHandlerContext channelHandlerContext) throws Exception {
        Channel channel = channelHandlerContext.channel();
        AgentFuture<AgentResponse> future = new AgentFuture<>();
        AgentHolder.putRequest(request.getMessageId(), future);
        Endpoint endpoint = LoadBalance.weightedrandomChoice();
        request.setEndpoint(endpoint);
        String key = channel.eventLoop().toString() + endpoint.toString();
        Channel nextChannel = channelMap.get(key);
        if (nextChannel == null) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(channel.eventLoop())
                    .channel(EpollSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new ConsumerClientInitializer());
            ChannelFuture channelFuture = bootstrap.connect(endpoint.getHost(),endpoint.getPort());
            channelFuture.addListener(new ListenerImpl(request,endpoint));
        } else {
            nextChannel.writeAndFlush(request, nextChannel.voidPromise());
        }
        return future;
    }

    private static final class ListenerImpl implements ChannelFutureListener {
        private final AgentRequest agentRequest;
        private final Endpoint endpoint;
        public ListenerImpl(AgentRequest agentRequest,Endpoint endpoint) {
            this.agentRequest = agentRequest;
            this.endpoint = endpoint;
        }
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isSuccess()) {
                Channel channel = channelFuture.channel();
                channelMap.put(channel.eventLoop().toString() + endpoint.toString(),channel);
                channel.writeAndFlush(agentRequest, channel.voidPromise());
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