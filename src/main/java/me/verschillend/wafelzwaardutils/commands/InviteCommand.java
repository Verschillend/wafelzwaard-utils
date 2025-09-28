package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InviteCommand implements CommandExecutor {

    private final Wafelzwaardutils plugin;

    public InviteCommand(Wafelzwaardutils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wafelzwaardutils.oneblock.invite")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can run this command!");
            return true;
        }
        if (args.length > 0) {
            Bukkit.dispatchCommand(sender, "ob kick " + args[0]);
            return true;
        }

        return true;
    }
}
