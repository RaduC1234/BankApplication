package me.raducapatina.server.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import me.raducapatina.server.data.DatabaseManager;
import me.raducapatina.server.data.User;
import me.raducapatina.server.data.UserService;
import me.raducapatina.server.util.ResourceServerMessages;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.NoResultException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Radu 1/11/22
 */
public class ServerChannelHandler extends ChannelInitializer<SocketChannel> {

    private static final Logger logger = LogManager.getLogger(ServerChannelHandler.class);
    private static final Level MESSAGE = Level.forName("MESSAGE", 450);

    private final Client client;
    private final ServerInstance instance;
    private final RequestChannelHandler channelHandler;

    public ServerChannelHandler(ServerInstance instance) {
        this.instance = instance;
        this.client = new Client();
        this.channelHandler = new RequestChannelHandler();
        this.channelHandler
                .addRequestTemplate("AUTHENTICATION", new Authentication(this.client));
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
                logger.info(ResourceServerMessages.getObjectAsString("core.clientConnected").replace("{0}",
                        ctx.channel().remoteAddress().toString()));
                client.setAddress((InetSocketAddress) ctx.channel().remoteAddress());
                instance.getConnectedClients().add(client);
            }

            @Override
            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                //new MessageChannel(channelHandlerContext).sendMessage(s);
                logger.log(MESSAGE, client.getAddress().getAddress().getHostAddress() + " says " + s);
                channelHandler.onMessage(channelHandlerContext, s);
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                ctx.close();
                instance.getConnectedClients().remove(client);
                logger.info(ResourceServerMessages.getObjectAsString("core.clientDisconnectedReason")
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
                    logger.error(cause.getMessage());
                }
            }
        });
    }

    /**
     * The request system works like REST but without using HTTP. The procedure is
     * make up of one "question" and one "answer". Works the same for outbound and inbound
     * requestsTemplates.
     *
     * @author Radu
     */
    private static class RequestChannelHandler {

        private Map<String, RequestTemplate> requestsTemplates = new HashMap();
        private List<Packet> waitingOutboundPackets = new ArrayList<>();

        public RequestChannelHandler() {

        }

        public RequestChannelHandler addRequestTemplate(String name, RequestTemplate template) {
            requestsTemplates.put(name, template);
            return this;
        }

        public void onMessage(ChannelHandlerContext channelHandlerContext, String message) {
            try {
                Packet receivedPacket = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).readValue(message, Packet.class);
                receivedPacket.setChannelHandlerContext(channelHandlerContext);

                // check if the packet is an answer to an inbound request
                if (receivedPacket.getRequestStatus()) {
                    for (Packet packet : waitingOutboundPackets) {
                        if (packet.getRequestId() == receivedPacket.getRequestId()) {
                            requestsTemplates.get(packet.getRequestName()).onAnswer(receivedPacket);
                            waitingOutboundPackets.remove(packet);
                            return;
                        }
                    }
                    // throw error: no request found
                    logger.error("No request found with id provided.");
                }

                // packet is a new request at this point
                if (requestsTemplates.get(receivedPacket.getRequestName()) == null) {
                    // throw error: no request with this name found
                    logger.error("Invalid request name.");
                    return;
                }

                requestsTemplates.get(receivedPacket.getRequestName()).onIncomingRequest(receivedPacket);

            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
        }

        public void sendRequest(String name, ChannelHandlerContext ctx) throws IllegalArgumentException {
            if (requestsTemplates.get(name) == null)
                throw new IllegalArgumentException("No request template found with passed name");
            Packet packet = new Packet(name, ctx);
            requestsTemplates.get(name).onNewRequest(packet);
            waitingOutboundPackets.add(packet);
        }
    }

    public interface RequestTemplate {

        void onNewRequest(Packet packet);

        void onAnswer(Packet packet);

        void onIncomingRequest(Packet packet);
    }

    public class Authentication implements RequestTemplate {

        private Client client;

        public Authentication(Client client) {

            this.client = client;
        }

        @Override
        public void onNewRequest(Packet packet) {

        }

        @Override
        public void onAnswer(Packet packet) {

        }

        @Override
        public void onIncomingRequest(Packet packet) {
            String username = packet.getRequestContent().get("username").asText();
            String password = packet.getRequestContent().get("password").asText();

            UserService userService = DatabaseManager.getInstance().getUserService();
            User user;
            try {
                user = userService.findByUsername(username);
            } catch (NoResultException e) {
                packet.sendError(Packet.PACKET_CODES.USER_NOT_FOUND); // user does not exist
                return;
            } catch (Exception e) {
                logger.error(e);
                return;
            }

            if (user.getPassword().equals(password)) {
                this.client.setAuthenticated(true);
                this.client.setAccount(user);
                logger.info(ResourceServerMessages.getObjectAsString("core.clientAuthenticated")
                        .replace("{0}", client.getAddress().toString())
                        .replace("{1}", user.getUsername()));
                packet.sendSuccess();
                return;
            }

            packet.sendError(Packet.PACKET_CODES.INVALID_PASSWORD); // invalid password
        }
    }

    public class SelfInfo implements RequestTemplate {

        @Override
        public void onNewRequest(Packet packet) {

        }

        @Override
        public void onAnswer(Packet packet) {

        }

        @Override
        public void onIncomingRequest(Packet packet) {

        }
    }
}
