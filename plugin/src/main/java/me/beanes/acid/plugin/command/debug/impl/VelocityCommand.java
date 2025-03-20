package me.beanes.acid.plugin.command.debug.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.command.debug.SimpleCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Random;

public class VelocityCommand implements SimpleCommand {

    @Override
    public void onExecute(CommandSender commandSender, String[] args) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(commandSender);
        Random random = new Random();

        Bukkit.getScheduler().runTaskLater(Acid.get(), () -> {
            double rX = Double.parseDouble(args[0]);
            double rY = Double.parseDouble(args[1]);
            double rZ = Double.parseDouble(args[2]);

            user.sendPacket(new WrapperPlayServerEntityVelocity(user.getEntityId(), new Vector3d(rX, rY, rZ)));
        }, 4 * 20L);

        /* Bukkit.getScheduler().runTaskTimer(Acid.get(), () -> {
            Bukkit.broadcastMessage("fuck yea!");

            short randomXShort = (short) random.nextInt(32768);
            double velX = (double) randomXShort / 8000.0;
            short randomYShort = (short) random.nextInt(32768);
            double velY = (double) randomYShort / 8000.0;
            short randomZShort = (short) random.nextInt(32768);
            double velZ = (double) randomZShort / 8000.0;


            double rX = velX * (random.nextBoolean() ? -1 : 1);
            double rY = velY * (random.nextBoolean() ? -1 : 1);
            double rZ = velZ * (random.nextBoolean() ? -1 : 1);

            // User user = PacketEvents.getAPI().getPlayerManager().getUser(commandSender);
            // user.sendPacket(new WrapperPlayServerEntityMetadata(user.getEntityId(), data));

            user.sendPacket(new WrapperPlayServerEntityVelocity(user.getEntityId(), new Vector3d(rX, rY, rZ)));
        }, 1L, 1L); *.



        /* Bukkit.getScheduler().runTaskTimer(Acid.get(), () -> {
            Bukkit.broadcastMessage("fuck yea!");

            double rX = random.nextDouble() * random.nextInt(5) * (random.nextBoolean() ? -1 : 1);
            double rY = random.nextDouble() * random.nextInt(2) * (random.nextBoolean() ? -1 : 1);
            double rZ = random.nextDouble() * random.nextInt(5) * (random.nextBoolean() ? -1 : 1);

            // User user = PacketEvents.getAPI().getPlayerManager().getUser(commandSender);
            // user.sendPacket(new WrapperPlayServerEntityMetadata(user.getEntityId(), data));

            user.sendPacket(new WrapperPlayServerEntityVelocity(user.getEntityId(), new Vector3d(rX, rY, rZ)));
        }, 1L, 1L); */
    }
}
