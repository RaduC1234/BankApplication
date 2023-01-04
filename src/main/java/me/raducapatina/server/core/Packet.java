package me.raducapatina.server.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JsonIgnoreProperties(value = {"channelHandlerContext"})
public class Packet {

    private String requestName;
    private boolean requestStatus;
    private long requestId;
    private JsonNode requestContent;

    @JsonIgnore
    private ChannelHandlerContext channelHandlerContext;

    @Setter @Getter @JsonIgnore
    private Client client;

    public Packet() {
    }

    public Packet(String name, ChannelHandlerContext channelHandlerContext) {
        this.requestName = name;
        this.channelHandlerContext = channelHandlerContext;
        this.requestStatus = false;
        this.requestId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }

    public ChannelFuture sendThis(boolean status) throws Exception {
        this.requestStatus = status;
        return this.channelHandlerContext.writeAndFlush(this.toJson() + "\r\n");
    }

    public ChannelFuture sendError(PACKET_CODES code) {
        return this.channelHandlerContext.writeAndFlush("{\"requestName\": \"" + requestName + "\", \"requestStatus\": true, \"requestId\" : " + requestId + ",\"responseStats\" : 0, \"requestContent\" : {\"message\" : \"" + code.name() + "\"}}\r\n");
    }

    public ChannelFuture sendSuccess() {
        return this.channelHandlerContext.writeAndFlush("{\"requestName\": \"" + requestName + "\", \"requestStatus\": true, \"requestId\" : " + requestId + ",\"responseStats\" : 1, \"requestContent\" : {\"message\" : \"" + PACKET_CODES.SUCCESS.name() + "\"}}\r\n");
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public String getRequestName() {
        return requestName;
    }

    public boolean getRequestStatus() {
        return requestStatus;
    }

    public long getRequestId() {
        return requestId;
    }

    public JsonNode getRequestContent() {
        return requestContent;
    }

    public Packet setRequestContent(JsonNode requestContent) {
        this.requestContent = requestContent;
        return this;
    }

    public Packet setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
        return this;
    }

    public enum PACKET_CODES {

        SUCCESS,
        ERROR,

        UNKNOWN_REQUEST,
        REQUEST_SYNTAX_ERROR,

        NOT_AUTHENTICATED,
        USER_IN_USE,
        USER_NOT_FOUND,
        INVALID_PASSWORD,
    }
}
