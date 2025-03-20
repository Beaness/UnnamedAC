package me.beanes.acid.plugin.command.debug;

import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.command.debug.impl.*;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class DebugCommandHandler {
    private Map<String, SimpleCommand> commands;

    public DebugCommandHandler() {
        if (Acid.DEBUG) {
            this.commands = new HashMap<>();
            register("fakeplayer", new FakePlayerDebugCommand());
            register("velocity", new VelocityCommand());
            register("chunkdebug", new ChunkDebugCommand());
            register("blockstate", new BlockStateDebugCommand());
            register("inv", new InvDebugCommand());
        }
    }

    public void handleCommand(CommandSender sender, String cmd, String[] args) {
        SimpleCommand command = commands.get(cmd);

        if (command == null) {
            sender.sendMessage("No debug command found");
            return;
        }

        command.onExecute(sender, args);
    }

    private void register(String cmd, SimpleCommand command) {
        this.commands.put(cmd, command);
    }
}
