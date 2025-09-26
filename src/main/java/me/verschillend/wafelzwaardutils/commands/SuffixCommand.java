package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.gui.SuffixGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SuffixCommand implements CommandExecutor {

    private final SuffixGUI suffixGUI;

    public SuffixCommand(SuffixGUI suffixGUI) {
        this.suffixGUI = suffixGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("wafelzwaardutils.suffix")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        suffixGUI.openGUI(player);
        return true;
    }
}
