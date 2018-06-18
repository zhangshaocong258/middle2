package com.alibaba.dubbo.performance.demo.agent.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentFuture;
import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerClientHandler extends SimpleChannelInboundHandler<AgentResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AgentResponse agentResponse) throws Exception {
        AgentFuture<AgentResponse> future = AgentHolder.removeRequest(agentResponse.getMessageId());
        if (future != null) {
            future.done(agentResponse);

        }
    }
}
