package me.beanes.acid.plugin.command;

import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.command.debug.DebugCommandHandler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class MainCommand implements CommandExecutor {
    private static final Component CHAT_LINE = Component.text("  » " + "                                                            " + " « ", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH);

    private final DebugCommandHandler debugCommandHandler;

    public MainCommand() {
        if (Acid.DEBUG) {
            debugCommandHandler = new DebugCommandHandler();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        Audience audience = Acid.get().getAdventure().sender(sender);

        if (args.length >= 1) {
            String subcommand = args[0];

            switch (subcommand) {
                case "cloud":
                    if (sender.hasPermission("acid.cloud")) {
                        runCloudCommand(audience);
                    } else {
                        sendInfoMessage(audience);
                    }
                    break;
                case "alerts":
                    if (args.length == 2 && args[1].toLowerCase().equals("simulation")) {
                        runSimulationAlertsCommand(sender, audience);
                        break;
                    }
                    runAlertsCommand(sender, audience);
                    break;
                case "logs":
                    if (sender.hasPermission("acid.logs")) {
                        runLogsCommand(audience);
                    } else {
                        sendInfoMessage(audience);
                    }
                    break;
                case "debug":
                    if (Acid.DEBUG) {
                        if (args.length <= 1) {
                            sender.sendMessage("Please provide a debug command");
                            return true;
                        }

                        String[] newArgs = Arrays.copyOfRange(args, 2, args.length);
                        debugCommandHandler.handleCommand(sender, args[1], newArgs);
                    } else {
                        sendInfoMessage(audience);
                    }
                    break;
                default:
                    sendInfoMessage(audience);
                    break;
            }
        } else {
            sendInfoMessage(audience);
        }

        return true;

        /* int start = Math.min((Math.max(Integer.parseInt(args[0]), 1) - 1), (0xFFFF / 100)) * 100;
        int end = Math.min(start + 100, 0xFFFF);

        TextComponent.Builder builder = Component.text();

        builder.append(CHAT_LINE);
        builder.appendNewline();

        for (int i = start; i <= end; i++) {
            char c = (char) i;
            builder.append(Component.text(c + " ", NamedTextColor.GRAY)
                    .hoverEvent(HoverEvent.showText(Component.text(c, NamedTextColor.GRAY, TextDecoration.BOLD)))
                    .clickEvent(ClickEvent.suggestCommand(String.valueOf(c))));
            if (i % 15 == 0) {
                builder.appendNewline();
            }
        }

        builder.appendNewline();
        builder.append(CHAT_LINE);

        audience.sendMessage(builder.build()); */
    }

    private static final Component INFO =
            Component.text("Acid is an anticheat made by ", NamedTextColor.YELLOW)
                    .append(Component.text("Beanes", NamedTextColor.WHITE))
                    .append(Component.text(" and ", NamedTextColor.YELLOW))
                    .append(Component.text("xEcho1337", NamedTextColor.WHITE))
                    .append(Component.text(".", NamedTextColor.YELLOW));

    private static final Component DISCORD =
            Component.text("Join our discord at ", NamedTextColor.YELLOW)
                    .append(Component.text("✒ discord.gg/soon", NamedTextColor.WHITE));

    public void sendInfoMessage(Audience audience) {
        audience.sendMessage(CHAT_LINE);
        audience.sendMessage(INFO);
        audience.sendMessage(DISCORD);
        audience.sendMessage(CHAT_LINE);
    }

    private static final Component CLOUD_STATUS = Component.text("Cloud status: ", NamedTextColor.YELLOW);
    private static final Component CLOUD_LATENCY = Component.text("Cloud latency: ", NamedTextColor.YELLOW);
    private static final Component CLOUD_HOVER = Component.text("Do not worry if the ping is a high number, all checks that require instant mitigation are ran locally", NamedTextColor.GRAY);
    private static final Component CLOUD_UP = Component.text("✈ Connected", NamedTextColor.GREEN);
    private static final Component CLOUD_DOWN = Component.text("⚠ Down", NamedTextColor.RED);

    public void runCloudCommand(Audience audience) {
        boolean connected = Acid.get().getCloudManager().isConnected();

        audience.sendMessage(CHAT_LINE);
        audience.sendMessage(CLOUD_STATUS.append(connected ? CLOUD_UP : CLOUD_DOWN));
        if (connected) {
            audience.sendMessage(CLOUD_LATENCY
                    .append(Component.text(Acid.get().getCloudManager().getPing() + " ms", NamedTextColor.GREEN))
                    .hoverEvent(HoverEvent.showText(CLOUD_HOVER))
            );
        }

        audience.sendMessage(CHAT_LINE);
    }

    private static final Component LOGS_INFO = Component.text("As of right now the logs command is not implemented. The anticheat is made to automatically mitigate cheaters.", NamedTextColor.YELLOW);

    public void runLogsCommand(Audience audience) {
        audience.sendMessage(CHAT_LINE);
        audience.sendMessage(LOGS_INFO);
        audience.sendMessage(CHAT_LINE);
    }

    private static final Component ENABLED_ALERTS = Component.text("✔", NamedTextColor.DARK_GREEN)
            .append(Component.text(" » ", NamedTextColor.GRAY))
            .append(Component.text("You have enabled alerts.", NamedTextColor.GREEN));

    private static final Component ENABLED_SIMULATION_ALERTS = Component.text("✔", NamedTextColor.DARK_GREEN)
            .append(Component.text(" » ", NamedTextColor.GRAY))
            .append(Component.text("You have enabled simulation alerts.", NamedTextColor.GREEN));

    private static final Component DISABLED_ALERTS = Component.text("✕", NamedTextColor.DARK_RED)
            .append(Component.text(" » ", NamedTextColor.GRAY))
            .append(Component.text("You have disabled alerts.", NamedTextColor.RED));

    private static final Component DISABLE_SIMULATION_ALERTS = Component.text("✕", NamedTextColor.DARK_RED)
            .append(Component.text(" » ", NamedTextColor.GRAY))
            .append(Component.text("You have disabled simulation alerts.", NamedTextColor.RED));

    public void runAlertsCommand(CommandSender sender, Audience audience) {
        UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : Acid.CONSOLE_UUID;

        if (Acid.get().getAlertsManager().hasAlertsEnabled(uuid)) {
            Acid.get().getAlertsManager().disableAlerts(uuid);
            audience.sendMessage(DISABLED_ALERTS);
        } else {
            if (sender.hasPermission("acid.alerts")) {
                Acid.get().getAlertsManager().enableAlerts(uuid);
                audience.sendMessage(ENABLED_ALERTS);
            } else {
                sendInfoMessage(audience);
            }
        }
    }

    public void runSimulationAlertsCommand(CommandSender sender, Audience audience) {
        UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : Acid.CONSOLE_UUID;

        if (Acid.get().getAlertsManager().hasSimulationAlertsEnabled(uuid)) {
            Acid.get().getAlertsManager().disableSimulationAlerts(uuid);
            audience.sendMessage(DISABLE_SIMULATION_ALERTS);
        } else {
            if (sender.hasPermission("acid.alerts")) {
                Acid.get().getAlertsManager().enableSimulationAlerts(uuid);
                audience.sendMessage(ENABLED_SIMULATION_ALERTS);
            } else {
                sendInfoMessage(audience);
            }
        }
    }
}
