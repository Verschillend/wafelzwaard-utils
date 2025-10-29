package me.verschillend.wafelzwaardutils.commands;

import me.verschillend.wafelzwaardutils.Wafelzwaardutils;
import me.verschillend.wafelzwaardutils.gui.BlackjackGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlackjackCommand implements CommandExecutor {

    private final Wafelzwaardutils plugin;
    private final BlackjackGUI blackjackGUI;

    public BlackjackCommand(Wafelzwaardutils plugin, BlackjackGUI blackjackGUI) {
        this.plugin = plugin;
        this.blackjackGUI = blackjackGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!player.hasPermission("wafelzwaardutils.blackjack")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        // check if lobby server
        if (!plugin.getConfig().getBoolean("server.lobby", false)) {
            player.sendMessage("§cBlackjack is not available on this server!");
            return true;
        }

        blackjackGUI.openBetGUI(player);
        return true;
    }
}
