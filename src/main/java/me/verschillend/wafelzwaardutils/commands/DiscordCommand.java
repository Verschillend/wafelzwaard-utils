package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscordCommand implements CommandExecutor {

    private final Wafelzwaardutils plugin;

    public DiscordCommand(Wafelzwaardutils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wafelzwaardutils.discord")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can run this command!");
            return true;
        }
        String link = plugin.getConfig().getString("server.discord-link", "");
        if (!link.isEmpty()) {
            Component message = MiniMessage.miniMessage().deserialize(
                    "<blue><u><click:open_url:" + link + "><hover:show_text:'Click to join the Discord'>" + link + "</hover></click></u></blue>"
            );
            player.sendMessage(message);
        }
        else {
            sender.sendMessage("§cThis command is not set up on this server! Please contact a administrator if you believe this is a error!");
        }

        return true;
    }
}
