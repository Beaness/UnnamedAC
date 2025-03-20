package me.beanes.acid.plugin.command.debug.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.command.debug.SimpleCommand;
import me.beanes.acid.plugin.player.PlayerData;
import org.bukkit.command.CommandSender;

public class ChunkDebugCommand implements SimpleCommand {
    @Override
    public void onExecute(CommandSender commandSender, String[] args) {
        PlayerData data = Acid.get().getPlayerManager().get(PacketEvents.getAPI().getPlayerManager().getUser(commandSender));

        ChannelHelper.runInEventLoop(data.getUser().getChannel(), () -> {
            data.getWorldTracker().debug();
        });

        commandSender.sendMessage("aight");
    }
}
