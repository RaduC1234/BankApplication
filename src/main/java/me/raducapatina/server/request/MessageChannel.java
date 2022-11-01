package me.raducapatina.server.request;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * Class wrapper for {@link ChannelHandlerContext}
 * @author Radu
 */
public class MessageChannel {
    private final ChannelHandlerContext ctx;

    public MessageChannel(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public ChannelFuture sendMessage(Object msj) {
        return ctx.writeAndFlush(msj + "\r\n");
    }
}
