package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import me.verschillend.wafelzwaardutils.gui.GemShopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GemShopCommand implements CommandExecutor {

    private final Wafelzwaardutils plugin;
    private final GemShopGUI gemShopGUI;

    public GemShopCommand(Wafelzwaardutils plugin, GemShopGUI gemShopGUI) {
        this.plugin = plugin;
        this.gemShopGUI = gemShopGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("wafelzwaardutils.gemshop")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        gemShopGUI.openMainGUI(player);
        return true;
    }
}