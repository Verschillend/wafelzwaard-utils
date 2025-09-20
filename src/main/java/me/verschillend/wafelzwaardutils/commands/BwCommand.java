package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import me.verschillend.wafelzwaardutils.gui.BwGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BwCommand implements CommandExecutor {

    private final Wafelzwaardutils plugin;
    private final BwGUI bwGUI;

    public BwCommand(Wafelzwaardutils plugin, BwGUI bwGUI) {
        this.plugin = plugin;
        this.bwGUI = bwGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("wafelzwaard.bw")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        boolean lobby = plugin.getConfig().getBoolean("server.lobby", false);
        if (!lobby) {
            player.sendMessage("§cThis command is not available on this server! Please contact a administrator if you believe this is a error!");
            return true;
        }

        bwGUI.openGUI(player);
        return true;
    }
}