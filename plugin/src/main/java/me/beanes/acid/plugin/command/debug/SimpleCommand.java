package me.beanes.acid.plugin.command.debug;

import org.bukkit.command.CommandSender;

public interface SimpleCommand {
    void onExecute(CommandSender commandSender, String[] args);
}
