package me.beanes.acid.plugin.cloud;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.cloud.packet.Packet;
import me.beanes.acid.plugin.cloud.packet.impl.PingPacket;
import me.beanes.acid.plugin.cloud.packet.impl.auth.LoginRequest;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

public class CloudManager {
    public static int CLOUD_RECONNECT_SECONDS = 5;
    @Getter
    private boolean authenticated = false;
    private boolean reconnect = true;
    private final Bootstrap bootstrap;
    private Channel channel;
    private long pingTime;
    @Getter
    private int ping;

    public CloudManager() {
        boolean isEpollAvailable = Epoll.isAvailable();

        EventLoopGroup workerGroupOther = isEpollAvailable ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroupOther)
                .channel(isEpollAvailable ? EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new CloudInitializer());

        this.attemptConnect();

        Bukkit.getScheduler().runTaskTimerAsynchronously(Acid.get(), this::ping, 20L * 5, 20L * 15);
    }

    public void disableReconnecting() {
        this.reconnect = false;
    }

    public void setAuthenticated() {
        this.authenticated = true;
    }

    public void ping() {
        pingTime = System.currentTimeMillis();
        sendPacket(new PingPacket());
    }

    public void receivePong() {
        this.ping = (int) (System.currentTimeMillis() - pingTime);
    }
    public void attemptConnect() {
        this.authenticated = false;

        if (!reconnect) {
            return;
        }

        bootstrap.connect("0.0.0.0", 3333).addListener((ChannelFuture f) -> {
            if (!f.isSuccess()) {
                Acid.get().getLogger().warning("Cloud connection failed, an attempt to reconnect will happen in " + CLOUD_RECONNECT_SECONDS + " seconds.");

                f.channel().eventLoop().schedule(this::attemptConnect, CLOUD_RECONNECT_SECONDS, TimeUnit.SECONDS);
            } else {
                channel = f.channel();
                Acid.get().getLogger().info("Connected to the cloud server, attempting to authenticate");

                f.channel().writeAndFlush(new LoginRequest("randomApikey", "1.7.10"));
            }
        });
    }

    public boolean isConnected() {
        if (!authenticated) {
            return false;
        }

        if (this.channel == null) {
            return false;
        }

        return this.channel.isOpen() && this.channel.isActive();
    }

    public void sendPacket(Packet packet) {
        if (isConnected()) {
            this.channel.eventLoop().execute(() -> {
                this.channel.writeAndFlush(packet);
            });
        }
    }
}
