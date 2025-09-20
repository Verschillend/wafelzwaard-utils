package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.gui.CCGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatColorCommand implements CommandExecutor {

    private final CCGUI ccGUI;

    public ChatColorCommand(CCGUI ccGUI) {
        this.ccGUI = ccGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("wafelzwaard.cc")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        ccGUI.openGUI(player);
        return true;
    }
}