package me.raducapatina.server.request;

import io.netty.channel.ChannelHandlerContext;
import me.raducapatina.server.request.core.Request;

public class EchoRequest extends Request {

    public EchoRequest() {
        this.requestType = "ECHO";
    }

    @Override
    public void executeOnRequest(ChannelHandlerContext ctx, String message) {

    }
}
