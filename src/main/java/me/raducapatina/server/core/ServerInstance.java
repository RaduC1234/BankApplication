package me.raducapatina.server.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.raducapatina.server.command.StopCommand;
import me.raducapatina.server.command.UserCommand;
import me.raducapatina.server.command.core.CommandHandler;
import me.raducapatina.server.util.Log;
import me.raducapatina.server.util.ResourceServerMessages;
import me.raducapatina.server.util.ResourceServerProperties;

import java.util.ArrayList;
import java.util.List;

public class ServerInstance {

    private final int port;
    private volatile List<Client> connectedClients;
    private CommandHandler commandHandler;

    public ServerInstance() {
        this.port = Integer.parseInt(ResourceServerProperties.getInstance().getObject("port").toString());
        this.connectedClients = new ArrayList<>(0);
    }

    public void start() {

        Log.info(ResourceServerMessages.getObjectAsString("core.startingServer").replace("{0}", String.valueOf(port)));

        commandHandler = new CommandHandler()
                .addCommand(new UserCommand())
                //.addCommand(new LoginCommand())
                //.addCommand(new BalanceCommand())
                .addCommand(new StopCommand(this));
        //.addCommand(new DisconnectCommand())

        commandHandler.listen();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerChannelHandler(this))
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            Log.info(ResourceServerMessages.getObjectAsString("core.finishedLoading"));
            b.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            stop();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stop() {
        Log.info(ResourceServerMessages.getObjectAsString("core.stoppingServer")); // Stopping server...
        commandHandler.stop();
        System.exit(0);
    }

    public List<Client> getConnectedClients() {
        return connectedClients;
    }

}
