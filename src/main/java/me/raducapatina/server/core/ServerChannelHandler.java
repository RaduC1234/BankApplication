package me.raducapatina.server.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import me.raducapatina.server.request.core.RequestChannelHandler;
import me.raducapatina.server.util.Log;
import me.raducapatina.server.util.ResourceServerMessages;

public class ServerChannelHandler extends ChannelInitializer<SocketChannel> {

    private ServerInstance instance;
    private RequestChannelHandler handler;
    private volatile Client client;

    public ServerChannelHandler(ServerInstance instance) {
        this.instance = instance;
        this.handler = new RequestChannelHandler();
        this.client = new Client();
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());

        pipeline.addLast(new SimpleChannelInboundHandler<String>() {

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                Log.info(ResourceServerMessages.getObjectAsString("core.clientConnected").replace("{0}", ctx.channel().remoteAddress().toString()));
                instance.getConnectedClients().add(client);
                //handler.registerRequest(RequestChannelHandler.RequestType.AUTHENTICATE);
            }

            @Override
            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {

            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                ctx.close();
                instance.getConnectedClients().remove(client);
                Log.info(ResourceServerMessages.getObjectAsString("core.clientDisconnectedReason")
                        .replace("{0}", (client.isAuthenticated()? client.getAccount().getUsername() : ctx.channel().remoteAddress().toString()))
                        .replace("{1}", "The connection was closed by the remote host"));
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                ctx.close();
                instance.getConnectedClients().remove(client);
                Log.error(cause.getMessage());
            }

        });
    }
}
