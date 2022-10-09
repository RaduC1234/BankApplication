package me.raducapatina.server.request.core;

import io.netty.channel.ChannelHandlerContext;

public abstract class Request {

    public String requestType = "UNKNOWN";

    public abstract void executeOnRequest(ChannelHandlerContext ctx, String message);
}
