package me.beanes.acid.plugin.command.debug.impl;

import com.github.retrooper.packetevents.PacketEvents;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.command.debug.SimpleCommand;
import me.beanes.acid.plugin.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class ResyncCommand implements SimpleCommand {
    public static boolean DEBUG_WORLD = false;

    @Override
    public void onExecute(CommandSender commandSender, String[] args) {
        if (args.length == 1) {
            DEBUG_WORLD = !DEBUG_WORLD;
        }

        if (args.length >= 3) {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);

            PlayerData data = Acid.get().getPlayerManager().get(PacketEvents.getAPI().getPlayerManager().getUser(commandSender));
            data.getWorldTracker().getResyncHandler().scheduleResync(x - 5, y - 5, z - 5, x + 5, y + 5, z + 5);

            commandSender.sendMessage("Aye aye resync going!! ");
        }
    }
}
