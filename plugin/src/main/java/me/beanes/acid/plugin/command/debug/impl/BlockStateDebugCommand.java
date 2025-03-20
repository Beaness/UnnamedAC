package me.beanes.acid.plugin.command.debug.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.command.debug.SimpleCommand;
import me.beanes.acid.plugin.player.PlayerData;
import org.bukkit.command.CommandSender;

public class BlockStateDebugCommand implements SimpleCommand {
    @Override
    public void onExecute(CommandSender commandSender, String[] args) {
        if (args.length >= 3) {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);

            PlayerData data = Acid.get().getPlayerManager().get(PacketEvents.getAPI().getPlayerManager().getUser(commandSender));

            WrappedBlockState state = data.getWorldTracker().getBlock(x, y, z).getValue();

            commandSender.sendMessage("State = " + state);
        } else {
            commandSender.sendMessage("Need 3 int arguments");
        }
    }
}
