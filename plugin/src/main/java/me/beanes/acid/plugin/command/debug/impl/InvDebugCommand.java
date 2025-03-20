package me.beanes.acid.plugin.command.debug.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.player.User;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.command.debug.SimpleCommand;
import org.bukkit.command.CommandSender;

public class InvDebugCommand implements SimpleCommand {

    @Override
    public void onExecute(CommandSender commandSender, String[] args) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(commandSender);

        ChannelHelper.runInEventLoop(user.getChannel(), () -> {
            Acid.get().getPlayerManager().get(user).getInventoryTracker().sendInventory(args.length > 0);
        });
    }
}
