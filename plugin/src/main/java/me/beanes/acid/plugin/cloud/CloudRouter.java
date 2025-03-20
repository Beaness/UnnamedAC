package me.beanes.acid.plugin.cloud;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.cloud.packet.Packet;
import me.beanes.acid.plugin.cloud.packet.impl.PingPacket;
import me.beanes.acid.plugin.cloud.packet.impl.auth.LoginRequestResponse;
import me.beanes.acid.plugin.cloud.packet.impl.player.PlayerMitigateDamagePacket;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CloudRouter extends SimpleChannelInboundHandler<Packet> {
    private boolean authenticated = false;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet rawPacket) throws Exception {
        if (rawPacket instanceof LoginRequestResponse) {
            LoginRequestResponse packet = (LoginRequestResponse) rawPacket;

            if (packet.getResponse() == LoginRequestResponse.Response.OUTDATED) {
                Acid.get().getLogger().warning("The plugin is too old to connect to the cloud server. Please reboot your server to load the latest version.");
                Acid.get().getCloudManager().disableReconnecting();
                ctx.close();
            } else if (packet.getResponse() == LoginRequestResponse.Response.MAXIMUM_CONNECTIONS) {
                Acid.get().getLogger().warning("The plugin was unable to connect to the cloud as you are on your connection limit.");
                ctx.close();
            } else if (packet.getResponse() == LoginRequestResponse.Response.INVALID_ID) {
                Bukkit.shutdown();
            } else if (packet.getResponse() == LoginRequestResponse.Response.SUCCESS) {
                Acid.get().getLogger().info("Successfully authenticated with the cloud server!");
                Acid.get().getCloudManager().setAuthenticated();
            }
        }

        if (Acid.get().getCloudManager().isAuthenticated()) {
            this.handlePacket(rawPacket);
        }
    }

    public void handlePacket(Packet rawPacket) {
        if (rawPacket instanceof PingPacket) {
            Acid.get().getCloudManager().receivePong();;
        } else if (rawPacket instanceof PlayerMitigateDamagePacket) {
            Acid.get().getMitigationManager().handleDamageMitigatePacket((PlayerMitigateDamagePacket) rawPacket);
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        Acid.get().getLogger().warning("The cloud has disconnected" );
        ctx.channel().eventLoop().schedule(() -> {
            Acid.get().getCloudManager().attemptConnect();
        }, CloudManager.CLOUD_RECONNECT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            Acid.get().getLogger().warning("The cloud has timed out");
        } else if (cause instanceof IOException) {
            Acid.get().getLogger().warning("The cloud has forcefully closed the connection");
        } else {
            Acid.get().getLogger().log(Level.SEVERE, "An error has occured in the cloud pipeline", cause);
        }

    }
}