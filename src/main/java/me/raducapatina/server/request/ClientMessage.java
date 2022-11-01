package me.raducapatina.server.request;

import io.netty.channel.ChannelHandlerContext;
import me.raducapatina.server.core.Client;

public class ClientMessage implements Cloneable {

    private Client client;
    private ChannelHandlerContext ctx;
    private String rawMessage;


}
