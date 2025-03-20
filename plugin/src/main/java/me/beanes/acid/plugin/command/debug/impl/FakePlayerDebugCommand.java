package me.beanes.acid.plugin.command.debug.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.npc.NPC;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.command.debug.SimpleCommand;
import me.beanes.acid.plugin.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakePlayerDebugCommand implements SimpleCommand {

    @Override
    public void onExecute(CommandSender commandSender, String[] args) {
        NPC npc = new NPC(new UserProfile(UUID.fromString("fef3a1e3-2c55-4fa1-9519-045b2d9582b6"),"cohadaddy"), Integer.MAX_VALUE);

        Player player = (Player) commandSender;

        npc.setLocation(new Location(new Vector3d(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()), player.getLocation().getYaw(), player.getLocation().getPitch()));

        Object channel = PacketEvents.getAPI().getPlayerManager().getUser(commandSender).getChannel();

        User user = PacketEvents.getAPI().getPlayerManager().getUser(commandSender);

        // List<EntityData> list = new ArrayList<>();
        // list.add(new EntityData(0, EntityDataTypes.BYTE, (byte) (0x20)));
        // list.add(new EntityData(1, EntityDataTypes.SHORT, Short.MIN_VALUE));
        // list.add(new EntityData(17, EntityDataTypes.FLOAT, -20.0F));
        // list.add(new EntityData(8, EntityDataTypes.BYTE, Byte.MAX_VALUE));

        ChannelHelper.runInEventLoop(channel, () -> {
            npc.spawn(channel);

            /* int minX = player.getLocation().getBlockX() - 5;
            int minY = player.getLocation().getBlockY() - 5;
            int minZ = player.getLocation().getBlockZ() - 5;
            int maxX = player.getLocation().getBlockX() + 5;
            int maxY = player.getLocation().getBlockY() + 5;
            int maxZ = player.getLocation().getBlockZ() + 5;

            List<Vector3i> locs = new ArrayList<>();

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        locs.add(new Vector3i(x, y, z));
                    }
                }
            }

            user.sendPacket(new WrapperPlayServerExplosion(
                    new Vector3d(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()),
                    1.0F,
                    locs,
                    new Vector3f(0, 0,  0)
            )); */

            // user.sendPacketSilently(new WrapperPlayServerUseBed(npc.getId(), new Vector3i((int) npc.getLocation().getX(), (int) npc.getLocation().getY(), (int) npc.getLocation().getZ())));

            // user.sendPacketSilently(new WrapperPlayServerEntityMetadata(user.getEntityId(), list));
        });

        PlayerData data = Acid.get().getPlayerManager().get(user);

        Bukkit.getScheduler().runTaskLater(Acid.get(), () -> {
            // boolean test = data.getPositionTracker().getX();
            Location location = new Location(data.getPositionTracker().getX(), data.getPositionTracker().getY(), data.getPositionTracker().getZ(), data.getRotationTracker().getYaw(), data.getRotationTracker().getPitch());

            user.sendPacket(new WrapperPlayServerEntityTeleport(npc.getId(), location, false));
        }, 20L * 5);
    }
}
