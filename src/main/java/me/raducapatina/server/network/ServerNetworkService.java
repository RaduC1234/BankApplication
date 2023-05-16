package me.raducapatina.server.network;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.NoArgsConstructor;
import me.raducapatina.server.core.Client;
import me.raducapatina.server.core.ServerInstance;
import me.raducapatina.server.data.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.NoResultException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Radu 1/11/22
 */
public class ServerNetworkService extends ChannelInitializer<SocketChannel> {

    private static final Logger logger = LogManager.getLogger(ServerNetworkService.class);
    private static final Level MESSAGE = Level.forName("MESSAGE", 450);

    private final ServerInstance instance;
    private final RequestChannelHandler channelHandler;

    public ServerNetworkService(ServerInstance instance) {
        this.instance = instance;
        this.channelHandler = new RequestChannelHandler();
        this.channelHandler
                .addRequestTemplate("AUTHENTICATION", new RequestChannelHandler.Authentication(instance.getConnectedClients()))
                .addRequestTemplate("GET_SELF_USER", new RequestChannelHandler.GetSelfInfo())
                .addRequestTemplate("GET_ARTICLES", new RequestChannelHandler.GetMainPageArticles())

                // ADMIN
                .addRequestTemplate("ADMIN_ADD_USERS", new RequestChannelHandler.AdminAddUsers())
                .addRequestTemplate("ADMIN_GET_USERS", new RequestChannelHandler.AdminGetUsers())
                .addRequestTemplate("ADMIN_DELETE_USERS", new RequestChannelHandler.AdminDeleteUsers())

                .addRequestTemplate("ADMIN_ADD_SUBJECTS", new RequestChannelHandler.AdminAddSubjects())
                .addRequestTemplate("ADMIN_GET_SUBJECTS", new RequestChannelHandler.AdminGetSubjects())
                .addRequestTemplate("ADMIN_DELETE_SUBJECTS", new RequestChannelHandler.AdminDeleteSubjects())

                .addRequestTemplate("ADMIN_GET_STUDENTS", new RequestChannelHandler.AdminGetStudents())
                .addRequestTemplate("ADMIN_GET_TEACHERS", new RequestChannelHandler.AdminGetTeachers())

                .addRequestTemplate("ADMIN_ADD_STUDENT_TO_SUBJECT", new RequestChannelHandler.AdminAddUserToSubject())

                // TEACHER
                .addRequestTemplate("TEACHER_MAIN_PAGE", new RequestChannelHandler.TeacherMainPage())
                .addRequestTemplate("TEACHER_LOAD_SUBJECT", new RequestChannelHandler.TeacherMainPage.TeacherLoadSubject());
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new DelimiterBasedFrameDecoder(2097152, Delimiters.lineDelimiter()));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());

        pipeline.addLast(new SimpleChannelInboundHandler<String>() {

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                logger.info("New client connected with ip: {0}.".replace("{0}",
                        ctx.channel().remoteAddress().toString()));
                Client client = new Client();
                client.setAddress((InetSocketAddress) ctx.channel().remoteAddress());
                instance.getConnectedClients().add(client);
            }

            @Override
            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                logger.log(MESSAGE, channelHandlerContext.channel().remoteAddress().toString() + " says " + s);
                channelHandler.onMessage(channelHandlerContext, s);
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                ctx.close();
                Client client = getClientByRemoteAddress(ctx.channel().remoteAddress());
                instance.getConnectedClients().remove(client);
                logger.info("Client {0} disconnected. Reason: {1}."
                        .replace("{0}",
                                (client.isAuthenticated() ? client.getUser().getUsername()
                                        : ctx.channel().remoteAddress().toString()))
                        .replace("{1}", "The connection was closed by the remote host"));
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

                if (!socketChannel.isOpen()) {
                    ctx.close();
                    Client client = getClientByRemoteAddress(ctx.channel().remoteAddress());
                    instance.getConnectedClients().remove(client);
                    logger.error(cause.getMessage());
                }
            }
        });
    }

    public Client getClientByRemoteAddress(SocketAddress remoteAddress) throws NoSuchFieldException {
        for (Client client : instance.getConnectedClients()) {
            if (client.getAddress().equals(remoteAddress))
                return client;
        }
        throw new NoSuchFieldException("No client found with this address");
    }

    /**
     * The request system works like REST but without using HTTP. The procedure is
     * make up of one "question" and one "answer". Works the same for outbound and inbound
     * requestsTemplates.
     *
     * @author Radu
     */
    @NoArgsConstructor
    private class RequestChannelHandler {

        private Map<String, IRequest> requestsTemplates = new HashMap<>();
        private List<Packet> waitingOutboundPackets = new ArrayList<>();


        public RequestChannelHandler addRequestTemplate(String name, IRequest template) {
            requestsTemplates.put(name, template);
            return this;
        }

        public void onMessage(ChannelHandlerContext channelHandlerContext, String message) {
            try {
                Packet receivedPacket = new ObjectMapper()
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .readValue(message, Packet.class);
                receivedPacket.setChannelHandlerContext(channelHandlerContext);
                receivedPacket.setClient(getClientByRemoteAddress(channelHandlerContext.channel().remoteAddress()));

                if (receivedPacket.getRequestStatus()) {
                    for (Packet packet : waitingOutboundPackets) {
                        if (packet.getRequestId() == receivedPacket.getRequestId()) {
                            requestsTemplates.get(packet.getRequestName()).onAnswer(receivedPacket);
                            waitingOutboundPackets.remove(packet);
                            return;
                        }
                    }
                    logger.error("No request found with id provided.");
                    return;
                }

                if (requestsTemplates.get(receivedPacket.getRequestName()) == null) {
                    logger.error("Invalid request name: " + receivedPacket.getRequestName());
                    return;
                }

                requestsTemplates.get(receivedPacket.getRequestName()).onIncomingRequest(receivedPacket);

            } catch (JsonProcessingException | NoSuchFieldException e) {
                logger.error(e.getMessage());
            }
        }


        public ChannelFuture sendRequest(String name, ChannelHandlerContext ctx, Object[] params) throws IllegalArgumentException {
            if (requestsTemplates.get(name) == null)
                throw new IllegalArgumentException("No request template found with passed name + '" + name + "'");
            Packet packet = new Packet(name, ctx);
            waitingOutboundPackets.add(packet);
            return requestsTemplates.get(name).onNewRequest(packet, params);
        }

        public interface IRequest {

            /**
             * Called when {@link #sendRequest(String, ChannelHandlerContext, Object[])} is called.
             *
             * @return an {@link io.netty.channel.ChannelFuture} representing the async task.
             */
            default ChannelFuture onNewRequest(Packet packet, Object[] params) {
                return null;
            }

            default void onAnswer(Packet packet) {
                packet.sendError(Packet.PACKET_CODES.ERROR);
                logger.error("Unknown request.");
            }

            ChannelFuture onIncomingRequest(Packet packet);
        }


        public static class Authentication implements IRequest {

            private List<Client> connectedClients;

            public Authentication(List<Client> list) {
                this.connectedClients = list;
            }


            //todo: fix SQL injection
            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {
                String username = packet.getRequestContent().get("username").asText();
                String password = packet.getRequestContent().get("password").asText();
                Client client = packet.getClient();
                UserService userService = DatabaseManager.getInstance().getUserService();

                try {
                    User user = userService.findByUsername(username);

                    // Check if the user is already connected
                    for (Client connectedClient : connectedClients) {
                        if (connectedClient.getUser() != null && connectedClient.getUser().getUsername().equals(username)) {
                            return packet.sendError(Packet.PACKET_CODES.USER_IN_USE);
                        }
                    }

                    // Authenticate the user
                    if (user.getPassword().equals(password)) {
                        client.setAuthenticated(true);
                        client.setUser(user);
                        logger.info("Client '{0}' authenticated with username '{1}'"
                                .replace("{0}", client.getAddress().toString())
                                .replace("{1}", user.getUsername()));
                        return packet.sendSuccess();
                    } else {
                        return packet.sendError(Packet.PACKET_CODES.INVALID_PASSWORD);
                    }

                } catch (NoResultException e) {
                    return packet.sendError(Packet.PACKET_CODES.USER_NOT_FOUND);
                } catch (Exception e) {
                    logger.error(e);
                    return packet.sendError(Packet.PACKET_CODES.ERROR);
                }
            }
        }

        public static class GetSelfInfo implements IRequest {

            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {
                if (!packet.getClient().isAuthenticated()) {
                    packet.sendError(Packet.PACKET_CODES.NOT_AUTHENTICATED);
                    return null;
                }

                UserService userService = DatabaseManager.getInstance().getUserService();
                try {
                    User user = userService.findByUsername(packet.getClient().getUser().getUsername());
                    packet.setRequestContent(new JsonMapper().convertValue(user, JsonNode.class));
                    return packet.sendThis(true);

                } catch (NoResultException e) {
                    return packet.sendError(Packet.PACKET_CODES.USER_NOT_FOUND); // user does not exist

                } catch (Exception e) {
                    logger.error(e);
                    return packet.sendError(Packet.PACKET_CODES.ERROR);
                }
            }
        }

        @Deprecated
        public static class GetMainPageArticles implements IRequest {

            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {

                ArticleService service = DatabaseManager.getInstance().getArticleService();
                try {
                    ArrayNode array = new ObjectMapper().valueToTree(service.getAllArticles());

                    packet.setRequestContent(new ObjectMapper().createObjectNode().set("articles", array));

                    return packet.sendThis(true);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    return packet.sendError(Packet.PACKET_CODES.ERROR);
                }
            }
        }

        public static class AdminGetUsers implements IRequest {

            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {
                if (!packet.getClient().getUser().getType().equals(UserType.ADMIN)) {
                    packet.sendError(Packet.PACKET_CODES.ERROR);
                }
                UserService service = DatabaseManager.getInstance().getUserService();

                try {
                    List<User> allUsers = service.getAllUsers();

                    ObjectMapper mapper = new ObjectMapper();
                    ArrayNode array = mapper.valueToTree(allUsers);
                    JsonNode result = mapper.createObjectNode().set("users", array);

                    packet.setRequestContent(result);
                    return packet.sendThis(true);
                } catch (Exception e) {
                    logger.error(e);
                    return packet.sendError(Packet.PACKET_CODES.ERROR);
                }
            }
        }

        public static class AdminAddUsers implements IRequest {

            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {
                if (!packet.getClient().getUser().getType().equals(UserType.ADMIN)) {
                    packet.sendError(Packet.PACKET_CODES.ERROR);
                }
                UserService userService = DatabaseManager.getInstance().getUserService();

                User user = new User();
                JsonNode requestContent = packet.getRequestContent();
                user.setUsername(requestContent.get("username").asText());
                user.setPassword(requestContent.get("password").asText());
                user.setFirstName(requestContent.get("firstName").asText());
                user.setLastName(requestContent.get("lastName").asText());

                switch (requestContent.get("type").asText()) {
                    case "Admin" -> {
                        user.setType(UserType.ADMIN);
                    }
                    case "Teacher" -> {
                        user.setType(UserType.TEACHER);
                    }
                    default -> {
                        user.setType(UserType.STUDENT);
                    }
                }

                userService.add(user);

                logger.info("New user + '" + user.getUsername() + "' by '" + packet.getClient().getUser().getUsername() + "'.");
                return packet.sendSuccess();
            }
        }

        public static class AdminDeleteUsers implements IRequest {

            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {
                if (!packet.getClient().getUser().getType().equals(UserType.ADMIN)) {
                    packet.sendError(Packet.PACKET_CODES.ERROR);
                }
                UserService service = DatabaseManager.getInstance().getUserService();

                try {
                    long id = packet.getRequestContent().get("id").asLong();
                    User user = service.findById(id);
                    service.deleteById(id);

                    logger.info("User '" + user.getUsername() + "' with id '" + id + "' deleted by user '" + packet.getClient().getUser().getUsername() + "'.");
                    return packet.sendThis(true);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
                return packet.sendError(Packet.PACKET_CODES.ERROR);
            }
        }

        public static class AdminAddSubjects implements IRequest {

            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {
                if (!packet.getClient().getUser().getType().equals(UserType.ADMIN)) {
                    packet.sendError(Packet.PACKET_CODES.ERROR);
                }

                SubjectService subjectService = DatabaseManager.getInstance().getSubjectService();
                UserService userService = DatabaseManager.getInstance().getUserService();
                try {
                    String name = packet.getRequestContent().get("name").toString();
                    User teacher = userService.findById(packet.getRequestContent().get("teacher").asLong());
                    User clone = new User();
                    clone.setId(teacher.getId());
                    clone.setUsername(teacher.getUsername());
                    clone.setPassword(teacher.getPassword());
                    clone.setSubjects(teacher.getSubjects());
                    clone.setGrades(teacher.getGrades());
                    clone.setFirstName(teacher.getFirstName());
                    clone.setLastName(teacher.getLastName());

                    subjectService.add(new Subject(name, clone));
                    return packet.sendThis(true);
                } catch (Exception e) {
                    packet.sendError(Packet.PACKET_CODES.USER_NOT_FOUND);
                    logger.error(e.getMessage());
                }

                return packet.sendError(Packet.PACKET_CODES.ERROR);
            }
        }

        public static class AdminGetSubjects implements IRequest {

            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {
                System.out.println("dsadasdas");
                if (!packet.getClient().getUser().getType().equals(UserType.ADMIN)) {
                    packet.sendError(Packet.PACKET_CODES.ERROR);
                }
                SubjectService service = DatabaseManager.getInstance().getSubjectService();

                List<Subject> allSubjects = service.getAllSubjects();

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE); //turn off everything
                    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY); // only use fields
                    ArrayNode array = mapper.valueToTree(allSubjects);
                    JsonNode result = mapper.createObjectNode().set("subjects", array);

                    packet.setRequestContent(result);
                    return packet.sendThis(true);
                } catch (Exception e) {
                    logger.error(e);
                    return packet.sendError(Packet.PACKET_CODES.ERROR);
                }
            }
        }

        private static class AdminDeleteSubjects implements IRequest {

            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {
                if (!packet.getClient().getUser().getType().equals(UserType.ADMIN)) {
                    packet.sendError(Packet.PACKET_CODES.ERROR);
                }
                SubjectService service = DatabaseManager.getInstance().getSubjectService();

                try {
                    long id = packet.getRequestContent().get("id").asLong();
                    Subject subject = service.findById(id);
                    service.deleteById(id);

                    logger.info("User '" + subject.getName() + "' with id '" + id + "' deleted by user '" + packet.getClient().getUser().getUsername() + "'.");
                    return packet.sendThis(true);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
                return packet.sendError(Packet.PACKET_CODES.ERROR);
            }
        }

        public static class AdminGetTeachers implements IRequest {

            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {
                if (!packet.getClient().getUser().getType().equals(UserType.ADMIN)) {
                    packet.sendError(Packet.PACKET_CODES.ERROR);
                }
                UserService service = DatabaseManager.getInstance().getUserService();

                try {
                    List<User> allUsers = service.getAllUsers();

                    // Filter users of type TEACHERS
                    List<User> teachers = allUsers.stream()
                            .filter(user -> user.getType() == UserType.TEACHER).toList();

                    ObjectMapper mapper = new ObjectMapper();
                    ArrayNode array = mapper.valueToTree(teachers);
                    JsonNode result = mapper.createObjectNode().set("teachers", array);

                    for (JsonNode node : result.get("teachers")) {
                        ((ObjectNode) node).remove("subjects");
                        ((ObjectNode) node).remove("grades");
                        ((ObjectNode) node).remove("password");
                    }

                    packet.setRequestContent(result);
                    return packet.sendThis(true);
                } catch (Exception e) {
                    logger.error(e);
                    return packet.sendError(Packet.PACKET_CODES.ERROR);
                }
            }
        }

        public static class AdminGetStudents implements IRequest {

            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {
                if (!packet.getClient().getUser().getType().equals(UserType.ADMIN)) {
                    packet.sendError(Packet.PACKET_CODES.ERROR);
                }
                UserService userService = DatabaseManager.getInstance().getUserService();
                SubjectService subjectService = DatabaseManager.getInstance().getSubjectService();

                try {
                    List<User> users = userService.getAllUsers();

                    long subjectId = packet.getRequestContent().get("subject").asLong();

                    // remove all students that are already enrolled to the subject + the teacher
                    users.removeAll(subjectService.findById(subjectId).getUsers());
                    users = users.stream().filter(user -> user.getType() == UserType.STUDENT).toList();


                    ObjectMapper mapper = new ObjectMapper();
                    ArrayNode array = mapper.valueToTree(users);
                    JsonNode result = mapper.createObjectNode().set("students", array);

                    for (JsonNode node : result.get("students")) {
                        ((ObjectNode) node).remove("subjects");
                        ((ObjectNode) node).remove("grades");
                        ((ObjectNode) node).remove("password");
                    }

                    packet.setRequestContent(result);
                    return packet.sendThis(true);
                } catch (Exception e) {
                    logger.error(e);
                    return packet.sendError(Packet.PACKET_CODES.ERROR);
                }
            }
        }

        public static class AdminAddUserToSubject implements IRequest {

            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {
                UserService userService = DatabaseManager.getInstance().getUserService();
                SubjectService subjectService = DatabaseManager.getInstance().getSubjectService();

                try {
                    User user = userService.findById(packet.getRequestContent().get("userId").asLong());
                    Subject subject = subjectService.findById(packet.getRequestContent().get("subjectId").asLong());

                    subject.addUser(user);
                    subjectService.update(subject);

                    return packet.sendThis(true);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    return packet.sendError(Packet.PACKET_CODES.ERROR);
                }

            }
        }

        public static class TeacherMainPage implements IRequest {

            @Override
            public ChannelFuture onIncomingRequest(Packet packet) {
                if (!packet.getClient().getUser().getType().equals(UserType.TEACHER)) {
                    packet.sendError(Packet.PACKET_CODES.ERROR);
                }
                UserService userService = DatabaseManager.getInstance().getUserService();
                SubjectService subjectService = DatabaseManager.getInstance().getSubjectService();

                try {

                    List<Subject> allSubjects = subjectService.getAllSubjects();
                    List<Subject> usersSubject = new ArrayList<>();

                    User user = userService.findById(packet.getClient().getUser().getId());

                    for (Subject subject : allSubjects) {
                        if (subject.getUsers().contains(user))
                            usersSubject.add(subject);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE); //turn off everything
                    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY); // only use fields
                    ArrayNode array = mapper.valueToTree(usersSubject);
                    JsonNode result = mapper.createObjectNode().set("subjects", array);

                    packet.setRequestContent(result);
                    return packet.sendThis(true);
                } catch (Exception e) {
                    logger.error(e);
                    return packet.sendError(Packet.PACKET_CODES.ERROR);
                }
            }

            public static class TeacherLoadSubject implements IRequest {

                @Override
                public ChannelFuture onIncomingRequest(Packet packet) {
                    if (!packet.getClient().getUser().getType().equals(UserType.TEACHER)) {
                        packet.sendError(Packet.PACKET_CODES.ERROR);
                    }

                    SubjectService service = DatabaseManager.getInstance().getSubjectService();

                    long id = packet.getRequestContent().get("id").asLong();

                    try {
                        Subject subject = service.findById(id);
                        JsonNode result = new ObjectMapper().valueToTree(subject);
                        packet.setRequestContent(result);
                        return packet.sendThis(true);
                    } catch (Exception e) {
                        logger.error(e);
                        return packet.sendError(Packet.PACKET_CODES.ERROR);
                    }
                }
            }
        }
    }
}
