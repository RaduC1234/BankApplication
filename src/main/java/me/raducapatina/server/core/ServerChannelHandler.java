package me.raducapatina.server.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import me.raducapatina.server.data.DatabaseManager;
import me.raducapatina.server.request.MessageChannel;
import me.raducapatina.server.request.Request;
import me.raducapatina.server.util.Log;
import me.raducapatina.server.util.ResourceServerMessages;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Radu 1/11/22
 */
public class ServerChannelHandler extends ChannelInitializer<SocketChannel> {

    private volatile Client client;
    private final ServerInstance instance;
    private RequestChannelHandler channelHandler;

    public ServerChannelHandler(ServerInstance instance) {
        this.instance = instance;
        this.client = new Client();
        this.channelHandler = new RequestChannelHandler();
        this.channelHandler.addRequests(
                new AuthenticationRequest(this.client),
                new UnknownRequest()
        );
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
                Log.info(ResourceServerMessages.getObjectAsString("core.clientConnected").replace("{0}",
                        ctx.channel().remoteAddress().toString()));
                client.setAddress((InetSocketAddress) ctx.channel().remoteAddress());
                instance.getConnectedClients().add(client);
            }

            @Override
            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                //new MessageChannel(channelHandlerContext).sendMessage(s);
                Log.message(s, client.getAddress().getAddress().getHostAddress());
                channelHandler.handleRequest(new MessageChannel(channelHandlerContext), s);
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                ctx.close();
                instance.getConnectedClients().remove(client);
                Log.info(ResourceServerMessages.getObjectAsString("core.clientDisconnectedReason")
                        .replace("{0}",
                                (client.isAuthenticated() ? client.getAccount().getUsername()
                                        : ctx.channel().remoteAddress().toString()))
                        .replace("{1}", "The connection was closed by the remote host"));
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

                if (!socketChannel.isOpen()) {
                    ctx.close();
                    instance.getConnectedClients().remove(client);
                    Log.error(cause.getMessage());
                }
            }
        });
    }

    /**
     * The request system works like REST but without using HTTP. The procedure is
     * make up of one "question" and one "answer". Works the same for outbound and inbound
     * requests.
     *
     * @author Radu
     */
    private class RequestChannelHandler {

        // atomic = thread-safe
        private AtomicReference<Request> outboundRequest = null; // request that the server is sending -> 0
        private Request inboundRequest = null; // request that the server is receiving -> 1
        private boolean giveAllChannel = false;
        private volatile List<Request> registeredRequests;

        public RequestChannelHandler() {
            registeredRequests = new ArrayList<>(0);
        }

        public RequestChannelHandler addRequest(Request request) {
            registeredRequests.add(request);
            return this;
        }

        public RequestChannelHandler addRequests(Request... requests) {
            for(Request thisReq : requests)
                addRequest(thisReq);
            return this;
        }

        public void sendRequest(String requestType, MessageChannel ctx) throws IllegalArgumentException {
            for (Request request : registeredRequests) {
                if(request.requestType.equalsIgnoreCase(requestType)) {
                    request.sendRequest(ctx);
                    return;
                }
            }
            throw new IllegalArgumentException("Invalid request name");
        }

        public void handleRequest(MessageChannel channelHandlerContext, String string) {

            if (giveAllChannel) {
                // inboundRequest = new EchoRequest(this);
                inboundRequest.onAllChannelContext(channelHandlerContext, string);
                return;
            }

            try {
                String requestType = getRequestType(string);
                boolean requestStatus = getRequestStatus(string);

                // checking to see if the message is an answer to an existing request
                if (requestStatus && outboundRequest == null) {
                    // the client send an answer to a nonexistent request --> disconnect
                    channelHandlerContext.sendMessage(Request.error("UNKNOWN", 104));
                    return;
                }

                if (requestStatus && outboundRequest != null) {
                    // if the request is an answer, and we are waiting for answers
                    outboundRequest.get().onAnswer(channelHandlerContext, string);
                    outboundRequest = null;
                    return;
                }

                for (Request request : registeredRequests) {
                    if (requestType.equals(request.requestType)) {
                        inboundRequest = request;
                        request.onIncomingRequest(channelHandlerContext, string);
                        break;
                    }
                }
                throw new IllegalStateException();

            } catch (JsonProcessingException e) {
                //
                // throw error 001 = Request Syntax Error
            }
        }
    }

    private String getRequestType(String text) throws JsonProcessingException {
        final ObjectNode node = new ObjectMapper().readValue(text, ObjectNode.class);
        return node.get("requestType").asText();
    }

    private boolean getRequestStatus(String text) throws JsonProcessingException {
        final ObjectNode node = new ObjectMapper().readValue(text, ObjectNode.class);
        return node.get("requestStatus").asBoolean();
    }

    /**
     * Proof of concept that request handler is working. To stop send 'stop' to the
     * server. Note: use only in debug and when you may need the channel as a stream.
     */
    private class EchoRequest extends Request {

        private RequestChannelHandler localChannelHandler;

        public EchoRequest(RequestChannelHandler localChannelHandler) {
            this.localChannelHandler = localChannelHandler;
            requestType = "ECHO";
        }

        @Override
        public void onIncomingRequest(MessageChannel ctx, String message) {
            localChannelHandler.giveAllChannel = true;
        }

        @Override
        public void sendRequest(MessageChannel ctx) {

        }

        @Override
        public void onAnswer(MessageChannel ctx, String string) {

        }

        @Override
        public void onAllChannelContext(MessageChannel ctx, String message) {
            if (!(message.charAt(0) == '\\')) {
                ctx.sendMessage(message);
                Log.info(message);
                return;
            }
            localChannelHandler.giveAllChannel = false;
        }
    }

    private class AuthenticationRequest extends Request {

        public AuthenticationRequest(Client client) {
            requestType = "AUTHENTICATION";
        }

        @Override
        public void onIncomingRequest(MessageChannel ctx, String message) throws JsonProcessingException {
            final ObjectNode node = new ObjectMapper().readValue(message, ObjectNode.class);
            JsonNode requestContent = node.get("requestContent");
            String username = requestContent.get("username").asText();
            if (!DatabaseManager.userExists(username)) {
                ctx.sendMessage(error(this.requestType, 100));
            }
            client.setAccount(DatabaseManager.getInstanceFromDatabase(username));
            client.setAuthenticated(true);
            ctx.sendMessage(success(this.requestType, 103));
        }

        @Override
        public void sendRequest(MessageChannel ctx) {

        }

        @Override
        public void onAnswer(MessageChannel ctx, String string) {

        }
    }

    private class UnknownRequest extends Request {
        public UnknownRequest() {
            requestType = "UNKNOWN";
        }

        @Override
        public void onIncomingRequest(MessageChannel ctx, String message) throws JsonProcessingException {

        }

        @Override
        public void sendRequest(MessageChannel ctx) {

        }

        @Override
        public void onAnswer(MessageChannel ctx, String string) {

        }
    }
}
